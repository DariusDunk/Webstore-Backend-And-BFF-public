// noinspection DuplicatedCode

import express from 'express';
const router = express.Router();
import {Backend_Url, WEB_CLIENT_NAME} from './config.js';
const AuthURL = `${Backend_Url}/auth`;
import sessionCache from '../services/sessionCache.js';
import {fetchWithSessionTokens} from "../services/requestTokenManager.js";
import axiosBackendClient from '../axiosBackendClient.js';
import axios from 'axios';
import {getCartSummary} from "../services/cartSummaryFetcher.js"
import {sessionHeaderBuilder} from "../services/sessionHeaderBuilder.js";

const timestamp = () => {
    const now = new Date();
    return `[${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')} ${String(now.getHours()).padStart(2, '0')}-${String(now.getMinutes()).padStart(2, '0')}-${String(now.getSeconds()).padStart(2, '0')}]`;
};

router.post(`/forgotten-password/:email`, async (req, res) => {
        const {email} = req.params;
        // const sessionId = req.cookies.session_id;
        const sessionId = req.headers["x-session-id"];

        try {
            const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
                    return await axiosBackendClient.post(`${Backend_Url}/auth/forgotten-password/${email}`, {},
                        {
                            headers:
                                {
                                    'Content-Type': 'application/json',
                                    'x-client_type': WEB_CLIENT_NAME,
                                    ...(sessionData?.access_token && {'Authorization': 'Bearer ' + sessionData.access_token}),
                                    ...(sessionData.session_id && {'x-session-id': sessionId})
                                },
                            bffContext: {
                                req, res
                            }
                        });
                },
                {req, res});

            return res.status(response.status).end();

        } catch (error) {

            if (error.response) {
                console.warn(`${timestamp()} Handled backend error for forgotten password`);


                const responseData = error.response.data;
                if (responseData != null) {
                    console.log("Error in forgotten password: ", responseData);

                    return res.status(error.response.status).json(responseData);
                }
            }

            console.error('-------------------Unexpected error in forgotten password-------------------\n', error);
            return res.status(500).end();
        }


    }
);


router.post(`/register`, async (req, res) => {

    const {name, familyName, email, password} = req.body;

    try {
        const response = await axios.post(`${AuthURL}/register`, {
            first_name: name,
            last_name: familyName,
            email: email,
            password: password
        });

        const responseData = response.data;

        if (responseData != null) {
            return res.status(response.status).json(responseData);
        }

        return res.status(response.status).end();
    } catch (error) {
        if (error.response) {
            const responseData = error.response.data;
            if (responseData != null) {
                return res.status(error.response.status).json(responseData);
            }
            return res.status(error.response.status).end();
        }
        console.error('Error with registration: ', error);
        return res.status(error.response?.status || 500).json({error: 'Internal server error'});
    }
})


router.post(`/login`, async (req, res) => {
    const {email, password, rememberMe = false} = req.body;

    // const guestSessionId = req.cookies.session_id;
    const guestSessionId = req.headers["x-session-id"];

    // console.log("Node login:" + email + " " + password);
    // console.log("Session id" + guestSessionId);


    let authResponse = null;
    const trimmedEmail = email.trim();
    try {

        const response = await axios.post(`${AuthURL}/login`, {
            identifier: trimmedEmail,
            password: password,
            remember_me: rememberMe,
            client_type: "Web"
        }, {
            headers: {
                'Content-Type': 'application/json',
                'x-client_type': WEB_CLIENT_NAME,
            ...(guestSessionId && {'x-session-id': guestSessionId})
        }
    })

        const responseData = await response.data;

        const {
            access_token, session_id, session_expires_in
        } = responseData;

        authResponse = responseData;

        sessionCache.setSession(session_id,
            access_token,
            false,
            rememberMe,
            session_expires_in);

        // res.cookie('session_id', authResponse.session_id,
        //     {
        //         maxAge: (session_expires_in ?? 660) * 1000,
        //         secure: true,
        //         path: '/',
        //         sameSite: 'none',
        //         httpOnly: true,
        //         domain: '.agromag.local'
        //     });

        sessionHeaderBuilder(res, session_id, session_expires_in);

    }
    catch (error) {
        console.error('-------------Error with login-------------\n', error);
        return res.status(error.response?.status || 500).end();
    }

    if (!authResponse.session_id) {
        return res.status(400).end();
    }

    try {
        const userDataResponse = await fetchWithSessionTokens(authResponse.session_id, async (sessionData) => {
            const [userResponse, cartSummary] = await Promise.all([
                axiosBackendClient.get(`${Backend_Url}/customer/me`, {
                    headers: {
                        'Content-Type': 'application/json',
                        'x-client_type': WEB_CLIENT_NAME,
                        ...(sessionData?.access_token && {'Authorization': 'Bearer ' + sessionData.access_token}),
                        ...(sessionData.session_id && {'x-session-id': sessionData.session_id})
                    },
                    bffContext: {
                        req, res
                    }
                },
                    ),
                getCartSummary(req, res, authResponse.session_id)

            ]);

            return {
                status: 200,
                data: {user: userResponse?.data, cartSummary: cartSummary?.data},
                headers: cartSummary?.headers
            }
        },{req, res});

        const userData = await userDataResponse.data;
        // console.log("UserData.data: "+ JSON.stringify(userData))
        return res.status(userDataResponse.status).json(userData);
    } catch (error) {
        console.error('Error fetching user data: ', error);
        return res.status(error.response?.status || 500).end();
    }
});


router.post('/logout', async (req, res) => {
    // const sessionId = req.cookies.session_id;
    const sessionId = req.headers["x-session-id"];

        try {
          const response =  await fetchWithSessionTokens(sessionId, async (sessionData) => await axiosBackendClient.get(
                `${AuthURL}/logout`,
                  {
                      headers: {
                          'Content-Type': 'application/json',
                          'x-client_type': WEB_CLIENT_NAME,
                          ...(!sessionData?.is_guest && {'Authorization': 'Bearer ' + sessionData?.access_token}),
                          ...(sessionData?.session_id && {'x-session-id': sessionData?.session_id}),
                      },
                      bffContext: {
                          req, res
                      }
                  }
            ),
              {req, res});

          const responseData = await response.data;

            if (responseData) {
                const {session_id, session_expires_in} = responseData;

                if (session_id && session_expires_in)
                {
                    // const summaryResponse = await getCartSummary(req, res, session_id);
                    // const cartSummary = await summaryResponse?.data;

                    sessionCache.setSession(session_id,
                        null,
                        true,
                        false,
                        session_expires_in);

                    // res.cookie('session_id', session_id,
                    //     {
                    //         maxAge: (session_expires_in ?? 660) * 1000,
                    //         secure: true,
                    //         path: '/',
                    //         sameSite: 'none',
                    //         httpOnly: true,
                    //         domain: '.agromag.local'
                    //     });


                    sessionHeaderBuilder(res, session_id, session_expires_in);

                    // return res.status(200).json({authenticated: false, cartSummary: cartSummary});
                    return res.status(200).end();
                }
            }

        } catch (error) {
            console.error("Error invalidating token and session, sessionCache will still be erased: ", error);
            // clearSessionCookies(res, sessionId);
            sessionCache.safeDelete(sessionId);
            return res.status(200).end();
        }

});
export default router;

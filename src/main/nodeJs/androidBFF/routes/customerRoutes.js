// noinspection DuplicatedCode

import express from 'express';
import {Backend_Url, WEB_CLIENT_NAME} from './config.js';
import {fetchWithSessionTokens} from "../services/requestTokenManager.js";
import axiosBackendClient from '../axiosBackendClient.js';
import sessionCache from "../services/sessionCache.js";

const router = express.Router();
import {getCartSummary} from "../services/cartSummaryFetcher.js"

const timestamp = () => {
    const now = new Date();
    return `[${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')} ${String(now.getHours()).padStart(2, '0')}-${String(now.getMinutes()).padStart(2, '0')}-${String(now.getSeconds()).padStart(2, '0')}]`;
};

router.post(`/password-update`, async (req, res) => {
    // const sessionId = req.cookies.session_id;
    const sessionId = req.headers["x-session-id"];

    try {
        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
                return await axiosBackendClient.post(`${Backend_Url}/customer/password-change`, {},
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
                    }
                );
            },
            {req, res});

        return res.status(response.status).end();
    } catch (error) {
        if (error.response) {
            console.warn(`${timestamp()} Handled backend error for updating password`);
            return res.status(error.response.status || 500).end();
        }
        console.error('-------------------Unexpected error updating password-------------------\n', error);
        return res.status(500).end();
    }
});

router.post(`/updateProfile`, async (req, res) => {
    // const sessionId = req.cookies.session_id;
    const sessionId = req.headers["x-session-id"];

    const {firstName, lastName, phone} = req.body;

    console.log("Request body in update profile: ", JSON.stringify(req.body));

    const requestBody = {first_name: firstName, last_name: lastName, phone_number: phone};

    console.log("Request body for backend: ", JSON.stringify(requestBody));

    try {
        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
                return await axiosBackendClient.patch(`${Backend_Url}/customer/update`, requestBody,
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
            console.warn(`${timestamp()} Handled backend error for updating profile: `, error);
            return res.status(error.response.status || 500).end();
        }
        console.error('-------------------Unexpected error updating profile-------------------\n', error);
        return res.status(500).end();
    }
});

router.get('/profile', async (req, res) => {
    // const sessionId = req.cookies.session_id;
    const sessionId = req.headers["x-session-id"];

    try {

        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
                return await axiosBackendClient.get(`${Backend_Url}/customer/profile`,
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
                    }
                );
            },
            {req, res}
        );

        const responseData = response.data;

        return res.status(response.status).json(responseData);

    } catch (error) {
        if (error.response) {
            console.warn(`${timestamp()} Handled backend error for fetching profile`);
            return res.status(error.response.status || 500).end();
        }
        console.error('-------------------Unexpected error fetching profile-------------------\n', error);
        return res.status(500).end();
    }
});

router.post(`/recipientTemplates/set`, async (req, res) => {
        // const sessionId = req.cookies.session_id;
        const sessionId = req.headers["x-session-id"];

        try {
            const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
                    return await axiosBackendClient.post(`${Backend_Url}/customer/recipientTemplates/set`, req.body, {
                            headers: {
                                'Content-Type': 'application/json',
                                'x-client_type': WEB_CLIENT_NAME,
                                ...(sessionData?.access_token && {'Authorization': 'Bearer ' + sessionData.access_token}),
                                ...(sessionData.session_id && {'x-session-id': sessionId})
                            },
                            bffContext: {
                                req, res
                            }
                        }
                    );
                },
                {req, res});

            return res.status(response.status).end();
        } catch (error) {

            if (error.response) {
                console.warn(`${timestamp()} Handled backend error for setting recipient templates`);

                const status = error.response.status;

                return res.status(status || 500).end();
            }

            console.error('-------------------Unexpected error setting recipient templates-------------------\n', error);
            return res.status(500).end();
        }

    }
);

router.get(`/recipientTemplates/get`, async (req, res) => {
    // const sessionId = req.cookies.session_id;
    const sessionId = req.headers["x-session-id"];

    // console.log("Session id in get recipient templates: ", sessionId);

    try {
        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
                return await axiosBackendClient.get(`${Backend_Url}/customer/recipientTemplates/get`,
                    {
                        headers: {
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

        const responseData = await response.data;

        console.log("Response data in get recipient templates: ", JSON.stringify(responseData));

        return res.status(response.status).json(responseData);

    } catch (error) {
        if (error.response) {
            console.warn(`${timestamp()} Handled backend error for fetching recipient templates`);
            return res.status(error.response.status || 500).end();
        }
        console.error('-------------------Unexpected error fetching recipient templates-------------------\n', error);
        return res.status(500).end();
    }
});

router.get('/getFavourites/:page', async (req, res) => {
    const page = req.params.page
    // const sessionId = req.cookies.session_id;
    const sessionId = req.headers["x-session-id"];

    try {

        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
            return await axiosBackendClient.get(`${Backend_Url}/customer/favourites/p/${page}`, {
                headers: {
                    'Content-Type': 'application/json',
                    'x-client_type': WEB_CLIENT_NAME,
                    ...(sessionData?.access_token && {'Authorization': 'Bearer ' + sessionData.access_token}),
                    ...(sessionData.session_id && {'x-session-id': sessionData.session_id})
                },
                bffContext: {
                    req, res
                }
            });
        }, {req, res})

        const responseData = await response.data;
        return res.status(response.status).json(responseData);
    } catch (error) {

        if (error.response) {
            console.warn(`${timestamp()} Handled backend error for fetching favourites`);

            const status = error.response.status;

            return res.status(status || 500).end();
        }

        console.error('-------------------Unexpected error fetching favourites-------------------\n', error);
        return res.status(500).end();
    }
});

router.post(`/addFavourite/:productCode`, async (req, res) => {

    // const sessionId = req.cookies.session_id;
    const sessionId = req.headers["x-session-id"];
    const productCode = req.params.productCode;

    try {

        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
            return await axiosBackendClient.post(`${Backend_Url}/customer/favorite/add/${productCode}`, {}, {
                headers: {
                    'Content-Type': 'application/json',
                    'x-client_type': WEB_CLIENT_NAME,
                    ...(sessionData?.access_token && {'Authorization': 'Bearer ' + sessionData.access_token}),
                    ...(sessionData.session_id && {'x-session-id': sessionId})
                },
                bffContext: {
                    req, res
                }
            });
        }, {req, res});

        const responseData = await response.data;

        if (responseData)
            return res.status(response.status).json(responseData);
        else
            return res.status(response.status).end();


    } catch (error) {

        if (error.response) {
            console.warn(`${timestamp()} Handled backend error for adding product to favourites`);
            return res.status(error.response.status || 500).json(error.response.data);
        }

        console.error('-------------------Unexpected error adding product to favourites-------------------\n', error);
        return res.status(500).json(error.response.data);
    }
});

router.post(`/removeFav/single`, async (req, res) => {

    // const sessionId = req.cookies.session_id;
    const sessionId = req.headers["x-session-id"];

    const {productCode, currentPage} = req.body;

    const requestBody = {product_code: productCode, current_page: currentPage};

    try {

        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
            return await axiosBackendClient.delete(`${Backend_Url}/customer/favorite/remove/single`, {
                headers: {
                    'Content-Type': 'application/json',
                    'x-client_type': WEB_CLIENT_NAME,
                    ...(!sessionData?.is_guest && {'Authorization': 'Bearer ' + sessionData?.access_token}),
                    ...(sessionData?.session_id && {'x-session-id': sessionData?.session_id}),
                },
                data: JSON.stringify(requestBody),
                bffContext: {
                    req, res
                }
            });
        }, {req, res})

        const responseData = await response.data;

        return res.status(response.status).json(responseData);

    } catch (error) {

        if (error.response) {
            console.warn(`${timestamp()} Handled backend error for removing product through the favourites page`);
            return res.status(error.response.status || 500).end();
        }

        console.error('-------------------Unexpected error removing product through the favourites page-------------------\n', error);
        return res.status(500).end();
    }
});

router.post(`/removeFav/detProd/:productCode`, async (req, res) => {

    // const sessionId = req.cookies.session_id;
    const sessionId = req.headers["x-session-id"];
    const productCode = req.params.productCode;

    try {

        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
            return await axiosBackendClient.delete(`${Backend_Url}/customer/favourites/remove/${productCode}`, {
                headers: {
                    'Content-Type': 'application/json',
                    'x-client_type': WEB_CLIENT_NAME,
                    ...(!sessionData?.is_guest && {'Authorization': 'Bearer ' + sessionData?.access_token}),
                    ...(sessionData?.session_id && {'x-session-id': sessionData?.session_id}),
                },
                bffContext: {
                    req, res
                }
            });
        }, {req, res});

        return res.status(response.status).end();

    } catch (error) {

        if (error.response) {
            console.warn(`${timestamp()} Handled backend error for removing product from favourites through the product page`);
            return res.status(error.response.status || 500).end();
        }

        console.error('-------------------Unexpected error removing product from favourites through the product page-------------------\n', error);
        return res.status(500).end();
    }
});

router.post(`/removeFav/batch`, async (req, res) => {
    try {
        // const sessionId = req.cookies.session_id;
        const sessionId = req.headers["x-session-id"];
        const {currentPage, productCodes} = req.body;

        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
            return await axiosBackendClient.delete(`${Backend_Url}/customer/favorite/remove/batch`, {
                headers: {
                    'Content-Type': 'application/json',
                    'x-client_type': WEB_CLIENT_NAME,
                    ...(!sessionData?.is_guest && {'Authorization': 'Bearer ' + sessionData?.access_token}),
                    ...(sessionData?.session_id && {'x-session-id': sessionData?.session_id}),
                },
                data: JSON.stringify({current_page: currentPage, product_codes: productCodes}),
                bffContext: {
                    req, res
                }
            });
        }, {req, res})

        const responseData = await response.data;

        return res.status(response.status).json(responseData);

    } catch (error) {

        if (error.response) {
            console.warn(`${timestamp()} Handled backend error for batch deleting products from favourites`);
            return res.status(error.response.status || 500).end();
        }
        console.error('-------------------Unexpected error batch deleting products from favourites-------------------\n', error);
        return res.status(500).end();
    }
});

router.get('/me', async (req, res) => {

    // let sessionId = req.cookies.session_id;

    let sessionId = req.headers["x-session-id"];

    const isGuest = sessionCache.get(sessionId)?.is_guest;

    console.log("isGuest: ", isGuest);

    if (!isGuest) {
        try {
            const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
                    const meResponse = await axiosBackendClient.get(`${Backend_Url}/customer/me`, {
                        headers: {
                            'Content-Type': 'application/json',
                            'x-client_type': WEB_CLIENT_NAME,
                            ...(!sessionData?.is_guest && {'Authorization': 'Bearer ' + sessionData?.access_token}),
                            ...(sessionData?.session_id && {'x-session-id': sessionData?.session_id}),
                        },
                        bffContext: {
                            req, res
                        }
                    });

                    meResponse.data.session_id = sessionData.session_id;
                    return meResponse;
                },
                {isMe: true, req, res});

            const responseData = await response.data;

            if (responseData)
                responseData.authenticated = !responseData.is_guest || false;

            if (responseData?.session_id) {
                // console.log("Replacing session_id in responseData: ", sessionId, " with ", responseData.session_id, " from '/me' request'")

                sessionId = responseData.session_id;
            }

            const cartSummaryResponse = await getCartSummary(req, res, sessionId);
            responseData.cartSummary = await cartSummaryResponse?.data;

            // console.log("responseData: ", JSON.stringify(responseData));

            return res.status(response.status).json(responseData);

        } catch (error) {

            if (error.response && error.response.status === 401) {

                const errorResponse = error.response.data;

                if (errorResponse?.guestError) {

                    // console.log("Guest error detected in '/me' request, creating guest session");

                    const {headers} = error.response;

                    // let newSessionId = null

                    if (headers) {
                        let {'x-session-info': sessionData} = headers;

                        if (sessionData) {
                            sessionData = JSON.parse(sessionData);

                            const {session_id} = sessionData;
                            // console.log("Replacing session_id in responseData: ", sessionId, " with ", session_id, " from '/me' request's error handling")
                            sessionId = session_id;
                        }
                    }
                    let summaryData = {};
                    if (sessionId) {
                        const cartSummaryResponse = await getCartSummary(req, res, sessionId);
                        summaryData = cartSummaryResponse?.data;
                    }

                    return res.status(200).json({authenticated: false, cartSummary: summaryData || {}});
                }
                return res.status(401).end();
            }

            console.error('------------------------Error fetching user info------------------------\n', error);
            return res.status(500).end();
        }
    } else {
        const cartSummaryResponse = await getCartSummary(req, res, sessionId);
        const summaryData = cartSummaryResponse?.data;
        return res.status(200).json({authenticated: false, cartSummary: summaryData});
    }
});

export default router;

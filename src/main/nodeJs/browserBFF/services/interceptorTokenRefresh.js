// import axiosBackendClient from '../axiosBackendClient.js';
import fetchTokensOfSession from "./refreshRequest.js";
import sessionCache from "./sessionCache.js";

export async function refreshToken(sessionId, res) {

    if (sessionId && res) {
        console.log("Refreshing tokens...");

        try {
            const responseData = await fetchTokensOfSession(sessionId);
            console.log('Fetched tokens:', responseData);
            if (!responseData) {
                return Promise.reject("No token response data received from backend.");
            }
            const {
                session_id,
                access_token,
                session_expires_in,
                is_guest,
                is_remember_me
            } = responseData;

            res.cookie('session_id', session_id,
                {
                    maxAge: (session_expires_in ?? 660) * 1000,
                    secure: true,
                    path: '/',
                    sameSite: 'none',
                    httpOnly: true,
                    domain: '.agromag.local'
                });

            sessionCache.setSession(
                session_id,
                access_token,
                is_guest,
                is_remember_me,
                session_expires_in
            );

            return;

        } catch (error) {
            console.error('Error refreshing token: ', error);
            return Promise.reject(error);
        }
    }
}



import axios from 'axios';
import {Backend_Url} from './routes/config.js';
import {refreshToken} from './services/interceptorTokenRefresh.js';
import sessionCache from "./services/sessionCache.js";

const axiosBackendClient = axios.create({baseURL: Backend_Url, withCredentials: true});

const refreshPromises = new Map();

axiosBackendClient.interceptors.response.use(
    response => response,
    async error => {
        const originalRequest = error.config;
        if (error.response?.status !== 401) {
            // console.error(error);
            return Promise.reject(error);
        }

        // console.log("Request failed due to unauthorized access.");

        if (!originalRequest?.bffContext) {
            console.error("No BFF context found in request during interceptor refresh, rejecting request.");
            resolveUnauthorizedAuthRequest(sessionId, res);

            return Promise.reject(error);
        }

        const req = originalRequest?.bffContext?.req;
        const res = originalRequest?.bffContext?.res;
        const sessionId = req?.cookies?.session_id;

        if (!sessionId) {
            resolveUnauthorizedAuthRequest(sessionId, res);

            return Promise.reject(error);
        }

        const isGuestSession = sessionCache.get(sessionId)?.is_guest;

        if (isGuestSession !== undefined && isGuestSession !== null && isGuestSession) {

            // console.log("Guest session detected, rejecting refresh...");
            resolveUnauthorizedAuthRequest(sessionId, res);

            return Promise.reject(error);
        }

        if (originalRequest._retry) {

            resolveUnauthorizedAuthRequest(sessionId, res);

            return Promise.reject(error);
        }

        originalRequest._retry = true;

        let refreshPromise = refreshPromises.get(sessionId);

        if (!refreshPromise) {

            refreshPromise = refreshToken(sessionId, res)
                .catch(err => {
                    console.error("Refresh token failed: " + err);

                    resolveUnauthorizedAuthRequest(sessionId, res);

                    throw err;
                })
                .finally(() => {
                    refreshPromises.delete(sessionId);
                });

            refreshPromises.set(sessionId, refreshPromise);

        } else {
            // console.log("Token refresh already in progress, request paused...");
        }

        return refreshPromise
            .then(() => {

                const updatedTokens = sessionCache.get(sessionId);

                if (updatedTokens && updatedTokens.access_token) {
                    originalRequest.headers['Authorization'] = 'Bearer ' + updatedTokens.access_token;
                }
                return axiosBackendClient(originalRequest)
            })
            .catch(err => {
                resolveUnauthorizedAuthRequest(sessionId, res);
               return Promise.reject(err)
            });
    }
);

function resolveUnauthorizedAuthRequest(sessionId, res) {

    console.log("session_id: " + sessionId + " is invalid, performing logout:");

    sessionCache.safeDelete(sessionId);

    console.log("session_id: " + sessionId + " is invalid, clearing cookies:");

    res.cookie('session_id', "",
        {
            maxAge: 0,
            secure: true,
            path: '/',
            sameSite: 'none',
            httpOnly: true,
            domain: '.agromag.local'
        });

}

export default axiosBackendClient;



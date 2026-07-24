import sessionCache from "./sessionCache.js";
import fetchTokensOfSession from "./refreshRequest.js";
import getExtractedExpiration, {TOKEN_REFRESH_BUFFER_MS} from "./accessTokenExpirationExtractor.js";

const refreshInProgress = new Map();

async function getSessionData(sessionId) {
    const cachedData = sessionCache.get(sessionId);
    if (cachedData) {

        if (cachedData.is_guest) {
            return {...cachedData, _isCacheHit: true};
        }

        const accessToken = cachedData.access_token;

        if (accessToken) {
            const currentTime = Date.now();

            const tokenExpiryMs = getExtractedExpiration(accessToken);

            if (currentTime >= (tokenExpiryMs - TOKEN_REFRESH_BUFFER_MS)) {
                console.log(`[BFF] Token expiring soon (${((tokenExpiryMs - currentTime) / 1000).toFixed(0)}s left). Triggering preemptive refresh...`);
            } else {
                return {...cachedData, _isCacheHit: true};
            }
        }


    }

    let refreshPromise = refreshInProgress.get(sessionId);

    // if (refreshInProgress.has(sessionId)) {
    //     return await refreshInProgress.get(sessionId);
    //     //todo tuk moje bi trqbva da se izvleq4e refresh promis-a predi da se pravi kakvoto i da e kakto e pri interceptora
    // }

    if (refreshPromise) {
        return await refreshPromise;
    }

    const fetchWork = async () => {
        try {

            const freshData = await fetchTokensOfSession(sessionId);
            return {...freshData, _isCacheHit: false};
        } catch (error) {
            console.error("Access token fetch internal error:", error.message);
            throw error;
        } finally {
            refreshInProgress.delete(sessionId);

        }
    };

    const p = fetchWork();
    refreshInProgress.set(sessionId, p);

    return await p;
}

async function fetchWithSessionTokens(sessionId, requestFn, options = {}) {
    const {isMe = false, req, res} = options;

    const makeResponse = (axiosResponse) => {
        {

            return {
                status: axiosResponse.status || 200,
                data: axiosResponse.data ?? {},
                headers: axiosResponse.headers ?? {}

            };
        }
    };

    let sessionData;
    try {

        try {
            sessionData = await getSessionData(sessionId);
            processRefreshedOrCachedSessionData(sessionData, res, isMe, sessionId);
        } catch (err) {

            if (err.response && err.response.status === 401) {

                // console.log("401 error in '/me' request detected, checking for guest error")

                const errorResponse = err.response.data;

                if (errorResponse?.guestError) {
                    throw err;
                }

            }

            console.error("Error fetching session data:", err);

            if (sessionId) {
                sessionData = {session_id: sessionId}

            }
        }

        const axiosResponse = await requestFn(sessionData);

        // console.log("----------------------------------\nResponse Body for of original request: ", JSON.stringify(axiosResponse.data), "\n----------------------------------\n");

        const responseBody = makeResponse(axiosResponse);
        // console.log("----------------------------------\nResponse Body for header processing: ", responseBody, "\n----------------------------------\n");
        processResponseHeaders(responseBody, res, sessionId);
        handleGuestState(responseBody.headers, sessionData, res);

        // const {headers} = responseBody;
        // console.log("----------------------------------\nResponse headers: ", headers, "\n----------------------------------\n");
        return responseBody;

    } catch (err) {

        console.error("Error in fetchWithSessionTokens:", err);

        const normalizedError = new Error(err?.message);

        if (err.isAxiosError && err.response) {

            // console.log("Axios Error Response:", err.response.data);

            if (err.response.headers) {
                if (err.response?.headers) {
                    processResponseHeaders(err.response.headers, res, sessionId, sessionData);
                    handleGuestState(err.response.headers, sessionData, res);
                }
            }

            normalizedError.response = {
                status: err.response.status,
                data: err.response.data
            };
        } else {
            // console.log("Non-Axios Error:", err);
            normalizedError.response = {
                status: err?.status || 500,
                data: {error: err?.message || 'Internal BFF Error'}
            };
        }

        // console.error("Normalized Error:", normalizedError);

        throw normalizedError;
    }

    function handleGuestState(headers, sessionDataAfterCacheRefresh, res) {

        let {'x-session-info': sessionData} = headers;

        let responseSessionId;

        responseSessionId = sessionData?.session_id;
        const {is_guest, session_id: refreshSessionId} = sessionDataAfterCacheRefresh;

        if (res) {
            if (responseSessionId) {

                res.setHeader('Access-Control-Expose-Headers', 'x-guest-state');

                res.setHeader('x-guest-state', is_guest
                    ? "guest"
                    : "authenticated");

            } else {
                const cacheData = sessionCache.get(refreshSessionId);

                if (cacheData) {

                    res.setHeader('Access-Control-Expose-Headers', 'x-guest-state');

                    res.setHeader('x-guest-state', cacheData?.is_guest
                        ? "guest"
                        : "authenticated");

                }
            }
            // console.log("----------------------------------\nResponse headers: ", res.headers, "\n----------------------------------\n");
        } else
            console.warn(" \n" +
                "----------------------------------\n" +
                "⚠️ fetchWithSessionTokens called without res" +
                " \n" +
                "----------------------------------\n");

    }

    function processRefreshedOrCachedSessionData(newSessionData, res, isMe = false, oldSessionId = null) {

        if (newSessionData == null) {
            return;
        }

        const {
            session_id: newSessionId,
            access_token,
            session_expires_in,
            is_guest,
            is_remember_me,
            _isCacheHit,
        } = newSessionData;

        // console.log("----------------------------------\nSession Data after cache check or refresh : ", newSessionData);

        //
        // console.log("Session TTL in refresh:", session_expires_in);
        // console.log("Cookie maxAge in refresh:", (session_expires_in ?? 3600) * 1000);

        if (newSessionId && newSessionId !== oldSessionId) {

            // console.log("----------------------------------\nReplacing session cookie from refresh request with new session:  \n" + newSessionId +
            //     "----------------------------------\n ");

            sessionCache.safeDelete(oldSessionId);
            // sessionCache.set(newSessionId, newSessionData);

            sessionCache.setSession(
                newSessionId,
                access_token,
                is_guest,
                is_remember_me,
                session_expires_in);

            res.cookie('session_id', newSessionId,
                {
                    maxAge: (session_expires_in ?? 660) * 1000,
                    secure: true,
                    path: '/',
                    sameSite: 'none',
                    httpOnly: true,
                    domain: '.agromag.local'
                });
        } else if (!_isCacheHit) {
            // sessionCache.set(oldSessionId, newSessionData);
            delete newSessionData._isCacheHit;

            sessionCache.setSession(
                oldSessionId,
                access_token,
                is_guest,
                is_remember_me,
                session_expires_in);
            res.cookie('session_id', oldSessionId,
                {
                    maxAge: (session_expires_in ?? 660) * 1000,
                    secure: true,
                    path: '/',
                    sameSite: 'none',
                    httpOnly: true,
                    domain: '.agromag.local'
                });
        }

        if (isMe && is_guest) {

            req.updatedSessionId = newSessionId;

            const guestErr = new Error("isMe guest error");
            guestErr.response = {
                data: {
                    guestError: true
                },
                status: 401
            }
            guestErr.isAxiosError = true;

            throw guestErr;
        }

    }

    function processResponseHeaders(responseObject, res, oldSessionId) {

        const {headers} = responseObject;

        // console.log("----------------------------------\nResponse Headers: ", headers, "\n----------------------------------\n");

        if (headers) {
            let {'x-session-info': sessionData} = headers;

            if (!sessionData) return;

            if (typeof sessionData === 'string') {
                sessionData = JSON.parse(sessionData);
            }

            const {session_id, session_expires_in, is_guest, is_replaced, is_remember_me} = sessionData;

            // console.log("Session TTL in headers:", session_expires_in);
            // console.log("Cookie maxAge in headers:", (session_expires_in ?? 3600) * 1000);
            //
            // console.log("----------------------------------\nSession Data after request headers: ", sessionData);
            //
            // console.log("----------------------------------\nSession ID from headers: ", session_id);
            // console.log("----------------------------------\nIs_replaced from headers: ", is_replaced);

            if (session_id) {
                responseObject.newSessionId = session_id;

                if (is_replaced) {
                    // console.log("----------------------------------\nReplacing session cookie and cache from headers with new session:  \n" + session_id +
                    //     "----------------------------------\n ");
                    sessionCache.safeDelete(oldSessionId);
                }

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
                    null,
                    is_guest,
                    is_remember_me,
                    session_expires_in
                );
            }
        }

    }
}

export default fetchWithSessionTokens

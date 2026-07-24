// noinspection DuplicatedCode

import {fetchWithSessionTokens} from "./requestTokenManager.js";
import axiosBackendClient from "../axiosBackendClient.js";
import {Backend_Url, WEB_CLIENT_NAME} from "../routes/config.js";

export async function getCartSummary(req, res, sessionId) {
    return await fetchWithSessionTokens(sessionId, async (sessionData) => {
            return await axiosBackendClient.get(`${Backend_Url}/cart/summary`,
                {
                    headers: {
                        'Content-Type': 'application/json',
                        'x-client_type': WEB_CLIENT_NAME,
                        ...(!sessionData?.is_guest && {'Authorization': 'Bearer ' + sessionData?.access_token}),
                        ...(sessionData.session_id && {'x-session-id': sessionData.session_id})
                    },
                    bffContext: {req, res}
                }
            );
        },
        {req, res});
}
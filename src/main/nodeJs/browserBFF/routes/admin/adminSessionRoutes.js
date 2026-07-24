import express from 'express';
const router = express.Router();
import {Backend_Url, WEB_CLIENT_NAME} from '../config.js';
import axiosBackendClient from '../../axiosBackendClient.js';
import fetchWithSessionTokens from "../../services/requestTokenManager.js";

const CONTROLLER_ROUTE = `${Backend_Url}/admin/session`;

router.get('/active-sessions', async (req, res) => {
    const sessionId = req.cookies.session_id;

    try{
        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
                return await axiosBackendClient.get(`${CONTROLLER_ROUTE}/active/get`, {
                    headers:
                        {
                            'Content-Type': 'application/json',
                            'x-client_type': WEB_CLIENT_NAME,
                            ...(!sessionData?.is_guest && {'Authorization': 'Bearer ' + sessionData?.access_token}),
                            ...(sessionData?.session_id && {'x-session-id': sessionData?.session_id}),
                        },
                    bffContext: {
                        req, res
                    }
                });
            },
            {req, res});

        const responseData = response.data;

        return res.status(response.status).json(responseData || {});
    }
    catch (error) {
        console.error('-------------------Error fetching active sessions-------------------\n', error);
        if (error.response)
            return res.status(error.response.status || 500).json(error.response.data);
        return res.status(500).end();

    }
})

export default router;
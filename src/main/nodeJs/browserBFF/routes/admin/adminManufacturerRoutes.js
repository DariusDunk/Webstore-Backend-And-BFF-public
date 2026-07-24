import express from 'express';
const router = express.Router();
import {Backend_Url, WEB_CLIENT_NAME} from '../config.js';
import axiosBackendClient from '../../axiosBackendClient.js';
import fetchWithSessionTokens from "../../services/requestTokenManager.js";

const CONTROLLER_ROUTE = `${Backend_Url}/admin/manufacturer`;

router.patch(`/update/:id`, async (req, res) => {
    const sessionId = req.cookies.session_id;
    const reqBody = req.body;
    const {id} = req.params;
    const {name} = reqBody;

    try {
        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
            return await axiosBackendClient.patch(`${CONTROLLER_ROUTE}/update/${id}`, {name}, {
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

        return res.status(response.status).end();
    }

    catch (error) {
        console.error('-------------------Error updating manufacturer-------------------\n', error);
        if (error.response)
            return res.status(error.response.status || 500).json(error.response.data);
        return res.status(500).end();
    }
})

router.post(`/create`, async (req, res) => {
    const sessionId = req.cookies.session_id;
    const reqBody = req.body;
    const {name} = reqBody;

    try {
        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
            return await axiosBackendClient.post(`${CONTROLLER_ROUTE}/create`, {name}, {
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

        return res.status(response.status).end();
    }
    catch (error) {
        console.error('-------------------Error creating manufacturer-------------------\n', error);
        if (error.response)
            return res.status(error.response.status || 500).json(error.response.data);
        return res.status(500).end();
    }
})

router.get(`/get/:id`, async (req, res) => {
    const sessionId = req.cookies.session_id;
    const {id} = req.params;

    try {
        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
            return await axiosBackendClient.get(`${CONTROLLER_ROUTE}/get/${id}`, {
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
        console.error('-------------------Error fetching manufacturer-------------------\n', error);
        if (error.response)
            return res.status(error.response.status || 500).json(error.response.data);
        return res.status(500).end();
    }
})

router.get('/all', async (req, res) => {
    const sessionId = req.cookies.session_id;
    try {
        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
            return await axiosBackendClient.get(`${CONTROLLER_ROUTE}/all`, {
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
        console.error('-------------------Error fetching all manufacturers-------------------\n', error);
        if (error.response)
            return res.status(error.response.status || 500).json(error.response.data);
        return res.status(500).end();
    }
})

export default router;
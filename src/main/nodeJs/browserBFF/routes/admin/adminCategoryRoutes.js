import express from 'express';
const router = express.Router();
import {Backend_Url, WEB_CLIENT_NAME} from '../config.js';
import axiosBackendClient from '../../axiosBackendClient.js';
import fetchWithSessionTokens from "../../services/requestTokenManager.js";

router.post(`/create`, async (req, res) => {
    const sessionId = req.cookies.session_id;
    const reqBody = req.body;
    const{name, isDeleted, selectedGroups} = reqBody;
    const bodyForRequest = {
        name: name,
        is_deleted: isDeleted,
        attribute_groups: selectedGroups
    };

    try{
        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
                return await axiosBackendClient.post(`${Backend_Url}/admin/category/create`, bodyForRequest, {
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
        console.error('-------------------Error creating category-------------------\n', error);
        if (error.response)
            return res.status(error.response.status || 500).json(error.response.data);
        return res.status(500).end();
    }
})

router.patch(`/update`, async (req, res) => {
    const sessionId = req.cookies.session_id;
    const reqBody = req.body;
    const{id, name, isDeleted, selectedGroups} = reqBody;
    const bodyForRequest = {
        id: id,
        name: name,
        is_deleted: isDeleted,
        attribute_groups: selectedGroups
    };

    try{
        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
            return await axiosBackendClient.patch(`${Backend_Url}/admin/category/update`, bodyForRequest, {
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
        console.error('-------------------Error updating category-------------------\n', error);
        if (error.response)
            return res.status(error.response.status || 500).json(error.response.data);
        return res.status(500).end();
    }

})

router.get(`/detailed/:id`, async (req, res) => {
    const sessionId = req.cookies.session_id;
    const {id} = req.params;

    try{

        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
            return await axiosBackendClient.get(`${Backend_Url}/admin/category/detailed/${id}`, {
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
        console.error('-------------------Error fetching detailed category-------------------\n', error);
        if (error.response)
            return res.status(error.response.status || 500).json(error.response.data);
        return res.status(500).end();
    }
})

router.get('/all/compact', async (req, res) => {
    const sessionId = req.cookies.session_id;
    try {
        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
            return await axiosBackendClient.get(`${Backend_Url}/admin/category/all/compact`, {
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
            })
        },
            {req, res});

        const responseData = response.data;
        return res.status(response.status).json(responseData || {});

    } catch (error)
        {
        console.error('-------------------Error fetching all categories-------------------\n', error);
        if (error.response)
            return res.status(error.response.status || 500).json(error.response.data);
        return res.status( 500).end();
        }
})

export default router;
import express from 'express';

const router = express.Router();
import {Backend_Url, WEB_CLIENT_NAME} from '../config.js';
import axiosBackendClient from '../../axiosBackendClient.js';
import fetchWithSessionTokens from "../../services/requestTokenManager.js";

router.patch(`/update/:id/attributes`, async (req, res) => {
    const sessionId = req.cookies.session_id;
    const reqBody = req.body;
    const {id} = req.params;

    try
    {
        const response = await fetchWithSessionTokens(sessionId, async (sessionData) =>{
            return await axiosBackendClient.patch(`${Backend_Url}/admin/product/update/${id}/attributes`, reqBody, {
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

    catch (error)
    {
        console.error('-------------------Error updating product attributes-------------------\n', error);
        if (error.response)
            return res.status(error.response.status || 500).json(error.response.data);
        return res.status(500).end();
    }
})

router.post(`/create`, async (req, res) => {
    const sessionId = req.cookies.session_id;
    const reqBody = req.body;

    try
    {
        const response = await fetchWithSessionTokens(sessionId, async (sessionData) =>{
            return axiosBackendClient.post(`${Backend_Url}/admin/product/create`, reqBody, {
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

        const createdId = response.data;

        return res.status(response.status).json({id: createdId} );
    }

    catch (error) {
        console.error('-------------------Error creating product-------------------\n', error);
        if (error.response)
            return res.status(error.response.status || 500).json(error.response.data);
        return res.status(500).end();
    }
})

router.patch(`/update/:id`, async (req, res) => {
    const sessionId = req.cookies.session_id;
    const reqBody = req.body;
    const {id} = req.params

    try {

        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
            return await axiosBackendClient.patch(`${Backend_Url}/admin/product/update/${id}`, reqBody, {
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

    } catch (error) {
        console.error('-------------------Error updating product-------------------\n', error);
        if (error.response)
            return res.status(error.response.status || 500).json(error.response.data);
        return res.status(500).end();
    }
});

router.get(`/detailed/:id`, async (req, res) => {
    const sessionId = req.cookies.session_id;
    const {id} = req.params;
    try {
        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
                return await axiosBackendClient.get(`${Backend_Url}/admin/product/detail/${id}`, {
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
    } catch (error) {
        console.error('-------------------Error fetching detailed product-------------------\n', error);
        if (error.response)
            return res.status(error.response.status || 500).json(error.response.data);
        return res.status(500).end();
    }
})

router.get('/all/:page', async (req, res) => {
    const sessionId = req.cookies.session_id;
    const {page} = req.params;
    try {
        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
                return await axiosBackendClient.get(`${Backend_Url}/admin/product/all/p/${page}`, {
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

    } catch (error) {
        console.error('-------------------Error fetching all products-------------------\n', error);
        if (error.response)
            return res.status(error.response.status || 500).json(error.response.data);
        return res.status(500).end();
    }
});

router.get('/sale-suggestions', async (req, res) => {
    const sessionId = req.cookies.session_id;
    const {keyword} = req.query;
    try {
        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
                return await axiosBackendClient.get(`${Backend_Url}/admin/product/suggestions?keyword=${keyword}`, {
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
        return res.status(response.status).json({suggestions: responseData} || {});
    } catch (error) {
        console.error('-------------------Error fetching product suggestions-------------------\n', error);
        if (error.response)
            return res.status(error.response.status || 500).json(error.response.data);
        return res.status(500).end();
    }
})

export default router;
import express from 'express';
const router = express.Router();
import {Backend_Url, WEB_CLIENT_NAME} from '../config.js';
import axiosBackendClient from '../../axiosBackendClient.js';
import fetchWithSessionTokens from "../../services/requestTokenManager.js";

const CONTROLLER_ROUTE = `${Backend_Url}/admin/sale`;

router.post('/create', async (req, res) => {
    const sessionId = req.cookies.session_id;
    const reqBody = req.body;
    const {name, defaultDiscount, startDate, endDate, isActive, products} = reqBody;
    const bodyForRequest = {
        name: name,
        default_discount: defaultDiscount,
        start_date: startDate,
        end_date: endDate,
        is_active: isActive,
        products: products
    };

    try{
        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
            return await axiosBackendClient.post(`${CONTROLLER_ROUTE}/create`, bodyForRequest, {
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
                }
            );
        },
            {req, res});

        return res.status(response.status).end();

    }
    catch (error) {
        console.error('-------------------Error creating sale-------------------\n', error);
        if (error.response)
            return res.status(error.response.status || 500).json(error.response.data);
        return res.status(500).end();
    }
})

router.patch('/update', async (req, res) => {
    const sessionId = req.cookies.session_id;
    const reqBody = req.body;
    const {id, name, defaultDiscount, startDate, endDate, isActive, products} = reqBody;

    try{
        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
            return await axiosBackendClient.patch(`${CONTROLLER_ROUTE}/update`, {
                    id: id,
                    name: name,
                    default_discount: defaultDiscount,
                    start_date: startDate,
                    end_date: endDate,
                    is_active: isActive,
                    products: products
                }, {
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
                }
            );
        },
            {req, res});

        return res.status(response.status).end();
    }
    catch (error) {
        console.error('-------------------Error updating sale-------------------\n', error);
        if (error.response)
            return res.status(error.response.status || 500).json(error.response.data);
        return res.status(500).end();
    }
})

router.get('/detailed/:id', async (req, res) => {
    const sessionId = req.cookies.session_id;
    const {id} = req.params;
    try {
        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
                return await axiosBackendClient.get(`${CONTROLLER_ROUTE}/detailed/${id}`, {
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
        console.error('-------------------Error fetching detailed sale-------------------\n', error);
        if (error.response)
            return res.status(error.response.status || 500).json(error.response.data);
        return res.status(500).end();

    }
});

router.get('/all/:page', async (req, res) => {
    const sessionId = req.cookies.session_id;
    const {page} = req.params;
    try {
        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
            return await axiosBackendClient.get(`${CONTROLLER_ROUTE}/all/${page}`, {
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
        console.error('-------------------Error fetching all sales-------------------\n', error);
        return res.status(error.response?.status || 500).end();
    }
})

export default router;
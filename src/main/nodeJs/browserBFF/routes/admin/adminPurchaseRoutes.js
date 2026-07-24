import express from 'express';

const router = express.Router();
import {Backend_Url, WEB_CLIENT_NAME} from '../config.js';
import axiosBackendClient from '../../axiosBackendClient.js';
import fetchWithSessionTokens from "../../services/requestTokenManager.js";
import {timestamp} from "../../services/timeStamper.js";

const PURCHASE_CONTROLLER_ROUTE = `${Backend_Url}/admin/purchase`;

router.get(`/purchase-statuses-report/pdf`, async (req, res) => {
    const sessionId = req.cookies.session_id;
    const{startDate, endDate, timezone} = req.query;
    const bodyForRequest = {start_date:startDate, end_date:endDate, timezone};

    try
    {
        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
                return await axiosBackendClient.post(`${PURCHASE_CONTROLLER_ROUTE}/purchase-statuses-report/pdf`, bodyForRequest,
                    {
                        headers:
                            {
                                'x-client_type': WEB_CLIENT_NAME,
                                ...(!sessionData?.is_guest && {'Authorization': 'Bearer ' + sessionData?.access_token}),
                                ...(sessionData?.session_id && {'x-session-id': sessionData?.session_id}),
                            }, bffContext: {
                            req, res
                        }, responseType: "stream"
                    });
            },
            {req, res});

        res.setHeader("Content-Type", "application/pdf");
        res.setHeader(
            "Content-Disposition",
            `inline; filename=purchase-status-report.pdf`
        );

        response.data.on("error", (err) => {
            console.error("PDF stream error:", err);
            res.status(500).end();
        });

        response.data.pipe(res);

        return;

    }
    catch (error)
    {
        console.error("error in get purchase statuses report pdf: ", error)
        if (error.response) {
            console.warn(`${timestamp()} Handled backend error for get purchase statuses report pdf request`);
            return res.status(error.response.status || 500).json(error.response.data);
        }
        return res.status(500).end();
    }
})

router.get(`/purchase-statuses-report`, async (req, res) => {
    const sessionId = req.cookies.session_id;
    const{startDate, endDate, timezone} = req.query;
    const bodyForRequest = {start_date:startDate, end_date:endDate, timezone};

    try {

        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
                return await axiosBackendClient.post(`${PURCHASE_CONTROLLER_ROUTE}/purchase-statuses-report`, bodyForRequest,
                    {
                        headers:
                            {
                                'Content-Type': 'application/json',
                                'x-client_type': WEB_CLIENT_NAME,
                                ...(!sessionData?.is_guest && {'Authorization': 'Bearer ' + sessionData?.access_token}),
                                ...(sessionData?.session_id && {'x-session-id': sessionData?.session_id}),
                            }
                        , bffContext: {
                            req, res
                        }
                    });
            },
            {req, res});

        const responseData = response.data;
        return res.status(response.status).json(responseData || {});

    } catch (error)
    {
        console.error("error in get purchase statuses report: ", error)
        if (error.response) {
            console.warn(`${timestamp()} Handled backend error for get purchase statuses report request`);
            return res.status(error.response.status || 500).json(error.response.data);
        }
        return res.status(500).end();
    }
})

router.get(`/top-selling-for-period/pdf`, async (req, res) => {
    const sessionId = req.cookies.session_id;
    const{startDate, endDate, timezone, limit} = req.query;
    const bodyForRequest = {start_date:startDate, end_date:endDate, timezone};

    try
    {
        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
                return await axiosBackendClient.post(`${PURCHASE_CONTROLLER_ROUTE}/top-selling-for-period/pdf?limit=${encodeURIComponent(limit)}`, bodyForRequest,
                    {
                        headers:
                            {
                                'x-client_type': WEB_CLIENT_NAME,
                                ...(!sessionData?.is_guest && {'Authorization': 'Bearer ' + sessionData?.access_token}),
                                ...(sessionData?.session_id && {'x-session-id': sessionData?.session_id}),
                            }
                        , bffContext: {
                            req, res
                        }, responseType: "stream"
                    });
            },
            {req, res}
        );


        res.setHeader("Content-Type", "application/pdf");
        res.setHeader(
            "Content-Disposition",
            `inline; filename=top-products-report.pdf`
        );

        response.data.on("error", (err) => {
            console.error("PDF stream error:", err);
            res.status(500).end();
        });

        response.data.pipe(res);

        return;

    }
    catch ( error )
    {
        console.error("error in get top selling products pdf: ", error)
        if (error.response) {
            console.warn(`${timestamp()} Handled backend error for get top selling products pdf request`);
            return res.status(error.response.status || 500).json(error.response.data);
        }
        return res.status(500).end();
    }
})

router.get(`/top-selling-products`, async (req, res) => {
    const sessionId = req.cookies.session_id;
    const{startDate, endDate, timezone, limit} = req.query;
    const bodyForRequest = {start_date:startDate, end_date:endDate, timezone};

    try {

        const response = await fetchWithSessionTokens(sessionId, async (sessionData)=>{
            return await axiosBackendClient.post(`${PURCHASE_CONTROLLER_ROUTE}/top-selling-for-period?limit=${encodeURIComponent(limit)}`, bodyForRequest, {
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
        console.error("error in get top selling products: ", error)
        if (error.response) {
            console.warn(`${timestamp()} Handled backend error for get top selling products request`);
            return res.status(error.response.status || 500).json(error.response.data);
        }
        return res.status(500).end();
    }
})

router.get(`/revenue-report/pdf`, async (req, res) => {
    const sessionId = req.cookies.session_id;
    // const reqBody = req.body;
    const{startDate, endDate, timezone} = req.query;
    const bodyForRequest = {start_date:startDate, end_date:endDate, timezone};

    // console.log("About to make request: " + JSON.stringify(bodyForRequest));

    try
    {
        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
            return await axiosBackendClient.post(`${PURCHASE_CONTROLLER_ROUTE}/revenue-report/pdf`, bodyForRequest, {
                headers:
                    {
                        'x-client_type': WEB_CLIENT_NAME,
                        ...(!sessionData?.is_guest && {'Authorization': 'Bearer ' + sessionData?.access_token}),
                        ...(sessionData?.session_id && {'x-session-id': sessionData?.session_id}),
                    },
                bffContext: {
                    req, res
                },
                responseType: "stream"
            });
        },
            {req, res});

        // console.log("About to return pdf response")

        res.setHeader("Content-Type", "application/pdf");
        res.setHeader(
            "Content-Disposition",
            `inline; filename=revenue-report.pdf`
        );

        response.data.on("error", (err) => {
            console.error("PDF stream error:", err);
            res.status(500).end();
        });

        response.data.pipe(res);

        return;

    }
    catch (error) {
        console.error('-------------------Error fetching revenue report-------------------\n', error);
        if (error.response)
            return res.status(error.response.status || 500).json(error.response.data);
        return res.status(500).end();
    }
})

router.post(`/revenue-report`, async (req, res) => {
    const sessionId = req.cookies.session_id;
    const reqBody = req.body;
    const{startDate, endDate, timezone} = reqBody;
    const bodyForRequest = {start_date:startDate, end_date:endDate, timezone};

    try {
        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
            return await axiosBackendClient.post(`${PURCHASE_CONTROLLER_ROUTE}/revenue-report`,bodyForRequest, {
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
        console.error('-------------------Error fetching revenue report-------------------\n', error);
        if (error.response)
            return res.status(error.response.status || 500).json(error.response.data);
        return res.status(500).end();
    }
})

router.patch(`/update-status`, async (req, res) => {
    const sessionId = req.cookies.session_id;
    const reqBody = req.body;

    console.log(JSON.stringify(reqBody));

    try {
        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
                return await axiosBackendClient.patch(`${PURCHASE_CONTROLLER_ROUTE}/purchase-action`, reqBody, {
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
        console.error('-------------------Error updating purchase status-------------------\n', error);
        if (error.response)
            return res.status(error.response.status || 500).json(error.response.data);
        return res.status(500).end();
    }
});

router.get(`/pending-refund-count`, async (req, res) => {
    const sessionId = req.cookies.session_id;
    try {
        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
            return await axiosBackendClient.get(`${PURCHASE_CONTROLLER_ROUTE}/pending-refund-count`, {
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
        console.error('-------------------Error fetching pending refund count-------------------\n', error);
        if (error.response)
            return res.status(error.response.status || 500).json(error.response.data);
        return res.status(500).end();
    }
})

router.get(`/get-all-purchases/:page`, async (req, res) => {
    const sessionId = req.cookies.session_id;
    const {page} = req.params;
    try {
        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
            return await axiosBackendClient.get(`${PURCHASE_CONTROLLER_ROUTE}/all/${page}`, {
                headers:
                    {
                        'Content-Type': 'application/json',
                        'x-client_type': WEB_CLIENT_NAME,
                        ...(!sessionData?.is_guest && {'Authorization': 'Bearer ' + sessionData?.access_token}),
                        ...(sessionData?.session_id && {'x-session-id': sessionData?.session_id}),
                    },
                bffContext: {
                    req, res
                },

            });
        },
            {req, res});

        const responseData = response.data;
        return res.status(response.status).json(responseData || {});
    }
    catch (error) {
        console.error('-------------------Error fetching all purchases-------------------\n', error);
        if (error.response)
            return res.status(error.response.status || 500).json(error.response.data);
        return res.status(500).end();
    }
})

export default router;
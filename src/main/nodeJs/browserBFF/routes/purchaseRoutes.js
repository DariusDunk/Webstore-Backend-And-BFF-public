import express from 'express';

const router = express.Router();
import {Backend_Url, WEB_CLIENT_NAME} from './config.js';
import fetchWithSessionTokens from "../services/requestTokenManager.js";
import axiosBackendClient from '../axiosBackendClient.js';
import {getCartSummary} from "../services/cartSummaryFetcher.js"


const timestamp = () => {
    const now = new Date();
    return `[${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')} ${String(now.getHours()).padStart(2, '0')}-${String(now.getMinutes()).padStart(2, '0')}-${String(now.getSeconds()).padStart(2, '0')}]`;
};

const authIntentHeader = 'x-auth-intent';

router.get(`/invoice/:code`, async (req, res) => {
    try {
        const sessionId = req.cookies.session_id;
        const {code} = req.params;

        const response = await fetchWithSessionTokens(sessionId, async (sessionData)=>{
            return await axiosBackendClient.get(`${Backend_Url}/purchase/invoice/${code}`,
                {
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

        res.setHeader("Content-Type", "application/pdf");
        res.setHeader(
            "Content-Disposition",
            `inline; filename=invoice-${code}.pdf`
        );

        response.data.on("error", (err) => {
            console.error("PDF stream error:", err);
            res.status(500).end();
        });

        response.data.pipe(res);

        return;
    }

    catch (error) {
        console.error("error in get invoice: ", error)
        if (error.response) {
            console.warn(`${timestamp()} Handled backend error for get invoice request`);
            return res.status(error.response.status || 500).json(error.response.data);
        }
        return res.status(500).end();
    }
})

router.post(`/refund-request/:code`, async (req, res) => {
    try {
        const sessionId = req.cookies.session_id;
        const {code} = req.params;

        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
              return await axiosBackendClient.patch(`${Backend_Url}/purchase/request-refund/c/${code}`, {}, {
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
        console.error("error in refund request: ", error);
        if (error.response) {
            console.warn(`${timestamp()} Handled backend error for refund request`);
            return res.status(error.response.status || 500).json(error.response.data);
        }
        return res.status(500).end();
    }
})

router.post(`/cancel/:code`, async (req, res) => {
    try {
        const sessionId = req.cookies.session_id;
        const {code} = req.params;

        console.log("in cancel");

        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
               return await axiosBackendClient.patch(`${Backend_Url}/purchase/cancel/c/${code}`, {}, {
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

        console.error("error in cancel purchase: ", error)

        if (error.response) {
            console.warn(`${timestamp()} Handled backend error for cancel purchase request`);
            return res.status(error.response.status || 500).json(error.response.data);
        }
        return res.status(500).end();
    }
})

router.get(`/detail/:code`, async (req, res) => {
    try {
        const sessionId = req.cookies.session_id;
        const {code} = req.params;

        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
                return await axiosBackendClient.get(`${Backend_Url}/purchase/detail/c/${code}`, {
                    headers: {
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
            {req, res}
        );

        const responseData = response.data;

        return res.status(response.status).json(responseData || {});

    } catch (error) {

        console.error("error in get purchase detail: ", error);

        if (error.response) {
            console.warn(`${timestamp()} Handled backend error for get purchase detail request`);
            return res.status(error.response.status || 500).json(error.response.data);
        }

        return res.status(500).end();
    }
});

router.get(`/history/:page`, async (req, res) => {
    try {
        const sessionId = req.cookies.session_id;
        const {page} = req.params;

        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
                return await axiosBackendClient.get(`${Backend_Url}/purchase/purchase-history/p/${page}`, {
                    headers: {
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

        // console.log("Purchase history response: ", JSON.stringify(responseData));

        return res.status(response.status).json(responseData || {});

    } catch (error) {
        console.error("error in get purchase history: ", error)

        if (error.response) {
            console.warn(`${timestamp()} Handled backend error for get purchase history request`);
            return res.status(error.response.status || 500).json(error.response.data);
        }

        return res.status(500).end();
    }
})

router.post('/complete', async (req, res) => {
    try {

        let sessionId = req.cookies.session_id;
        const authIntent = req.get(authIntentHeader);

        if (authIntent && (sessionId === undefined || sessionId === null)) {
            return res.status(401).json({message: "Unauthorized purchase activity"});
        }

        const reqBody = req.body;

        const response = await fetchWithSessionTokens(sessionId,
            async (sessionData) => {
                return await axiosBackendClient.post(`${Backend_Url}/purchase/complete`, reqBody, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'x-client_type': WEB_CLIENT_NAME,
                        ...(!sessionData?.is_guest && {'Authorization': 'Bearer ' + sessionData?.access_token}),
                        ...(sessionData?.session_id && {'x-session-id': sessionData?.session_id}),
                    },
                    body: JSON.stringify(req.body),
                    bffContext: {
                        req, res
                    }
                })
            },
            {req, res});

        // console.log("Purchase response: ", JSON.stringify(response));

        const {data: responseData, newSessionId} = response;

        sessionId = newSessionId || sessionId;

        const cartSummaryResponse = await getCartSummary(req, res, sessionId);

        responseData.cartSummary = cartSummaryResponse?.data;

        return res.status(response.status).json(responseData || {});

    } catch (error) {
        console.error("error in complete purchase: ", error)

        if (error.response) {
            console.warn(`${timestamp()} Handled backend error for complete purchase request`);
            return res.status(error.response.status || 500).json(error.response.data);
        }

        return res.status(500).end();
    }
})

export default router

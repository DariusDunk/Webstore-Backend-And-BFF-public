// noinspection DuplicatedCode


import express from 'express';
import {Backend_Url, WEB_CLIENT_NAME} from './config.js';
import {fetchWithSessionTokens} from "../services/requestTokenManager.js";
import axiosBackendClient from '../axiosBackendClient.js';
import {getCartSummary} from "../services/cartSummaryFetcher.js"

const router = express.Router();

const timestamp = () => {
    const now = new Date();
    return `[${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')} ${String(now.getHours()).padStart(2, '0')}-${String(now.getMinutes()).padStart(2, '0')}-${String(now.getSeconds()).padStart(2, '0')}]`;
};

const authIntentHeader = 'x-auth-intent';


router.get('/createCartForSession', async (req, res) => {
    try {
        const sessionId = req.headers["x-session-id"];
        const authIntent = req.get(authIntentHeader);

        if (authIntent && (sessionId === undefined || sessionId === null)) {
            return res.status(401).json({message: "Unauthorized cart activity"});
        }

        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
            return await axiosBackendClient.get(`${Backend_Url}/cart/createCartForSession`, {
                    headers: {
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
        }, {req, res});

        return res.status(response.status).end();

    } catch (error) {
        if (error.response) {
            console.warn(`${timestamp()} Handled backend error for creating cart`);
            return res.status(error.response.status || 500).end();
        }
        console.error('-------------------Error creating cart-------------------\n', error);
        return res.status(500).end();
    }
})

router.get('/getCart', async (req, res) => {
    let sessionId = req.headers["x-session-id"];
    
    const authIntent = req.get(authIntentHeader);

    if (authIntent && (sessionId === undefined || sessionId === null)) {
        return res.status(401).json({message: "Unauthorized cart activity"});
    }

    try {
        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
            return await axiosBackendClient.get(`${Backend_Url}/cart/get`, {
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
        }, {req, res})

        const {data: cartResponseData, newSessionId} = response;


        sessionId = newSessionId || sessionId;


        const cartSummaryResponse = await getCartSummary(req, res, sessionId);

        const cartSummaryData = await cartSummaryResponse?.data;

        const responseData = {products: cartResponseData, cartSummary: cartSummaryData};

        return res.status(response.status).json(responseData);

    } catch (error) {

        if (error.response) {
            console.warn(`${timestamp()} Handled backend error for fetching the cart`);
            return res.status(error.response.status || 500).end();
        }

        console.error('-------------------Unexpected error for fetching the cart-------------------\n', error);
        return res.status(500).end();
    }
});

router.post('/addToCart', async (req, res) => {
    try {
        const {productCode, doIncrement} = req.body;
        let sessionId = req.headers["x-session-id"];

        console.log("Add to cart body: " + JSON.stringify(req.body) + "")

        const authIntent = req.get(authIntentHeader);

        // console.log("Auth intent in AddToCart: " + authIntent);

        if (authIntent && (sessionId === undefined || sessionId === null)) {

            // console.log("Zombie request detected, action rejected");

            return res.status(401).json({message: "Unauthorized cart activity"});
        }

        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
            return await axiosBackendClient.post(`${Backend_Url}/cart/manageQuant`, {
                product_code: productCode,
                do_increment: doIncrement
            }, {
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
        }, {req, res})

        const {data: responseData, newSessionId} = response;

        sessionId = newSessionId || sessionId;

        const cartSummaryResponse = await getCartSummary(req, res, sessionId);
        const cartSummaryData = await cartSummaryResponse?.data;

        return res.status(response.status).json({message: responseData, cartSummary: cartSummaryData});

    } catch (error) {

        if (error.response) {
            console.warn(`${timestamp()} Handled backend error for adding product to cart`);
            return res.status(error.response.status || 500).json(error.response.data);
        }

        console.error('-------------------Unexpected error adding product to cart-------------------\n', error);
        return res.status(500).end();
    }
});

router.post('/add/quantity', async (req, res) => {
    try {
        const {productCode, quantity} = req.body;
        let sessionId = req.headers["x-session-id"];

        const authIntent = req.get(authIntentHeader);

        if (authIntent && (sessionId === undefined || sessionId === null)) {
            return res.status(401).json({message: "Unauthorized cart activity"});
        }

        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
            return await axiosBackendClient.post(`${Backend_Url}/cart/add/quantity`, {
                product_code: productCode,
                quantity: quantity
            }, {
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
        }, {req, res});

        const {data: responseData, newSessionId} = response;

        sessionId = newSessionId || sessionId;

        const cartSummaryResponse = await getCartSummary(req, res, sessionId);
        const cartSummaryData = await cartSummaryResponse?.data;
        return res.status(response.status).json({message: responseData, cartSummary: cartSummaryData});
    } catch (error) {
        if (error.response) {
            console.warn(`${timestamp()} Handled backend error for adding product quantity to cart`);
            return res.status(error.response.status || 500).json(error.response.data);
        }

        console.error('-------------------Unexpected error adding product quantity to cart-------------------\n', error);
        return res.status(500).end();
    }
})

router.post('/addToCart/batch', async (req, res) => {
    try {
        const productCodes = req.body;
        let sessionId = req.headers["x-session-id"];

        const authIntent = req.get(authIntentHeader);

        if (authIntent && (sessionId === undefined || sessionId === null)) {
            return res.status(401).json({message: "Unauthorized cart activity"});
        }

        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
            return await axiosBackendClient.post(`${Backend_Url}/cart/add/batch`, productCodes, {
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
        }, {req, res})

        const {data: responseData, newSessionId} = response;

        sessionId = newSessionId || sessionId;

        const cartSummaryResponse = await getCartSummary(req, res, sessionId);
        const cartSummaryData =  cartSummaryResponse?.data;

        if (responseData)
            return res.status(response.status).json({messageResponse: responseData, cartSummaryResponse: cartSummaryData});
        else
            return res.status(response.status).end();

    } catch (error) {

        if (error.response) {
            console.warn(`${timestamp()} Handled backend error for batch adding products to cart`);
            return res.status(error.response.status || 500).json(error.response.data);
        }

        console.error('-------------------Error batch adding products to cart-------------------\n', error);
        return res.status(500).end();
    }
})

router.post('/removeFromCart/:productCode', async (req, res) => {
    try {

        const productCode = req.params.productCode;
        let sessionId = req.headers["x-session-id"];

        const authIntent = req.get(authIntentHeader);

        if (authIntent && (sessionId === undefined || sessionId === null)) {
            return res.status(401).json({message: "Unauthorized cart activity"});
        }

        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
            return await axiosBackendClient.delete(`${Backend_Url}/cart/remove/${productCode}`, {
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
        }, {req, res})

        const {data: responseData, newSessionId} = response;


        sessionId = newSessionId || sessionId;


        const cartSummaryResponse = await getCartSummary(req, res, sessionId);
        const cartSummaryData = await cartSummaryResponse?.data;

        return res.status(response.status).json({products: responseData, cartSummary: cartSummaryData});

    } catch (error) {

        if (error.response) {
            console.warn(`${timestamp()} Handled backend error for removing product from cart`);
            return res.status(error.response.status || 500).end();
        }

        console.error('-------------------Error removing product from cart-------------------\n', error);
        return res.status(500).end();
    }
})

router.post(`/removeFromCart/batch/turbo`, async (req, res) => {
    try {

        let sessionId = req.headers["x-session-id"];
        const productCodes = req.body;

        const authIntent = req.get(authIntentHeader);

        if (authIntent && (sessionId === undefined || sessionId === null)) {
            return res.status(401).json({message: "Unauthorized cart activity"});
        }

        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
            return await axiosBackendClient.delete(`${Backend_Url}/cart/remove/batch`, {
                headers: {
                    'Content-Type': 'application/json',
                    'x-client_type': WEB_CLIENT_NAME,
                    ...(!sessionData?.is_guest && {'Authorization': 'Bearer ' + sessionData?.access_token}),
                    ...(sessionData?.session_id && {'x-session-id': sessionData?.session_id}),
                },
                data: JSON.stringify(productCodes),
                bffContext: {
                    req, res
                }
            });
        }, {req, res})

        const {data: responseData, newSessionId} = response;

        sessionId = newSessionId || sessionId;

        const cartSummaryResponse = await getCartSummary(req, res, sessionId);
        const cartSummaryData = await cartSummaryResponse?.data;

        return res.status(response.status).json({products: responseData, cartSummary: cartSummaryData});

    } catch (error) {

        if (error.response) {
            console.warn(`${timestamp()} Handled backend error for batch removing product from cart`);
            return res.status(error.response.status || 500).end();
        }

        console.error('-------------------Error batch removing product from cart-------------------\n', error);
        return res.status(500).end();
    }
})

export default router;
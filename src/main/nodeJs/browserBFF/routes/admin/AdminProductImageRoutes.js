import express from 'express';
const router = express.Router();
import {Backend_Url, WEB_CLIENT_NAME} from '../config.js';
import axiosBackendClient from '../../axiosBackendClient.js';
import fetchWithSessionTokens from "../../services/requestTokenManager.js";
import multer from "multer";
const upload = multer();
import FormData from "form-data";
// import { Readable } from "stream";

const CONTROLLER_ROUTE = `${Backend_Url}/admin/product-image`;

router.post("/upload/:id", upload.fields([
    { name: "mainImageFile", maxCount: 1 },
    { name: "galleryFiles", maxCount: 50 }
]), async (req, res) => {

    const sessionId = req.cookies.session_id;
    const formData = new FormData();
    const { id } = req.params;


    // console.log("files: "+req.files);
    // console.log("body: "+JSON.stringify(req.body));

    try {


        formData.append(
            "replaceMainImage",
            req.body.replaceMainImage
        );


        if (req.body.existingGalleryImages) {

            const existing = Array.isArray(req.body.existingGalleryImages)
                ? req.body.existingGalleryImages
                : [req.body.existingGalleryImages];

            existing.forEach(name => {
                formData.append("existingGalleryImages", name);
            });
        }

        if (req.files?.mainImageFile?.length > 0) {
            const file = req.files.mainImageFile[0];

            formData.append(
                "mainImageFile",
                file.buffer,
                file.originalname
            );
        }


        if (req.files?.galleryFiles?.length > 0) {
            for (const file of req.files.galleryFiles) {
                formData.append(
                    "galleryFiles",
                    file.buffer,
                    file.originalname
                );
            }
        }


        const response = await fetchWithSessionTokens(
            sessionId,
            async (sessionData) => {
                return axiosBackendClient.post(
                    `${CONTROLLER_ROUTE}/upload/${id}`,
                    formData,
                    {
                        headers:
                            {
                            ...formData.getHeaders(),
                            'x-client_type': WEB_CLIENT_NAME,
                            ...(!sessionData?.is_guest && {'Authorization': 'Bearer ' + sessionData?.access_token}),
                            ...(sessionData?.session_id && {'x-session-id': sessionData?.session_id}),

                        }
                        ,
                        bffContext: {
                            req, res
                        }
                    }
                );
            },
            { req, res }
        );

        return res.status(response.status).end();

    } catch (error) {
        if (error.response) {
            return res.status(error.response.status).json(error.response.data);
        }

        return res.status(500).json({ error: "Internal server error" });
    }
});

router.get(`/of-product/:productId`, async (req, res) => {
    const sessionId = req.cookies.session_id;
    const {productId} = req.params;
    try {
        const response = await fetchWithSessionTokens(sessionId, async (sessionData) => {
            return await axiosBackendClient.get(`${CONTROLLER_ROUTE}/of-product/${productId}`, {
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
        console.error('-------------------Error fetching product images-------------------\n', error);
        if (error.response)
            return res.status(error.response.status || 500).json(error.response.data);
        return res.status(500).end();
    }
})

export default router;
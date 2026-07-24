// noinspection DuplicatedCode


import express from  'express';
const router = express.Router();
import {Backend_Url, WEB_CLIENT_NAME} from './config.js';
import axiosBackendClient from '../axiosBackendClient.js';
import {fetchWithSessionTokens} from "../services/requestTokenManager.js";

router.get('/names', async (req, res)=> {
  try {
      const sessionId = req.headers["x-session-id"];

      const response = await fetchWithSessionTokens(sessionId, async (sessionData)=>{
         return await axiosBackendClient.get(`${Backend_Url}/category/names`, {
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
      const responseData = response.data;

      return res.status(response.status).json(responseData || {})

  } catch (error) {
    console.error('-------------------Error fetching category names data-------------------\n', error);
    return res.status(error.response?.status || 500).end();
  }
});

export default router;

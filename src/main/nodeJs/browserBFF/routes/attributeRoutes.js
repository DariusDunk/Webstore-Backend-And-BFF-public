// noinspection DuplicatedCode


import express from 'express';
const router = express.Router();
import {Backend_Url, WEB_CLIENT_NAME} from './config.js';
import axiosBackendClient from '../axiosBackendClient.js';
import fetchWithSessionTokens from "../services/requestTokenManager.js";

router.get('/getFilters/:categoryName', async (req, res)=>{

  const categoryName = req.params.categoryName
  const sessionId = req.cookies.session_id;

  try
  {
      const response = await fetchWithSessionTokens(sessionId, async (sessionData)=>{
             return await axiosBackendClient.get(`${Backend_Url}/category/filters?categoryName=${categoryName}`, {
                  headers: {
                      'Content-Type': 'application/json',
                      'x-client_type': WEB_CLIENT_NAME,
                      ...(!sessionData?.is_guest && {'Authorization': 'Bearer ' + sessionData?.access_token}),
                      ...(sessionData?.session_id && {'x-session-id': sessionData?.session_id}),
                  },
                  bffContext: {
                      req, res
                  }
              })
          }, {req, res}
      );

      const responseData = response.data;

    return res.status(response.status).json(responseData || {})
  }catch (error) {
    console.error('-------------------Error getting category filters-------------------\n', error);
    return res.status(error.response?.status || 500).end();
  }
})

export default router;

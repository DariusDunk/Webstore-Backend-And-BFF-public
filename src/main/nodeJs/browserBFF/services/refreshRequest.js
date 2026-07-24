import axios from "axios";
import {Backend_Url, WEB_CLIENT_NAME} from "../routes/config.js";

 async function fetchTokensOfSession(sessionID) {

    try {
        const {data} = await axios.get(`${Backend_Url}/auth/tokens`,
            {
                headers: {
                    'Content-Type': 'application/json',
                    'x-client_type': WEB_CLIENT_NAME,
                    ...(sessionID && {'x-session-id': sessionID})
                }
            });
        return data;
    } catch (error) {
        console.error("Error fetching tokens of session: ", error);
        throw error;
    }
}

export default fetchTokensOfSession;
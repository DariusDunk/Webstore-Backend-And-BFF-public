export default function getExtractedExpiration(accessToken) {
    try {
        const payloadBase64 = accessToken.split('.')[1];
        const payloadJson = JSON.parse(Buffer.from(payloadBase64, 'base64').toString());

        return payloadJson.exp * 1000;
        // return payloadJson.exp;
    } catch (error) {
        return 0; //
    }
}
export const TOKEN_REFRESH_BUFFER_MS = 60 * 1000;

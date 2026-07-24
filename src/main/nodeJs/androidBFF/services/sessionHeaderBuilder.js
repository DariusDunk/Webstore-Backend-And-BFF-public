export function sessionHeaderBuilder(res, sessionId, ttl) {
    if (res) {
        res.setHeader('x-session-id', sessionId);
        res.setHeader('x-session-ttl', ttl);
    }
}

export function deleteSessionHeaders(res) {
   if (res)
    {
        res.setHeader('x-session-id', "");
        res.setHeader('x-session-ttl', 0);
    }
}

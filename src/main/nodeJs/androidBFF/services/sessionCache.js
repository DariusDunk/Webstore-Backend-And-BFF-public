// noinspection DuplicatedCode

import NodeCache from "node-cache";

const ONE_HOUR = 60 * 60;

const cache = new NodeCache({
    stdTTL: ONE_HOUR,
    checkperiod: 60
});

export default {
    get(sessionId) {

        if (!sessionId) return null;

        return cache.get(sessionId);
    },

    // set(sessionId, value, ttl) {
    //     cache.set(sessionId, value, ttl);
    // },

    setSession(sessionId,
               access_token,
               is_guest,
               is_remember_me,
               sessionExpiresIn){

        const entryTTL = Math.min((sessionExpiresIn ?? 660), ONE_HOUR) ;

        console.log("Added new session to the cache: ", sessionId, "with ttl: ", entryTTL/60, " minutes.");

       cache.set(sessionId, {
            session_id:sessionId,
            access_token: access_token,
            is_guest: is_guest,
            is_remember_me: is_remember_me,
            session_expires_in: sessionExpiresIn
        },entryTTL);

    },

    safeDelete(sessionId) {
        if (!cache.has(sessionId)) return;
        cache.del(sessionId);
    },

    ttl(sessionId, newTTL) {
        cache.ttl(sessionId, newTTL);
    },

    print() {
        console.log("cache data: \n" + JSON.stringify(cache.data));
    },
    //
    // has(sessionId) {
    //     return cache.has(sessionId);
    // }
};
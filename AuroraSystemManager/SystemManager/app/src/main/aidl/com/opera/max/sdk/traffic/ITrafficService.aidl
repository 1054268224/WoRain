package com.opera.max.sdk.traffic;

import com.opera.max.sdk.traffic.TrafficEntry;

interface ITrafficService {
    /**
     * Set current IMSI for network traffic consuming.
     */
    void setImsi(String imsi);

    /**
     * Get traffic statistics summary for all apps that have network traffic during specific time span.
     *
     * @param imsi The IMSI selected for query data traffic usage, null means selecting all imsi.
     * @param fromMill Start time for traffic query, milliseconds since Jan. 1, 1970, midnight GMT.
     * @param toMill End time for traffic query, milliseconds since Jan. 1, 1970, midnight GMT.
     * @param includeSavingOffTraffic whether return the traffic that is generated during saving is off.
     * @return The actual concrete class returned is HashMap<Integer, TrafficEntry>, the key is app uid,
     *        return null if no imsi is matched.
     */
    Map getTrafficSummaryForAllUids(String imsi, long fromMill, long toMill, boolean includeSavingOffTraffic);

    /**
     * Get traffic statistics summary for specific app during specific time span.
     *
     * @param imsi Used to indicate the SIM card for query network traffic, null means selecting all imsi.
     * @param uid uid of the app
     * @param fromMill Start time for query data traffic usage
     * @param toMill  End time for query data traffic usage
     * @param includeSavingOffTraffic whether return the traffic that is generated during saving is off.
     * @return null if no imsi and uid are matched.
     */
    TrafficEntry getTrafficSummaryByUid(String imsi, int uid, long fromMill, long toMill, boolean includeSavingOffTraffic);

    /**
     * Get traffic statistics for specific app during specific time span.
     *
     * @param imsi Used to indicate the SIM card for query data traffic usage, null means selecting all imsi.
     * @param uid uid of the app
     * @param fromMill Start time for query data traffic usage
     * @param toMill End time for query data traffic usage
     * @param interval Used to slice the total data usage into sub-pieces.
     * @param includeSavingOffTraffic whether return the traffic that is generated during saving is off.
     * @return Array of TrafficEntry for data usage, null if no imsi and uid are matched.
     */
    TrafficEntry[] getTrafficForUid(String imsi, int uid, long fromMill, long toMill, long interval, boolean includeSavingOffTraffic);
}

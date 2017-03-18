package com.secarp.protocol;

/**
 * An asbtraction for classes that wait upon incoming packets
 */
public interface Receivable {
    /**
     * Incoming packet handler
     * This function is called whenever a packet arrives
     *
     * @param packet The received packet
     */
    public void handlePacket(Packet packet);
}

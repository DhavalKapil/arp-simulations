package com.secarp.protocol.arp;

import com.secarp.address.Ipv4Address;
import com.secarp.address.MacAddress;
import com.secarp.protocol.Header;

/**
 * The ARP header
 */
public class ArpHeader extends Header {
    // Sender Hardware Address
    private MacAddress senderMac;

    // Sender Protocol Address
    private Ipv4Address senderIp;

    // Receiver Hardware Address
    private MacAddress receiverMac;

    // Receiver Protocol Address
    private Ipv4Address receiverIp;

    // Packet type
    private ArpType arpType;

    /**
     * Constructor function
     */
    public ArpHeader(MacAddress senderMac,
                     Ipv4Address senderIp,
                     MacAddress receiverMac,
                     Ipv4Address receiverIp,
                     ArpType arpType
                     ) {
        this.senderMac = senderMac;
        this.senderIp = senderIp;
        this.receiverMac = receiverMac;
        this.receiverIp = receiverIp;
        this.arpType = arpType;
    }

    /**
     * Getter functions
     */
    public MacAddress getSenderMac() {
        return this.senderMac;
    }

    public Ipv4Address getSenderIp() {
        return this.senderIp;
    }

    public MacAddress getReceiverMac() {
        return this.receiverMac;
    }

    public Ipv4Address getReceiverIp() {
        return this.receiverIp;
    }

    public ArpType getArpType() {
        return this.arpType;
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public byte[] getBytes() {
        return null; // TODO
    }
}


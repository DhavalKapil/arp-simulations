package com.secarp.protocol.secarp;

import com.secarp.address.Ipv4Address;
import com.secarp.address.MacAddress;
import com.secarp.protocol.Header;
import com.secarp.protocol.arp.ArpType;

/**
 * The SecARP Header
 */
public class SecArpHeader extends Header {
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

    // Packet Sequence Number
    private int sequenceNumber;

    // ARP Flood Flag
    private boolean arpFloodFlag;

    /**
     * Constructor function
     */
    public SecArpHeader(MacAddress senderMac,
                        Ipv4Address senderIp,
                        MacAddress receiverMac,
                        Ipv4Address receiverIp,
                        ArpType arpType,
                        int sequenceNumber,
                        boolean arpFloodFlag
                        ) {
        this.senderMac = senderMac;
        this.senderIp = senderIp;
        this.receiverMac = receiverMac;
        this.receiverIp = receiverIp;
        this.arpType = arpType;
        this.sequenceNumber = sequenceNumber;
        this.arpFloodFlag = arpFloodFlag;
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

    public int getSequenceNumber() {
        return this.sequenceNumber;
    }

    public boolean isArpFloodFlag() {
        return this.arpFloodFlag;
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public byte[] getBytes() {
        return null; //TODO
    }
}

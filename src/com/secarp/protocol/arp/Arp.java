package com.secarp.protocol.arp;

import com.secarp.address.Ipv4Address;
import com.secarp.address.MacAddress;
import com.secarp.common.Timer;
import com.secarp.device.Node;
import com.secarp.protocol.AddressResolutionProtocol;
import com.secarp.protocol.Header;
import com.secarp.protocol.Receivable;
import com.secarp.protocol.Packet;

/**
 * Represents the ARP protocol stack
 */
public class Arp extends AddressResolutionProtocol implements Receivable {
    //The timeout of an entry in seconds
    private static final int TIMEOUT = 60;

    // The ARP cache
    private ArpCache arpCache;

    // The node associated with the protocol
    // Assuming one to one mapping with a particular node
    private Node node;

    /**
     * Constructor function
     */
    public Arp() {
        this.arpCache = new ArpCache(TIMEOUT);
    }

    /**
     * Getter for arpcache
     */
    public ArpCache getArpCache() {
        return this.arpCache;
    }

    /**
     * @{inheritDocs}
     */
    @Override
    public MacAddress getMacAddress(Ipv4Address ipv4Address) {
        MacAddress address;

        while((address = this.arpCache.lookup(ipv4Address)) == null) {
            Packet packet = createRequestPacket(this.node.getMacAddress(),
                                                this.node.getIpv4Address(),
                                                ipv4Address
                                                );

            this.node.sendPacket(packet,
                                 MacAddress.getBroadcast()
                                 );
            // Waiting before looking up in cache
            Timer.sleep(1000);
        }

        return address;
    }

    /**
     * This checks for incoming ARP reply packets and updated cache accordingly
     * TODO: check whether cache needs to be updated using the source of
     * ARP request packets as well?
     *
     * @param packet The incoming packet which is received
     */
    @Override
    public void handlePacket(Packet packet) {
        // Checking if packet is ARP or not
        if ( !(packet.getHeader() instanceof ArpHeader)) {
            return;
        }

        ArpHeader header = (ArpHeader)packet.getHeader();
        // Checking if packet is an ARP reply packet
        if (header.getArpType() == ArpType.REPLY) {
            // Add entry in cache
            this.arpCache.put(header.getSenderIp(),
                              header.getSenderMac()
                              );
            // Logging
            this.node.getLogger().logArpCache();
        } else if (header.getArpType() == ArpType.REQUEST) {
            // Check if target ip matches node's ip or not
            if (this.node.getIpv4Address().equals(header.getReceiverIp())) {
                // Sending a reply
                Packet reply = Arp.createReplyPacket(this.node.getMacAddress(),
                                                     this.node.getIpv4Address(),
                                                     header.getSenderMac(),
                                                     header.getSenderIp()
                                                     );
                this.node.sendPacket(reply,
                                     header.getSenderMac()
                                     );
            }
        }
    }

    /**
     * @{inheritDocs}
     */
    @Override
    public void install(Node node) {
        this.node = node;
        node.setArp(this);
        node.registerReceivable(this);
    }

    /**
     * Creates an ARP request packet
     *
     * @param senderMac The MAC of the sender node
     * @param senderIp The IP of the sender node
     * @param receiverIp The IP of the receiver node
     *
     * @return The ARP Packet
     */
    public static Packet createRequestPacket(MacAddress senderMac,
                                             Ipv4Address senderIp,
                                             Ipv4Address receiverIp
                                             ) {
        MacAddress receiverMac = new MacAddress("00:00:00:00:00:00");
        Header arpHeader = new ArpHeader(senderMac,
                                         senderIp,
                                         receiverMac,
                                         receiverIp,
                                         ArpType.REQUEST
                                         );

        Packet arpPacket = new Packet(arpHeader);
        return arpPacket;
    }

    /**
     * Creates an ARP reply packet
     *
     * @param senderMac The MAC of the sender node
     * @param senderIp The IP of the sender node
     * @param receiverMac The MAC of the receiver node
     * @param receiverIp The IP of the receiver node
     *
     * @return The ARP Packet
     */
    public static Packet createReplyPacket(MacAddress senderMac,
                                           Ipv4Address senderIp,
                                           MacAddress receiverMac,
                                           Ipv4Address receiverIp
                                           ) {
        Header arpHeader = new ArpHeader(senderMac,
                                         senderIp,
                                         receiverMac,
                                         receiverIp,
                                         ArpType.REPLY
                                         );

        Packet arpPacket = new Packet(arpHeader);
        return arpPacket;
    }
}

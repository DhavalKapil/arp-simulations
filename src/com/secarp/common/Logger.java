package com.secarp.common;

import java.util.Map;

import com.secarp.address.Ipv4Address;
import com.secarp.address.MacAddress;
import com.secarp.device.Node;
import com.secarp.protocol.Header;
import com.secarp.protocol.Packet;
import com.secarp.protocol.arp.ArpCache;
import com.secarp.protocol.arp.ArpHeader;
import com.secarp.protocol.arp.ArpType;

/**
 * Logs all events. Each node is mapped to exactly one instace of a Logger
 * Presently, outputs to console
 *
 * Logs the following things:
 * 1. Changes in ARP cache of a node
 * 2. Packets sent and received by a node
 */
public class Logger {
    // The node
    private Node node;

    /**
     * Constructor function
     */
    public Logger(Node node) {
        this.node = node;
    }

    /**
     * Logs the ARP cache
     */
    public void logArpCache() {
        ArpCache cache = this.node.getArpCache();
        Map<Ipv4Address, MacAddress> map = cache.getMap();
        System.out.println("Arp Cache for node: " + this.node.getId());
        for(Ipv4Address ip : map.keySet()) {
            MacAddress mac = map.get(ip);
            System.out.println(ip + "\t" + mac);
        }
        System.out.println();
    }

    /**
     * Logs a packet
     *
     * @param packet The packet to be logged
     * @param sent Whether the packet is sent or received
     */
    public void logPacket(Packet packet, boolean sent) {
        System.out.println("Node " + this.node.getId() + " " +
                           (sent?"sent":"received") + " the following packet:");
        if (packet.getHeader() instanceof ArpHeader) {
            // Arp packet
            ArpHeader header = (ArpHeader)packet.getHeader();

            System.out.println("ARP Packet");
            System.out.println("Type: " +
                               (header.getArpType() == ArpType.REQUEST?
                                "request":"reply")
                               );
            System.out.println("Sender " + header.getSenderMac());
            System.out.println("Sender " + header.getSenderIp());
            System.out.println("Receiver " + header.getReceiverMac());
            System.out.println("Receiver " + header.getReceiverIp());
            System.out.println();
        }

        byte[] payload = packet.getPayload();
        if (payload != null) {
            System.out.println("Payload:");
            System.out.println(new String(payload));
            System.out.println();
        }
    }
}

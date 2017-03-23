package com.secarp.protocol.secarp;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.secarp.address.Ipv4Address;
import com.secarp.address.MacAddress;
import com.secarp.common.Timer;
import com.secarp.device.Node;
import com.secarp.protocol.AddressResolutionProtocol;
import com.secarp.protocol.Header;
import com.secarp.protocol.Receivable;
import com.secarp.protocol.Packet;
import com.secarp.protocol.arp.Arp;
import com.secarp.protocol.arp.ArpCache;
import com.secarp.protocol.arp.ArpType;

/**
 * Represents the SecArp protocol stack
 */
public class SecArp extends AddressResolutionProtocol implements Receivable {
    // Arp Reply Wait time in milliseconds
    private static final int ARP_REPLY_WAIT_TIME = 1000;

    // The timeout for an entry, in seconds, in L1 Cache
    private static final int TTL_ARP_CACHE_L1 = 60;

    // The timeout for an entry, in seconds, in L2 Cache
    private static final int TTL_ARP_CACHE_L2 = 3600;

    // Capacity of sequence number entries
    private static final int SEQUENCE_NUMBER_CAPACITY = 1000;

    // L1 Cache, similar to ARP cache
    private ArpCache L1Cache;

    // L2 Cache
    private ArpCache L2Cache;

    // The node associated with the protocol
    // Assuming one to one mapping with a particular node
    private Node node;

    // Array of Sequence numbers of sent request packets
    private SequenceNumberEntry[] sequenceNumberEntries;

    /**
     * Constructor function
     */
    public SecArp() {
        this.L1Cache = new ArpCache(TTL_ARP_CACHE_L1);
        this.L2Cache = new ArpCache(TTL_ARP_CACHE_L2);
        this.sequenceNumberEntries =
            new SequenceNumberEntry[SEQUENCE_NUMBER_CAPACITY];
    }

    /**
     * Getter for L1 Cache
     */
    public ArpCache getL1Cache() {
        return this.L1Cache;
    }

    /**
     * Getter for L2 Cache
     */
    public ArpCache getL2Cache() {
        return this.L2Cache;
    }

    /**
     * @{inheritDocs}
     */
    @Override
    public MacAddress getMacAddress(Ipv4Address ipv4Address) {
        MacAddress macAddress = L1Cache.lookup(ipv4Address);
        if (macAddress != null) {
            // Entry present in L1Cache
            return macAddress;
        } else if ( (macAddress = L2Cache.lookup(ipv4Address))
                    != null) {
            // Entry present in L2Cache
            if (macAddress.equals(resolveIpToMac(macAddress,
                                                 ipv4Address))) {
                // Unicast ARP request succeeded
                // The old node still has the same ip mac mapping
                // update cache
                L1Cache.put(ipv4Address, macAddress);
                L2Cache.put(ipv4Address, macAddress);
                return macAddress;
            }
        }
        // Need to broadcast request
        macAddress = resolveIpToMac(MacAddress.getBroadcast(),
                                    ipv4Address);
        // Updating cache
        L1Cache.put(ipv4Address, macAddress);
        L2Cache.put(ipv4Address, macAddress);
        return macAddress;
    }

    /**
     * Returns Target Mac Address after sending ARP request packets
     *
     * @param receiverMac Mac Address of the receiver
     * @param receiverIp Ip Address which needs to be resolved
     *
     * @return The mac address found
     */
    public MacAddress resolveIpToMac(MacAddress receiverMac,
                                     Ipv4Address receiverIp) {
        MacAddress targetMacAddress;
        SequenceNumberEntry sequenceNumberEntry = sendRequestPacket(receiverMac,
                                                                    receiverIp,
                                                                    false);

        if (!sequenceNumberEntry.conflict()) {
            // No clash found
            // Conflict will never arise in case of unicast flow as request is
            // sent to only 1 host
            targetMacAddress = sequenceNumberEntry.getMacAddressWithMaxCount();
        } else {
            // Clash found
            sequenceNumberEntry = sendRequestPacket(MacAddress.getBroadcast(),
                                                    receiverIp,
                                                    true
                                                    );
            targetMacAddress = sequenceNumberEntry.getMacAddressWithMaxCount();
        }
        return targetMacAddress;
    }

    /**
     * Get sequence number entry corresponding to the generated request packet
     * @param macAddress Mac address of the receiver
     * @param ipv4Address Ip address which needs to be resolved
     * @param arpFloodFlag Value of the arp flood flag in request packet
     */
    public SequenceNumberEntry sendRequestPacket(MacAddress macAddress,
                                                 Ipv4Address ipv4Address,
                                                 boolean arpFloodFlag) {
        int randomSequenceNumber = generateSequenceNumber();
        // Creating request packet
        Packet requestPacket = createRequestPacket(this.node.getMacAddress(),
                                                   this.node.getIpv4Address(),
                                                   macAddress,
                                                   ipv4Address,
                                                   randomSequenceNumber,
                                                   arpFloodFlag
                                                   );
        // Send Request Packet
        this.node.sendPacket(requestPacket, macAddress);
        // Initializing sequence number entry
        sequenceNumberEntries[randomSequenceNumber] =
            new SequenceNumberEntry(ipv4Address,
                                    Timer.getCurrentTime() + ARP_REPLY_WAIT_TIME
                                    );
        Timer.sleep(ARP_REPLY_WAIT_TIME);
        SequenceNumberEntry sequenceNumberEntry =
            sequenceNumberEntries[randomSequenceNumber];
        // Removing sequence number entry
        sequenceNumberEntries[randomSequenceNumber] = null;
        return sequenceNumberEntry;
    }

    /**
     * This checks for incoming Arp reply packets and updates both the caches
     * accordingly
     *
     * @param packet The incoming packet which is received
     */
    @Override
    public void handlePacket(Packet packet) {
        // Checking if packet is ARP or not
        if (!(packet.getHeader() instanceof SecArpHeader)) {
            return;
        }

        SecArpHeader header = (SecArpHeader)packet.getHeader();

        // Checking if packet is a reply packet
        if (header.getArpType() == ArpType.REPLY) {
            SequenceNumberEntry sequenceNumberEntry =
                sequenceNumberEntries[header.getSequenceNumber()];

            if (sequenceNumberEntry == null) {
                // No request generated for this sequence number
                // Invalid reply packet
                return;
            } else {
                // Update count in stored data structure
                sequenceNumberEntry.updateMacCount(header.getSenderMac());
            }
        } else if (header.getArpType() == ArpType.REQUEST) {
            // Handling request packets
            Packet reply;
            if (this.node.getIpv4Address().equals(header.getReceiverIp())) {
                // This node is the target node
                reply = createReplyPacket(this.node.getMacAddress(),
                                          this.node.getIpv4Address(),
                                          header.getSenderMac(),
                                          header.getSenderIp(),
                                          header.getSequenceNumber()
                                          );
            } else {
                // Trying to lookup in cache
                MacAddress address = L1Cache.lookup(header.getReceiverIp());
                if (address == null) {
                    // Not found in L1Cache
                    address = L2Cache.lookup(header.getReceiverIp());
                    if (address == null) {
                        // Not found in L2Cache
                        // No need to send any reply packet
                        return;
                    }
                }
                reply = createReplyPacket(address, // The new found MAC
                                          header.getReceiverIp(),
                                          header.getSenderMac(),
                                          header.getSenderIp(),
                                          header.getSequenceNumber()
                                          );
            }
            if (header.isArpFloodFlag()) {
                // Flood till a particular time
                long current_time = Timer.getCurrentTimeInMillis();
                while (Timer.getCurrentTimeInMillis() >
                       (current_time + ARP_REPLY_WAIT_TIME)) {
                    this.node.sendPacket(reply,
                                         header.getSenderMac()
                                         );
                }
            } else {
                this.node.sendPacket(reply,
                                     header.getSenderMac()
                                     );
            }
        }
    }

    /**
     * Generates random sequence number
     *
     * @return A random sequence number
     */
    private int generateSequenceNumber() {
        Random rand = new Random();

        int randomSequenceNumber = rand.nextInt(SEQUENCE_NUMBER_CAPACITY);
        while (sequenceNumberEntries[randomSequenceNumber] != null) {
            randomSequenceNumber = rand.nextInt(SEQUENCE_NUMBER_CAPACITY);
        }

        return randomSequenceNumber;
    }

    /**
     * @{inheritDocs}
     */
    @Override
    public void install(Node node) {
        this.node = node;
        this.node.setArp(this);
        node.registerReceivable(this);
    }

    /**
     * Creates an SecARP request packet
     *
     * @param senderMac The MAC of the sender node
     * @param senderIp The IP of the sender node
     * @param receiverIp The IP of the receiver node
     * @param sequenceNumber The sequence number of the packet
     * @param floodFlag The value of the flood flag
     *
     * @return The SecARP Packet
     */
    public static Packet createRequestPacket(MacAddress senderMac,
                                             Ipv4Address senderIp,
                                             MacAddress receiverMac,
                                             Ipv4Address receiverIp,
                                             int sequenceNumber,
                                             boolean floodFlag
                                             ) {
        Header secArpHeader = new SecArpHeader(senderMac,
                                               senderIp,
                                               receiverMac,
                                               receiverIp,
                                               ArpType.REQUEST,
                                               sequenceNumber,
                                               floodFlag
                                               );

        Packet arpPacket = new Packet(secArpHeader);
        return arpPacket;
    }

    /**
     * Creates an ARP reply packet
     *
     * @param senderMac The MAC of the sender node
     * @param senderIp The IP of the sender node
     * @param receiverMac The MAC of the receiver node
     * @param receiverIp The IP of the receiver node
     * @param sequenceNumber The sequence number of the packet
     *
     * @return The ARP Packet
     */
    public static Packet createReplyPacket(MacAddress senderMac,
                                           Ipv4Address senderIp,
                                           MacAddress receiverMac,
                                           Ipv4Address receiverIp,
                                           int sequenceNumber
                                           ) {
        Header secArpHeader = new SecArpHeader(senderMac,
                                               senderIp,
                                               receiverMac,
                                               receiverIp,
                                               ArpType.REPLY,
                                               sequenceNumber,
                                               false
                                               );

        Packet arpPacket = new Packet(secArpHeader);
        return arpPacket;
    }
}

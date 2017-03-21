package com.secarp.protocol.secarp;

import com.secarp.address.Ipv4Address;
import com.secarp.address.MacAddress;
import com.secarp.common.Timer;
import com.secarp.device.Node;
import com.secarp.protocol.*;
import com.secarp.protocol.arp.ArpCache;
import com.secarp.protocol.arp.ArpType;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Represents the SecArp protocol stack
 */
public class SecArp extends AddressResolutionProtocol implements Receivable{

    //Arp flood count
    private static final int ARP_FLOOD_COUNT = 10;

    //Arp Reply Wait time
    private static final int ARP_REPLY_WAIT_TIME = 1000;

    //The timeout for an entry, in seconds, in L1 Cache
    private static final int TTL_ARP_CACHE_L1 = 60;

    //The timeout for an entry, in seconds, in L2 Cache
    private static final int TTL_ARP_CACHE_L2 = 3600;

    //Capacity of sequence number entries
    private static final int SEQUENCE_NUMBER_CAPACITY = 1000;

    //L1 Cache, similar to ARP cache
    private ArpCache L1Cache;

    //L2 Cache
    private ArpCache L2Cache;

    //The node associated with the protocol
    //Assuming one to one mapping with a particular node
    private Node node;

    //Array of Sequence numbers of sent request packets
    SequenceNumberEntry[] sequenceNumberEntries;

    /**
     * Constructor function
     */
    public SecArp() {
        this.L1Cache = new ArpCache(TTL_ARP_CACHE_L1);
        this.L2Cache = new ArpCache(TTL_ARP_CACHE_L2);
        sequenceNumberEntries = new SequenceNumberEntry[SEQUENCE_NUMBER_CAPACITY];
    }

    /**
     * @{inheritDocs}
     */
    @Override
    public MacAddress getTargetMacAddress(Ipv4Address targetIpv4Address) {
        MacAddress targetMacAddress = L1Cache.lookup(targetIpv4Address);
        if(targetMacAddress != null) {
            return targetMacAddress;
        } else if((targetMacAddress = L2Cache.lookup(targetIpv4Address)) != null) {
                if(targetMacAddress.equals(resolveIpToMac(targetMacAddress,
                                                          targetIpv4Address))) {
                    return targetMacAddress;
                } else {
                    return resolveIpToMac(MacAddress.getBroadcast(), targetIpv4Address);
                }
        } else {
            return resolveIpToMac(MacAddress.getBroadcast(), targetIpv4Address);
        }
    }

    /**
     * Returns Target Mac Address after sending ARP request packets
     * @param receiverMac Mac Address of the receiver
     * @param receiverIp Ip Address which needs to be resolved
     */
    public MacAddress resolveIpToMac(MacAddress receiverMac, Ipv4Address receiverIp) {
        MacAddress targetMacAddress;

        HashMap<MacAddress, Integer> macCountMap =
                getMacCountMap(receiverMac, receiverIp, false);

        if(macCountMap.keySet().size() == 1) {
            targetMacAddress = macCountMap.keySet().iterator().next();
            L1Cache.put(receiverIp, targetMacAddress);
            L2Cache.put(receiverIp, targetMacAddress);
            return targetMacAddress;
        } else {
            macCountMap = getMacCountMap(MacAddress.getBroadcast(),
                                         receiverIp,
                                        true
                                         );
            Map.Entry<MacAddress, Integer> maxEntry = null;
            for (Map.Entry<MacAddress, Integer> entry: macCountMap.entrySet()) {
                if (maxEntry == null || entry.getValue().
                                        compareTo(maxEntry.getValue())> 0) {
                    maxEntry = entry;
                }
            }
            L1Cache.put(receiverIp, maxEntry.getKey());
            L2Cache.put(receiverIp, maxEntry.getKey());
            return maxEntry.getKey();
        }
    }

    /**
     * Get mac to count map
     * @param macAddress Mac address of the receiver
     * @param ipv4Address Ip address which needs to be resolved
     * @param arpFloodFlag Value of the arp flood flag in request packet
     */
    public HashMap<MacAddress, Integer> getMacCountMap(MacAddress macAddress,
                                                       Ipv4Address ipv4Address,
                                                       boolean arpFloodFlag) {
        int randomSequenceNumber = generateSequenceNumber();
        Packet requestPacket = createRequestPacket(this.node.getMacAddress(),
                                                   this.node.getIpv4Address(),
                                                   macAddress,
                                                   ipv4Address,
                                                   randomSequenceNumber,
                                                   arpFloodFlag
                                                   );
        this.node.sendPacket(requestPacket, macAddress);
        sequenceNumberEntries[randomSequenceNumber] =
                new SequenceNumberEntry(ipv4Address,
                                        Timer.getCurrentTime() +
                                        ARP_REPLY_WAIT_TIME,
                                        new HashMap<>()
                                        );
        Timer.sleep(ARP_REPLY_WAIT_TIME);
        HashMap<MacAddress, Integer> map = sequenceNumberEntries[randomSequenceNumber].
                                           getMacCountMap();
        sequenceNumberEntries[randomSequenceNumber] = null;
        return map;
    }

    /**
     * This checks for incoming Arp reply packets and updates both the caches accordingly
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

        //Checking if packet is a reply packet
        if (header.getArpType() == ArpType.REPLY) {
            SequenceNumberEntry sequenceNumberEntry = sequenceNumberEntries[header.getSequenceNumber()];
            if (sequenceNumberEntry == null) {
                return;
            } else {
                HashMap<MacAddress, Integer> macCountMap = sequenceNumberEntry.getMacCountMap();
                macCountMap.putIfAbsent(header.getSenderMac(), 0);
                macCountMap.put(header.getSenderMac(), macCountMap.get(header.getSenderMac()) + 1);
            }
        } else if(header.getArpType() == ArpType.REQUEST) {
            if (header.getReceiverMac() == this.node.getMacAddress()) {
                Packet reply = createReplyPacket(this.node.getMacAddress(),
                                                 this.node.getIpv4Address(),
                                                 header.getSenderMac(),
                                                 header.getSenderIp(),
                                                 header.getSequenceNumber()
                                                 );

                this.node.sendPacket(reply, header.getSenderMac());
            } else if (L1Cache.lookup(header.getReceiverIp())!=null) {

                Packet reply = createReplyPacket(header.getReceiverMac(),
                                                 header.getReceiverIp(),
                                                 header.getSenderMac(),
                                                 header.getSenderIp(),
                                                 header.getSequenceNumber()
                                                 );
                if (header.isArpFloodFlag()) {
                    for (int i = 0; i < ARP_FLOOD_COUNT; i++){
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
    }

    /**
     * Generates random sequence number
     */
    private int generateSequenceNumber() {
        Random rand = new Random();

        int randomSequenceNumber = rand.nextInt(SEQUENCE_NUMBER_CAPACITY);
        while (sequenceNumberEntries[randomSequenceNumber] == null) {
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
        this.node.setAddressResolutionProtocol(this);
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

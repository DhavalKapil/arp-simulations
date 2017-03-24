package com.secarp.device;

import com.secarp.address.Ipv4Address;
import com.secarp.address.MacAddress;
import com.secarp.common.Timer;
import com.secarp.protocol.Packet;
import com.secarp.protocol.arp.ArpType;
import com.secarp.protocol.secarp.SecArp;
import com.secarp.protocol.secarp.SecArpHeader;

public class AttackerNode extends Node {
    // The victim ip
    private Ipv4Address victimIp;
    /**
     * Constructor function
     */
    public AttackerNode(Ipv4Address ipv4Address,
                        MacAddress macAddress,
                        Ipv4Address victimIp) {
        super(ipv4Address, macAddress);
        this.victimIp = victimIp;
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public void handlePacket(Packet packet) {
        super.getLogger().logPacket(packet, false);
        if (!(packet.getHeader() instanceof SecArpHeader)) {
            return;
        }
        SecArpHeader header = (SecArpHeader) packet.getHeader();
        if (header.getArpType() == ArpType.REQUEST &&
                header.getSenderIp().matches(victimIp)) {
            // Spawning new thread
            new Thread(new Runnable() {
                public void run() {
                    attack(header);
                }
            }).start();
        }
    }

    /**
     * Attacks the victim node
     *
     * @param header The header of incoming arp sec packet
     */
    public void attack(SecArpHeader header) {
        Packet replyPacket = SecArp.createReplyPacket(
                this.getMacAddress(),
                header.getReceiverIp(),
                header.getSenderMac(),
                header.getSenderIp(),
                header.getSequenceNumber()
        );
        int currentTime = Timer.getCurrentTimeInMillis();
        while (Timer.getCurrentTimeInMillis() <
                (currentTime + 3000)) {
            super.sendPacket(replyPacket, header.getSenderMac());
        }
    }
}
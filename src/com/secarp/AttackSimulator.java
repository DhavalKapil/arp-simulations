package com.secarp;

import com.secarp.address.Ipv4Address;
import com.secarp.address.MacAddress;
import com.secarp.common.Timer;
import com.secarp.device.Node;
import com.secarp.network.Ethernet;
import com.secarp.network.Network;
import com.secarp.protocol.arp.Arp;
import com.secarp.protocol.Packet;
import com.secarp.protocol.Protocol;

public class AttackSimulator {
    public static void main(String args[]) {
        Network network = new Ethernet(10);
        Node[] nodes = new Node[3];
        Protocol[] arps = new Arp[3];

        nodes[0] = new Node(new Ipv4Address("1.1.1.1"),
                new MacAddress("11:11:11:11:11:11")
        );
        network.addNode(nodes[0]);
        arps[0] = new Arp();
        arps[0].install(nodes[0]);

        nodes[1] = new Node(new Ipv4Address("2.2.2.2"),
                new MacAddress("22:22:22:22:22:22")
        );
        network.addNode(nodes[1]);
        arps[1] = new Arp();
        arps[1].install(nodes[1]);

        nodes[2] = new Node(new Ipv4Address("3.3.3.3"),
                new MacAddress("33:33:33:33:33:33")
        );
        network.addNode(nodes[2]);
        arps[2] = new Arp();
        arps[2].install(nodes[2]);

        Packet falseReplyPacket = Arp.createReplyPacket(
                nodes[2].getMacAddress(),
                nodes[1].getIpv4Address(),
                nodes[0].getMacAddress(),
                nodes[0].getIpv4Address()
                );

        new Thread(new Runnable() {
            @Override
            public void run() {
                Timer.sleep(500);
                nodes[2].sendPacket(falseReplyPacket, nodes[0].getMacAddress());
            }
        }).start();

        // Sending packets
        Packet packet = new Packet("test-payload".getBytes());
        nodes[0].sendPacket(packet, nodes[1].getIpv4Address());
    }
}

package com.secarp;

import com.secarp.address.Ipv4Address;
import com.secarp.address.MacAddress;
import com.secarp.device.AttackerNode;
import com.secarp.device.Node;
import com.secarp.network.Ethernet;
import com.secarp.network.Network;
import com.secarp.protocol.Packet;
import com.secarp.protocol.Protocol;
import com.secarp.protocol.secarp.SecArp;

/**
 * Simulates:
 * - 4 nodes in the network using SecArp
 * - node[4] sends data packet to node[1]
 * - so node[4] has node[1]'s ip and mac in cache
 * - node[0] sends data packet to node[1]
 * - node[2] attacks node[0]
 * - node[2] knows sequence number
 * - Unsuccessful attack
 */
public class AttackSecArpSimulator3 {
    public static void main(String args[]) {
        Network network = new Ethernet(10);
        Node[] nodes = new Node[4];
        Protocol[] arps = new SecArp[4];

        nodes[0] = new Node(new Ipv4Address("1.1.1.1"),
                new MacAddress("11:11:11:11:11:11")
        );
        network.addNode(nodes[0]);
        arps[0] = new SecArp();
        arps[0].install(nodes[0]);

        nodes[1] = new Node(new Ipv4Address("2.2.2.2"),
                new MacAddress("22:22:22:22:22:22")
        );
        network.addNode(nodes[1]);
        arps[1] = new SecArp();
        arps[1].install(nodes[1]);

        nodes[2] = new AttackerNode(new Ipv4Address("3.3.3.3"),
                new MacAddress("33:33:33:33:33:33"),
                new Ipv4Address("1.1.1.1")
        );
        network.addNode(nodes[2]);
        arps[2] = new SecArp();
        arps[2].install(nodes[2]);

        nodes[3] = new Node(new Ipv4Address("4.4.4.4"),
                new MacAddress("44:44:44:44:44:44:44")
        );
        network.addNode(nodes[3]);
        arps[3] = new SecArp();
        arps[3].install(nodes[3]);


        // Sending packets
        Packet packet = new Packet("node4-node1 payload".getBytes());
        nodes[3].sendPacket(packet, nodes[1].getIpv4Address());

        packet = new Packet("test-payload".getBytes());
        nodes[0].sendPacket(packet, nodes[1].getIpv4Address());
    }
}
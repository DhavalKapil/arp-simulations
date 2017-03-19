package com.secarp;

import com.secarp.address.*;
import com.secarp.device.*;
import com.secarp.network.*;
import com.secarp.protocol.*;
import com.secarp.protocol.arp.*;

public class Simulator {
    public static void main(String[] args) {
        Network network = new Ethernet(20);
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

        // Sending packets
        Packet packet = new Packet("test-payload".getBytes());
        nodes[0].sendPacket(packet, nodes[2].getIpv4Address());
    }
}

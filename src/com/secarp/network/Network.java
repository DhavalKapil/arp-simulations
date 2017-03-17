package com.secarp.network;

import com.secarp.address.MacAddress;
import com.secarp.common.CircularQueue;
import com.secarp.device.Node;
import com.secarp.protocol.Packet;

/**
 * An abstraction of a particular network
 * This network can be Ethernet, CDMA, etc.
 */
public abstract class Network {

    // List of nodes in the network;
    protected Node[] nodes;

    // Receive packet queue for each node
    private CircularQueue<Packet>[] recQ;

    // The id of the new node to be added
    private int id;

    /**
     * Constructor
     *
     * @param size The expected max size of the network
     * @param capacity The capacity of the receiving queue
     */
    public Network(int size, int capacity) {
        this.nodes = new Node[size];
        this.recQ = (CircularQueue<Packet>[])new Object[capacity];
        this.id = 0;
    }

    /**
     * Adds a new node to the network
     *
     * @param Node The node to be added
     *
     * @return Whether the operation was executed or not
     */
    public boolean addNode(Node node) {
        if (this.id == this.nodes.length) {
            // Array is already full
            return false;
        }
        node.setId(this.id);
        this.nodes[this.id] = node;
        this.id++;
        return true;
    }

    /**
     * Send a packet from a particular node id to a particular MAC address
     *
     * @param id The id of the node
     * @param packet The packet to send
     * @param address The MAC address of the target node/nodes
     */
    public abstract void sendPacket(int id,
                                    Packet packet,
                                    MacAddress address);
}

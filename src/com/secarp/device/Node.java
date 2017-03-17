package com.secarp.device;

import com.secarp.address.Ipv4Address;
import com.secarp.address.MacAddress;
import com.secarp.protocol.Packet;

/**
 * An abstraction of a particular node
 */
public class Node {
    // The id of the node
    private int id;

    // Ipv4 address
    private Ipv4Address ipv4Address;

    // Mac address
    private MacAddress macAddress;

    /**
     * Constructor function
     */
    public Node(Ipv4Address ipv4Address, MacAddress macAddress) {
        this.ipv4Address = ipv4Address;
        this.macAddress = macAddress;
    }

    /**
     * Getter and setter functions
     */
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Ipv4Address getIpv4Address() {
        return this.ipv4Address;
    }

    public void setIpv4Address(Ipv4Address ipv4Address) {
        this.ipv4Address = ipv4Address;
    }

    public MacAddress getMacAddress() {
        return this.macAddress;
    }

    public void setMacAddress(MacAddress macAddress) {
        this.macAddress = macAddress;
    }

    /**
     * Handles a packet receive operation
     *
     * @param Packet The received packet
     */
    public void handlePacket(Packet packet) {
        // TODO
    }
}

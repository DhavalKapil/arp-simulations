package com.secarp.device;

import java.util.*;

import com.secarp.address.Ipv4Address;
import com.secarp.address.MacAddress;
import com.secarp.protocol.Packet;
import com.secarp.protocol.Receivable;

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

    // The registered receivable instances that handle incoming packets
    private ArrayList<Receivable> receivables;

    /**
     * Constructor function
     */
    public Node(Ipv4Address ipv4Address, MacAddress macAddress) {
        this.ipv4Address = ipv4Address;
        this.macAddress = macAddress;
        this.receivables = new ArrayList<Receivable>();
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

    public ArrayList<Receivable> getReceivables() {
        return this.receivables;
    }

    public void setReceivables(ArrayList<Receivable> receivables) {
        this.receivables = receivables;
    }

    /**
     * Registers a new receivable
     *
     * @param Receivable The new receivable to be registered
     */
    public void registerReceivable(Receivable receivable) {
        this.receivables.add(receivable);
    }

    /**
     * Handles a packet receive operation
     * Iteratively calls all the receivables in a separate thread
     *
     * @param Packet The received packet
     */
    public void handlePacket(Packet packet) {
        for (Receivable receivable : this.receivables) {
            // Spawning a new thread
            // The good old Java way :)
            new Thread(new Runnable() {
                    @Override
                    public void run() {
                        receivable.handlePacket(packet);
                    }
                }).start();
        }
    }
}

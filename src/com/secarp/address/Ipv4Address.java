package com.secarp.address;

/**
 * An abstraction for the IPv4 address
 */
public class Ipv4Address extends Address {
    // String representation of the address
    private String address;

    /**
     * Constructor
     */
    public Ipv4Address(String address) {
        this.address = address;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBroadcast() {
        if (this.address.equals("255.255.255.255")) {
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getBytes() {
        // Simulation purposes, simply convert
        return this.address.getBytes();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("Ipv4 Address: %s",
                             this.address);
    }

    /**
     * Implements equality of addresses
     *
     * @param ipv4Address
     *
     * @return
     */
    public boolean matches(Ipv4Address ipv4Address) {
        return this.address.equals(ipv4Address.address);
    }
}

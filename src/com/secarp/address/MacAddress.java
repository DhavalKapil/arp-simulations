package com.secarp.address;

/**
 * An abstraction for the MAC address
 */
public class MacAddress extends Address {
    // String representation of the address
    private String address;

    /**
     * Constructor
     */
    public MacAddress(String address) {
        this.address = address;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBroadcast() {
        if (this.address.equals("FF:FF:FF:FF:FF:FF")) {
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
        return String.format("Mac Address: %s",
                             this.address);
    }
}

package com.secarp.address;

/**
 * An abstraction for the MAC address
 */
public class MacAddress extends Address {
    // String representation of the address
    private String address;

    // The broadcast mac address as String
    private static final String BROADCAST_STRING = "FF:FF:FF:FF:FF:FF";

    /**
     * Constructor
     */
    public MacAddress(String address) {
        this.address = address;
    }

    /**
     * Returns an instance of the Broadcast address
     *
     * @return A broadcast MAC address
     */
    public static MacAddress getBroadcast() {
        return new MacAddress(BROADCAST_STRING);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBroadcast() {
        if (this.address.equals(BROADCAST_STRING)) {
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

    /**
     * Implements equality of addresses
     *
     * @param macAddress
     *
     * @return
     */
    public boolean matches(MacAddress macAddress) {
        return this.address.equals(macAddress.address);
    }
}

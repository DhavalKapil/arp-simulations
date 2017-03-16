package com.secarp.address;

/**
 * An abstraction for different kinds of addresses used by the underlying
 * network.
 */
abstract class Address {
    /**
     * Checks for broadcast address
     *
     * @return Whether the address is a broadcast address or not
     */
    public abstract boolean isBroadcast();

    /**
     * Returns a byte array representation of the address.
     *
     * @return An array of bytes
     */
    public abstract byte[] getBytes();
}

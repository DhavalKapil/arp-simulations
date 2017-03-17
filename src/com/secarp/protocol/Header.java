package com.secarp.protocol;

/**
 * An Abstraction for the header of a packet
 */
public abstract class Header {
    /**
     * Returns a byte representation of the header
     */
    public abstract byte[] getBytes();
}

package com.secarp.protocol;

/**
 * An abstraction for a packet that is sent between nodes in the network
 */
public class Packet {
    // The header
    Header header;

    // The payload
    byte[] payload;

    /**
     * Constructors
     */
    public Packet() {
        this(null, null);
    }

    public Packet(Header header) {
        this(header, null);
    }

    public Packet(byte[] payload) {
        this(null, payload);
    }

    public Packet(Header header, byte[] Payload) {
        this.header = header;
        this.payload = payload;
    }

    /**
     * Getters and Setters
     */
    public Header getHeader() {
        return this.header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public byte[] getPayload() {
        return this.payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }
}

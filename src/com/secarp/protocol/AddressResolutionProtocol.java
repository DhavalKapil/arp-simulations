package com.secarp.protocol;

import com.secarp.address.Ipv4Address;
import com.secarp.address.MacAddress;

/**
 * An abstraction for different kinds of Address resolution protocols
 */
public abstract class AddressResolutionProtocol extends Protocol {
    /**
     * Returns MAC address for a particular IPv4 address by using the
     * underlying ARP protocol
     *
     * @param ipv4Address Ip address of the target node
     * @return Mac Address of the target node
     */
    public abstract MacAddress getMacAddress(Ipv4Address ipv4Address);
}

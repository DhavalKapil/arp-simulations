package com.secarp.protocol;

import com.secarp.address.Ipv4Address;
import com.secarp.address.MacAddress;

/**
 * An abstraction for different kinds of Address resolution protocols
 */
public abstract class AddressResolutionProtocol extends Protocol {
    /**
     *
     * @param targetIpv4Address Ip address of the target node
     * @return Mac Address of the target node
     */
    public abstract MacAddress getTargetMacAddress(Ipv4Address targetIpv4Address);
}

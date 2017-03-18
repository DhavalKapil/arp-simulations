package com.secarp.protocol;

import com.secarp.device.Node;

/**
 * An abstraction for different kinds of protocol at the Network Layer
 */
public abstract class Protocol {
    /**
     * This function should install the protocol stack in a particular Node
     *
     * @param node The node to which the stack is to be installed
     */
    public abstract void install(Node node);
}

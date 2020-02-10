package net.earthcomputer.multiconnect.api;

/**
 * Contains information about a supported protocol version
 */
public interface IProtocol {

    /**
     * Returns the protocol version ID, to be compared with values in {@link Protocols}.
     */
    int getValue();

    /**
     * Returns the name of the Minecraft version that this protocol represents.
     */
    String getName();

}

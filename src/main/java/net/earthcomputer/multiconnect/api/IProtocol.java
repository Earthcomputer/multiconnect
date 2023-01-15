package net.earthcomputer.multiconnect.api;

import java.util.List;

/**
 * Contains information about a supported protocol version
 */
public interface IProtocol {

    /**
     * Returns the protocol version ID, to be compared with values in {@link Protocols}.
     */
    @ThreadSafe
    int getValue();

    /**
     * Returns the name of the Minecraft version that this protocol represents.
     */
    @ThreadSafe
    String getName();

    /**
     * Returns the datafix version of this protocol
     */
    @ThreadSafe
    int getDataVersion();

    /**
     * Returns whether this version is considered a major release, i.e. a parent of other protocols,
     * where those protocols are subcategories. Note: the concept of a "major release" is not necessarily the
     * same as semver. It is used to categorize versions (e.g. the 1.15 versions).
     */
    @ThreadSafe
    boolean isMajorRelease();

    /**
     * Returns the major release of this protocol. Either this protocol or its parent release.
     */
    @ThreadSafe
    IProtocol getMajorRelease();

    /**
     * Gets the name of the major release of this protocol. May not, although usually will, be the same as the name of
     * the {@code IProtocol} representing the major release. For example, the name of an {@code IProtocol} representing
     * a major release may be "1.7.2", but the name of the major release is "1.7".
     */
    @ThreadSafe
    String getMajorReleaseName();

    /**
     * If this protocol is a major release, returns a list of all minor releases (children) of this protocol,
     * including this protocol. Throws an exception if this protocol is not a major release.
     */
    @ThreadSafe
    List<IProtocol> getMinorReleases();

    /**
     * Returns whether this protocol is only in beta support by multiconnect, and may have stability issues when
     * connected.
     */
    @ThreadSafe
    boolean isMulticonnectBeta();

    boolean isMulticonnectExtension();

}

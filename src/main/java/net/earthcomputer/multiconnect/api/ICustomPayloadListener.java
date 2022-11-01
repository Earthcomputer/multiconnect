package net.earthcomputer.multiconnect.api;

/**
 * @deprecated See <a href="https://github.com/Earthcomputer/multiconnect/blob/master/docs/custom_payloads.md">the docs on custom payload handling</a>.
 */
@Deprecated
@FunctionalInterface
public interface ICustomPayloadListener<T> {
    /**
     * Called on a custom payload.
     */
    @ThreadSafe
    void onCustomPayload(ICustomPayloadEvent<T> event);
}

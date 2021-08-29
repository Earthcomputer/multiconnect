package net.earthcomputer.multiconnect.api;

/**
 * A listener for custom payloads.
 *
 * @param <T> The type of the channel, either {@linkplain net.minecraft.util.Identifier Identifier} or
 *              {@linkplain String}.
 *
 * @see MultiConnectAPI#addClientboundIdentifierCustomPayloadListener(ICustomPayloadListener)
 * @see MultiConnectAPI#addClientboundStringCustomPayloadListener(ICustomPayloadListener)
 * @see MultiConnectAPI#addServerboundIdentifierCustomPayloadListener(ICustomPayloadListener)
 * @see MultiConnectAPI#addServerboundStringCustomPayloadListener(ICustomPayloadListener)
 */
@FunctionalInterface
public interface ICustomPayloadListener<T> {
    /**
     * Called on a custom payload.
     */
    @ThreadSafe
    void onCustomPayload(ICustomPayloadEvent<T> event);
}

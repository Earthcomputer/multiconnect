# Custom Payload Handling

By default, multiconnect blocks non-vanilla custom payloads. This document walks modders through how to override this behavior.

## ⚠ Pitfalls ⚠

Multiconnect blocks custom payloads for a good reason. Data is not necessarily formatted and interpreted the same on older servers. This means that if you want to use a custom payload, you need to be careful to ensure that it is compatible with the server you are connecting to. If it is not, you will need to handle the necessary translation yourself. The multiconnect API provides a few methods that may help with this, but they are not exhaustive.

Some well-known types have changed over different Minecraft versions:
- `BlockPos` is packed into a `long` when sent over the network, but the order in which the bits are packed changed between Minecraft 1.13.2 and 1.14. This means that if you naively deserialize a `BlockPos` from 1.13.2 and below, you will get unexpected results.
- `ItemStack` has changed format a couple of times.
- Registry entries, such as blocks and items, are typically serialized by their integer IDs. These IDs change very frequently between Minecraft versions, and should not be relied upon. Resource locations (textual identifiers) are more stable, but even those have been known to change.

## Unblocking channels

You can tell multiconnect to unblock channels by adding to the `custom` data in your `fabric.mod.json`, like so:

```json
{
  "custom": {
    "multiconnect": {
      "custom_payloads": {
        "allowed_clientbound": "mymod:channel",
        "allowed_serverbound": "mymod:channel"
      }
    }
  }
}
```

You can see a real-world example in multiconnect's own `fabric.mod.json`, which unblocks the `minecraft:register` channel.

If you want to unblock multiple channels, you can use a list for `allowed_clientbound` and `allowed_serverbound`.

## 1.12 servers

In Minecraft 1.13, custom payload channels changed from being strings to resource locations. The old string channels commonly contained illegal characters for resource locations, so are not compatible with the new channel format. This means that you need to specify mappings between old channel names and new channel names. This is again done in your `fabric.mod.json`, like so:

```json
{
  "custom": {
    "multiconnect": {
      "custom_payloads": {
        "clientbound_112_names": {
          "mymod:channel": "MYMOD|CHANNEL"
        },
        "serverbound_112_names": {
          "mymod:channel": "MYMOD|CHANNEL"
        }
      }
    }
  }
}
```

With the above example, clientbound custom payloads with the channel name `MYMOD|CHANNEL` will be received as `mymod:channel`, and serverbound custom payloads with the channel `mymod:channel` will be sent as `MYMOD|CHANNEL`.

You can see a real-world example in multiconnect's own `fabric.mod.json`, which maps the `minecraft:register` channel to `REGISTER`.

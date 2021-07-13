from net.earthcomputer.multiconnect.api import MultiConnectAPI

supportedProtocols = MultiConnectAPI.instance().getSupportedProtocols()
for protocol in supportedProtocols:
    Chat.log(protocol.getName() + ': ' + str(protocol.getValue()))

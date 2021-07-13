from net.earthcomputer.multiconnect.api import Protocols
from net.earthcomputer.multiconnect.integrationtest import IntegrationTest

ip = IntegrationTest.setupServer(Protocols.V1_16_5)
Client.connect(ip)

Client.waitTick(100)

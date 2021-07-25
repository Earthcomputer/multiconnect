from java.lang import Integer
from net.earthcomputer.multiconnect.api import MultiConnectAPI
from net.earthcomputer.multiconnect.integrationtest import IntegrationTest
import importlib
import os
import sys
sys.path.insert(0, os.path.abspath('.'))
from testing import *

# Import tests
for dirname, dirs, files in os.walk('tests', followlinks=True):
    for file in files:
        if file != '__init__.py' and file.endswith('.py'):
            sys.path.insert(0, os.path.abspath(dirname))
            __import__(file[:-len('.py')], globals=globals())
            del sys.path[0]

context.releaseLock()


def on_join_server(event, ctx):
    global connected_to_server
    connected_to_server = True


connected_to_server = False
JsMacros.on('JoinServer', JavaWrapper.methodToJava(on_join_server))

min_protocol = Integer.getInteger('multiconnect.integrationTest.minProtocol', 0)
max_protocol = Integer.getInteger('multiconnect.integrationTest.maxProtocol', Integer.MAX_VALUE)

for protocol in MultiConnectAPI.instance().getSupportedProtocols():
    if protocol.getValue() < min_protocol or protocol.getValue() > max_protocol:
        continue

    tests = [test for test in get_all_tests() if test.min_protocol <= protocol.getValue() <= test.max_protocol]
    if len(tests) == 0:
        continue

    ip = IntegrationTest.setupServer(protocol.getValue())
    connected_to_server = False
    Client.connect(ip)
    timer = 0
    tries = 0
    while not connected_to_server:
        if timer > 100:
            if tries > 5:
                Client.shutdown()
            screen = Hud.getOpenScreen()
            if screen:
                buttons = screen.getButtonWidgets()
                if buttons.size() != 0:
                    buttons.get(0).click()
                    Client.waitTick()
                    Client.connect(ip)
            timer = 0
            tries += 1
        timer += 1
        Client.waitTick()

    for test in tests:
        test.func()
    IntegrationTest.stopServer()

Client.shutdown()

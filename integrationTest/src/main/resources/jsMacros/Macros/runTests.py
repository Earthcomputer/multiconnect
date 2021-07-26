old_globals = dir()
import sys
import os
from xyz.wagyourtail.jsmacros.core.library import BaseLibrary
jsmacros_libs = type(os)('jsmacros')
for attr in old_globals:
    val = globals()[attr]
    if isinstance(val, BaseLibrary):
        setattr(jsmacros_libs, attr, val)
sys.modules['jsmacros'] = jsmacros_libs
sys.path.insert(0, os.path.abspath('.'))

from java.lang import Integer
from net.earthcomputer.multiconnect.api import MultiConnectAPI
from net.earthcomputer.multiconnect.integrationtest import IntegrationTest
from testing import *

context.releaseLock()

# Import tests
for dirname, dirs, files in os.walk('tests', followlinks=True):
    for file in files:
        if file != '__init__.py' and file.endswith('.py'):
            sys.path.insert(0, os.path.abspath(dirname))
            __import__(file[:-len('.py')], globals=globals())
            del sys.path[0]

min_protocol = Integer.getInteger('multiconnect.integrationTest.minProtocol', 0)
max_protocol = Integer.getInteger('multiconnect.integrationTest.maxProtocol', Integer.MAX_VALUE)

for protocol in MultiConnectAPI.instance().getSupportedProtocols():
    if protocol.getValue() < min_protocol or protocol.getValue() > max_protocol:
        continue

    tests = [test for test in get_all_tests() if test.min_protocol <= protocol.getValue() <= test.max_protocol]
    if len(tests) == 0:
        continue

    server_ip = IntegrationTest.setupServer(protocol.getValue())
    set_server_ip(server_ip)
    connect_to_server()

    for test in tests:
        test.func()
        clean_up_test()
    IntegrationTest.stopServer()

Client.shutdown()

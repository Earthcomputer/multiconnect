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

import collections
import datetime
from java.lang import Integer, InterruptedException, System
from java.nio.file import Paths
from net.earthcomputer.multiconnect.api import MultiConnectAPI
from net.earthcomputer.multiconnect.integrationtest import IntegrationTest
from org.apache.commons.lang3 import StringEscapeUtils
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
    set_server(protocol.getValue(), server_ip)
    connect_to_server()

    for index, test in enumerate(tests):
        set_current_test(test)
        fail_count = len(get_test_failures())
        try:
            test.func()
        except InterruptedException:
            if not get_is_test_canceled():
                raise
            set_is_test_canceled(False)
        except:
            typ, val, tb = sys.exc_info()
            add_failure(str(val), tb=tb, cancel_test=False)
        clean_up_test(fail_count != len(get_test_failures()), index == len(tests) - 1)

    expect_disconnect()
    IntegrationTest.stopServer()

if len(get_test_failures()) > 0:
    time = datetime.datetime.now().strftime('%Y-%m-%d_%H-%M-%S')

    html = '''
    <!DOCTYPE html>
    <html>
    <head>
    <meta charset="UTF-8">
    <title>Multiconnect integration test failures</title>
    <link rel="icon" href="https://raw.githubusercontent.com/Earthcomputer/multiconnect/master/src/main/resources/assets/multiconnect/icon.png">
    <style>
    body { background-color: #2b2b2b; color: white; }
    .error-section { background-color: #5c1717; margin: 5px; }
    .error { border: 2px solid red; margin: 5px; }
    </style>
    </head>
    <body>
    <h1>Multiconnect integration test failures</h1>
    '''
    html += '<p>Time: ' + time + '</p>'
    html += '<p>Total failures: ' + str(len(get_test_failures())) + '</p>'

    failures_by_protocol = collections.OrderedDict()
    for failure in get_test_failures():
        if failure.protocol not in failures_by_protocol:
            failures_by_protocol[failure.protocol] = []
        failures_by_protocol[failure.protocol].append(failure)

    n = 0

    for protocol in failures_by_protocol:
        html += '<div class="error-section">'
        html += '<h2>Errors in protocol ' + str(protocol) + ' (' + MultiConnectAPI.instance().byProtocolVersion(protocol).getName() + ')</h2>'
        for failure in failures_by_protocol[protocol]:
            n += 1
            html += '<div class="error">'
            html += '<h3>#' + str(n) + ': ' + StringEscapeUtils.escapeHtml4(failure.test.name) + '</h3>'
            html += '<p>Description: ' + StringEscapeUtils.escapeHtml4(failure.description) + '</p>'
            html += '<details><summary>Traceback</summary>'
            html += '<p>' + '<br/>'.join((StringEscapeUtils.escapeHtml4(line) for line in failure.tb.split('\n'))) + '</p>'
            html += '</details>'
            html += '<details><summary>Stack Trace</summary>'
            html += '<p>' + '<br/>'.join((StringEscapeUtils.escapeHtml4(line) for line in failure.stack_trace.split('\n'))) + '</p>'
            html += '</details>'
            html += '</div>'
        html += '</div>'

    html += '</body></html>'

    failures_dir = os.path.join(System.getProperty('user.dir'), 'failures')
    if not os.path.exists(failures_dir):
        os.makedirs(failures_dir)
    html_file = os.path.join(failures_dir, time + '.html')
    with open(html_file, 'w') as f:
        f.write(html)
    html_path = Paths.get(html_file).toAbsolutePath()
    macro_path = JsMacros.getConfig().macroFolder.toPath().toAbsolutePath()
    JsMacros.open(macro_path.relativize(html_path).toString())
else:
    print('===========')
    print('All tests succeeded!')
    print('===========')

Client.shutdown()

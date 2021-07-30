import os
import re
import sys
from java.lang import Exception, Integer, InterruptedException, System, Thread
from java.io import PrintWriter, StringWriter
from jsmacros import *
from net.earthcomputer.multiconnect.api import MultiConnectAPI, Protocols
from net.earthcomputer.multiconnect.integrationtest import IntegrationTest
import traceback

current_protocol = 0
server_ip = ''
current_test = None
test_failures = []
is_test_canceled = False
testing_thread = Thread.currentThread()


def set_server(protocol, ip):
    global current_protocol
    global server_ip
    current_protocol = protocol
    server_ip = ip


def set_current_test(test):
    global current_test
    current_test = test


def get_is_test_canceled():
    return is_test_canceled


def set_is_test_canceled(canceled):
    global is_test_canceled
    is_test_canceled = canceled


def get_test_failures():
    return test_failures


all_tests = []

GROUND_LEVEL = 3
DIR_DOWN = 0
DIR_UP = 1
DIR_NORTH = 2
DIR_SOUTH = 3
DIR_WEST = 4
DIR_EAST = 5

inventoryChanged = JsMacros.createCustomEvent('multiconnect.InventoryChanged')
inventoryChanged.registerEvent()
currentInventory = []


def on_tick(event, context):
    global currentInventory
    if not World.isWorldLoaded():
        currentInventory = []
        return
    inv = Player.openInventory()
    new_inv = []
    for slot in range(inv.getTotalSlots()):
        new_inv.append(inv.getSlot(slot))
    if len(currentInventory) != len(new_inv) or not all((pair[0].isItemEqual(pair[1]) and pair[0].isNBTEqual(pair[1]) for pair in zip(currentInventory, new_inv))):
        inventoryChanged.trigger()


JsMacros.on('Tick', JavaWrapper.methodToJava(on_tick))


def on_join_server(event, ctx):
    global connected_to_server
    connected_to_server = True


connected_to_server = False
JsMacros.on('JoinServer', JavaWrapper.methodToJava(on_join_server))


def on_disconnected(event, ctx):
    if not expected_disconnect:
        Client.waitTick(100)
        add_failure('Unexpectedly disconnected from server')
    else:
        expect_disconnect(False)


def expect_disconnect(val=True):
    global expected_disconnect
    expected_disconnect = val


expected_disconnect = False
JsMacros.on('Disconnect', JavaWrapper.methodToJava(on_disconnected))


class Test:
    def __init__(self, func, min_protocol, max_protocol, name):
        self.func = func
        self.min_protocol = min_protocol
        self.max_protocol = max_protocol
        self.name = name


class TestFailure:
    def __init__(self, description):
        self.protocol = current_protocol
        self.test = current_test
        self.description = description
        sw = StringWriter()
        Exception('Stack trace').printStackTrace(PrintWriter(sw))
        self.stack_trace = sw.toString()
        self.tb = '\n'.join((line.strip() for line in traceback.format_stack()))

    def print_to_stderr(self):
        System.err.println('=====')
        System.err.println('Test "' + self.test.name + '" failed!')
        System.err.println('Protocol: ' + str(self.protocol))
        System.err.println(self.description)
        System.err.println(self.stack_trace)


def test(min_protocol=0, max_protocol=Integer.MAX_VALUE, name=None):
    def decorator(test_func):
        actual_name = name
        if actual_name is None:
            actual_name = test_func.__name__
        all_tests.append(Test(test_func, min_protocol, max_protocol, actual_name))
        return test_func
    return decorator


def get_all_tests():
    return all_tests


def clean_up_test(failed, is_last_test):
    if not failed:
        Chat.say('/clear')
        Client.waitTick()
    else:
        if not is_last_test:
            expect_disconnect()
            IntegrationTest.resetServer()
            connect_to_server()


def check(condition):
    def get_description(line):
        if not line:
            return 'Check failed'
        match = re.match(r'^\s*\w+\s*\((.*)\)\s*(?:#.*)?$', line)
        if not match:
            return 'Check failed'
        return 'Expected "' + match.group(1) + '" to be True'

    check_desc(condition, get_description)


def check_desc(condition, description):
    if not condition:
        line = None
        for _filename, _lineno, _name, _line in traceback.extract_stack():
            if _filename != 'testing.py' and not _filename.endswith(os.sep + 'testing.py'):
                line = _line
        if callable(description):
            description = description(line)
        add_failure(description)


def add_failure(description, tb=None, stack_trace=None, cancel_test=True):
    failure = TestFailure(description)
    if tb is not None:
        failure.tb = tb
    if stack_trace is not None:
        failure.stack_trace = stack_trace
    failure.print_to_stderr()
    test_failures.append(failure)
    if cancel_test:
        global is_test_canceled
        is_test_canceled = True
        expect_disconnect()
        if Thread.currentThread() == testing_thread:
            raise InterruptedException()
        else:
            testing_thread.interrupt()


IntegrationTest.setAddFailureFunc(JavaWrapper.methodToJava(lambda desc, stack_trace: add_failure(desc, stack_trace=stack_trace)))


def connect_to_server():
    expect_disconnect(False)
    global connected_to_server
    connected_to_server = False
    Client.connect(server_ip)
    timer = 0
    tries = 0
    while not connected_to_server:
        if timer > 20 * 10:  # 10 seconds
            if tries > 5:
                Client.shutdown()
            screen = Hud.getOpenScreen()
            if screen:
                buttons = screen.getButtonWidgets()
                if buttons.size() != 0:
                    buttons.get(0).click()
                    Client.waitTick()
                    Client.connect(server_ip)
            timer = 0
            tries += 1
        timer += 1
        Client.waitTick()

    Client.waitTick(20)


def relog():
    expect_disconnect()
    Client.disconnect()
    JsMacros.waitForEvent('OpenScreen').context.releaseLock()
    connect_to_server()


def give(name, name112=None, item_id=None):
    protocol = MultiConnectAPI.instance().getProtocolVersion()
    if protocol < Protocols.V1_8 and item_id:
        name = str(item_id)
    elif protocol <= Protocols.V1_12_2 and name112:
        name = name112
    Chat.say('/give @p ' + name)
    JsMacros.waitForEvent('multiconnect.InventoryChanged').context.releaseLock()
    Client.waitTick()

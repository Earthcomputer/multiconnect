import os
import re
import sys
from java.lang import Integer, System
from jsmacros import *
from net.earthcomputer.multiconnect.api import MultiConnectAPI, Protocols
import traceback

server_ip = ''


def set_server_ip(ip):
    global server_ip
    server_ip = ip


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


class Test:
    def __init__(self, func, min_protocol, max_protocol):
        self.func = func
        self.min_protocol = min_protocol
        self.max_protocol = max_protocol


def test(min_protocol=0, max_protocol=Integer.MAX_VALUE):
    def decorator(test_func):
        all_tests.append(Test(test_func, min_protocol, max_protocol))
        return test_func
    return decorator


def get_all_tests():
    return all_tests


def clean_up_test():
    Chat.say('/clear')
    Client.waitTick()


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
        filename = None
        lineno = None
        name = None
        line = None
        for _filename, _lineno, _name, _line in traceback.extract_stack():
            if _filename != 'testing.py' and not _filename.endswith(os.sep + 'testing.py'):
                filename = _filename
                lineno = _lineno
                name = _name
                line = _line
        if callable(description):
            description = description(line)
        System.err.println('Check failed in ' + filename + ' line ' + str(lineno) + ' in ' + name + ': ' + description)


def connect_to_server():
    global connected_to_server
    connected_to_server = False
    Client.connect(server_ip)
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
                    Client.connect(server_ip)
            timer = 0
            tries += 1
        timer += 1
        Client.waitTick()

    Client.waitTick(20)


def relog():
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

from jsmacros import *
from testing import *


@test()
def place_block():
    give('cobblestone')
    player = Player.getPlayer()
    player.interact(0, GROUND_LEVEL, 3, DIR_UP, False)
    Client.waitTick(5)
    relog()
    check(World.getBlock(0, GROUND_LEVEL + 1, 3).getId() == 'minecraft:cobblestone')


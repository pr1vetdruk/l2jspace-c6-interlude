/*
 * This file is part of the L2jSpace project.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package ru.privetdruk.l2jspace.gameserver.handler.itemhandlers;

import ru.privetdruk.l2jspace.commons.concurrent.ThreadPool;
import ru.privetdruk.l2jspace.gameserver.datatables.sql.NpcTable;
import ru.privetdruk.l2jspace.gameserver.handler.IItemHandler;
import ru.privetdruk.l2jspace.gameserver.instancemanager.IdManager;
import ru.privetdruk.l2jspace.gameserver.model.WorldObject;
import ru.privetdruk.l2jspace.gameserver.model.actor.Playable;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.NpcInstance;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.model.actor.templates.NpcTemplate;
import ru.privetdruk.l2jspace.gameserver.model.items.instance.ItemInstance;
import ru.privetdruk.l2jspace.gameserver.model.spawn.Spawn;

public class ChristmasTree implements IItemHandler {
    private static final int[] ITEM_IDS =
            {
                    5560, /* x-mas tree */
                    5561, /* Special x-mas tree */
            };

    private static final int[] NPC_IDS =
            {
                    13006, /* Christmas tree w. flashing lights and snow */
                    13007
            };

    @Override
    public void useItem(Playable playable, ItemInstance item) {
        final PlayerInstance player = (PlayerInstance) playable;
        NpcTemplate template1 = null;

        final int itemId = item.getItemId();
        for (int i = 0; i < ITEM_IDS.length; i++) {
            if (ITEM_IDS[i] == itemId) {
                template1 = NpcTable.getInstance().getTemplate(NPC_IDS[i]);
                break;
            }
        }

        if (template1 == null) {
            return;
        }

        WorldObject target = player.getTarget();
        if (target == null) {
            target = player;
        }

        try {
            final Spawn spawn = new Spawn(template1);
            spawn.setId(IdManager.getInstance().getNextId());
            spawn.setX(target.getX());
            spawn.setY(target.getY());
            spawn.setZ(target.getZ());
            final NpcInstance result = spawn.doSpawn();
            player.destroyItem("Consume", item.getObjectId(), 1, null, false);
            ThreadPool.schedule(new DeSpawn(result), 3600000);
        } catch (Exception e) {
            player.sendMessage("Target is not ingame.");
        }
    }

    public class DeSpawn implements Runnable {
        NpcInstance _npc = null;

        public DeSpawn(NpcInstance npc) {
            _npc = npc;
        }

        @Override
        public void run() {
            _npc.onDecay();
        }
    }

    @Override
    public int[] getItemIds() {
        return ITEM_IDS;
    }
}

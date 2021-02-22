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

import ru.privetdruk.l2jspace.commons.util.Chronos;
import ru.privetdruk.l2jspace.gameserver.datatables.xml.DoorData;
import ru.privetdruk.l2jspace.gameserver.handler.IItemHandler;
import ru.privetdruk.l2jspace.gameserver.model.WorldObject;
import ru.privetdruk.l2jspace.gameserver.model.actor.Playable;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.DoorInstance;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.model.items.instance.ItemInstance;
import ru.privetdruk.l2jspace.gameserver.network.SystemMessageId;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.ActionFailed;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.SocialAction;

/**
 * @author chris
 */
public class MOSKey implements IItemHandler {
    private static final int[] ITEM_IDS =
            {
                    8056
            };
    public static final int INTERACTION_DISTANCE = 150;
    public static long _lastOpen = 0;

    @Override
    public void useItem(Playable playable, ItemInstance item) {
        final int itemId = item.getItemId();
        if (!(playable instanceof PlayerInstance)) {
            return;
        }

        final PlayerInstance player = (PlayerInstance) playable;
        final WorldObject target = player.getTarget();
        if (!(target instanceof DoorInstance)) {
            player.sendPacket(SystemMessageId.INVALID_TARGET);
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        final DoorInstance door = (DoorInstance) target;
        if (!player.isInsideRadius(door, INTERACTION_DISTANCE, false, false)) {
            player.sendMessage("Door is to far.");
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }

        if ((player.getAbnormalEffect() > 0) || player.isInCombat()) {
            player.sendMessage("You can`t use the key right now.");
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }

        if ((_lastOpen + 1800000) > Chronos.currentTimeMillis()) // 30 * 60 * 1000 = 1800000
        {
            player.sendMessage("You can`t use the key right now.");
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }

        if (!playable.destroyItem("Consume", item.getObjectId(), 1, null, false)) {
            return;
        }

        if ((itemId == 8056) && ((door.getDoorId() == 23150003) || (door.getDoorId() == 23150004))) {
            DoorData.getInstance().getDoor(23150003).openMe();
            DoorData.getInstance().getDoor(23150004).openMe();
            DoorData.getInstance().getDoor(23150003).onOpen();
            DoorData.getInstance().getDoor(23150004).onOpen();
            player.broadcastPacket(new SocialAction(player.getObjectId(), 3));
            _lastOpen = Chronos.currentTimeMillis();
        }
    }

    @Override
    public int[] getItemIds() {
        return ITEM_IDS;
    }
}

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

import ru.privetdruk.l2jspace.gameserver.handler.IItemHandler;
import ru.privetdruk.l2jspace.gameserver.model.WorldObject;
import ru.privetdruk.l2jspace.gameserver.model.actor.Playable;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.GrandBossInstance;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.model.items.instance.ItemInstance;
import ru.privetdruk.l2jspace.gameserver.network.SystemMessageId;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.ActionFailed;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.SocialAction;

public class BreakingArrow implements IItemHandler {
    private static final int[] ITEM_IDS =
            {
                    8192
            };

    @Override
    public void useItem(Playable playable, ItemInstance item) {
        final int itemId = item.getItemId();
        if (!(playable instanceof PlayerInstance)) {
            return;
        }
        final PlayerInstance player = (PlayerInstance) playable;
        final WorldObject target = player.getTarget();
        if (!(target instanceof GrandBossInstance)) {
            player.sendPacket(SystemMessageId.INVALID_TARGET);
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        final GrandBossInstance frintezza = (GrandBossInstance) target;
        if (!player.isInsideRadius(frintezza, 500, false, false)) {
            player.sendMessage("The purpose is inaccessible");
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        if ((itemId == 8192) && (frintezza.getObjectId() == 29045)) {
            frintezza.broadcastPacket(new SocialAction(frintezza.getObjectId(), 2));
            playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
        }
    }

    @Override
    public int[] getItemIds() {
        return ITEM_IDS;
    }
}
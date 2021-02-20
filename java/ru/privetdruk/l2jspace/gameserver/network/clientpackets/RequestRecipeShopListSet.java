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
package ru.privetdruk.l2jspace.gameserver.network.clientpackets;

import ru.privetdruk.l2jspace.Config;
import ru.privetdruk.l2jspace.gameserver.model.ManufactureItem;
import ru.privetdruk.l2jspace.gameserver.model.ManufactureList;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.model.zone.ZoneId;
import ru.privetdruk.l2jspace.gameserver.network.SystemMessageId;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.ActionFailed;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.RecipeShopMsg;

public class RequestRecipeShopListSet extends GameClientPacket {
    private int _count;
    private int[] _items; // count*2

    @Override
    protected void readImpl() {
        _count = readD();
        if ((_count < 0) || ((_count * 8) > _buf.remaining()) || (_count > Config.MAX_ITEM_IN_PACKET)) {
            _count = 0;
        }

        _items = new int[_count * 2];
        for (int x = 0; x < _count; x++) {
            final int recipeID = readD();
            _items[(x * 2) + 0] = recipeID;
            final int cost = readD();
            _items[(x * 2) + 1] = cost;
        }
    }

    @Override
    protected void runImpl() {
        final PlayerInstance player = getClient().getPlayer();
        if (player == null) {
            return;
        }

        if (player.isInDuel()) {
            player.sendPacket(SystemMessageId.WHILE_YOU_ARE_ENGAGED_IN_COMBAT_YOU_CANNOT_OPERATE_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP);
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }

        if (player.isTradeDisabled()) {
            player.sendMessage("Private manufacture is disabled here. Try another place.");
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }

        if (player.isInsideZone(ZoneId.NO_STORE)) {
            player.sendMessage("Private manufacture is disabled here. Try another place.");
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }

        if (_count == 0) {
            player.setPrivateStoreType(PlayerInstance.STORE_PRIVATE_NONE);
            player.broadcastUserInfo();
            player.standUp();
        } else {
            final ManufactureList createList = new ManufactureList();
            for (int x = 0; x < _count; x++) {
                final int recipeID = _items[(x * 2) + 0];
                final int cost = _items[(x * 2) + 1];
                createList.add(new ManufactureItem(recipeID, cost));
            }
            createList.setStoreName(player.getCreateList() != null ? player.getCreateList().getStoreName() : "");
            player.setCreateList(createList);

            player.setPrivateStoreType(PlayerInstance.STORE_PRIVATE_MANUFACTURE);
            player.sitDown();
            player.broadcastUserInfo();
            player.sendPacket(new RecipeShopMsg(player));
            player.broadcastPacket(new RecipeShopMsg(player));
        }
    }
}

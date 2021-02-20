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

import ru.privetdruk.l2jspace.gameserver.model.ManufactureList;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.ActionFailed;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.RecipeShopManageList;

public class RequestRecipeShopManageList extends GameClientPacket {
    @Override
    protected void readImpl() {
        // trigger
    }

    @Override
    protected void runImpl() {
        final PlayerInstance player = getClient().getPlayer();
        if (player == null) {
            return;
        }

        // Player shouldn't be able to set stores if he/she is alike dead (dead or fake death)
        if (player.isAlikeDead()) {
            sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }

        if (player.getPrivateStoreType() != 0) {
            player.setPrivateStoreType(PlayerInstance.STORE_PRIVATE_NONE);
            player.broadcastUserInfo();
            if (player.isSitting()) {
                player.standUp();
            }
        }

        if (player.getCreateList() == null) {
            player.setCreateList(new ManufactureList());
        }

        player.sendPacket(new RecipeShopManageList(player, true));
    }
}

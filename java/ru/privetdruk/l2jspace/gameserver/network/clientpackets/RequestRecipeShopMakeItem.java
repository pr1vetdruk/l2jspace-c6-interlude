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

import ru.privetdruk.l2jspace.gameserver.RecipeController;
import ru.privetdruk.l2jspace.gameserver.model.World;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.network.SystemMessageId;
import ru.privetdruk.l2jspace.gameserver.util.Util;

public class RequestRecipeShopMakeItem extends GameClientPacket {
    private int _id;
    private int _recipeId;
    @SuppressWarnings("unused")
    private int _unknow;

    @Override
    protected void readImpl() {
        _id = readD();
        _recipeId = readD();
        _unknow = readD();
    }

    @Override
    protected void runImpl() {
        final PlayerInstance player = getClient().getPlayer();
        if (player == null) {
            return;
        }

        if (!getClient().getFloodProtectors().getManufacture().tryPerformAction("RecipeShopMake")) {
            return;
        }

        final PlayerInstance manufacturer = World.getInstance().getPlayer(_id);
        if (manufacturer == null) {
            return;
        }

        if (player.getPrivateStoreType() != 0) {
            // player.sendMessage("Cannot create items while trading.");
            return;
        }

        if (manufacturer.getPrivateStoreType() != 5) {
            // player.sendMessage("Cannot make items while trading");
            return;
        }

        if (player.isCrafting() || manufacturer.isCrafting()) {
            // player.sendMessage("Currently in Craft Mode.");
            return;
        }

        if (manufacturer.isInDuel() || player.isInDuel()) {
            player.sendPacket(SystemMessageId.WHILE_YOU_ARE_ENGAGED_IN_COMBAT_YOU_CANNOT_OPERATE_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP);
            return;
        }

        if (Util.checkIfInRange(150, player, manufacturer, true)) {
            RecipeController.getInstance().requestManufactureItem(manufacturer, _recipeId, player);
        }
    }
}

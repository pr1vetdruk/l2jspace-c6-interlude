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

public class RequestRecipeBookOpen extends GameClientPacket {
    private boolean _isDwarvenCraft;

    @Override
    protected void readImpl() {
        _isDwarvenCraft = readD() == 0;
    }

    @Override
    protected void runImpl() {
        if (getClient().getPlayer() == null) {
            return;
        }

        if (getClient().getPlayer().getPrivateStoreType() != 0) {
            getClient().getPlayer().sendMessage("Cannot use recipe book while trading.");
            return;
        }

        RecipeController.getInstance().requestBookOpen(getClient().getPlayer(), _isDwarvenCraft);
    }
}

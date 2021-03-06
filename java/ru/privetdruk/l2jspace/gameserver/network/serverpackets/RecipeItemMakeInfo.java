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
package ru.privetdruk.l2jspace.gameserver.network.serverpackets;

import ru.privetdruk.l2jspace.gameserver.datatables.xml.RecipeData;
import ru.privetdruk.l2jspace.gameserver.model.RecipeList;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;

/**
 * format dddd
 */
public class RecipeItemMakeInfo extends GameServerPacket {
    private final int _id;
    private final PlayerInstance _player;
    private final boolean _success;

    public RecipeItemMakeInfo(int id, PlayerInstance player, boolean success) {
        _id = id;
        _player = player;
        _success = success;
    }

    public RecipeItemMakeInfo(int id, PlayerInstance player) {
        _id = id;
        _player = player;
        _success = true;
    }

    @Override
    protected final void writeImpl() {
        final RecipeList recipe = RecipeData.getInstance().getRecipe(_id);
        if (recipe != null) {
            writeC(0xD7);

            writeD(_id);
            writeD(recipe.isDwarvenRecipe() ? 0 : 1); // 0 = Dwarven - 1 = Common
            writeD((int) _player.getCurrentMp());
            writeD(_player.getMaxMp());
            writeD(_success ? 1 : 0); // item creation success/failed
        }
    }
}

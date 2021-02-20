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
import ru.privetdruk.l2jspace.gameserver.model.actor.Playable;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.model.items.instance.ItemInstance;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.RadarControl;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.ShowMiniMap;

public class Maps implements IItemHandler {
    // All the items ids that this handler knows
    private static final int[] ITEM_IDS =
            {
                    1665,
                    1863,
                    7063
            };

    @Override
    public void useItem(Playable playable, ItemInstance item) {
        if (!(playable instanceof PlayerInstance)) {
            return;
        }

        final PlayerInstance player = (PlayerInstance) playable;
        final int itemId = item.getItemId();
        if (itemId == 7063) {
            player.sendPacket(new ShowMiniMap(1665));
            player.sendPacket(new RadarControl(0, 1, 51995, -51265, -3104));
        } else {
            player.sendPacket(new ShowMiniMap(itemId));
        }
    }

    @Override
    public int[] getItemIds() {
        return ITEM_IDS;
    }
}

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
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.NpcHtmlMessage;

public class MapForestOfTheDead implements IItemHandler {
    private static final int[] ITEM_IDS =
            {
                    7063
            };

    @Override
    public void useItem(Playable playable, ItemInstance item) {
        if (!(playable instanceof PlayerInstance)) {
            return;
        }
        final int itemId = item.getItemId();
        if (itemId == 7063) {
            final NpcHtmlMessage html = new NpcHtmlMessage(5);
            final StringBuilder map = new StringBuilder("<html><title>Map - Forest of the Dead</title>");
            map.append("<body>");
            map.append("<br>");
            map.append("Map :");
            map.append("<br>");
            map.append("<table>");
            map.append("<tr><td>");
            map.append("<img src=\"icon.Quest_deadperson_forest_t00\" width=255 height=255>");
            map.append("</td></tr>");
            map.append("</table>");
            map.append("</body></html>");
            html.setHtml(map.toString());
            playable.sendPacket(html);
        }
    }

    @Override
    public int[] getItemIds() {
        return ITEM_IDS;
    }
}

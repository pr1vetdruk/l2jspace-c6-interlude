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
package ru.privetdruk.l2jspace.gameserver.model.actor.instance;

import ru.privetdruk.l2jspace.gameserver.datatables.xml.HennaData;
import ru.privetdruk.l2jspace.gameserver.model.actor.Creature;
import ru.privetdruk.l2jspace.gameserver.model.actor.templates.NpcTemplate;
import ru.privetdruk.l2jspace.gameserver.network.SystemMessageId;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.HennaEquipList;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.HennaRemoveList;

public class SymbolMakerInstance extends FolkInstance {
    public SymbolMakerInstance(int objectID, NpcTemplate template) {
        super(objectID, template);
    }

    @Override
    public void onBypassFeedback(PlayerInstance player, String command) {
        if (command.equals("Draw")) {
            player.sendPacket(new HennaEquipList(player, HennaData.getInstance().getAvailableHennasFor(player)));
        } else if (command.equals("RemoveList")) {
            boolean hasHennas = false;
            for (int i = 1; i <= 3; i++) {
                if (player.getHenna(i) != null) {
                    hasHennas = true;
                }
            }

            if (hasHennas) {
                player.sendPacket(new HennaRemoveList(player));
            } else {
                player.sendPacket(SystemMessageId.THE_SYMBOL_INFORMATION_CANNOT_BE_FOUND);
            }
        } else {
            super.onBypassFeedback(player, command);
        }
    }

    @Override
    public String getHtmlPath(int npcId, int value) {
        return "data/html/symbolmaker/SymbolMaker.htm";
    }

    @Override
    public boolean isAutoAttackable(Creature attacker) {
        return false;
    }
}

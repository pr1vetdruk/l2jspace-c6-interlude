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

import ru.privetdruk.l2jspace.gameserver.datatables.xml.HennaData;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.model.items.Henna;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.HennaItemRemoveInfo;

public final class RequestHennaItemRemoveInfo extends GameClientPacket {
    private int _symbolId;

    @Override
    protected void readImpl() {
        _symbolId = readD();
    }

    @Override
    protected void runImpl() {
        final PlayerInstance player = getClient().getPlayer();
        if (player == null) {
            return;
        }

        final Henna template = HennaData.getInstance().getHenna(_symbolId);
        if (template == null) {
            return;
        }

        player.sendPacket(new HennaItemRemoveInfo(template, player));
    }
}
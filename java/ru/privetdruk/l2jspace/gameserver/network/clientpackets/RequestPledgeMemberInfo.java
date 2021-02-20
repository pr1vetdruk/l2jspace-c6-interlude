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

import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.model.clan.Clan;
import ru.privetdruk.l2jspace.gameserver.model.clan.ClanMember;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.PledgeReceiveMemberInfo;

/**
 * Format: (ch) dS
 *
 * @author -Wooden-
 */
public class RequestPledgeMemberInfo extends GameClientPacket {
    @SuppressWarnings("unused")
    private int _unk1;
    private String _player;

    @Override
    protected void readImpl() {
        _unk1 = readD();
        _player = readS();
    }

    @Override
    protected void runImpl() {
        // LOGGER.info("C5: RequestPledgeMemberInfo d:"+_unk1);
        // LOGGER.info("C5: RequestPledgeMemberInfo S:"+_player);
        final PlayerInstance player = getClient().getPlayer();
        if (player == null) {
            return;
        }
        // do we need powers to do that??
        final Clan clan = player.getClan();
        if (clan == null) {
            return;
        }
        final ClanMember member = clan.getClanMember(_player);
        if (member == null) {
            return;
        }
        player.sendPacket(new PledgeReceiveMemberInfo(member));
    }
}

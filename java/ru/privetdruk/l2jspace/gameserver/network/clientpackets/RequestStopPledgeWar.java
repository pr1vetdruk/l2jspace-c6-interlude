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

import ru.privetdruk.l2jspace.gameserver.datatables.sql.ClanTable;
import ru.privetdruk.l2jspace.gameserver.model.World;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.model.clan.Clan;
import ru.privetdruk.l2jspace.gameserver.network.SystemMessageId;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.ActionFailed;

public class RequestStopPledgeWar extends GameClientPacket {
    private String _pledgeName;

    @Override
    protected void readImpl() {
        _pledgeName = readS();
    }

    @Override
    protected void runImpl() {
        final PlayerInstance player = getClient().getPlayer();
        if (player == null) {
            return;
        }

        final Clan playerClan = player.getClan();
        if (playerClan == null) {
            return;
        }

        final Clan clan = ClanTable.getInstance().getClanByName(_pledgeName);
        if (clan == null) {
            player.sendMessage("No such clan.");
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }

        if (!playerClan.isAtWarWith(clan.getClanId())) {
            player.sendMessage("You aren't at war with this clan.");
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }

        // Check if player who does the request has the correct rights to do it
        if ((player.getClanPrivileges() & Clan.CP_CL_PLEDGE_WAR) != Clan.CP_CL_PLEDGE_WAR) {
            player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
            return;
        }

        // LOGGER.info("RequestStopPledgeWar: By leader or authorized player: " + playerClan.getLeaderName() + " of clan: "
        // + playerClan.getName() + " to clan: " + _pledgeName);

        // PlayerInstance leader = World.getInstance().getPlayer(clan.getLeaderName());
        // if(leader != null && leader.isOnline() == 0)
        // {
        // player.sendMessage("Clan leader isn't online.");
        // player.sendPacket(ActionFailed.STATIC_PACKET);
        // return;
        // }

        // if (leader.isProcessingRequest())
        // {
        // SystemMessage sm = new SystemMessage(SystemMessage.S1_IS_BUSY_TRY_LATER);
        // sm.addString(leader.getName());
        // player.sendPacket(sm);
        // return;
        // }
        ClanTable.getInstance().deleteClanWars(playerClan.getClanId(), clan.getClanId());
        for (PlayerInstance cha : World.getInstance().getAllPlayers()) {
            if ((cha.getClan() == player.getClan()) || (cha.getClan() == clan)) {
                cha.broadcastUserInfo();
            }
        }
    }
}

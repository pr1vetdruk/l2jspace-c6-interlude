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

import ru.privetdruk.l2jspace.gameserver.model.Party;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.model.partymatching.PartyMatchRoom;
import ru.privetdruk.l2jspace.gameserver.model.partymatching.PartyMatchRoomList;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.ExClosePartyRoom;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.ExPartyRoomMember;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.PartyMatchDetail;

public class RequestWithDrawalParty extends GameClientPacket {
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

        final Party party = player.getParty();
        if (party != null) {
            if (party.isInDimensionalRift() && !party.getDimensionalRift().getRevivedAtWaitingRoom().contains(player)) {
                player.sendMessage("You can't exit party when you are in Dimensional Rift.");
            } else {
                party.removePartyMember(player);

                if (player.isInPartyMatchRoom()) {
                    final PartyMatchRoom room = PartyMatchRoomList.getInstance().getPlayerRoom(player);
                    if (room != null) {
                        player.sendPacket(new PartyMatchDetail(room));
                        player.sendPacket(new ExPartyRoomMember(room, 0));
                        player.sendPacket(new ExClosePartyRoom());
                        room.deleteMember(player);
                    }
                    player.setPartyRoom(0);
                    player.broadcastUserInfo();
                }
            }
        }
    }
}
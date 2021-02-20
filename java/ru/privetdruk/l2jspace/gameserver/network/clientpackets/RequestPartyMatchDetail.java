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
import ru.privetdruk.l2jspace.gameserver.model.partymatching.PartyMatchRoom;
import ru.privetdruk.l2jspace.gameserver.model.partymatching.PartyMatchRoomList;
import ru.privetdruk.l2jspace.gameserver.model.partymatching.PartyMatchWaitingList;
import ru.privetdruk.l2jspace.gameserver.network.SystemMessageId;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.ExManagePartyRoomMember;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.ExPartyRoomMember;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.PartyMatchDetail;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.SystemMessage;

/**
 * @author Gnacik
 */

public class RequestPartyMatchDetail extends GameClientPacket {
    private int _roomid;

    @SuppressWarnings("unused")
    private int _unk1;
    @SuppressWarnings("unused")
    private int _unk2;
    @SuppressWarnings("unused")
    private int _unk3;

    @Override
    protected void readImpl() {
        _roomid = readD();
        /*
         * IF player click on Room all unk are 0 IF player click AutoJoin values are -1 1 1
         */
        _unk1 = readD();
        _unk2 = readD();
        _unk3 = readD();
    }

    @Override
    protected void runImpl() {
        final PlayerInstance player = getClient().getPlayer();
        if (player == null) {
            return;
        }

        final PartyMatchRoom room = PartyMatchRoomList.getInstance().getRoom(_roomid);
        if (room == null) {
            return;
        }

        if ((player.getLevel() >= room.getMinLevel()) && (player.getLevel() <= room.getMaxLevel())) {
            // Remove from waiting list
            PartyMatchWaitingList.getInstance().removePlayer(player);

            player.setPartyRoom(_roomid);

            player.sendPacket(new PartyMatchDetail(room));
            player.sendPacket(new ExPartyRoomMember(room, 0));
            for (PlayerInstance _member : room.getPartyMembers()) {
                if (_member == null) {
                    continue;
                }

                _member.sendPacket(new ExManagePartyRoomMember(player, room, 0));
                _member.sendPacket(new SystemMessage(SystemMessageId.S1_HAS_ENTERED_THE_PARTY_ROOM).addString(player.getName()));
            }
            room.addMember(player);

            // Info Broadcast
            player.broadcastUserInfo();
        } else {
            player.sendPacket(SystemMessageId.YOU_DO_NOT_MEET_THE_REQUIREMENTS_TO_ENTER_THAT_PARTY_ROOM);
        }
    }
}
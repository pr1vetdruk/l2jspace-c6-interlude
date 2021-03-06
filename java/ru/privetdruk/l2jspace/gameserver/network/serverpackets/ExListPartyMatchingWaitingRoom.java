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

import java.util.ArrayList;
import java.util.List;

import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.model.partymatching.PartyMatchRoom;
import ru.privetdruk.l2jspace.gameserver.model.partymatching.PartyMatchRoomList;
import ru.privetdruk.l2jspace.gameserver.model.partymatching.PartyMatchWaitingList;

/**
 * @author Gnacik
 */
public class ExListPartyMatchingWaitingRoom extends GameServerPacket {
    private final PlayerInstance _player;
    @SuppressWarnings("unused")
    private final int _page;
    private final int _minLevel;
    private final int _maxLevel;
    private final int _mode;
    private final List<PlayerInstance> _members;

    public ExListPartyMatchingWaitingRoom(PlayerInstance player, int page, int minLevel, int maxLevel, int mode) {
        _player = player;
        _page = page;
        _minLevel = minLevel;
        _maxLevel = maxLevel;
        _mode = mode;
        _members = new ArrayList<>();
    }

    @Override
    protected void writeImpl() {
        writeC(0xfe);
        writeH(0x35);

        // If the mode is 0 and the activeChar isn't the PartyRoom leader, return an empty list.
        if (_mode == 0) {
            // Retrieve the activeChar PartyMatchRoom
            final PartyMatchRoom room = PartyMatchRoomList.getInstance().getRoom(_player.getPartyRoom());
            if ((room != null) && (room.getOwner() != null) && !room.getOwner().equals(_player)) {
                writeD(0);
                writeD(0);
                return;
            }
        }

        for (PlayerInstance cha : PartyMatchWaitingList.getInstance().getPlayers()) {
            // Don't add yourself in the list
            if ((cha == null) || (cha == _player)) {
                continue;
            }

            if (!cha.isPartyWaiting()) {
                PartyMatchWaitingList.getInstance().removePlayer(cha);
                continue;
            }

            if ((cha.getLevel() < _minLevel) || (cha.getLevel() > _maxLevel)) {
                continue;
            }

            _members.add(cha);
        }

        int count = 0;
        final int size = _members.size();
        writeD(1);
        writeD(size);
        while (size > count) {
            writeS(_members.get(count).getName());
            writeD(_members.get(count).getActiveClass());
            writeD(_members.get(count).getLevel());
            count++;
        }
    }
}
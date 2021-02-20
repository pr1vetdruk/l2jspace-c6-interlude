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

import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;

/**
 * @version $Revision: 1.4.2.1.2.5 $ $Date: 2005/03/27 15:29:39 $
 */
public class PartySmallWindowUpdate extends GameServerPacket {
    private final PlayerInstance _member;

    public PartySmallWindowUpdate(PlayerInstance member) {
        _member = member;
    }

    @Override
    protected final void writeImpl() {
        writeC(0x52);
        writeD(_member.getObjectId());
        writeS(_member.getName());

        writeD((int) _member.getCurrentCp()); // c4
        writeD(_member.getMaxCp()); // c4

        writeD((int) _member.getCurrentHp());
        writeD(_member.getMaxHp());
        writeD((int) _member.getCurrentMp());
        writeD(_member.getMaxMp());
        writeD(_member.getLevel());
        writeD(_member.getClassId().getId());
    }
}

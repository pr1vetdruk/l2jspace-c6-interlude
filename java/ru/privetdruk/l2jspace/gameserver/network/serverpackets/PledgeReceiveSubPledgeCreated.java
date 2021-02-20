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

import ru.privetdruk.l2jspace.gameserver.model.clan.Clan;

/**
 * @author -Wooden-
 */
public class PledgeReceiveSubPledgeCreated extends GameServerPacket {
    private final Clan.SubPledge _subPledge;

    /**
     * @param subPledge
     */
    public PledgeReceiveSubPledgeCreated(Clan.SubPledge subPledge) {
        _subPledge = subPledge;
    }

    @Override
    protected void writeImpl() {
        writeC(0xfe);
        writeH(0x3f);

        writeD(0x01);
        writeD(_subPledge.getId());
        writeS(_subPledge.getName());
        writeS(_subPledge.getLeaderName());
    }
}

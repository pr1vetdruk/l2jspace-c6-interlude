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

/**
 * sample
 * <p>
 * 7d c1 b2 e0 4a 00 00 00 00
 * <p>
 * format cdd
 *
 * @version $Revision: 1.1.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class AskJoinAlly extends GameServerPacket {
    private final String _requestorName;
    private final int _requestorObjId;

    /**
     * @param requestorObjId
     * @param requestorName
     */
    public AskJoinAlly(int requestorObjId, String requestorName) {
        _requestorName = requestorName;
        _requestorObjId = requestorObjId;
    }

    @Override
    protected final void writeImpl() {
        writeC(0xa8);
        writeD(_requestorObjId);
        writeS(_requestorName);
    }
}

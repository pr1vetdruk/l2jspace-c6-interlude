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

import ru.privetdruk.l2jspace.gameserver.model.Location;
import ru.privetdruk.l2jspace.gameserver.model.actor.Creature;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;

/**
 * @author Maktakien
 */
public class MoveToLocationInVehicle extends GameServerPacket {
    private int _objectId;
    private int _boatId;
    private Location _destination;
    private Location _origin;

    public MoveToLocationInVehicle(Creature actor, Location destination, Location origin) {
        if (!(actor instanceof PlayerInstance)) {
            return;
        }

        final PlayerInstance player = (PlayerInstance) actor;
        if (player.getBoat() == null) {
            return;
        }

        _objectId = player.getObjectId();
        _boatId = player.getBoat().getObjectId();
        _destination = destination;
        _origin = origin;
    }

    @Override
    protected void writeImpl() {
        writeC(0x71);
        writeD(_objectId);
        writeD(_boatId);
        writeD(_destination.getX());
        writeD(_destination.getY());
        writeD(_destination.getZ());
        writeD(_origin.getX());
        writeD(_origin.getY());
        writeD(_origin.getZ());
    }
}

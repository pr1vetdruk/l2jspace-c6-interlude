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
package ru.privetdruk.l2jspace.gameserver.model.actor.knownlist;

import ru.privetdruk.l2jspace.gameserver.model.WorldObject;
import ru.privetdruk.l2jspace.gameserver.model.actor.Creature;
import ru.privetdruk.l2jspace.gameserver.model.actor.Playable;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.CabaleBufferInstance;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.FestivalGuideInstance;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.FolkInstance;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.NpcInstance;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.CharMoveToLocation;

public class NpcKnownList extends CreatureKnownList {
    public NpcKnownList(NpcInstance activeChar) {
        super(activeChar);
    }

    // Mobius: Fix for not broadcasting correct position.
    @Override
    public boolean addKnownObject(WorldObject object) {
        if (!super.addKnownObject(object)) {
            return false;
        }

        // Broadcast correct walking NPC position.
        if (getActiveObject().isNpc() && (object instanceof Creature) && object.isPlayer() && getActiveChar().isMoving() && !getActiveChar().isInCombat()) {
            ((Creature) object).broadcastPacket(new CharMoveToLocation(getActiveChar()));
        }
        return true;
    }

    @Override
    public NpcInstance getActiveChar() {
        return (NpcInstance) super.getActiveChar();
    }

    @Override
    public int getDistanceToForgetObject(WorldObject object) {
        return 2 * getDistanceToWatchObject(object);
    }

    @Override
    public int getDistanceToWatchObject(WorldObject object) {
        if (object instanceof FestivalGuideInstance) {
            return 10000;
        }

        if ((object instanceof FolkInstance) || !(object instanceof Creature)) {
            return 0;
        }

        if (object instanceof CabaleBufferInstance) {
            return 900;
        }

        if (object instanceof Playable) {
            return 1500;
        }

        return 500;
    }
}

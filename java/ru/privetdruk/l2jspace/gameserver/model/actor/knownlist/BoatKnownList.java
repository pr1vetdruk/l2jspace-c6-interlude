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
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;

/**
 * @author Maktakien
 */
public class BoatKnownList extends CreatureKnownList {
    public BoatKnownList(Creature creature) {
        super(creature);
    }

    @Override
    public int getDistanceToForgetObject(WorldObject object) {
        if (!(object instanceof PlayerInstance)) {
            return 0;
        }
        return 8000;
    }

    @Override
    public int getDistanceToWatchObject(WorldObject object) {
        if (!(object instanceof PlayerInstance)) {
            return 0;
        }
        return 4000;
    }
}

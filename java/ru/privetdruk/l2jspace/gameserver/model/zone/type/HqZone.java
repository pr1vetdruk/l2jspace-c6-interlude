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
package ru.privetdruk.l2jspace.gameserver.model.zone.type;

import ru.privetdruk.l2jspace.gameserver.model.actor.Creature;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.model.zone.ZoneId;
import ru.privetdruk.l2jspace.gameserver.model.zone.ZoneType;

/**
 * The only zone where 'Build Headquarters' is allowed.
 *
 * @author Tryskell, reverted version of Gnat's NoHqZone
 */
public class HqZone extends ZoneType {
    public HqZone(int id) {
        super(id);
    }

    @Override
    protected void onEnter(Creature character) {
        if (character instanceof PlayerInstance) {
            character.setInsideZone(ZoneId.HQ, true);
        }
    }

    @Override
    protected void onExit(Creature character) {
        if (character instanceof PlayerInstance) {
            character.setInsideZone(ZoneId.HQ, false);
        }
    }

    @Override
    public void onDieInside(Creature character) {
    }

    @Override
    public void onReviveInside(Creature character) {
    }
}
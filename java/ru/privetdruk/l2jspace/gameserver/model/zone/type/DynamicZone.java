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

import java.util.concurrent.Future;

import ru.privetdruk.l2jspace.commons.concurrent.ThreadPool;
import ru.privetdruk.l2jspace.gameserver.model.Skill;
import ru.privetdruk.l2jspace.gameserver.model.WorldRegion;
import ru.privetdruk.l2jspace.gameserver.model.actor.Creature;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.model.zone.ZoneType;

/**
 * A dynamic zone? Maybe use this for interlude skills like protection field :>
 *
 * @author durgus
 */
public class DynamicZone extends ZoneType {
    private final WorldRegion _region;
    private final Creature _owner;
    private Future<?> _task;
    private final Skill _skill;

    protected void setTask(Future<?> task) {
        _task = task;
    }

    public DynamicZone(WorldRegion region, Creature owner, Skill skill) {
        super(-1);
        _region = region;
        _owner = owner;
        _skill = skill;

        final Runnable r = this::remove;
        setTask(ThreadPool.schedule(r, skill.getBuffDuration()));
    }

    @Override
    protected void onEnter(Creature creature) {
        try {
            if (creature instanceof PlayerInstance) {
                ((PlayerInstance) creature).sendMessage("You have entered a temporary zone!");
            }

            _skill.getEffects(_owner, creature, false, false, false);
        } catch (NullPointerException e) {
        }
    }

    @Override
    protected void onExit(Creature creature) {
        if (creature instanceof PlayerInstance) {
            ((PlayerInstance) creature).sendMessage("You have left a temporary zone!");
        }

        if (creature == _owner) {
            remove();
            return;
        }
        creature.stopSkillEffects(_skill.getId());
    }

    protected void remove() {
        if (_task == null) {
            return;
        }

        _task.cancel(false);
        _task = null;
        _region.removeZone(this);

        for (Creature member : getCharactersInside()) {
            try {
                member.stopSkillEffects(_skill.getId());
            } catch (NullPointerException e) {
            }
        }
        _owner.stopSkillEffects(_skill.getId());

    }

    @Override
    protected void onDieInside(Creature creature) {
        if (creature == _owner) {
            remove();
        } else {
            creature.stopSkillEffects(_skill.getId());
        }
    }

    @Override
    protected void onReviveInside(Creature creature) {
        _skill.getEffects(_owner, creature, false, false, false);
    }
}

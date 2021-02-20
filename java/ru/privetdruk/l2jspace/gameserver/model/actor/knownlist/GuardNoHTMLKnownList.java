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

import ru.privetdruk.l2jspace.Config;
import ru.privetdruk.l2jspace.gameserver.ai.CreatureAI;
import ru.privetdruk.l2jspace.gameserver.ai.CtrlIntention;
import ru.privetdruk.l2jspace.gameserver.model.WorldObject;
import ru.privetdruk.l2jspace.gameserver.model.actor.Creature;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.GuardNoHTMLInstance;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.MonsterInstance;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;

public class GuardNoHTMLKnownList extends AttackableKnownList {
    public GuardNoHTMLKnownList(GuardNoHTMLInstance activeChar) {
        super(activeChar);
    }

    @Override
    public boolean addKnownObject(WorldObject object) {
        return addKnownObject(object, null);
    }

    @Override
    public boolean addKnownObject(WorldObject object, Creature dropper) {
        if (!super.addKnownObject(object, dropper)) {
            return false;
        }

        // Set home location of the GuardInstance (if not already done)
        if (getActiveChar().getHomeX() == 0) {
            getActiveChar().getHomeLocation();
        }

        if (object instanceof PlayerInstance) {
            // Check if the object added is a PlayerInstance that owns Karma
            final PlayerInstance player = (PlayerInstance) object;

            // Set the GuardInstance Intention to AI_INTENTION_ACTIVE
            if ((player.getKarma() > 0) && (getActiveChar().getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)) {
                getActiveChar().getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
            }
        } else if (Config.ALLOW_GUARDS && (object instanceof MonsterInstance)) {
            // Check if the object added is an aggressive MonsterInstance
            final MonsterInstance mob = (MonsterInstance) object;

            // Set the GuardInstance Intention to AI_INTENTION_ACTIVE
            if (mob.isAggressive() && (getActiveChar().getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)) {
                getActiveChar().getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
            }
        }

        return true;
    }

    @Override
    public boolean removeKnownObject(WorldObject object) {
        if (!super.removeKnownObject(object)) {
            return false;
        }

        // Check if the _aggroList of the GuardInstance is Empty
        if (getActiveChar().noTarget()) {
            // Set the GuardInstance to AI_INTENTION_IDLE
            final CreatureAI ai = getActiveChar().getAI();
            if (ai != null) {
                ai.setIntention(CtrlIntention.AI_INTENTION_IDLE, null);
            }
        }

        return true;
    }

    @Override
    public GuardNoHTMLInstance getActiveChar() {
        return (GuardNoHTMLInstance) super.getActiveChar();
    }
}
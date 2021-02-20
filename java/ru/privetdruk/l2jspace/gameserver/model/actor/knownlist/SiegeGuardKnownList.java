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

import ru.privetdruk.l2jspace.gameserver.ai.CtrlIntention;
import ru.privetdruk.l2jspace.gameserver.model.WorldObject;
import ru.privetdruk.l2jspace.gameserver.model.actor.Creature;
import ru.privetdruk.l2jspace.gameserver.model.actor.Summon;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.SiegeGuardInstance;

public class SiegeGuardKnownList extends AttackableKnownList {
    public SiegeGuardKnownList(SiegeGuardInstance activeChar) {
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

        if (getActiveChar().getHomeX() == 0) {
            getActiveChar().getHomeLocation();
        }

        // Check if siege is in progress
        if ((getActiveChar().getCastle() != null) && getActiveChar().getCastle().getSiege().isInProgress()) {
            PlayerInstance player = null;
            if (object instanceof PlayerInstance) {
                player = (PlayerInstance) object;
            } else if (object instanceof Summon) {
                player = ((Summon) object).getOwner();
            }

            // Check if player is not the defender
            if ((player != null) && ((player.getClan() == null) || (getActiveChar().getCastle().getSiege().getAttackerClan(player.getClan()) != null)) && (getActiveChar().getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)) {
                getActiveChar().getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
            }
        }

        return true;
    }

    @Override
    public SiegeGuardInstance getActiveChar() {
        return (SiegeGuardInstance) super.getActiveChar();
    }
}

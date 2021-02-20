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
package ru.privetdruk.l2jspace.gameserver.model.skills.effects;

import ru.privetdruk.l2jspace.gameserver.ai.CtrlIntention;
import ru.privetdruk.l2jspace.gameserver.model.Effect;
import ru.privetdruk.l2jspace.gameserver.model.actor.Playable;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.SiegeSummonInstance;
import ru.privetdruk.l2jspace.gameserver.model.skills.Env;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.MyTargetSelected;

/**
 * @author eX1steam
 */
public class EffectTargetMe extends Effect {
    public EffectTargetMe(Env env, EffectTemplate template) {
        super(env, template);
    }

    @Override
    public EffectType getEffectType() {
        return EffectType.TARGET_ME;
    }

    @Override
    public void onStart() {
        if (getEffected() instanceof Playable) {
            if (getEffected() instanceof SiegeSummonInstance) {
                return;
            }

            if (getEffected().getTarget() != getEffector()) {
                // Target is different - stop autoattack and break cast
                getEffected().setTarget(getEffector());
                getEffected().sendPacket(new MyTargetSelected(getEffector().getObjectId(), 0));
                getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
            }
            getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, getEffector());
        }
    }

    @Override
    public void onExit() {
    }

    @Override
    public boolean onActionTime() {
        return false;
    }
}

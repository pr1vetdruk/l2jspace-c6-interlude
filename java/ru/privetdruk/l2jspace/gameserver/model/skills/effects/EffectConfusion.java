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

import java.util.ArrayList;
import java.util.List;

import ru.privetdruk.l2jspace.commons.util.Rnd;
import ru.privetdruk.l2jspace.gameserver.ai.CtrlIntention;
import ru.privetdruk.l2jspace.gameserver.model.Effect;
import ru.privetdruk.l2jspace.gameserver.model.WorldObject;
import ru.privetdruk.l2jspace.gameserver.model.actor.Creature;
import ru.privetdruk.l2jspace.gameserver.model.skills.Env;

/**
 * @author littlecrow Implementation of the Confusion Effect
 */
final class EffectConfusion extends Effect {
    public EffectConfusion(Env env, EffectTemplate template) {
        super(env, template);
    }

    @Override
    public EffectType getEffectType() {
        return EffectType.CONFUSION;
    }

    @Override
    public void onStart() {
        getEffected().startConfused();
        onActionTime();
    }

    @Override
    public void onExit() {
        getEffected().stopConfused(this);
    }

    @Override
    public boolean onActionTime() {
        final List<Creature> targetList = new ArrayList<>();

        // Getting the possible targets
        for (WorldObject obj : getEffected().getKnownList().getKnownObjects().values()) {
            if (obj == null) {
                continue;
            }

            if ((obj instanceof Creature) && (obj != getEffected())) {
                targetList.add((Creature) obj);
            }
        }
        // if there is no target, exit function
        if (targetList.isEmpty()) {
            return true;
        }

        // Choosing randomly a new target
        final int nextTargetIdx = Rnd.get(targetList.size());
        final WorldObject target = targetList.get(nextTargetIdx);

        // Attacking the target
        getEffected().setTarget(target);
        getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
        return true;
    }
}

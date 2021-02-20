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
package ru.privetdruk.l2jspace.gameserver.model.skills.handlers;

import java.util.List;

import ru.privetdruk.l2jspace.gameserver.model.Effect;
import ru.privetdruk.l2jspace.gameserver.model.Skill;
import ru.privetdruk.l2jspace.gameserver.model.StatSet;
import ru.privetdruk.l2jspace.gameserver.model.WorldObject;
import ru.privetdruk.l2jspace.gameserver.model.actor.Creature;
import ru.privetdruk.l2jspace.gameserver.model.skills.effects.EffectSeed;

public class SkillSeed extends Skill {
    public SkillSeed(StatSet set) {
        super(set);
    }

    @Override
    public void useSkill(Creature caster, List<Creature> targets) {
        if (caster.isAlikeDead()) {
            return;
        }

        // Update Seeds Effects
        for (WorldObject target2 : targets) {
            final Creature target = (Creature) target2;
            if (target.isAlikeDead() && (getTargetType() != SkillTargetType.TARGET_CORPSE_MOB)) {
                continue;
            }

            final EffectSeed oldEffect = (EffectSeed) target.getFirstEffect(getId());
            if (oldEffect == null) {
                getEffects(caster, target, false, false, false);
            } else {
                oldEffect.increasePower();
            }

            final Effect[] effects = target.getAllEffects();
            for (Effect effect : effects) {
                if (effect.getEffectType() == Effect.EffectType.SEED) {
                    effect.rescheduleEffect();
                }
            }
        }
    }
}

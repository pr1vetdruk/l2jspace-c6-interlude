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
package ru.privetdruk.l2jspace.gameserver.handler.skillhandlers;

import java.util.List;
import java.util.logging.Logger;

import ru.privetdruk.l2jspace.gameserver.handler.ISkillHandler;
import ru.privetdruk.l2jspace.gameserver.model.Effect;
import ru.privetdruk.l2jspace.gameserver.model.Skill;
import ru.privetdruk.l2jspace.gameserver.model.WorldObject;
import ru.privetdruk.l2jspace.gameserver.model.actor.Creature;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;

public class Charge implements ISkillHandler {
    static Logger LOGGER = Logger.getLogger(Charge.class.getName());

    private static final Skill.SkillType[] SKILL_IDS =
            {
                    /* SkillType.CHARGE */
            };

    @Override
    public void useSkill(Creature creature, Skill skill, List<Creature> targets) {
        for (WorldObject target1 : targets) {
            if (!(target1 instanceof PlayerInstance)) {
                continue;
            }
            final PlayerInstance target = (PlayerInstance) target1;
            skill.getEffects(creature, target, false, false, false);
        }
        // self Effect :]

        final Effect effect = creature.getFirstEffect(skill.getId());
        if ((effect != null) && effect.isSelfEffect()) {
            // Replace old effect with new one.
            effect.exit(false);
        }
        skill.getEffectsSelf(creature);
    }

    @Override
    public Skill.SkillType[] getSkillIds() {
        return SKILL_IDS;
    }
}

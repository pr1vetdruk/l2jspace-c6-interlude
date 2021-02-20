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

import ru.privetdruk.l2jspace.gameserver.handler.ISkillHandler;
import ru.privetdruk.l2jspace.gameserver.model.Skill;
import ru.privetdruk.l2jspace.gameserver.model.WorldObject;
import ru.privetdruk.l2jspace.gameserver.model.actor.Creature;
import ru.privetdruk.l2jspace.gameserver.model.actor.Playable;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.model.skills.Formulas;

/**
 * Just a quick draft to support Wrath skill. Missing angle based calculation etc.
 */
public class CpDam implements ISkillHandler {
    private static final Skill.SkillType[] SKILL_IDS =
            {
                    Skill.SkillType.CPDAM
            };

    @Override
    public void useSkill(Creature creature, Skill skill, List<Creature> targets) {
        if (!(creature instanceof Playable)) {
            return;
        }

        if (creature.isAlikeDead()) {
            return;
        }

        final boolean bss = creature.checkBss();
        final boolean sps = creature.checkSps();
        final boolean ss = creature.checkSs();
        for (WorldObject target2 : targets) {
            if (target2 == null) {
                continue;
            }

            final Creature target = (Creature) target2;
            if ((creature instanceof PlayerInstance) && (target instanceof PlayerInstance) && target.isAlikeDead() && target.isFakeDeath()) {
                target.stopFakeDeath(null);
            } else if (target.isAlikeDead()) {
                continue;
            }

            if (target.isInvul()) {
                continue;
            }

            if (!Formulas.getInstance().calcSkillSuccess(creature, target, skill, ss, sps, bss)) {
                return;
            }

            final int damage = (int) (target.getCurrentCp() * (1 - skill.getPower()));

            // Manage attack or cast break of the target (calculating rate, sending message...)
            if (!target.isRaid() && Formulas.calcAtkBreak(target, damage)) {
                target.breakAttack();
                target.breakCast();
            }
            skill.getEffects(creature, target, ss, sps, bss);
            creature.sendDamageMessage(target, damage, false, false, false);
            target.setCurrentCp(target.getCurrentCp() - damage);
        }

        if (skill.isMagic()) {
            if (bss) {
                creature.removeBss();
            } else if (sps) {
                creature.removeSps();
            }
        } else {
            creature.removeSs();
        }
    }

    @Override
    public Skill.SkillType[] getSkillIds() {
        return SKILL_IDS;
    }
}

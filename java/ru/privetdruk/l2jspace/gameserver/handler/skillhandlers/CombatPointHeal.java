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
import ru.privetdruk.l2jspace.gameserver.handler.SkillHandler;
import ru.privetdruk.l2jspace.gameserver.model.Skill;
import ru.privetdruk.l2jspace.gameserver.model.WorldObject;
import ru.privetdruk.l2jspace.gameserver.model.actor.Creature;
import ru.privetdruk.l2jspace.gameserver.network.SystemMessageId;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.StatusUpdate;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.SystemMessage;

public class CombatPointHeal implements ISkillHandler {
    private static final Skill.SkillType[] SKILL_IDS =
            {
                    Skill.SkillType.COMBATPOINTHEAL,
                    Skill.SkillType.COMBATPOINTPERCENTHEAL
            };

    @Override
    public void useSkill(Creature creature, Skill skill, List<Creature> targets) {
        // check for other effects
        try {
            final ISkillHandler handler = SkillHandler.getInstance().getSkillHandler(Skill.SkillType.BUFF);
            if (handler != null) {
                handler.useSkill(creature, skill, targets);
            }
        } catch (Exception e) {
        }

        for (WorldObject object : targets) {
            if (!(object instanceof Creature)) {
                continue;
            }

            final Creature target = (Creature) object;
            double cp = skill.getPower();
            if (skill.getSkillType() == Skill.SkillType.COMBATPOINTPERCENTHEAL) {
                cp = (target.getMaxCp() * cp) / 100.0;
            }
            final SystemMessage sm = new SystemMessage(SystemMessageId.S1_CPS_HAVE_BEEN_RESTORED);
            sm.addNumber((int) cp);
            target.sendPacket(sm);

            target.setCurrentCp(cp + target.getCurrentCp());
            final StatusUpdate sump = new StatusUpdate(target.getObjectId());
            sump.addAttribute(StatusUpdate.CUR_CP, (int) target.getCurrentCp());
            target.sendPacket(sump);
        }
    }

    @Override
    public Skill.SkillType[] getSkillIds() {
        return SKILL_IDS;
    }
}

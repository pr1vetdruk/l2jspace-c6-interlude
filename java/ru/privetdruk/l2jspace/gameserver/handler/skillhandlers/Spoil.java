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

import ru.privetdruk.l2jspace.gameserver.ai.CtrlEvent;
import ru.privetdruk.l2jspace.gameserver.handler.ISkillHandler;
import ru.privetdruk.l2jspace.gameserver.model.Skill;
import ru.privetdruk.l2jspace.gameserver.model.WorldObject;
import ru.privetdruk.l2jspace.gameserver.model.actor.Creature;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.MonsterInstance;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.model.skills.Formulas;
import ru.privetdruk.l2jspace.gameserver.network.SystemMessageId;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.SystemMessage;

/**
 * @author _drunk_ TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style - Code Templates
 */
public class Spoil implements ISkillHandler {
    private static final Skill.SkillType[] SKILL_IDS =
            {
                    Skill.SkillType.SPOIL
            };

    @Override
    public void useSkill(Creature creature, Skill skill, List<Creature> targets) {
        if (!(creature instanceof PlayerInstance)) {
            return;
        }

        if (targets == null) {
            return;
        }

        for (WorldObject target1 : targets) {
            if (!(target1 instanceof MonsterInstance)) {
                continue;
            }

            final MonsterInstance target = (MonsterInstance) target1;
            if (target.isSpoil()) {
                creature.sendPacket(new SystemMessage(SystemMessageId.IT_HAS_ALREADY_BEEN_SPOILED));
                continue;
            }

            boolean spoil = false;
            if (!target.isDead()) {
                spoil = Formulas.calcMagicSuccess(creature, (Creature) target1, skill);
                if (spoil) {
                    target.setSpoil(true);
                    target.setSpoiledBy(creature.getObjectId());
                    creature.sendPacket(new SystemMessage(SystemMessageId.THE_SPOIL_CONDITION_HAS_BEEN_ACTIVATED));
                } else {
                    final SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_RESISTED_YOUR_S2);
                    sm.addString(target.getName());
                    sm.addSkillName(skill.getDisplayId());
                    creature.sendPacket(sm);
                }
                target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, creature);
            }
        }
    }

    @Override
    public Skill.SkillType[] getSkillIds() {
        return SKILL_IDS;
    }
}

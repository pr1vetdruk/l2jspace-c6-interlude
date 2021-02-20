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
import ru.privetdruk.l2jspace.gameserver.model.actor.Creature;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.model.skills.Stat;
import ru.privetdruk.l2jspace.gameserver.network.SystemMessageId;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.StatusUpdate;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.SystemMessage;

public class ManaHeal implements ISkillHandler {
    private static final Skill.SkillType[] SKILL_IDS =
            {
                    Skill.SkillType.MANAHEAL,
                    Skill.SkillType.MANARECHARGE,
                    Skill.SkillType.MANAHEAL_PERCENT
            };

    @Override
    public void useSkill(Creature creature, Skill skill, List<Creature> targets) {
        for (Creature target : targets) {
            if ((target == null) || target.isDead() || target.isInvul()) {
                continue;
            }

            double mp = skill.getPower();
            if (skill.getSkillType() == Skill.SkillType.MANAHEAL_PERCENT) {
                mp = (target.getMaxMp() * mp) / 100.0;
            } else {
                mp = (skill.getSkillType() == Skill.SkillType.MANARECHARGE) ? target.calcStat(Stat.RECHARGE_MP_RATE, mp, null, null) : mp;
            }

            target.setLastHealAmount((int) mp);
            target.setCurrentMp(mp + target.getCurrentMp());
            final StatusUpdate sump = new StatusUpdate(target.getObjectId());
            sump.addAttribute(StatusUpdate.CUR_MP, (int) target.getCurrentMp());
            target.sendPacket(sump);

            if ((creature instanceof PlayerInstance) && (creature != target)) {
                final SystemMessage sm = new SystemMessage(SystemMessageId.S2_MP_HAS_BEEN_RESTORED_BY_S1);
                sm.addString(creature.getName());
                sm.addNumber((int) mp);
                target.sendPacket(sm);
            } else {
                final SystemMessage sm = new SystemMessage(SystemMessageId.S1_MP_HAS_BEEN_RESTORED);
                sm.addNumber((int) mp);
                target.sendPacket(sm);
            }
        }

        if (skill.isMagic() && skill.useSpiritShot()) {
            if (creature.checkBss()) {
                creature.removeBss();
            }
            if (creature.checkSps()) {
                creature.removeSps();
            }
        } else if (skill.useSoulShot()) {
            if (creature.checkSs()) {
                creature.removeSs();
            }
        }
    }

    @Override
    public Skill.SkillType[] getSkillIds() {
        return SKILL_IDS;
    }
}

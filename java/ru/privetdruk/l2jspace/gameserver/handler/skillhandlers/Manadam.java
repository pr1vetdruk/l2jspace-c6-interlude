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
import ru.privetdruk.l2jspace.gameserver.model.actor.Summon;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.NpcInstance;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.model.skills.Formulas;
import ru.privetdruk.l2jspace.gameserver.network.SystemMessageId;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.StatusUpdate;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.SystemMessage;

/**
 * Class handling the Mana damage skill
 *
 * @author slyce
 */
public class Manadam implements ISkillHandler {
    private static final Skill.SkillType[] SKILL_IDS =
            {
                    Skill.SkillType.MANADAM
            };

    @Override
    public void useSkill(Creature creature, Skill skill, List<Creature> targets) {
        Creature target = null;
        if (creature.isAlikeDead()) {
            return;
        }

        final boolean sps = creature.checkSps();
        final boolean bss = creature.checkBss();
        for (WorldObject target2 : targets) {
            target = (Creature) target2;
            if (target.reflectSkill(skill)) {
                target = creature;
            }

            if (target == null) {
                continue;
            }

            final boolean acted = Formulas.getInstance().calcMagicAffected(creature, target, skill);
            if (target.isInvul() || !acted) {
                creature.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_MISSED));
            } else {
                final double damage = Formulas.getInstance().calcManaDam(creature, target, skill, sps, bss);
                final double mp = (damage > target.getCurrentMp() ? target.getCurrentMp() : damage);
                target.reduceCurrentMp(mp);

                if ((damage > 0) && target.isSleeping()) {
                    target.stopSleeping(null);
                }

                final StatusUpdate sump = new StatusUpdate(target.getObjectId());
                sump.addAttribute(StatusUpdate.CUR_MP, (int) target.getCurrentMp());
                target.sendPacket(sump);

                final SystemMessage sm = new SystemMessage(SystemMessageId.S2_S_MP_HAS_BEEN_DRAINED_BY_S1);
                if (creature instanceof NpcInstance) {
                    final int mobId = ((NpcInstance) creature).getNpcId();
                    sm.addNpcName(mobId);
                } else if (creature instanceof Summon) {
                    final int mobId = ((Summon) creature).getNpcId();
                    sm.addNpcName(mobId);
                } else {
                    sm.addString(creature.getName());
                }
                sm.addNumber((int) mp);
                target.sendPacket(sm);

                if (creature instanceof PlayerInstance) {
                    final SystemMessage sm2 = new SystemMessage(SystemMessageId.YOUR_OPPONENT_S_MP_WAS_REDUCED_BY_S1);
                    sm2.addNumber((int) mp);
                    creature.sendPacket(sm2);
                }
            }
        }

        if (bss) {
            creature.removeBss();
        } else if (sps) {
            creature.removeSps();
        }
    }

    @Override
    public Skill.SkillType[] getSkillIds() {
        return SKILL_IDS;
    }
}

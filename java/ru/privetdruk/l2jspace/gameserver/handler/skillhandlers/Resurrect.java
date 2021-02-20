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

import java.util.ArrayList;
import java.util.List;

import ru.privetdruk.l2jspace.gameserver.handler.ISkillHandler;
import ru.privetdruk.l2jspace.gameserver.model.Skill;
import ru.privetdruk.l2jspace.gameserver.model.WorldObject;
import ru.privetdruk.l2jspace.gameserver.model.actor.Creature;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PetInstance;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.model.skills.Formulas;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.SystemMessage;
import ru.privetdruk.l2jspace.gameserver.taskmanager.DecayTaskManager;

public class Resurrect implements ISkillHandler {
    private static final Skill.SkillType[] SKILL_IDS =
            {
                    Skill.SkillType.RESURRECT
            };

    @Override
    public void useSkill(Creature creature, Skill skill, List<Creature> targets) {
        PlayerInstance player = null;
        if (creature instanceof PlayerInstance) {
            player = (PlayerInstance) creature;
        }

        Creature target = null;
        PlayerInstance targetPlayer;
        final List<Creature> targetToRes = new ArrayList<>();
        for (WorldObject target2 : targets) {
            if (target2 == null) {
                continue;
            }

            target = (Creature) target2;
            if (target instanceof PlayerInstance) {
                targetPlayer = (PlayerInstance) target;

                // Check for same party or for same clan, if target is for clan.
                if ((skill.getTargetType() == Skill.SkillTargetType.TARGET_CORPSE_CLAN) && ((player == null) || (player.getClanId() != targetPlayer.getClanId()))) {
                    continue;
                }
            }

            if (target.isSpawned()) {
                targetToRes.add(target);
            }
        }

        if (targetToRes.isEmpty()) {
            creature.abortCast();
            creature.sendPacket(SystemMessage.sendString("No valid target to resurrect"));
        }

        for (Creature c : targetToRes) {
            if (creature instanceof PlayerInstance) {
                if (c instanceof PlayerInstance) {
                    ((PlayerInstance) c).reviveRequest((PlayerInstance) creature, skill, false);
                } else if (c instanceof PetInstance) {
                    if (((PetInstance) c).getOwner() == creature) {
                        c.doRevive(Formulas.getInstance().calculateSkillResurrectRestorePercent(skill.getPower(), creature));
                    } else {
                        ((PetInstance) c).getOwner().reviveRequest((PlayerInstance) creature, skill, true);
                    }
                } else {
                    c.doRevive(Formulas.getInstance().calculateSkillResurrectRestorePercent(skill.getPower(), creature));
                }
            } else {
                DecayTaskManager.getInstance().cancelDecayTask(c);
                c.doRevive(Formulas.getInstance().calculateSkillResurrectRestorePercent(skill.getPower(), creature));
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

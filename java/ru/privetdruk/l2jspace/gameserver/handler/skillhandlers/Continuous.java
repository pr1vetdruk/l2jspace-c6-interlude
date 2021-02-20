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

import ru.privetdruk.l2jspace.commons.util.Rnd;

//

import ru.privetdruk.l2jspace.gameserver.ai.CtrlEvent;
import ru.privetdruk.l2jspace.gameserver.ai.CtrlIntention;
import ru.privetdruk.l2jspace.gameserver.datatables.SkillTable;
import ru.privetdruk.l2jspace.gameserver.handler.ISkillHandler;
import ru.privetdruk.l2jspace.gameserver.instancemanager.DuelManager;
import ru.privetdruk.l2jspace.gameserver.model.Effect;
import ru.privetdruk.l2jspace.gameserver.model.Skill;
import ru.privetdruk.l2jspace.gameserver.model.WorldObject;
import ru.privetdruk.l2jspace.gameserver.model.actor.Attackable;
import ru.privetdruk.l2jspace.gameserver.model.actor.Creature;
import ru.privetdruk.l2jspace.gameserver.model.actor.Playable;
import ru.privetdruk.l2jspace.gameserver.model.actor.Summon;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.DoorInstance;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.NpcInstance;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.model.skills.Formulas;
import ru.privetdruk.l2jspace.gameserver.network.SystemMessageId;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.SystemMessage;

public class Continuous implements ISkillHandler {
    private static final Skill.SkillType[] SKILL_IDS =
            {
                    Skill.SkillType.BUFF,
                    Skill.SkillType.DEBUFF,
                    Skill.SkillType.DOT,
                    Skill.SkillType.MDOT,
                    Skill.SkillType.POISON,
                    Skill.SkillType.BLEED,
                    Skill.SkillType.HOT,
                    Skill.SkillType.CPHOT,
                    Skill.SkillType.MPHOT,
                    // Skill.SkillType.MANAHEAL,
                    // Skill.SkillType.MANA_BY_LEVEL,
                    Skill.SkillType.FEAR,
                    Skill.SkillType.CONT,
                    Skill.SkillType.WEAKNESS,
                    Skill.SkillType.REFLECT,
                    Skill.SkillType.UNDEAD_DEFENSE,
                    Skill.SkillType.AGGDEBUFF,
                    Skill.SkillType.FORCE_BUFF
            };

    @Override
    public void useSkill(Creature creature, Skill skillValue, List<Creature> targets) {
        if (creature == null) {
            return;
        }

        PlayerInstance player = null;
        if (creature instanceof PlayerInstance) {
            player = (PlayerInstance) creature;
        }

        Skill usedSkill = skillValue;
        if (usedSkill.getEffectId() != 0) {
            final int skillLevel = usedSkill.getEffectLvl();
            final int skillEffectId = usedSkill.getEffectId();
            Skill skill;
            if (skillLevel == 0) {
                skill = SkillTable.getInstance().getSkill(skillEffectId, 1);
            } else {
                skill = SkillTable.getInstance().getSkill(skillEffectId, skillLevel);
            }

            if (skill != null) {
                usedSkill = skill;
            }
        }

        final Skill skill = usedSkill;
        final boolean bss = creature.checkBss();
        final boolean sps = creature.checkSps();
        final boolean ss = creature.checkSs();
        for (WorldObject target2 : targets) {
            Creature target = (Creature) target2;
            if (target == null) {
                continue;
            }

            if ((target instanceof PlayerInstance) && (creature instanceof Playable) && skill.isOffensive()) {
                final PlayerInstance targetChar = (creature instanceof PlayerInstance) ? (PlayerInstance) creature : ((Summon) creature).getOwner();
                final PlayerInstance attacked = (PlayerInstance) target;
                if ((attacked.getClanId() != 0) && (targetChar.getClanId() != 0) && (attacked.getClanId() == targetChar.getClanId()) && (attacked.getPvpFlag() == 0)) {
                    continue;
                }
                if ((attacked.getAllyId() != 0) && (targetChar.getAllyId() != 0) && (attacked.getAllyId() == targetChar.getAllyId()) && (attacked.getPvpFlag() == 0)) {
                    continue;
                }
            }

            if ((skill.getSkillType() != Skill.SkillType.BUFF) && (skill.getSkillType() != Skill.SkillType.HOT) && (skill.getSkillType() != Skill.SkillType.CPHOT) && (skill.getSkillType() != Skill.SkillType.MPHOT) && (skill.getSkillType() != Skill.SkillType.UNDEAD_DEFENSE) && (skill.getSkillType() != Skill.SkillType.AGGDEBUFF) && (skill.getSkillType() != Skill.SkillType.CONT) && target.reflectSkill(skill)) {
                target = creature;
            }

            // Walls and Door should not be buffed
            if ((target instanceof DoorInstance) && ((skill.getSkillType() == Skill.SkillType.BUFF) || (skill.getSkillType() == Skill.SkillType.HOT))) {
                continue;
            }

            // Anti-Buff Protection prevents you from getting buffs by other players
            if ((creature instanceof Playable) && (target != creature) && target.isBuffProtected() && !skill.isHeroSkill() && ((skill.getSkillType() == Skill.SkillType.BUFF) || (skill.getSkillType() == Skill.SkillType.HEAL_PERCENT) || (skill.getSkillType() == Skill.SkillType.FORCE_BUFF) || (skill.getSkillType() == Skill.SkillType.MANAHEAL_PERCENT) || (skill.getSkillType() == Skill.SkillType.COMBATPOINTHEAL) || (skill.getSkillType() == Skill.SkillType.REFLECT))) {
                continue;
            }

            // Player holding a cursed weapon can't be buffed and can't buff
            if ((skill.getSkillType() == Skill.SkillType.BUFF) && (target != creature)) {
                if ((target instanceof PlayerInstance) && ((PlayerInstance) target).isCursedWeaponEquiped()) {
                    continue;
                } else if ((player != null) && player.isCursedWeaponEquiped()) {
                    continue;
                }
            }

            // Possibility of a lethal strike
            if (!target.isRaid() && (!(target instanceof NpcInstance) || (((NpcInstance) target).getNpcId() != 35062))) {
                final int chance = Rnd.get(1000);
                Formulas.getInstance();
                if ((skill.getLethalChance2() > 0) && (chance < Formulas.calcLethal(creature, target, skill.getLethalChance2()))) {
                    if (target instanceof NpcInstance) {
                        target.reduceCurrentHp(target.getCurrentHp() - 1, creature);
                        creature.sendPacket(new SystemMessage(SystemMessageId.LETHAL_STRIKE));
                    }
                } else {
                    Formulas.getInstance();
                    if ((skill.getLethalChance1() > 0) && (chance < Formulas.calcLethal(creature, target, skill.getLethalChance1())) && (target instanceof NpcInstance)) {
                        target.reduceCurrentHp(target.getCurrentHp() / 2, creature);
                        creature.sendPacket(new SystemMessage(SystemMessageId.LETHAL_STRIKE));
                    }
                }
            }

            if (skill.isOffensive()) {
                final boolean acted = Formulas.getInstance().calcSkillSuccess(creature, target, skill, ss, sps, bss);
                if (!acted) {
                    creature.sendPacket(new SystemMessage(SystemMessageId.YOUR_ATTACK_HAS_FAILED));
                    continue;
                }
            } else if ((skill.getSkillType() == Skill.SkillType.BUFF) && !Formulas.getInstance().calcBuffSuccess(target, skill)) {
                if (player != null) {
                    final SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_RESISTED_YOUR_S2);
                    sm.addString(target.getName());
                    sm.addSkillName(skill.getDisplayId());
                    creature.sendPacket(sm);
                }
                continue;
            }

            if (skill.isToggle()) {
                boolean stopped = false;

                final Effect[] effects = target.getAllEffects();
                if (effects != null) {
                    for (Effect e : effects) {
                        if ((e != null) && (e.getSkill().getId() == skill.getId())) {
                            e.exit(false);
                            stopped = true;
                        }
                    }
                }

                if (stopped) {
                    break;
                }
            }

            // If target is not in game anymore...
            if ((target instanceof PlayerInstance) && !((PlayerInstance) target).isOnline()) {
                continue;
            }

            // if this is a debuff let the duel manager know about it so the debuff can be removed after the duel (player & target must be in the same duel)
            if ((target instanceof PlayerInstance) && (player != null) && ((PlayerInstance) target).isInDuel() && ((skill.getSkillType() == Skill.SkillType.DEBUFF) || (skill.getSkillType() == Skill.SkillType.BUFF)) && (player.getDuelId() == ((PlayerInstance) target).getDuelId())) {
                final DuelManager dm = DuelManager.getInstance();
                if (dm != null) {
                    final Effect[] effects = skill.getEffects(creature, target, ss, sps, bss);
                    if (effects != null) {
                        for (Effect buff : effects) {
                            if (buff != null) {
                                dm.onBuff(((PlayerInstance) target), buff);
                            }
                        }
                    }
                }
            } else {
                skill.getEffects(creature, target, ss, sps, bss);
            }

            if (skill.getSkillType() == Skill.SkillType.AGGDEBUFF) {
                if (target instanceof Attackable) {
                    target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, creature, (int) skill.getPower());
                } else if (target instanceof Playable) {
                    if (target.getTarget() == creature) {
                        target.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, creature);
                    } else {
                        target.setTarget(creature);
                    }
                }
            }

            if (target.isDead() && (skill.getTargetType() == Skill.SkillTargetType.TARGET_AREA_CORPSE_MOB) && (target instanceof NpcInstance)) {
                ((NpcInstance) target).endDecayTask();
            }
        }

        if (!skill.isToggle()) {
            if (skill.isMagic() && skill.useSpiritShot()) {
                if (bss) {
                    creature.removeBss();
                } else if (sps) {
                    creature.removeSps();
                }
            } else if (skill.useSoulShot()) {
                creature.removeSs();
            }
        }

        skill.getEffectsSelf(creature);
    }

    @Override
    public Skill.SkillType[] getSkillIds() {
        return SKILL_IDS;
    }
}

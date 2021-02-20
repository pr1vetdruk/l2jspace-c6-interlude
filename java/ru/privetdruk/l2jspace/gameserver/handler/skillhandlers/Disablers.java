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
import java.util.logging.Logger;

import ru.privetdruk.l2jspace.Config;
import ru.privetdruk.l2jspace.commons.concurrent.ThreadPool;
import ru.privetdruk.l2jspace.commons.util.Rnd;
import ru.privetdruk.l2jspace.gameserver.ai.AttackableAI;
import ru.privetdruk.l2jspace.gameserver.ai.CtrlEvent;
import ru.privetdruk.l2jspace.gameserver.ai.CtrlIntention;
import ru.privetdruk.l2jspace.gameserver.datatables.xml.ExperienceData;
import ru.privetdruk.l2jspace.gameserver.handler.ISkillHandler;
import ru.privetdruk.l2jspace.gameserver.handler.SkillHandler;
import ru.privetdruk.l2jspace.gameserver.model.Effect;
import ru.privetdruk.l2jspace.gameserver.model.Skill;
import ru.privetdruk.l2jspace.gameserver.model.WorldObject;
import ru.privetdruk.l2jspace.gameserver.model.actor.Attackable;
import ru.privetdruk.l2jspace.gameserver.model.actor.Creature;
import ru.privetdruk.l2jspace.gameserver.model.actor.Summon;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PetInstance;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.SiegeSummonInstance;
import ru.privetdruk.l2jspace.gameserver.model.actor.tasks.player.CancelSkillRestoreTask;
import ru.privetdruk.l2jspace.gameserver.model.skills.Formulas;
import ru.privetdruk.l2jspace.gameserver.model.skills.Stat;
import ru.privetdruk.l2jspace.gameserver.network.SystemMessageId;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.SystemMessage;

/**
 * This Handles Disabler skills
 *
 * @author _drunk_
 */
public class Disablers implements ISkillHandler {
    protected static final Logger LOGGER = Logger.getLogger(Disablers.class.getName());

    private static final Skill.SkillType[] SKILL_IDS =
            {
                    Skill.SkillType.STUN,
                    Skill.SkillType.ROOT,
                    Skill.SkillType.SLEEP,
                    Skill.SkillType.CONFUSION,
                    Skill.SkillType.AGGDAMAGE,
                    Skill.SkillType.AGGREDUCE,
                    Skill.SkillType.AGGREDUCE_CHAR,
                    Skill.SkillType.AGGREMOVE,
                    Skill.SkillType.UNBLEED,
                    Skill.SkillType.UNPOISON,
                    Skill.SkillType.MUTE,
                    Skill.SkillType.FAKE_DEATH,
                    Skill.SkillType.CONFUSE_MOB_ONLY,
                    Skill.SkillType.NEGATE,
                    Skill.SkillType.CANCEL,
                    Skill.SkillType.PARALYZE,
                    Skill.SkillType.ERASE,
                    Skill.SkillType.MAGE_BANE,
                    Skill.SkillType.WARRIOR_BANE,
                    Skill.SkillType.BETRAY
            };

    @Override
    public void useSkill(Creature creature, Skill skill, List<Creature> targets) {
        final Skill.SkillType type = skill.getSkillType();
        final boolean bss = creature.checkBss();
        final boolean sps = creature.checkSps();
        final boolean ss = creature.checkSs();
        for (WorldObject target2 : targets) {
            // Get a target
            if (!(target2 instanceof Creature)) {
                continue;
            }

            Creature target = (Creature) target2;
            if (target.isDead()) {
                continue;
            }

            switch (type) {
                case BETRAY: {
                    if (Formulas.getInstance().calcSkillSuccess(creature, target, skill, ss, sps, bss)) {
                        skill.getEffects(creature, target, ss, sps, bss);
                    } else {
                        final SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_RESISTED_YOUR_S2);
                        sm.addString(target.getName());
                        sm.addSkillName(skill.getId());
                        creature.sendPacket(sm);
                    }
                    break;
                }
                case FAKE_DEATH: {
                    // stun/fakedeath is not mdef dependant, it depends on lvl difference, target CON and power of stun
                    skill.getEffects(creature, target, ss, sps, bss);
                    break;
                }
                case STUN: {
                    // Calculate skill evasion
                    if (Formulas.calcPhysicalSkillEvasion(target, skill)) {
                        creature.sendPacket(new SystemMessage(SystemMessageId.YOUR_ATTACK_HAS_FAILED));
                        break;
                    }
                    // Calculate vengeance
                    if (target.vengeanceSkill(skill)) {
                        target = creature;
                    }
                    // fallthrough?
                }
                case ROOT: {
                    if (target.reflectSkill(skill)) {
                        target = creature;
                    }
                    if (Formulas.getInstance().calcSkillSuccess(creature, target, skill, ss, sps, bss)) {
                        skill.getEffects(creature, target, ss, sps, bss);
                    } else if (creature instanceof PlayerInstance) {
                        final SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_RESISTED_YOUR_S2);
                        sm.addString(target.getName());
                        sm.addSkillName(skill.getDisplayId());
                        creature.sendPacket(sm);
                    }
                    break;
                }
                case SLEEP:
                case PARALYZE: // use same as root for now
                {
                    if (target.reflectSkill(skill)) {
                        target = creature;
                    }
                    if (Formulas.getInstance().calcSkillSuccess(creature, target, skill, ss, sps, bss)) {
                        skill.getEffects(creature, target, ss, sps, bss);
                    } else if (creature instanceof PlayerInstance) {
                        final SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_RESISTED_YOUR_S2);
                        sm.addString(target.getName());
                        sm.addSkillName(skill.getDisplayId());
                        creature.sendPacket(sm);
                    }
                    break;
                }
                case CONFUSION:
                case MUTE: {
                    if (target.reflectSkill(skill)) {
                        target = creature;
                    }
                    if (Formulas.getInstance().calcSkillSuccess(creature, target, skill, ss, sps, bss)) {
                        skill.getEffects(creature, target, ss, sps, bss);
                    } else if (creature instanceof PlayerInstance) {
                        final SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_RESISTED_YOUR_S2);
                        sm.addString(target.getName());
                        sm.addSkillName(skill.getDisplayId());
                        creature.sendPacket(sm);
                    }
                    break;
                }
                case CONFUSE_MOB_ONLY: {
                    // do nothing if not on mob
                    if (Formulas.getInstance().calcSkillSuccess(creature, target, skill, ss, sps, bss)) {
                        final Effect[] effects = target.getAllEffects();
                        for (Effect e : effects) {
                            if (e.getSkill().getSkillType() == type) {
                                e.exit(false);
                            }
                        }
                        skill.getEffects(creature, target, ss, sps, bss);
                    } else if (creature instanceof PlayerInstance) {
                        creature.sendPacket(new SystemMessage(SystemMessageId.S1_HAS_RESISTED_YOUR_S2));
                    }
                    break; // Used to be fallthrough.
                }
                case AGGDAMAGE: {
                    if (target instanceof Attackable) {
                        target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, creature, (int) ((150 * skill.getPower()) / (target.getLevel() + 7)));
                    }
                    // TODO [Nemesiss] should this have 100% chance?
                    skill.getEffects(creature, target, ss, sps, bss);
                    break;
                }
                case AGGREDUCE: {
                    // these skills needs to be rechecked
                    if (target instanceof Attackable) {
                        skill.getEffects(creature, target, ss, sps, bss);
                        final double aggdiff = ((Attackable) target).getHating(creature) - target.calcStat(Stat.AGGRESSION, ((Attackable) target).getHating(creature), target, skill);
                        if (skill.getPower() > 0) {
                            ((Attackable) target).reduceHate(null, (int) skill.getPower());
                        } else if (aggdiff > 0) {
                            ((Attackable) target).reduceHate(null, (int) aggdiff);
                        }
                    }
                    break;
                }
                case AGGREDUCE_CHAR: {
                    // these skills needs to be rechecked
                    if (skill.getName().equals("Bluff")) {
                        if (target instanceof Attackable) {
                            final Attackable attackable = (Attackable) target;
                            attackable.stopHating(creature);
                            if (attackable.getMostHated() == null) {
                                ((AttackableAI) attackable.getAI()).setGlobalAggro(-25);
                                attackable.clearAggroList();
                                attackable.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
                                attackable.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
                                attackable.setWalking();
                            }
                        }
                        skill.getEffects(creature, target, ss, sps, bss);
                    } else if (Formulas.getInstance().calcSkillSuccess(creature, target, skill, ss, sps, bss)) {
                        if (target instanceof Attackable) {
                            final Attackable targ = (Attackable) target;
                            targ.stopHating(creature);
                            if (targ.getMostHated() == null) {
                                ((AttackableAI) targ.getAI()).setGlobalAggro(-25);
                                targ.clearAggroList();
                                targ.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
                                targ.setWalking();
                            }
                        }
                        skill.getEffects(creature, target, ss, sps, bss);
                    } else if (creature instanceof PlayerInstance) {
                        final SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_RESISTED_YOUR_S2);
                        sm.addString(target.getName());
                        sm.addSkillName(skill.getId());
                        creature.sendPacket(sm);
                    }
                    break;
                }
                case AGGREMOVE: {
                    // these skills needs to be rechecked
                    if ((target instanceof Attackable) && !target.isRaid()) {
                        if (Formulas.getInstance().calcSkillSuccess(creature, target, skill, ss, sps, bss)) {
                            if (skill.getTargetType() == Skill.SkillTargetType.TARGET_UNDEAD) {
                                if (target.isUndead()) {
                                    ((Attackable) target).reduceHate(null, ((Attackable) target).getHating(((Attackable) target).getMostHated()));
                                }
                            } else {
                                ((Attackable) target).reduceHate(null, ((Attackable) target).getHating(((Attackable) target).getMostHated()));
                            }
                        } else if (creature instanceof PlayerInstance) {
                            final SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_RESISTED_YOUR_S2);
                            sm.addString(target.getName());
                            sm.addSkillName(skill.getId());
                            creature.sendPacket(sm);
                        }
                    }
                    break;
                }
                case UNBLEED: {
                    negateEffect(target, Skill.SkillType.BLEED, skill.getPower());
                    break;
                }
                case UNPOISON: {
                    negateEffect(target, Skill.SkillType.POISON, skill.getPower());
                    break;
                }
                case ERASE: {
                    if (Formulas.getInstance().calcSkillSuccess(creature, target, skill, ss, sps, bss)
                            // Doesn't affect siege golem, wild hog cannon and Pets
                            && !(target instanceof SiegeSummonInstance) && !(target instanceof PetInstance)) {
                        PlayerInstance summonOwner = null;
                        Summon summonPet = null;
                        summonOwner = ((Summon) target).getOwner();
                        summonPet = summonOwner.getPet();
                        summonPet.unSummon(summonOwner);
                        summonOwner.sendPacket(new SystemMessage(SystemMessageId.LETHAL_STRIKE));
                    } else if (creature instanceof PlayerInstance) {
                        final SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_RESISTED_YOUR_S2);
                        sm.addString(target.getName());
                        sm.addSkillName(skill.getId());
                        creature.sendPacket(sm);
                    }
                    break;
                }
                case MAGE_BANE: {
                    for (WorldObject t : targets) {
                        Creature target1 = (Creature) t;
                        if (target1.reflectSkill(skill)) {
                            target1 = creature;
                        }
                        if (!Formulas.getInstance().calcSkillSuccess(creature, target1, skill, ss, sps, bss)) {
                            continue;
                        }
                        final Effect[] effects = target1.getAllEffects();
                        for (Effect e : effects) {
                            if (e.getStackType().equals("mAtkSpeedUp") || e.getStackType().equals("mAtk") || (e.getSkill().getId() == 1059) || (e.getSkill().getId() == 1085) || (e.getSkill().getId() == 4356) || (e.getSkill().getId() == 4355)) {
                                e.exit();
                            }
                        }
                    }
                    break;
                }
                case WARRIOR_BANE: {
                    for (WorldObject t : targets) {
                        Creature target1 = (Creature) t;
                        if (target1.reflectSkill(skill)) {
                            target1 = creature;
                        }
                        if (!Formulas.getInstance().calcSkillSuccess(creature, target1, skill, ss, sps, bss)) {
                            continue;
                        }
                        final Effect[] effects = target1.getAllEffects();
                        for (Effect e : effects) {
                            if (e.getStackType().equals("SpeedUp") || e.getStackType().equals("pAtkSpeedUp") || (e.getSkill().getId() == 1204) || (e.getSkill().getId() == 1086) || (e.getSkill().getId() == 4342) || (e.getSkill().getId() == 4357)) {
                                e.exit();
                            }
                        }
                    }
                    break;
                }
                case CANCEL: {
                    if (target.reflectSkill(skill)) {
                        target = creature;
                    }
                    if (skill.getId() == 1056) {
                        // If target isInvul (for example Celestial shield) CANCEL doesn't work
                        if (target.isInvul()) {
                            if (creature instanceof PlayerInstance) {
                                final SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_RESISTED_YOUR_S2);
                                sm.addString(target.getName());
                                sm.addSkillName(skill.getDisplayId());
                                creature.sendPacket(sm);
                            }
                            break;
                        }
                        if (target.isRaid()) {
                            if (creature instanceof PlayerInstance) {
                                final SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_RESISTED_YOUR_S2);
                                sm.addString(target.getName());
                                sm.addSkillName(skill.getDisplayId());
                                creature.sendPacket(sm);
                            }
                            break;
                        }

                        final List<Skill> cancelledBuffs = new ArrayList<>();
                        int lvlmodifier = 52 + (skill.getLevel() * 2);
                        if (skill.getLevel() == 12) {
                            lvlmodifier = (ExperienceData.getInstance().getMaxLevel() - 1);
                        }
                        int landrate = (int) skill.getPower();
                        if ((target.getLevel() - lvlmodifier) > 0) {
                            landrate = 90 - (4 * (target.getLevel() - lvlmodifier));
                        }
                        landrate = (int) target.calcStat(Stat.CANCEL_VULN, landrate, target, null);
                        if (Rnd.get(100) < landrate) {
                            final Effect[] effects = target.getAllEffects();
                            int maxfive = 5;
                            for (Effect e : effects) {
                                switch (e.getEffectType()) {
                                    case SIGNET_GROUND:
                                    case SIGNET_EFFECT: {
                                        continue;
                                    }
                                }

                                if ((e.getSkill().getId() != 4082) && (e.getSkill().getId() != 4215) && (e.getSkill().getId() != 5182) && (e.getSkill().getId() != 4515) && (e.getSkill().getId() != 110) && (e.getSkill().getId() != 111) && (e.getSkill().getId() != 1323) && (e.getSkill().getId() != 1325))
                                // Cannot cancel skills 4082, 4215, 4515, 110, 111, 1323, 1325
                                {
                                    if (e.getSkill().getSkillType() != Skill.SkillType.BUFF) {
                                        e.exit(true);
                                    } else {
                                        int rate = 100;
                                        final int level = e.getLevel();
                                        if (level > 0) {
                                            rate = 150 / (1 + level);
                                        }

                                        if (rate > 95) {
                                            rate = 95;
                                        } else if (rate < 5) {
                                            rate = 5;
                                        }

                                        if (Rnd.get(100) < rate) {
                                            if (Config.RESTORE_CANCELLED_BUFFS_SECONDS > 0) {
                                                // store them
                                                if (!cancelledBuffs.contains(e.getSkill())) {
                                                    cancelledBuffs.add(e.getSkill());
                                                }
                                            }
                                            // cancel them
                                            e.exit(true);
                                            maxfive--;
                                            if (maxfive == 0) {
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                            if ((Config.RESTORE_CANCELLED_BUFFS_SECONDS > 0) && (cancelledBuffs.size() > 0)) {
                                ThreadPool.schedule(new CancelSkillRestoreTask((PlayerInstance) target, cancelledBuffs), Config.RESTORE_CANCELLED_BUFFS_SECONDS * 1000);
                            }
                        } else if (creature instanceof PlayerInstance) {
                            final SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_RESISTED_YOUR_S2);
                            sm.addString(target.getName());
                            sm.addSkillName(skill.getDisplayId());
                            creature.sendPacket(sm);
                        }
                        break;
                    }

                    int landrate = (int) skill.getPower();
                    landrate = (int) target.calcStat(Stat.CANCEL_VULN, landrate, target, null);
                    if (Rnd.get(100) < landrate) {
                        final Effect[] effects = target.getAllEffects();
                        int maxdisp = (int) skill.getNegatePower();
                        if (maxdisp == 0) {
                            maxdisp = Config.BUFFS_MAX_AMOUNT + Config.DEBUFFS_MAX_AMOUNT + 6;
                        }
                        for (Effect e : effects) {
                            switch (e.getEffectType()) {
                                case SIGNET_GROUND:
                                case SIGNET_EFFECT: {
                                    continue;
                                }
                            }

                            if ((e.getSkill().getId() != 4082) && (e.getSkill().getId() != 4215) && (e.getSkill().getId() != 5182) && (e.getSkill().getId() != 4515) && (e.getSkill().getId() != 110) && (e.getSkill().getId() != 111) && (e.getSkill().getId() != 1323) && (e.getSkill().getId() != 1325) && (e.getSkill().getSkillType() == Skill.SkillType.BUFF)) {
                                int rate = 100;
                                final int level = e.getLevel();
                                if (level > 0) {
                                    rate = 150 / (1 + level);
                                }

                                if (rate > 95) {
                                    rate = 95;
                                } else if (rate < 5) {
                                    rate = 5;
                                }

                                if (Rnd.get(100) < rate) {
                                    e.exit(true);
                                    maxdisp--;
                                    if (maxdisp == 0) {
                                        break;
                                    }
                                }
                            }
                        }
                    } else if (creature instanceof PlayerInstance) {
                        final SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_RESISTED_YOUR_S2);
                        sm.addString(target.getName());
                        sm.addSkillName(skill.getDisplayId());
                        creature.sendPacket(sm);
                    }
                    break;
                }
                case NEGATE: {
                    float negatePower;
                    if (skill.getId() == 2275) // fishing potion
                    {
                        negatePower = skill.getNegatePower();
                        final int negateId = skill.getNegateId();
                        negateEffect(target, Skill.SkillType.BUFF, negatePower, negateId);
                    } else // all others negate type skills
                    {
                        final String[] negateSkillTypes = skill.getNegateSkillTypes();
                        final String[] negateEffectTypes = skill.getNegateEffectTypes();
                        negatePower = skill.getNegatePower();
                        for (String stat : negateSkillTypes) {
                            stat = stat.toLowerCase().intern();
                            if (stat.equals("buff")) {
                                int lvlmodifier = 52 + (skill.getMagicLevel() * 2);
                                if (skill.getMagicLevel() == 12) {
                                    lvlmodifier = (ExperienceData.getInstance().getMaxLevel() - 1);
                                }
                                int landrate = 90;
                                if ((target.getLevel() - lvlmodifier) > 0) {
                                    landrate = 90 - (4 * (target.getLevel() - lvlmodifier));
                                }
                                landrate = (int) target.calcStat(Stat.CANCEL_VULN, landrate, target, null);
                                if (Rnd.get(100) < landrate) {
                                    negateEffect(target, Skill.SkillType.BUFF, -1);
                                }
                            }
                            if (stat.equals("debuff")) {
                                negateEffect(target, Skill.SkillType.DEBUFF, -1);
                            }
                            if (stat.equals("weakness")) {
                                negateEffect(target, Skill.SkillType.WEAKNESS, -1);
                            }
                            if (stat.equals("stun")) {
                                negateEffect(target, Skill.SkillType.STUN, -1);
                            }
                            if (stat.equals("sleep")) {
                                negateEffect(target, Skill.SkillType.SLEEP, -1);
                            }
                            if (stat.equals("mdam")) {
                                negateEffect(target, Skill.SkillType.MDAM, -1);
                            }
                            if (stat.equals("confusion")) {
                                negateEffect(target, Skill.SkillType.CONFUSION, -1);
                            }
                            if (stat.equals("mute")) {
                                negateEffect(target, Skill.SkillType.MUTE, -1);
                            }
                            if (stat.equals("fear")) {
                                negateEffect(target, Skill.SkillType.FEAR, -1);
                            }
                            if (stat.equals("poison")) {
                                negateEffect(target, Skill.SkillType.POISON, negatePower);
                            }
                            if (stat.equals("bleed")) {
                                negateEffect(target, Skill.SkillType.BLEED, negatePower);
                            }
                            if (stat.equals("paralyze")) {
                                negateEffect(target, Skill.SkillType.PARALYZE, -1);
                            }
                            if (stat.equals("root")) {
                                negateEffect(target, Skill.SkillType.ROOT, -1);
                            }
                            if (stat.equals("heal")) {
                                final ISkillHandler healhandler = SkillHandler.getInstance().getSkillHandler(Skill.SkillType.HEAL);
                                if (healhandler == null) {
                                    LOGGER.warning("Couldn't find skill handler for HEAL.");
                                    continue;
                                }

                                final List<Creature> tgts = new ArrayList<>();
                                tgts.add(target);

                                // try
                                // {
                                healhandler.useSkill(creature, skill, tgts);
                                // }
                                // catch (IOException e)
                                // {
                                // LOGGER.warning(e.getMessage());
                                // }
                            }
                        }
                        for (String stat : negateEffectTypes) {
                            Effect.EffectType effectType = null;
                            try {
                                effectType = Effect.EffectType.valueOf(stat.toUpperCase());
                            } catch (Exception e) {
                                //
                            }
                            if (effectType != null) {
                                switch (effectType) {
                                    case BUFF: {
                                        int lvlmodifier = 52 + (skill.getMagicLevel() * 2);
                                        if (skill.getMagicLevel() == 12) {
                                            lvlmodifier = (ExperienceData.getInstance().getMaxLevel() - 1);
                                        }
                                        int landrate = 90;
                                        if ((target.getLevel() - lvlmodifier) > 0) {
                                            landrate = 90 - (4 * (target.getLevel() - lvlmodifier));
                                        }
                                        landrate = (int) target.calcStat(Stat.CANCEL_VULN, landrate, target, null);
                                        if (Rnd.get(100) < landrate) {
                                            target.stopEffects(effectType);
                                        }
                                    }
                                    break;
                                    default: {
                                        target.stopEffects(effectType);
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }
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

        // self Effect :]
        final Effect effect = creature.getFirstEffect(skill.getId());
        if ((effect != null) && effect.isSelfEffect()) {
            // Replace old effect with new one.
            effect.exit(false);
        }
        skill.getEffectsSelf(creature);
    }

    private void negateEffect(Creature target, Skill.SkillType type, double power) {
        negateEffect(target, type, power, 0);
    }

    private void negateEffect(Creature target, Skill.SkillType type, double power, int skillId) {
        final Effect[] effects = target.getAllEffects();
        for (Effect e : effects) {
            if (((e.getSkill() != null) && (e.getSkill().getId() == 4215)) || (e.getSkill().getId() == 4515)) {
                continue; // skills cannot be removed
            } else if (power == -1) // if power is -1 the effect is always removed without power/lvl check ^^
            {
                if ((e.getSkill().getSkillType() == type) || ((e.getSkill().getEffectType() != null) && (e.getSkill().getEffectType() == type))) {
                    if (skillId != 0) {
                        if (skillId == e.getSkill().getId()) {
                            e.exit(true);
                        }
                    } else {
                        e.exit(true);
                    }
                }
            } else if (((e.getSkill().getSkillType() == type) && (e.getSkill().getPower() <= power)) || ((e.getSkill().getEffectType() != null) && (e.getSkill().getEffectType() == type) && (e.getSkill().getEffectLvl() <= power))) {
                if (skillId != 0) {
                    if (skillId == e.getSkill().getId()) {
                        e.exit(true);
                    }
                } else {
                    e.exit(true);
                }
            }
        }
    }

    @Override
    public Skill.SkillType[] getSkillIds() {
        return SKILL_IDS;
    }
}

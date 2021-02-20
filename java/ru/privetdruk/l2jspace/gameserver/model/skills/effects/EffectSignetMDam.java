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
package ru.privetdruk.l2jspace.gameserver.model.skills.effects;

import java.util.ArrayList;
import java.util.List;

import ru.privetdruk.l2jspace.gameserver.ai.CtrlEvent;
import ru.privetdruk.l2jspace.gameserver.datatables.sql.NpcTable;
import ru.privetdruk.l2jspace.gameserver.instancemanager.IdManager;
import ru.privetdruk.l2jspace.gameserver.model.Effect;
import ru.privetdruk.l2jspace.gameserver.model.Location;
import ru.privetdruk.l2jspace.gameserver.model.Skill;
import ru.privetdruk.l2jspace.gameserver.model.World;
import ru.privetdruk.l2jspace.gameserver.model.actor.Attackable;
import ru.privetdruk.l2jspace.gameserver.model.actor.Creature;
import ru.privetdruk.l2jspace.gameserver.model.actor.Playable;
import ru.privetdruk.l2jspace.gameserver.model.actor.Summon;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.EffectPointInstance;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.model.actor.templates.NpcTemplate;
import ru.privetdruk.l2jspace.gameserver.model.skills.Env;
import ru.privetdruk.l2jspace.gameserver.model.skills.Formulas;
import ru.privetdruk.l2jspace.gameserver.model.skills.handlers.SkillSignetCasttime;
import ru.privetdruk.l2jspace.gameserver.network.SystemMessageId;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.MagicSkillLaunched;

/**
 * @author Shyla
 */
public class EffectSignetMDam extends Effect {
    private EffectPointInstance _actor;
    private boolean bss;
    private boolean sps;

    public EffectSignetMDam(Env env, EffectTemplate template) {
        super(env, template);
    }

    @Override
    public EffectType getEffectType() {
        return EffectType.SIGNET_GROUND;
    }

    @Override
    public void onStart() {
        NpcTemplate template;
        if (getSkill() instanceof SkillSignetCasttime) {
            template = NpcTable.getInstance().getTemplate(((SkillSignetCasttime) getSkill())._effectNpcId);
        } else {
            return;
        }

        final EffectPointInstance effectPoint = new EffectPointInstance(IdManager.getInstance().getNextId(), template, getEffector());
        effectPoint.getStatus().setCurrentHp(effectPoint.getMaxHp());
        effectPoint.getStatus().setCurrentMp(effectPoint.getMaxMp());

        World.getInstance().storeObject(effectPoint);

        int x = getEffector().getX();
        int y = getEffector().getY();
        int z = getEffector().getZ();
        if ((getEffector() instanceof PlayerInstance) && (getSkill().getTargetType() == Skill.SkillTargetType.TARGET_GROUND)) {
            final Location wordPosition = ((PlayerInstance) getEffector()).getCurrentSkillWorldPosition();
            if (wordPosition != null) {
                x = wordPosition.getX();
                y = wordPosition.getY();
                z = wordPosition.getZ();
            }
        }
        effectPoint.setInvul(true);
        effectPoint.spawnMe(x, y, z);
        _actor = effectPoint;
    }

    @Override
    public boolean onActionTime() {
        if (getCount() >= (getTotalCount() - 2)) {
            return true; // do nothing first 2 times
        }

        final int mpConsume = getSkill().getMpConsume();
        final PlayerInstance caster = (PlayerInstance) getEffector();
        sps = caster.checkSps();
        bss = caster.checkBss();

        final List<Creature> targets = new ArrayList<>();
        for (Creature creature : _actor.getKnownList().getKnownCharactersInRadius(getSkill().getSkillRadius())) {
            if ((creature == null) || (creature == caster)) {
                continue;
            }

            if ((creature instanceof Attackable) || (creature instanceof Playable)) {
                if (creature.isAlikeDead()) {
                    continue;
                }

                if (mpConsume > caster.getStatus().getCurrentMp()) {
                    caster.sendPacket(SystemMessageId.YOUR_SKILL_WAS_REMOVED_DUE_TO_A_LACK_OF_MP);
                    return false;
                }

                caster.reduceCurrentMp(mpConsume);

                if ((creature instanceof Playable) && (!(creature instanceof Summon) || (((Summon) creature).getOwner() != caster))) {
                    caster.updatePvPStatus(creature);
                }

                targets.add(creature);
            }
        }

        if (!targets.isEmpty()) {
            caster.broadcastPacket(new MagicSkillLaunched(caster, getSkill().getDisplayId(), getSkill().getLevel(), targets));
            for (Creature target : targets) {
                final boolean mcrit = Formulas.calcMCrit(caster.getMCriticalHit(target, getSkill()));
                final int mdam = (int) Formulas.calcMagicDam(caster, target, getSkill(), sps, bss, mcrit);
                if (target instanceof Summon) {
                    target.broadcastStatusUpdate();
                }

                if (mdam > 0) {
                    if (!target.isRaid() && Formulas.calcAtkBreak(target, mdam)) {
                        target.breakAttack();
                        target.breakCast();
                    }
                    caster.sendDamageMessage(target, mdam, mcrit, false, false);
                    target.reduceCurrentHp(mdam, caster);
                }
                target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, caster);
            }
        }

        return true;
    }

    @Override
    public void onExit() {
        if (_actor != null) {
            final PlayerInstance caster = (PlayerInstance) getEffector();

            // remove shots
            if (bss) {
                caster.removeBss();

            } else if (sps) {
                caster.removeSps();
            }
            _actor.deleteMe();
        }
    }
}

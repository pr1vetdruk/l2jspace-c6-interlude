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
package ru.privetdruk.l2jspace.gameserver.ai;

import ru.privetdruk.l2jspace.gameserver.model.Skill;
import ru.privetdruk.l2jspace.gameserver.model.WorldObject;
import ru.privetdruk.l2jspace.gameserver.model.actor.Creature;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.StaticObjectInstance;

import java.util.EmptyStackException;
import java.util.Stack;

import static ru.privetdruk.l2jspace.gameserver.ai.CtrlIntention.*;

public class PlayerAI extends CreatureAI {
    class IntentionCommand {
        protected CtrlIntention _crtlIntention;
        protected Object _arg0;
        protected Object _arg1;

        protected IntentionCommand(CtrlIntention pIntention, Object pArg0, Object pArg1) {
            _crtlIntention = pIntention;
            _arg0 = pArg0;
            _arg1 = pArg1;
        }
    }

    private final Stack<IntentionCommand> _interuptedIntentions = new Stack<>();

    private synchronized Stack<IntentionCommand> getInterruptedIntentions() {
        return _interuptedIntentions;
    }

    public PlayerAI(Creature.AIAccessor accessor) {
        super(accessor);
    }

    /**
     * Saves the current Intention for this PlayerAI if necessary and calls changeIntention in AbstractAI.
     *
     * @param intention The new Intention to set to the AI
     * @param arg0      The first parameter of the Intention
     * @param arg1      The second parameter of the Intention
     */
    @Override
    public void changeIntention(CtrlIntention intention, Object arg0, Object arg1) {
        // nothing to do if it does not CAST intention
        if (intention != AI_INTENTION_CAST) {
            super.changeIntention(intention, arg0, arg1);
            return;
        }

        final CtrlIntention oldIntention = getIntention();
        final Object oldArg0 = getIntentionArg0();
        final Object oldArg1 = getIntentionArg1();

        // do nothing if next intention is same as current one.
        if ((intention == oldIntention) && (arg0 == oldArg0) && (arg1 == oldArg1)) {
            super.changeIntention(intention, arg0, arg1);
            return;
        }

        // push current intention to stack
        getInterruptedIntentions().push(new IntentionCommand(oldIntention, oldArg0, oldArg1));
        super.changeIntention(intention, arg0, arg1);
    }

    /**
     * Finalize the casting of a skill. This method overrides CreatureAI method.<br>
     * <b>What it does:</b> Check if actual intention is set to CAST and, if so, retrieves latest intention before the actual CAST and set it as the current intention for the player
     */
    @Override
    protected void onEvtFinishCasting() {
        // forget interupted actions after offensive skill
        final Skill skill = getSkill();
        if ((skill != null) && skill.isOffensive()) {
            getInterruptedIntentions().clear();
        }

        if (getIntention() == AI_INTENTION_CAST) {
            // run interupted intention if it remain.
            if (!getInterruptedIntentions().isEmpty()) {
                IntentionCommand cmd = null;
                try {
                    cmd = getInterruptedIntentions().pop();
                } catch (EmptyStackException ese) {
                }

                if ((cmd != null) && (cmd._crtlIntention != AI_INTENTION_CAST)) // previous state shouldn't be casting
                {
                    setIntention(cmd._crtlIntention, cmd._arg0, cmd._arg1);
                } else {
                    setIntention(AI_INTENTION_IDLE);
                }
            } else {
                // set intention to idle if skill doesn't change intention.
                setIntention(AI_INTENTION_IDLE);
            }
        }
    }

    @Override
    protected void onIntentionRest() {
        if (getIntention() != AI_INTENTION_REST) {
            changeIntention(AI_INTENTION_REST, null, null);
            setTarget(null);

            if (getAttackTarget() != null) {
                setAttackTarget(null);
            }

            clientStopMoving(null);
        }
    }

    @Override
    protected void onIntentionActive() {
        setIntention(AI_INTENTION_IDLE);
    }

    @Override
    protected void clientNotifyDead() {
        _clientMovingToPawnOffset = 0;
        _clientMoving = false;
        super.clientNotifyDead();
    }

    private void thinkAttack() {
        final Creature target = getAttackTarget();
        if (target == null) {
            return;
        }

        if (checkTargetLostOrDead(target)) {
            // Notify the target
            setAttackTarget(null);
            return;
        }

        if (maybeMoveToPawn(target, _actor.getPhysicalAttackRange())) {
            return;
        }

        _accessor.doAttack(target);
    }

    private void thinkPickUp() {
        if (_actor.isAllSkillsDisabled()) {
            return;
        }

        final WorldObject target = getTarget();
        if (checkTargetLost(target)) {
            return;
        }

        if (maybeMoveToPawn(target, 36)) {
            return;
        }

        setIntention(AI_INTENTION_IDLE);
        ((PlayerInstance.AIAccessor) _accessor).doPickupItem(target);
    }

    private void thinkInteract() {
        if (_actor.isAllSkillsDisabled()) {
            return;
        }

        final WorldObject target = getTarget();
        if (checkTargetLost(target)) {
            return;
        }

        if (maybeMoveToPawn(target, 36)) {
            return;
        }

        if (!(target instanceof StaticObjectInstance)) {
            ((PlayerInstance.AIAccessor) _accessor).doInteract((Creature) target);
        }

        setIntention(AI_INTENTION_IDLE);
    }

    @Override
    public void onEvtThink() {
        if (isThinking() || _actor.isAllSkillsDisabled()) {
            return;
        }

        setThinking(true);

        try {
            if (getIntention() == AI_INTENTION_ATTACK) {
                thinkAttack();
            } else if (getIntention() == AI_INTENTION_CAST) {
                thinkCast();
            } else if (getIntention() == AI_INTENTION_PICK_UP) {
                thinkPickUp();
            } else if (getIntention() == AI_INTENTION_INTERACT) {
                thinkInteract();
            }
        } finally {
            setThinking(false);
        }
    }

    @Override
    protected void onEvtArrivedRevalidate() {
        if (_actor != null) {
            _actor.getKnownList().updateKnownObjects();
        }
        super.onEvtArrivedRevalidate();
    }
}

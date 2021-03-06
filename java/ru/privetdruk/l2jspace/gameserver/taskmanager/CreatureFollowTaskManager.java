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
package ru.privetdruk.l2jspace.gameserver.taskmanager;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import ru.privetdruk.l2jspace.commons.concurrent.ThreadPool;
import ru.privetdruk.l2jspace.commons.util.Rnd;
import ru.privetdruk.l2jspace.gameserver.ai.CreatureAI;
import ru.privetdruk.l2jspace.gameserver.model.WorldObject;
import ru.privetdruk.l2jspace.gameserver.model.actor.Creature;
import ru.privetdruk.l2jspace.gameserver.model.actor.Summon;
import ru.privetdruk.l2jspace.gameserver.ai.CtrlIntention;

/**
 * @author Mobius
 */
public class CreatureFollowTaskManager {
    private static final Map<Creature, Integer> NORMAL_FOLLOW_CREATURES = new ConcurrentHashMap<>();
    private static final Map<Creature, Integer> ATTACK_FOLLOW_CREATURES = new ConcurrentHashMap<>();
    private static boolean _workingNormal = false;
    private static boolean _workingAttack = false;

    public CreatureFollowTaskManager() {
        ThreadPool.scheduleAtFixedRate(() ->
        {
            if (_workingNormal) {
                return;
            }
            _workingNormal = true;

            for (Entry<Creature, Integer> entry : NORMAL_FOLLOW_CREATURES.entrySet()) {
                follow(entry.getKey(), entry.getValue().intValue());
            }

            _workingNormal = false;
        }, 1000, 1000);

        ThreadPool.scheduleAtFixedRate(() ->
        {
            if (_workingAttack) {
                return;
            }
            _workingAttack = true;

            for (Entry<Creature, Integer> entry : ATTACK_FOLLOW_CREATURES.entrySet()) {
                follow(entry.getKey(), entry.getValue().intValue());
            }

            _workingAttack = false;
        }, 500, 500);
    }

    private void follow(Creature creature, int range) {
        try {
            if (creature.hasAI()) {
                final CreatureAI ai = creature.getAI();
                if (ai != null) {
                    final WorldObject followTarget = ai.getFollowTarget();
                    if (followTarget == null) {
                        if (creature.isSummon()) {
                            ((Summon) creature).setFollowStatus(false);
                        }
                        ai.setIntention(CtrlIntention.AI_INTENTION_IDLE);
                        return;
                    }

                    final int followRange = range == -1 ? Rnd.get(50, 100) : range;
                    if (!creature.isInsideRadius(followTarget, followRange, true, false)) {
                        if (!creature.isInsideRadius(followTarget, 3000, true, false)) {
                            // If the target is too far (maybe also teleported).
                            if (creature.isSummon()) {
                                ((Summon) creature).setFollowStatus(false);
                            }
                            ai.setIntention(CtrlIntention.AI_INTENTION_IDLE);
                            return;
                        }
                        ai.moveToPawn(followTarget, followRange);
                    }
                } else {
                    remove(creature);
                }
            } else {
                remove(creature);
            }
        } catch (Exception e) {
            // Ignore.
        }
    }

    public boolean isFollowing(Creature creature) {
        return NORMAL_FOLLOW_CREATURES.containsKey(creature) || ATTACK_FOLLOW_CREATURES.containsKey(creature);
    }

    public void addNormalFollow(Creature creature, int range) {
        NORMAL_FOLLOW_CREATURES.putIfAbsent(creature, range);
        follow(creature, range);
    }

    public void addAttackFollow(Creature creature, int range) {
        ATTACK_FOLLOW_CREATURES.putIfAbsent(creature, range);
        follow(creature, range);
    }

    public void remove(Creature creature) {
        NORMAL_FOLLOW_CREATURES.remove(creature);
        ATTACK_FOLLOW_CREATURES.remove(creature);
    }

    public static CreatureFollowTaskManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static class SingletonHolder {
        protected static final CreatureFollowTaskManager INSTANCE = new CreatureFollowTaskManager();
    }
}

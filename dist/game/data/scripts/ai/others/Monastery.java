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
package ai.others;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ru.privetdruk.l2jspace.commons.util.Rnd;
import ru.privetdruk.l2jspace.gameserver.ai.CtrlIntention;
import ru.privetdruk.l2jspace.gameserver.datatables.SkillTable;
import ru.privetdruk.l2jspace.gameserver.enums.ChatType;
import ru.privetdruk.l2jspace.gameserver.model.Skill;
import ru.privetdruk.l2jspace.gameserver.model.Skill.SkillType;
import ru.privetdruk.l2jspace.gameserver.model.WorldObject;
import ru.privetdruk.l2jspace.gameserver.model.actor.Attackable;
import ru.privetdruk.l2jspace.gameserver.model.actor.Creature;
import ru.privetdruk.l2jspace.gameserver.model.actor.Playable;
import ru.privetdruk.l2jspace.gameserver.model.actor.Summon;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.NpcInstance;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PetInstance;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.model.quest.EventType;
import ru.privetdruk.l2jspace.gameserver.model.quest.Quest;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.CreatureSay;
import ru.privetdruk.l2jspace.gameserver.util.Util;

public class Monastery extends Quest {
    private static final int[] MOBS_1 =
            {
                    22124,
                    22125,
                    22126,
                    22127,
                    22129
            };
    private static final int[] MOBS_2 =
            {
                    22134,
                    22135
            };
    private static final String[] TEXT =
            {
                    "You cannot carry a weapon without authorization!",
                    "name, why would you choose the path of darkness?!",
                    "name! How dare you defy the will of Einhasad!"
            };

    public Monastery() {
        super(-1, "ai");
        registerMobs(MOBS_1, EventType.ON_AGGRO_RANGE_ENTER, EventType.ON_SPAWN, EventType.ON_SPELL_FINISHED);
        registerMobs(MOBS_2, EventType.ON_SPELL_FINISHED);
    }

    @Override
    public String onAggroRangeEnter(NpcInstance npc, PlayerInstance player, boolean isPet) {
        if (Util.contains(MOBS_1, npc.getNpcId()) && !npc.isInCombat() && (npc.getTarget() == null)) {
            if ((player.getActiveWeaponInstance() != null) && !player.isSilentMoving()) {
                npc.setTarget(player);
                npc.broadcastPacket(new CreatureSay(npc.getObjectId(), ChatType.GENERAL, npc.getName(), TEXT[0]));

                switch (npc.getNpcId()) {
                    case 22124:
                    case 22126: {
                        final Skill skill = SkillTable.getInstance().getSkill(4589, 8);
                        npc.doCast(skill);
                        break;
                    }
                    default: {
                        npc.setRunning(true);
                        ((Attackable) npc).addDamageHate(player, 0, 999);
                        npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
                        break;
                    }
                }
            } else if (((Attackable) npc).getMostHated() == null) {
                return null;
            }
        }
        return super.onAggroRangeEnter(npc, player, isPet);
    }

    @Override
    public String onSpawn(NpcInstance npc) {
        if (Util.contains(MOBS_1, npc.getNpcId())) {
            final List<Playable> result = new ArrayList<>();
            final Collection<WorldObject> objs = npc.getKnownList().getKnownObjects().values();
            for (WorldObject obj : objs) {
                if (((obj instanceof PlayerInstance) || (obj instanceof PetInstance)) && Util.checkIfInRange(npc.getAggroRange(), npc, obj, true) && !((Creature) obj).isDead()) {
                    result.add((Playable) obj);
                }
            }
            if (!result.isEmpty()) {
                final Object[] characters = result.toArray();
                for (Object obj : characters) {
                    final Playable target = (Playable) (obj instanceof PlayerInstance ? obj : ((Summon) obj).getOwner());
                    if ((target.getActiveWeaponInstance() == null) || ((target instanceof PlayerInstance) && ((PlayerInstance) target).isSilentMoving()) || ((target instanceof Summon) && ((Summon) target).getOwner().isSilentMoving())) {
                        continue;
                    }

                    if ((target.getActiveWeaponInstance() != null) && !npc.isInCombat() && (npc.getTarget() == null)) {
                        npc.setTarget(target);
                        npc.broadcastPacket(new CreatureSay(npc.getObjectId(), ChatType.GENERAL, npc.getName(), TEXT[0]));
                        switch (npc.getNpcId()) {
                            case 22124:
                            case 22126:
                            case 22127: {
                                final Skill skill = SkillTable.getInstance().getSkill(4589, 8);
                                npc.doCast(skill);
                                break;
                            }
                            default: {
                                npc.setRunning(true);
                                ((Attackable) npc).addDamageHate(target, 0, 999);
                                npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
                                break;
                            }
                        }
                    }
                }
            }
        }

        return super.onSpawn(npc);
    }

    @Override
    public String onSpellFinished(NpcInstance npc, PlayerInstance player, Skill skill) {
        if (Util.contains(MOBS_1, npc.getNpcId()) && (skill.getId() == 4589)) {
            npc.setRunning(true);
            ((Attackable) npc).addDamageHate(player, 0, 999);
            npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
        }

        if (Util.contains(MOBS_2, npc.getNpcId()) && (skill.getSkillType() == SkillType.AGGDAMAGE)) {
            npc.broadcastPacket(new CreatureSay(npc.getObjectId(), ChatType.GENERAL, npc.getName(), TEXT[Rnd.get(2) + 1].replace("name", player.getName())));
            ((Attackable) npc).addDamageHate(player, 0, 999);
            npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
        }

        return super.onSpellFinished(npc, player, skill);
    }

    public static void main(String[] args) {
        new Monastery();
    }
}
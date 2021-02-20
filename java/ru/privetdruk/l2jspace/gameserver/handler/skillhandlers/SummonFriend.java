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
import ru.privetdruk.l2jspace.gameserver.instancemanager.GrandBossManager;
import ru.privetdruk.l2jspace.gameserver.model.Skill;
import ru.privetdruk.l2jspace.gameserver.model.WorldObject;
import ru.privetdruk.l2jspace.gameserver.model.actor.Creature;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.model.entity.event.CTF;
import ru.privetdruk.l2jspace.gameserver.model.entity.event.DM;
import ru.privetdruk.l2jspace.gameserver.model.entity.event.TvT;
import ru.privetdruk.l2jspace.gameserver.model.entity.event.VIP;
import ru.privetdruk.l2jspace.gameserver.model.zone.ZoneId;
import ru.privetdruk.l2jspace.gameserver.network.SystemMessageId;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.ConfirmDlg;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.SystemMessage;
import ru.privetdruk.l2jspace.gameserver.util.Util;

public class SummonFriend implements ISkillHandler {
    private static final Skill.SkillType[] SKILL_IDS =
            {
                    Skill.SkillType.SUMMON_FRIEND
            };

    @Override
    public void useSkill(Creature creature, Skill skill, List<Creature> targets) {
        if (!(creature instanceof PlayerInstance)) {
            return;
        }

        final PlayerInstance activePlayer = creature.getActingPlayer();
        if (!PlayerInstance.checkSummonerStatus(activePlayer)) {
            return;
        }

        if (activePlayer.isInOlympiadMode()) {
            activePlayer.sendPacket(SystemMessageId.YOU_CANNOT_USE_THAT_ITEM_IN_A_GRAND_OLYMPIAD_GAMES_MATCH);
            return;
        }

        if (activePlayer._inEvent) {
            activePlayer.sendMessage("You cannot use this skill in an Event.");
            return;
        }

        if (activePlayer._inEventCTF && CTF.isStarted()) {
            activePlayer.sendMessage("You cannot use this skill in an Event.");
            return;
        }

        if (activePlayer._inEventDM && DM.hasStarted()) {
            activePlayer.sendMessage("You cannot use this skill in an Event.");
            return;
        }

        if (activePlayer._inEventTvT && TvT.isStarted()) {
            activePlayer.sendMessage("You cannot use this skill in an Event.");
            return;
        }

        if (activePlayer._inEventVIP && VIP._started) {
            activePlayer.sendMessage("You cannot use this skill in an Event.");
            return;
        }

        // Checks summoner not in siege zone
        if (activePlayer.isInsideZone(ZoneId.SIEGE)) {
            activePlayer.sendMessage("You cannot summon in a siege zone.");
            return;
        }

        // Checks summoner not in arenas, siege zones, jail
        if (activePlayer.isInsideZone(ZoneId.PVP)) {
            activePlayer.sendPacket(SystemMessageId.YOU_CANNOT_SUMMON_DURING_COMBAT);
            return;
        }

        if ((GrandBossManager.getInstance().getZone(activePlayer) != null) && !activePlayer.isGM()) {
            activePlayer.sendPacket(SystemMessageId.YOU_MAY_NOT_SUMMON_FROM_YOUR_CURRENT_LOCATION);
            return;
        }

        try {
            for (WorldObject wo : targets) {
                if (!(wo instanceof Creature)) {
                    continue;
                }

                final Creature target = (Creature) wo;
                if (creature == target) {
                    continue;
                }

                if (target instanceof PlayerInstance) {
                    final PlayerInstance targetPlayer = target.getActingPlayer();
                    if (!PlayerInstance.checkSummonTargetStatus(targetPlayer, activePlayer)) {
                        continue;
                    }

                    if (targetPlayer.isAlikeDead()) {
                        final SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_DEAD_AT_THE_MOMENT_AND_CANNOT_BE_SUMMONED);
                        sm.addString(targetPlayer.getName());
                        activePlayer.sendPacket(sm);
                        continue;
                    }

                    if (targetPlayer._inEvent) {
                        targetPlayer.sendMessage("You cannot use this skill in an Event.");
                        return;
                    }
                    if (targetPlayer._inEventCTF) {
                        targetPlayer.sendMessage("You cannot use this skill in an Event.");
                        return;
                    }
                    if (targetPlayer._inEventDM) {
                        targetPlayer.sendMessage("You cannot use this skill in an Event.");
                        return;
                    }
                    if (targetPlayer._inEventTvT) {
                        targetPlayer.sendMessage("You cannot use this skill in an Event.");
                        return;
                    }
                    if (targetPlayer._inEventVIP) {
                        targetPlayer.sendMessage("You cannot use this skill in an Event.");
                        return;
                    }

                    if (targetPlayer.isInStoreMode()) {
                        final SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_CURRENTLY_TRADING_OR_OPERATING_A_PRIVATE_STORE_AND_CANNOT_BE_SUMMONED);
                        sm.addString(targetPlayer.getName());
                        activePlayer.sendPacket(sm);
                        continue;
                    }

                    // Target cannot be in combat (or dead, but that's checked by TARGET_PARTY)
                    if (targetPlayer.isRooted() || targetPlayer.isInCombat()) {
                        final SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_ENGAGED_IN_COMBAT_AND_CANNOT_BE_SUMMONED);
                        sm.addString(targetPlayer.getName());
                        activePlayer.sendPacket(sm);
                        continue;
                    }

                    if ((GrandBossManager.getInstance().getZone(targetPlayer) != null) && !targetPlayer.isGM()) {
                        activePlayer.sendPacket(new SystemMessage(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING));
                        continue;
                    }
                    // Check for the the target's festival status
                    if (targetPlayer.isInOlympiadMode()) {
                        activePlayer.sendPacket(new SystemMessage(SystemMessageId.YOU_CANNOT_SUMMON_PLAYERS_WHO_ARE_CURRENTLY_PARTICIPATING_IN_THE_GRAND_OLYMPIAD));
                        continue;
                    }

                    // Check for the the target's festival status
                    if (targetPlayer.isFestivalParticipant()) {
                        activePlayer.sendPacket(new SystemMessage(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING));
                        continue;
                    }

                    // Check for the target's jail status, arenas and siege zones
                    if (targetPlayer.isInsideZone(ZoneId.PVP)) {
                        activePlayer.sendPacket(new SystemMessage(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING));
                        continue;
                    }

                    // Requires a Summoning Crystal
                    if ((targetPlayer.getInventory().getItemByItemId(8615) == null) && (skill.getId() != 1429)) // KidZor
                    {
                        activePlayer.sendMessage("Your target cannot be summoned while he hasn't got a Summoning Crystal.");
                        targetPlayer.sendMessage("You cannot be summoned while you haven't got a Summoning Crystal.");
                        continue;
                    }

                    if (!Util.checkIfInRange(0, activePlayer, targetPlayer, false)) {
                        // Check already summon
                        if (!targetPlayer.teleportRequest(activePlayer, skill)) {
                            final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_ALREADY_BEEN_SUMMONED);
                            sm.addString(targetPlayer.getName());
                            activePlayer.sendPacket(sm);
                            continue;
                        }

                        // Summon friend
                        if (skill.getId() == 1403) {
                            // Send message
                            final ConfirmDlg confirm = new ConfirmDlg(SystemMessageId.S1_WISHES_TO_SUMMON_YOU_FROM_S2_DO_YOU_ACCEPT.getId());
                            confirm.addString(activePlayer.getName());
                            confirm.addZoneName(activePlayer.getX(), activePlayer.getY(), activePlayer.getZ());
                            confirm.addTime(30000);
                            confirm.addRequesterId(activePlayer.getObjectId());
                            targetPlayer.sendPacket(confirm);
                        } else {
                            PlayerInstance.teleToTarget(targetPlayer, activePlayer, activePlayer.getLocation(), skill);
                            targetPlayer.teleportRequest(activePlayer, skill);
                        }
                    }
                }
            }
        } catch (Throwable e) {
        }
    }

    @Override
    public Skill.SkillType[] getSkillIds() {
        return SKILL_IDS;
    }
}
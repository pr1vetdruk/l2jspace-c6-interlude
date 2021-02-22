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
package ru.privetdruk.l2jspace.gameserver.model.entity.event.core;

import ru.privetdruk.l2jspace.Config;
import ru.privetdruk.l2jspace.commons.concurrent.ThreadPool;
import ru.privetdruk.l2jspace.commons.util.Chronos;
import ru.privetdruk.l2jspace.gameserver.datatables.sql.NpcTable;
import ru.privetdruk.l2jspace.gameserver.datatables.sql.SpawnTable;
import ru.privetdruk.l2jspace.gameserver.instancemanager.CastleManager;
import ru.privetdruk.l2jspace.gameserver.model.Location;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.model.actor.templates.NpcTemplate;
import ru.privetdruk.l2jspace.gameserver.model.base.Npc;
import ru.privetdruk.l2jspace.gameserver.model.entity.Announcements;
import ru.privetdruk.l2jspace.gameserver.model.entity.event.core.manager.EventTask;
import ru.privetdruk.l2jspace.gameserver.model.entity.olympiad.Olympiad;
import ru.privetdruk.l2jspace.gameserver.model.entity.siege.Castle;
import ru.privetdruk.l2jspace.gameserver.model.skills.Skill;
import ru.privetdruk.l2jspace.gameserver.model.spawn.Spawn;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.MagicSkillUse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static ru.privetdruk.l2jspace.gameserver.model.entity.event.core.State.*;

public abstract class EventEngine implements EventTask {
    protected static final Logger LOGGER = Logger.getLogger(EventTask.class.getName());

    protected GeneralSetting generalSetting = new GeneralSetting();
    protected final List<PlayerInstance> players = Collections.synchronizedList(new ArrayList<>());
    protected Event eventType;
    protected volatile State eventState;
    protected EventBorder eventBorder;
    protected String eventStartTime;

    protected boolean preLaunchChecks() {
        if (eventState != INACTIVE) {
            return false;
        }

        if (generalSetting.getTimeRegistration() <= 0) {
            return false;
        }

        if (eventType.isTeam() && !preLaunchTeamPlayEventChecks()) {
            return false;
        } else if (!preLaunchSinglePlayerEventChecks()) {
            return false;
        }

        if (!Config.ALLOW_EVENTS_DURING_OLY && Olympiad.getInstance().inCompPeriod()) {
            return false;
        }

        for (Castle castle : CastleManager.getInstance().getCastles()) {
            if ((castle != null) && (castle.getSiege() != null) && castle.getSiege().isInProgress()) {
                return false;
            }
        }

        return customPreLaunchChecks();
    }

    protected void spawnMainEventNpc(Event event) {
        Npc npc = generalSetting.getMainNpc();

        final NpcTemplate npcTemplate = NpcTable.getInstance().getTemplate(npc.getId());

        try {
            Spawn npcSpawn = new Spawn(npcTemplate);

            npcSpawn.setX(npc.getSpawnPosition().getX());
            npcSpawn.setY(npc.getSpawnPosition().getY());
            npcSpawn.setZ(npc.getSpawnPosition().getZ());
            npcSpawn.setAmount(1);
            npcSpawn.setHeading(npc.getSpawnPosition().getHeading());
            npcSpawn.setRespawnDelay(1);

            SpawnTable.getInstance().addNewSpawn(npcSpawn, false);
            npcSpawn.init();
            npcSpawn.getLastSpawn().getStatus().setCurrentHp(999999999);
            npcSpawn.getLastSpawn().setTitle(generalSetting.getEventName());
            npcSpawn.getLastSpawn().isAggressive();
            npcSpawn.getLastSpawn().decayMe();
            npcSpawn.getLastSpawn().spawnMe(
                    npcSpawn.getLastSpawn().getX(),
                    npcSpawn.getLastSpawn().getY(),
                    npcSpawn.getLastSpawn().getZ()
            );
            npcSpawn.getLastSpawn().broadcastPacket(
                    new MagicSkillUse(
                            npcSpawn.getLastSpawn(),
                            npcSpawn.getLastSpawn(),
                            Skill.Bishop.REPOSE.getId(),
                            1,
                            1,
                            1
                    )
            );

            switch (event) {
                case CTF -> npcSpawn.getLastSpawn().isCtfMainNpc = true;
                case TVT -> npcSpawn.getLastSpawn()._isEventMobTvT = true;
                case DM -> npcSpawn.getLastSpawn()._isEventMobDM = true;
            }

            generalSetting.setSpawnMainNpc(npcSpawn);
        } catch (Exception e) {
            LOGGER.warning(generalSetting.getEventName() + " spawnMainEventNpc() exception: " + e.getMessage());
        }
    }

    protected void sitPlayer() {
        for (PlayerInstance player : players) {
            if (player != null) {
                if (player.isSitting()) {
                    player.standUp();
                } else {
                    player.stopMove(null, false);
                    player.abortAttack();
                    player.abortCast();
                    player.sitDown();
                }
            }
        }
    }

    protected abstract void customUnspawnEventNpc();

    protected void unspawnEventNpc() {
        customUnspawnEventNpc();

        Spawn spawnMainNpc = generalSetting.getSpawnMainNpc();

        if (spawnMainNpc == null || spawnMainNpc.getLastSpawn() == null) {
            return;
        }

        spawnMainNpc.getLastSpawn().deleteMe();
        spawnMainNpc.stopRespawn();

        SpawnTable.getInstance().deleteSpawn(spawnMainNpc, true);
    }

    protected void abortEvent() {
        unspawnEventNpc();
        restorePlayerData();

        if (eventState != REGISTRATION) {
            customAbortEvent();
            returnPlayer();
        }

        eventState = ABORT;

        Announcements.getInstance().criticalAnnounceToAll(generalSetting.getEventName() + ": Match aborted!");
    }

    protected void returnPlayer() {
        Announcements.getInstance().criticalAnnounceToAll(generalSetting.getEventName() + ": Teleport back to participation NPC in 20 seconds!");

        sitPlayer();

        ThreadPool.schedule(() -> {
            // TODO Реализовать возврат на исходную позицию перед эвентом.
            Location spawnPosition = generalSetting.getMainNpc().getSpawnPosition();

            for (PlayerInstance player : players) {
                if (player != null) {
                    if (player.isOnline()) {
                        player.teleToLocation(
                                spawnPosition.getX(),
                                spawnPosition.getY(),
                                spawnPosition.getZ(),
                                false
                        );
                    }
                }
            }

            restorePlayerData();
            sitPlayer();
        }, 20000);
    }

    protected void waiter(int intervalMinutes) {
        long interval = TimeUnit.MINUTES.toMillis(intervalMinutes);
        final long startWaiterTime = Chronos.currentTimeMillis();
        int seconds = (int) (interval / 1000);

        String eventName = generalSetting.getEventName();
        String registrationLocationName = generalSetting.getRegistrationLocationName();

        while (((startWaiterTime + interval) > Chronos.currentTimeMillis()) && eventState != ABORT) {
            seconds--; // Here because we don't want to see two time announce at the same time

            if (eventState == REGISTRATION || eventState == START || eventState == TELEPORTATION) {
                switch (seconds) {
                    case 3600: // 1 hour left
                    case 1800: // 30 minutes left
                    case 900: // 15 minutes left
                    case 600: // 10 minutes left
                    case 300: // 5 minutes left
                    case 240: // 4 minutes left
                    case 180: // 3 minutes left
                    case 120: // 2 minutes left
                    case 60: { // 1 minute left
                        if (seconds == 3600) {
                            removeOfflinePlayers();
                        }

                        if (eventState == REGISTRATION) {
                            Announcements.getInstance().criticalAnnounceToAll(eventName + ": Registration in " + registrationLocationName + "!");
                            Announcements.getInstance().criticalAnnounceToAll(eventName + ": " + (seconds / 60) + " minute(s) till registration close!");
                        } else if (eventState == START) {
                            Announcements.getInstance().criticalAnnounceToAll(eventName + ": " + (seconds / 60) + " minute(s) till event finish!");
                        }

                        break;
                    }
                    case 30: // 30 seconds left
                    case 15: // 15 seconds left
                    case 10: { // 10 seconds left
                        removeOfflinePlayers();
                        // fallthrou?
                    }
                    case 3: // 3 seconds left
                    case 2: // 2 seconds left
                    case 1: { // 1 seconds left
                        if (eventState == REGISTRATION) {
                            Announcements.getInstance().criticalAnnounceToAll(eventName + ": " + seconds + " second(s) till registration close!");
                        } else if (eventState == TELEPORTATION) {
                            Announcements.getInstance().criticalAnnounceToAll(eventName + ": " + seconds + " seconds(s) till start fight!");
                        } else if (eventState == START) {
                            Announcements.getInstance().criticalAnnounceToAll(eventName + ": " + seconds + " second(s) till event finish!");
                        }
                        break;
                    }
                }
            }

            long startOneSecondWaiterStartTime = Chronos.currentTimeMillis();

            // TODO Какая-то печаль, нужно в будущем разобраться.
            // Only the try catch with Thread.sleep(1000) give bad countdown on high wait times
            while ((startOneSecondWaiterStartTime + 1000) > Chronos.currentTimeMillis()) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    protected int getIntervalBetweenMatches() {
        final long actualTime = Chronos.currentTimeMillis();
        final long totalTime = actualTime + generalSetting.getIntervalBetweenMatches();
        final long interval = totalTime - actualTime;
        final int seconds = (int) (interval / 1000);
        return seconds / 60;
    }

    @Override
    public String getEventIdentifier() {
        return generalSetting.getEventName();
    }

    protected boolean checkMinPlayers() {
        return generalSetting.getMinPlayers() <= players.size();
    }

    public boolean checkMaxPlayers() {
        return generalSetting.getMaxPlayers() > players.size();
    }

    public boolean checkMaxLevel(int maxLevel) {
        return generalSetting.getMinLevel() < maxLevel;
    }

    public boolean checkMinLevel(int minLevel) {
        return generalSetting.getMaxLevel() > minLevel;
    }

    public String getEventStartTime() {
        return eventStartTime;
    }

    public void setEventStartTime(String eventStartTime) {
        this.eventStartTime = eventStartTime;
    }

    public GeneralSetting getGeneralSetting() {
        return generalSetting;
    }

    public State getEventState() {
        return eventState;
    }

    public List<PlayerInstance> getPlayers() {
        return players;
    }

    protected abstract void registerPlayer(PlayerInstance player, String teamName);

    protected abstract boolean customPreLaunchChecks();

    protected abstract boolean preLaunchSinglePlayerEventChecks();

    protected abstract boolean preLaunchTeamPlayEventChecks();

    protected abstract void startEvent();

    protected abstract void removeOfflinePlayers();

    protected abstract boolean teleportPlayer();

    protected abstract void startRegistrationPlayer();

    public abstract void loadData(int eventId);

    protected abstract void restorePlayerData();

    protected abstract void customAbortEvent();

    protected abstract void finishEvent();

    /*protected class AutoEventTask implements Runnable {
        @Override
        public void run() {
            String name = generalSetting.getEventName();

            LOGGER.info("Starting " + name + "!");
            LOGGER.info("Matchs Are Restarted At Every: " + getIntervalBetweenMatches() + " Minutes.");

            if (checkAutoEventStartJoinOk() && startRegistrationPlayer() && !eventStates.contains(ABORT)) {
                if (generalSetting.getTimeRegistration() > 0) {
                    waiter((long) generalSetting.getTimeRegistration() * 60 * 1000); // minutes for join event
                } else {
                    LOGGER.info(name + ": join time <= 0 aborting event.");

                    abortEvent();

                    return;
                }

                if (teleportPlayer() && !eventStates.contains(ABORT)) {
                    waiter(TimeUnit.SECONDS.toMillis(30)); // 30 sec wait time untill start fight after teleported

                    if (startEvent() && !eventStates.contains(ABORT)) {
                        LOGGER.warning(name + ": waiting.....minutes for event time " + generalSetting.getDurationEvent());

                        waiter(TimeUnit.MINUTES.toMillis(generalSetting.getDurationEvent()));

                        finishEvent();

                        LOGGER.info(name + ": waiting... delay for final messages ");
                        waiter(60000); // just a give a delay delay for final messages
                        sendFinalMessages();

                        if (!eventStates.containsAll(EnumSet.of(START, ABORT))) { // if is not already started and it's not aborted

                            LOGGER.info(name + ": waiting.....delay for restart event  " + generalSetting.getIntervalBetweenMatches() + " minutes.");
                            waiter(60000); // just a give a delay to next restart

                            try {
                                if (!eventStates.contains(ABORT)) {
                                    restartEvent();
                                }
                            } catch (Exception e) {
                                LOGGER.warning("Error while tying to Restart Event " + e);
                            }
                        }
                    }
                } else if (!eventStates.contains(ABORT)) {
                    abortEvent();
                    restartEvent();
                }
            }
        }

    }

    protected void sendFinalMessages() {
        if (!eventStates.containsAll(EnumSet.of(START, ABORT))) {
            Announcements.getInstance().criticalAnnounceToAll("Thank you For Participating At, " + generalSetting.getEventName() + " Event.");
        }
    }

    protected void autoEvent() {
        ThreadPool.execute(new AutoEventTask());
    }

      public synchronized void restartEvent() {
        String name = generalSetting.getEventName();

        LOGGER.info(name + ": Event has been restarted...");

        eventStates.removeAll(EnumSet.of(REGISTRATION, START, IN_PROGRESS, ABORT));

        final long delay = generalSetting.getIntervalBetweenMatches();

        Announcements.getInstance().criticalAnnounceToAll(name + ": joining period will be avaible again in " + delay + " minute(s)!");

        waiter(delay);

        try {
            if (!eventStates.contains(ABORT)) {
                autoEvent(); // start a new event
            } else {
                Announcements.getInstance().criticalAnnounceToAll(name + ": next event aborted!");
            }
        } catch (Exception e) {
            LOGGER.warning(name + ": Error While Trying to restart Event... " + e);
        }
    }*/
}

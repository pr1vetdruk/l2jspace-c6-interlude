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
package ru.privetdruk.l2jspace.gameserver.model.entity.event.ctf;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import ru.privetdruk.l2jspace.Config;
import ru.privetdruk.l2jspace.commons.concurrent.ThreadPool;
import ru.privetdruk.l2jspace.commons.database.DatabaseFactory;
import ru.privetdruk.l2jspace.commons.util.Rnd;
import ru.privetdruk.l2jspace.gameserver.builder.EventGeneralSettingBuilder;
import ru.privetdruk.l2jspace.gameserver.datatables.ItemTable;
import ru.privetdruk.l2jspace.gameserver.datatables.SkillTable;
import ru.privetdruk.l2jspace.gameserver.datatables.sql.NpcTable;
import ru.privetdruk.l2jspace.gameserver.datatables.sql.SpawnTable;
import ru.privetdruk.l2jspace.gameserver.enums.ChatType;
import ru.privetdruk.l2jspace.gameserver.model.Effect;
import ru.privetdruk.l2jspace.gameserver.model.Location;
import ru.privetdruk.l2jspace.gameserver.model.Radar;
import ru.privetdruk.l2jspace.gameserver.model.World;
import ru.privetdruk.l2jspace.gameserver.model.actor.Summon;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.NpcInstance;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PetInstance;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.model.actor.templates.NpcTemplate;
import ru.privetdruk.l2jspace.gameserver.model.base.Npc;
import ru.privetdruk.l2jspace.gameserver.model.base.RewardItem;
import ru.privetdruk.l2jspace.gameserver.model.entity.Announcements;
import ru.privetdruk.l2jspace.gameserver.model.entity.event.core.Event;
import ru.privetdruk.l2jspace.gameserver.model.entity.event.core.EventBorder;
import ru.privetdruk.l2jspace.gameserver.model.entity.event.core.GeneralSetting;
import ru.privetdruk.l2jspace.gameserver.model.entity.event.core.State;
import ru.privetdruk.l2jspace.gameserver.model.entity.event.core.TeamSetting;
import ru.privetdruk.l2jspace.gameserver.model.entity.event.core.EventEngine;
import ru.privetdruk.l2jspace.gameserver.model.entity.olympiad.Olympiad;
import ru.privetdruk.l2jspace.gameserver.model.itemcontainer.Inventory;
import ru.privetdruk.l2jspace.gameserver.model.items.Item;
import ru.privetdruk.l2jspace.gameserver.model.items.instance.ItemInstance;
import ru.privetdruk.l2jspace.gameserver.model.spawn.Spawn;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.ActionFailed;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.CreatureSay;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.InventoryUpdate;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.ItemList;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.MagicSkillUse;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.NpcHtmlMessage;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.RadarControl;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.Ride;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.SocialAction;

import static ru.privetdruk.l2jspace.Config.CTF_EVEN_TEAMS;
import static ru.privetdruk.l2jspace.gameserver.model.entity.event.core.State.ERROR;
import static ru.privetdruk.l2jspace.gameserver.model.entity.event.core.State.FINISH;
import static ru.privetdruk.l2jspace.gameserver.model.entity.event.core.State.INACTIVE;
import static ru.privetdruk.l2jspace.gameserver.model.entity.event.core.State.REGISTRATION;
import static ru.privetdruk.l2jspace.gameserver.model.entity.event.core.State.START;
import static ru.privetdruk.l2jspace.gameserver.model.entity.event.core.State.TELEPORTATION;
import static ru.privetdruk.l2jspace.gameserver.model.entity.event.ctf.CtfMode.BALANCE;
import static ru.privetdruk.l2jspace.gameserver.model.entity.event.ctf.CtfMode.NO;
import static ru.privetdruk.l2jspace.gameserver.model.entity.event.ctf.CtfMode.SHUFFLE;

public class CTF extends EventEngine {
    private static final Logger LOGGER = Logger.getLogger(CTF.class.getName());
    private static final List<CTF> eventTaskList = new ArrayList<>();

    private final List<CtfTeamSetting> teamSetting;
    private final CtfMode EVENT_MODE;
    private final List<String> savedPlayerNames = new ArrayList<>();
    private final List<String> savedTeamNamesPlayers = new ArrayList<>();

    public CTF() {
        eventType = Event.CTF;
        eventState = INACTIVE;

        teamSetting = new ArrayList<>();

        if (SHUFFLE.name().equals(CTF_EVEN_TEAMS)) {
            EVENT_MODE = SHUFFLE;
        } else if (BALANCE.name().equals(CTF_EVEN_TEAMS)) {
            EVENT_MODE = BALANCE;
        } else {
            EVENT_MODE = NO;
        }

        eventTaskList.add(this);
    }

    @Override
    public void run() {
        players.clear();
        savedPlayerNames.clear();
        savedTeamNamesPlayers.clear();

        LOGGER.info(generalSetting.getEventName() + ": Event notification start");

        if (!preLaunchChecks()) {
            LOGGER.info(generalSetting.getEventName() + ": The event was canceled because has already been launched before.");
            return;
        }

        try {
            startRegistrationPlayer();

            if (teleportPlayer()) {
                waiter(1);

                startEvent();

                waiter(generalSetting.getDurationEvent());

                finishEvent();
            } else {
                abortEvent();
            }
        } catch (Exception e) {
            LOGGER.severe("CTF.run() exception: " + e.getMessage());
        } finally {
            eventState = INACTIVE;
        }
    }

    private List<String> determineWinner() {
        String eventName = generalSetting.getEventName();

        Map.Entry<Integer, List<CtfTeamSetting>> winningTeamEntry = teamSetting.stream()
                .filter(team -> team.getPoints() > 0)
                .collect(
                        Collectors.groupingBy(
                                CtfTeamSetting::getPoints,
                                TreeMap::new,
                                Collectors.toList()
                        )
                ).lastEntry();

        List<String> winningNameList = new ArrayList<>();

        if (winningTeamEntry != null) {
            List<CtfTeamSetting> winningTeamList = winningTeamEntry.getValue();

            winningNameList = winningTeamList.stream()
                    .map(TeamSetting::getName)
                    .collect(Collectors.toList());

            playAnimation(winningNameList);

            if (Config.CTF_ANNOUNCE_TEAM_STATS) {
                Announcements.getInstance().criticalAnnounceToAll(eventName + " Team Statistics:");
                for (CtfTeamSetting team : teamSetting) {
                    Announcements.getInstance().criticalAnnounceToAll(eventName + ": Team: " + team.getName() + " - Flags taken: " + team.getPoints());
                }
            }

            CtfTeamSetting winningTeam = winningTeamList.get(0);

            if (winningTeamList.size() == 1) {
                Announcements.getInstance().criticalAnnounceToAll(eventName + ": Team " + winningTeam.getName() + " wins the match, with " + winningTeam.getPoints() + " flags taken!");
            } else {
                Announcements.getInstance().criticalAnnounceToAll(eventName + ": The event finished with a TIE: " + winningTeam.getPoints() + " flags taken by each team!");
            }

            if (Config.CTF_STATS_LOGGER) {
                LOGGER.info("**** " + eventName + " ****");
                LOGGER.info(eventName + " Team Statistics:");

                for (CtfTeamSetting team : teamSetting) {
                    LOGGER.info("Team: " + team.getName() + " - Flags taken: " + team.getPoints());
                }

                LOGGER.info(eventName + ": Team " + winningTeam.getName() + " wins the match, with " + winningTeam.getPoints() + " flags taken!");
            }
        } else {
            Announcements.getInstance().criticalAnnounceToAll(eventName + ": The event finished with a TIE: No team wins the match(nobody took flags)!");

            if (Config.CTF_STATS_LOGGER) {
                LOGGER.info(eventName + ": No team win the match(nobody took flags).");
            }
        }

        return winningNameList;
    }

    @Override
    protected void finishEvent() {
        if (eventState != START) {
            return;
        }

        eventState = FINISH;

        unspawnEventNpc();
        List<String> listWinners = determineWinner();
        giveRewardTeam(listWinners);

        returnPlayer();
    }

    private void spawnAllFlags() {
        for (CtfTeamSetting team : teamSetting) {
            Throne throne = team.getThrone();
            NpcTemplate throneTemplate = NpcTable.getInstance().getTemplate(throne.getNpc().getId());

            Flag flag = team.getFlag();
            NpcTemplate flagTemplate = NpcTable.getInstance().getTemplate(flag.getNpc().getId());

            try {
                Spawn throneSpawn = configureSpawn(throneTemplate, throne.getNpc().getSpawnPosition(), team.getName() + " Throne");
                throneSpawn.setZ(throneSpawn.getZ() - throne.getOffsetZ());
                NpcInstance throneLastSpawn = throneSpawn.getLastSpawn();
                throneLastSpawn.broadcastPacket(new MagicSkillUse(throneLastSpawn, throneLastSpawn, 1036, 1, 5500, 1)); // TODO скилы
                throneLastSpawn.isCtfThroneNpc = true;
                throne.setSpawn(throneSpawn);

                Spawn flagSpawn = configureSpawn(flagTemplate, flag.getNpc().getSpawnPosition(), team.getName() + "'s Flag");
                flagSpawn.getLastSpawn().ctfFlagTeamName = team.getName();
                flagSpawn.getLastSpawn().isCtfFlagNpc = true;
                flag.setSpawn(flagSpawn);

                calculateOutSideOfCTF(); // Sets event boundaries so players don't run with the flag.
            } catch (Exception e) {
                LOGGER.severe("CTF Engine[spawnAllFlags()]: exception: " + e);
            }
        }
    }

    /**
     * Used to calculate the event CTF area, so that players don't run off with the flag.
     * Essential, since a player may take the flag just so other teams can't score points.
     * This function is Only called upon ONE time on BEGINING OF EACH EVENT right after we spawn the flags.
     */
    private void calculateOutSideOfCTF() {
        if (eventBorder != null) {
            return;
        }

        eventBorder = new EventBorder();

        int division = teamSetting.size() * 2;

        int pos = 0;
        final int[] locX = new int[division];
        final int[] locY = new int[division];
        final int[] locZ = new int[division];

        // Get all coordinates inorder to create a polygon:
        for (CtfTeamSetting team : teamSetting) {
            Spawn flag = team.getFlag().getSpawn();
            if (flag == null) {
                continue;
            }

            locX[pos] = flag.getX();
            locY[pos] = flag.getY();
            locZ[pos] = flag.getZ();
            pos++;
            if (pos > (division / 2)) {
                break;
            }
        }

        for (CtfTeamSetting team : teamSetting) {
            Location spawnLocation = team.getSpawnLocation();
            locX[pos] = spawnLocation.getX();
            locY[pos] = spawnLocation.getY();
            locZ[pos] = spawnLocation.getZ();

            pos++;

            if (pos > division) {
                break;
            }
        }

        // Find the polygon center, note that it's not the mathematical center of the polygon,
        // Rather than a point which centers all coordinates:
        int centerX = 0;
        int centerY = 0;
        int centerZ = 0;
        for (int x = 0; x < pos; x++) {
            centerX += (locX[x] / division);
            centerY += (locY[x] / division);
            centerZ += (locZ[x] / division);
        }

        // Now let's find the furthest distance from the "center" to the egg shaped sphere
        // Surrounding the polygon, size x1.5 (for maximum logical area to wander...):
        int maxX = 0;
        int maxY = 0;
        int maxZ = 0;
        for (int x = 0; x < pos; x++) {
            maxX = Math.max(maxX, 2 * Math.abs(centerX - locX[x]));
            maxY = Math.max(maxY, 2 * Math.abs(centerY - locY[x]));
            maxZ = Math.max(maxZ, 2 * Math.abs(centerZ - locZ[x]));
        }

        // CenterX,centerY,centerZ are the coordinates of the "event center".
        // So let's save those coordinates to check on the players:
        Location center = new Location(centerX, centerY, centerZ);
        eventBorder.setCenter(center);
        eventBorder.setOffset(Math.max(Math.max(maxX, maxY), maxZ));
    }

    private Spawn configureSpawn(NpcTemplate npcTemplate, Location spawnPosition, String title) throws NoSuchMethodException, ClassNotFoundException {
        Spawn spawn = new Spawn(npcTemplate);

        spawn.setX(spawnPosition.getX());
        spawn.setY(spawnPosition.getY());
        spawn.setZ(spawnPosition.getZ());

        spawn.setAmount(1);
        spawn.setHeading(0);
        spawn.setRespawnDelay(1);

        SpawnTable.getInstance().addNewSpawn(spawn, false);
        spawn.init();

        NpcInstance throneLastSpawn = spawn.getLastSpawn();
        throneLastSpawn.getStatus().setCurrentHp(999999999);
        throneLastSpawn.decayMe();
        throneLastSpawn.spawnMe(throneLastSpawn.getX(), throneLastSpawn.getY(), throneLastSpawn.getZ());
        throneLastSpawn.setTitle(title);

        return spawn;
    }

    @Override
    protected void customUnspawnEventNpc() {
        try {
            for (CtfTeamSetting team : teamSetting) {
                unspawn(team.getThrone().getSpawn());
                unspawn(team.getFlag().getSpawn());
            }
        } catch (Exception e) {
            LOGGER.severe("CTF Engine[unspawnAllFlags()]: exception: " + e);
        }
    }

    private void unspawn(Spawn spawn) {
        if (spawn != null) {
            spawn.getLastSpawn().deleteMe();
            spawn.stopRespawn();
            SpawnTable.getInstance().deleteSpawn(spawn, true);
        }
    }

    @Override
    protected void startEvent() {
        Announcements.getInstance().criticalAnnounceToAll(generalSetting.getEventName() + ": Started. Go Capture the Flags!");

        eventState = START;

        sitPlayer();
    }

    @Override
    public boolean teleportPlayer() {
        final int WAIT_TELEPORT_SECONDS = 5;

        if (eventState != REGISTRATION || !shuffleTeams()) {
            return false;
        }

        removeOfflinePlayers();

        eventState = TELEPORTATION;

        Announcements.getInstance().criticalAnnounceToAll(generalSetting.getEventName() + ": Teleport to team spot in " + WAIT_TELEPORT_SECONDS + " seconds!");

        ThreadPool.schedule(() -> {
            setUserEventData();

            sitPlayer();

            spawnAllFlags();

            for (PlayerInstance player : players) {
                if (player != null) {
                    preTeleportPlayerChecks(player);

                    int offset = Config.CTF_SPAWN_OFFSET;

                    Location spawnLocation = findTeam(player.teamNameCtf).getSpawnLocation();

                    player.teleToLocation(
                            spawnLocation.getX() + Rnd.get(offset),
                            spawnLocation.getY() + Rnd.get(offset),
                            spawnLocation.getZ()
                    );
                }
            }
        }, TimeUnit.SECONDS.toMillis(WAIT_TELEPORT_SECONDS));

        return true;
    }

    @Override
    protected void removeOfflinePlayers() {
        try {
            if (players.isEmpty()) {
                return;
            }

            Iterator<PlayerInstance> playerIterator = players.iterator();

            while (playerIterator.hasNext()) {
                PlayerInstance player = playerIterator.next();

                if (player == null) {
                    playerIterator.remove();
                } else if (!player.isOnline() || player.isInJail() || player.isInOfflineMode()) {
                    excludePlayer(player, playerIterator);
                }
            }
        } catch (Exception e) {
            LOGGER.warning(e.getMessage());
        }
    }

    @Override
    protected void restorePlayerData() {
        Iterator<PlayerInstance> playerIterator = players.iterator();

        while (playerIterator.hasNext()) {
            PlayerInstance player = playerIterator.next();

            if (player != null) {
                removeFlagFromPlayer(player);

                excludePlayer(player, playerIterator);

                savedPlayerNames.remove(player.getName());
            }
        }
    }

    public void removeFlagFromPlayer(PlayerInstance player) {
        int flagItemId = findTeam(player.teamNameCtf).getFlag().getItemId();

        if (!player.haveFlagCtf) {
            player.getInventory().destroyItemByItemId("", flagItemId, 1, player, null);
            return;
        }

        player.haveFlagCtf = false;
        player.teamNameHaveFlagCtf = null;

        ItemInstance weaponEquipped = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LRHAND);

        // Get your weapon back now ...
        if (weaponEquipped != null) {
            ItemInstance[] unequipped = player.getInventory().unEquipItemInBodySlotAndRecord(weaponEquipped.getItem().getBodyPart());

            player.getInventory().destroyItemByItemId("", flagItemId, 1, player, null);

            InventoryUpdate inventoryUpdate = new InventoryUpdate();

            for (ItemInstance element : unequipped) {
                inventoryUpdate.addModifiedItem(element);
            }

            player.sendPacket(inventoryUpdate);
        } else {
            player.getInventory().destroyItemByItemId("", flagItemId, 1, player, null);
        }

        player.sendPacket(new ItemList(player, true)); // Get your weapon back now ...
        player.abortAttack();
        player.broadcastUserInfo();
    }

    private void preTeleportPlayerChecks(PlayerInstance player) {
        if (Config.CTF_ON_START_UNSUMMON_PET && player.getPet() != null) {
            Summon summon = player.getPet();

            for (Effect e1 : summon.getAllEffects()) {
                if (e1 != null) {
                    e1.exit(true);
                }
            }

            if (summon instanceof PetInstance) {
                summon.unSummon(player);
            }
        }

        if (Config.CTF_ON_START_REMOVE_ALL_EFFECTS) {
            for (Effect effectPlayer : player.getAllEffects()) {
                if (effectPlayer != null) {
                    effectPlayer.exit(true);
                }
            }
        }

        if (player.getParty() != null) {
            player.getParty().removePartyMember(player);
        }
    }

    protected void setUserEventData() {
        for (PlayerInstance player : players) {
            player.originalNameColorCtf = player.getAppearance().getNameColor();
            player.originalKarmaCtf = player.getKarma();
            player.originalTitleCtf = player.getTitle();
            player.teamNameHaveFlagCtf = null;
            player.haveFlagCtf = false;

            CtfTeamSetting team = findTeam(player.teamNameCtf);

            player.getAppearance().setNameColor(team.getColor());
            player.setKarma(0);
            if (Config.CTF_AURA && (teamSetting.size() >= 2)) {
                player.setTeam(teamSetting.indexOf(team) + 1);
            }

            if (player.isMounted() && player.setMountType(0)) {
                if (player.isFlying()) {
                    player.removeSkill(SkillTable.getInstance().getSkill(4289, 1));
                }

                player.broadcastPacket(new Ride(player.getObjectId(), Ride.ACTION_DISMOUNT, 0));
                player.setMountObjectID(0);
            }

            player.broadcastUserInfo();
        }
    }

    private boolean shuffleTeams() {
        if (EVENT_MODE == SHUFFLE && checkMinPlayers()) {
            int teamCount = 0;
            int playersCount = 0;

            List<PlayerInstance> playersShuffle = new ArrayList<>(players);
            players.clear();

            while (!playersShuffle.isEmpty()) {
                int playerToAddIndex = Rnd.get(playersShuffle.size());

                players.add(playersShuffle.get(playerToAddIndex));

                PlayerInstance player = players.get(playersCount);

                CtfTeamSetting team = teamSetting.get(teamCount);

                player.teamNameCtf = team.getName();

                savedPlayerNames.add(player.getName()); // TODO нужно понять, действительно ли нужны эти списки.
                savedTeamNamesPlayers.add(team.getName()); // TODO нужно понять, действительно ли нужны эти списки.

                playersShuffle.remove(playerToAddIndex);

                if (teamCount == (teamSetting.size() - 1)) {
                    teamCount = 0;
                } else {
                    teamCount++;
                }

                playersCount++;
            }
        } else if (EVENT_MODE == SHUFFLE) {
            String eventName = generalSetting.getEventName();
            int minPlayers = generalSetting.getMinPlayers();

            Announcements.getInstance().criticalAnnounceToAll(eventName + ": Not enough players for event. Min Requested : " + minPlayers + ", Participating : " + players.size());

            if (Config.CTF_STATS_LOGGER) {
                LOGGER.info(eventName + ":Not enough players for event. Min Requested : " + minPlayers + ", Participating : " + players.size());
            }

            return false;
        }

        return true;
    }

    @Override
    protected void startRegistrationPlayer() {
        eventState = REGISTRATION;

        spawnMainEventNpc(Event.CTF);

        String name = generalSetting.getEventName();

        Announcements announce = Announcements.getInstance();

        announce.criticalAnnounceToAll(name + ": Event " + name + "!");

        Item rewardTemplate = ItemTable.getInstance().getTemplate(generalSetting.getReward().getId());

        if (Config.CTF_ANNOUNCE_REWARD && rewardTemplate != null) {
            announce.criticalAnnounceToAll(name + ": Reward: " + generalSetting.getReward().getAmount() + " " + rewardTemplate.getName());
        }

        announce.criticalAnnounceToAll(name + ": Recruiting levels: " + generalSetting.getMinLevel() + " to " + generalSetting.getMaxLevel());
        announce.criticalAnnounceToAll(name + ": Registration in " + generalSetting.getRegistrationLocationName() + ".");

        if (Config.CTF_COMMAND) {
            announce.criticalAnnounceToAll(name + ": Commands .ctfjoin .ctfleave .ctfinfo!");
        }

        waiter(generalSetting.getTimeRegistration());
    }

    public void registerPlayer(PlayerInstance player, String teamName) {
        if (!checkPlayer(player) && !checkTeam(player, teamName)) {
            return;
        }

        if (NO.name().equals(CTF_EVEN_TEAMS) || BALANCE.name().equals(CTF_EVEN_TEAMS)) {
            player.teamNameCtf = teamName;

            findTeam(teamName).addPlayer();
        }

        players.add(player);

        player.inEventCtf = true;
        player._countCTFflags = 0;

        player.sendMessage(generalSetting.getEventName() + ": You successfully registered for the event.");
    }

    protected void excludePlayer(PlayerInstance player, Iterator<PlayerInstance> playerIterator) {
        restorePlayerData(player);

        playerIterator.remove();
    }

    public void excludePlayer(PlayerInstance player) {
        restorePlayerData(player);

        players.remove(player);
    }

    private void restorePlayerData(PlayerInstance player) {
        String eventName = generalSetting.getEventName();

        if (!player.inEventCtf) {
            player.sendMessage("You aren't registered in the " + eventName + " Event.");
            return;
        }

        player.sendMessage("Your participation in the CTF event has been removed.");

        player.getAppearance().setNameColor(player.originalNameColorCtf);
        player.setTitle(player.originalTitleCtf);
        player.setKarma(player.originalKarmaCtf);
        if (Config.CTF_AURA) {
            player.setTeam(0); // clear aura :P
        }
        player.broadcastUserInfo();

        // after remove, all event data must be cleaned in player
        player.originalNameColorCtf = 0;
        player.originalTitleCtf = null;
        player.originalKarmaCtf = 0;
        player.teamNameCtf = "";
        player._countCTFflags = 0;
        player.inEventCtf = false;
        player.broadcastUserInfo();

        if ((NO.name().equals(CTF_EVEN_TEAMS) || BALANCE.name().equals(CTF_EVEN_TEAMS)) && players.contains(player)) {
            findTeam(player.teamNameCtf).removePlayer();
        }
    }

    public void displayEventInformation(PlayerInstance player) {
        if (eventState == REGISTRATION) {
            RewardItem reward = generalSetting.getReward();

            player.sendMessage("There is " + players.size() + " player participating in this event.");
            player.sendMessage("Reward: " + reward.getAmount() + " " + ItemTable.getInstance().getTemplate(reward.getId()).getName() + " !");
            player.sendMessage("Player Min lvl: " + generalSetting.getMinLevel() + ".");
            player.sendMessage("Player Max lvl: " + generalSetting.getMaxLevel() + ".");
        }
    }

    public CtfTeamSetting findTeam(String name) {
        return teamSetting.stream()
                .filter(team -> team.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    private boolean checkPlayer(PlayerInstance player) {
        if (player.isAio() && !Config.ALLOW_AIO_IN_EVENTS) {
            player.sendMessage("AIO charactes are not allowed to participate in events :/");
            return false;
        }

        if (player.isCursedWeaponEquipped()) {
            player.sendMessage("You are not allowed to participate to the event because you are holding a Cursed Weapon.");
            return false;
        }

        if (player.getLevel() < generalSetting.getMinLevel()) {
            player.sendMessage("You are not allowed to participate to the event because your level is too low.");
            return false;
        }

        if (player.getLevel() > generalSetting.getMaxLevel()) {
            player.sendMessage("You are not allowed to participate to the event because your level is too high.");
            return false;
        }

        if (player.getKarma() > 0) {
            player.sendMessage("You are not allowed to participate to the event because you have Karma.");
            return false;
        }

        if (player.inEventCtf) {
            player.sendMessage("You already participated in the event!");
            return false;
        }

        if (player._inEventTvT || player._inEventDM) {
            player.sendMessage("You already participated in another event!");
            return false;
        }

        if (Olympiad.getInstance().isRegistered(player) || player.isInOlympiadMode()) {
            player.sendMessage("You already participated in Olympiad!");
            return false;
        }

        if ((player._activeBoxes > 1) && !Config.ALLOW_DUALBOX_EVENT) {
            List<String> playerBoxes = player._activeBoxeCharacters;

            if ((playerBoxes != null) && (playerBoxes.size() > 1)) {
                for (String characterName : playerBoxes) {
                    PlayerInstance playerInstance = World.getInstance().getPlayer(characterName);

                    if ((playerInstance != null) && playerInstance.inEventCtf) {
                        playerInstance.sendMessage("You already participated in event with another char!");
                        return false;
                    }
                }
            }
        }

        if (players.contains(player) || savedPlayerNames.contains(player.getName())) {
            player.sendMessage("You already participated in the event!");
            return false;
        }

        for (PlayerInstance playerInstance : players) {
            if (playerInstance.getObjectId() == playerInstance.getObjectId() ||
                    playerInstance.getName().equalsIgnoreCase(playerInstance.getName())) {
                playerInstance.sendMessage("You already participated in the event!");
                return false;
            }
        }

        return false;
    }

    private boolean checkTeam(PlayerInstance player, String teamName) {
        switch (EVENT_MODE) {
            case NO:
            case SHUFFLE:
                return true;
            case BALANCE:
                boolean allTeamsEqual = true;

                int playersCount = teamSetting.get(0).getPlayers();

                for (CtfTeamSetting team : teamSetting) {
                    if (playersCount != team.getPlayers()) {
                        allTeamsEqual = false;
                        break;
                    }

                    playersCount = team.getPlayers();
                }

                if (allTeamsEqual) {
                    return true;
                }

                int minPlayersCount = teamSetting.stream()
                        .map(TeamSetting::getPlayers)
                        .min(Comparator.naturalOrder())
                        .orElse(Integer.MIN_VALUE);

                for (CtfTeamSetting team : teamSetting) {
                    if (team.getPlayers() == minPlayersCount && team.getName().equals(teamName)) {
                        return true;
                    }
                }

                break;
        }

        player.sendMessage("Too many players in team \"" + teamName + "\"");

        return false;
    }

    @Override
    protected boolean preLaunchTeamPlayEventChecks() {
        return teamSetting.size() >= 2;
    }

    @Override
    protected boolean preLaunchSinglePlayerEventChecks() {
        return true;
    }

    @Override
    protected boolean customPreLaunchChecks() {
        return true;
    }

    @Override
    protected void customAbortEvent() {
        customUnspawnEventNpc();
    }

    public void dumpData() {
        LOGGER.info("");
        LOGGER.info("");

        String name = generalSetting.getEventName();

        LOGGER.info("<<--------------------------------->>");
        LOGGER.info(">> " + name + " Engine infos dump (" + eventState.name() + ") <<");
        LOGGER.info("<<--^----^^-----^----^^------^----->>");

        LOGGER.info("Name: " + name);
        LOGGER.info("Desc: " + generalSetting.getEventDescription());
        LOGGER.info("Join location: " + generalSetting.getRegistrationLocationName());
        LOGGER.info("Min lvl: " + generalSetting.getMinLevel());
        LOGGER.info("Max lvl: " + generalSetting.getMaxLevel());
        LOGGER.info("");
        LOGGER.info("##########################");
        LOGGER.info("# _teams(List<String>) #");
        LOGGER.info("##########################");

        for (CtfTeamSetting team : teamSetting) {
            LOGGER.info(team.getName() + " Flags Taken :" + team.getPoints());
        }

        if (CTF_EVEN_TEAMS.equals("SHUFFLE")) {
            LOGGER.info("");
            LOGGER.info("#########################################");
            LOGGER.info("# playersShuffle(List<PlayerInstance>) #");
            LOGGER.info("#########################################");

            for (PlayerInstance player : players) {
                if (player != null) {
                    LOGGER.info("Name: " + player.getName());
                }
            }
        }

        LOGGER.info("");
        LOGGER.info("##################################");
        LOGGER.info("# _players(List<PlayerInstance>) #");
        LOGGER.info("##################################");

        for (PlayerInstance player : players) {
            if (player != null) {
                LOGGER.info("Name: " + player.getName() + "   Team: " + player.teamNameCtf + "  Flags :" + player._countCTFflags);
            }
        }

        LOGGER.info("");
        LOGGER.info("#####################################################################");
        LOGGER.info("# _savePlayers(List<String>) and _savePlayerTeams(List<String>) #");
        LOGGER.info("#####################################################################");

        for (String player : savedPlayerNames) {
            LOGGER.info("Name: " + player + "	Team: " + savedTeamNamesPlayers.get(savedPlayerNames.indexOf(player)));
        }

        LOGGER.info("");
        LOGGER.info("");

        dumpLocalEventInfo();
    }

    private void dumpLocalEventInfo() {
        for (CtfTeamSetting team : teamSetting) {
            LOGGER.info("**********==CTF==************");
            LOGGER.info("CTF._teamPointsCount:" + team.getPoints());
            LOGGER.info("CTF._flagIds:" + team.getFlag().getNpc().getId());
            LOGGER.info("CTF._flagSpawns:" + team.getFlag().getSpawn());
            LOGGER.info("CTF._throneSpawns:" + team.getThrone().getSpawn());
            LOGGER.info("CTF._flagsTaken:" + team.getFlag().getTaken());
            LOGGER.info("CTF._flagsX:" + team.getFlag().getNpc().getSpawnPosition().getX());
            LOGGER.info("CTF._flagsY:" + team.getFlag().getNpc().getSpawnPosition().getY());
            LOGGER.info("CTF._flagsZ:" + team.getFlag().getNpc().getSpawnPosition().getZ());
            LOGGER.info("************EOF**************\n");
            LOGGER.info("");
        }
    }

    @Override
    public void loadData(int eventId) {
        try (Connection connection = DatabaseFactory.getConnection()) {
            PreparedStatement statement;
            ResultSet resultSet;

            statement = connection.prepareStatement("SELECT * FROM event WHERE id = ? AND type = ?");
            statement.setInt(1, eventId);
            statement.setString(2, eventType.name());
            resultSet = statement.executeQuery();

            if (!resultSet.next()) {
                LOGGER.warning("Setting ctf not found!");
                return;
            }

            generalSetting = new EventGeneralSettingBuilder()
                    .setName(resultSet.getString("name"))
                    .setDescription(resultSet.getString("description"))
                    .setRegistrationLocationName(resultSet.getString("registration_location"))
                    .setMinLevel(resultSet.getInt("min_level"))
                    .setMaxLevel(resultSet.getInt("max_level"))
                    .setNpc(new Npc(
                            resultSet.getInt("npc_id"),
                            new Location(
                                    resultSet.getInt("npc_x"),
                                    resultSet.getInt("npc_y"),
                                    resultSet.getInt("npc_z"),
                                    resultSet.getInt("npc_heading")
                            )
                    ))
                    .setReward(new RewardItem(
                            resultSet.getInt("reward_id"),
                            resultSet.getInt("reward_amount")
                    ))
                    .setTimeRegistration(resultSet.getInt("time_registration"))
                    .setDurationTime(resultSet.getInt("duration_event"))
                    .setMinPlayers(resultSet.getInt("min_players"))
                    .setMaxPlayers(resultSet.getInt("max_players"))
                    .setIntervalBetweenMatches(resultSet.getLong("delay_next_event"))
                    .createCtfGeneralSetting();

            statement.close();

            statement = connection.prepareStatement("SELECT * FROM ctf_team_setting WHERE event_id = ?");
            statement.setInt(1, eventId);
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                teamSetting.add(
                        new CtfTeamSetting(
                                resultSet.getInt("id"),
                                resultSet.getString("name"),
                                resultSet.getInt("name_color"),
                                new Location(
                                        resultSet.getInt("position_x"),
                                        resultSet.getInt("position_y"),
                                        resultSet.getInt("position_z")
                                )
                                ,
                                new Flag(
                                        new Npc(
                                                resultSet.getInt("flag_npc_id"),
                                                new Location(
                                                        resultSet.getInt("flag_position_x"),
                                                        resultSet.getInt("flag_position_y"),
                                                        resultSet.getInt("flag_position_z")
                                                )
                                        ),
                                        resultSet.getInt("flag_item_id"),
                                        null,
                                        false
                                ),
                                new Throne(
                                        new Npc(
                                                resultSet.getInt("throne_npc_id"),
                                                new Location(
                                                        resultSet.getInt("flag_position_x"),
                                                        resultSet.getInt("flag_position_y"),
                                                        resultSet.getInt("flag_position_z") - resultSet.getInt("offset_throne_position_z")
                                                )
                                        ),
                                        null,
                                        resultSet.getInt("offset_throne_position_z")
                                )
                        )
                );
            }

            statement.close();

        } catch (Exception e) {
            eventState = ERROR;
            LOGGER.severe("Exception: CTF.loadData(): " + e.getMessage());
        }
    }

    /**
     * Show event html.
     *
     * @param eventPlayer the event player
     * @param objectId    the object id
     */
    public static void showEventHtml(PlayerInstance eventPlayer, String objectId) {
        try {
            CTF ctf = find(REGISTRATION);

            String eventName = ctf.getEventIdentifier();

            GeneralSetting setting = ctf.getGeneralSetting();

            NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
            StringBuilder page = new StringBuilder();

            page.append("<html><title>").append(eventName).append("</title><body>");
            page.append("<center><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32></center><br1>");
            page.append("<center><font color=\"3366CC\">Current event:</font></center><br1>");
            page.append("<center>Name:&nbsp;<font color=\"00FF00\">").append(eventName).append("</font></center><br1>");
            page.append("<center>Description:&nbsp;<font color=\"00FF00\">")
                    .append(setting.getEventDescription()).append("</font></center><br><br>");

            State eventState = ctf.getEventState();

            if (eventState != START && eventState != REGISTRATION) {
                page.append("<center>Wait till the admin/gm start the participation.</center>");
            } else if (eventState != START && SHUFFLE.name().equals(CTF_EVEN_TEAMS) && !ctf.checkMaxPlayers()) {
                page.append("Currently participated: <font color=\"00FF00\">").append(ctf.getPlayers().size()).append(".</font><br>");
                page.append("Max players: <font color=\"00FF00\">").append(setting.getMaxPlayers()).append("</font><br><br>");
                page.append("<font color=\"FFFF00\">You can't participate to this event.</font><br>");
            } else if (eventPlayer.isCursedWeaponEquiped() && !Config.CTF_JOIN_CURSED) {
                page.append("<font color=\"FFFF00\">You can't participate to this event with a cursed Weapon.</font><br>");
            } else if (eventState == REGISTRATION && eventPlayer.getLevel() >= setting.getMinLevel() && eventPlayer.getLevel() <= setting.getMaxLevel()) {
                synchronized (ctf.getPlayers()) {
                    if (ctf.getPlayers().contains(eventPlayer)) {
                        if (NO.name().equals(CTF_EVEN_TEAMS) || BALANCE.name().equals(CTF_EVEN_TEAMS)) {
                            page.append("You participated already in team <font color=\"LEVEL\">").append(eventPlayer.teamNameCtf).append("</font><br><br>");
                        } else if (CTF_EVEN_TEAMS.equals("SHUFFLE")) {
                            page.append("<center><font color=\"3366CC\">You participated already!</font></center><br><br>");
                        }

                        page.append("<center>Joined Players: <font color=\"00FF00\">").append(ctf.getPlayers().size()).append("</font></center><br>");
                        page.append("<center><font color=\"3366CC\">Wait till event start or remove your participation!</font><center>");
                        page.append("<center><button value=\"Remove\" action=\"bypass -h npc_")
                                .append(objectId).append("_ctf_player_leave\" width=85 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\"></center>");
                    } else {
                        page.append("<center><font color=\"3366CC\">You want to participate in the event?</font></center><br>");
                        page.append("<center><td width=\"200\">Min lvl: <font color=\"00FF00\">").append(setting.getMinLevel()).append("</font></center></td><br>");
                        page.append("<center><td width=\"200\">Max lvl: <font color=\"00FF00\">").append(setting.getMaxLevel()).append("</font></center></td><br><br>");
                        page.append("<center><font color=\"3366CC\">Teams:</font></center><br>");
                        if (NO.name().equals(CTF_EVEN_TEAMS) || BALANCE.name().equals(CTF_EVEN_TEAMS)) {
                            page.append("<center><table border=\"0\">");
                            for (CtfTeamSetting team : ctf.getTeamSetting()) {
                                page.append("<tr><td width=\"100\"><font color=\"LEVEL\">")
                                        .append(team.getName()).append("</font>&nbsp;(").append(team.getPlayers()).append(" joined)</td>");
                                page.append("<center><td width=\"60\"><button value=\"Join\" action=\"bypass -h npc_")
                                        .append(objectId).append("_ctf_player_join ").append(team.getName())
                                        .append("\" width=85 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\"></center></td></tr>");
                            }
                            page.append("</table></center>");
                        } else if (SHUFFLE.name().equals(CTF_EVEN_TEAMS)) {
                            page.append("<center>");

                            for (CtfTeamSetting team : ctf.getTeamSetting()) {
                                page.append("<tr><td width=\"100\"><font color=\"LEVEL\">").append(team.getName()).append("</font> &nbsp;</td>");
                            }

                            page.append("</center><br>");

                            page.append("<center><button value=\"Join Event\" action=\"bypass -h npc_")
                                    .append(objectId)
                                    .append("_ctf_player_join eventShuffle\" width=85 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\"></center>");
                            page.append("<center><font color=\"3366CC\">Teams will be randomly generated!</font></center><br>");
                            page.append("<center>Joined Players:</font> <font color=\"LEVEL\">").append(ctf.getPlayers().size()).append("</center></font><br>");
                            page.append("<center>Reward: <font color=\"LEVEL\">").append(setting.getReward().getAmount())
                                    .append(" ").append(ItemTable.getInstance().getTemplate(setting.getReward().getId()).getName()).append("</center></font>");
                        }
                    }
                }
            } else if (eventState == START) {
                page.append("<center>").append(eventName).append(" match is in progress.</center>");
            } else if (eventPlayer.getLevel() < setting.getMinLevel() || eventPlayer.getLevel() > setting.getMaxLevel()) {
                page.append("Your lvl: <font color=\"00FF00\">").append(eventPlayer.getLevel()).append("</font><br>");
                page.append("Min lvl: <font color=\"00FF00\">").append(eventPlayer.getLevel()).append("</font><br>");
                page.append("Max lvl: <font color=\"00FF00\">").append(setting.getMaxLevel()).append("</font><br><br>");
                page.append("<font color=\"FFFF00\">You can't participate to this event.</font><br>");
            }

            page.append("</body></html>");
            adminReply.setHtml(page.toString());
            eventPlayer.sendPacket(adminReply);

            // Send a Server->Client ActionFailed to the PlayerInstance in order to avoid that the client wait another packet
            eventPlayer.sendPacket(ActionFailed.STATIC_PACKET);
        } catch (Exception e) {
            LOGGER.severe("CTF.showEventHtlm(" + eventPlayer.getName() + ", " + objectId + ")]: exception" + e.getMessage());
        }
    }

    // TODO need test
    public synchronized void addDisconnectedPlayer(PlayerInstance player) {
        if (eventState == START || eventState == TELEPORTATION) {
            if (Config.CTF_ON_START_REMOVE_ALL_EFFECTS) {
                player.stopAllEffects();
            }

            player.teamNameCtf = savedTeamNamesPlayers.get(savedPlayerNames.indexOf(player.getName()));

            Iterator<PlayerInstance> playersIterator = players.iterator();

            while (playersIterator.hasNext()) {
                PlayerInstance eventPlayer = playersIterator.next();

                if (eventPlayer != null && eventPlayer.getName().equals(player.getName())) {
                    player.teamNameCtf = eventPlayer.teamNameCtf;
                    player.originalNameColorCtf = player.getAppearance().getNameColor();
                    player.originalTitleCtf = player.getTitle();
                    player.originalKarmaCtf = player.getKarma();
                    player.inEventCtf = true;
                    player._countCTFflags = eventPlayer._countCTFflags;
                    playersIterator.remove(); // removing old object id from vector

                    break;
                }
            }

            players.add(player); // adding new objectId to vector

            CtfTeamSetting team = findTeam(player.teamNameCtf);

            player.getAppearance().setNameColor(team.getColor());
            player.setKarma(0);
            if (Config.CTF_AURA) {
                player.setTeam(team.getId());
            }
            player.broadcastUserInfo();

            int offset = Config.CTF_SPAWN_OFFSET;
            Location spawnLocation = team.getSpawnLocation();
            player.teleToLocation(
                    spawnLocation.getX() + Rnd.get(offset),
                    spawnLocation.getX() + Rnd.get(offset),
                    spawnLocation.getX()
            );

            player.teamNameHaveFlagCtf = null;
            player.haveFlagCtf = false;

            restoreFlag();
        }
    }

    private void playAnimation(List<String> winningNameList) {
        for (PlayerInstance player : players) {
            if (player != null) {
                int actionId = winningNameList.contains(player.teamNameCtf) ? 3 : 7; // TODO animation id

                player.broadcastPacket(new SocialAction(player.getObjectId(), actionId));
            }
        }
    }

    private void giveRewardTeam(List<String> listWinners) {
        RewardItem reward = generalSetting.getReward();
        int rewardAmount;

        NpcHtmlMessage nhm = new NpcHtmlMessage(5); // TODO id
        String winnerPage = "<html><body><font color=\"FFFF00\">Your team wins the event. Look in your inventory for the reward.</font></body></html>";
        String tiePage = "<html><body><font color=\"FFFF00\">Your team had a tie in the event. Look in your inventory for the reward.</font></body></html>";
        String page;

        for (PlayerInstance player : players) {
            if (player == null || !player.isOnline() || !player.inEventCtf || !listWinners.contains(player.teamNameCtf)) {
                continue;
            }

            if (listWinners.size() == 1 && listWinners.contains(player.teamNameCtf)) {
                rewardAmount = reward.getAmount();
                page = winnerPage;
            } else {
                if (findTeam(player.teamNameCtf).getPoints() > 0) {
                    rewardAmount = reward.getAmount() / 2;
                } else {
                    // nobody took flags
                    rewardAmount = reward.getAmount() / 4;
                }

                page = tiePage;
            }

            player.addItem(generalSetting.getEventName() + " Event: " + generalSetting.getEventName(), reward.getId(), rewardAmount, player, true);

            nhm.setHtml(page);
            player.sendPacket(nhm);

            // Send a Server->Client ActionFailed to the PlayerInstance in order to avoid that the client wait another packet
            player.sendPacket(ActionFailed.STATIC_PACKET);
        }
    }

    public void onDisconnect(PlayerInstance player) {
        if (player.inEventCtf) {
            excludePlayer(player);

            Location spawnPosition = generalSetting.getMainNpc().getSpawnPosition();
            player.teleToLocation(spawnPosition.getX(), spawnPosition.getY(), spawnPosition.getZ());
        }
    }

    public void showFlagHtml(PlayerInstance eventPlayer, String objectId, String teamName) {
        if (eventPlayer == null) {
            return;
        }

        try {
            NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
            StringBuilder page = new StringBuilder("<html><head><body><center>");

            page.append("CTF Flag<br><br>");
            page.append("<font color=\"00FF00\">").append(teamName).append("'s Flag</font><br1>");

            if (eventPlayer.teamNameCtf != null && eventPlayer.teamNameCtf.equals(teamName)) {
                page.append("<font color=\"LEVEL\">This is your Flag</font><br1>");
            } else {
                page.append("<font color=\"LEVEL\">Enemy Flag!</font><br1>");
            }

            if (eventState == START) {
                processInFlagRange(eventPlayer);
            } else {
                page.append("CTF match is not in progress yet.<br>Wait for a GM to start the event<br>");
            }

            page.append("</center></body></html>");
            adminReply.setHtml(page.toString());
            eventPlayer.sendPacket(adminReply);
        } catch (Exception e) {
            LOGGER.severe("CTF.showFlagHtlm(" + eventPlayer.getName() + ", " + objectId + ")]: exception: " + Arrays.toString(e.getStackTrace()));
        }
    }

    public void restoreFlag() {
        try {
            String eventName = generalSetting.getEventName();

            for (PlayerInstance player : players) {
                if (player != null) {
                    CtfTeamSetting team = findTeam(player.teamNameHaveFlagCtf);

                    // logged off with a flag in his hands
                    if (!player.isOnline() && player.haveFlagCtf) {
                        Announcements.getInstance().criticalAnnounceToAll(eventName + ": " + player.getName() + " logged off with a CTF flag!");

                        team.getFlag().setTaken(false);

                        removeFlagFromPlayer(player);
                        spawnFlag(team);

                        Announcements.getInstance().criticalAnnounceToAll(eventName + ": " + team.getName() + " flag now returned to place.");

                        return;
                    } else if (player.haveFlagCtf) {
                        team.getFlag().setTaken(true);
                    }
                }
            }

            // Check if a player ran away from the event holding a flag:
            for (PlayerInstance player : players) {
                if (player != null && player.haveFlagCtf && isOutsideCtfArea(player)) {
                    Announcements.getInstance().criticalAnnounceToAll(eventName + ": " + player.getName() + " escaped from the event holding a flag!");

                    CtfTeamSetting team = findTeam(player.teamNameHaveFlagCtf);

                    if (team.getFlag().getTaken()) {
                        team.getFlag().setTaken(false);

                        spawnFlag(team);

                        Announcements.getInstance().criticalAnnounceToAll(eventName + ": " + team.getName() + " flag now returned to place.");
                    }

                    removeFlagFromPlayer(player);

                    Location spawnLocation = team.getSpawnLocation();

                    player.teleToLocation(
                            spawnLocation.getX(),
                            spawnLocation.getY(),
                            spawnLocation.getZ()
                    );

                    player.sendMessage("You have been returned to your team spawn");

                    return;
                }
            }
        } catch (Exception e) {
            LOGGER.severe("CTF.restoreFlags() Error:" + e);
        }
    }

    public void addFlagToPlayer(PlayerInstance player, int flagItemId) {
        // Remove items from the player hands (right, left, both)
        // This is NOT a BUG, I don't want them to see the icon they have 8D
        ItemInstance weapon = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);

        if (weapon == null) {
            weapon = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LRHAND);

            if (weapon != null) {
                player.getInventory().unEquipItemInBodySlotAndRecord(Inventory.PAPERDOLL_LRHAND);
            }
        } else {
            player.getInventory().unEquipItemInBodySlotAndRecord(Inventory.PAPERDOLL_RHAND);
            weapon = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);

            if (weapon != null) {
                player.getInventory().unEquipItemInBodySlotAndRecord(Inventory.PAPERDOLL_LHAND);
            }
        }

        // Add the flag in his hands
        player.getInventory().equipItem(ItemTable.getInstance().createItem("", flagItemId, 1, player, null));
        player.broadcastPacket(new SocialAction(player.getObjectId(), 16)); // Amazing glow
        player.haveFlagCtf = true;
        player.broadcastUserInfo();
        player.sendPacket(new CreatureSay(player.getObjectId(), ChatType.PARTYROOM_COMMANDER, ":", "You got it! Run back! ::"));
    }

    public void spawnFlag(CtfTeamSetting team) {
        final NpcTemplate flagTemplate = NpcTable.getInstance().getTemplate(team.getFlag().getNpc().getId());

        try {
            Spawn flagSpawn = configureSpawn(flagTemplate, team.getFlag().getNpc().getSpawnPosition(), team.getName() + "' Flag");

            flagSpawn.getLastSpawn().ctfFlagTeamName = team.getName();
            flagSpawn.getLastSpawn().isCtfFlagNpc = true;
        } catch (Exception e) {
            LOGGER.severe("CTF.spawnFlag(" + team.getName() + ")]: exception: " + e);
        }
    }

    public boolean inRangeOfFlag(PlayerInstance player, Location flag, int offset) {
        return player.getX() > (flag.getX() - offset) &&
                player.getX() < (flag.getX() + offset) &&
                player.getY() > (flag.getY() - offset) &&
                player.getY() < (flag.getY() + offset) &&
                player.getZ() > (flag.getZ() - offset) &&
                player.getZ() < (flag.getZ() + offset);
    }

    public void processInFlagRange(PlayerInstance player) {
        try {
            restoreFlag();

            for (CtfTeamSetting team : teamSetting) {
                Flag flag = team.getFlag();

                if (team.getName().equals(player.teamNameCtf)) {
                    // If player is near his team flag holding the enemy flag
                    if (inRangeOfFlag(player, flag.getNpc().getSpawnPosition(), 100) && !flag.getTaken() && player.haveFlagCtf) {
                        CtfTeamSetting enemyTeam = findTeam(player.teamNameHaveFlagCtf);

                        enemyTeam.getFlag().setTaken(false);
                        spawnFlag(enemyTeam);

                        // Remove the flag from this player
                        player.broadcastPacket(new SocialAction(player.getObjectId(), 16)); // Amazing glow TODO id
                        player.broadcastUserInfo();
                        player.broadcastPacket(new SocialAction(player.getObjectId(), 3)); // Victory TODO id
                        player.broadcastUserInfo();

                        removeFlagFromPlayer(player);

                        team.addPoint();

                        Announcements.getInstance().criticalAnnounceToAll(generalSetting.getEventName() + ": " + player.getName() + " scores for " + team.getName() + ".");
                    }
                } else {
                    // If the player is near a enemy flag
                    if (inRangeOfFlag(player, flag.getNpc().getSpawnPosition(), 100) && !flag.getTaken() && !player.haveFlagCtf && !player.isDead()) {
                        flag.setTaken(true);
                        unspawn(flag.getSpawn());

                        player.teamNameHaveFlagCtf = team.getName();
                        addFlagToPlayer(player, flag.getItemId());

                        Announcements.getInstance().criticalAnnounceToAll(generalSetting.getEventName() + ": " + team.getName() + " flag taken by " + player.getName() + "...");

                        displayRadar(player, team.getName());

                        break;
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warning(e.toString());
        }
    }

    private void displayRadar(PlayerInstance hasFlag, String ourFlag) {
        try {
            for (PlayerInstance player : players) {
                if ((player != null) && player.isOnline() && player.teamNameCtf.equals(ourFlag)) {
                    player.sendMessage(hasFlag.getName() + " took your flag!");

                    if (player.haveFlagCtf) {
                        player.sendMessage("You can not return the flag to headquarters, until your flag is returned to it's place.");
                        player.sendPacket(new RadarControl(1, 1, player.getX(), player.getY(), player.getZ()));
                    } else {
                        player.sendPacket(new RadarControl(0, 1, hasFlag.getX(), hasFlag.getY(), hasFlag.getZ()));
                        Radar radar = new Radar(player);
                        Radar.RadarOnPlayer radarOnPlayer = radar.new RadarOnPlayer(hasFlag, player);
                        ThreadPool.schedule(radarOnPlayer, 10000 + Rnd.get(30000));
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.severe(e.toString());
        }
    }

    // TODO need TEST
    private boolean isOutsideCtfArea(PlayerInstance player) {
        Location center = eventBorder.getCenter();

        return player == null ||
                !player.isOnline() ||
                player.getX() <= (center.getX() - eventBorder.getOffset()) ||
                player.getX() >= (center.getX() + eventBorder.getOffset()) ||
                player.getY() <= (center.getY() - eventBorder.getOffset()) ||
                player.getY() >= (center.getY() + eventBorder.getOffset()) ||
                player.getZ() <= (center.getZ() - eventBorder.getOffset()) ||
                player.getZ() >= (center.getZ() + eventBorder.getOffset());
    }

    public void removeFlagOnPlayerDie(PlayerInstance player) {
        CtfTeamSetting team = findTeam(player.teamNameHaveFlagCtf);

        team.getFlag().setTaken(false);
        spawnFlag(team);
        removeFlagFromPlayer(player);

        player.broadcastUserInfo();

        Announcements.getInstance().criticalAnnounceToAll(generalSetting.getEventName() + ": " + team.getName() + "'s flag returned.");
    }

        /*public void saveData() {
        try (Connection con = DatabaseFactory.getConnection()) {
            PreparedStatement statement;
            statement = con.prepareStatement("Delete from ctf");
            statement.execute();
            statement.close();

            statement = con.prepareStatement("INSERT INTO ctf (eventName, eventDesc, joiningLocation, minLevel, maxLevel, npcId, npcX, npcY, npcZ, npcHeading, rewardId, rewardAmount, teamsCount, joinTime, eventTime, minPlayers, maxPlayers,delayForNextEvent) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            statement.setString(1, name);
            statement.setString(2, description);
            statement.setString(3, joiningLocationName);
            statement.setInt(4, minLevel);
            statement.setInt(5, maxLevel);
            statement.setInt(6, npc.getId());
            statement.setInt(7, npc.getSpawnPosition().getX());
            statement.setInt(8, npc.getSpawnPosition().getY());
            statement.setInt(9, npc.getSpawnPosition().getZ());
            statement.setInt(10, npcHeading);
            statement.setInt(11, rewardId);
            statement.setInt(12, rewardAmount);
            statement.setInt(13, _teams.size());
            statement.setInt(14, joinTime);
            statement.setInt(15, eventTime);
            statement.setInt(16, minPlayers);
            statement.setInt(17, maxPlayers);
            statement.setLong(18, intervalBetweenMatches);
            statement.execute();
            statement.close();

            statement = con.prepareStatement("Delete from ctf_teams");
            statement.execute();
            statement.close();

            for (String teamName : _teams) {
                final int index = _teams.indexOf(teamName);
                if (index == -1) {
                    return;
                }
                statement = con.prepareStatement("INSERT INTO ctf_teams (teamId ,teamName, teamX, teamY, teamZ, teamColor, flagX, flagY, flagZ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
                statement.setInt(1, index);
                statement.setString(2, teamName);
                statement.setInt(3, _teamsX.get(index));
                statement.setInt(4, _teamsY.get(index));
                statement.setInt(5, _teamsZ.get(index));
                statement.setInt(6, _teamColors.get(index));
                statement.setInt(7, _flagsX.get(index));
                statement.setInt(8, _flagsY.get(index));
                statement.setInt(9, _flagsZ.get(index));
                statement.execute();
                statement.close();
            }
        } catch (Exception e) {
            LOGGER.warning("Exception: saveData(): " + e.getMessage());
        }
    }
       public void addTeam(String teamName) {
        if (inProgress) {
            return;
        }

        if (teamName.equals(" ")) {
            return;
        }

        _teams.add(teamName);
        _teamPlayersCount.add(0);
        _teamPointsCount.add(0);
        _teamColors.add(0);
        _teamsX.add(0);
        _teamsY.add(0);
        _teamsZ.add(0);

        addTeamEventOperations(teamName);
    }

    private void addTeamEventOperations(String teamName) {
        addOrSet(_teams.indexOf(teamName), null, false, _FlagNPC, 0, 0, 0);
    }


        public void removeTeam(String teamName) {
            if (inProgress || _teams.isEmpty()) {
                return;
            }

            if (teamPlayersCount(teamName) > 0) {
                return;
            }

            final int index = _teams.indexOf(teamName);
            if (index == -1) {
                return;
            }

            _teamsZ.remove(index);
            _teamsY.remove(index);
            _teamsX.remove(index);
            _teamColors.remove(index);
            _teamPointsCount.remove(index);
            _teamPlayersCount.remove(index);
            _teams.remove(index);

            removeTeamEventItems(teamName);
        }

    private void removeTeamEventItems(String teamName) {
        final int index = _teams.indexOf(teamName);
        _flagSpawns.remove(index);
        _flagsTaken.remove(index);
        _flagIds.remove(index);
        _flagsX.remove(index);
        _flagsY.remove(index);
        _flagsZ.remove(index);
    }

    public void setTeamPos(String teamName, PlayerInstance player) {
        final int index = _teams.indexOf(teamName);
        if (index == -1) {
            return;
        }

        _teamsX.set(index, player.getX());
        _teamsY.set(index, player.getY());
        _teamsZ.set(index, player.getZ());
    }

    public void setTeamPos(String teamName, int x, int y, int z) {
        final int index = _teams.indexOf(teamName);
        if (index == -1) {
            return;
        }

        _teamsX.set(index, x);
        _teamsY.set(index, y);
        _teamsZ.set(index, z);
    }

    public void setTeamColor(String teamName, int color) {
        if (inProgress) {
            return;
        }

        final int index = _teams.indexOf(teamName);
        if (index == -1) {
            return;
        }

        _teamColors.set(index, color);
    }

    public void setTeamPlayersCount(String teamName, int teamPlayersCount) {
        final int index = _teams.indexOf(teamName);
        if (index == -1) {
            return;
        }

        _teamPlayersCount.set(index, teamPlayersCount);
    }

        public static void setTeamFlag(String teamName, PlayerInstance player) {
        final int index = _teams.indexOf(teamName);
        if (index == -1) {
            return;
        }
        addOrSet(_teams.indexOf(teamName), null, false, _FlagNPC, player.getX(), player.getY(), player.getZ());
    }
    */

    public static List<CTF> getEventTaskList() {
        return eventTaskList;
    }

    public static CTF find(State state) {
        return eventTaskList.stream()
                .filter(event -> event.eventState == state)
                .findFirst()
                .orElse(null);
    }

    public static boolean isStarted() {
        return find(START) != null;
    }

    public static boolean isTeleported() {
        return find(TELEPORTATION) != null;
    }

    public static CTF findActive() {
        return eventTaskList.stream()
                .filter(event -> event.eventState != INACTIVE)
                .findFirst()
                .orElse(null);
    }

    public List<CtfTeamSetting> getTeamSetting() {
        return teamSetting;
    }
}
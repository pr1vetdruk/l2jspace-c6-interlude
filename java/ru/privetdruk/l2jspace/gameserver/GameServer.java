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
package ru.privetdruk.l2jspace.gameserver;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import ru.privetdruk.l2jspace.Config;
import ru.privetdruk.l2jspace.commons.concurrent.ThreadPool;
import ru.privetdruk.l2jspace.commons.database.DatabaseFactory;
import ru.privetdruk.l2jspace.commons.enums.ServerMode;
import ru.privetdruk.l2jspace.commons.mmocore.SelectorConfig;
import ru.privetdruk.l2jspace.commons.mmocore.SelectorThread;
import ru.privetdruk.l2jspace.commons.util.DeadlockDetector;
import ru.privetdruk.l2jspace.commons.util.IPv4Filter;
import ru.privetdruk.l2jspace.commons.util.Util;
import ru.privetdruk.l2jspace.gameserver.cache.CrestCache;
import ru.privetdruk.l2jspace.gameserver.cache.HtmCache;
import ru.privetdruk.l2jspace.gameserver.communitybbs.Manager.ForumsBBSManager;
import ru.privetdruk.l2jspace.gameserver.datatables.HeroSkillTable;
import ru.privetdruk.l2jspace.gameserver.datatables.ItemTable;
import ru.privetdruk.l2jspace.gameserver.datatables.NobleSkillTable;
import ru.privetdruk.l2jspace.gameserver.datatables.OfflineTradeTable;
import ru.privetdruk.l2jspace.gameserver.datatables.SchemeBufferTable;
import ru.privetdruk.l2jspace.gameserver.datatables.SkillTable;
import ru.privetdruk.l2jspace.gameserver.datatables.sql.CharNameTable;
import ru.privetdruk.l2jspace.gameserver.datatables.sql.ClanTable;
import ru.privetdruk.l2jspace.gameserver.datatables.sql.HelperBuffTable;
import ru.privetdruk.l2jspace.gameserver.datatables.sql.NpcTable;
import ru.privetdruk.l2jspace.gameserver.datatables.sql.PetDataTable;
import ru.privetdruk.l2jspace.gameserver.datatables.sql.SkillSpellbookTable;
import ru.privetdruk.l2jspace.gameserver.datatables.sql.SkillTreeTable;
import ru.privetdruk.l2jspace.gameserver.datatables.sql.SpawnTable;
import ru.privetdruk.l2jspace.gameserver.datatables.sql.TeleportLocationTable;
import ru.privetdruk.l2jspace.gameserver.datatables.xml.AdminData;
import ru.privetdruk.l2jspace.gameserver.datatables.xml.ArmorSetData;
import ru.privetdruk.l2jspace.gameserver.datatables.xml.AugmentationData;
import ru.privetdruk.l2jspace.gameserver.datatables.xml.BoatData;
import ru.privetdruk.l2jspace.gameserver.datatables.xml.DoorData;
import ru.privetdruk.l2jspace.gameserver.datatables.xml.ExperienceData;
import ru.privetdruk.l2jspace.gameserver.datatables.xml.ExtractableItemData;
import ru.privetdruk.l2jspace.gameserver.datatables.xml.FenceData;
import ru.privetdruk.l2jspace.gameserver.datatables.xml.FishData;
import ru.privetdruk.l2jspace.gameserver.datatables.xml.HennaData;
import ru.privetdruk.l2jspace.gameserver.datatables.xml.ManorSeedData;
import ru.privetdruk.l2jspace.gameserver.datatables.xml.MapRegionData;
import ru.privetdruk.l2jspace.gameserver.datatables.xml.MultisellData;
import ru.privetdruk.l2jspace.gameserver.datatables.xml.PlayerTemplateData;
import ru.privetdruk.l2jspace.gameserver.datatables.xml.RecipeData;
import ru.privetdruk.l2jspace.gameserver.datatables.xml.StaticObjectData;
import ru.privetdruk.l2jspace.gameserver.datatables.xml.SummonItemData;
import ru.privetdruk.l2jspace.gameserver.datatables.xml.WalkerRouteData;
import ru.privetdruk.l2jspace.gameserver.datatables.xml.ZoneData;
import ru.privetdruk.l2jspace.gameserver.geoengine.GeoEngine;
import ru.privetdruk.l2jspace.gameserver.handler.AdminCommandHandler;
import ru.privetdruk.l2jspace.gameserver.handler.AutoAnnouncementHandler;
import ru.privetdruk.l2jspace.gameserver.handler.AutoChatHandler;
import ru.privetdruk.l2jspace.gameserver.handler.ItemHandler;
import ru.privetdruk.l2jspace.gameserver.handler.SkillHandler;
import ru.privetdruk.l2jspace.gameserver.handler.UserCommandHandler;
import ru.privetdruk.l2jspace.gameserver.handler.VoicedCommandHandler;
import ru.privetdruk.l2jspace.gameserver.instancemanager.AuctionManager;
import ru.privetdruk.l2jspace.gameserver.instancemanager.CastleManager;
import ru.privetdruk.l2jspace.gameserver.instancemanager.CastleManorManager;
import ru.privetdruk.l2jspace.gameserver.instancemanager.ClanHallManager;
import ru.privetdruk.l2jspace.gameserver.instancemanager.ClassDamageManager;
import ru.privetdruk.l2jspace.gameserver.instancemanager.CoupleManager;
import ru.privetdruk.l2jspace.gameserver.instancemanager.CrownManager;
import ru.privetdruk.l2jspace.gameserver.instancemanager.CursedWeaponsManager;
import ru.privetdruk.l2jspace.gameserver.instancemanager.CustomMailManager;
import ru.privetdruk.l2jspace.gameserver.instancemanager.DayNightSpawnManager;
import ru.privetdruk.l2jspace.gameserver.instancemanager.DimensionalRiftManager;
import ru.privetdruk.l2jspace.gameserver.instancemanager.DuelManager;
import ru.privetdruk.l2jspace.gameserver.instancemanager.FishingChampionshipManager;
import ru.privetdruk.l2jspace.gameserver.instancemanager.FortManager;
import ru.privetdruk.l2jspace.gameserver.instancemanager.FortSiegeManager;
import ru.privetdruk.l2jspace.gameserver.instancemanager.FourSepulchersManager;
import ru.privetdruk.l2jspace.gameserver.instancemanager.GlobalVariablesManager;
import ru.privetdruk.l2jspace.gameserver.instancemanager.GrandBossManager;
import ru.privetdruk.l2jspace.gameserver.instancemanager.IdManager;
import ru.privetdruk.l2jspace.gameserver.instancemanager.ItemsOnGroundManager;
import ru.privetdruk.l2jspace.gameserver.instancemanager.MercTicketManager;
import ru.privetdruk.l2jspace.gameserver.instancemanager.PetitionManager;
import ru.privetdruk.l2jspace.gameserver.instancemanager.QuestManager;
import ru.privetdruk.l2jspace.gameserver.instancemanager.RaidBossPointsManager;
import ru.privetdruk.l2jspace.gameserver.instancemanager.RaidBossSpawnManager;
import ru.privetdruk.l2jspace.gameserver.instancemanager.ServerRestartManager;
import ru.privetdruk.l2jspace.gameserver.instancemanager.SiegeManager;
import ru.privetdruk.l2jspace.gameserver.model.World;
import ru.privetdruk.l2jspace.gameserver.model.entity.Announcements;
import ru.privetdruk.l2jspace.gameserver.model.entity.Hero;
import ru.privetdruk.l2jspace.gameserver.model.entity.event.Lottery;
import ru.privetdruk.l2jspace.gameserver.model.entity.event.MonsterRace;
import ru.privetdruk.l2jspace.gameserver.model.entity.event.PcPoint;
import ru.privetdruk.l2jspace.gameserver.model.entity.event.manager.EventManager;
import ru.privetdruk.l2jspace.gameserver.model.entity.olympiad.Olympiad;
import ru.privetdruk.l2jspace.gameserver.model.entity.sevensigns.SevenSigns;
import ru.privetdruk.l2jspace.gameserver.model.entity.sevensigns.SevenSignsFestival;
import ru.privetdruk.l2jspace.gameserver.model.entity.siege.clanhalls.BanditStrongholdSiege;
import ru.privetdruk.l2jspace.gameserver.model.entity.siege.clanhalls.DevastatedCastle;
import ru.privetdruk.l2jspace.gameserver.model.entity.siege.clanhalls.FortressOfResistance;
import ru.privetdruk.l2jspace.gameserver.model.partymatching.PartyMatchRoomList;
import ru.privetdruk.l2jspace.gameserver.model.partymatching.PartyMatchWaitingList;
import ru.privetdruk.l2jspace.gameserver.model.spawn.AutoSpawn;
import ru.privetdruk.l2jspace.gameserver.network.GameClient;
import ru.privetdruk.l2jspace.gameserver.network.GamePacketHandler;
import ru.privetdruk.l2jspace.gameserver.script.EventDroplist;
import ru.privetdruk.l2jspace.gameserver.script.faenor.FaenorScriptEngine;
import ru.privetdruk.l2jspace.gameserver.scripting.ScriptEngineManager;
import ru.privetdruk.l2jspace.gameserver.taskmanager.TaskManager;
import ru.privetdruk.l2jspace.gameserver.ui.Gui;
import ru.privetdruk.l2jspace.telnet.TelnetStatusThread;

public class GameServer {
    private static final Logger LOGGER = Logger.getLogger(GameServer.class.getName());

    private static SelectorThread<GameClient> selectorThread;
    private static LoginServerThread loginThread;
    private static GamePacketHandler gamePacketHandler;
    private static TelnetStatusThread statusServer;
    private static GameServer INSTANCE;

    public static final Calendar dateTimeServerStarted = Calendar.getInstance();

    public GameServer() throws Exception {
        final long serverLoadStart = System.currentTimeMillis();

        // GUI
        if (!GraphicsEnvironment.isHeadless()) {
            System.out.println("GameServer: Running in GUI mode.");
            new Gui();
        }

        // Create log folder
        final File logFolder = new File(".", "log");
        logFolder.mkdir();

        // Create input stream for log file -- or store file data into memory
        try (InputStream is = new FileInputStream(new File("./log.cfg"))) {
            LogManager.getLogManager().readConfiguration(is);
        }

        // Initialize config
        Config.load(ServerMode.GAME);

        Util.printSection("Database");
        DatabaseFactory.init();

        Util.printSection("ThreadPool");
        ThreadPool.init();
        if (Config.DEADLOCKCHECK_INTIAL_TIME > 0) {
            ThreadPool.scheduleAtFixedRate(DeadlockDetector.getInstance(), Config.DEADLOCKCHECK_INTIAL_TIME, Config.DEADLOCKCHECK_DELAY_TIME);
        }

        Util.printSection("IdManager");
        IdManager.getInstance();
        if (!IdManager.hasInitialized()) {
            LOGGER.severe("IdFactory: Could not read object IDs from database. Please check your configuration.");
            throw new Exception("Could not initialize the ID factory!");
        }

        new File(Config.DATAPACK_ROOT, "data/clans").mkdirs();
        new File(Config.DATAPACK_ROOT, "data/crests").mkdirs();
        new File(Config.DATAPACK_ROOT, "data/geodata").mkdirs();

        HtmCache.getInstance();
        CrestCache.getInstance();
        ScriptEngineManager.getInstance();

        Util.printSection("World");
        World.getInstance();
        MapRegionData.getInstance();
        Announcements.getInstance();
        AutoAnnouncementHandler.getInstance();
        GlobalVariablesManager.getInstance();
        StaticObjectData.getInstance();
        TeleportLocationTable.getInstance();
        PartyMatchWaitingList.getInstance();
        PartyMatchRoomList.getInstance();
        GameTimeController.getInstance();
        CharNameTable.getInstance();
        ExperienceData.getInstance();
        DuelManager.getInstance();

        Util.printSection("Players");
        PlayerTemplateData.getInstance();
        if (Config.ENABLE_CLASS_DAMAGE_SETTINGS) {
            ClassDamageManager.loadConfig();
        }
        ClanTable.getInstance();
        if (Config.ENABLE_COMMUNITY_BOARD) {
            ForumsBBSManager.getInstance().initRoot();
        }

        Util.printSection("Skills");
        if (!SkillTable.getInstance().isInitialized()) {
            LOGGER.info("Could not find the extraced files. Please Check Your Data.");
            throw new Exception("Could not initialize the skill table");
        }
        SkillTreeTable.getInstance();
        SkillSpellbookTable.getInstance();
        NobleSkillTable.getInstance();
        HeroSkillTable.getInstance();
        if (!HelperBuffTable.getInstance().isInitialized()) {
            throw new Exception("Could not initialize the Helper Buff Table.");
        }
        LOGGER.info("Skills: All skills loaded.");

        Util.printSection("Items");
        ItemTable.getInstance();
        ArmorSetData.getInstance();
        ExtractableItemData.getInstance();
        SummonItemData.getInstance();
        HennaData.getInstance();
        if (Config.ALLOWFISHING) {
            FishData.getInstance();
        }

        Util.printSection("Npc");
        SchemeBufferTable.getInstance();
        WalkerRouteData.getInstance();
        if (!NpcTable.getInstance().isInitialized()) {
            LOGGER.info("Could not find the extracted files. Please Check Your Data.");
            throw new Exception("Could not initialize the npc table");
        }

        Util.printSection("Geodata");
        GeoEngine.getInstance();

        Util.printSection("Economy");
        TradeController.getInstance();
        MultisellData.getInstance();

        Util.printSection("Clan Halls");
        ClanHallManager.getInstance();
        FortressOfResistance.getInstance();
        DevastatedCastle.getInstance();
        BanditStrongholdSiege.getInstance();
        AuctionManager.getInstance();

        Util.printSection("Zone");
        ZoneData.getInstance();

        Util.printSection("Spawnlist");
        if (!Config.ALT_DEV_NO_SPAWNS) {
            SpawnTable.getInstance();
        } else {
            LOGGER.info("Spawn: disable load.");
        }
        if (!Config.ALT_DEV_NO_RB) {
            RaidBossSpawnManager.getInstance();
            GrandBossManager.getInstance();
            RaidBossPointsManager.init();
        } else {
            LOGGER.info("RaidBoss: disable load.");
        }
        DayNightSpawnManager.getInstance().notifyChangeMode();

        Util.printSection("Dimensional Rift");
        DimensionalRiftManager.getInstance();

        Util.printSection("Misc");
        RecipeData.getInstance();
        RecipeController.getInstance();
        EventDroplist.getInstance();
        AugmentationData.getInstance();
        MonsterRace.getInstance();
        Lottery.getInstance();
        MercTicketManager.getInstance();
        PetitionManager.getInstance();
        CursedWeaponsManager.getInstance();
        TaskManager.getInstance();
        PetDataTable.getInstance();
        if (Config.SAVE_DROPPED_ITEM) {
            ItemsOnGroundManager.getInstance();
        }
        if ((Config.AUTODESTROY_ITEM_AFTER > 0) || (Config.HERB_AUTO_DESTROY_TIME > 0)) {
            ItemsAutoDestroy.getInstance();
        }

        Util.printSection("Manor");
        ManorSeedData.getInstance();
        CastleManorManager.getInstance();

        Util.printSection("Castles");
        CastleManager.getInstance();
        SiegeManager.getInstance();
        FortManager.getInstance();
        FortSiegeManager.getInstance();
        CrownManager.getInstance();

        Util.printSection("Boat");
        BoatData.getInstance();

        Util.printSection("Doors");
        DoorData.getInstance().load();
        FenceData.getInstance();

        Util.printSection("Four Sepulchers");
        FourSepulchersManager.getInstance();

        Util.printSection("Seven Signs");
        SevenSigns.getInstance();
        SevenSignsFestival.getInstance();
        AutoSpawn.getInstance();
        AutoChatHandler.getInstance();

        Util.printSection("Olympiad System");
        Olympiad.getInstance();
        Hero.getInstance();

        Util.printSection("Access Levels");
        AdminData.getInstance();

        Util.printSection("Handlers");
        ItemHandler.getInstance();
        SkillHandler.getInstance();
        AdminCommandHandler.getInstance();
        UserCommandHandler.getInstance();
        VoicedCommandHandler.getInstance();

        LOGGER.info("AutoChatHandler: Loaded " + AutoChatHandler.getInstance().size() + " handlers in total.");
        LOGGER.info("AutoSpawnHandler: Loaded " + AutoSpawn.getInstance().size() + " handlers in total.");

        Runtime.getRuntime().addShutdownHook(Shutdown.getInstance());

        // Schedule auto opening/closing doors.
        DoorData.getInstance().checkAutoOpen();

        if (Config.CUSTOM_MAIL_MANAGER_ENABLED) {
            CustomMailManager.getInstance();
        }

        Util.printSection("Scripts");
        if (!Config.ALT_DEV_NO_SCRIPT) {
            LOGGER.info("ScriptEngineManager: Loading server scripts:");
            ScriptEngineManager.getInstance().executeScriptList();
            FaenorScriptEngine.getInstance();
        } else {
            LOGGER.info("Script: disable load.");
        }

        if (Config.ALT_FISH_CHAMPIONSHIP_ENABLED) {
            FishingChampionshipManager.getInstance();
        }

        /* QUESTS */
        Util.printSection("Quests");
        if (!Config.ALT_DEV_NO_QUESTS) {
            if (QuestManager.getInstance().getQuests().size() == 0) {
                QuestManager.getInstance().reloadAllQuests();
            } else {
                QuestManager.getInstance().report();
            }
        } else {
            QuestManager.getInstance().unloadAllQuests();
        }

        Util.printSection("Game Server");

        LOGGER.info("IdFactory: Free ObjectID's remaining: " + IdManager.size());

        if (Config.ALLOW_WEDDING) {
            CoupleManager.getInstance();
        }

        if (Config.PCB_ENABLE) {
            ThreadPool.scheduleAtFixedRate(PcPoint.getInstance(), Config.PCB_INTERVAL * 1000, Config.PCB_INTERVAL * 1000);
        }

        Util.printSection("EventManager");
        EventManager.getInstance().startEventRegistration();

        if (EventManager.TVT_EVENT_ENABLED || EventManager.CTF_EVENT_ENABLED || EventManager.DM_EVENT_ENABLED) {
            if (EventManager.TVT_EVENT_ENABLED) {
                LOGGER.info("TVT Event is Enabled.");
            }
            if (EventManager.CTF_EVENT_ENABLED) {
                LOGGER.info("CTF Event is Enabled.");
            }
            if (EventManager.DM_EVENT_ENABLED) {
                LOGGER.info("DM Event is Enabled.");
            }
        } else {
            LOGGER.info("All events are Disabled.");
        }

        if ((Config.OFFLINE_TRADE_ENABLE || Config.OFFLINE_CRAFT_ENABLE) && Config.RESTORE_OFFLINERS) {
            OfflineTradeTable.restoreOfflineTraders();
        }

        Util.printSection("Protection");

        if (Config.CHECK_SKILLS_ON_ENTER) {
            LOGGER.info("Check skills on enter actived.");
        }

        if (Config.CHECK_NAME_ON_LOGIN) {
            LOGGER.info("Check bad name on enter actived.");
        }

        if (Config.PROTECTED_ENCHANT) {
            LOGGER.info("Check OverEnchant items on enter actived.");
        }

        if (Config.BYPASS_VALIDATION) {
            LOGGER.info("Bypass Validation actived.");
        }

        if (Config.L2WALKER_PROTECTION) {
            LOGGER.info("L2Walker protection actived.");
        }

        if (Config.SERVER_RESTART_SCHEDULE_ENABLED) {
            ServerRestartManager.getInstance();
        }

        System.gc();

        Util.printSection("Info");
        LOGGER.info("Maximum Numbers of Connected Players: " + Config.MAXIMUM_ONLINE_USERS);
        LOGGER.info("GameServer Started, free memory " + (((Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory()) + Runtime.getRuntime().freeMemory()) / 1048576) + " Mb of " + (Runtime.getRuntime().maxMemory() / 1048576) + " Mb");
        LOGGER.info("Used memory: " + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576) + " MB");

        Util.printSection("Status");
        LOGGER.info("Server Loaded in " + ((System.currentTimeMillis() - serverLoadStart) / 1000) + " seconds.");

        // Load telnet status
        Util.printSection("Telnet");
        if (Config.IS_TELNET_ENABLED) {
            statusServer = new TelnetStatusThread();
            statusServer.start();
        } else {
            LOGGER.info("Telnet server is disabled.");
        }

        Util.printSection("Login");
        loginThread = LoginServerThread.getInstance();
        loginThread.start();

        final SelectorConfig sc = new SelectorConfig();
        sc.MAX_READ_PER_PASS = Config.MMO_MAX_READ_PER_PASS;
        sc.MAX_SEND_PER_PASS = Config.MMO_MAX_SEND_PER_PASS;
        sc.SLEEP_TIME = Config.MMO_SELECTOR_SLEEP_TIME;
        sc.HELPER_BUFFER_COUNT = Config.MMO_HELPER_BUFFER_COUNT;

        gamePacketHandler = new GamePacketHandler();

        selectorThread = new SelectorThread<>(sc, gamePacketHandler, gamePacketHandler, gamePacketHandler, new IPv4Filter());

        InetAddress bindAddress = null;
        if (!Config.GAMESERVER_HOSTNAME.equals("*")) {
            try {
                bindAddress = InetAddress.getByName(Config.GAMESERVER_HOSTNAME);
            } catch (UnknownHostException e1) {
                LOGGER.warning("The GameServer bind address is invalid, using all avaliable IPs. Reason: " + e1);
            }
        }

        try {
            selectorThread.openServerSocket(bindAddress, Config.PORT_GAME);
        } catch (IOException e) {
            LOGGER.severe("Failed to open server socket. Reason: " + e);
            System.exit(1);
        }
        selectorThread.start();
    }

    public static SelectorThread<GameClient> getSelectorThread() {
        return selectorThread;
    }

    public static void main(String[] args) throws Exception {
        INSTANCE = new GameServer();
    }

    public static GameServer getInstance() {
        return INSTANCE;
    }
}
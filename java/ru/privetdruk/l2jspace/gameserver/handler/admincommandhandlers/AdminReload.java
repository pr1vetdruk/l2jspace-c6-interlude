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
package ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;

import ru.privetdruk.l2jspace.Config;
import ru.privetdruk.l2jspace.commons.enums.ServerMode;
import ru.privetdruk.l2jspace.gameserver.TradeController;
import ru.privetdruk.l2jspace.gameserver.cache.HtmCache;
import ru.privetdruk.l2jspace.gameserver.datatables.ItemTable;
import ru.privetdruk.l2jspace.gameserver.datatables.SkillTable;
import ru.privetdruk.l2jspace.gameserver.datatables.sql.NpcTable;
import ru.privetdruk.l2jspace.gameserver.datatables.sql.TeleportLocationTable;
import ru.privetdruk.l2jspace.gameserver.datatables.xml.MultisellData;
import ru.privetdruk.l2jspace.gameserver.datatables.xml.WalkerRouteData;
import ru.privetdruk.l2jspace.gameserver.handler.IAdminCommandHandler;
import ru.privetdruk.l2jspace.gameserver.instancemanager.QuestManager;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.util.BuilderUtil;

/**
 * @author KidZor
 */
public class AdminReload implements IAdminCommandHandler {
    private static final String[] ADMIN_COMMANDS =
            {
                    "admin_reload"
            };

    @Override
    public boolean useAdminCommand(String command, PlayerInstance activeChar) {
        if (command.startsWith("admin_reload")) {
            sendReloadPage(activeChar);
            final StringTokenizer st = new StringTokenizer(command);
            st.nextToken();

            if (!st.hasMoreTokens()) {
                BuilderUtil.sendSysMessage(activeChar, "Usage:  //reload <type>");
                return false;
            }

            try {
                final String type = st.nextToken();
                if (type.equals("multisell")) {
                    MultisellData.getInstance().reload();
                    sendReloadPage(activeChar);
                    BuilderUtil.sendSysMessage(activeChar, "Multisell reloaded.");
                } else if (type.startsWith("teleport")) {
                    TeleportLocationTable.getInstance().load();
                    sendReloadPage(activeChar);
                    BuilderUtil.sendSysMessage(activeChar, "Teleport location table reloaded.");
                } else if (type.startsWith("skill")) {
                    SkillTable.getInstance().reload();
                    sendReloadPage(activeChar);
                    BuilderUtil.sendSysMessage(activeChar, "Skills reloaded.");
                } else if (type.equals("npc")) {
                    NpcTable.getInstance().reloadAllNpc();
                    sendReloadPage(activeChar);
                    BuilderUtil.sendSysMessage(activeChar, "Npcs reloaded.");
                } else if (type.startsWith("htm")) {
                    HtmCache.getInstance().reload();
                    sendReloadPage(activeChar);
                    BuilderUtil.sendSysMessage(activeChar, "Cache[HTML]: " + HtmCache.getInstance().getMemoryUsage() + " megabytes on " + HtmCache.getInstance().getLoadedFiles() + " files loaded");
                } else if (type.startsWith("item")) {
                    ItemTable.getInstance().reload();
                    sendReloadPage(activeChar);
                    BuilderUtil.sendSysMessage(activeChar, "Item templates reloaded");
                } else if (type.startsWith("npcwalkers")) {
                    WalkerRouteData.getInstance().load();
                    sendReloadPage(activeChar);
                    BuilderUtil.sendSysMessage(activeChar, "All NPC walker routes have been reloaded");
                } else if (type.startsWith("quests")) {
                    final String folder = "quests";
                    QuestManager.getInstance().reload(folder);
                    sendReloadPage(activeChar);
                    BuilderUtil.sendSysMessage(activeChar, "Quests Reloaded.");
                } else if (type.equals("configs")) {
                    Config.load(ServerMode.GAME);
                    sendReloadPage(activeChar);
                    BuilderUtil.sendSysMessage(activeChar, "Server Config Reloaded.");
                } else if (type.equals("tradelist")) {
                    TradeController.getInstance();
                    sendReloadPage(activeChar);
                    BuilderUtil.sendSysMessage(activeChar, "TradeList Table reloaded.");
                }
                BuilderUtil.sendSysMessage(activeChar, "WARNING: There are several known issues regarding this feature. Reloading server data during runtime is STRONGLY NOT RECOMMENDED for live servers, just for developing environments.");
            } catch (Exception e) {
                BuilderUtil.sendSysMessage(activeChar, "Usage:  //reload <type>");
            }
        }
        return true;
    }

    /**
     * send reload page
     *
     * @param activeChar
     */
    private void sendReloadPage(PlayerInstance activeChar) {
        AdminHelpPage.showHelpPage(activeChar, "reload_menu.htm");
    }

    @Override
    public String[] getAdminCommandList() {
        return ADMIN_COMMANDS;
    }
}

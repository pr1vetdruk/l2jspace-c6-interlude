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

import ru.privetdruk.l2jspace.commons.util.StringUtil;
import ru.privetdruk.l2jspace.gameserver.datatables.xml.MapRegionData;
import ru.privetdruk.l2jspace.gameserver.datatables.xml.ZoneData;
import ru.privetdruk.l2jspace.gameserver.handler.IAdminCommandHandler;
import ru.privetdruk.l2jspace.gameserver.model.World;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.model.zone.ZoneId;
import ru.privetdruk.l2jspace.gameserver.model.zone.ZoneType;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.NpcHtmlMessage;

public class AdminZone implements IAdminCommandHandler {
    private static final String[] ADMIN_COMMANDS =
            {
                    "admin_zone_check",
                    "admin_zone_visual"
            };

    @Override
    public boolean useAdminCommand(String command, PlayerInstance activeChar) {
        if (activeChar == null) {
            return false;
        }

        StringTokenizer st = new StringTokenizer(command, " ");
        String actualCommand = st.nextToken(); // Get actual command
        if (actualCommand.equalsIgnoreCase("admin_zone_check")) {
            showHtml(activeChar);
        } else if (actualCommand.equalsIgnoreCase("admin_zone_visual")) {
            try {
                String next = st.nextToken();
                if (next.equalsIgnoreCase("all")) {
                    for (ZoneType zone : ZoneData.getInstance().getZones(activeChar)) {
                        zone.visualizeZone(activeChar.getZ());
                    }

                    showHtml(activeChar);
                } else if (next.equalsIgnoreCase("clear")) {
                    ZoneData.getInstance().clearDebugItems();
                    showHtml(activeChar);
                } else {
                    int zoneId = Integer.parseInt(next);
                    ZoneData.getInstance().getZoneById(zoneId).visualizeZone(activeChar.getZ());
                }
            } catch (Exception e) {
                activeChar.sendMessage("Invalid parameter for //zone_visual.");
            }
        }

        return true;
    }

    private static void showHtml(PlayerInstance activeChar) {
        final int x = activeChar.getX();
        final int y = activeChar.getY();
        final int rx = ((x - World.MAP_MIN_X) >> 15) + World.TILE_X_MIN;
        final int ry = ((y - World.MAP_MIN_Y) >> 15) + World.TILE_Y_MIN;
        final NpcHtmlMessage html = new NpcHtmlMessage(0);
        html.setFile("data/html/admin/zone.htm");

        html.replaceAll("%MAPREGION%", "[x:" + MapRegionData.getInstance().getMapRegionX(x) + " y:" + MapRegionData.getInstance().getMapRegionY(y) + "]");
        html.replaceAll("%GEOREGION%", rx + "_" + ry);
        html.replaceAll("%CLOSESTTOWN%", MapRegionData.getInstance().getClosestTownName(activeChar));
        html.replaceAll("%CURRENTLOC%", x + ", " + y + ", " + activeChar.getZ());
        html.replaceAll("%PVP%", (activeChar.isInsideZone(ZoneId.PVP) ? "<font color=\"LEVEL\">YES</font>" : "NO"));
        html.replaceAll("%PEACE%", (activeChar.isInsideZone(ZoneId.PEACE) ? "<font color=\"LEVEL\">YES</font>" : "NO"));
        html.replaceAll("%SIEGE%", (activeChar.isInsideZone(ZoneId.SIEGE) ? "<font color=\"LEVEL\">YES</font>" : "NO"));
        html.replaceAll("%MOTHERTREE%", (activeChar.isInsideZone(ZoneId.MOTHERTREE) ? "<font color=\"LEVEL\">YES</font>" : "NO"));
        html.replaceAll("%CLANHALL%", (activeChar.isInsideZone(ZoneId.CLAN_HALL) ? "<font color=\"LEVEL\">YES</font>" : "NO"));
        html.replaceAll("%NOLANDING%", (activeChar.isInsideZone(ZoneId.NO_LANDING) ? "<font color=\"LEVEL\">YES</font>" : "NO"));
        html.replaceAll("%WATER%", (activeChar.isInsideZone(ZoneId.WATER) ? "<font color=\"LEVEL\">YES</font>" : "NO"));
        html.replaceAll("%JAIL%", (activeChar.isInsideZone(ZoneId.JAIL) ? "<font color=\"LEVEL\">YES</font>" : "NO"));
        html.replaceAll("%MONSTERTRACK%", (activeChar.isInsideZone(ZoneId.MONSTER_TRACK) ? "<font color=\"LEVEL\">YES</font>" : "NO"));
        html.replaceAll("%CASTLE%", (activeChar.isInsideZone(ZoneId.CASTLE) ? "<font color=\"LEVEL\">YES</font>" : "NO"));
        html.replaceAll("%SWAMP%", (activeChar.isInsideZone(ZoneId.SWAMP) ? "<font color=\"LEVEL\">YES</font>" : "NO"));
        html.replaceAll("%NOSUMMONFRIEND%", (activeChar.isInsideZone(ZoneId.NO_SUMMON_FRIEND) ? "<font color=\"LEVEL\">YES</font>" : "NO"));
        html.replaceAll("%NOSTORE%", (activeChar.isInsideZone(ZoneId.NO_STORE) ? "<font color=\"LEVEL\">YES</font>" : "NO"));
        html.replaceAll("%TOWN%", (activeChar.isInsideZone(ZoneId.TOWN) ? "<font color=\"LEVEL\">YES</font>" : "NO"));
        html.replaceAll("%HQ%", (activeChar.isInsideZone(ZoneId.HQ) ? "<font color=\"LEVEL\">YES</font>" : "NO"));
        html.replaceAll("%DANGERAREA%", (activeChar.isInsideZone(ZoneId.DANGER_AREA) ? "<font color=\"LEVEL\">YES</font>" : "NO"));
        html.replaceAll("%BOSS%", (activeChar.isInsideZone(ZoneId.BOSS) ? "<font color=\"LEVEL\">YES</font>" : "NO"));
        html.replaceAll("%NORESTART%", (activeChar.isInsideZone(ZoneId.NO_RESTART) ? "<font color=\"LEVEL\">YES</font>" : "NO"));

        final StringBuilder sb = new StringBuilder(100);
        for (ZoneType zone : World.getInstance().getRegion(x, y).getZones()) {
            if (zone.isCharacterInZone(activeChar)) {
                StringUtil.append(sb, zone.getId(), " ");
            }
        }
        html.replaceAll("%ZLIST%", sb.toString());
        activeChar.sendPacket(html);
    }

    @Override
    public String[] getAdminCommandList() {
        return ADMIN_COMMANDS;
    }
}
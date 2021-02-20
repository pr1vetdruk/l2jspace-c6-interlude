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

import ru.privetdruk.l2jspace.gameserver.geoengine.GeoEngine;
import ru.privetdruk.l2jspace.gameserver.handler.IAdminCommandHandler;
import ru.privetdruk.l2jspace.gameserver.model.World;
import ru.privetdruk.l2jspace.gameserver.model.WorldObject;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.util.BuilderUtil;
import ru.privetdruk.l2jspace.gameserver.util.GeoUtils;

/**
 * @author -Nemesiss-
 */
public class AdminGeodata implements IAdminCommandHandler {
    private static final String[] ADMIN_COMMANDS =
            {
                    "admin_geo_pos",
                    "admin_geo_spawn_pos",
                    "admin_geo_can_move",
                    "admin_geo_can_see",
                    "admin_geogrid",
                    "admin_geomap"
            };

    @Override
    public boolean useAdminCommand(String command, PlayerInstance activeChar) {
        if (command.equals("admin_geo_pos")) {
            final int worldX = activeChar.getX();
            final int worldY = activeChar.getY();
            final int worldZ = activeChar.getZ();
            final int geoX = GeoEngine.getInstance().getGeoX(worldX);
            final int geoY = GeoEngine.getInstance().getGeoY(worldY);
            if (GeoEngine.getInstance().hasGeoPos(geoX, geoY)) {
                BuilderUtil.sendSysMessage(activeChar, "WorldX: " + worldX + ", WorldY: " + worldY + ", WorldZ: " + worldZ + ", GeoX: " + geoX + ", GeoY: " + geoY + ", GeoZ: " + GeoEngine.getInstance().getHeight(worldX, worldY, worldZ));
            } else {
                BuilderUtil.sendSysMessage(activeChar, "There is no geodata at this position.");
            }
        } else if (command.equals("admin_geo_spawn_pos")) {
            final int worldX = activeChar.getX();
            final int worldY = activeChar.getY();
            final int worldZ = activeChar.getZ();
            final int geoX = GeoEngine.getInstance().getGeoX(worldX);
            final int geoY = GeoEngine.getInstance().getGeoY(worldY);
            if (GeoEngine.getInstance().hasGeoPos(geoX, geoY)) {
                BuilderUtil.sendSysMessage(activeChar, "WorldX: " + worldX + ", WorldY: " + worldY + ", WorldZ: " + worldZ + ", GeoX: " + geoX + ", GeoY: " + geoY + ", GeoZ: " + GeoEngine.getInstance().getHeight(worldX, worldY, worldZ));
            } else {
                BuilderUtil.sendSysMessage(activeChar, "There is no geodata at this position.");
            }
        } else if (command.equals("admin_geo_can_move")) {
            final WorldObject target = activeChar.getTarget();
            if (target != null) {
                if (GeoEngine.getInstance().canSeeTarget(activeChar, target)) {
                    BuilderUtil.sendSysMessage(activeChar, "Can move beeline.");
                } else {
                    BuilderUtil.sendSysMessage(activeChar, "Can not move beeline!");
                }
            } else {
                BuilderUtil.sendSysMessage(activeChar, "Incorrect Target.");
            }
        } else if (command.equals("admin_geo_can_see")) {
            final WorldObject target = activeChar.getTarget();
            if (target != null) {
                if (GeoEngine.getInstance().canSeeTarget(activeChar, target)) {
                    BuilderUtil.sendSysMessage(activeChar, "Can see target.");
                } else {
                    BuilderUtil.sendSysMessage(activeChar, "Cannot see Target.");
                }
            } else {
                BuilderUtil.sendSysMessage(activeChar, "Incorrect Target.");
            }
        } else if (command.equals("admin_geogrid")) {
            GeoUtils.debugGrid(activeChar);
        } else if (command.equals("admin_geomap")) {
            final int x = ((activeChar.getX() - World.MAP_MIN_X) >> 15) + World.TILE_X_MIN;
            final int y = ((activeChar.getY() - World.MAP_MIN_Y) >> 15) + World.TILE_Y_MIN;
            BuilderUtil.sendSysMessage(activeChar, "GeoMap: " + x + "_" + y);
        } else {
            return false;
        }
        return true;
    }

    @Override
    public String[] getAdminCommandList() {
        return ADMIN_COMMANDS;
    }
}
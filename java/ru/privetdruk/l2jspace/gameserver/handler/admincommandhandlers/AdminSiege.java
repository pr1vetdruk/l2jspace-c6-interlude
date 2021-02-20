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

import ru.privetdruk.l2jspace.gameserver.datatables.sql.ClanTable;
import ru.privetdruk.l2jspace.gameserver.handler.IAdminCommandHandler;
import ru.privetdruk.l2jspace.gameserver.instancemanager.AuctionManager;
import ru.privetdruk.l2jspace.gameserver.instancemanager.CastleManager;
import ru.privetdruk.l2jspace.gameserver.instancemanager.ClanHallManager;
import ru.privetdruk.l2jspace.gameserver.instancemanager.SiegeManager;
import ru.privetdruk.l2jspace.gameserver.model.WorldObject;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.model.clan.Clan;
import ru.privetdruk.l2jspace.gameserver.model.entity.ClanHall;
import ru.privetdruk.l2jspace.gameserver.model.entity.siege.Castle;
import ru.privetdruk.l2jspace.gameserver.model.zone.type.ClanHallZone;
import ru.privetdruk.l2jspace.gameserver.network.SystemMessageId;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.NpcHtmlMessage;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.SystemMessage;
import ru.privetdruk.l2jspace.gameserver.util.BuilderUtil;

/**
 * This class handles all siege commands: TODO: change the class name, and neaten it up
 */
public class AdminSiege implements IAdminCommandHandler {
    private static final String[] ADMIN_COMMANDS =
            {
                    "admin_siege",
                    "admin_add_attacker",
                    "admin_add_defender",
                    "admin_add_guard",
                    "admin_list_siege_clans",
                    "admin_clear_siege_list",
                    "admin_move_defenders",
                    "admin_spawn_doors",
                    "admin_endsiege",
                    "admin_startsiege",
                    "admin_setcastle",
                    "admin_removecastle",
                    "admin_clanhall",
                    "admin_clanhallset",
                    "admin_clanhalldel",
                    "admin_clanhallopendoors",
                    "admin_clanhallclosedoors",
                    "admin_clanhallteleportself"
            };

    @SuppressWarnings("null")
    @Override
    public boolean useAdminCommand(String commandValue, PlayerInstance activeChar) {
        String command = commandValue;
        final StringTokenizer st = new StringTokenizer(command, " ");
        command = st.nextToken(); // Get actual command

        // Get castle
        Castle castle = null;
        ClanHall clanhall = null;
        if (command.startsWith("admin_clanhall")) {
            clanhall = ClanHallManager.getInstance().getClanHallById(Integer.parseInt(st.nextToken()));
        } else if (st.hasMoreTokens()) {
            castle = CastleManager.getInstance().getCastle(st.nextToken());
        }

        // Get castle
        String val = "";
        if (st.hasMoreTokens()) {
            val = st.nextToken();
        }

        if (((castle == null) || (castle.getCastleId() < 0)) && (clanhall == null)) {
            // No castle specified
            showCastleSelectPage(activeChar);
        } else {
            final WorldObject target = activeChar.getTarget();
            PlayerInstance player = null;
            if (target instanceof PlayerInstance) {
                player = (PlayerInstance) target;
            }

            if (command.equalsIgnoreCase("admin_add_attacker")) {
                if (player == null) {
                    activeChar.sendPacket(SystemMessageId.THAT_IS_THE_INCORRECT_TARGET);
                } else if (SiegeManager.getInstance().checkIsRegistered(player.getClan(), castle.getCastleId())) {
                    BuilderUtil.sendSysMessage(activeChar, "Clan is already registered!");
                } else {
                    castle.getSiege().registerAttacker(player, true);
                }
            } else if (command.equalsIgnoreCase("admin_add_defender")) {
                if (player == null) {
                    activeChar.sendPacket(SystemMessageId.THAT_IS_THE_INCORRECT_TARGET);
                } else {
                    castle.getSiege().registerDefender(player, true);
                }
            } else if (command.equalsIgnoreCase("admin_add_guard")) {
                try {
                    final int npcId = Integer.parseInt(val);
                    castle.getSiege().getSiegeGuardManager().addSiegeGuard(activeChar, npcId);
                } catch (Exception e) {
                    BuilderUtil.sendSysMessage(activeChar, "Usage: //add_guard npcId");
                }
            } else if (command.equalsIgnoreCase("admin_clear_siege_list")) {
                castle.getSiege().clearSiegeClan();
            } else if (command.equalsIgnoreCase("admin_endsiege")) {
                castle.getSiege().endSiege();
            } else if (command.equalsIgnoreCase("admin_list_siege_clans")) {
                castle.getSiege().listRegisterClan(activeChar);

                return true;
            } else if (command.equalsIgnoreCase("admin_move_defenders")) {
                activeChar.sendPacket(SystemMessage.sendString("Not implemented yet."));
            } else if (command.equalsIgnoreCase("admin_setcastle")) {
                if ((player == null) || (player.getClan() == null)) {
                    activeChar.sendPacket(SystemMessageId.THAT_IS_THE_INCORRECT_TARGET);
                } else {
                    castle.setOwner(player.getClan());
                }
            } else if (command.equalsIgnoreCase("admin_removecastle")) {
                final Clan clan = ClanTable.getInstance().getClan(castle.getOwnerId());
                if (clan != null) {
                    castle.removeOwner(clan);
                } else {
                    BuilderUtil.sendSysMessage(activeChar, "Unable to remove castle");
                }
            } else if (command.equalsIgnoreCase("admin_clanhallset")) {
                if ((player == null) || (player.getClan() == null)) {
                    activeChar.sendPacket(SystemMessageId.THAT_IS_THE_INCORRECT_TARGET);
                } else if (!ClanHallManager.getInstance().isFree(clanhall.getId())) {
                    BuilderUtil.sendSysMessage(activeChar, "This ClanHall isn't free!");
                } else if (player.getClan().getHideoutId() == 0) {
                    ClanHallManager.getInstance().setOwner(clanhall.getId(), player.getClan());
                    if (AuctionManager.getInstance().getAuction(clanhall.getId()) != null) {
                        AuctionManager.getInstance().getAuction(clanhall.getId()).deleteAuctionFromDB();
                    }
                } else {
                    BuilderUtil.sendSysMessage(activeChar, "You have already a ClanHall!");
                }
            } else if (command.equalsIgnoreCase("admin_clanhalldel")) {
                if (!ClanHallManager.getInstance().isFree(clanhall.getId())) {
                    ClanHallManager.getInstance().setFree(clanhall.getId());
                    AuctionManager.getInstance().initNPC(clanhall.getId());
                } else {
                    BuilderUtil.sendSysMessage(activeChar, "This ClanHall is already Free!");
                }
            } else if (command.equalsIgnoreCase("admin_clanhallopendoors")) {
                clanhall.openCloseDoors(true);
            } else if (command.equalsIgnoreCase("admin_clanhallclosedoors")) {
                clanhall.openCloseDoors(false);
            } else if (command.equalsIgnoreCase("admin_clanhallteleportself")) {
                final ClanHallZone zone = clanhall.getZone();
                if (zone != null) {
                    activeChar.teleToLocation(zone.getSpawnLoc(), true);
                }
            } else if (command.equalsIgnoreCase("admin_spawn_doors")) {
                castle.spawnDoor();
            } else if (command.equalsIgnoreCase("admin_startsiege")) {
                castle.getSiege().startSiege();
            }

            if (clanhall != null) {
                showClanHallPage(activeChar, clanhall);
            } else {
                showSiegePage(activeChar, castle.getName());
            }
        }

        return true;
    }

    private void showCastleSelectPage(PlayerInstance activeChar) {
        int i = 0;

        final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
        adminReply.setFile("data/html/admin/castles.htm");
        StringBuilder cList = new StringBuilder();
        for (Castle castle : CastleManager.getInstance().getCastles()) {
            if (castle != null) {
                final String name = castle.getName();
                cList.append("<td fixwidth=90><a action=\"bypass -h admin_siege " + name + "\">" + name + "</a></td>");
                i++;
            }

            if (i > 2) {
                cList.append("</tr><tr>");
                i = 0;
            }
        }

        adminReply.replace("%castles%", cList.toString());
        cList = new StringBuilder();
        i = 0;
        for (ClanHall clanhall : ClanHallManager.getInstance().getClanHalls().values()) {
            if (clanhall != null) {
                cList.append("<td fixwidth=134><a action=\"bypass -h admin_clanhall " + clanhall.getId() + "\">");
                cList.append(clanhall.getName() + "</a></td>");
                i++;
            }

            if (i > 1) {
                cList.append("</tr><tr>");
                i = 0;
            }
        }

        adminReply.replace("%clanhalls%", cList.toString());
        cList = new StringBuilder();
        i = 0;
        for (ClanHall clanhall : ClanHallManager.getInstance().getFreeClanHalls().values()) {
            if (clanhall != null) {
                cList.append("<td fixwidth=134><a action=\"bypass -h admin_clanhall " + clanhall.getId() + "\">");
                cList.append(clanhall.getName() + "</a></td>");
                i++;
            }

            if (i > 1) {
                cList.append("</tr><tr>");
                i = 0;
            }
        }
        adminReply.replace("%freeclanhalls%", cList.toString());
        activeChar.sendPacket(adminReply);
    }

    private void showSiegePage(PlayerInstance activeChar, String castleName) {
        final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
        adminReply.setFile("data/html/admin/castle.htm");
        adminReply.replace("%castleName%", castleName);
        activeChar.sendPacket(adminReply);
    }

    private void showClanHallPage(PlayerInstance activeChar, ClanHall clanhall) {
        final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
        adminReply.setFile("data/html/admin/clanhall.htm");
        adminReply.replace("%clanhallName%", clanhall.getName());
        adminReply.replace("%clanhallId%", String.valueOf(clanhall.getId()));
        final Clan owner = ClanTable.getInstance().getClan(clanhall.getOwnerId());
        if (owner == null) {
            adminReply.replace("%clanhallOwner%", "None");
        } else {
            adminReply.replace("%clanhallOwner%", owner.getName());
        }

        activeChar.sendPacket(adminReply);
    }

    @Override
    public String[] getAdminCommandList() {
        return ADMIN_COMMANDS;
    }
}

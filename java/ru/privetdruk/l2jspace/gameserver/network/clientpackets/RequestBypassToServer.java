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
package ru.privetdruk.l2jspace.gameserver.network.clientpackets;

import java.util.logging.Logger;

import ru.privetdruk.l2jspace.Config;
import ru.privetdruk.l2jspace.gameserver.ai.CtrlIntention;
import ru.privetdruk.l2jspace.gameserver.communitybbs.CommunityBoard;
import ru.privetdruk.l2jspace.gameserver.datatables.xml.AdminData;
import ru.privetdruk.l2jspace.gameserver.handler.AdminCommandHandler;
import ru.privetdruk.l2jspace.gameserver.handler.IAdminCommandHandler;
import ru.privetdruk.l2jspace.gameserver.model.Location;
import ru.privetdruk.l2jspace.gameserver.model.World;
import ru.privetdruk.l2jspace.gameserver.model.WorldObject;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.ClassMasterInstance;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.NpcInstance;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.SymbolMakerInstance;
import ru.privetdruk.l2jspace.gameserver.model.entity.Rebirth;
import ru.privetdruk.l2jspace.gameserver.model.entity.event.DM;
import ru.privetdruk.l2jspace.gameserver.model.entity.event.GameEvent;
import ru.privetdruk.l2jspace.gameserver.model.entity.event.TvT;
import ru.privetdruk.l2jspace.gameserver.model.entity.event.VIP;
import ru.privetdruk.l2jspace.gameserver.model.entity.event.core.State;
import ru.privetdruk.l2jspace.gameserver.model.entity.event.ctf.CTF;
import ru.privetdruk.l2jspace.gameserver.model.entity.olympiad.Olympiad;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.ActionFailed;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.NpcHtmlMessage;
import ru.privetdruk.l2jspace.gameserver.util.GMAudit;

public class RequestBypassToServer extends GameClientPacket {
    private static final Logger LOGGER = Logger.getLogger(RequestBypassToServer.class.getName());

    // S
    private String _command;

    @Override
    protected void readImpl() {
        _command = readS();
    }

    @Override
    protected void runImpl() {
        final PlayerInstance player = getClient().getPlayer();
        if (player == null) {
            return;
        }

        if (!getClient().getFloodProtectors().getServerBypass().tryPerformAction(_command)) {
            return;
        }

        try {
            if (_command.startsWith("admin_")) {
                if (!player.isGM()) {
                    return;
                }

                String command;
                if (_command.contains(" ")) {
                    command = _command.substring(0, _command.indexOf(' '));
                } else {
                    command = _command;
                }

                final IAdminCommandHandler ach = AdminCommandHandler.getInstance().getAdminCommandHandler(command);
                if (ach == null) {
                    player.sendMessage("The command " + command + " does not exists!");
                    LOGGER.warning("No handler registered for admin command '" + command + "'");
                    return;
                }

                if (!AdminData.getInstance().hasAccess(command, player.getAccessLevel())) {
                    player.sendMessage("You don't have the access right to use this command!");
                    return;
                }

                if (Config.GMAUDIT) {
                    GMAudit.auditGMAction(player.getName() + " [" + player.getObjectId() + "]", command, (player.getTarget() != null ? player.getTarget().getName() : "no-target"), _command.replace(command, ""));
                }

                ach.useAdminCommand(_command, player);
            } else if (_command.equals("come_here") && player.isGM()) {
                final WorldObject obj = player.getTarget();
                if (obj == null) {
                    return;
                }

                if (obj instanceof NpcInstance) {
                    final NpcInstance npc = (NpcInstance) obj;
                    npc.setTarget(player);
                    npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(player.getX(), player.getY(), player.getZ(), 0));
                }
            } else if (_command.startsWith("player_help ")) {
                final String path = _command.substring(12);
                if (path.contains("..")) {
                    return;
                }

                final String filename = "data/html/help/" + path;
                final NpcHtmlMessage html = new NpcHtmlMessage(1);
                html.setFile(filename);
                player.sendPacket(html);
            } else if (_command.startsWith("npc_")) {
                if (!player.validateBypass(_command)) {
                    return;
                }

                String id;
                final int endOfId = _command.indexOf('_', 5);
                if (endOfId > 0) {
                    id = _command.substring(4, endOfId);
                } else {
                    id = _command.substring(4);
                }

                try {
                    String command = _command.substring(endOfId + 1);

                    if (command.startsWith("event_participate")) {
                        GameEvent.inscribePlayer(player);
                    } else if (command.startsWith("tvt_player_join ")) {
                        final String teamName = command.substring(16);
                        if (TvT.isJoining()) {
                            TvT.addPlayer(player, teamName);
                        } else {
                            player.sendMessage("The event is already started. You can not join now!");
                        }
                    } else if (command.startsWith("tvt_player_leave")) {
                        if (TvT.isJoining()) {
                            TvT.removePlayer(player);
                        } else {
                            player.sendMessage("The event is already started. You can not leave now!");
                        }
                    } else if (command.startsWith("dmevent_player_join")) {
                        if (DM.isJoining()) {
                            DM.addPlayer(player);
                        } else {
                            player.sendMessage("The event is already started. You can't join now!");
                        }
                    } else if (command.startsWith("dmevent_player_leave")) {
                        if (DM.isJoining()) {
                            DM.removePlayer(player);
                        } else {
                            player.sendMessage("The event is already started. You can't leave now!");
                        }
                    } else if (command.startsWith("ctf_")) {
                        CTF ctf = CTF.find(State.REGISTRATION);

                        if (ctf != null) {
                            if (command.startsWith("ctf_player_join ")) {
                                String teamName = command.substring(16);
                                ctf.registerPlayer(player, teamName);
                            } else if (command.startsWith("ctf_player_leave")) {
                                ctf.excludePlayer(player);
                            }
                        } else {
                            player.sendMessage("The event is already started. You can't join now!");
                        }
                    }

                    if (command.startsWith("vip_joinVIPTeam")) {
                        VIP.addPlayerVIP(player);
                    }

                    if (command.startsWith("vip_joinNotVIPTeam")) {
                        VIP.addPlayerNotVIP(player);
                    }

                    if (command.startsWith("vip_finishVIP")) {
                        VIP.vipWin(player);
                    }

                    if (command.startsWith("event_participate")) {
                        GameEvent.inscribePlayer(player);
                    }

                    final WorldObject object = World.getInstance().findObject(Integer.parseInt(id));
                    if ((Config.ALLOW_CLASS_MASTERS && Config.ALLOW_REMOTE_CLASS_MASTERS && (object instanceof ClassMasterInstance)) //
                            || ((object instanceof NpcInstance) && (endOfId > 0) && player.isInsideRadius(object, NpcInstance.INTERACTION_DISTANCE, false, false))) {
                        ((NpcInstance) object).onBypassFeedback(player, _command.replace("npc_" + object.getObjectId() + "_", ""));
                    }

                    player.sendPacket(ActionFailed.STATIC_PACKET);
                } catch (NumberFormatException nfe) {
                }
            }
            // Draw a Symbol
            else if (_command.equals("Draw")) {
                final WorldObject object = player.getTarget();
                if (object instanceof NpcInstance) {
                    ((SymbolMakerInstance) object).onBypassFeedback(player, _command);
                }
            } else if (_command.equals("RemoveList")) {
                final WorldObject object = player.getTarget();
                if (object instanceof NpcInstance) {
                    ((SymbolMakerInstance) object).onBypassFeedback(player, _command);
                }
            } else if (_command.equals("Remove ")) {
                final WorldObject object = player.getTarget();
                if (object instanceof NpcInstance) {
                    ((SymbolMakerInstance) object).onBypassFeedback(player, _command);
                }
            }
            // Navigate throught Manor windows
            else if (_command.startsWith("manor_menu_select?")) {
                final WorldObject object = player.getTarget();
                if (object instanceof NpcInstance) {
                    ((NpcInstance) object).onBypassFeedback(player, _command);
                }
            } else if (_command.startsWith("bbs_") || _command.startsWith("_bbs") || _command.startsWith("_friend") || _command.startsWith("_mail") || _command.startsWith("_block")) {
                CommunityBoard.getInstance().handleCommands(getClient(), _command);
            } else if (_command.startsWith("Quest ")) {
                if (!player.validateBypass(_command)) {
                    return;
                }

                final String p = _command.substring(6).trim();
                final int idx = p.indexOf(' ');
                if (idx < 0) {
                    player.processQuestEvent(p, "");
                } else {
                    final WorldObject object = player.getTarget();
                    if ((object instanceof NpcInstance) && (player.getLastQuestNpcObject() != object.getObjectId())) {
                        final WorldObject lastQuestNpc = World.getInstance().findObject(player.getLastQuestNpcObject());
                        if ((lastQuestNpc == null) || !player.isInsideRadius(lastQuestNpc, NpcInstance.INTERACTION_DISTANCE, false, false)) {
                            player.setLastQuestNpcObject(object.getObjectId());
                        }
                    }
                    player.processQuestEvent(p.substring(0, idx), p.substring(idx).trim());
                }
            } else if (_command.startsWith("OlympiadArenaChange")) {
                Olympiad.bypassChangeArena(_command, player);
            } else if (_command.startsWith("custom_rebirth_")) {
                Rebirth.getInstance().handleCommand(player, _command);
            }
        } catch (Exception e) {
            LOGGER.warning("Bad RequestBypassToServer: " + e);
        }
    }
}

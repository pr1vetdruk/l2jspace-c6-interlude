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
package ru.privetdruk.l2jspace.gameserver.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminAdmin;
import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminAio;
import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminAnnouncements;
import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminBan;
import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminBuffs;
import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminCTFEngine;
import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminCache;
import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminChangeAccessLevel;
import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminChristmas;
import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminCreateItem;
import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminCursedWeapons;
import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminDMEngine;
import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminDelete;
import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminDestroyItems;
import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminDonator;
import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminDoorControl;
import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminEditChar;
import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminEditNpc;
import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminEffects;
import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminEnchant;
import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminEventEngine;
import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminExpSp;
import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminFence;
import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminFightCalculator;
import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminFortSiege;
import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminGeodata;
import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminGm;
import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminGmChat;
import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminGmSpeed;
import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminHeal;
import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminHelpPage;
import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminHide;
import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminInvul;
import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminKick;
import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminKill;
import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminLevel;
import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminLogin;
import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminMammon;
import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminManor;
import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminMassControl;
import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminMassRecall;
import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminMenu;
import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminMobGroup;
import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminNoble;
import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminOnline;
import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminPForge;
import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminPetition;
import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminPledge;
import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminQuest;
import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminReload;
import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminRepairChar;
import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminRes;
import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminRideWyvern;
import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminScript;
import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminShop;
import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminShutdown;
import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminSiege;
import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminSkill;
import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminSpawn;
import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminSuperHaste;
import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminTarget;
import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminTeleport;
import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminTest;
import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminTownWar;
import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminTvTEngine;
import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminVIPEngine;
import ru.privetdruk.l2jspace.gameserver.handler.admincommandhandlers.AdminZone;

/**
 * @version $Revision: 1.1.4.5 $ $Date: 2005/03/27 15:30:09 $
 */
public class AdminCommandHandler {
    protected static final Logger LOGGER = Logger.getLogger(AdminCommandHandler.class.getName());

    private final Map<String, IAdminCommandHandler> _datatable;

    private AdminCommandHandler() {
        _datatable = new HashMap<>();
        registerAdminCommandHandler(new AdminAdmin());
        registerAdminCommandHandler(new AdminAio());
        registerAdminCommandHandler(new AdminAnnouncements());
        registerAdminCommandHandler(new AdminBan());
        registerAdminCommandHandler(new AdminBuffs());
        registerAdminCommandHandler(new AdminCache());
        registerAdminCommandHandler(new AdminChangeAccessLevel());
        registerAdminCommandHandler(new AdminChristmas());
        registerAdminCommandHandler(new AdminCreateItem());
        registerAdminCommandHandler(new AdminCTFEngine());
        registerAdminCommandHandler(new AdminCursedWeapons());
        registerAdminCommandHandler(new AdminDelete());
        registerAdminCommandHandler(new AdminDestroyItems());
        registerAdminCommandHandler(new AdminDMEngine());
        registerAdminCommandHandler(new AdminDonator());
        registerAdminCommandHandler(new AdminDoorControl());
        registerAdminCommandHandler(new AdminEditChar());
        registerAdminCommandHandler(new AdminEditNpc());
        registerAdminCommandHandler(new AdminEffects());
        registerAdminCommandHandler(new AdminEnchant());
        registerAdminCommandHandler(new AdminEventEngine());
        registerAdminCommandHandler(new AdminExpSp());
        registerAdminCommandHandler(new AdminFence());
        registerAdminCommandHandler(new AdminFightCalculator());
        registerAdminCommandHandler(new AdminFortSiege());
        registerAdminCommandHandler(new AdminGeodata());
        registerAdminCommandHandler(new AdminGm());
        registerAdminCommandHandler(new AdminGmChat());
        registerAdminCommandHandler(new AdminGmSpeed());
        registerAdminCommandHandler(new AdminHeal());
        registerAdminCommandHandler(new AdminHelpPage());
        registerAdminCommandHandler(new AdminHide());
        registerAdminCommandHandler(new AdminInvul());
        registerAdminCommandHandler(new AdminKick());
        registerAdminCommandHandler(new AdminKill());
        registerAdminCommandHandler(new AdminLevel());
        registerAdminCommandHandler(new AdminLogin());
        registerAdminCommandHandler(new AdminMammon());
        registerAdminCommandHandler(new AdminManor());
        registerAdminCommandHandler(new AdminMassControl());
        registerAdminCommandHandler(new AdminMassRecall());
        registerAdminCommandHandler(new AdminMenu());
        registerAdminCommandHandler(new AdminMobGroup());
        registerAdminCommandHandler(new AdminNoble());
        registerAdminCommandHandler(new AdminOnline());
        registerAdminCommandHandler(new AdminPetition());
        registerAdminCommandHandler(new AdminPForge());
        registerAdminCommandHandler(new AdminPledge());
        registerAdminCommandHandler(new AdminQuest());
        registerAdminCommandHandler(new AdminReload());
        registerAdminCommandHandler(new AdminRepairChar());
        registerAdminCommandHandler(new AdminRes());
        registerAdminCommandHandler(new AdminRideWyvern());
        registerAdminCommandHandler(new AdminScript());
        registerAdminCommandHandler(new AdminShop());
        registerAdminCommandHandler(new AdminShutdown());
        registerAdminCommandHandler(new AdminSiege());
        registerAdminCommandHandler(new AdminSkill());
        registerAdminCommandHandler(new AdminSpawn());
        registerAdminCommandHandler(new AdminSuperHaste());
        registerAdminCommandHandler(new AdminTarget());
        registerAdminCommandHandler(new AdminTeleport());
        registerAdminCommandHandler(new AdminTest());
        registerAdminCommandHandler(new AdminTownWar());
        registerAdminCommandHandler(new AdminTvTEngine());
        registerAdminCommandHandler(new AdminVIPEngine());
        registerAdminCommandHandler(new AdminZone());

        LOGGER.info("AdminCommandHandler: Loaded " + _datatable.size() + " handlers.");
    }

    public void registerAdminCommandHandler(IAdminCommandHandler handler) {
        final String[] ids = handler.getAdminCommandList();
        for (String element : ids) {
            if (_datatable.keySet().contains(element)) {
                LOGGER.warning("Duplicated command \"" + element + "\" definition in " + handler.getClass().getName() + ".");
            } else {
                _datatable.put(element, handler);
            }
        }
    }

    public IAdminCommandHandler getAdminCommandHandler(String adminCommand) {
        String command = adminCommand;
        if (adminCommand.indexOf(' ') != -1) {
            command = adminCommand.substring(0, adminCommand.indexOf(' '));
        }
        return _datatable.get(command);
    }

    public static AdminCommandHandler getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static class SingletonHolder {
        protected static final AdminCommandHandler INSTANCE = new AdminCommandHandler();
    }
}
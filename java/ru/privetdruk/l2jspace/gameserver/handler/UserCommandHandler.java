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

import ru.privetdruk.l2jspace.Config;
import ru.privetdruk.l2jspace.gameserver.handler.usercommandhandlers.ChannelDelete;
import ru.privetdruk.l2jspace.gameserver.handler.usercommandhandlers.ChannelLeave;
import ru.privetdruk.l2jspace.gameserver.handler.usercommandhandlers.ChannelListUpdate;
import ru.privetdruk.l2jspace.gameserver.handler.usercommandhandlers.ClanPenalty;
import ru.privetdruk.l2jspace.gameserver.handler.usercommandhandlers.ClanWarsList;
import ru.privetdruk.l2jspace.gameserver.handler.usercommandhandlers.DisMount;
import ru.privetdruk.l2jspace.gameserver.handler.usercommandhandlers.Escape;
import ru.privetdruk.l2jspace.gameserver.handler.usercommandhandlers.Loc;
import ru.privetdruk.l2jspace.gameserver.handler.usercommandhandlers.Mount;
import ru.privetdruk.l2jspace.gameserver.handler.usercommandhandlers.OfflineShop;
import ru.privetdruk.l2jspace.gameserver.handler.usercommandhandlers.OlympiadStat;
import ru.privetdruk.l2jspace.gameserver.handler.usercommandhandlers.PartyInfo;
import ru.privetdruk.l2jspace.gameserver.handler.usercommandhandlers.SiegeStatus;
import ru.privetdruk.l2jspace.gameserver.handler.usercommandhandlers.Time;

public class UserCommandHandler {
    private static final Logger LOGGER = Logger.getLogger(UserCommandHandler.class.getName());

    private final Map<Integer, IUserCommandHandler> _datatable;

    private UserCommandHandler() {
        _datatable = new HashMap<>();
        registerUserCommandHandler(new ChannelDelete());
        registerUserCommandHandler(new ChannelLeave());
        registerUserCommandHandler(new ChannelListUpdate());
        registerUserCommandHandler(new ClanPenalty());
        registerUserCommandHandler(new ClanWarsList());
        registerUserCommandHandler(new DisMount());
        registerUserCommandHandler(new Escape());
        registerUserCommandHandler(new Loc());
        registerUserCommandHandler(new Mount());
        registerUserCommandHandler(new OlympiadStat());
        registerUserCommandHandler(new PartyInfo());
        registerUserCommandHandler(new SiegeStatus());
        registerUserCommandHandler(new Time());
        if (Config.OFFLINE_TRADE_ENABLE && Config.OFFLINE_COMMAND1) {
            registerUserCommandHandler(new OfflineShop());
        }

        LOGGER.info("UserCommandHandler: Loaded " + _datatable.size() + " handlers.");
    }

    public void registerUserCommandHandler(IUserCommandHandler handler) {
        for (int id : handler.getUserCommandList()) {
            _datatable.put(id, handler);
        }
    }

    public IUserCommandHandler getUserCommandHandler(int userCommand) {
        return _datatable.get(userCommand);
    }

    public int size() {
        return _datatable.size();
    }

    public static UserCommandHandler getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static class SingletonHolder {
        protected static final UserCommandHandler INSTANCE = new UserCommandHandler();
    }
}
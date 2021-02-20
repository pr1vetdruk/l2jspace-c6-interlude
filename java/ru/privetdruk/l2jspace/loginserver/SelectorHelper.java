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
package ru.privetdruk.l2jspace.loginserver;

import java.nio.channels.SocketChannel;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ru.privetdruk.l2jspace.commons.mmocore.IAcceptFilter;
import ru.privetdruk.l2jspace.commons.mmocore.IClientFactory;
import ru.privetdruk.l2jspace.commons.mmocore.IMMOExecutor;
import ru.privetdruk.l2jspace.commons.mmocore.MMOConnection;
import ru.privetdruk.l2jspace.commons.mmocore.ReceivablePacket;
import ru.privetdruk.l2jspace.commons.util.IPv4Filter;
import ru.privetdruk.l2jspace.loginserver.network.serverpackets.Init;

/**
 * @author KenM
 */
public class SelectorHelper implements IMMOExecutor<LoginClient>, IClientFactory<LoginClient>, IAcceptFilter {
    private final ThreadPoolExecutor _generalPacketsThreadPool;
    private final IPv4Filter _ipv4filter;

    public SelectorHelper() {
        _generalPacketsThreadPool = new ThreadPoolExecutor(4, 6, 15L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        _ipv4filter = new IPv4Filter();
    }

    @Override
    public void execute(ReceivablePacket<LoginClient> packet) {
        _generalPacketsThreadPool.execute(packet);
    }

    @Override
    public LoginClient create(MMOConnection<LoginClient> con) {
        final LoginClient client = new LoginClient(con);
        client.sendPacket(new Init(client));
        return client;
    }

    @Override
    public boolean accept(SocketChannel sc) {
        // return !LoginController.getInstance().isBannedAddress(sc.socket().getInetAddress());
        return _ipv4filter.accept(sc) && !LoginController.getInstance().isBannedAddress(sc.socket().getInetAddress());
    }
}

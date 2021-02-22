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

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import ru.privetdruk.l2jspace.Config;
import ru.privetdruk.l2jspace.commons.util.Chronos;

/**
 * @author -Wooden-
 */
public abstract class FloodProtectedListener extends Thread {
    private static final Logger LOGGER = Logger.getLogger(FloodProtectedListener.class.getName());
    private final Map<String, ForeignConnection> _floodProtection = new HashMap<>();
    private final String _listenIp;
    private final int _port;
    private ServerSocket _serverSocket;

    public FloodProtectedListener(String listenIp, int port) throws IOException {
        _port = port;
        _listenIp = listenIp;
        if (_listenIp.equals("*")) {
            _serverSocket = new ServerSocket(_port);
        } else {
            _serverSocket = new ServerSocket(_port, 50, InetAddress.getByName(_listenIp));
        }
    }

    @Override
    public void run() {
        Socket connection = null;

        while (true) {
            try {
                connection = _serverSocket.accept();
                if (Config.FLOOD_PROTECTION) {
                    ForeignConnection fConnection = _floodProtection.get(connection.getInetAddress().getHostAddress());
                    if (fConnection != null) {
                        fConnection.connectionNumber += 1;
                        if (((fConnection.connectionNumber > Config.FAST_CONNECTION_LIMIT) && ((Chronos.currentTimeMillis() - fConnection.lastConnection) < Config.NORMAL_CONNECTION_TIME)) || ((Chronos.currentTimeMillis() - fConnection.lastConnection) < Config.FAST_CONNECTION_TIME) || (fConnection.connectionNumber > Config.MAX_CONNECTION_PER_IP)) {
                            fConnection.lastConnection = Chronos.currentTimeMillis();
                            connection.close();

                            fConnection.connectionNumber -= 1;
                            if (!fConnection.isFlooding) {
                                LOGGER.warning("Potential Flood from " + connection.getInetAddress().getHostAddress());
                            }

                            fConnection.isFlooding = true;
                            continue;
                        }

                        if (fConnection.isFlooding) // if connection was flooding server but now passed the check
                        {
                            fConnection.isFlooding = false;
                            LOGGER.info(connection.getInetAddress().getHostAddress() + " is not considered as flooding anymore.");
                        }

                        fConnection.lastConnection = Chronos.currentTimeMillis();
                    } else {
                        fConnection = new ForeignConnection(Chronos.currentTimeMillis());
                        _floodProtection.put(connection.getInetAddress().getHostAddress(), fConnection);
                    }
                }
                addClient(connection);
            } catch (Exception e) {
                try {
                    if (connection != null) {
                        connection.close();
                    }
                } catch (Exception e2) {
                }
                if (isInterrupted()) {
                    // shutdown?
                    try {
                        _serverSocket.close();
                    } catch (IOException io) {
                        LOGGER.warning("fixme: unhandled exception " + io);
                    }
                    break;
                }
            }
        }
    }

    protected static class ForeignConnection {
        public int connectionNumber;
        public long lastConnection;
        public boolean isFlooding = false;

        /**
         * @param time
         */
        public ForeignConnection(long time) {
            lastConnection = time;
            connectionNumber = 1;
        }
    }

    public abstract void addClient(Socket s);

    public void removeFloodProtection(String ip) {
        if (!Config.FLOOD_PROTECTION) {
            return;
        }

        final ForeignConnection fConnection = _floodProtection.get(ip);
        if (fConnection != null) {
            fConnection.connectionNumber -= 1;
            if (fConnection.connectionNumber == 0) {
                _floodProtection.remove(ip);
            }
        } else {
            LOGGER.warning("Removing a flood protection for a GameServer that was not in the connection map??? :" + ip);
        }
    }

    public void close() {
        try {
            _serverSocket.close();
        } catch (IOException e) {
            LOGGER.warning(e.toString());
        }
    }
}

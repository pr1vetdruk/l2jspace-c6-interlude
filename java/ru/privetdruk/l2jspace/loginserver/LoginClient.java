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
import java.nio.ByteBuffer;
import java.security.interfaces.RSAPrivateKey;
import java.util.logging.Logger;

import ru.privetdruk.l2jspace.Config;
import ru.privetdruk.l2jspace.commons.crypt.LoginCrypt;
import ru.privetdruk.l2jspace.commons.crypt.ScrambledKeyPair;
import ru.privetdruk.l2jspace.commons.mmocore.MMOClient;
import ru.privetdruk.l2jspace.commons.mmocore.MMOConnection;
import ru.privetdruk.l2jspace.commons.mmocore.SendablePacket;
import ru.privetdruk.l2jspace.commons.util.Chronos;
import ru.privetdruk.l2jspace.commons.util.Rnd;
import ru.privetdruk.l2jspace.loginserver.network.serverpackets.LoginFail;
import ru.privetdruk.l2jspace.loginserver.network.serverpackets.LoginServerPacket;
import ru.privetdruk.l2jspace.loginserver.network.serverpackets.PlayFail;

/**
 * Represents a client connected into the LoginServer
 *
 * @author KenM
 */
public class LoginClient extends MMOClient<MMOConnection<LoginClient>> {
    private static final Logger LOGGER = Logger.getLogger(LoginClient.class.getName());

    public enum LoginClientState {
        CONNECTED,
        AUTHED_GG,
        AUTHED_LOGIN
    }

    private LoginClientState _state;

    // Crypt
    private final LoginCrypt _loginCrypt;
    private final ScrambledKeyPair _scrambledPair;
    private final byte[] _blowfishKey;

    private String _account = "";
    private int _accessLevel;
    private int _lastServer;
    private boolean _usesInternalIP;
    private SessionKey _sessionKey;
    private final int _sessionId;
    private boolean _joinedGS;
    private final String _ip;
    private long _connectionStartTime;

    /**
     * @param con
     */
    public LoginClient(MMOConnection<LoginClient> con) {
        super(con);
        _state = LoginClientState.CONNECTED;
        _ip = getConnection().getInetAddress().getHostAddress();
        final String[] localip = Config.NETWORK_IP_LIST.split(";");
        for (String oneIp : localip) {
            if (_ip.startsWith(oneIp) || _ip.startsWith("127.0")) {
                _usesInternalIP = true;
            }
        }

        _scrambledPair = LoginController.getInstance().getScrambledRSAKeyPair();
        _blowfishKey = LoginController.getInstance().getBlowfishKey();
        _sessionId = Rnd.get(Integer.MAX_VALUE);
        _connectionStartTime = Chronos.currentTimeMillis();
        _loginCrypt = new LoginCrypt();
        _loginCrypt.setKey(_blowfishKey);
        LoginController.getInstance().addLoginClient(this);

        // This checkup must go next to BAN because it can cause decrease ban account time
        if (!BruteProtector.canLogin(_ip)) {
            LoginController.getInstance().addBanForAddress(getConnection().getInetAddress(), Config.BRUT_BAN_IP_TIME * 1000);
            LOGGER.warning("Drop connection from IP " + _ip + " because of BruteForce.");
        }
    }

    public String getIntetAddress() {
        return _ip;
    }

    public boolean usesInternalIP() {
        return _usesInternalIP;
    }

    @Override
    public boolean decrypt(ByteBuffer buf, int size) {
        boolean ret = false;
        try {
            ret = _loginCrypt.decrypt(buf.array(), buf.position(), size);
            _connectionStartTime = Chronos.currentTimeMillis();
        } catch (IOException e) {
            LOGGER.warning(e.toString());
            super.getConnection().close((SendablePacket<LoginClient>) null);
            return false;
        }

        if (!ret) {
            final byte[] dump = new byte[size];
            System.arraycopy(buf.array(), buf.position(), dump, 0, size);
            LOGGER.warning("Wrong checksum from client: " + this);
            super.getConnection().close((SendablePacket<LoginClient>) null);
        }

        return ret;
    }

    @Override
    public boolean encrypt(ByteBuffer buf, int sizeValue) {
        final int offset = buf.position();
        int size = sizeValue;
        try {
            size = _loginCrypt.encrypt(buf.array(), offset, size);
        } catch (IOException e) {
            LOGGER.warning(e.toString());
            return false;
        }

        buf.position(offset + size);
        return true;
    }

    public LoginClientState getState() {
        return _state;
    }

    public void setState(LoginClientState state) {
        _state = state;
    }

    public byte[] getBlowfishKey() {
        return _blowfishKey;
    }

    public byte[] getScrambledModulus() {
        return _scrambledPair._scrambledModulus;
    }

    public RSAPrivateKey getRSAPrivateKey() {
        return (RSAPrivateKey) _scrambledPair._pair.getPrivate();
    }

    public String getAccount() {
        return _account;
    }

    public void setAccount(String account) {
        _account = account;
    }

    public void setAccessLevel(int accessLevel) {
        _accessLevel = accessLevel;
    }

    public int getAccessLevel() {
        return _accessLevel;
    }

    public void setLastServer(int lastServer) {
        _lastServer = lastServer;
    }

    public int getLastServer() {
        return _lastServer;
    }

    public int getSessionId() {
        return _sessionId;
    }

    public boolean hasJoinedGS() {
        return _joinedGS;
    }

    public void setJoinedGS(boolean value) {
        _joinedGS = value;
    }

    public void setSessionKey(SessionKey sessionKey) {
        _sessionKey = sessionKey;
    }

    public SessionKey getSessionKey() {
        return _sessionKey;
    }

    public long getConnectionStartTime() {
        return _connectionStartTime;
    }

    public void sendPacket(LoginServerPacket lsp) {
        getConnection().sendPacket(lsp);
    }

    public void close(LoginFail.LoginFailReason reason) {
        getConnection().close(new LoginFail(reason));
    }

    public void close(PlayFail.PlayFailReason reason) {
        getConnection().close(new PlayFail(reason));
    }

    public void close(LoginServerPacket lsp) {
        getConnection().close(lsp);
    }

    @Override
    public void onDisconnection() {
        LoginController.getInstance().removeLoginClient(this);
        if (!_joinedGS && (_account != null)) {
            LoginController.getInstance().removeAuthedLoginClient(getAccount());
        }
    }

    @Override
    public String toString() {
        final InetAddress address = getConnection().getInetAddress();
        if (_state == LoginClientState.AUTHED_LOGIN) {
            return "[" + _account + " (" + (address == null ? "disconnected" : address.getHostAddress()) + ")]";
        }
        return "[" + (address == null ? "disconnected" : address.getHostAddress()) + "]";
    }

    @Override
    protected void onForcedDisconnection() {
        // Empty
    }
}

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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.RSAKeyGenParameterSpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.crypto.Cipher;

import ru.privetdruk.l2jspace.Config;
import ru.privetdruk.l2jspace.commons.crypt.ScrambledKeyPair;
import ru.privetdruk.l2jspace.commons.database.DatabaseFactory;
import ru.privetdruk.l2jspace.commons.util.Rnd;
import ru.privetdruk.l2jspace.loginserver.GameServerTable.GameServerInfo;
import ru.privetdruk.l2jspace.loginserver.network.gameserverpackets.ServerStatus;
import ru.privetdruk.l2jspace.loginserver.network.serverpackets.LoginFail;

/**
 * @version $Revision: 1.7.4.3 $ $Date: 2005/03/27 15:30:09 $
 */
public class LoginController {
    protected class ConnectionChecker extends Thread {
        @Override
        public void run() {
            for (; ; ) {
                final long now = Chronos.currentTimeMillis();
                if (_stopNow) {
                    break;
                }
                for (LoginClient cl : _clients) {
                    try {
                        if ((cl != null) && ((now - cl.getConnectionStartTime()) > Config.SESSION_TTL)) {
                            cl.close(LoginFail.LoginFailReason.REASON_TEMP_PASS_EXPIRED);
                        } else {
                            _clients.remove(cl);
                        }
                    } catch (Exception e) {
                    }
                }
                try {
                    Thread.sleep(2500);
                } catch (Exception e) {
                }
            }
        }
    }

    protected static final Logger LOGGER = Logger.getLogger(LoginController.class.getName());

    private static LoginController _instance;

    /**
     * Time before kicking the client if he didnt logged yet
     */
    private static final int LOGIN_TIMEOUT = 60 * 1000;

    /**
     * Clients that are on the LS but arent assocated with a account yet
     */
    protected Collection<LoginClient> _clients = ConcurrentHashMap.newKeySet();

    /**
     * Authed Clients on LoginServer
     */
    protected Map<String, LoginClient> _loginServerClients = new ConcurrentHashMap<>();

    private final Map<InetAddress, BanInfo> _bannedIps = new ConcurrentHashMap<>();

    private final Map<InetAddress, FailedLoginAttempt> _hackProtection;
    protected ScrambledKeyPair[] _keyPairs;

    protected byte[][] _blowfishKeys;
    private static final int BLOWFISH_KEYS = 20;

    public static void load() throws GeneralSecurityException {
        if (_instance == null) {
            _instance = new LoginController();
        } else {
            throw new IllegalStateException("LoginController can only be loaded a single time.");
        }
    }

    public static LoginController getInstance() {
        return _instance;
    }

    private LoginController() throws GeneralSecurityException {
        _hackProtection = new HashMap<>();
        _keyPairs = new ScrambledKeyPair[10];
        KeyPairGenerator keygen = null;
        keygen = KeyPairGenerator.getInstance("RSA");
        final RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(1024, RSAKeyGenParameterSpec.F4);
        keygen.initialize(spec);

        // generate the initial set of keys
        for (int i = 0; i < 10; i++) {
            _keyPairs[i] = new ScrambledKeyPair(keygen.generateKeyPair());
        }

        LOGGER.info("Cached 10 KeyPairs for RSA communication.");
        testCipher((RSAPrivateKey) _keyPairs[0]._pair.getPrivate());

        // Store keys for blowfish communication
        generateBlowFishKeys();

        new ConnectionChecker().start();
    }

    /**
     * This is mostly to force the initialization of the Crypto Implementation, avoiding it being done on runtime when its first needed.<br>
     * In short it avoids the worst-case execution time on runtime by doing it on loading.
     *
     * @param key Any private RSA Key just for testing purposes.
     * @throws GeneralSecurityException if a underlying exception was thrown by the Cipher
     */
    private void testCipher(RSAPrivateKey key) throws GeneralSecurityException {
        // avoid worst-case execution, KenM
        final Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
        rsaCipher.init(Cipher.DECRYPT_MODE, key);
    }

    protected boolean _stopNow = false;

    public void shutdown() {
        _stopNow = true;
    }

    private void generateBlowFishKeys() {
        _blowfishKeys = new byte[BLOWFISH_KEYS][16];
        for (int i = 0; i < BLOWFISH_KEYS; i++) {
            for (int j = 0; j < _blowfishKeys[i].length; j++) {
                _blowfishKeys[i][j] = (byte) (Rnd.get(255) + 1);
            }
        }
        LOGGER.info("Stored " + _blowfishKeys.length + " keys for Blowfish communication.");
    }

    /**
     * @return Returns a random key
     */
    public byte[] getBlowfishKey() {
        return _blowfishKeys[(int) (Math.random() * BLOWFISH_KEYS)];
    }

    public void addLoginClient(LoginClient client) {
        if (_clients.size() >= Config.MAX_LOGINSESSIONS) {
            for (LoginClient cl : _clients) {
                try {
                    cl.close(LoginFail.LoginFailReason.REASON_DUAL_BOX);
                } catch (Exception e) {
                }
            }
        }
        synchronized (_clients) {
            _clients.add(client);
        }
    }

    public void removeLoginClient(LoginClient client) {
        if (_clients.contains(client)) {
            synchronized (_clients) {
                try {
                    _clients.remove(client);
                } catch (Exception e) {
                }
            }
        }
    }

    public SessionKey assignSessionKeyToClient(String account, LoginClient client) {
        SessionKey key;
        key = new SessionKey(Rnd.nextInt(), Rnd.nextInt(), Rnd.nextInt(), Rnd.nextInt());
        _loginServerClients.put(account, client);
        return key;
    }

    public void removeAuthedLoginClient(String account) {
        try {
            _loginServerClients.remove(account);
        } catch (Exception e) {
        }
    }

    public boolean isAccountInLoginServer(String account) {
        return _loginServerClients.containsKey(account);
    }

    public LoginClient getAuthedClient(String account) {
        return _loginServerClients.get(account);
    }

    public enum AuthLoginResult {
        INVALID_PASSWORD,
        ACCOUNT_BANNED,
        ALREADY_ON_LS,
        ALREADY_ON_GS,
        AUTH_SUCCESS
    }

    public AuthLoginResult tryAuthLogin(String account, String password, LoginClient client) {
        AuthLoginResult ret = AuthLoginResult.INVALID_PASSWORD;
        // check auth
        if (loginValid(account, password, client)) {
            // login was successful, verify presence on Gameservers
            ret = AuthLoginResult.ALREADY_ON_GS;
            if (!isAccountInAnyGameServer(account)) {
                // account isnt on any GS verify LS itself
                ret = AuthLoginResult.ALREADY_ON_LS;

                // dont allow 2 simultaneous login
                synchronized (_loginServerClients) {
                    if (!_loginServerClients.containsKey(account)) {
                        _loginServerClients.put(account, client);
                        ret = AuthLoginResult.AUTH_SUCCESS;

                        // remove him from the non-authed list
                        removeLoginClient(client);
                    }
                }
            }
        } else if (client.getAccessLevel() < 0) {
            ret = AuthLoginResult.ACCOUNT_BANNED;
        }
        return ret;
    }

    /**
     * Adds the address to the ban list of the login server, with the given duration.
     *
     * @param address    The Address to be banned.
     * @param expiration Timestamp in miliseconds when this ban expires
     * @throws UnknownHostException if the address is invalid.
     */
    public void addBanForAddress(String address, long expiration) throws UnknownHostException {
        final InetAddress netAddress = InetAddress.getByName(address);
        _bannedIps.put(netAddress, new BanInfo(netAddress, expiration));
    }

    /**
     * Adds the address to the ban list of the login server, with the given duration.
     *
     * @param address  The Address to be banned.
     * @param duration is miliseconds
     */
    public void addBanForAddress(InetAddress address, long duration) {
        _bannedIps.put(address, new BanInfo(address, Chronos.currentTimeMillis() + duration));
    }

    public boolean isBannedAddress(InetAddress address) {
        final BanInfo bi = _bannedIps.get(address);
        if (bi != null) {
            if (bi.hasExpired()) {
                _bannedIps.remove(address);
                return false;
            }
            return true;
        }
        return false;
    }

    public Map<InetAddress, BanInfo> getBannedIps() {
        return _bannedIps;
    }

    /**
     * Remove the specified address from the ban list
     *
     * @param address The address to be removed from the ban list
     * @return true if the ban was removed, false if there was no ban for this ip
     */
    public boolean removeBanForAddress(InetAddress address) {
        return _bannedIps.remove(address) != null;
    }

    /**
     * Remove the specified address from the ban list
     *
     * @param address The address to be removed from the ban list
     * @return true if the ban was removed, false if there was no ban for this ip or the address was invalid.
     */
    public boolean removeBanForAddress(String address) {
        try {
            return removeBanForAddress(InetAddress.getByName(address));
        } catch (UnknownHostException e) {
            return false;
        }
    }

    public SessionKey getKeyForAccount(String account) {
        final LoginClient client = _loginServerClients.get(account);
        if (client != null) {
            return client.getSessionKey();
        }
        return null;
    }

    public int getOnlinePlayerCount(int serverId) {
        final GameServerInfo gsi = GameServerTable.getInstance().getRegisteredGameServerById(serverId);
        if ((gsi != null) && gsi.isAuthed()) {
            return gsi.getCurrentPlayerCount();
        }
        return 0;
    }

    public boolean isAccountInAnyGameServer(String account) {
        final Collection<GameServerInfo> serverList = GameServerTable.getInstance().getRegisteredGameServers().values();
        for (GameServerInfo gsi : serverList) {
            final GameServerThread gst = gsi.getGameServerThread();
            if ((gst != null) && gst.hasAccountOnGameServer(account)) {
                return true;
            }
        }
        return false;
    }

    public GameServerInfo getAccountOnGameServer(String account) {
        final Collection<GameServerInfo> serverList = GameServerTable.getInstance().getRegisteredGameServers().values();
        for (GameServerInfo gsi : serverList) {
            final GameServerThread gst = gsi.getGameServerThread();
            if ((gst != null) && gst.hasAccountOnGameServer(account)) {
                return gsi;
            }
        }
        return null;
    }

    public int getTotalOnlinePlayerCount() {
        int total = 0;
        final Collection<GameServerInfo> serverList = GameServerTable.getInstance().getRegisteredGameServers().values();
        for (GameServerInfo gsi : serverList) {
            if (gsi.isAuthed()) {
                total += gsi.getCurrentPlayerCount();
            }
        }
        return total;
    }

    public int getMaxAllowedOnlinePlayers(int id) {
        final GameServerInfo gsi = GameServerTable.getInstance().getRegisteredGameServerById(id);
        if (gsi != null) {
            return gsi.getMaxPlayers();
        }
        return 0;
    }

    /**
     * @param client
     * @param serverId
     * @return
     */
    public boolean isLoginPossible(LoginClient client, int serverId) {
        final GameServerInfo gsi = GameServerTable.getInstance().getRegisteredGameServerById(serverId);
        final int access = client.getAccessLevel();
        if ((gsi != null) && gsi.isAuthed()) {
            final boolean loginOk = ((gsi.getCurrentPlayerCount() < gsi.getMaxPlayers()) && (gsi.getStatus() != ServerStatus.STATUS_GM_ONLY)) || (access >= 100);
            if (loginOk && (client.getLastServer() != serverId)) {
                try (Connection con = DatabaseFactory.getConnection()) {
                    final String stmt = "UPDATE accounts SET lastServer = ? WHERE login = ?";
                    final PreparedStatement statement = con.prepareStatement(stmt);
                    statement.setInt(1, serverId);
                    statement.setString(2, client.getAccount());
                    statement.executeUpdate();
                    statement.close();
                } catch (Exception e) {
                    LOGGER.warning("Could not set lastServer: " + e);
                }
            }
            return loginOk;
        }
        return false;
    }

    public void setAccountAccessLevel(String account, int banLevel) {
        try (Connection con = DatabaseFactory.getConnection()) {
            final String stmt = "UPDATE accounts SET accessLevel=? WHERE login=?";
            final PreparedStatement statement = con.prepareStatement(stmt);
            statement.setInt(1, banLevel);
            statement.setString(2, account);
            statement.executeUpdate();
            statement.close();
        } catch (Exception e) {
            LOGGER.warning("Could not set accessLevel: " + e);
        }
    }

    public boolean isGM(String user) {
        boolean ok = false;
        try (Connection con = DatabaseFactory.getConnection()) {
            final PreparedStatement statement = con.prepareStatement("SELECT accessLevel FROM accounts WHERE login=?");
            statement.setString(1, user);
            final ResultSet rset = statement.executeQuery();
            if (rset.next()) {
                final int accessLevel = rset.getInt(1);
                if (accessLevel >= 100) {
                    ok = true;
                }
            }

            rset.close();
            statement.close();
        } catch (Exception e) {
            LOGGER.warning("could not check GM state:" + e);
            ok = false;
        }
        return ok;
    }

    /**
     * <p>
     * This method returns one of the cached {@link ScrambledKeyPair ScrambledKeyPairs} for communication with Login Clients.
     * </p>
     *
     * @return a scrambled keypair
     */
    public ScrambledKeyPair getScrambledRSAKeyPair() {
        return _keyPairs[Rnd.get(10)];
    }

    /**
     * user name is not case sensitive any more
     *
     * @param user
     * @param password
     * @param client
     * @return
     */
    public synchronized boolean loginValid(String user, String password, LoginClient client) {
        boolean ok = false;
        final InetAddress address = client.getConnection().getInetAddress();

        // player disconnected meanwhile
        if (address == null) {
            return false;
        }

        try (Connection con = DatabaseFactory.getConnection()) {
            final MessageDigest md = MessageDigest.getInstance("SHA");
            final byte[] raw = password.getBytes(StandardCharsets.UTF_8);
            final byte[] hash = md.digest(raw);
            byte[] expected = null;
            int access = 0;
            int lastServer = 1;
            PreparedStatement statement = con.prepareStatement("SELECT password, accessLevel, lastServer FROM accounts WHERE login=?");
            statement.setString(1, user);
            ResultSet rset = statement.executeQuery();
            if (rset.next()) {
                expected = Base64.getDecoder().decode(rset.getString("password"));
                access = rset.getInt("accessLevel");
                lastServer = rset.getInt("lastServer");
                if (lastServer <= 0) {
                    lastServer = 1; // minServerId is 1 in Interlude
                }
            }

            rset.close();
            statement.close();
            rset = null;
            statement = null;

            // if account doesnt exists
            if (expected == null) {
                if (Config.AUTO_CREATE_ACCOUNTS) {
                    if ((user != null) && ((user.length()) >= 2) && (user.length() <= 14)) {
                        statement = con.prepareStatement("INSERT INTO accounts (login,password,lastactive,accessLevel,lastIP) values(?,?,?,?,?)");
                        statement.setString(1, user);
                        statement.setString(2, Base64.getEncoder().encodeToString(hash));
                        statement.setLong(3, Chronos.currentTimeMillis());
                        statement.setInt(4, 0);
                        statement.setString(5, address.getHostAddress());
                        statement.execute();
                        statement.close();

                        LOGGER.info("Created new account : " + user + " on IP : " + address.getHostAddress());
                        con.close();
                        return true;
                    }
                    LOGGER.warning("Invalid username creation/use attempt: " + user);
                    con.close();
                    return false;
                }
                LOGGER.warning("Account missing for user " + user + " IP: " + address.getHostAddress());
                con.close();
                return false;
            }

            // is this account banned?
            if (access < 0) {
                client.setAccessLevel(access);
                con.close();
                return false;
            }

            // check password hash
            ok = true;
            for (int i = 0; i < expected.length; i++) {
                if (hash[i] != expected[i]) {
                    ok = false;
                    break;
                }
            }

            if (ok) {
                client.setAccessLevel(access);
                client.setLastServer(lastServer);
                statement = con.prepareStatement("UPDATE accounts SET lastactive=?, lastIP=? WHERE login=?");
                statement.setLong(1, Chronos.currentTimeMillis());
                statement.setString(2, address.getHostAddress());
                statement.setString(3, user);
                statement.execute();
                statement.close();
            }
        } catch (Exception e) {
            LOGGER.warning("Could not check password:" + e);
            ok = false;
        }

        if (!ok) {
            final FailedLoginAttempt failedAttempt = _hackProtection.get(address);
            int failedCount;
            if (failedAttempt == null) {
                _hackProtection.put(address, new FailedLoginAttempt(address, password));
                failedCount = 1;
            } else {
                failedAttempt.increaseCounter(password);
                failedCount = failedAttempt.getCount();
            }

            if (failedCount >= Config.LOGIN_TRY_BEFORE_BAN) {
                LOGGER.info("Banning '" + address.getHostAddress() + "' for " + Config.LOGIN_BLOCK_AFTER_BAN + " seconds due to " + failedCount + " invalid user/pass attempts");
                addBanForAddress(address, Config.LOGIN_BLOCK_AFTER_BAN * 1000);
            }
        } else {
            _hackProtection.remove(address);
        }

        return ok;
    }

    public boolean loginBanned(String user) {
        boolean ok = false;

        try (Connection con = DatabaseFactory.getConnection()) {
            final PreparedStatement statement = con.prepareStatement("SELECT accessLevel FROM accounts WHERE login=?");
            statement.setString(1, user);
            final ResultSet rset = statement.executeQuery();
            if (rset.next()) {
                final int accessLevel = rset.getInt(1);
                if (accessLevel < 0) {
                    ok = true;
                }
            }

            rset.close();
            statement.close();
        } catch (Exception e) {
            // digest algo not found ??
            // out of bounds should not be possible
            LOGGER.warning("could not check ban state:" + e);
            ok = false;
        }

        return ok;
    }

    class FailedLoginAttempt {
        // private InetAddress _ipAddress;
        private int _count;
        private long _lastAttempTime;
        private String _lastPassword;

        public FailedLoginAttempt(InetAddress address, String lastPassword) {
            // _ipAddress = address;
            _count = 1;
            _lastAttempTime = Chronos.currentTimeMillis();
            _lastPassword = lastPassword;
        }

        public void increaseCounter(String password) {
            if (!_lastPassword.equals(password)) {
                // check if theres a long time since last wrong try
                if ((Chronos.currentTimeMillis() - _lastAttempTime) < (300 * 1000)) {
                    _count++;
                } else {
                    // restart the status
                    _count = 1;
                }
                _lastPassword = password;
                _lastAttempTime = Chronos.currentTimeMillis();
            } else // trying the same password is not brute force
            {
                _lastAttempTime = Chronos.currentTimeMillis();
            }
        }

        public int getCount() {
            return _count;
        }
    }

    class BanInfo {
        private final InetAddress _ipAddress;
        // Expiration
        private final long _expiration;

        public BanInfo(InetAddress ipAddress, long expiration) {
            _ipAddress = ipAddress;
            _expiration = expiration;
        }

        public InetAddress getAddress() {
            return _ipAddress;
        }

        public boolean hasExpired() {
            return (Chronos.currentTimeMillis() > _expiration) && (_expiration > 0);
        }
    }

    class PurgeThread extends Thread {
        @Override
        public void run() {
            for (; ; ) {
                synchronized (_clients) {
                    for (LoginClient client : _clients) {
                        if ((client.getConnectionStartTime() + LOGIN_TIMEOUT) >= Chronos.currentTimeMillis()) {
                            client.close(LoginFail.LoginFailReason.REASON_ACCESS_FAILED);
                        }
                    }
                }

                synchronized (_loginServerClients) {
                    for (Entry<String, LoginClient> e : _loginServerClients.entrySet()) {
                        final LoginClient client = e.getValue();
                        if ((client.getConnectionStartTime() + LOGIN_TIMEOUT) >= Chronos.currentTimeMillis()) {
                            client.close(LoginFail.LoginFailReason.REASON_ACCESS_FAILED);
                        }
                    }
                }

                try {
                    Thread.sleep(2 * LOGIN_TIMEOUT);
                } catch (InterruptedException e) {
                    LOGGER.warning(e.toString());
                }
            }
        }
    }
}

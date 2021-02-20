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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Logger;

import ru.privetdruk.l2jspace.commons.database.DatabaseFactory;
import ru.privetdruk.l2jspace.gameserver.datatables.sql.CharNameTable;
import ru.privetdruk.l2jspace.gameserver.model.World;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.network.SystemMessageId;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.FriendList;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.SystemMessage;

public class RequestFriendDel extends GameClientPacket {
    private static final Logger LOGGER = Logger.getLogger(RequestFriendDel.class.getName());

    private String _name;

    @Override
    protected void readImpl() {
        _name = readS();
    }

    @Override
    protected void runImpl() {
        final PlayerInstance player = getClient().getPlayer();
        if (player == null) {
            return;
        }

        final int id = CharNameTable.getInstance().getPlayerObjectId(_name);
        if ((id <= 0) || !player.getFriendList().contains(id)) {
            player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_IS_NOT_ON_YOUR_FRIEND_LIST).addString(_name));
            return;
        }

        try (Connection con = DatabaseFactory.getConnection()) {
            final PreparedStatement statement = con.prepareStatement("DELETE FROM character_friends WHERE (char_id = ? AND friend_id = ?) OR (char_id = ? AND friend_id = ?)");
            statement.setInt(1, player.getObjectId());
            statement.setInt(2, id);
            statement.setInt(3, id);
            statement.setInt(4, player.getObjectId());
            statement.execute();
            statement.close();

            // Player deleted from your friendlist
            player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_DELETED_FROM_YOUR_FRIENDS_LIST).addString(_name));

            player.getFriendList().remove(Integer.valueOf(id));
            player.sendPacket(new FriendList(player)); // update friendList *heavy method*

            final PlayerInstance target = World.getInstance().getPlayer(_name);
            if (target != null) {
                target.getFriendList().remove(Integer.valueOf(player.getObjectId()));
                target.sendPacket(new FriendList(target)); // update friendList *heavy method*
            }
        } catch (Exception e) {
            LOGGER.warning("could not delete friend objectid: " + e);
        }
    }
}
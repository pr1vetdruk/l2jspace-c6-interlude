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
package ru.privetdruk.l2jspace.gameserver.instancemanager;

import java.util.logging.Logger;

import ru.privetdruk.l2jspace.gameserver.datatables.CrownTable;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.model.clan.Clan;
import ru.privetdruk.l2jspace.gameserver.model.clan.ClanMember;
import ru.privetdruk.l2jspace.gameserver.model.entity.siege.Castle;
import ru.privetdruk.l2jspace.gameserver.model.items.instance.ItemInstance;

/**
 * @author evill33t Reworked by NB4L1
 */
public class CrownManager {
    protected static final Logger LOGGER = Logger.getLogger(CrownManager.class.getName());

    public void checkCrowns(Clan clan) {
        if (clan == null) {
            return;
        }

        for (ClanMember member : clan.getMembers()) {
            if ((member != null) && member.isOnline() && (member.getPlayerInstance() != null)) {
                checkCrowns(member.getPlayerInstance());
            }
        }
    }

    public void checkCrowns(PlayerInstance player) {
        if (player == null) {
            return;
        }

        boolean isLeader = false;
        int crownId = -1;

        final Clan playerClan = player.getClan();
        ClanMember playerClanLeader;
        if (playerClan != null) {
            playerClanLeader = player.getClan().getLeader();
        } else {
            playerClanLeader = null;
        }

        if (playerClan != null) {
            final Castle playerCastle = CastleManager.getInstance().getCastleByOwner(playerClan);
            if (playerCastle != null) {
                crownId = CrownTable.getCrownId(playerCastle.getCastleId());
            }

            if ((playerClanLeader != null) && (playerClanLeader.getObjectId() == player.getObjectId())) {
                isLeader = true;
            }
        }

        if (crownId > 0) {
            if (isLeader && (player.getInventory().getItemByItemId(6841) == null)) {
                player.addItem("Crown", 6841, 1, player, true);
                player.getInventory().updateDatabase();
            }

            if (player.getInventory().getItemByItemId(crownId) == null) {
                player.addItem("Crown", crownId, 1, player, true);
                player.getInventory().updateDatabase();
            }
        }

        boolean alreadyFoundCirclet = false;
        boolean alreadyFoundCrown = false;
        for (ItemInstance item : player.getInventory().getItems()) {
            if (CrownTable.getCrownList().contains(item.getItemId())) {
                if (crownId > 0) {
                    if (item.getItemId() == crownId) {
                        if (!alreadyFoundCirclet) {
                            alreadyFoundCirclet = true;
                            continue;
                        }
                    } else if ((item.getItemId() == 6841) && isLeader && !alreadyFoundCrown) {
                        alreadyFoundCrown = true;
                        continue;
                    }
                }

                player.destroyItem("Removing Crown", item, player, true);
                player.getInventory().updateDatabase();
            }
        }
    }

    public static CrownManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static class SingletonHolder {
        protected static final CrownManager INSTANCE = new CrownManager();
    }
}

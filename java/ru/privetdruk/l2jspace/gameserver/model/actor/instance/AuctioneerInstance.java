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
package ru.privetdruk.l2jspace.gameserver.model.actor.instance;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import ru.privetdruk.l2jspace.commons.util.Chronos;
import ru.privetdruk.l2jspace.gameserver.ai.CtrlIntention;
import ru.privetdruk.l2jspace.gameserver.datatables.xml.MapRegionData;
import ru.privetdruk.l2jspace.gameserver.instancemanager.AuctionManager;
import ru.privetdruk.l2jspace.gameserver.instancemanager.ClanHallManager;
import ru.privetdruk.l2jspace.gameserver.model.actor.templates.NpcTemplate;
import ru.privetdruk.l2jspace.gameserver.model.clan.Clan;
import ru.privetdruk.l2jspace.gameserver.model.entity.Auction;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.ActionFailed;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.MyTargetSelected;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.NpcHtmlMessage;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.ValidateLocation;

public class AuctioneerInstance extends FolkInstance {
    private static final int COND_ALL_FALSE = 0;
    private static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
    private static final int COND_REGULAR = 3;

    private final Map<Integer, Auction> _pendingAuctions = new HashMap<>();

    public AuctioneerInstance(int objectId, NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public void onAction(PlayerInstance player) {
        if (!canTarget(player)) {
            return;
        }

        player.setLastFolkNPC(this);

        // Check if the PlayerInstance already target the NpcInstance
        if (this != player.getTarget()) {
            // Set the target of the PlayerInstance player
            player.setTarget(this);

            // Send a Server->Client packet MyTargetSelected to the PlayerInstance player
            player.sendPacket(new MyTargetSelected(getObjectId(), 0));

            // Send a Server->Client packet ValidateLocation to correct the NpcInstance position and heading on the client
            player.sendPacket(new ValidateLocation(this));
        } else if (!canInteract(player)) // Calculate the distance between the PlayerInstance and the NpcInstance
        {
            // Notify the PlayerInstance AI with AI_INTENTION_INTERACT
            player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
        } else {
            showMessageWindow(player);
        }
        // Send a Server->Client ActionFailed to the PlayerInstance in order to avoid that the client wait another packet
        player.sendPacket(ActionFailed.STATIC_PACKET);
    }

    @Override
    public void onBypassFeedback(PlayerInstance player, String command) {
        final int condition = validateCondition(player);
        if (condition == COND_ALL_FALSE) {
            // TODO: html
            player.sendMessage("Wrong conditions.");
            return;
        }

        if (condition == COND_BUSY_BECAUSE_OF_SIEGE) {
            // TODO: html
            player.sendMessage("Busy because of siege.");
            return;
        } else if (condition == COND_REGULAR) {
            final StringTokenizer st = new StringTokenizer(command, " ");
            final String actualCommand = st.nextToken(); // Get actual command
            String val = "";
            if (st.countTokens() >= 1) {
                val = st.nextToken();
            }

            if (actualCommand.equalsIgnoreCase("auction")) {
                if (val.equals("")) {
                    return;
                }

                try {
                    final int days = Integer.parseInt(val);
                    try {
                        final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                        int bid = 0;
                        if (st.countTokens() >= 1) {
                            bid = Integer.parseInt(st.nextToken());
                        }

                        final Auction a = new Auction(player.getClan().getHideoutId(), player.getClan(), days * 86400000, bid, ClanHallManager.getInstance().getClanHallByOwner(player.getClan()).getName());
                        if (_pendingAuctions.get(a.getId()) != null) {
                            _pendingAuctions.remove(a.getId());
                        }

                        _pendingAuctions.put(a.getId(), a);

                        final String filename = "data/html/auction/AgitSale3.htm";
                        final NpcHtmlMessage html = new NpcHtmlMessage(1);
                        html.setFile(filename);
                        html.replaceAll("%x%", val);
                        html.replaceAll("%AGIT_AUCTION_END%", format.format(a.getEndDate()));
                        html.replaceAll("%AGIT_AUCTION_MINBID%", String.valueOf(a.getStartingBid()));
                        html.replaceAll("%AGIT_AUCTION_MIN%", String.valueOf(a.getStartingBid()));
                        html.replaceAll("%AGIT_AUCTION_DESC%", ClanHallManager.getInstance().getClanHallByOwner(player.getClan()).getDesc());
                        html.replaceAll("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_sale2");
                        html.replaceAll("%objectId%", String.valueOf(getObjectId()));
                        player.sendPacket(html);
                    } catch (Exception e) {
                        player.sendMessage("Invalid bid!");
                    }
                } catch (Exception e) {
                    player.sendMessage("Invalid auction duration!");
                }
                return;
            }
            if (actualCommand.equalsIgnoreCase("confirmAuction")) {
                try {
                    final Auction a = _pendingAuctions.get(player.getClan().getHideoutId());
                    a.confirmAuction();
                    _pendingAuctions.remove(player.getClan().getHideoutId());
                } catch (Exception e) {
                    player.sendMessage("Invalid auction");
                }
                return;
            } else if (actualCommand.equalsIgnoreCase("bidding")) {
                if (val.equals("")) {
                    return;
                }

                try {
                    final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                    final int auctionId = Integer.parseInt(val);
                    final String filename = "data/html/auction/AgitAuctionInfo.htm";
                    final Auction a = AuctionManager.getInstance().getAuction(auctionId);
                    final NpcHtmlMessage html = new NpcHtmlMessage(1);
                    html.setFile(filename);
                    if (a != null) {
                        html.replaceAll("%AGIT_NAME%", a.getItemName());
                        html.replaceAll("%OWNER_PLEDGE_NAME%", a.getSellerClanName());
                        html.replaceAll("%OWNER_PLEDGE_MASTER%", a.getSellerName());
                        html.replaceAll("%AGIT_SIZE%", "30 ");
                        html.replaceAll("%AGIT_LEASE%", String.valueOf(ClanHallManager.getInstance().getClanHallById(a.getItemId()).getLease()));
                        html.replaceAll("%AGIT_LOCATION%", ClanHallManager.getInstance().getClanHallById(a.getItemId()).getLocation());
                        html.replaceAll("%AGIT_AUCTION_END%", format.format(a.getEndDate()));
                        html.replaceAll("%AGIT_AUCTION_REMAIN%", ((a.getEndDate() - Chronos.currentTimeMillis()) / 3600000) + " hours " + (((a.getEndDate() - Chronos.currentTimeMillis()) / 60000) % 60) + " minutes");
                        html.replaceAll("%AGIT_AUCTION_MINBID%", String.valueOf(a.getStartingBid()));
                        html.replaceAll("%AGIT_AUCTION_COUNT%", String.valueOf(a.getBidders().size()));
                        html.replaceAll("%AGIT_AUCTION_DESC%", ClanHallManager.getInstance().getClanHallById(a.getItemId()).getDesc());
                        html.replaceAll("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_list");
                        html.replaceAll("%AGIT_LINK_BIDLIST%", "bypass -h npc_" + getObjectId() + "_bidlist " + a.getId());
                        html.replaceAll("%AGIT_LINK_RE%", "bypass -h npc_" + getObjectId() + "_bid1 " + a.getId());
                    } else {
                        LOGGER.warning("Auctioneer Auction null for AuctionId : " + auctionId);
                    }
                    player.sendPacket(html);
                } catch (Exception e) {
                    player.sendMessage("Invalid auction!");
                }

                return;
            } else if (actualCommand.equalsIgnoreCase("bid")) {
                if (val.equals("")) {
                    return;
                }

                try {
                    final int auctionId = Integer.parseInt(val);
                    try {
                        int bid = 0;
                        if (st.countTokens() >= 1) {
                            bid = Integer.parseInt(st.nextToken());
                        }

                        AuctionManager.getInstance().getAuction(auctionId).setBid(player, bid);
                    } catch (NumberFormatException e) {
                        player.sendMessage("Invalid bid!");
                    } catch (Exception e) {
                    }
                } catch (Exception e) {
                    player.sendMessage("Invalid auction!");
                }

                return;
            } else if (actualCommand.equalsIgnoreCase("bid1")) {
                if ((player.getClan() == null) || (player.getClan().getLevel() < 2)) {
                    player.sendMessage("Your clan's level needs to be at least 2, before you can bid in an auction");
                    return;
                }

                if (val.equals("")) {
                    return;
                }

                if (((player.getClan().getAuctionBiddedAt() > 0) && (player.getClan().getAuctionBiddedAt() != Integer.parseInt(val))) || (player.getClan().getHideoutId() > 0)) {
                    player.sendMessage("You can't bid at more than one auction");
                    return;
                }

                try {
                    final String filename = "data/html/auction/AgitBid1.htm";
                    int minimumBid = AuctionManager.getInstance().getAuction(Integer.parseInt(val)).getHighestBidderMaxBid();
                    if (minimumBid == 0) {
                        minimumBid = AuctionManager.getInstance().getAuction(Integer.parseInt(val)).getStartingBid();
                    }

                    final NpcHtmlMessage html = new NpcHtmlMessage(1);
                    html.setFile(filename);
                    html.replaceAll("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_bidding " + val);
                    html.replaceAll("%PLEDGE_ADENA%", String.valueOf(player.getClan().getWarehouse().getAdena()));
                    html.replaceAll("%AGIT_AUCTION_MINBID%", String.valueOf(minimumBid));
                    html.replaceAll("npc_%objectId%_bid", "npc_" + getObjectId() + "_bid " + val);
                    player.sendPacket(html);
                } catch (Exception e) {
                    player.sendMessage("Invalid auction!");
                }
                return;
            } else if (actualCommand.equalsIgnoreCase("list")) {
                final List<Auction> auctions = AuctionManager.getInstance().getAuctions();
                final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                /** Limit for make new page, prevent client crash **/
                int limit = 15;
                int start;
                int i = 1;
                final double npage = Math.ceil((float) auctions.size() / limit);
                if (val.equals("")) {
                    start = 1;
                } else {
                    start = (limit * (Integer.parseInt(val) - 1)) + 1;
                    limit *= Integer.parseInt(val);
                }

                String items = "";
                items += "<table width=280 border=0><tr>";
                for (int j = 1; j <= npage; j++) {
                    items += "<td><center><a action=\"bypass -h npc_" + getObjectId() + "_list " + j + "\"> Page " + j + " </a></center></td>";
                }
                items += "</tr></table><table width=280 border=0>";
                for (Auction a : auctions) {
                    if (i > limit) {
                        break;
                    } else if (i < start) {
                        i++;
                        continue;
                    } else {
                        i++;
                    }

                    items += "<tr><td>" + ClanHallManager.getInstance().getClanHallById(a.getItemId()).getLocation() + "</td><td><a action=\"bypass -h npc_" + getObjectId() + "_bidding " + a.getId() + "\">" + a.getItemName() + "</a></td><td>" + format.format(a.getEndDate()) + "</td><td>" + a.getStartingBid() + "</td></tr>";
                }
                items += "</table>";
                final String filename = "data/html/auction/AgitAuctionList.htm";
                final NpcHtmlMessage html = new NpcHtmlMessage(1);
                html.setFile(filename);
                html.replaceAll("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_start");
                html.replaceAll("%itemsField%", items);
                player.sendPacket(html);

                return;
            } else if (actualCommand.equalsIgnoreCase("bidlist")) {
                int auctionId = 0;
                if (val.equals("")) {
                    if (player.getClan().getAuctionBiddedAt() <= 0) {
                        return;
                    }
                    auctionId = player.getClan().getAuctionBiddedAt();
                } else {
                    auctionId = Integer.parseInt(val);
                }

                String biders = "";
                final Map<Integer, Auction.Bidder> bidders = AuctionManager.getInstance().getAuction(auctionId).getBidders();
                for (Auction.Bidder b : bidders.values()) {
                    biders += "<tr><td>" + b.getClanName() + "</td><td>" + b.getName() + "</td><td>" + b.getTimeBid().get(Calendar.YEAR) + "/" + (b.getTimeBid().get(Calendar.MONTH) + 1) + "/" + b.getTimeBid().get(Calendar.DATE) + "</td><td>" + b.getBid() + "</td></tr>";
                }
                final String filename = "data/html/auction/AgitBidderList.htm";
                final NpcHtmlMessage html = new NpcHtmlMessage(1);
                html.setFile(filename);
                html.replaceAll("%AGIT_LIST%", biders);
                html.replaceAll("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_selectedItems");
                html.replaceAll("%x%", val);
                html.replaceAll("%objectId%", String.valueOf(getObjectId()));
                player.sendPacket(html);

                return;
            } else if (actualCommand.equalsIgnoreCase("selectedItems")) {
                if ((player.getClan() != null) && (player.getClan().getHideoutId() == 0) && (player.getClan().getAuctionBiddedAt() > 0)) {
                    final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                    final String filename = "data/html/auction/AgitBidInfo.htm";
                    final NpcHtmlMessage html = new NpcHtmlMessage(1);
                    html.setFile(filename);
                    final Auction a = AuctionManager.getInstance().getAuction(player.getClan().getAuctionBiddedAt());
                    if (a != null) {
                        html.replaceAll("%AGIT_NAME%", a.getItemName());
                        html.replaceAll("%OWNER_PLEDGE_NAME%", a.getSellerClanName());
                        html.replaceAll("%OWNER_PLEDGE_MASTER%", a.getSellerName());
                        html.replaceAll("%AGIT_SIZE%", "30 ");
                        html.replaceAll("%AGIT_LEASE%", String.valueOf(ClanHallManager.getInstance().getClanHallById(a.getItemId()).getLease()));
                        html.replaceAll("%AGIT_LOCATION%", ClanHallManager.getInstance().getClanHallById(a.getItemId()).getLocation());
                        html.replaceAll("%AGIT_AUCTION_END%", format.format(a.getEndDate()));
                        html.replaceAll("%AGIT_AUCTION_REMAIN%", ((a.getEndDate() - Chronos.currentTimeMillis()) / 3600000) + " hours " + (((a.getEndDate() - Chronos.currentTimeMillis()) / 60000) % 60) + " minutes");
                        html.replaceAll("%AGIT_AUCTION_MINBID%", String.valueOf(a.getStartingBid()));
                        html.replaceAll("%AGIT_AUCTION_MYBID%", String.valueOf(a.getBidders().get(player.getClanId()).getBid()));
                        html.replaceAll("%AGIT_AUCTION_DESC%", ClanHallManager.getInstance().getClanHallById(a.getItemId()).getDesc());
                        html.replaceAll("%objectId%", String.valueOf(getObjectId()));
                        html.replaceAll("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_start");
                    } else {
                        LOGGER.warning("Auctioneer Auction null for AuctionBiddedAt : " + player.getClan().getAuctionBiddedAt());
                    }
                    player.sendPacket(html);

                    return;
                } else if ((player.getClan() != null) && (AuctionManager.getInstance().getAuction(player.getClan().getHideoutId()) != null)) {
                    final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                    final String filename = "data/html/auction/AgitSaleInfo.htm";
                    final NpcHtmlMessage html = new NpcHtmlMessage(1);
                    html.setFile(filename);
                    final Auction a = AuctionManager.getInstance().getAuction(player.getClan().getHideoutId());
                    if (a != null) {
                        html.replaceAll("%AGIT_NAME%", a.getItemName());
                        html.replaceAll("%AGIT_OWNER_PLEDGE_NAME%", a.getSellerClanName());
                        html.replaceAll("%OWNER_PLEDGE_MASTER%", a.getSellerName());
                        html.replaceAll("%AGIT_SIZE%", "30 ");
                        html.replaceAll("%AGIT_LEASE%", String.valueOf(ClanHallManager.getInstance().getClanHallById(a.getItemId()).getLease()));
                        html.replaceAll("%AGIT_LOCATION%", ClanHallManager.getInstance().getClanHallById(a.getItemId()).getLocation());
                        html.replaceAll("%AGIT_AUCTION_END%", format.format(a.getEndDate()));
                        html.replaceAll("%AGIT_AUCTION_REMAIN%", ((a.getEndDate() - Chronos.currentTimeMillis()) / 3600000) + " hours " + (((a.getEndDate() - Chronos.currentTimeMillis()) / 60000) % 60) + " minutes");
                        html.replaceAll("%AGIT_AUCTION_MINBID%", String.valueOf(a.getStartingBid()));
                        html.replaceAll("%AGIT_AUCTION_BIDCOUNT%", String.valueOf(a.getBidders().size()));
                        html.replaceAll("%AGIT_AUCTION_DESC%", ClanHallManager.getInstance().getClanHallById(a.getItemId()).getDesc());
                        html.replaceAll("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_start");
                        html.replaceAll("%id%", String.valueOf(a.getId()));
                        html.replaceAll("%objectId%", String.valueOf(getObjectId()));
                    } else {
                        LOGGER.warning("Auctioneer Auction null for getHideoutId : " + player.getClan().getHideoutId());
                    }
                    player.sendPacket(html);

                    return;
                } else if ((player.getClan() != null) && (player.getClan().getHideoutId() != 0)) {
                    final int ItemId = player.getClan().getHideoutId();
                    final String filename = "data/html/auction/AgitInfo.htm";
                    final NpcHtmlMessage html = new NpcHtmlMessage(1);
                    html.setFile(filename);

                    if (ClanHallManager.getInstance().getClanHallById(ItemId) != null) {
                        html.replaceAll("%AGIT_NAME%", ClanHallManager.getInstance().getClanHallById(ItemId).getName());
                        html.replaceAll("%AGIT_OWNER_PLEDGE_NAME%", player.getClan().getName());
                        html.replaceAll("%OWNER_PLEDGE_MASTER%", player.getClan().getLeaderName());
                        html.replaceAll("%AGIT_SIZE%", "30 ");
                        html.replaceAll("%AGIT_LEASE%", String.valueOf(ClanHallManager.getInstance().getClanHallById(ItemId).getLease()));
                        html.replaceAll("%AGIT_LOCATION%", ClanHallManager.getInstance().getClanHallById(ItemId).getLocation());
                        html.replaceAll("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_start");
                        html.replaceAll("%objectId%", String.valueOf(getObjectId()));
                    } else {
                        LOGGER.warning("Clan Hall ID NULL : " + ItemId + " Can be caused by concurent write in ClanHallManager");
                    }
                    player.sendPacket(html);

                    return;
                }
            } else if (actualCommand.equalsIgnoreCase("cancelBid")) {
                final int bid = AuctionManager.getInstance().getAuction(player.getClan().getAuctionBiddedAt()).getBidders().get(player.getClanId()).getBid();
                final String filename = "data/html/auction/AgitBidCancel.htm";
                final NpcHtmlMessage html = new NpcHtmlMessage(1);
                html.setFile(filename);
                html.replaceAll("%AGIT_BID%", String.valueOf(bid));
                html.replaceAll("%AGIT_BID_REMAIN%", String.valueOf((int) (bid * 0.9)));
                html.replaceAll("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_selectedItems");
                html.replaceAll("%objectId%", String.valueOf(getObjectId()));
                player.sendPacket(html);

                return;
            } else if (actualCommand.equalsIgnoreCase("doCancelBid")) {
                if (AuctionManager.getInstance().getAuction(player.getClan().getAuctionBiddedAt()) != null) {
                    AuctionManager.getInstance().getAuction(player.getClan().getAuctionBiddedAt()).cancelBid(player.getClanId());
                    player.sendMessage("You have succesfully canceled your bidding at the auction");
                }
                return;
            } else if (actualCommand.equalsIgnoreCase("cancelAuction")) {
                if (((player.getClanPrivileges() & Clan.CP_CH_AUCTION) != Clan.CP_CH_AUCTION)) {
                    player.sendMessage("You don't have the right privilleges to do this");
                    return;
                }
                final String filename = "data/html/auction/AgitSaleCancel.htm";
                final NpcHtmlMessage html = new NpcHtmlMessage(1);
                html.setFile(filename);
                html.replaceAll("%AGIT_DEPOSIT%", String.valueOf(ClanHallManager.getInstance().getClanHallByOwner(player.getClan()).getLease()));
                html.replaceAll("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_selectedItems");
                html.replaceAll("%objectId%", String.valueOf(getObjectId()));
                player.sendPacket(html);

                return;
            } else if (actualCommand.equalsIgnoreCase("doCancelAuction")) {
                if (AuctionManager.getInstance().getAuction(player.getClan().getHideoutId()) != null) {
                    AuctionManager.getInstance().getAuction(player.getClan().getHideoutId()).cancelAuction();
                    player.sendMessage("Your auction has been canceled");
                }
                return;
            } else if (actualCommand.equalsIgnoreCase("sale2")) {
                final String filename = "data/html/auction/AgitSale2.htm";
                final NpcHtmlMessage html = new NpcHtmlMessage(1);
                html.setFile(filename);
                html.replaceAll("%AGIT_LAST_PRICE%", String.valueOf(ClanHallManager.getInstance().getClanHallByOwner(player.getClan()).getLease()));
                html.replaceAll("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_sale");
                html.replaceAll("%objectId%", String.valueOf(getObjectId()));
                player.sendPacket(html);

                return;
            } else if (actualCommand.equalsIgnoreCase("sale")) {
                if (((player.getClanPrivileges() & Clan.CP_CH_AUCTION) != Clan.CP_CH_AUCTION)) {
                    player.sendMessage("You don't have the right privilleges to do this");
                    return;
                }
                final String filename = "data/html/auction/AgitSale1.htm";
                final NpcHtmlMessage html = new NpcHtmlMessage(1);
                html.setFile(filename);
                html.replaceAll("%AGIT_DEPOSIT%", String.valueOf(ClanHallManager.getInstance().getClanHallByOwner(player.getClan()).getLease()));
                html.replaceAll("%AGIT_PLEDGE_ADENA%", String.valueOf(player.getClan().getWarehouse().getAdena()));
                html.replaceAll("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_selectedItems");
                html.replaceAll("%objectId%", String.valueOf(getObjectId()));
                player.sendPacket(html);

                return;
            } else if (actualCommand.equalsIgnoreCase("rebid")) {
                final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                if (((player.getClanPrivileges() & Clan.CP_CH_AUCTION) != Clan.CP_CH_AUCTION)) {
                    player.sendMessage("You don't have the right privileges to do this");
                    return;
                }
                try {
                    final String filename = "data/html/auction/AgitBid2.htm";
                    final NpcHtmlMessage html = new NpcHtmlMessage(1);
                    html.setFile(filename);
                    final Auction a = AuctionManager.getInstance().getAuction(player.getClan().getAuctionBiddedAt());
                    if (a != null) {
                        html.replaceAll("%AGIT_AUCTION_BID%", String.valueOf(a.getBidders().get(player.getClanId()).getBid()));
                        html.replaceAll("%AGIT_AUCTION_MINBID%", String.valueOf(a.getStartingBid()));
                        html.replaceAll("%AGIT_AUCTION_END%", format.format(a.getEndDate()));
                        html.replaceAll("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_selectedItems");
                        html.replaceAll("npc_%objectId%_bid1", "npc_" + getObjectId() + "_bid1 " + a.getId());
                    } else {
                        LOGGER.warning("Auctioneer Auction null for AuctionBiddedAt : " + player.getClan().getAuctionBiddedAt());
                    }
                    player.sendPacket(html);
                } catch (Exception e) {
                    player.sendMessage("Invalid auction!");
                }

                return;
            } else if (actualCommand.equalsIgnoreCase("location")) {
                final NpcHtmlMessage html = new NpcHtmlMessage(1);
                html.setFile("data/html/auction/location.htm");
                html.replaceAll("%location%", MapRegionData.getInstance().getClosestTownName(player));
                html.replaceAll("%LOCATION%", getPictureName(player));
                html.replaceAll("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_start");
                player.sendPacket(html);
                return;
            } else if (actualCommand.equalsIgnoreCase("start")) {
                showMessageWindow(player);
                return;
            }
        }
        super.onBypassFeedback(player, command);
    }

    public void showMessageWindow(PlayerInstance player) {
        String filename; // = "data/html/auction/auction-no.htm";

        final int condition = validateCondition(player);
        if (condition == COND_BUSY_BECAUSE_OF_SIEGE) {
            filename = "data/html/auction/auction-busy.htm"; // Busy because of siege
        } else {
            filename = "data/html/auction/auction.htm";
        }

        final NpcHtmlMessage html = new NpcHtmlMessage(1);
        html.setFile(filename);
        html.replaceAll("%objectId%", String.valueOf(getObjectId()));
        html.replaceAll("%npcId%", String.valueOf(getNpcId()));
        html.replaceAll("%npcname%", getName());
        player.sendPacket(html);
    }

    private int validateCondition(PlayerInstance player) {
        if ((getCastle() != null) && (getCastle().getCastleId() > 0)) {
            if (getCastle().getSiege().isInProgress()) {
                return COND_BUSY_BECAUSE_OF_SIEGE; // Busy because of siege
            }
            return COND_REGULAR;
        }
        return COND_ALL_FALSE;
    }

    private String getPictureName(PlayerInstance plyr) {
        final int nearestTownId = MapRegionData.getInstance().getMapRegion(plyr.getX(), plyr.getY());
        String nearestTown;

        switch (nearestTownId) {
            case 5: {
                nearestTown = "GLUDIO";
                break;
            }
            case 6: {
                nearestTown = "GLUDIN";
                break;
            }
            case 7: {
                nearestTown = "DION";
                break;
            }
            case 8: {
                nearestTown = "GIRAN";
                break;
            }
            case 14: {
                nearestTown = "RUNE";
                break;
            }
            case 15: {
                nearestTown = "GODARD";
                break;
            }
            case 16: {
                nearestTown = "SCHUTTGART";
                break;
            }
            default: {
                nearestTown = "ADEN";
                break;
            }
        }
        return nearestTown;
    }
}

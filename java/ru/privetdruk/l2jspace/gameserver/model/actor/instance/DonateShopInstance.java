package ru.privetdruk.l2jspace.gameserver.model.actor.instance;

import ru.privetdruk.l2jspace.Config;
import ru.privetdruk.l2jspace.commons.util.Chronos;
import ru.privetdruk.l2jspace.gameserver.datatables.sql.CharNameTable;
import ru.privetdruk.l2jspace.gameserver.enums.Color;
import ru.privetdruk.l2jspace.gameserver.enums.ColorNameType;
import ru.privetdruk.l2jspace.gameserver.enums.PlayerStatus;
import ru.privetdruk.l2jspace.gameserver.enums.bypass.DonateShopBypass;
import ru.privetdruk.l2jspace.gameserver.enums.page.DonateShopPage;
import ru.privetdruk.l2jspace.gameserver.model.actor.templates.NpcTemplate;
import ru.privetdruk.l2jspace.gameserver.model.itemcontainer.PlayerInventory;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.NpcHtmlMessage;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.SocialAction;

import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.privetdruk.l2jspace.gameserver.enums.page.DonateShopPage.NAME_COLOR;

public class DonateShopInstance extends FolkInstance {
    /* Ник */
    private final int NickNameChangeItem = 4037; // ID айтема
    private final int NickNameChangeItemCount = 200; // Количество
    private final String NickNameChangeItemName = "Coin of Luck"; // Имя айтема
    /* Цвет титула */
    private final int ChangeTitleColorItem = 4037; // ID айтема
    private final int ChangeTitleColorItemCount = 20; // Количество
    private final String ChangeTitleColorItemName = "Coin of Luck"; // Имя айтема
    /* Цвет ника */
    private final int ChangeNickColorItem = 4037; // ID айтема
    private final int ChangeNickColorItemCount = 30;
    private final String ChangeNickColorItemName = "Coin of Luck"; // Имя айтема
    /* Ноблес */
    private final int SetNobleItem = 4037; // ID айтема
    private final int SetNobleItemCount = 100; // Количество
    private final String SetNobleItemName = "Coin of Luck"; // Имя айтема
    /* Hero */
    private final int SetHeroItem = 4037; // ID айтема
    private final int SetHeroItemCount = 300; // Количество
    private final String SetHeroItemName = "Coin of Luck"; // Имя айтема
    /* Пол */
    private final int SetSexItem = 4037; // ID айтема
    private final int SetSexItemCount = 350; // Количество
    private final String SetSexItemName = "Coin of Luck"; // Имя айтема
    /* Снятие ПК */
    private final int ChangePkKillsItem = 4037; // ID айтема
    private final int ChangePkKillsItemCount = 2; // Цена за 1 ПК;1 ПК = 2 COL
    private final String ChangePkKillsItemName = "Coin of Luck"; // Имя айтема

    private static final Pattern pattern = Pattern.compile("^.{3,16}$");

    public DonateShopInstance(int objectId, NpcTemplate template) {
        super(objectId, template, "mods/donate/");
    }

    @Override
    public void configurePage(PlayerInstance player, NpcHtmlMessage html) {
        html.replace("%name%", player.getName());
        html.replace("%name_price%", String.valueOf(NickNameChangeItemCount));
        html.replace("%color_name_price%", String.valueOf(ChangeNickColorItemCount));
        html.replace("%color_title_price%", String.valueOf(ChangeTitleColorItemCount));
        html.replace("%name_item%", NickNameChangeItemName);
        html.replace("%color_name_item%", ChangeNickColorItemName);
        html.replace("%color_title_item%", ChangeTitleColorItemName);
    }

    @Override
    public void onBypassFeedback(PlayerInstance player, String command) {
        StringTokenizer tokenizer = new StringTokenizer(command, " ");
        DonateShopBypass bypass = DonateShopBypass.valueOf(tokenizer.nextToken().toUpperCase(Locale.ROOT));

        switch (bypass) {
            case SHOW -> show(player, Integer.parseInt(tokenizer.nextToken()));
            case NAME -> changeName(player, tokenizer);
            case COLOR -> changeColor(player, tokenizer);
            case STATUS -> changeStatus(player, tokenizer);
        }

        super.onBypassFeedback(player, command);
    }

    private void changeStatus(PlayerInstance player, StringTokenizer tokenizer) {
        PlayerStatus playerStatus = PlayerStatus.valueOf(tokenizer.nextToken().toUpperCase());

        switch (playerStatus) {
            case HERO -> setHero(player);
        }
    }

    private void setHero(PlayerInstance player) {
        if (player.isHero()) {
            player.sendMessage("You are already a hero.");
            return;
        }

        if (!player.checkItemAvailability(SetHeroItem, SetHeroItemCount)) {
            player.sendMessage("Not enough item!");
            return;
        }

        player.destroyItemByItemId("Consume", SetHeroItem, SetHeroItemCount, player, false);
        player.broadcastPacket(new SocialAction(player.getObjectId(), 16));
        player.setHero(true);
        player.getInventory().addItem("CustomHeroWings", 6842, 1, player, null);

        final long heroTime = /*Config.HERO_CUSTOM_DAY * 24 * 60 * 60*/1 * 1000;
        player.getVariables().set("CustomHero", true);
        player.getVariables().set("CustomHeroEnd", heroTime == 0 ? 0 : Chronos.currentTimeMillis() + heroTime);
        player.broadcastUserInfo();
    }

    private void show(PlayerInstance player, int page) {
        NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
        html.setFile(getHtmlPath(getNpcId(), page));

        configurePage(player, html);

        html.replaceAll("%objectId%", getObjectId());

        player.sendPacket(html);
    }

    private void changeName(PlayerInstance player, StringTokenizer tokenizer) {
        String newName;

        try {
            newName = tokenizer.nextToken();
        } catch (NoSuchElementException e) {
            player.sendMessage("The name cannot be empty!");
            return;
        }

        Matcher matcher = pattern.matcher(newName);

        if (!matcher.matches()) {
            player.sendMessage("The entered name does not match the pattern!");
            return;
        }

        if (!player.checkItemAvailability(NickNameChangeItem, NickNameChangeItemCount)) {
            player.sendMessage("Not enough item!");
            return;
        }

        synchronized (CharNameTable.getInstance()) {
            if (CharNameTable.getInstance().doesCharNameExist(newName)) {
                player.sendMessage("The specified name is already taken!");
            } else {
                player.setName(newName);
                player.destroyItemByItemId("Consume", NickNameChangeItem, NickNameChangeItemCount, player, false);
                player.broadcastUserInfo();
                player.sendMessage("New name set.");
                player.store();
            }
        }

        show(player, NAME_COLOR.getPageId());
    }

    private void changeColor(PlayerInstance player, StringTokenizer tokenizer) {
        Color color = Color.fromColor(tokenizer.nextToken().toUpperCase(Locale.ROOT));

        if (color != null) {
            ColorNameType type = ColorNameType.valueOf(tokenizer.nextToken().toUpperCase(Locale.ROOT));

            int itemId = type == ColorNameType.NAME ? ChangeNickColorItem : ChangeTitleColorItem;
            int price = type == ColorNameType.NAME ? ChangeNickColorItemCount : ChangeTitleColorItemCount;

            if (!player.checkItemAvailability(itemId, price)) {
                player.sendMessage("Not enough item!");
                return;
            }

            player.destroyItemByItemId("Consume", itemId, price, player, false);

            switch (type) {
                case NAME -> player.getAppearance().setNameColor(color.getCode());
                case TITLE -> player.getAppearance().setTitleColor(color.getCode());
            }

            player.broadcastUserInfo();
            player.sendMessage("The new color is set to " + color.name());
            player.store();
        } else {
            player.sendMessage("You have chosen a color that does not exist.");
        }

        show(player, NAME_COLOR.getPageId());
    }
}

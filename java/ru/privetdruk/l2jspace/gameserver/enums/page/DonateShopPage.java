package ru.privetdruk.l2jspace.gameserver.enums.page;

public enum DonateShopPage {
    NAME_COLOR(1);

    private final int pageId;

    DonateShopPage(int pageId) {
        this.pageId = pageId;
    }

    public int getPageId() {
        return pageId;
    }
}

package ru.privetdruk.l2jspace.gameserver.enums;

public enum ItemEnum {
    ADENA(57);

    private final int id;

    ItemEnum(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}

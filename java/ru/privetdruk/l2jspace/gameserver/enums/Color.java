package ru.privetdruk.l2jspace.gameserver.enums;

public enum Color {
    AQUAMARINE("FFEE00"),
    DARK_AQUAMARINE("6B6400"),
    BLUE("FF0000"),
    DARK_BLUE("6B1400"),
    BLACK("000000"),
    GREEN("00FF00"),
    DARK_GREEN("0E6B00"),
    ORANGE("0099FF"),
    WHITE("FFFFFF"),
    YELLOW("00FFFF"),
    DARK_YELLOW("006B6b"),
    LIGHT_GREEN("00FF9D"),
    DARK_LIGHT_GREEN("006B42"),
    PINK("CB9CFF"),
    DARK_PINK("6B0022");

    private final String code;

    Color(String code) {
        this.code = code;
    }

    public Integer getCode() {
        return Integer.decode("0x" + code);
    }

    public static Color fromColor(String colorName) {
        try {
            return valueOf(colorName);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

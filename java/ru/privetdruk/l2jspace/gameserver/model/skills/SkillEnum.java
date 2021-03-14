package ru.privetdruk.l2jspace.gameserver.model.skills;

public enum SkillEnum {
    CLASS;

    public enum Warlock {
        ICON_BLESSING_OF_QUEEN(1331),
        BLESSING_OF_QUEEN(4699);

        private final int id;

        Warlock(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    public enum ElementalSummoner {
        ICON_GIFT_OF_SERAPHIM(1332),
        GIFT_OF_SERAPHIM(4703);

        private final int id;

        ElementalSummoner(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }


    public enum GeneralBuff {
        MAGIC_BARRIER(1036);

        private final int id;

        GeneralBuff(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    public enum Bishop {
        GREATER_BATTLE_HEAL(1218),
        REPOSE(1034);

        private final int id;

        Bishop(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    public enum Spellsinger {
        CANCELLATION(1056);

        private final int id;

        Spellsinger(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    public enum HotSprings {
        ICON(4037),
        FLU(4553),
        MALARIA(4554);

        private final int id;

        HotSprings(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }
}

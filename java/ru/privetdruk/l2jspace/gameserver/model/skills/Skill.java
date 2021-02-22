package ru.privetdruk.l2jspace.gameserver.model.skills;

public enum Skill {
    CLASS;

    public enum Bishop {
        REPOSE(1034);

        private final int id;

        Bishop(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }
}

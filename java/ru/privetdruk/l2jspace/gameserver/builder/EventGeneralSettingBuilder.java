package ru.privetdruk.l2jspace.gameserver.builder;

import ru.privetdruk.l2jspace.gameserver.model.base.Npc;
import ru.privetdruk.l2jspace.gameserver.model.base.RewardItem;
import ru.privetdruk.l2jspace.gameserver.model.entity.event.core.GeneralSetting;

public class EventGeneralSettingBuilder {
    private String name;
    private String description;
    private String registrationLocationName;
    private int minLevel;
    private int maxLevel;
    private Npc npc;
    private RewardItem reward;
    private int timeRegistration;
    private int durationTime;
    private int minPlayers;
    private int maxPlayers;
    private long intervalBetweenMatches;

    public EventGeneralSettingBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public EventGeneralSettingBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public EventGeneralSettingBuilder setRegistrationLocationName(String registrationLocationName) {
        this.registrationLocationName = registrationLocationName;
        return this;
    }

    public EventGeneralSettingBuilder setMinLevel(int minLevel) {
        this.minLevel = minLevel;
        return this;
    }

    public EventGeneralSettingBuilder setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
        return this;
    }

    public EventGeneralSettingBuilder setNpc(Npc npc) {
        this.npc = npc;
        return this;
    }

    public EventGeneralSettingBuilder setReward(RewardItem reward) {
        this.reward = reward;
        return this;
    }

    public EventGeneralSettingBuilder setTimeRegistration(int timeRegistration) {
        this.timeRegistration = timeRegistration;
        return this;
    }

    public EventGeneralSettingBuilder setDurationTime(int durationTime) {
        this.durationTime = durationTime;
        return this;
    }

    public EventGeneralSettingBuilder setMinPlayers(int minPlayers) {
        this.minPlayers = minPlayers;
        return this;
    }

    public EventGeneralSettingBuilder setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
        return this;
    }

    public EventGeneralSettingBuilder setIntervalBetweenMatches(long intervalBetweenMatches) {
        this.intervalBetweenMatches = intervalBetweenMatches;
        return this;
    }

    public GeneralSetting createCtfGeneralSetting() {
        return new GeneralSetting(name, description, registrationLocationName, minLevel, maxLevel, npc, reward, timeRegistration, durationTime, minPlayers, maxPlayers, intervalBetweenMatches);
    }
}
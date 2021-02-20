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
package ru.privetdruk.l2jspace.gameserver.model.base;

import static ru.privetdruk.l2jspace.gameserver.model.base.ClassType.Fighter;
import static ru.privetdruk.l2jspace.gameserver.model.base.ClassType.Mystic;
import static ru.privetdruk.l2jspace.gameserver.model.base.ClassType.Priest;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Set;

import ru.privetdruk.l2jspace.gameserver.enums.Race;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;

/**
 * @author luisantonioa
 */
public enum PlayerClass {
    HumanFighter(Race.HUMAN, Fighter, ClassLevel.FIRST),
    Warrior(Race.HUMAN, Fighter, ClassLevel.SECOND),
    Gladiator(Race.HUMAN, Fighter, ClassLevel.THIRD),
    Warlord(Race.HUMAN, Fighter, ClassLevel.THIRD),
    HumanKnight(Race.HUMAN, Fighter, ClassLevel.SECOND),
    Paladin(Race.HUMAN, Fighter, ClassLevel.THIRD),
    DarkAvenger(Race.HUMAN, Fighter, ClassLevel.THIRD),
    Rogue(Race.HUMAN, Fighter, ClassLevel.SECOND),
    TreasureHunter(Race.HUMAN, Fighter, ClassLevel.THIRD),
    Hawkeye(Race.HUMAN, Fighter, ClassLevel.THIRD),
    HumanMystic(Race.HUMAN, Mystic, ClassLevel.FIRST),
    HumanWizard(Race.HUMAN, Mystic, ClassLevel.SECOND),
    Sorceror(Race.HUMAN, Mystic, ClassLevel.THIRD),
    Necromancer(Race.HUMAN, Mystic, ClassLevel.THIRD),
    Warlock(Race.HUMAN, Mystic, ClassLevel.THIRD),
    Cleric(Race.HUMAN, Priest, ClassLevel.SECOND),
    Bishop(Race.HUMAN, Priest, ClassLevel.THIRD),
    Prophet(Race.HUMAN, Priest, ClassLevel.THIRD),

    ElvenFighter(Race.ELF, Fighter, ClassLevel.FIRST),
    ElvenKnight(Race.ELF, Fighter, ClassLevel.SECOND),
    TempleKnight(Race.ELF, Fighter, ClassLevel.THIRD),
    Swordsinger(Race.ELF, Fighter, ClassLevel.THIRD),
    ElvenScout(Race.ELF, Fighter, ClassLevel.SECOND),
    Plainswalker(Race.ELF, Fighter, ClassLevel.THIRD),
    SilverRanger(Race.ELF, Fighter, ClassLevel.THIRD),
    ElvenMystic(Race.ELF, Mystic, ClassLevel.FIRST),
    ElvenWizard(Race.ELF, Mystic, ClassLevel.SECOND),
    Spellsinger(Race.ELF, Mystic, ClassLevel.THIRD),
    ElementalSummoner(Race.ELF, Mystic, ClassLevel.THIRD),
    ElvenOracle(Race.ELF, Priest, ClassLevel.SECOND),
    ElvenElder(Race.ELF, Priest, ClassLevel.THIRD),

    DarkElvenFighter(Race.DARK_ELF, Fighter, ClassLevel.FIRST),
    PalusKnight(Race.DARK_ELF, Fighter, ClassLevel.SECOND),
    ShillienKnight(Race.DARK_ELF, Fighter, ClassLevel.THIRD),
    Bladedancer(Race.DARK_ELF, Fighter, ClassLevel.THIRD),
    Assassin(Race.DARK_ELF, Fighter, ClassLevel.SECOND),
    AbyssWalker(Race.DARK_ELF, Fighter, ClassLevel.THIRD),
    PhantomRanger(Race.DARK_ELF, Fighter, ClassLevel.THIRD),
    DarkElvenMystic(Race.DARK_ELF, Mystic, ClassLevel.FIRST),
    DarkElvenWizard(Race.DARK_ELF, Mystic, ClassLevel.SECOND),
    Spellhowler(Race.DARK_ELF, Mystic, ClassLevel.THIRD),
    PhantomSummoner(Race.DARK_ELF, Mystic, ClassLevel.THIRD),
    ShillienOracle(Race.DARK_ELF, Priest, ClassLevel.SECOND),
    ShillienElder(Race.DARK_ELF, Priest, ClassLevel.THIRD),

    OrcFighter(Race.ORC, Fighter, ClassLevel.FIRST),
    OrcRaider(Race.ORC, Fighter, ClassLevel.SECOND),
    Destroyer(Race.ORC, Fighter, ClassLevel.THIRD),
    OrcMonk(Race.ORC, Fighter, ClassLevel.SECOND),
    Tyrant(Race.ORC, Fighter, ClassLevel.THIRD),
    OrcMystic(Race.ORC, Mystic, ClassLevel.FIRST),
    OrcShaman(Race.ORC, Mystic, ClassLevel.SECOND),
    Overlord(Race.ORC, Mystic, ClassLevel.THIRD),
    Warcryer(Race.ORC, Mystic, ClassLevel.THIRD),

    DwarvenFighter(Race.DWARF, Fighter, ClassLevel.FIRST),
    DwarvenScavenger(Race.DWARF, Fighter, ClassLevel.SECOND),
    BountyHunter(Race.DWARF, Fighter, ClassLevel.THIRD),
    DwarvenArtisan(Race.DWARF, Fighter, ClassLevel.SECOND),
    Warsmith(Race.DWARF, Fighter, ClassLevel.THIRD),

    // TODO: Diminish the use of PlayerClass.values() and drop this class.
    dummyEntry1(null, null, null),
    dummyEntry2(null, null, null),
    dummyEntry3(null, null, null),
    dummyEntry4(null, null, null),
    dummyEntry5(null, null, null),
    dummyEntry6(null, null, null),
    dummyEntry7(null, null, null),
    dummyEntry8(null, null, null),
    dummyEntry9(null, null, null),
    dummyEntry10(null, null, null),
    dummyEntry11(null, null, null),
    dummyEntry12(null, null, null),
    dummyEntry13(null, null, null),
    dummyEntry14(null, null, null),
    dummyEntry15(null, null, null),
    dummyEntry16(null, null, null),
    dummyEntry17(null, null, null),
    dummyEntry18(null, null, null),
    dummyEntry19(null, null, null),
    dummyEntry20(null, null, null),
    dummyEntry21(null, null, null),
    dummyEntry22(null, null, null),
    dummyEntry23(null, null, null),
    dummyEntry24(null, null, null),
    dummyEntry25(null, null, null),
    dummyEntry26(null, null, null),
    dummyEntry27(null, null, null),
    dummyEntry28(null, null, null),
    dummyEntry29(null, null, null),
    dummyEntry30(null, null, null),

    /*
     * (3rd classes)
     */
    duelist(Race.HUMAN, Fighter, ClassLevel.FOURTH),
    dreadnought(Race.HUMAN, Fighter, ClassLevel.FOURTH),
    phoenixKnight(Race.HUMAN, Fighter, ClassLevel.FOURTH),
    hellKnight(Race.HUMAN, Fighter, ClassLevel.FOURTH),
    sagittarius(Race.HUMAN, Fighter, ClassLevel.FOURTH),
    adventurer(Race.HUMAN, Fighter, ClassLevel.FOURTH),
    archmage(Race.HUMAN, Mystic, ClassLevel.FOURTH),
    soultaker(Race.HUMAN, Mystic, ClassLevel.FOURTH),
    arcanaLord(Race.HUMAN, Mystic, ClassLevel.FOURTH),
    cardinal(Race.HUMAN, Priest, ClassLevel.FOURTH),
    hierophant(Race.HUMAN, Priest, ClassLevel.FOURTH),

    evaTemplar(Race.ELF, Fighter, ClassLevel.FOURTH),
    swordMuse(Race.ELF, Fighter, ClassLevel.FOURTH),
    windRider(Race.ELF, Fighter, ClassLevel.FOURTH),
    moonlightSentinel(Race.ELF, Fighter, ClassLevel.FOURTH),
    mysticMuse(Race.ELF, Mystic, ClassLevel.FOURTH),
    elementalMaster(Race.ELF, Mystic, ClassLevel.FOURTH),
    evaSaint(Race.ELF, Priest, ClassLevel.FOURTH),

    shillienTemplar(Race.DARK_ELF, Fighter, ClassLevel.FOURTH),
    spectralDancer(Race.DARK_ELF, Fighter, ClassLevel.FOURTH),
    ghostHunter(Race.DARK_ELF, Fighter, ClassLevel.FOURTH),
    ghostSentinel(Race.DARK_ELF, Fighter, ClassLevel.FOURTH),
    stormScreamer(Race.DARK_ELF, Mystic, ClassLevel.FOURTH),
    spectralMaster(Race.DARK_ELF, Mystic, ClassLevel.FOURTH),
    shillienSaint(Race.DARK_ELF, Priest, ClassLevel.FOURTH),

    titan(Race.ORC, Fighter, ClassLevel.FOURTH),
    grandKhavatari(Race.ORC, Fighter, ClassLevel.FOURTH),
    dominator(Race.ORC, Mystic, ClassLevel.FOURTH),
    doomcryer(Race.ORC, Mystic, ClassLevel.FOURTH),

    fortuneSeeker(Race.DWARF, Fighter, ClassLevel.FOURTH),
    maestro(Race.DWARF, Fighter, ClassLevel.FOURTH);

    private Race _race;
    private ClassLevel _level;
    private ClassType _type;

    private static final Set<PlayerClass> mainSubclassSet;
    private static final Set<PlayerClass> neverSubclassed = EnumSet.of(Overlord, Warsmith);

    private static final Set<PlayerClass> subclasseSet1 = EnumSet.of(DarkAvenger, Paladin, TempleKnight, ShillienKnight);
    private static final Set<PlayerClass> subclasseSet2 = EnumSet.of(TreasureHunter, AbyssWalker, Plainswalker);
    private static final Set<PlayerClass> subclasseSet3 = EnumSet.of(Hawkeye, SilverRanger, PhantomRanger);
    private static final Set<PlayerClass> subclasseSet4 = EnumSet.of(Warlock, ElementalSummoner, PhantomSummoner);
    private static final Set<PlayerClass> subclasseSet5 = EnumSet.of(Sorceror, Spellsinger, Spellhowler);

    private static final EnumMap<PlayerClass, Set<PlayerClass>> subclassSetMap = new EnumMap<>(PlayerClass.class);

    static {
        final Set<PlayerClass> subclasses = getSet(null, ClassLevel.THIRD);
        subclasses.removeAll(neverSubclassed);

        mainSubclassSet = subclasses;

        subclassSetMap.put(DarkAvenger, subclasseSet1);
        subclassSetMap.put(Paladin, subclasseSet1);
        subclassSetMap.put(TempleKnight, subclasseSet1);
        subclassSetMap.put(ShillienKnight, subclasseSet1);

        subclassSetMap.put(TreasureHunter, subclasseSet2);
        subclassSetMap.put(AbyssWalker, subclasseSet2);
        subclassSetMap.put(Plainswalker, subclasseSet2);

        subclassSetMap.put(Hawkeye, subclasseSet3);
        subclassSetMap.put(SilverRanger, subclasseSet3);
        subclassSetMap.put(PhantomRanger, subclasseSet3);

        subclassSetMap.put(Warlock, subclasseSet4);
        subclassSetMap.put(ElementalSummoner, subclasseSet4);
        subclassSetMap.put(PhantomSummoner, subclasseSet4);

        subclassSetMap.put(Sorceror, subclasseSet5);
        subclassSetMap.put(Spellsinger, subclasseSet5);
        subclassSetMap.put(Spellhowler, subclasseSet5);
    }

    PlayerClass(Race race, ClassType pType, ClassLevel pLevel) {
        _race = race;
        _level = pLevel;
        _type = pType;
    }

    public Set<PlayerClass> getAvailableSubclasses(PlayerInstance player) {
        Set<PlayerClass> subclasses = null;

        if (_level == ClassLevel.THIRD) {
            subclasses = EnumSet.copyOf(mainSubclassSet);

            subclasses.removeAll(neverSubclassed);
            subclasses.remove(this);

            switch (player.getRace()) {
                case ELF: {
                    subclasses.removeAll(getSet(Race.DARK_ELF, ClassLevel.THIRD));
                    break;
                }
                case DARK_ELF: {
                    subclasses.removeAll(getSet(Race.ELF, ClassLevel.THIRD));
                    break;
                }
            }

            final Set<PlayerClass> unavailableClasses = subclassSetMap.get(this);

            if (unavailableClasses != null) {
                subclasses.removeAll(unavailableClasses);
            }
        }

        return subclasses;
    }

    public static Set<PlayerClass> getSet(Race race, ClassLevel level) {
        final Set<PlayerClass> allOf = EnumSet.noneOf(PlayerClass.class);
        for (PlayerClass playerClass : EnumSet.allOf(PlayerClass.class)) {
            if (((race == null) || playerClass.isOfRace(race)) && ((level == null) || playerClass.isOfLevel(level))) {
                allOf.add(playerClass);
            }
        }
        return allOf;
    }

    public boolean isOfRace(Race pRace) {
        return _race == pRace;
    }

    public boolean isOfType(ClassType pType) {
        return _type == pType;
    }

    public boolean isOfLevel(ClassLevel pLevel) {
        return _level == pLevel;
    }

    public ClassLevel getLevel() {
        return _level;
    }
}

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
package ru.privetdruk.l2jspace.gameserver.datatables;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import ru.privetdruk.l2jspace.Config;
import ru.privetdruk.l2jspace.commons.database.DatabaseFactory;
import ru.privetdruk.l2jspace.gameserver.model.buffer.BufferProfileSetting;
import ru.privetdruk.l2jspace.gameserver.model.holders.BuffSkillHolder;

/**
 * This class loads available skills and stores players' buff schemes into _schemesTable.
 */
public class BufferTable {
    private static final Logger LOGGER = Logger.getLogger(BufferTable.class.getName());

    private static final String LOAD_SCHEMES = "SELECT * FROM buffer_profiles";
    private static final String DELETE_SCHEMES = "DELETE FROM buffer_profiles WHERE object_id <> 0";
    private static final String INSERT_SCHEME = "INSERT INTO buffer_profiles (object_id, scheme_name, skills, last_used) VALUES (?, ?, ?, ?)";

    private static final Map<String, BufferProfileSetting> defaultProfiles = new HashMap<>();
    private final Map<Integer, Map<String, BufferProfileSetting>> tableBuffProfiles = new ConcurrentHashMap<>();
    private final Map<Integer, BuffSkillHolder> availableBuffs = new LinkedHashMap<>();

    private BufferTable() {
        int count = 0;

        try (Connection connection = DatabaseFactory.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(LOAD_SCHEMES);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int playerId = resultSet.getInt("object_id");
                String schemeName = resultSet.getString("scheme_name");
                boolean isLastUsed = resultSet.getBoolean("last_used");
                String[] skills = resultSet.getString("skills").split(",");
                List<Integer> buffList = new ArrayList<>();

                for (String skill : skills) {
                    // Don't feed the skills list if the list is empty.
                    if (skill.isEmpty()) {
                        break;
                    }

                    buffList.add(Integer.parseInt(skill));
                }

                addProfile(playerId, schemeName, isLastUsed, buffList);
                count++;
            }

            defaultProfiles.putAll(tableBuffProfiles.get(0));
            tableBuffProfiles.remove(0);

            resultSet.close();
            preparedStatement.close();
        } catch (Exception e) {
            LOGGER.severe("BufferTable: Failed to load buff schemes : " + e);
        }

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new File("./data/BufferSkills.xml"));
            Node n = doc.getFirstChild();
            for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
                if (!d.getNodeName().equalsIgnoreCase("category")) {
                    continue;
                }

                String category = d.getAttributes().getNamedItem("type").getNodeValue();
                for (Node c = d.getFirstChild(); c != null; c = c.getNextSibling()) {
                    if (!c.getNodeName().equalsIgnoreCase("buff")) {
                        continue;
                    }

                    NamedNodeMap attrs = c.getAttributes();
                    int skillId = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
                    availableBuffs.put(skillId, new BuffSkillHolder(skillId, Integer.parseInt(attrs.getNamedItem("level").getNodeValue()), Integer.parseInt(attrs.getNamedItem("price").getNodeValue()), category, attrs.getNamedItem("desc").getNodeValue()));
                }
            }
        } catch (Exception e) {
            LOGGER.severe("BufferTable: Failed to load buff info : " + e);
        }

        LOGGER.info("BufferTable: Loaded " + count + " players schemes and " + availableBuffs.size() + " available buffs.");
    }

    public void saveSchemes() {
        try (Connection con = DatabaseFactory.getConnection()) {
            // Delete all entries from database.
            try (PreparedStatement st = con.prepareStatement(DELETE_SCHEMES)) {
                st.execute();
            }

            // Save _schemesTable content.
            try (PreparedStatement st = con.prepareStatement(INSERT_SCHEME)) {
                for (Map.Entry<Integer, Map<String, BufferProfileSetting>> player : tableBuffProfiles.entrySet()) {
                    for (Map.Entry<String, BufferProfileSetting> scheme : player.getValue().entrySet()) {
                        BufferProfileSetting bufferProfileSetting = scheme.getValue();

                        // Build a String composed of skill ids seperated by a ",".
                        StringBuilder sb = new StringBuilder();
                        for (Integer skillId : bufferProfileSetting.getSkills()) {
                            sb.append(skillId).append(",");
                        }

                        // Delete the last "," : must be called only if there is something to delete !
                        if (sb.length() > 0) {
                            sb.setLength(sb.length() - 1);
                        }

                        st.setInt(1, player.getKey());
                        st.setString(2, scheme.getKey());
                        st.setString(3, sb.toString());
                        st.setBoolean(4, bufferProfileSetting.getLastUsed());
                        st.addBatch();
                    }
                }
                st.executeBatch();
            }
        } catch (Exception e) {
            LOGGER.warning("BufferTableScheme: Error while saving schemes : " + e);
        }
    }

    public void addProfile(int playerId, String profileName, Boolean isLastUsed, List<Integer> skills) {
        Map<String, BufferProfileSetting> playerProfiles = tableBuffProfiles.get(playerId);

        if (playerProfiles == null) {
            tableBuffProfiles.put(playerId, new HashMap<>());
        }

        playerProfiles = tableBuffProfiles.get(playerId);

        if (playerProfiles.size() >= Config.BUFFER_MAX_SCHEMES) {
            return;
        }

        playerProfiles.values().forEach(profile -> profile.setLastUsed(false));

        playerProfiles.put(profileName, new BufferProfileSetting(profileName, isLastUsed, skills));
    }

    public void setLastUsedProfile(int playerId, String profileName) {
        Map<String, BufferProfileSetting> playerProfiles = tableBuffProfiles.get(playerId);

        if (playerProfiles != null) {
            playerProfiles.values().forEach(profile -> profile.setLastUsed(profile.getName().equals(profileName)));
        }
    }

    public Map<String, BufferProfileSetting> getProfiles(int playerId) {
        return tableBuffProfiles.get(playerId);
    }

    public String getLastUsedProfile(int playerId) {
        Map<String, BufferProfileSetting> playerProfiles = tableBuffProfiles.get(playerId);

        if (playerProfiles == null || playerProfiles.size() == 0) {
            return defaultProfiles.entrySet().iterator().next().getKey();
        }

        String playerProfileName = playerProfiles.entrySet().iterator().next().getKey();

        return playerProfiles.entrySet().stream()
                .filter(profile -> profile.getValue().getLastUsed())
                .findAny()
                .map(Map.Entry::getKey)
                .orElse(playerProfileName);
    }

    /**
     * @param playerId    : The player objectId to check.
     * @param profileName : The scheme name to check.
     * @return the List holding skills for the given scheme name and player, or null (if scheme or player isn't registered).
     */
    public List<Integer> getSkills(int playerId, String profileName) {
        if (defaultProfiles.containsKey(profileName)) {
            return defaultProfiles.get(profileName).getSkills();
        }

        if (tableBuffProfiles.get(playerId) == null || tableBuffProfiles.get(playerId).get(profileName) == null) {
            return Collections.emptyList();
        }

        return tableBuffProfiles.get(playerId).get(profileName).getSkills();
    }

    /**
     * @param groupType : The type of skills to return.
     * @return a list of skills ids based on the given groupType.
     */
    public List<Integer> getSkillsIdsByType(String groupType) {
        List<Integer> skills = new ArrayList<>();
        for (BuffSkillHolder skill : availableBuffs.values()) {
            if (skill.getType().equalsIgnoreCase(groupType)) {
                skills.add(skill.getId());
            }
        }
        return skills;
    }

    /**
     * @return a list of all buff types available.
     */
    public List<String> getSkillTypes() {
        List<String> skillTypes = new ArrayList<>();
        for (BuffSkillHolder skill : availableBuffs.values()) {
            if (!skillTypes.contains(skill.getType())) {
                skillTypes.add(skill.getType());
            }
        }
        return skillTypes;
    }

    public BuffSkillHolder getAvailableBuff(int skillId) {
        return availableBuffs.get(skillId);
    }

    public static Map<String, BufferProfileSetting> getDefaultProfiles() {
        return defaultProfiles;
    }

    public static BufferTable getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static class SingletonHolder {
        protected static BufferTable INSTANCE = new BufferTable();
    }
}
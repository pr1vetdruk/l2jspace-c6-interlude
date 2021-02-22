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
package ru.privetdruk.l2jspace.gameserver.model.entity.event.core.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import ru.privetdruk.l2jspace.commons.database.DatabaseFactory;
import ru.privetdruk.l2jspace.gameserver.model.entity.event.core.LocationLoadingMode;
import ru.privetdruk.l2jspace.gameserver.model.entity.event.core.State;
import ru.privetdruk.l2jspace.gameserver.model.entity.event.core.EventEngine;
import ru.privetdruk.l2jspace.gameserver.model.entity.event.ctf.CTF;
import ru.privetdruk.l2jspace.gameserver.model.entity.event.DM;
import ru.privetdruk.l2jspace.gameserver.model.entity.event.TvT;

import static ru.privetdruk.l2jspace.gameserver.model.entity.event.core.LocationLoadingMode.RANDOMLY;

/**
 * @author Shyla
 */
public class EventManager {
    protected static final Logger LOGGER = Logger.getLogger(EventManager.class.getName());

    private static final String EVENT_MANAGER_CONFIGURATION_FILE = "./config/events/EventManager.ini";

    public static boolean TVT_EVENT_ENABLED;
    public static List<String> TVT_TIMES_LIST;

    public static LocationLoadingMode CTF_LOCATION_LOADING_MODE;
    public static boolean CTF_EVENT_ENABLED;
    public static List<String> CTF_TIMES_LIST;

    public static boolean DM_EVENT_ENABLED;
    public static List<String> DM_TIMES_LIST;

    private static EventManager instance = null;

    private EventManager() {
        loadConfiguration();
    }

    public static EventManager getInstance() {
        if (instance == null) {
            instance = new EventManager();
        }
        return instance;
    }

    public static void loadConfiguration() {
        InputStream is = null;
        try {
            final Properties eventSettings = new Properties();
            is = new FileInputStream(new File(EVENT_MANAGER_CONFIGURATION_FILE));
            eventSettings.load(is);

            TVT_EVENT_ENABLED = Boolean.parseBoolean(eventSettings.getProperty("TVTEventEnabled", "false"));
            TVT_TIMES_LIST = new ArrayList<>();
            String[] propertySplit;
            propertySplit = eventSettings.getProperty("TVTStartTime", "").split(";");
            TVT_TIMES_LIST.addAll(Arrays.asList(propertySplit));

            CTF_EVENT_ENABLED = Boolean.parseBoolean(eventSettings.getProperty("CTFEventEnabled", "false"));
            CTF_LOCATION_LOADING_MODE = LocationLoadingMode.valueOf(eventSettings.getProperty("CtfLocationLoadingMode", RANDOMLY.name()));
            CTF_TIMES_LIST = new ArrayList<>();
            propertySplit = eventSettings.getProperty("CTFStartTime", "").split(";");
            CTF_TIMES_LIST.addAll(Arrays.asList(propertySplit));

            DM_EVENT_ENABLED = Boolean.parseBoolean(eventSettings.getProperty("DMEventEnabled", "false"));
            DM_TIMES_LIST = new ArrayList<>();
            propertySplit = eventSettings.getProperty("DMStartTime", "").split(";");
            DM_TIMES_LIST.addAll(Arrays.asList(propertySplit));
        } catch (Exception e) {
            LOGGER.severe(e.toString());
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    LOGGER.severe(e.toString());
                }
            }
        }
    }

    public void startEventRegistration() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        if (TVT_EVENT_ENABLED) {
            registerTvT();
        }

        if (CTF_EVENT_ENABLED) {
            register(CTF.class, CTF_TIMES_LIST);
        }

        if (DM_EVENT_ENABLED) {
            registerDM();
        }
    }

    private void register(Class<? extends EventEngine> eventClass, List<String> timeList) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        EventsGlobalTask.getInstance().clearEventTasksByEventName("ALL");

        Set<Integer> eventIdSet = new HashSet<>(timeList.size());
        int eventId = 0;
        int count = 0;
        List<Integer> eventIdList = new ArrayList<>();

        try (Connection connection = DatabaseFactory.getConnection()) {
            PreparedStatement statement;
            ResultSet resultSet;

            statement = connection.prepareStatement("SELECT id FROM event e WHERE e.type = 'CTF' order by e.loading_order");
            resultSet = statement.executeQuery();

            if (!resultSet.next()) {
                LOGGER.warning("Settings ctf not found!");
                return;
            }

            do {
                eventIdList.add(resultSet.getInt("id"));
            } while (resultSet.next());
        } catch (Exception e) {
            LOGGER.severe("An error occurred while reading event data!");
            return;
        }

        if (CTF_LOCATION_LOADING_MODE == RANDOMLY) {
            Collections.shuffle(eventIdList);
        }

        for (int timeIndex = 0, eventIdIndex = 0; timeIndex < timeList.size(); timeIndex++, eventIdIndex++) {
            if (eventIdIndex == eventIdList.size()) {
                eventIdIndex = 0;
            }

            EventEngine eventTask = eventClass.getDeclaredConstructor().newInstance();
            eventTask.loadData(eventIdList.get(eventIdIndex));

            if (eventTask.getEventState() != State.ERROR) {
                eventTask.setEventStartTime(timeList.get(timeIndex));
                EventsGlobalTask.getInstance().registerNewEventTask(eventTask);
                LOGGER.info(eventTask.getEventIdentifier() + ": starts at " + timeList.get(timeIndex));
            }
        }
    }

    private void registerTvT() {
        TvT.loadData();
        if (!TvT.checkStartJoinOk()) {
            LOGGER.warning("registerTvT: TvT Event is not setted Properly");
        }

        EventsGlobalTask.getInstance().clearEventTasksByEventName(TvT.getEventName());

        for (String time : TVT_TIMES_LIST) {
            final TvT newInstance = TvT.getNewInstance();
            newInstance.setEventStartTime(time);
            EventsGlobalTask.getInstance().registerNewEventTask(newInstance);
        }
    }

    private void registerDM() {
        DM.loadData();
        if (!DM.checkStartJoinOk()) {
            LOGGER.warning("registerDM: DM Event is not setted Properly");
        }

        EventsGlobalTask.getInstance().clearEventTasksByEventName(DM.getEventName());

        for (String time : DM_TIMES_LIST) {
            final DM newInstance = DM.getNewInstance();
            newInstance.setEventStartTime(time);
            EventsGlobalTask.getInstance().registerNewEventTask(newInstance);
        }
    }
}

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
package ru.privetdruk.l2jspace.gameserver.taskmanager.tasks;

import java.util.logging.Logger;

import ru.privetdruk.l2jspace.gameserver.model.entity.olympiad.Olympiad;
import ru.privetdruk.l2jspace.gameserver.taskmanager.Task;
import ru.privetdruk.l2jspace.gameserver.taskmanager.TaskManager;
import ru.privetdruk.l2jspace.gameserver.taskmanager.TaskTypes;

/**
 * Updates all data of Olympiad nobles in db
 *
 * @author godson
 */
public class TaskOlympiadSave extends Task {
    private static final Logger LOGGER = Logger.getLogger(TaskOlympiadSave.class.getName());
    public static final String NAME = "olympiadsave";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void onTimeElapsed(TaskManager.ExecutedTask task) {
        try {
            if (Olympiad.getInstance().inCompPeriod()) {
                Olympiad.getInstance().saveOlympiadStatus();
                LOGGER.info("[GlobalTask] Olympiad System save launched.");
            }
        } catch (Exception e) {
            LOGGER.warning("Olympiad System: Failed to save Olympiad configuration: " + e);
        }
    }

    @Override
    public void initializate() {
        super.initializate();
        TaskManager.addUniqueTask(NAME, TaskTypes.TYPE_FIXED_SHEDULED, "900000", "1800000", "");
    }
}
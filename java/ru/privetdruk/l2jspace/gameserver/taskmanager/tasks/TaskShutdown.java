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

import ru.privetdruk.l2jspace.gameserver.Shutdown;
import ru.privetdruk.l2jspace.gameserver.taskmanager.Task;
import ru.privetdruk.l2jspace.gameserver.taskmanager.TaskManager;

/**
 * @author Layane
 */
public class TaskShutdown extends Task {
    private static final Logger LOGGER = Logger.getLogger(TaskShutdown.class.getName());
    public static final String NAME = "shutdown";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void onTimeElapsed(TaskManager.ExecutedTask task) {
        LOGGER.info("[GlobalTask] Server Shutdown launched.");
        Shutdown.getInstance().startShutdown(null, Integer.parseInt(task.getParams()[2]), false);
    }
}
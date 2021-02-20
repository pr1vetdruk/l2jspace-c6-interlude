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

import ru.privetdruk.l2jspace.gameserver.model.World;
import ru.privetdruk.l2jspace.gameserver.model.actor.instance.PlayerInstance;
import ru.privetdruk.l2jspace.gameserver.network.serverpackets.UserInfo;
import ru.privetdruk.l2jspace.gameserver.taskmanager.Task;
import ru.privetdruk.l2jspace.gameserver.taskmanager.TaskManager;
import ru.privetdruk.l2jspace.gameserver.taskmanager.TaskTypes;

/**
 * @author Layane
 */
public class TaskRecom extends Task {
    private static final Logger LOGGER = Logger.getLogger(TaskRecom.class.getName());
    private static final String NAME = "sp_recommendations";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void onTimeElapsed(TaskManager.ExecutedTask task) {
        for (PlayerInstance player : World.getInstance().getAllPlayers()) {
            player.restartRecom();
            player.sendPacket(new UserInfo(player));
        }
        LOGGER.info("[GlobalTask] Restart Recommendation launched.");
    }

    @Override
    public void initializate() {
        super.initializate();
        TaskManager.addUniqueTask(NAME, TaskTypes.TYPE_GLOBAL_TASK, "1", "13:00:00", "");
    }
}
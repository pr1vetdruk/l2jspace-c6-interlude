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
package ru.privetdruk.l2jspace.gameserver.taskmanager;

import ru.privetdruk.l2jspace.commons.concurrent.ThreadPool;
import ru.privetdruk.l2jspace.gameserver.TradeController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BuyListTaskManager {
    private static final Map<Integer, Long> REFRESH_TIME = new ConcurrentHashMap<>();
    private static final List<Integer> PENDING_UPDATES = new ArrayList<>();
    private static boolean _workingTimes = false;
    private static boolean _workingSaves = false;

    public BuyListTaskManager() {
        ThreadPool.scheduleAtFixedRate(() -> {
            if (_workingTimes) {
                return;
            }
            _workingTimes = true;

            final long currentTime = Chronos.currentTimeMillis();
            for (Map.Entry<Integer, Long> entry : REFRESH_TIME.entrySet()) {
                if (currentTime > entry.getValue()) {
                    final Integer time = entry.getKey();
                    synchronized (PENDING_UPDATES) {
                        PENDING_UPDATES.add(time);
                    }
                    REFRESH_TIME.put(time, currentTime + (time * 60 * 60 * 1000L));
                }
            }

            _workingTimes = false;
        }, 1000, 60000);

        ThreadPool.scheduleAtFixedRate(() -> {
            if (_workingSaves) {
                return;
            }
            _workingSaves = true;

            if (!PENDING_UPDATES.isEmpty()) {
                final int time;

                synchronized (PENDING_UPDATES) {
                    time = PENDING_UPDATES.get(0);
                    PENDING_UPDATES.remove(PENDING_UPDATES.get(0));
                }

                TradeController.getInstance().restoreCount(time);
                TradeController.getInstance().dataTimerSave(time);
            }

            _workingSaves = false;
        }, 50, 50);
    }

    public void addTime(int time, long refreshTime) {
        if (refreshTime == 0) {
            synchronized (PENDING_UPDATES) {
                PENDING_UPDATES.add(time);
            }
            REFRESH_TIME.put(time, Chronos.currentTimeMillis() + (time * 60 * 60 * 1000L));
        } else {
            REFRESH_TIME.put(time, refreshTime);
        }
    }

    public static BuyListTaskManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static class SingletonHolder {
        protected static final BuyListTaskManager INSTANCE = new BuyListTaskManager();
    }
}
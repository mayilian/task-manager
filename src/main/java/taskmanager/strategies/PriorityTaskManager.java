package taskmanager.strategies;

import process.Priority;
import process.Process;

import java.util.Set;

public class PriorityTaskManager extends AbstractTaskManager {
    public PriorityTaskManager(Long maxCapacity) {
        super(maxCapacity);;
    }

    @Override
    public synchronized boolean capacityViolationAlgorithm(Priority priority) {
        if (processes.size() < MAX_CAPACITY) {
            return true;
        }

        for (int i = priority.getMinPriority(); i <= priority.getMaxPriority(); ++i) {
            Set<Long> processIds = processGroups.get(i);
            if (processIds != null && i < priority.getPriority()) {
                Long eldestItemId = processIds.iterator().next();
                Process processToBeRemoved = processes.get(eldestItemId);
                if (!processToBeRemoved.kill()) {
                    System.out.println("Could not kill process with PID : " + processToBeRemoved.pid);
                    return false;
                }

                processIds.remove(eldestItemId);
                if (processIds.isEmpty()) {
                    processGroups.put(i, null);
                }
                processes.remove(eldestItemId);
                recycledIds.add(eldestItemId);

                Process processWithHighestId = sortById().get(processes.size() - 1);
                lastId = processWithHighestId.pid;
                return true;
            }
        }

        return false;
    }
}

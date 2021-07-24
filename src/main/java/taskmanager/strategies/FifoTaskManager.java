package taskmanager.strategies;

import process.Priority;
import process.Process;

import java.util.Map;
import java.util.Set;

public class FifoTaskManager extends AbstractTaskManager {

    public FifoTaskManager(Long maxCapacity) {
        super(maxCapacity);
    }

    @Override
    public synchronized boolean capacityViolationAlgorithm(Priority priority) {
        if (processes.size() < MAX_CAPACITY) {
            return true;
        }

        Map.Entry<Long, Process> leastRecentlyAdded = processes.entrySet().iterator().next();

        Process eldestProcess = leastRecentlyAdded.getValue();
        if (!eldestProcess.kill()) {
            System.out.println("Could not kill process with PID : " + eldestProcess.pid);
            return false;
        }

        processes.remove(eldestProcess.pid);
        Set<Long> groups = processGroups.get(eldestProcess.priority.getPriority());
        groups.remove(eldestProcess.pid);
        if (groups.isEmpty()) {
            processGroups.put(eldestProcess.priority.getPriority(), null);
        }
        recycledIds.add(leastRecentlyAdded.getKey());

        Process processWithHighestId = sortById().get(processes.size() - 1);
        lastId = processWithHighestId.pid;

        return true;
    }

}

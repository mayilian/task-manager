package taskmanager.strategies;

import process.Priority;

public class DefaultTaskManager extends AbstractTaskManager {
    public DefaultTaskManager(Long maxCapacity) {
        super(maxCapacity);
    }

    @Override
    public boolean capacityViolationAlgorithm(Priority priority) {
        return processes.size() < MAX_CAPACITY;
    }
}

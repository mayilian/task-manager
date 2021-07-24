package taskmanager;

import java.security.InvalidParameterException;

public enum TaskManagerRegistry {
    DefaultTaskManager(
            taskmanager.strategies.DefaultTaskManager.class,
            "Default Task Manager",
            "If there is no capacity do not accept processes"),
    FifoTaskManager(
            taskmanager.strategies.FifoTaskManager.class,
            "FIFO Task Manager",
            "If there is no capacity remove the least recently added process and accept the new one"),
    PriorityTaskManager(
            taskmanager.strategies.PriorityTaskManager.class,
            "Priority Task Manager",
            "If there is no capacity, then if the new process has a higher priority compared to any of the existing one we remove the lowest priority that is the oldest, otherwise we skip it");

    private final String name;
    private final String description;
    private final Class<? extends TaskManager> tmKlass;

    TaskManagerRegistry(Class<? extends TaskManager> tmKlass, String name, String description) {
        this.tmKlass = tmKlass;
        this.name = name;
        this.description = description;
    }

    public static TaskManager create(String tmCode, long maxCapacity) throws ReflectiveOperationException {
        for (TaskManagerRegistry value : values()) {
            if (! value.name().equals(tmCode)) {
                continue;
            }
            return createInstance(value.getTmKlass(), maxCapacity);
        }

        throw new InvalidParameterException("Task manager not found");
    }

    private static TaskManager createInstance(Class<? extends TaskManager> tmKlass, long maxCapacity)  throws ReflectiveOperationException {
        return tmKlass.getConstructor(Long.class).newInstance(maxCapacity);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Class<? extends TaskManager> getTmKlass() {
        return tmKlass;
    }
}

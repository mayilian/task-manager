package taskmanager.strategies;

import process.Priority;
import process.Process;
import process.SortCriteria;
import taskmanager.TaskManager;

import java.util.*;

public abstract class AbstractTaskManager implements TaskManager {
    protected long lastId = 0;
    protected final long MAX_CAPACITY;
    protected final Queue<Long> recycledIds = new LinkedList<>();

    Map<Long, Process> processes = new LinkedHashMap<>();
    Map<Integer, Set<Long>> processGroups = new HashMap<>();

    public AbstractTaskManager(Long maxCapacity) {
        MAX_CAPACITY = maxCapacity;
    }

    public synchronized Process addProcess(Priority priority) {
        if (!capacityViolationAlgorithm(priority)) {
            return null;
        }


        long id = getId();
        putInGroup(priority, id);

        Process process = new Process(id, priority);
        processes.put(id, process);

        return process;
    }

    public abstract boolean capacityViolationAlgorithm(Priority priority);

    private void putInGroup(Priority priority, long id) {
        Set<Long> processGroup = processGroups.get(priority.getPriority());
        if (processGroup == null) {
            processGroup = new LinkedHashSet<>();
            processGroup.add(id);
            processGroups.put(priority.getPriority(), processGroup);
        } else {
            processGroup.add(id);
        }
    }

    private long getId() {
        long id = lastId + 1;
        if (recycledIds.isEmpty()) {
            if (lastId == MAX_CAPACITY) {
                throw new IllegalStateException("Should not happen!!");
            }

            ++lastId;
        } else {
            id = recycledIds.poll();
        }

        return id;
    }

    public synchronized List<Process>  list(SortCriteria criteria) {
        switch (criteria) {
            case ID:
                return sortById();
            case PRIORITY:
                return sortByPriority();
            default:
                return sortByTime();
        }
    }

    private List<Process>  sortByTime() {
        return new ArrayList<>(processes.values());
    }

    protected List<Process> sortById() {
        List<Process> sorted = new ArrayList<>();
        processes.values().stream().sorted().forEach(sorted::add);

        return sorted;
    }

    private List<Process>  sortByPriority() {
        List<Process> sorted = new ArrayList<>();
        processGroups.keySet().stream().sorted().forEach( priority-> {
            processGroups.get(priority).forEach(pid -> sorted.add(processes.get(pid)));
        });

        return sorted;
    }

    public synchronized boolean kill(long pid) {
        Process removed = processes.remove(pid);
        if (removed == null) {
            return true;
        }

        Set<Long> processIds = processGroups.get(removed.priority.getPriority());
        processIds.remove(removed.pid);
        if (processIds.isEmpty()) {
            processGroups.put(removed.priority.getPriority(), null);
        }

        if (removed.kill()) {
            recycledIds.add(removed.pid);
            return true;
        }

        return false;
    }

    public synchronized boolean killGroup(Priority priority) {
        Set<Long> processIds = processGroups.get(priority.getPriority());
        if (processIds == null) {
            return true;
        }

        boolean success = true;
        for (long id : processIds) {
            Process removed = processes.remove(id);
            if (removed != null) {
                if(!removed.kill()) {
                    success = false;
                } else {
                    recycledIds.add(id);
                }
            }
        }

        processGroups.put(priority.getPriority(), null);

        return success;
    }

    public synchronized boolean killAll() {
        processes.values().forEach(p -> {
            if (p.kill()) {
                System.out.println("Could not kill process with PID : " + p.pid);
            }
        });

        processes.clear();
        processGroups.clear();
        recycledIds.clear();
        lastId = 0;

        return true;
    }

    @Override
    public long getMaxCapacity() {
        return MAX_CAPACITY;
    }
}

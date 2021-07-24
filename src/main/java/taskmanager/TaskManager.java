package taskmanager;

import process.Priority;
import process.Process;
import process.SortCriteria;

import java.util.*;


public interface TaskManager {
    long getMaxCapacity();

    Process addProcess(Priority priority);

    List<Process> list(SortCriteria criteria);
    default List<Process>  list() {
        return list(SortCriteria.TIME);
    }

    boolean kill(long pid);
    boolean killGroup(Priority priority);
    boolean killAll();
}

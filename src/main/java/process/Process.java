package process;

import java.util.Objects;

public final class Process implements Comparable<Process> {
    public final long pid;
    public final Priority priority;

    public Process(long pid, Priority priority) {
        this.pid = pid;
        this.priority = priority;
    }

    public boolean kill() {
        // imagine there's a logic destroying this process
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Process process = (Process) o;
        return pid == process.pid;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pid);
    }

    @Override
    public int compareTo(Process p) {
        return (int) p.pid;
    }
}

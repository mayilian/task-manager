import process.Priority;
import process.SortCriteria;
import taskmanager.TaskManager;
import taskmanager.TaskManagerRegistry;

public class Main {

    public static void main(String[] args) throws ReflectiveOperationException {
        TaskManager taskManager = TaskManagerRegistry.create(TaskManagerRegistry.DefaultTaskManager.name(), 65536L);
        taskManager.addProcess(Priority.LOW);
        taskManager.list(SortCriteria.TIME);
    }
}

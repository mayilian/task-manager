import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import process.Priority;
import process.Process;
import process.SortCriteria;
import taskmanager.TaskManager;
import taskmanager.TaskManagerRegistry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FifoTaskManagerTest {
    @Test
    void testMaxCap() throws ReflectiveOperationException {
        TaskManager taskManager = TaskManagerRegistry.create(TaskManagerRegistry.FifoTaskManager.name(), 10L);
        Assertions.assertEquals(10L, taskManager.getMaxCapacity());
    }

    @Test
    void testAdd() throws ReflectiveOperationException {
        TaskManager taskManager = TaskManagerRegistry.create(TaskManagerRegistry.FifoTaskManager.name(), 10L);

        taskManager.addProcess(Priority.LOW);
        taskManager.addProcess(Priority.LOW);
        taskManager.addProcess(Priority.HIGH);
        taskManager.addProcess(Priority.LOW);
        taskManager.addProcess(Priority.MEDIUM);

        List<Process> list = taskManager.list();
        Assertions.assertEquals(5, list.size());
    }

    @Test
    void testMaxCapReached() throws ReflectiveOperationException {
        TaskManager taskManager = TaskManagerRegistry.create(TaskManagerRegistry.FifoTaskManager.name(), 5L);

        taskManager.addProcess(Priority.LOW);
        taskManager.addProcess(Priority.HIGH);
        taskManager.addProcess(Priority.HIGH);
        taskManager.addProcess(Priority.HIGH);
        taskManager.addProcess(Priority.MEDIUM);

        Process shouldNotIgnore = taskManager.addProcess(Priority.HIGH);
        Assertions.assertNotNull(shouldNotIgnore);

        List<Process> list = taskManager.list();
        Assertions.assertEquals(5, list.size());
        list.forEach(p -> Assertions.assertNotEquals(p.priority, Priority.LOW));

        list = taskManager.list();
        Assertions.assertEquals(1, list.get(list.size() - 1).pid);

        // kill and add
        Assertions.assertTrue(taskManager.kill(1));
        list = taskManager.list();
        Assertions.assertEquals(5, list.get(list.size() - 1).pid);
    }


    @Test
    void testSortByTime() throws ReflectiveOperationException {
        TaskManager taskManager = TaskManagerRegistry.create(TaskManagerRegistry.FifoTaskManager.name(), 10L);

        List<Process> addedOrder = new ArrayList<>();
        addedOrder.add(taskManager.addProcess(Priority.LOW));
        addedOrder.add(taskManager.addProcess(Priority.LOW));
        addedOrder.add(taskManager.addProcess(Priority.LOW));
        addedOrder.add(taskManager.addProcess(Priority.MEDIUM));
        addedOrder.add(taskManager.addProcess(Priority.HIGH));

        // default sort criteria should be by time
        List<Process> list = taskManager.list();
        Assertions.assertEquals(list, addedOrder);

        list = taskManager.list(SortCriteria.TIME);
        Assertions.assertEquals(list, addedOrder);
    }

    @Test
    void testSortById() throws ReflectiveOperationException {
        TaskManager taskManager = TaskManagerRegistry.create(TaskManagerRegistry.FifoTaskManager.name(), 10L);

        List<Process> addedOrder = new ArrayList<>();
        addedOrder.add(taskManager.addProcess(Priority.LOW));
        addedOrder.add(taskManager.addProcess(Priority.LOW));
        addedOrder.add(taskManager.addProcess(Priority.LOW));
        addedOrder.add(taskManager.addProcess(Priority.MEDIUM));
        addedOrder.add(taskManager.addProcess(Priority.HIGH));

        addedOrder.sort((a, b) -> (int) (a.pid - b.pid));

        List<Process> list = taskManager.list(SortCriteria.ID);
        Assertions.assertEquals(list, addedOrder);
    }

    @Test
    void testSortByPriority() throws ReflectiveOperationException {
        TaskManager taskManager = TaskManagerRegistry.create(TaskManagerRegistry.FifoTaskManager.name(), 10L);

        List<Process> addedOrder = new ArrayList<>();
        addedOrder.add(taskManager.addProcess(Priority.MEDIUM));
        addedOrder.add(taskManager.addProcess(Priority.LOW));
        addedOrder.add(taskManager.addProcess(Priority.LOW));
        addedOrder.add(taskManager.addProcess(Priority.LOW));
        addedOrder.add(taskManager.addProcess(Priority.HIGH));

        addedOrder.sort(Comparator.comparingInt(a -> a.priority.getPriority()));

        List<Process> list = taskManager.list(SortCriteria.PRIORITY);
        Assertions.assertEquals(list, addedOrder);
    }

    @Test
    void testKill() throws ReflectiveOperationException {
        TaskManager taskManager = TaskManagerRegistry.create(TaskManagerRegistry.FifoTaskManager.name(), 10L);

        taskManager.addProcess(Priority.MEDIUM);
        taskManager.addProcess(Priority.LOW);
        taskManager.addProcess(Priority.LOW);
        taskManager.addProcess(Priority.LOW);
        taskManager.addProcess(Priority.HIGH);

        Assertions.assertEquals(5, taskManager.list().size());
        // not existing pid
        Assertions.assertTrue(taskManager.kill(100));
        // nothing should change
        Assertions.assertEquals(5, taskManager.list().size());

        // kill existing one
        Assertions.assertTrue(taskManager.kill(3));
        Assertions.assertEquals(4, taskManager.list().size());
        Assertions.assertFalse(taskManager.list().contains(new Process(3, Priority.LOW)));
    }

    @Test
    void testKillGroup() throws ReflectiveOperationException {
        TaskManager taskManager = TaskManagerRegistry.create(TaskManagerRegistry.FifoTaskManager.name(), 10L);

        taskManager.addProcess(Priority.MEDIUM);
        taskManager.addProcess(Priority.LOW);
        taskManager.addProcess(Priority.LOW);
        taskManager.addProcess(Priority.LOW);
        taskManager.addProcess(Priority.HIGH);

        Assertions.assertEquals(5, taskManager.list().size());
        Assertions.assertTrue(taskManager.killGroup(Priority.LOW));
        Assertions.assertEquals(2, taskManager.list().size());

        Assertions.assertFalse(taskManager.list().contains(new Process(2, Priority.LOW)));
        Assertions.assertFalse(taskManager.list().contains(new Process(3, Priority.LOW)));
        Assertions.assertFalse(taskManager.list().contains(new Process(4, Priority.LOW)));
    }

    @Test
    void testKillAll() throws ReflectiveOperationException {
        TaskManager taskManager = TaskManagerRegistry.create(TaskManagerRegistry.FifoTaskManager.name(), 10L);

        taskManager.addProcess(Priority.MEDIUM);
        taskManager.addProcess(Priority.LOW);
        taskManager.addProcess(Priority.LOW);
        taskManager.addProcess(Priority.LOW);
        taskManager.addProcess(Priority.HIGH);

        Assertions.assertEquals(5, taskManager.list().size());
        Assertions.assertTrue(taskManager.killAll());
        Assertions.assertEquals(0, taskManager.list().size());

        // add after kill
        taskManager.addProcess(Priority.MEDIUM);
        Assertions.assertEquals(1, taskManager.list().size());
        Assertions.assertEquals(1, taskManager.list().get(0).pid);

    }
}

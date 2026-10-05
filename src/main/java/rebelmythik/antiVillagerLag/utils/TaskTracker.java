package rebelmythik.antiVillagerLag.utils;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class TaskTracker {

    private final Queue<ScheduledTask> tasks = new ConcurrentLinkedQueue<>();

    public void track(ScheduledTask task) {
        if (task != null) {
            tasks.add(task);
        }
    }

    public void cancelAll() {
        ScheduledTask task;
        while ((task = tasks.poll()) != null) {
            task.cancel();
        }
    }
}

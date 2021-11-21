package bgu.atd.a1;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The general thread loop behavior:
 * look for a free actor queue
 * try execution
 * execute one action
 * look for suspended actions for the current actor
 * repeat
 *
 * if no actor are available or action queues are empty sleep
 */
public class ActorThreadLoop implements Runnable{
    private final ConcurrentHashMap<String,PrivateState> actors;
    private final ConcurrentHashMap<String,ActorActionsQueue> actorsActionQueues;
    private final ConcurrentHashMap<String, Map<? extends Action<?>,ActionDependencies>> actorSuspendedActionsMap;
    private final Object waitObject;
    private final ActorThreadPool aTPool;
    private boolean isInterrupted = false;

    public ActorThreadLoop(ConcurrentHashMap<String, PrivateState> actors,
                           ConcurrentHashMap<String, ActorActionsQueue> actorsActionQueues,
                           ConcurrentHashMap<String, Map<? extends Action<?>, ActionDependencies>> actorSuspendedActionsMap,
                           Object waitObject,
                           ActorThreadPool aTPool) {
        this.actors = actors;
        this.actorsActionQueues = actorsActionQueues;
        this.actorSuspendedActionsMap = actorSuspendedActionsMap;
        this.waitObject = waitObject;
        this.aTPool = aTPool;
    }

    public void run() {
        while (!this.isInterrupted){
            boolean actionExecutionOccurred = false;
            for (String actorID : this.actorsActionQueues.keySet()) // trying to acquire action queue
            {
                // overall interruption handling mechanism
                if(Thread.currentThread().isInterrupted()){
                    this.isInterrupted = true;
                    break;
                }

                Queue<? extends Action<?>> actionQ = this.actorsActionQueues.get(actorID).tryAcquire();
                if (actionQ != null) { // acquired action queue successfully
                    if (actionQ.size() != 0) {
                        actionQ.remove().handle(this.aTPool, actorID, this.actors.get(actorID));
                        actionExecutionOccurred = true;
                    }
                    try {
                        this.actorsActionQueues.get(actorID).releaseActorQueue();
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
                // actor suspended actions handling procedure
                for (Action<?> suspendedAction : this.actorSuspendedActionsMap.get(actorID).keySet()) {
                    if (this.actorSuspendedActionsMap.get(actorID).get(suspendedAction).isAllResolved()) {
                        this.aTPool.submit(suspendedAction, actorID, this.actors.get(actorID)); // submit back to original actor
                        this.actorSuspendedActionsMap.get(actorID).remove(suspendedAction);
                    }
                }

            }
            // no work available -> wait
            if (!actionExecutionOccurred && !isInterrupted) {
                try {
                    this.waitObject.wait();
                } catch (InterruptedException e) {
                    this.isInterrupted = true;
                }
            }
        }
    }
}

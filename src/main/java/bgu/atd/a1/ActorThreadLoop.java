package bgu.atd.a1;

import java.util.Map;
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
 * TODO: insert interruption handling for shutdown
 */
public class ActorThreadLoop implements Runnable{
    private final ConcurrentHashMap<String,PrivateState> actors;
    private final ConcurrentHashMap<String,ActorActionsQueue> actorsActionQueues;
    private final ConcurrentHashMap<String, Map<? extends Action<?>,ActionDependencies>> actorSuspendedActionsMap;

    public ActorThreadLoop(ConcurrentHashMap<String, PrivateState> actors, ConcurrentHashMap<String, ActorActionsQueue> actorsActionQueues, ConcurrentHashMap<String, Map<? extends Action<?>, ActionDependencies>> actorSuspendedActionsMap) {
        this.actors = actors;
        this.actorsActionQueues = actorsActionQueues;
        this.actorSuspendedActionsMap = actorSuspendedActionsMap;
    }

    public void run() {
        //TODO: implement general actor thread behavior
    }
}

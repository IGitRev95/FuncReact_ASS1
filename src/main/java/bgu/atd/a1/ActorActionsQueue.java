package bgu.atd.a1;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An actor actions queue.
 */

public class ActorActionsQueue {

    private final ConcurrentLinkedQueue<Action<?>> actionsQueue = new ConcurrentLinkedQueue<Action<?>>();
    private final AtomicBoolean isExecutedNow = new AtomicBoolean(false);


    /**
     * Tries to acquire the actor's action queue.
     * @return the actor's action queue if successful and null otherwise.
     */
    public Queue<? extends Action<?>> tryAcquire() {
        if(this.isExecutedNow.compareAndSet(false,true)){ // queue is free - replace with the try lock if locked allow queue access
            return getActionsQueue();
        }
        else {
            return null;
        }
    }

    public boolean enQueueAction(Action<?> action){ // "free" to always adding actions to the queue
        return this.actionsQueue.add(action);
    }

    private Queue<? extends Action<?>> getActionsQueue() { // single method for accessing the actionQ.
        return actionsQueue;

    }

    /**
     * releases the actor action queue from execution state and enables other threads to access it.
     * @throws Exception - in case of multiple acquisition of the action queue an exception is thrown.
     */
    public void releaseActorQueue() throws Exception{ // so after finishing current execution (to complete or not) release the actor queue for further execution.
        if(!this.isExecutedNow.compareAndSet(true, false)){
            throw new Exception("parallel actor queue acquisition");
        }
    }

}

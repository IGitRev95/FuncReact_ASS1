package bgu.atd.a1;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * An actor actions queue.
 * TODO: needed functionality:
 * { read-write lock for inserting actions while in use,
 *   try acquire - trying to get the actor (the queue) for execution and non-blocking in case of already in-use case,
 *   }
 */

public class ActorActionsQueue {

    private final ConcurrentLinkedQueue<Action<?>> actionsQueue = new ConcurrentLinkedQueue<Action<?>>();
//    private final ConcurrentLinkedQueue<? extends Action<?>> actionsQueue = new ConcurrentLinkedQueue<Action<?>>(); ? extends - problem
    /**
     * Fear of main delivery thread to get stuck upon trying writing to the queue
     * reading and writing should be quick actions so should not be a problem
      */

    //    private ReadWriteLock readWriteLock = new ReentrantReadWriteLock(); // no need in use of concurrent queue

    private final AtomicBoolean isExecutedNow = new AtomicBoolean(false);

    public Queue<? extends Action<?>> tryAcquire() throws Exception {
        if(this.isExecutedNow.compareAndSet(false,true)){ // queue is free
            if(!this.actionsQueue.isEmpty()){
                return getActionsQueue();
            }
            throw new Exception("Queue is empty");
        }
        throw new Exception("Queue is occupied");
    }

    public boolean enqueueAction(Action<?> action){
        return this.actionsQueue.add(action);
    }

    private Queue<? extends Action<?>> getActionsQueue() {
        return actionsQueue;

    }
}

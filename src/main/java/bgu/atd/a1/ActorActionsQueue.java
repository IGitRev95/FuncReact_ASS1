package bgu.atd.a1;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
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

    private final Lock isExecutedNowLock = new ReentrantLock();

//    public boolean()

//TODO modify
    public Queue<? extends Action<?>> tryAcquire() throws Exception {
        if(this.isExecutedNow.compareAndSet(false,true)){ // queue is free - replace with the try lock if locked allow queue access
            if(!this.actionsQueue.isEmpty()){
                return getActionsQueue();
            }
            throw new Exception("Queue is empty");
        }
        throw new Exception("Queue is occupied");

    }

//    public Queue<? extends Action<?>> tryAcquire() throws Exception {
//        if(this.isExecutedNow.compareAndSet(false,true)){ // queue is free - replace with the try lock if locked allow queue access
//            if(!this.actionsQueue.isEmpty()){
//                return getActionsQueue();
//            }
//            throw new Exception("Queue is empty");
//        }
//        throw new Exception("Queue is occupied");
//
//    }

    public boolean enQueueAction(Action<?> action){ // "free" to always adding actions to the queue
        return this.actionsQueue.add(action);
    }

    private Queue<? extends Action<?>> getActionsQueue() { // single method for accessing the actionQ.
        return actionsQueue;

    }

    public void releaseActorQueue(){ // so after finishing current execution (to complete or not) release the actor queue for further execution.
        this.isExecutedNowLock.unlock();
    }

}

package bgu.atd.a1;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * represents an actor thread pool - to understand what this class does please
 * refer to your assignment.
 *
 * Note for implementors: you may add methods and synchronize any of the
 * existing methods in this class *BUT* you must be able to explain why the
 * synchronization is needed. In addition, the methods you add can only be
 * private, protected or package protected - in other words, no new public
 * methods
 */
public class ActorThreadPool {
	
	private final ConcurrentHashMap<String,PrivateState> actors = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<String,ActorActionsQueue> actorsActionQueues = new ConcurrentHashMap<>();
	private final Object threadWaitObject;
	private final ThreadPoolExecutor executor;
	private final AtomicInteger submissionCounter;

	/**
	 * creates a {@link ActorThreadPool} which has nthreads. Note, threads
	 * should not get started until calling to the {@link #start()} method.
	 *
	 * Implementors note: you may not add other constructors to this class nor
	 * you allowed to add any other parameter to this constructor - changing
	 * this may cause automatic tests to fail..
	 *
	 * @param nthreads
	 *            the number of threads that should be started by this thread
	 *            pool
	 */
	public ActorThreadPool(int nthreads) {
		BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();
		// add the general thread behavior runnable to the executor {nthreas} times. @ActorThreadLoop
		this.submissionCounter = new AtomicInteger(0);
		this.threadWaitObject = new Object();
		this.executor = new ThreadPoolExecutor(nthreads,nthreads,0, TimeUnit.SECONDS,workQueue);
	}

	/**
	 * getter for actors
	 * @return actors
	 */
	public Map<String, PrivateState> getActors(){
		return this.actors;
	}
	
	/**
	 * getter for actor's private state
	 * @param actorId actor's id
	 * @return actor's private state
	 */
	public PrivateState getPrivateState(String actorId){
		return this.actors.get(actorId);
	}


	/**
	 * submits an action into an actor to be executed by a thread belongs to
	 * this thread pool
	 *
	 * @param action
	 *            the action to execute
	 * @param actorId
	 *            corresponding actor's id
	 * @param actorState
	 *            actor's private state (actor's information)
	 */
	public void submit(Action<?> action, String actorId, PrivateState actorState) {
		if(!this.actors.containsKey(actorId)){
			this.addNewActor(actorId,actorState);
		}
		this.actorsActionQueues.get(actorId).enQueueAction(action);
		this.submissionCounter.getAndIncrement(); // Overall waiting tasks count - meant to prevent a notifying missing
		synchronized (threadWaitObject){
			this.threadWaitObject.notify(); // wake a waiting threads when a new action is submitted
		}
	}

	/**
	 * closes the thread pool - this method interrupts all the threads and waits
	 * for them to stop - it is returns *only* when there are no live threads in
	 * the queue.
	 *
	 * after calling this method - one should not use the queue anymore.
	 *
	 * @throws InterruptedException
	 *             if the thread that shut down the threads is interrupted
	 */
	public void shutdown() throws InterruptedException {
		this.executor.shutdownNow();
		this.executor.awaitTermination(10,TimeUnit.SECONDS); // current thread wait for all working threads termination
	}

	/**
	 * start the threads belonging to this thread pool
	 */
	public void start() {
		for(int i = 0; i<this.executor.getCorePoolSize(); i++){
			this.executor.execute(new ActorThreadLoop(actors, actorsActionQueues, threadWaitObject, this, this.submissionCounter));
		}

	}

	private boolean addNewActor(String actorID,PrivateState actorState){
		return (this.actors.putIfAbsent(actorID, actorState) == null) & (this.actorsActionQueues.putIfAbsent(actorID, new ActorActionsQueue()) == null);
	}
}

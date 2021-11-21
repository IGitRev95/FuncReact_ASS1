package bgu.atd.a1;

import java.util.Map;
import java.util.concurrent.*;

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
	
	private final ConcurrentHashMap<String,PrivateState> actors = new ConcurrentHashMap<String,PrivateState>();
	private final ConcurrentHashMap<String,ActorActionsQueue> actorsActionQueues = new ConcurrentHashMap<String,ActorActionsQueue>();
	private final ConcurrentHashMap<String, Map<? extends Action<?>,ActionDependencies>> actorSuspendedActionsMap = new ConcurrentHashMap<String,Map<? extends Action<?>, ActionDependencies>>();
	private final Object threadWaitObject = new Object();
	private final ThreadPoolExecutor executor; // TODO: replace with custom

	//TODO:
	// 1.implement custom executor
	// 2.update actorThreadPool constructor
	// 3.update actorThreadPool start


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
		BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();
		// add the general thread behavior runnable to the executor {nthreas} times. @ActorThreadLoop
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
		// TODO:should the private state (log) needed to be updated upon action submission or action completion?
		this.actorsActionQueues.get(actorId).enQueueAction(action);
		this.actors.get(actorId).addRecord(action.getActionName());
		this.threadWaitObject.notify();
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
		this.executor.awaitTermination(10,TimeUnit.SECONDS);
	}

	/**
	 * start the threads belonging to this thread pool
	 */
	public void start() {
		for(int i = 0; i<this.executor.getCorePoolSize(); i++){
			this.executor.execute(new ActorThreadLoop(actors, actorsActionQueues, actorSuspendedActionsMap, threadWaitObject, this));
		}

	}

}

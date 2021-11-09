package bgu.atd.a1;

import java.util.Collection;
import java.util.LinkedHashMap;

/**
 * an abstract class that represents an action that may be executed using the
 * {@link ActorThreadPool}
 *
 * Note for implementors: you may add methods and synchronize any of the
 * existing methods in this class *BUT* you must be able to explain why the
 * synchronization is needed. In addition, the methods you add to this class can
 * only be private!
 *
 * @param <R> the action result type
 */
public abstract class Action<R> {

    private String actionName;
    private final Promise<R> resultPromise = new Promise<R>();
    private ActorThreadPool actorPool;
    private boolean isStarting = true;
    private final LinkedHashMap<Collection<? extends Action<?>>, callback> actionsToCallbackMap = new LinkedHashMap<Collection<? extends Action<?>>, callback>();


	/**
     * start handling the action - note that this method is protected, a thread
     * cannot call it directly.
     */
    protected abstract void start();
    

    /**
    *
    * start/continue handling the action
    *
    * this method should be called in order to start this action
    * or continue its execution in the case where it has been already started.
    *
    * IMPORTANT: this method is package protected, i.e., only classes inside
    * the same package can access it - you should *not* change it to
    * public/private/protected
    *
    */
   /*package*/ final void handle(ActorThreadPool pool, String actorId, PrivateState actorState) {
       if (isStarting)
       {
           actorPool = pool;
           start();
           isStarting = false;
       }
       else
       {
           for (Collection<? extends Action<?>> actionList : actionsToCallbackMap.keySet()) {
               if (allAreDone(actionList)) {
                   actionsToCallbackMap.get(actionList).call();
                   actionsToCallbackMap.remove(actionList);
               }
           }
       }

   }
    
    private boolean allAreDone(Collection<? extends Action<?>> actionList)
    {
        for (Action<?> action : actionList)
        {
            if (!action.getResult().isResolved())
            {
                return false;
            }
        }
        return true;
    }
    /**
     * add a callback to be executed once *all* the given actions results are
     * resolved
     * 
     * Implementors note: make sure that the callback is running only once when
     * all the given actions completed.
     *
     * @param actions the actions that need to be run
     * @param callback the callback to execute once all the results are resolved
     */
    protected final void then(Collection<? extends Action<?>> actions, callback callback) {
       	actionsToCallbackMap.put(actions, callback);
   
    }

    /**
     * resolve the internal result - should be called by the action derivative
     * once it is done.
     *
     * @param result - the action calculated result
     */
    protected final void complete(R result) {
       	resultPromise.resolve(result);
   
    }
    
    /**
     * @return action's promise (result)
     */
    public final Promise<R> getResult() {
    	return resultPromise;
    }
    
    /**
     * send an action to another actor
     * 
     * @param action
     * 				the action
     * @param actorId
     * 				actor's id
     * @param actorState
	 * 				actor's private state (actor's information)
     */
	public void sendMessage(Action<?> action, String actorId, PrivateState actorState){
        actorPool.submit(action, actorId, actorState);
	}
	
	/**
	 * set action's name
	 * @param actionName
     *              the action's name
	 */
	public void setActionName(String actionName){
        this.actionName = actionName;
	}
	
	/**
	 * @return action's name
	 */
	public String getActionName(){
        return actionName;
	}
}

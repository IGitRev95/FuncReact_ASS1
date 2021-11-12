package bgu.atd.a1;

import java.util.ArrayList;
import java.util.Collection;


public class ActionDependencies {
    /**
     * ActionDependencies - an Action dependencies data structure
     * dependenciesCollection - the actual dependencies' container
     * remainingActionCollection - non-resolved action names list
     * allIsResolved - implies which all the dependencies are resolved
     *
     */

    private final Collection<Action<?>> dependenciesCollection;
    //    private final Collection<? extends Action> dependenciesCollection; //TODO: check the use of <? extends Action<?>> - empty constructor and add dependency problem related
    private final Collection<String> remainingActionsCollection;
    private boolean allIsResolved;

    /**
     * empty constructor - creates an empty dependencies collection
     */
    public ActionDependencies() {
        this.dependenciesCollection = new ArrayList<Action<?>>();
        this.remainingActionsCollection = new ArrayList<String>();
        resolvedAllCheck();
    }

    /**
     * constructor - creates dependencies collection from a give collection
     */
    public ActionDependencies(Collection<Action<?>> dependenciesCollection) {
        this.dependenciesCollection = dependenciesCollection;
        this.remainingActionsCollection = new ArrayList<String>();
        for (Action<?> action : dependenciesCollection){
            this.remainingActionsCollection.add(action.getActionName());
        }
        resolvedAllCheck();
    }

//    public ActionDependencies(Collection<? extends Action> dependenciesCollection) {
//        this.dependenciesCollection = dependenciesCollection;
//        this.remainingActionCollection = new ArrayList<String>();
//        for (Action action : dependenciesCollection){
//            this.remainingActionCollection.add(action.getActionName());
//        }
//        this.allIsResolved = this.remainingActionCollection.isEmpty();
//    }

    /**
     * add new action to the dependency collection
     * @param newDependency - the new action as dependency
     * @return - addition Success bool
     */
    public boolean addDependency(Action<?> newDependency){
        boolean additionSuccess = this.dependenciesCollection.add(newDependency);
        if(additionSuccess && !newDependency.getResult().isResolved()) {
            this.remainingActionsCollection.add(newDependency.getActionName());
            resolvedAllCheck();
        }
        return additionSuccess;
    }

    public Collection<? extends Action<?>> getDependenciesCollection() {
        return dependenciesCollection;
    }

    public boolean getAllIsResolved(){
        return allIsResolved;
    }

    /**
     * upon dependency satisfaction remove the dependency from the remaining dependencies list
     * @param action - the satisfied dependency
     */
    public void removeResolvedDependency(Action<?> action){
        if(!action.getResult().isResolved())
            throw new IllegalStateException(" Action promise is not resolved, action name: "+action.getActionName());
        boolean isRemoved = this.remainingActionsCollection.remove(action.getActionName());
        if(isRemoved)
            resolvedAllCheck();
    }

    /**
     * updating allIsResolved value according to remainingActionsCollection size
     */
    private void resolvedAllCheck(){
        this.allIsResolved = this.remainingActionsCollection.isEmpty();
    }


}

package bgu.atd.a1;

import java.util.*;

public class ActionDependencies {
    /**
     * ActionDependencies - an Action dependencies data structure
     * dependenciesCollection - the actual dependencies' container
     * remainingActionCollection - <ActionName, Action> non-resolved action names container
     * allIsResolved - implies which all the dependencies are resolved
     */

    private final Collection<? extends Action<?>> dependenciesCollection; // used to store and quick access to the dependencies actions results
    private final LinkedList< Action<?>> remainingActionsCollection; // used to control in remaining dependencies
    private boolean allIsResolved;

    /**
     * constructor - creates dependencies collection from a give collection
     */
    public ActionDependencies(Collection<? extends Action<?>> dependenciesCollection) {
        this.dependenciesCollection = dependenciesCollection;
        this.remainingActionsCollection = new LinkedList<>();
        this.remainingActionsCollection.addAll(dependenciesCollection);
        resolvedAllCheck();
    }

    public Collection<? extends Action<?>> getDependenciesCollection() {
        return dependenciesCollection;
    }

    /**
     * the method includes the updating stage of the overall isAllResolved value.
     * @return - return bool value of all the dependencies are resolved.
     */
    public synchronized boolean isAllResolved(){ // method is synchronized causes it involves entire data structure updating
        if(!this.allIsResolved){
            List<Action<?>> remainActionNames = new ArrayList<>(this.remainingActionsCollection);
            for (Action<?> action : remainActionNames){
                if(action.getResult().isResolved()){
                    this.remainingActionsCollection.remove(action);
                }
            }
            resolvedAllCheck();
        }
        return this.allIsResolved;
    }

    /**
     * updating allIsResolved value according to remainingActionsCollection size
     */
    private void resolvedAllCheck(){
        this.allIsResolved = this.remainingActionsCollection.isEmpty();
    }


}

package bgu.atd.a1;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

//TODO: Build Tests

public class ActionDependencies {
    /**
     * ActionDependencies - an Action dependencies data structure
     * dependenciesCollection - the actual dependencies' container
     * remainingActionCollection - <ActionName, Action> non-resolved action names container
     * allIsResolved - implies which all the dependencies are resolved
     *
     */

    private final Collection<? extends Action<?>> dependenciesCollection; // used to store and quick access to the dependencies actions results
    private final HashMap<String, Action<?>> remainingActionsCollection; // used to control in remaining dependencies
    private boolean allIsResolved;

    /**
     * empty constructor - creates an empty dependencies collection
     */
    public ActionDependencies() {
        this.dependenciesCollection = new ArrayList<>();
        this.remainingActionsCollection = new HashMap<>();
        resolvedAllCheck();
    }

    /**
     * constructor - creates dependencies collection from a give collection
     */
    public ActionDependencies(Collection<? extends Action<?>> dependenciesCollection) {
        this.dependenciesCollection = dependenciesCollection;
        this.remainingActionsCollection = new HashMap<>();
        for (Action<?> action : dependenciesCollection){
            this.remainingActionsCollection.put(action.getActionName(),action);
        }
        resolvedAllCheck();
    }

    public Collection<? extends Action<?>> getDependenciesCollection() {
        return dependenciesCollection;
    }

    /**
     * the method includes the updating stage of the overall isAllResolved value.
     * @return - return bool value of all the dependencies are resolved.
     */
    public boolean isAllResolved(){ // changed cause of flow constrains
        if(!this.allIsResolved){
            for (String actionName : this.remainingActionsCollection.keySet()){
                if(this.remainingActionsCollection.get(actionName).getResult().isResolved()){
                    this.remainingActionsCollection.remove(actionName);
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

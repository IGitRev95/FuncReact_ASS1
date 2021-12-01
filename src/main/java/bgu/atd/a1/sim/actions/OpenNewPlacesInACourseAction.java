package bgu.atd.a1.sim.actions;

import bgu.atd.a1.Action;
import bgu.atd.a1.sim.privateStates.CoursePrivateState;

/**
 *  Increases the number of available spaces for the given course.
 *  Should be initially submitted to the course's actor.
 */
public class OpenNewPlacesInACourseAction extends Action<Boolean> {
    private final Integer extensionAmount;

    public OpenNewPlacesInACourseAction(Integer extensionAmount) {
        this.setActionName("Add Spaces");
        this.extensionAmount = extensionAmount;
    }

    @Override
    protected void start() {
        CoursePrivateState coursePS = ((CoursePrivateState)this.actorState);
        int availableSpots = coursePS.getAvailableSpots();
        if(availableSpots>-1 && this.extensionAmount>=0){ // Course is open check && legal amount
            coursePS.setAvailableSpots(availableSpots+this.extensionAmount);
            this.complete(true);
        }else{this.complete(false);}
    }
}

package bgu.atd.a1.sim.actions;

import bgu.atd.a1.Action;
import bgu.atd.a1.sim.privateStates.CoursePrivateState;
import com.sun.org.apache.xpath.internal.operations.Bool;

import java.util.List;

/**
    This class is used to initiate a new course actor with its given parameters.
 */
public class NewCourseInitAction extends Action<Boolean> {
    private final Integer availableSpots;
    private final List<String> prequisites;

    public NewCourseInitAction(Integer availableSpots, List<String> prequisites) {
        this.availableSpots = availableSpots;
        this.prequisites = prequisites;
    }

    @Override
    protected void start() {
        CoursePrivateState coursePS = ((CoursePrivateState)this.actorState);
        coursePS.setAvailableSpots(this.availableSpots);
        coursePS.setPrequisites(this.prequisites);
        this.complete(true);
    }
}

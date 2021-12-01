package bgu.atd.a1.sim.actions;

import bgu.atd.a1.Action;
import bgu.atd.a1.sim.privateStates.CoursePrivateState;
import bgu.atd.a1.sim.privateStates.DepartmentPrivateState;

import java.util.ArrayList;
import java.util.List;

/**
 * Opens a new course in a specified department.
 * Should be initially submitted to the Department's actor.
 */
public class OpenNewCourseAction extends Action<Boolean> {
    private final String courseName;
    private final Integer availableSpots;
    private final List<String> prequisites;

    public OpenNewCourseAction(String courseName, Integer availableSpots, List<String> prequisites) {
        this.setActionName("Open Course");
        this.courseName = courseName;
        this.availableSpots = availableSpots;
        this.prequisites = prequisites;
    }

    @Override
    protected void start() {
        List<String> departmentCourseList = ((DepartmentPrivateState)this.actorState).getCourseList();
        if(!departmentCourseList.contains(this.courseName)){
            departmentCourseList.add(this.courseName);
            CoursePrivateState newCoursePS = new CoursePrivateState();
            NewCourseInitAction newCourseInitAction = new NewCourseInitAction(this.availableSpots, this.prequisites);
            sendMessage(newCourseInitAction,this.courseName, newCoursePS);
            this.complete(true);
        }else{this.complete(false);} // False - means this course existed previously to this request
    }
}

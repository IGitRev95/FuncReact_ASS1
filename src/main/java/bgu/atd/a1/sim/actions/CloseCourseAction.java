package bgu.atd.a1.sim.actions;

import bgu.atd.a1.Action;
import bgu.atd.a1.sim.privateStates.CoursePrivateState;
import bgu.atd.a1.sim.privateStates.DepartmentPrivateState;

import java.util.ArrayList;
import java.util.List;

/**
 * Closes a course. Should unregister registered students and remove the course from the department courses' list
 * and the grade-sheets of the students. The number of available spaces of the closed
 * course should be updated to -1. DO NOT remove its actor. After closing the course, all requests for
 * registration should be denied.
 * Should be initially submitted to the department's actor
 */
public class CloseCourseAction extends Action<Boolean> {

    private final String courseName;

    public CloseCourseAction(String courseName){
        this.setActionName("Close Course");
        this.courseName = courseName;
    }

    @Override
    protected void start() {
        DepartmentPrivateState departmentPS = (DepartmentPrivateState) this.actorState;
        if (departmentPS.getCourseList().contains(this.courseName))
        {
            //remove from department course list
            departmentPS.getCourseList().remove(this.courseName);
            CourseSelfCloseAction courseSelfClose = new CourseSelfCloseAction();
            List<Action<Boolean>> actions = new ArrayList<>();
            actions.add(courseSelfClose);
            then(actions,()->{
                if(actions.get(0).getResult().get()){
                    this.complete(true);
                }else{
                    this.complete(false);
                }
            });
            this.sendMessage(courseSelfClose,this.courseName,new CoursePrivateState());
        } else {this.complete(false);}

    }

}

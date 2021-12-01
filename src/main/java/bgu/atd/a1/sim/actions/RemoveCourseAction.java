package bgu.atd.a1.sim.actions;

import bgu.atd.a1.Action;
import bgu.atd.a1.sim.privateStates.DepartmentPrivateState;

public class RemoveCourseAction extends Action<Boolean> {

    private final String courseName;

    public RemoveCourseAction(String courseName) {
        this.setActionName("Remove Course");
        this.courseName = courseName;
    }

    @Override
    protected void start() {
        DepartmentPrivateState departmentPS = (DepartmentPrivateState) this.actorState;
        if (departmentPS.getCourseList().contains(this.courseName))
        {
            departmentPS.getCourseList().remove(this.courseName);
            this.complete(true);
        }
        else {this.complete(false);}
    }
}

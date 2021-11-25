package bgu.atd.a1.sim.actions;

import bgu.atd.a1.Action;
import bgu.atd.a1.sim.privateStates.CoursePrivateState;
import bgu.atd.a1.sim.privateStates.DepartmentPrivateState;
import bgu.atd.a1.sim.privateStates.StudentPrivateState;

import java.util.List;

/**
 * Adds a new student to a given department.
 * Should be initially submitted to the Department's actor.
 */
public class AddStudentAction extends Action<Boolean> {
    private String studentName;

    public AddStudentAction(String studentName) {
        this.setActionName("AddStudentAction");
        this.studentName = studentName;
    }

    @Override
    protected void start() {
        List<String> departmentStudentList = ((DepartmentPrivateState)this.actorState).getStudentList();
        if(!departmentStudentList.contains(this.studentName)){
            departmentStudentList.add(this.studentName);
//            StudentPrivateState newStudentPS = new StudentPrivateState();
//            sendMessage(actorInit,this.studentName, newCoursePS);//TODO should we create the student actor ahead of time or just when the first message is submitted to it?
            this.complete(true);
        }else{this.complete(false);} // False - means this student existed previously to this request
    }
}
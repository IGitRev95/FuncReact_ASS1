package bgu.atd.a1.sim.actions;

import bgu.atd.a1.Action;
import bgu.atd.a1.sim.privateStates.CoursePrivateState;
import bgu.atd.a1.sim.privateStates.DepartmentPrivateState;

import java.util.ArrayList;
import java.util.List;

public class CloseCourseAction extends Action<Boolean> {

    private String courseName;

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

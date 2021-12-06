package bgu.atd.a1.sim.actions;

import bgu.atd.a1.Action;
import bgu.atd.a1.sim.privateStates.CoursePrivateState;

import java.util.ArrayList;
import java.util.List;

/**
 * Course self-closing procedure
 */
public class CourseSelfCloseAction extends Action<Boolean> {
    public CourseSelfCloseAction() {
        this.setActionName("Course Self Close");
    }

    @Override
    protected void start() {
        CoursePrivateState coursePS = (CoursePrivateState) this.actorState;
        //first setting available spots to -1 to prevent more students registrations
        coursePS.setAvailableSpots(-1);
        List<Action<Boolean>> actions = new ArrayList<>();
        // unregister procedure for each registered student
        for (String student : coursePS.getRegStudents())
        {
            actions.add(new UnregisterAction(student));
        }
        if(actions.isEmpty()){
            this.complete(true);
        }else{
            then(actions,()->{
                //if each of unregistering succeeded so the students list is empty
                if(coursePS.getRegStudents().isEmpty()){
                    this.complete(true);
                }else{
                    this.complete(false);
                }
            });
            for (Action<Boolean> action: actions)
            {
                this.sendMessage(action, this.actorId, new CoursePrivateState());
            }
        }
    }
}

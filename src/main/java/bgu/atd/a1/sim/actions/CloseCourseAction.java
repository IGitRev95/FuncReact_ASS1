package bgu.atd.a1.sim.actions;

import bgu.atd.a1.Action;
import bgu.atd.a1.sim.privateStates.CoursePrivateState;
import bgu.atd.a1.sim.privateStates.StudentPrivateState;

import java.util.ArrayList;
import java.util.List;

public class CloseCourseAction extends Action<Boolean> {

    public CloseCourseAction(){
        this.setActionName("CloseCourseAction");
    }

    @Override
    protected void start() {
        CoursePrivateState coursePS = (CoursePrivateState) this.actorState;
        List<Action<Boolean>> actions = new ArrayList<>();
        actions.add(new RemoveCourseAction(this.actorId));
        then(actions, () -> {
            if (actions.get(0).getResult().get())
            {
                coursePS.getRegStudents().clear();
                coursePS.setAvailableSpots(-1);
                for (String student : coursePS.getRegStudents())
                {
                    this.sendMessage(new RemoveFromGradeSheetAction(this.actorId), student, new StudentPrivateState());
                }
                this.complete(true);

            }
            else {this.complete(false);}

        });
    }
}

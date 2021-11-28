package bgu.atd.a1.sim.actions;

import bgu.atd.a1.Action;
import bgu.atd.a1.sim.privateStates.StudentPrivateState;

public class AddToGradeSheetAction extends Action<Boolean> {

    private final Integer studentGrade;
    private final String courseName;

    public AddToGradeSheetAction(String courseName, Integer studentGrade)
    {
        this.courseName = courseName;
        this.studentGrade = studentGrade;
        this.setActionName("AddToGradeSheetAction");
    }

    @Override
    protected void start() {
        StudentPrivateState studentPS = (StudentPrivateState) this.actorState;
        if (studentPS.getGrades().containsKey(this.courseName))
        {
            this.complete(false);
        }
        else {
            studentPS.getGrades().put(this.courseName, this.studentGrade);
            this.complete(true);
        }
    }
}

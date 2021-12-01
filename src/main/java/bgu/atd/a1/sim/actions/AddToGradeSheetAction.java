package bgu.atd.a1.sim.actions;

import bgu.atd.a1.Action;
import bgu.atd.a1.sim.privateStates.StudentPrivateState;

public class AddToGradeSheetAction extends Action<Boolean> {

    private final Integer studentGrade;
    private final String courseName;
    private final String[] prereq;

    public AddToGradeSheetAction(String courseName, Integer studentGrade, String[] prereq)
    {
        this.setActionName("Add To GradeSheet");
        this.courseName = courseName;
        this.studentGrade = studentGrade;
        this.prereq = prereq;
    }

    @Override
    protected void start() {
        StudentPrivateState studentPS = (StudentPrivateState) this.actorState;
        if (studentPS.getGrades().containsKey(this.courseName))
        {
            this.complete(false);
        }
        else {
            for (String course : prereq)
            {
                if (!studentPS.getGrades().containsKey(course))
                {
                    this.complete(false);
                    return;
                }
            }
            studentPS.getGrades().put(this.courseName, this.studentGrade);
            this.complete(true);
        }
    }
}

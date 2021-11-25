package bgu.atd.a1.sim.actions;

import bgu.atd.a1.Action;

public class ParticipatingInCourseAction extends Action<Boolean> {
    private final String studentName;
    private final Integer studentGrade;
    //TODO:
    // Check prequisiotions
    // Check open course ( spots!=-1 )

    public ParticipatingInCourseAction(String studentName) {
        this.studentName = studentName;
        this.studentGrade = null;
    }

    public ParticipatingInCourseAction(String studentName, Integer studentGrade) {
        this.studentName = studentName;
        this.studentGrade = studentGrade;
    }

    @Override
    protected void start() {

    }
}

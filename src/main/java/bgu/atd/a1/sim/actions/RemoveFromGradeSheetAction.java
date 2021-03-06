package bgu.atd.a1.sim.actions;

import bgu.atd.a1.Action;
import bgu.atd.a1.sim.privateStates.StudentPrivateState;

import java.util.Map;

/**
 * Removes course from a student grade sheet
 * Should be initially submitted to the Student's actor.
 */
public class RemoveFromGradeSheetAction extends Action<Boolean> {
    private final String courseName;

    public RemoveFromGradeSheetAction(String courseName) {
        this.setActionName("Remove Course From GradeSheet");
        this.courseName = courseName;
    }

    @Override
    protected void start() {
        StudentPrivateState studentPS = ((StudentPrivateState) this.actorState);
        Map<String, Integer> studentGradeSheet = studentPS.getGrades();
        studentGradeSheet.remove(this.courseName);
        this.complete(true);
    }
}

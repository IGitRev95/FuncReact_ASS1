package bgu.atd.a1.sim.actions;

import bgu.atd.a1.Action;
import bgu.atd.a1.sim.privateStates.StudentPrivateState;

import java.util.HashMap;

public class GetGradeSheetAction extends Action<HashMap<String, Integer>> {
    public GetGradeSheetAction() {
        this.setActionName("Get GradeSheet");
    }

    @Override
    protected void start() {
        StudentPrivateState studentPS = (StudentPrivateState) this.actorState;
        complete(studentPS.getGrades());
    }
}

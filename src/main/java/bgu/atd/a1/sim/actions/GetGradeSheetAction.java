package bgu.atd.a1.sim.actions;

import bgu.atd.a1.Action;
import bgu.atd.a1.sim.Computer;
import bgu.atd.a1.sim.privateStates.StudentPrivateState;

import java.util.HashMap;
import java.util.List;

public class GetGradeSheetAction extends Action<HashMap<String, Integer>> {
    public GetGradeSheetAction() {
        this.setActionName("GetGradeSheetAction");
    }

    @Override
    protected void start() {
        StudentPrivateState studentPS = (StudentPrivateState) this.actorState;
        complete(studentPS.getGrades());
    }
}

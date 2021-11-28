package bgu.atd.a1.sim.actions;

import bgu.atd.a1.Action;
import bgu.atd.a1.sim.Computer;
import bgu.atd.a1.sim.privateStates.DepartmentPrivateState;
import bgu.atd.a1.sim.privateStates.StudentPrivateState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class CheckAdministrativeObligationsAction extends Action<Boolean> {

    private final List<String> obligations;

    public CheckAdministrativeObligationsAction(List<String> obligations) {
        this.obligations = obligations;
        this.setActionName("CheckAdministrativeObligationsAction");
    }

    @Override
    protected void start() {
        DepartmentPrivateState departmentPS = (DepartmentPrivateState) this.actorState;
        HashMap<String, GetGradeSheetAction> getGSActions = new HashMap<>();
        GetComputerAction getComp = new GetComputerAction();
        List<GetComputerAction> actions = new ArrayList<>();
        actions.add(getComp);
        then(actions, ()-> {
            Computer comp = actions.get(0).getResult().get();
           for (String student : departmentPS.getStudentList()){
               getGSActions.put(student, new GetGradeSheetAction());
           }
           Collection<GetGradeSheetAction> gradeSheets = getGSActions.values();
           then(gradeSheets, () -> {
               for (String student : getGSActions.keySet()) {
                   SetSignatureAction setSigAction = new SetSignatureAction(
                           comp.checkAndSign(obligations, getGSActions.get(student).getResult().get()));
                   this.sendMessage(setSigAction,student, new StudentPrivateState());
               }
           });
        });


    }
}

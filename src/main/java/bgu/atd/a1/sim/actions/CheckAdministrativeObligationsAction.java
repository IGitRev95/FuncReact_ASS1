package bgu.atd.a1.sim.actions;

import bgu.atd.a1.Action;
import bgu.atd.a1.sim.Computer;
import bgu.atd.a1.sim.Warehouse;
import bgu.atd.a1.sim.privateStates.DepartmentPrivateState;
import bgu.atd.a1.sim.privateStates.StudentPrivateState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class CheckAdministrativeObligationsAction extends Action<Boolean> {

    private final List<String> obligations;
    private final List<String> students;
    private final String type;

    //TODO add Student list
    public CheckAdministrativeObligationsAction(List<String> obligations,List<String> students, String type) {
        this.setActionName("Administrative Check");
        this.obligations = obligations;
        this.students=students;
        this.type = type;
    }

    @Override
    protected void start() {
        HashMap<String, GetGradeSheetAction> getGSActions = new HashMap<>();
        GetComputerAction getComp = new GetComputerAction(this.type);
        List<GetComputerAction> actions = new ArrayList<>();
        actions.add(getComp);
        then(actions, ()-> { //waiting until a computer is available
            Computer comp = actions.get(0).getResult().get();
           for (String student : this.students){
               getGSActions.put(student, new GetGradeSheetAction());
           }
           Collection<GetGradeSheetAction> gradeSheets = getGSActions.values();
           then(gradeSheets, () -> { //getting all gradeSheets from all students
               for (String student : getGSActions.keySet()) {

                   try {
                       SetSignatureAction setSigAction = new SetSignatureAction(
                               comp.checkAndSign(obligations, getGSActions.get(student).getResult().get()));
                       this.sendMessage(setSigAction, student, new StudentPrivateState());
                   }
                   catch (IllegalStateException e) {
                       System.out.println("Student is: " + student);
                   }
               }
               ReleaseComputerAction releaseComp = new ReleaseComputerAction(comp);
               this.sendMessage(releaseComp, "Warehouse", new Warehouse());
               this.complete(true);
           });
           for (String student : getGSActions.keySet() )
           {
               sendMessage(getGSActions.get(student), student, new StudentPrivateState());
           }
        });
        this.sendMessage(getComp, "Warehouse", new Warehouse());

    }
}

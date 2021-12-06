package bgu.atd.a1.sim.actions;

import bgu.atd.a1.Action;
import bgu.atd.a1.sim.privateStates.CoursePrivateState;
import bgu.atd.a1.sim.privateStates.StudentPrivateState;

import java.util.ArrayList;
import java.util.List;

/**
 * If the student is enrolled in the course, unregister him (update the list of students of course,
 * remove the course from the grades sheet of the student and increases the number of available spaces).
 * Should be initially submitted to the course's actor.
 */
public class UnregisterAction extends Action<Boolean> {
    private final String studentName;

    public UnregisterAction(String studentName) {
        this.setActionName("Unregister");
        this.studentName = studentName;
    }

    @Override
    protected void start() {
        CoursePrivateState coursePS = ((CoursePrivateState)this.actorState);
        List<String> courseStudentList = coursePS.getRegStudents();

        if(courseStudentList.contains(this.studentName)){
            Action<Boolean> rmvGradeAction = new RemoveFromGradeSheetAction(this.actorId);
            List<Action<Boolean>> courseRemovalDependencies = new ArrayList<>();
            courseRemovalDependencies.add(rmvGradeAction);

            this.then(courseRemovalDependencies,()->{
                // I chose that student self grade sheet manipulation is a dependency for completion of unregistering procedure
                Boolean removedFromGradeSheet = courseRemovalDependencies.get(0).getResult().get();
                if(removedFromGradeSheet){
                    courseStudentList.remove(this.studentName);
                    if(coursePS.getAvailableSpots()!=-1){ // if course is closed do not update available spots
                        coursePS.setAvailableSpots(coursePS.getAvailableSpots()+1);
                    }
                    coursePS.setRegistered(coursePS.getRegistered()-1);
                    this.complete(true);
                }else{this.complete(false);}
            });

            sendMessage(rmvGradeAction,this.studentName,new StudentPrivateState());

        }else{ this.complete(false); }
    }
}

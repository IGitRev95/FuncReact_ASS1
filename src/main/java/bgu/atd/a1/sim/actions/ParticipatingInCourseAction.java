package bgu.atd.a1.sim.actions;

import bgu.atd.a1.Action;
import bgu.atd.a1.sim.privateStates.CoursePrivateState;
import bgu.atd.a1.sim.privateStates.StudentPrivateState;

import java.util.ArrayList;
import java.util.List;

/**
 * Registers a student to the course, if succeeds, adds the course to the grades-sheet of the student
 * and give him a grade if supplied.
 * Should be initially submitted to the course's actor.
 */
public class ParticipatingInCourseAction extends Action<Boolean> {
    private final String studentName;
    private final Integer studentGrade;

    public ParticipatingInCourseAction(String studentName) {
        this.setActionName("Participate In Course");
        this.studentName = studentName;
        this.studentGrade = null;
    }

    public ParticipatingInCourseAction(String studentName, Integer studentGrade) {
        this.setActionName("Participate In Course");
        this.studentName = studentName;
        this.studentGrade = studentGrade;
    }

    @Override
    protected void start() {
        CoursePrivateState coursePS = (CoursePrivateState)this.actorState;
        if (coursePS.getAvailableSpots() <= 0 || coursePS.getRegStudents().contains(this.studentName)){
            complete(false);
        }
        else {
            AddToGradeSheetAction addToGSAction = new AddToGradeSheetAction(this.actorId, this.studentGrade, coursePS.getPrequisites().toArray(new String[0]));
            List<Action<Boolean>> actions = new ArrayList<>();
            actions.add(addToGSAction);
            then(actions, () -> {
                if(actions.get(0).getResult().get()){
                    coursePS.setAvailableSpots(coursePS.getAvailableSpots() -1);
                    coursePS.setRegistered(coursePS.getRegistered()+1);
                    coursePS.getRegStudents().add(this.studentName);
                    this.complete(true);
                }
                else {this.complete(false);}
            });
            this.sendMessage(addToGSAction, this.studentName, new StudentPrivateState());
        }
    }
}

package bgu.atd.a1.sim.actions;

import bgu.atd.a1.Action;
import bgu.atd.a1.sim.privateStates.CoursePrivateState;

import java.util.ArrayList;
import java.util.List;

/**
 * The student supply a list of courses he is interested in them, and wish to register
 * for ONLY one course of them. The courses are ordered by preference. That is, if he succeed to register for
 * the course with the highest preference it will not try to register for the rest, otherwise it will try to register
 * for the second one, and so on. At the end, he will register for at most one course.
 */
public class RegisterWithPreferencesAction extends Action<Boolean> {
    private final List<String> coursePreferenceList;
    private final List<Integer> suppliedGrades;

    public RegisterWithPreferencesAction(List<String> coursePreferenceList) {
        this.coursePreferenceList = coursePreferenceList;
        this.suppliedGrades = null;
    }

    public RegisterWithPreferencesAction(List<String> coursePreferenceList, List<Integer> suppliedGrades) {
        this.coursePreferenceList = coursePreferenceList;
        this.suppliedGrades = suppliedGrades;
        if(coursePreferenceList.size()!=suppliedGrades.size())
            throw new IllegalArgumentException("course list and grade list have non equal lengths");
    }

    @Override
    protected void start() {
        if (this.suppliedGrades == null) {
            registerWithoutGrades();
        } else {
            registerWithGrades();
        }
    }

    private void registerWithoutGrades(){
        if(this.coursePreferenceList.isEmpty()){
            this.complete(false);
        }else {
            String preferredCourse = this.coursePreferenceList.remove(0);
            ParticipatingInCourseAction courseRegistrationAttempt = new ParticipatingInCourseAction(this.actorId);
            List<Action<Boolean>> registrationAttemptDependency = this.singleRegistrationDependency(courseRegistrationAttempt);
            this.then(registrationAttemptDependency,()->{
                boolean registrationSuccess = registrationAttemptDependency.get(0).getResult().get();
                if(registrationSuccess){
                    this.complete(true);
                }else{
                    registerWithoutGrades();
                }
            });
            sendMessage(courseRegistrationAttempt, preferredCourse, new CoursePrivateState());
        }
    }

    private void registerWithGrades(){
        if(this.coursePreferenceList.isEmpty()){
            this.complete(false);
        }else {
            String preferredCourse = this.coursePreferenceList.remove(0);
            Integer preferredCourseGrade = this.suppliedGrades.remove(0);
            ParticipatingInCourseAction courseRegistrationAttempt = new ParticipatingInCourseAction(this.actorId,preferredCourseGrade);
            List<Action<Boolean>> registrationAttemptDependency = this.singleRegistrationDependency(courseRegistrationAttempt);
            this.then(registrationAttemptDependency,()->{
                boolean registrationSuccess = registrationAttemptDependency.get(0).getResult().get();
                if(registrationSuccess){
                    this.complete(true);
                }else{
                    registerWithGrades();
                }
            });
            sendMessage(courseRegistrationAttempt, preferredCourse, new CoursePrivateState());
        }
    }

    private List<Action<Boolean>> singleRegistrationDependency(ParticipatingInCourseAction courseRegistrationAttempt){
        List<Action<Boolean>> registrationAttemptDependency = new ArrayList<>();
        registrationAttemptDependency.add(courseRegistrationAttempt);
        return registrationAttemptDependency;
    }
}

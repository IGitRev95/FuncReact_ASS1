/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bgu.atd.a1.sim;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import bgu.atd.a1.Action;
import bgu.atd.a1.ActorThreadPool;
import bgu.atd.a1.PrivateState;
import bgu.atd.a1.sim.actions.*;
import bgu.atd.a1.sim.privateStates.CoursePrivateState;
import bgu.atd.a1.sim.privateStates.DepartmentPrivateState;
import com.google.gson.*;
import com.google.gson.annotations.SerializedName;


/**
 * A class describing the simulator for part 2 of the assignment
 */
public class Simulator {

	public static ActorThreadPool actorThreadPool;
	private static String inputJsonPath;
	private static Gson gson;
	private static Object simulationAlert;
	private static HashMap<String,PrivateState> actorTPRecords = null;

	/**
	* Begin the simulation Should not be called before attachActorThreadPool()
	*/
    public static void start() {
		try {
			actorThreadPool.start();
			ParsedJson parsedJson = gson.fromJson(new FileReader(inputJsonPath), ParsedJson.class);
			HashMap<Computer, Boolean> warehouseComputers = new HashMap<>();
			for (ComputerRawJson comp : parsedJson.computers) {
				warehouseComputers.put(new Computer(comp.type, comp.sigFail, comp.sigSuccess), false);
			}
			actorThreadPool.submit(new Action<Boolean>() {
				@Override
				protected void start() {
					this.complete(true);
				}
			}, "Warehouse", new Warehouse(warehouseComputers));
			System.out.println("start p1");
			runPhase1(parsedJson);
			synchronized (simulationAlert) {
				simulationAlert.wait();
			}
		}catch (Exception e){
			System.out.println("Exception in Start(), exception: "+e.toString());
		}
		actorTPRecords=end();
    }

	private static void runPhase1(ParsedJson parsedJson)
	{
		ArrayList<Action<?>> actionArrayList = new ArrayList<>();
		for(RawAction rawAction: parsedJson.phase1){
			actionArrayList.add(buildNSendAction(rawAction));
		}

		actorThreadPool.submit(new Action<Boolean>() {
			@Override
			protected void start() {
				then(actionArrayList,()->{
					System.out.println("start p2");
					runPhase2(parsedJson);
					this.complete(true);
				});
			}
		}, "Warehouse", new Warehouse()); // arbitrary target actor
	}

	private static void runPhase2(ParsedJson parsedJson)
	{
		ArrayList<Action<?>> actionArrayList = new ArrayList<>();
		for(RawAction rawAction: parsedJson.phase2){
			actionArrayList.add(buildNSendAction(rawAction));
		}
		System.out.println("Finished sending P2 actions");
		actorThreadPool.submit(new Action<Boolean>() {
			@Override
			protected void start() {
				then(actionArrayList,()->{
					System.out.println("start p3");
					runPhase3(parsedJson);
					this.complete(true);
				});
			}
		}, "Warehouse", new Warehouse()); // arbitrary target actor
	}
	private static void runPhase3(ParsedJson parsedJson)
	{
		ArrayList<Action<?>> actionArrayList = new ArrayList<>();
		for(RawAction rawAction: parsedJson.phase3){
			actionArrayList.add(buildNSendAction(rawAction));
		}
		actorThreadPool.submit(new Action<Boolean>() {
			@Override
			protected void start() {
				then(actionArrayList,()->{
					System.out.println("beforeEnd");
					this.complete(true);
					synchronized (simulationAlert){
						simulationAlert.notify();
					}
				});
			}
		}, "Warehouse", new Warehouse()); // arbitrary target actor
	}

	private static Action<?> buildNSendAction(RawAction rawAction){
		Action<?> action = null;
		switch (rawAction.actionName){
			case "Open Course":
				action = new OpenNewCourseAction(rawAction.course, rawAction.space, Arrays.asList(rawAction.prerequisites));
				actorThreadPool.submit(action,rawAction.department,new DepartmentPrivateState());
				break;
			case "Add Student":
				action =  new AddStudentAction(rawAction.student);
				actorThreadPool.submit(action,rawAction.department,new DepartmentPrivateState());
				break;
			case "Participate In Course":
				action =  new ParticipatingInCourseAction(rawAction.student, rawAction.grade[0]);
				actorThreadPool.submit(action,rawAction.course,new CoursePrivateState());
				break;
			case "Unregister":
				action =  new UnregisterAction(rawAction.student);
				actorThreadPool.submit(action,rawAction.course,new CoursePrivateState());
				break;
			case "Close Course":
				action =  new CloseCourseAction();
				actorThreadPool.submit(action,rawAction.course,new CoursePrivateState());
				break;
			case "Add Spaces":
				action =  new OpenNewPlacesInACourseAction(rawAction.space);
				actorThreadPool.submit(action,rawAction.course,new CoursePrivateState());
				break;
			case "Administrative Check":
				action =  new CheckAdministrativeObligationsAction(Arrays.asList(rawAction.conditions),Arrays.asList(rawAction.students), rawAction.type);
				actorThreadPool.submit(action,rawAction.department,new DepartmentPrivateState());
				break;
			case "Register With Preferences":
				action =  new RegisterWithPreferencesAction(Arrays.asList(rawAction.conditions),Arrays.asList(rawAction.grade));
				actorThreadPool.submit(action,rawAction.department,new DepartmentPrivateState()); //TODO check actor
				break;
		}
		return action;
	}
	
	/**
	* attach an ActorThreadPool to the Simulator, this ActorThreadPool will be used to run the simulation
	* 
	* @param myActorThreadPool - the ActorThreadPool which will be used by the simulator
	*/
	public static void attachActorThreadPool(ActorThreadPool myActorThreadPool){
		actorThreadPool=myActorThreadPool;
	}
	
	/**
	* shut down the simulation
	* returns list of private states
	*/
	public static HashMap<String,PrivateState> end(){
		try {
			actorThreadPool.shutdown();
		}catch (InterruptedException ignored){}
		return new HashMap<>(actorThreadPool.getActors()); // new Hashmap due to incompatible types
	}
	
	
	public static void main(String [] args) throws FileNotFoundException {
		init(args[0]);

		//Json Parsing
		ThreadAmountExtraction inputJson = gson.fromJson(new FileReader( inputJsonPath ), ThreadAmountExtraction.class);
		attachActorThreadPool(new ActorThreadPool(inputJson.threads));

		start();



	}

	private static OutputSkeleton generateOutputSkeletonFromRecords(){
		OutputSkeleton outputSkeleton = new OutputSkeleton();
		List<DepartmentOutputSkeleton> departmentsPSList = new ArrayList<>();
		List<CourseOutputSkeleton> coursesPSList = new ArrayList<>();
		List<StudentOutputSkeleton> studentsPSList = new ArrayList<>();
		for (String actor: actorTPRecords.keySet()){

		}


		return outputSkeleton;
	}

	private static void init(String inputPath){
		inputJsonPath = inputPath;
		gson = new GsonBuilder().setPrettyPrinting().create();
		simulationAlert = new Object();
	}

	private static class ThreadAmountExtraction {
		private int threads;
	}
	private static class ParsedJson {
		@SerializedName("Computers")
		private ComputerRawJson[] computers;
		@SerializedName("Phase 1")
		private RawAction[] phase1;
		@SerializedName("Phase 2")
		private RawAction[] phase2;
		@SerializedName("Phase 3")
		private RawAction[] phase3;
	}
	private static class ComputerRawJson {
		@SerializedName("Type")
		private String type;
		@SerializedName("Sig Success")
		private long sigSuccess;
		@SerializedName("Sig Fail")
		private long sigFail;
	}
	private static class RawAction {
		@SerializedName("Action")
		private String actionName;
		@SerializedName("Department")
		private String department;
		@SerializedName("Course")
		private String course;
		@SerializedName("Space")
		private Integer space;
		@SerializedName("Prerequisites")
		private String[] prerequisites;
		@SerializedName("Student")
		private String student;
		@SerializedName("Grade")
		private Integer[] grade;
		@SerializedName("Conditions")
		private String[] conditions;
		@SerializedName("Students")
		private String[] students;
		@SerializedName("Courses")
		private String[] courses;
		@SerializedName("Computer")
		private String type;
	}

	private static class OutputSkeleton{
		@SerializedName("Departments")
		private DepartmentOutputSkeleton[] departmentsPrivateStates;
		@SerializedName("Courses")
		private CourseOutputSkeleton[] coursesPrivateStates;
		@SerializedName("Students")
		private StudentOutputSkeleton[] studentsPrivateStates;
	}
	private static class DepartmentOutputSkeleton{
		@SerializedName("Department")
		private String departmentName;
		@SerializedName("actions")
		private String[] actionLog;
		@SerializedName("courseList")
		private String[] courseList;
		@SerializedName("studentList")
		private String[] studentList;
	}
	private static class CourseOutputSkeleton{
		@SerializedName("Course")
		private String courseName;
		@SerializedName("actions")
		private String[] actionLog;
		@SerializedName("availableSpots")
		private Integer availableSpots;
		@SerializedName("registered")
		private Integer registered;
		@SerializedName("regStudents")
		private String[] regStudents;
		@SerializedName("prequisites")
		private String[] prerequisites;
	}
	private static class StudentOutputSkeleton{
		@SerializedName("Student")
		private String studentName;
		@SerializedName("actions")
		private String[] actionLog;
		@SerializedName("grades")
		private String[] grades;
		@SerializedName("signature")
		private long signature;
	}

	private static List<String> gradeSheetToList(HashMap<String,Integer> gradeSheet){
		List<String> gradeSheetAsList = new ArrayList<>();
		for (String courseName: gradeSheet.keySet()){
			gradeSheetAsList.add("("+courseName+","+gradeSheet.get(courseName)+")");
		}
		return gradeSheetAsList;
	}
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bgu.atd.a1.sim;
import java.io.*;
import java.util.*;

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

	public static void main(String [] args) {
		init(args[0]);

		//Json Parsing
		try {
			ThreadAmountExtraction inputJson = gson.fromJson(new FileReader(inputJsonPath), ThreadAmountExtraction.class);
			attachActorThreadPool(new ActorThreadPool(inputJson.threads));
			start();
		}catch (Exception fileNotFoundException){
			System.out.println(fileNotFoundException.getMessage());
		}

		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("result.ser" ))){
			oos.writeObject(actorTPRecords);
		}catch(Exception e){
			System.out.println(e.getMessage());
		}

	}
//
//	private static OutputSkeleton generateOutputSkeletonFromRecords(){
//		OutputSkeleton outputSkeleton = new OutputSkeleton();
//		List<DepartmentOutputSkeleton> departmentsPSList = new ArrayList<>();
//		List<CourseOutputSkeleton> coursesPSList = new ArrayList<>();
//		List<StudentOutputSkeleton> studentsPSList = new ArrayList<>();
//		for (String actor: actorTPRecords.keySet()){
//
//		}
//
//
//		return outputSkeleton;
//	}

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

//	private static class OutputSkeleton implements Serializable {
//		@SerializedName("Departments")
//		private DepartmentOutputSkeleton[] Departments;
//		@SerializedName("Courses")
//		private CourseOutputSkeleton[] Courses;
//		@SerializedName("Students")
//		private StudentOutputSkeleton[] Students;
//
//		public DepartmentOutputSkeleton[] getDepartments() {
//			return Departments;
//		}
//
//		public void setDepartments(DepartmentOutputSkeleton[] departments) {
//			Departments = departments;
//		}
//
//		public CourseOutputSkeleton[] getCourses() {
//			return Courses;
//		}
//
//		public void setCourses(CourseOutputSkeleton[] courses) {
//			Courses = courses;
//		}
//
//		public StudentOutputSkeleton[] getStudents() {
//			return Students;
//		}
//
//		public void setStudents(StudentOutputSkeleton[] students) {
//			Students = students;
//		}
//
//		public OutputSkeleton(DepartmentOutputSkeleton[] departments, CourseOutputSkeleton[] courses, StudentOutputSkeleton[] students) {
//			Departments = departments;
//			Courses = courses;
//			Students = students;
//		}
//	}
//	private static class DepartmentOutputSkeleton implements Serializable {
//		@SerializedName("Department")
//		private String Department;
//		@SerializedName("actions")
//		private String[] actions;
//		@SerializedName("courseList")
//		private String[] courseList;
//		@SerializedName("studentList")
//		private String[] studentList;
//
//		public String getDepartment() {
//			return Department;
//		}
//
//		public void setDepartment(String department) {
//			Department = department;
//		}
//
//		public String[] getActions() {
//			return actions;
//		}
//
//		public void setActions(String[] actions) {
//			this.actions = actions;
//		}
//
//		public String[] getCourseList() {
//			return courseList;
//		}
//
//		public void setCourseList(String[] courseList) {
//			this.courseList = courseList;
//		}
//
//		public String[] getStudentList() {
//			return studentList;
//		}
//
//		public void setStudentList(String[] studentList) {
//			this.studentList = studentList;
//		}
//
//		public DepartmentOutputSkeleton(String department, String[] actions, String[] courseList, String[] studentList) {
//			Department = department;
//			this.actions = actions;
//			this.courseList = courseList;
//			this.studentList = studentList;
//		}
//	}
//	private static class CourseOutputSkeleton implements Serializable{
//		@SerializedName("Course")
//		private String Course;
//		@SerializedName("actions")
//		private String[] actions;
//		@SerializedName("availableSpots")
//		private Integer availableSpots;
//		@SerializedName("registered")
//		private Integer registered;
//		@SerializedName("regStudents")
//		private String[] regStudents;
//		@SerializedName("prequisites")
//		private String[] prequisites;
//
//		public CourseOutputSkeleton(String course, String[] actions, Integer availableSpots, Integer registered, String[] regStudents, String[] prequisites) {
//			Course = course;
//			this.actions = actions;
//			this.availableSpots = availableSpots;
//			this.registered = registered;
//			this.regStudents = regStudents;
//			this.prequisites = prequisites;
//		}
//
//		public String getCourse() {
//			return Course;
//		}
//
//		public void setCourse(String course) {
//			Course = course;
//		}
//
//		public String[] getActions() {
//			return actions;
//		}
//
//		public void setActions(String[] actions) {
//			this.actions = actions;
//		}
//
//		public Integer getAvailableSpots() {
//			return availableSpots;
//		}
//
//		public void setAvailableSpots(Integer availableSpots) {
//			this.availableSpots = availableSpots;
//		}
//
//		public Integer getRegistered() {
//			return registered;
//		}
//
//		public void setRegistered(Integer registered) {
//			this.registered = registered;
//		}
//
//		public String[] getRegStudents() {
//			return regStudents;
//		}
//
//		public void setRegStudents(String[] regStudents) {
//			this.regStudents = regStudents;
//		}
//
//		public String[] getPrequisites() {
//			return prequisites;
//		}
//
//		public void setPrequisites(String[] prequisites) {
//			this.prequisites = prequisites;
//		}
//	}
//	private static class StudentOutputSkeleton implements Serializable{
//		@SerializedName("Student")
//		private String Student;
//		@SerializedName("actions")
//		private String[] actions;
//		@SerializedName("grades")
//		private String[] grades;
//		@SerializedName("signature")
//		private long signature;
//
//		public StudentOutputSkeleton(String student, String[] actions, String[] grades, long signature) {
//			Student = student;
//			this.actions = actions;
//			this.grades = grades;
//			this.signature = signature;
//		}
//
//		public String getStudent() {
//			return Student;
//		}
//
//		public void setStudent(String student) {
//			Student = student;
//		}
//
//		public String[] getActions() {
//			return actions;
//		}
//
//		public void setActions(String[] actions) {
//			this.actions = actions;
//		}
//
//		public String[] getGrades() {
//			return grades;
//		}
//
//		public void setGrades(String[] grades) {
//			this.grades = grades;
//		}
//
//		public long getSignature() {
//			return signature;
//		}
//
//		public void setSignature(long signature) {
//			this.signature = signature;
//		}
//	}
//
//	private static List<String> gradeSheetToList(HashMap<String,Integer> gradeSheet){
//		List<String> gradeSheetAsList = new ArrayList<>();
//		for (String courseName: gradeSheet.keySet()){
//			gradeSheetAsList.add("("+courseName+","+gradeSheet.get(courseName)+")");
//		}
//		return gradeSheetAsList;
//	}
}

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
import bgu.atd.a1.sim.privateStates.StudentPrivateState;
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
			// Warehouse computer init
			for (ComputerRawJson comp : parsedJson.computers) {
				warehouseComputers.put(new Computer(comp.type, comp.sigFail, comp.sigSuccess), false);
			}
			actorThreadPool.submit(new Action<Boolean>() {
				@Override
				protected void start() {
					this.complete(true);
				}
			}, "Warehouse", new Warehouse(warehouseComputers));
			runPhase1(parsedJson);
			// Main thread waits for simulation completion
			synchronized (simulationAlert) {
				simulationAlert.wait();
			}
		}catch (Exception e){
			System.out.println("Exception in Start(), exception: "+e.getMessage());
		}
		//extracting records
		actorTPRecords=end();
	}

	private static void runPhase1(ParsedJson parsedJson)
	{
		ArrayList<Action<?>> actionArrayList = new ArrayList<>();
		for(RawAction rawAction: parsedJson.phase1){
			actionArrayList.add(buildNSendAction(rawAction));
		}
		// submitting phase 1 actions and set a callback of running phase 2
		actorThreadPool.submit(new Action<Boolean>() {
			@Override
			protected void start() {
				then(actionArrayList,()->{
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
		// submitting phase 2 actions and set a callback of running phase 3
		actorThreadPool.submit(new Action<Boolean>() {
			@Override
			protected void start() {
				then(actionArrayList,()->{
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
		// submitting phase 3 actions and set a callback of final steps of simulation
		actorThreadPool.submit(new Action<Boolean>() {
			@Override
			protected void start() {
				then(actionArrayList,()->{
					this.complete(true);
					// waking main thread that simulation completed
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
				// grade not necessarily supplied
				Integer grade=null;
				if(rawAction.grade!=null){
					grade=rawAction.grade[0];
				}
				action =  new ParticipatingInCourseAction(rawAction.student, grade);
				actorThreadPool.submit(action,rawAction.course,new CoursePrivateState());
				break;
			case "Unregister":
				action =  new UnregisterAction(rawAction.student);
				actorThreadPool.submit(action,rawAction.course,new CoursePrivateState());
				break;
			case "Close Course":
				action =  new CloseCourseAction(rawAction.course);
				actorThreadPool.submit(action,rawAction.department,new CoursePrivateState());
				break;
			case "Add Spaces":
				action =  new OpenNewPlacesInACourseAction(rawAction.newSpace); // @newSpace name was in accurate - name changing
				actorThreadPool.submit(action,rawAction.course,new CoursePrivateState());
				break;
			case "Administrative Check":
				action =  new CheckAdministrativeObligationsAction(Arrays.asList(rawAction.conditions),Arrays.asList(rawAction.students), rawAction.type);
				actorThreadPool.submit(action,rawAction.department,new DepartmentPrivateState());
				break;
			case "Register With Preferences":
				action =  new RegisterWithPreferencesAction(new ArrayList<>(Arrays.asList(rawAction.preferences)),new ArrayList<>(Arrays.asList(rawAction.grade))); // @ Preferences name was in accurate - name changing
				actorThreadPool.submit(action,rawAction.student,new StudentPrivateState());
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
		// output thread pool records to ser file
		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("result.ser" ))){
			actorTPRecords.remove("Warehouse");
			oos.writeObject(actorTPRecords);
		}catch(Exception e){
			System.out.println(e.getMessage());
		}

	}

	private static void init(String inputPath){
		inputJsonPath = inputPath;
		gson = new GsonBuilder().setPrettyPrinting().create();
		simulationAlert = new Object();
	}


	// Skeleton classes for json gson based parsing
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
		@SerializedName("Number")
		private Integer newSpace;
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
		@SerializedName("Preferences")
		private String[] preferences;
		@SerializedName("Computer")
		private String type;
	}
}

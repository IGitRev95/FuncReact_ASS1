/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bgu.atd.a1.sim;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;

import bgu.atd.a1.ActorThreadPool;
import bgu.atd.a1.PrivateState;
import com.google.gson.*;
import com.google.gson.annotations.SerializedName;


/**
 * A class describing the simulator for part 2 of the assignment
 */
public class Simulator {

	public static ActorThreadPool actorThreadPool;
	private static String inputJsonPath;
	private static Gson gson;

	/**
	* Begin the simulation Should not be called before attachActorThreadPool()
	*/
    public static void start() throws FileNotFoundException {
		ParsedJson parsedJson = gson.fromJson(new FileReader( inputJsonPath ), ParsedJson.class);
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

//		return 0;
	}

	private static class ThreadAmountExtraction {
		private int threads = 0;
	}

	private static void init(String inputPath) throws FileNotFoundException {
		inputJsonPath = inputPath;
		gson = new GsonBuilder().setPrettyPrinting().create();
	}

	private static class ParsedJson {
		@SerializedName("Computers")
		private ComputerRawJson[] computers;
		@SerializedName("Phase 1")
		private Action[] phase1;
		@SerializedName("Phase 2")
		private Action[] phase2;
		@SerializedName("Phase 3")
		private Action[] phase3;
	}
	private static class ComputerRawJson {
		@SerializedName("Type")
		private String type;
		@SerializedName("Sig Success")
		private long sigSuccess;
		@SerializedName("Sig Fail")
		private long sigFail;
	}
	private class Action{
		@SerializedName("Action")
		private String actionName;
		@SerializedName("Department")
		private String department;
		@SerializedName("Course")
		private String course;
		@SerializedName("Space")
		private int space;
		@SerializedName("Prerequisites")
		private String[] prerequisites;
		@SerializedName("Student")
		private String student;
		@SerializedName("Grade")
		private int[] Grade;
		@SerializedName("Conditions")
		private String[] conditions;

	}

}

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
    public static void start(){
		//TODO: replace method body with real implementation
		throw new UnsupportedOperationException("Not Implemented Yet.");
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
	
	
	public static int main(String [] args) throws FileNotFoundException {
		init(args[0]);

		//Json Parsing
		ThreadAmountExtraction inputJson = gson.fromJson(new FileReader( inputJsonPath ), ThreadAmountExtraction.class);
		attachActorThreadPool(new ActorThreadPool(inputJson.threads));

		return 0;
	}

	private static class ThreadAmountExtraction {
		private int threads = 0;
	}

	private static void init(String inputPath){
		inputJsonPath=inputPath;
		gson = new GsonBuilder().setPrettyPrinting().create();
	}

//	private static class ThreadAmountExtraction {
//		private int threads = 0;
//	}
}

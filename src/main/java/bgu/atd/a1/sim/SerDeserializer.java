package bgu.atd.a1.sim;

import bgu.atd.a1.PrivateState;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.util.HashMap;

public class SerDeserializer {
    public static void main(String... args){
        if (args.length != 1){
            throw new IllegalArgumentException("No file path supplied");
        }

        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(args[0]))){
            HashMap<String, PrivateState> map = (HashMap<String, PrivateState>) ois.readObject();
//            System.out.println(map.toString());
            objectToOutputJsonFile(map, "mapAsJson.json");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void objectToOutputJsonFile(Object toOutputFile, String outPutPath)  {
        try(FileWriter outputJson = new FileWriter(new File( outPutPath ))){
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson( toOutputFile, outputJson);
        }catch (Exception ignore){}
    }
}

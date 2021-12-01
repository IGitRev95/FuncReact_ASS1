package bgu.atd.a1.sim;

import bgu.atd.a1.PrivateState;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;

public class SerDeserializer {
    public static void main(String... args){
        System.out.println(args.length);
        if (args.length != 1){
            throw new IllegalArgumentException("No file path supplied");
        }

        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(args[0]))){
            HashMap<String, PrivateState> map = (HashMap<String, PrivateState>) ois.readObject();
//            Object map = ois.readObject();
            System.out.println(map.toString());
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

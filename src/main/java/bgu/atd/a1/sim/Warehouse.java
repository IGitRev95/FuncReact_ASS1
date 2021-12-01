package bgu.atd.a1.sim;

import bgu.atd.a1.PrivateState;

import java.io.Serializable;
import java.util.HashMap;

/**
 * represents a warehouse that holds a finite amount of computers
 * and their suspended mutexes.
 * releasing and acquiring should be blocking free.
 */
public class Warehouse extends PrivateState implements Serializable {
	private final HashMap<Computer,Boolean> computersUsage;

    public Warehouse() {
        this.computersUsage = new HashMap<>();
    }

    public Warehouse(HashMap<Computer, Boolean> computersUsage) {
        this.computersUsage = computersUsage;
    }

    public HashMap<Computer, Boolean> getComputersUsage() {
        return computersUsage;
    }
}

package bgu.atd.a1.sim.actions;

import bgu.atd.a1.Action;
import bgu.atd.a1.sim.Computer;
import bgu.atd.a1.sim.Warehouse;

import java.util.HashMap;

/**
 * release an acquired computer after use is finished
 */
public class ReleaseComputerAction extends Action<Boolean> {
    private final Computer comp;

    public ReleaseComputerAction(Computer comp) {
        this.setActionName("Release Computer");
        this.comp = comp;
    }

    @Override
    protected void start() {
        HashMap<Computer,Boolean> warehouseComputerMap = ((Warehouse)this.actorState).getComputersUsage();
        if(!warehouseComputerMap.get(this.comp)){
            this.complete(false);
        }else {
            warehouseComputerMap.put(this.comp,false);
            this.complete(true);
        }
    }
}

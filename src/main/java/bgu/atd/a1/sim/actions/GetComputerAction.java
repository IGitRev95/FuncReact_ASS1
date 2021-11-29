package bgu.atd.a1.sim.actions;

import bgu.atd.a1.Action;
import bgu.atd.a1.sim.Computer;
import bgu.atd.a1.sim.Warehouse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GetComputerAction extends Action<Computer> {
    @Override
    protected void start() {
        //TODO: TEST
        HashMap<Computer,Boolean> warehouseComputerMap = ((Warehouse)this.actorState).getComputersUsage();
        for(Computer comp: warehouseComputerMap.keySet()){
            if(!warehouseComputerMap.get(comp)){
                warehouseComputerMap.put(comp,true);
                this.complete(comp);
                return;
            }
        }
        GetComputerAction retry = new GetComputerAction();
        List<Action<Computer>> dependencies = new ArrayList<>();
        dependencies.add(retry);
        then(dependencies,()->{
            this.complete(dependencies.get(0).getResult().get());
        });
        this.sendMessage(retry,this.actorId,new Warehouse());
    }
}

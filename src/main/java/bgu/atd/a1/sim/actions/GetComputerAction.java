package bgu.atd.a1.sim.actions;

import bgu.atd.a1.Action;
import bgu.atd.a1.sim.Computer;
import bgu.atd.a1.sim.Warehouse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class GetComputerAction extends Action<Computer> {
    private String type;

    public GetComputerAction(String type) {
        this.setActionName("GetComputerAction");
        this.type = type;
    }

    @Override
    protected void start() {
        //TODO: TEST
        HashMap<Computer,Boolean> warehouseComputerMap = ((Warehouse)this.actorState).getComputersUsage();
        for(Computer comp: warehouseComputerMap.keySet()){
            if(!warehouseComputerMap.get(comp)){
                if(comp.getComputerType().equals(this.type)) {
                    warehouseComputerMap.put(comp, true);
                    this.complete(comp);
                    return;
                }
            }
        }
        GetComputerAction retry = new GetComputerAction(this.type);
        List<Action<Computer>> dependencies = new ArrayList<>();
        dependencies.add(retry);
        then(dependencies,()->{
            this.complete(dependencies.get(0).getResult().get());
        });
        this.sendMessage(retry,this.actorId,new Warehouse());
    }
}

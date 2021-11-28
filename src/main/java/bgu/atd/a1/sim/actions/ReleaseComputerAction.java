package bgu.atd.a1.sim.actions;

import bgu.atd.a1.Action;
import bgu.atd.a1.sim.Computer;

public class ReleaseComputerAction extends Action<Boolean> {
    private final Computer comp;

    public ReleaseComputerAction(Computer comp) {
        this.comp = comp;
    }

    @Override
    protected void start() {
        //TODO: implement
    }
}

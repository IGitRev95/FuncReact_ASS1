package bgu.atd.a1.sim.actions;

import bgu.atd.a1.Action;
import bgu.atd.a1.sim.privateStates.StudentPrivateState;

public class SetSignatureAction extends Action<Boolean> {

    private final long signature;

    public SetSignatureAction(long signature) {
        this.setActionName("SetSignatureAction");
        this.signature = signature;
    }

    @Override
    protected void start() {
        StudentPrivateState studentPS = (StudentPrivateState) this.actorState;
        complete(true);
    }
}

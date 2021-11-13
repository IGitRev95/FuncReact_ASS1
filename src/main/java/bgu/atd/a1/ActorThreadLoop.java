package bgu.atd.a1;

/**
 * The general thread loop behavior:
 * look for a free actor queue
 * try execution
 * execute one action
 * look for suspended actions for the current actor
 * repeat
 *
 * if no actor are available or action queues are empty sleep
 * TODO: insert interruption handling for shutdown
 */
public class ActorThreadLoop implements Runnable{
    public void run() {
        //TODO: implement general actor thread behavior
    }
}

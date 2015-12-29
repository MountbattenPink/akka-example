package akka.task.main.java;

import akka.actor.*;
import org.apache.log4j.BasicConfigurator;


/**
 * @author olsh
 *
 */
public class RecordFilter {

    public void calculate(final int numberOfWorkers, final int numberOfElements, final int numberOfMessages) {
        ActorSystem system = ActorSystem.create("RecordFilter");
        final ActorRef listener = system.actorOf(new Props(Actors.Listener.class), "listener");
        ActorRef master = system.actorOf(new Props(new UntypedActorFactory() {
            public UntypedActor create() {
                return new Actors.Master(numberOfWorkers, numberOfMessages, numberOfElements, listener,"tz.txt");
            }
        }), "master");
        master.tell(new Messages.Calculate());
    }

    public static void main(String[] args) {
        BasicConfigurator.configure();
        new RecordFilter().calculate(7, 100000, 1000);
    }
}

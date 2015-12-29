package akka.task.main.java;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.routing.RoundRobinRouter;
import akka.util.Duration;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author olsh
 *
 */

public class Actors {
    final static Logger logger = Logger.getLogger(Actors.class);

    public static void putEntry(Int2IntMap map, int key, int newValue){
        if (map.containsKey(key)){
            map.put(key, newValue + map.get(key));
        }
        else {
            map.put(key, newValue);
        }
    }

    public static void putAllEntries(Int2IntMap destMap,Int2IntMap sourceMap){
        for (Int2IntMap.Entry sourceEntry:sourceMap.int2IntEntrySet()) {
            putEntry(destMap, sourceEntry.getIntKey(),sourceEntry.getIntValue());
        }
    }


    public static class Worker extends UntypedActor {

        private Int2IntMap doCalculation(String filePath, int numberOfElements, int startElement){
            Int2IntMap entries = new Int2IntOpenHashMap();
            try {
                LineIterator it = IOUtils.lineIterator(new BufferedReader(new FileReader(filePath)));
                for (int lineNumber = 0; it.hasNext(); lineNumber++) {
                    String line = (String) it.next();
                    if ((lineNumber >= startElement)&&(lineNumber < startElement+numberOfElements)) {
                        Actors.putEntry(entries,Integer.parseInt(line.split(";")[0]),Integer.parseInt(line.split(";")[1]));
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return entries;
        }


        @Override
        public void onReceive(Object o) throws Exception {
             if (o instanceof Messages.Work){
                Messages.Work work=(Messages.Work)o;
                Int2IntMap tempResult=doCalculation(work.getFilePath(), work.getNumberOfElements(),work.getStartElement());
                getSender().tell(new Messages.Result(tempResult), getSelf());
            } else {
                unhandled(o);
            }

        }
    }


    public static class Master extends UntypedActor {
        private final int numberOfMessages;
        private final int numberOfElements;
        private Int2IntMap finalResult;
        private int numberOfResults;
        private final long timeStart = System.currentTimeMillis();
        private final String fileName;
        private final ActorRef listener;
        private final ActorRef workerRouter;


        public Master(final int numberOfWorkers, int numberOfMessages, int numberOfElements, ActorRef listener, String fileName) {
            this.numberOfMessages = numberOfMessages;
            this.numberOfElements = numberOfElements;
            this.listener = listener;
            this.fileName=fileName;
            this.finalResult=new Int2IntOpenHashMap();
            workerRouter = this.getContext().actorOf(new Props(Worker.class).withRouter(new RoundRobinRouter(numberOfWorkers)), "workerRouter");
        }

        @Override
        public void onReceive(Object o) throws Exception {
            if (o instanceof Messages.Calculate) {
                for (int start = 0; start < numberOfMessages; start++) {
                    workerRouter.tell(new Messages.Work(start, numberOfElements, fileName) {
                    }, getSelf());
                }
            }
            else if (o instanceof Messages.Result) {
                Messages.Result result = (Messages.Result) o;
                Actors.putAllEntries(finalResult, result.getValue());
                numberOfResults += 1;
                if (numberOfResults == numberOfMessages) {
                    Duration duration = Duration.create(System.currentTimeMillis() - timeStart, TimeUnit.MILLISECONDS);
                    listener.tell(new Messages.FinalCalculation(finalResult, duration), getSelf());
                    getContext().stop(getSelf());
                }
            } else {
                unhandled(o);
            }
        }
    }


    public static class Listener extends UntypedActor {

        @Override
        public void onReceive(Object o) throws Exception {
            if (o instanceof Messages.FinalCalculation){
                Messages.FinalCalculation finalCalculation=(Messages.FinalCalculation)o;
                logger.info("\nResult: " + finalCalculation.getFinalResult()+"\nTime:" + finalCalculation.getDuration());
                PrintWriter writer = new PrintWriter("result.txt", "UTF-8");
                for (Int2IntMap.Entry entry:finalCalculation.getFinalResult().int2IntEntrySet())
                writer.println(entry.getIntKey()+":"+entry.getIntValue());
                writer.close();
                getContext().system().shutdown();
            }
            else {
                unhandled(o);
            }
        }
    }
}

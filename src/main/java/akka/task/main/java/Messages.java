package akka.task.main.java;

import akka.util.Duration;
import it.unimi.dsi.fastutil.ints.Int2IntMap;

/**
 *
 * @author olsh
 *
 */

public class Messages {

    static class Calculate {
    }

    static class Work {
        private final int startElement;
        private final int numberOfElements;
        private final String filePath;
        public Work(int startElement, int numberOfElements, String filePath) {
            this.startElement = startElement;
            this.numberOfElements = numberOfElements;
            this.filePath=filePath;
        }

        public int getStartElement() {
            return startElement;
        }

        public int getNumberOfElements() {
            return numberOfElements;
        }

        public String getFilePath() { return filePath; }
    }


    static class Result {
        private final Int2IntMap value;

        public Result(Int2IntMap value) {
            this.value = value;
        }

        public Int2IntMap getValue() {
            return value;
        }
    }




    static class FinalCalculation {
        private final Int2IntMap finalResult;
        private final Duration duration;

        public FinalCalculation(Int2IntMap finalResult, Duration duration) {
            this.finalResult=finalResult;
            this.duration = duration;
        }

        public Int2IntMap getFinalResult() {
            return finalResult;
        }

        public Duration getDuration() {
            return duration;
        }
    }



}

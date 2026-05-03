package engine.drawInstructors;

import java.util.List;

import engine.PriceData.TimeSeries;
import engine.components.DrawInstruction;

public class BoundPlotter extends LinePlotter {

    public BoundPlotter(int index) {
        super(index);
    }

    @Override
    public String toString() {
        return "bounding quad";
    }

    @Override
    protected List<DrawInstruction.Point> plotPreds(TimeSeries pred) {
         return new TimeSeries(List.of(pred.get(0), pred.get(pred.getMaxIndex()), pred.get(pred.size()-1), pred.get(pred.getMinIndex()), pred.get(0)))
                .extract((dt, px) -> new DrawInstruction.Point(dt.toEpochSecond(), px));
    }
    //    @Override
//    List<DrawInstruction.Point> makeUnformattedPrediction(TimeSeries<?> pred) {
//        if (pred.size() == 0) {
//            return List.of();
//        }
//        return new TimeSeries<>(List.of(pred.get(0), pred.get(pred.getMaxIndex()), pred.get(pred.size()-1), pred.get(pred.getMinIndex()), pred.get(0)))
//                .extract((dt, px) -> new DrawInstruction.Point(dt.toEpochSecond(), px));
//    }
//
//    @Override
//    public StateLoader<? extends StateMachine<DrawInstructor>> getLoader() {
//        return new StateLoader<>() {
//            @Override
//            public DrawInstructor load(Map<String, String> state) {
//                return new BoundPlotter<>();
//            }
//
//            @Override
//            public String toString(Map<String, String> state) {
//                return "";
//            }
//        };
//    }
}

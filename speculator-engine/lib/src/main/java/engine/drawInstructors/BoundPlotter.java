package engine.drawInstructors;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Stream;

import engine.PriceData.Ticker;
import engine.PriceData.TimeSeries;
import engine.components.DrawInstruction;
import engine.components.DrawInstructor;
import engine.components.InstructedDrawer;

public class BoundPlotter <V extends Number> extends LinePlotter<V>{

    public BoundPlotter() {
        super();
    }

    public BoundPlotter(ZonedDateTime anchor) {
        super(anchor);
    }

    @Override
    public String toString() {
        return "bounding quad";
    }

    @Override
    List<DrawInstruction.Point> makeUnformattedPrediction(TimeSeries<?> pred) {
        if (pred.size() == 0) {
            return List.of();
        }
        return new TimeSeries<>(List.of(pred.get(0), pred.get(pred.getMaxIndex()), pred.get(pred.size()-1), pred.get(pred.getMinIndex()), pred.get(0)))
                .extract((dt, px) -> new DrawInstruction.Point(dt.toEpochSecond(), px));
    }
}

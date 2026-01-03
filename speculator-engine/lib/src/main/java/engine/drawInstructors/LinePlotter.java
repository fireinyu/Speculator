package engine.drawInstructors;

import engine.components.DrawInstruction;
import engine.components.DrawInstructor;
import engine.components.InstructedDrawer;
import engine.PriceData.Ticker;
import engine.PriceData.TimeSeries;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LinePlotter <V extends Number> extends DrawInstructor<V> {
    private static List<DrawInstruction.Color> lineColors = List.of(
            DrawInstruction.Color.RED,
            DrawInstruction.Color.BLUE,
            DrawInstruction.Color.GREEN
    );
    private ZonedDateTime anchor;
    public LinePlotter () {
        this(ZonedDateTime.now());
    }

    public LinePlotter (ZonedDateTime anchor) {
        this.anchor = anchor;
    }

    @Override
    public String toString() {
        return "basic line";
    }

    List<DrawInstruction.Point> makeUnformattedFeatures(TimeSeries<?> features) {
        // shape: ticker/1 -> feature
        // before normalisation
        return features.extract((dt, px) -> new DrawInstruction.Point(dt.toEpochSecond(), px));
    }

    List<DrawInstruction.Point> makeUnformattedPrediction(TimeSeries<?> pred) {
        // shape: ticker/predictor -> prediction
        // before normalisation
        return pred.extract((dt, px) -> new DrawInstruction.Point(dt.toEpochSecond(), px));
    }

    List<DrawInstruction.Point> makeUnformattedTargets(TimeSeries<?> targets) {
        // shape: ticker/1 -> feature
        // before normalisation
        return targets.extract((dt, px) -> new DrawInstruction.Point(dt.toEpochSecond(), px));
    }

    @Override
    protected DrawInstruction singleDraw(TimeSeries<V> input, String label) {
        return new DrawInstruction(
                input.extract((dt, px) -> new DrawInstruction.Point(dt.toEpochSecond(), px)),
                lineColors.get(0),
                DrawInstruction.Style.SOLID,
                ""
        );
    }

    @Override
    protected List<DrawInstruction> drawAll(List<? extends Ticker> tickers, List<? extends TimeSeries<V>> featuresLs, List<? extends TimeSeries<V>> predsLs, List<? extends TimeSeries<V>> targetsLs) {
        ZonedDateTime anchor = TimeSeries.getAnchor(featuresLs);
        ArrayList<DrawInstruction> instructions = new ArrayList<>();
        if (tickers.size() == 1) {
            double anchorPrice = featuresLs.get(0).priceAt(anchor).doubleValue();
            featuresLs.stream()
                    .map(this::makeUnformattedFeatures)
                    .map(line -> line.stream().map(point -> new DrawInstruction.Point(point.getX().doubleValue() - anchor.toEpochSecond(), point.getY().doubleValue()/ anchorPrice)).collect(Collectors.toList()))
                    .forEach(line -> instructions.add(new DrawInstruction(line, lineColors.get(0), DrawInstruction.Style.SOLID, "features")));
            Iterator<DrawInstruction.Color> colorsP = LinePlotter.lineColors.iterator();
            predsLs.stream()
                    .map(this::makeUnformattedPrediction)
                    .map(line -> line.stream().map(point -> new DrawInstruction.Point(point.getX().doubleValue() - anchor.toEpochSecond(), point.getY().doubleValue()/ anchorPrice)).collect(Collectors.toList()))
                    .forEach(line -> instructions.add(new DrawInstruction(line, colorsP.next(), DrawInstruction.Style.DASHED, "prediction")));
            targetsLs.stream()
                    .map(this::makeUnformattedTargets)
                    .map(line -> line.stream().map(point -> new DrawInstruction.Point(point.getX().doubleValue() - anchor.toEpochSecond(), point.getY().doubleValue()/ anchorPrice)).collect(Collectors.toList()))
                    .forEach(line -> instructions.add(new DrawInstruction(line, lineColors.get(0), DrawInstruction.Style.DOTTED, "targets")));
        } else {
            Iterator<DrawInstruction.Color> colors = lineColors.iterator();
            for (int i = 0; i < tickers.size(); i++) {
                DrawInstruction.Color color = lineColors.get(i);
                TimeSeries<V> f = featuresLs.get(i);
                TimeSeries<V> p =  predsLs.get(i);
                double anchorPrice = f.priceAt(anchor).doubleValue();
                instructions.add(new DrawInstruction(
                        this.makeUnformattedFeatures(f).stream().map(point -> new DrawInstruction.Point(point.getX().doubleValue() - anchor.toEpochSecond(), point.getY().doubleValue()/ anchorPrice)).collect(Collectors.toList()),
                        color,
                        DrawInstruction.Style.SOLID,
                        "features"

                ));
                instructions.add(new DrawInstruction(
                        this.makeUnformattedPrediction(p).stream().map(point -> new DrawInstruction.Point(point.getX().doubleValue() - anchor.toEpochSecond(), point.getY().doubleValue()/ anchorPrice)).collect(Collectors.toList()),
                        color,
                        DrawInstruction.Style.DASHED,
                        "prediction"

                ));
                if (i < targetsLs.size()) {
                    TimeSeries<V> t =  targetsLs.get(i);
                    instructions.add(new DrawInstruction(
                            this.makeUnformattedTargets(t).stream().map(point -> new DrawInstruction.Point(point.getX().doubleValue() - anchor.toEpochSecond(), point.getY().doubleValue()/ anchorPrice)).collect(Collectors.toList()),
                            color,
                            DrawInstruction.Style.DOTTED,
                            "targets"

                    ));
                }
            }
        }
        return instructions;
    }
}

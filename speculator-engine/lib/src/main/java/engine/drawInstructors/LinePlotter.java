package engine.drawInstructors;

import engine.components.DrawInstruction;
import engine.components.DrawInstructor;
import engine.components.InstructedDrawer;
import engine.PriceData.Ticker;
import engine.PriceData.TimeSeries;
import engine.components.PredictManager;

import java.sql.Time;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LinePlotter <T extends Number, V extends Number> extends DrawInstructor<T, V> {
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
    protected List<DrawInstruction> drawAllPredict(List<PredictManager.PredictResult<T, V>> results) {
        return this.drawAllBacktest(results.stream()
                .map(res -> new PredictManager.BacktestResult<>(res, new TimeSeries<>(List.of())))
                .collect(Collectors.toList())
        );
    }

    @Override
    protected List<DrawInstruction> drawAllBacktest(List<PredictManager.BacktestResult<T, V>> results) {
        List<Ticker> tickers = results.stream().map(PredictManager.PredictResult::getTicker).collect(Collectors.toList());
        List<TimeSeries<T>> featuresLs = results.stream().map(PredictManager.PredictResult::getFeatures).collect(Collectors.toList());

        List<TimeSeries<T>> targetsLs = results.stream().map(PredictManager.BacktestResult::getTargets).collect(Collectors.toList());

        ZonedDateTime anchor = TimeSeries.getAnchor(featuresLs);
        ArrayList<DrawInstruction> instructions = new ArrayList<>();
        if (tickers.size() == 1) {
            List<TimeSeries<V>> predsLs = new ArrayList<>(results.get(0).getPrediction().values());
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
            List<List<TimeSeries<V>>> allPredLs = results.stream()
                    .map(PredictManager.PredictResult::getPrediction)
                    .map(Map::values)
                    .map(ArrayList::new)
                    .collect(Collectors.toList());
            System.out.println("end");

            for (int i = 0; i < tickers.size(); i++) {
                DrawInstruction.Color color = lineColors.get(i);
                TimeSeries<T> f = featuresLs.get(i);
                List<TimeSeries<V>> predsLs = allPredLs.get(i);
                double anchorPrice = f.priceAt(anchor).doubleValue();
                instructions.add(new DrawInstruction(
                        this.makeUnformattedFeatures(f).stream().map(point -> new DrawInstruction.Point(point.getX().doubleValue() - anchor.toEpochSecond(), point.getY().doubleValue()/ anchorPrice)).collect(Collectors.toList()),
                        color,
                        DrawInstruction.Style.SOLID,
                        "features"

                ));

                predsLs.stream()
                        .map(this::makeUnformattedPrediction)
                        .map(line -> line.stream().map(point -> new DrawInstruction.Point(point.getX().doubleValue() - anchor.toEpochSecond(), point.getY().doubleValue()/ anchorPrice)).collect(Collectors.toList()))
                        .forEach(line -> instructions.add(new DrawInstruction(line, color, DrawInstruction.Style.DASHED, "prediction")));

                if (i < targetsLs.size()) {
                    TimeSeries<T> t =  targetsLs.get(i);
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

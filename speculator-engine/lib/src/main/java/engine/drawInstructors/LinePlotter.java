package engine.drawInstructors;

import engine.menus.DrawInstructors;
import engine.Serialisation.StateMachine;
import engine.components.DrawInstruction;
import engine.components.DrawInstructor;
import engine.PriceData.TimeSeries;

import java.util.List;
import java.util.Map;

public class LinePlotter extends DrawInstructor {

    public LinePlotter(int index) {
        super(index);
    }

    @Override
    protected List<DrawInstruction.Point> plotFeatures(TimeSeries data) {
        return data.extract((dt, px) -> new DrawInstruction.Point(dt.toEpochSecond(), px));
    }

    @Override
    protected List<DrawInstruction.Point> plotTargets(TimeSeries data) {
        return data.extract((dt, px) -> new DrawInstruction.Point(dt.toEpochSecond(), px));
    }

    @Override
    protected List<DrawInstruction.Point> plotPreds(TimeSeries data) {
        return data.extract((dt, px) -> new DrawInstruction.Point(dt.toEpochSecond(), px));
    }

    @Override
    public String toString() {
        return "basic line";
    }
//
//    List<DrawInstruction.Point> makeUnformattedFeatures(TimeSeries<?> features) {
//        // shape: ticker/1 -> feature
//        // before normalisation
//        return features.extract((dt, px) -> new DrawInstruction.Point(dt.toEpochSecond(), px));
//    }
//
//    List<DrawInstruction.Point> makeUnformattedPrediction(TimeSeries<?> pred) {
//        // shape: ticker/predictor -> prediction
//        // before normalisation
//        return pred.extract((dt, px) -> new DrawInstruction.Point(dt.toEpochSecond(), px));
//    }
//
//    List<DrawInstruction.Point> makeUnformattedTargets(TimeSeries<?> targets) {
//        // shape: ticker/1 -> feature
//        // before normalisation
//        return targets.extract((dt, px) -> new DrawInstruction.Point(dt.toEpochSecond(), px));
//    }
//
//    @Override
//    protected List<DrawInstruction> drawAllPredict(List<PredictManager.PredictResult> results) {
//        return this.drawAllBacktest(results.stream()
//                .map(res -> new PredictManager.BacktestResult<>(res, new TimeSeries<>(List.of())))
//                .collect(Collectors.toList())
//        );
//    }
//
//    @Override
//    protected List<DrawInstruction> drawAllBacktest(List<PredictManager.BacktestResult> results) {
//        List<Ticker> tickers = results.stream().map(PredictManager.PredictResult::getTicker).collect(Collectors.toList());
//        List<TimeSeries> featuresLs = results.stream().map(PredictManager.PredictResult::getFeatures).collect(Collectors.toList());
//
//        List<TimeSeries> targetsLs = results.stream().map(PredictManager.BacktestResult::getTargets).collect(Collectors.toList());
//
//        ZonedDateTime anchor = TimeSeries.getAnchor(featuresLs);
//        ArrayList<DrawInstruction> instructions = new ArrayList<>();
//        if (tickers.size() == 1) {
//            List<TimeSeries> predsLs = new ArrayList<>(results.get(0).getPrediction().values());
//            double anchorPrice = featuresLs.get(0).priceAt(anchor).doubleValue();
//            featuresLs.stream()
//                    .map(this::makeUnformattedFeatures)
//                    .map(line -> line.stream().map(point -> new DrawInstruction.Point(point.getX().doubleValue() - anchor.toEpochSecond(), point.getY().doubleValue()/ anchorPrice)).collect(Collectors.toList()))
//                    .forEach(line -> instructions.add(new DrawInstruction(line, lineColors.get(0), DrawInstruction.Style.SOLID, "features")));
//            Iterator<DrawInstruction.Color> colorsP = LinePlotter.lineColors.iterator();
//            predsLs.stream()
//                    .map(this::makeUnformattedPrediction)
//                    .map(line -> line.stream().map(point -> new DrawInstruction.Point(point.getX().doubleValue() - anchor.toEpochSecond(), point.getY().doubleValue()/ anchorPrice)).collect(Collectors.toList()))
//                    .forEach(line -> instructions.add(new DrawInstruction(line, colorsP.next(), DrawInstruction.Style.DASHED, "prediction")));
//            targetsLs.stream()
//                    .map(this::makeUnformattedTargets)
//                    .map(line -> line.stream().map(point -> new DrawInstruction.Point(point.getX().doubleValue() - anchor.toEpochSecond(), point.getY().doubleValue()/ anchorPrice)).collect(Collectors.toList()))
//                    .forEach(line -> instructions.add(new DrawInstruction(line, lineColors.get(0), DrawInstruction.Style.DOTTED, "targets")));
//        } else {
//            List<List<TimeSeries>> allPredLs = results.stream()
//                    .map(PredictManager.PredictResult::getPrediction)
//                    .map(Map::values)
//                    .map(ArrayList::new)
//                    .collect(Collectors.toList());
//            System.out.println("end");
//
//            for (int i = 0; i < tickers.size(); i++) {
//                DrawInstruction.Color color = lineColors.get(i);
//                TimeSeries f = featuresLs.get(i);
//                List<TimeSeries> predsLs = allPredLs.get(i);
//                double anchorPrice = f.priceAt(anchor).doubleValue();
//                instructions.add(new DrawInstruction(
//                        this.makeUnformattedFeatures(f).stream().map(point -> new DrawInstruction.Point(point.getX().doubleValue() - anchor.toEpochSecond(), point.getY().doubleValue()/ anchorPrice)).collect(Collectors.toList()),
//                        color,
//                        DrawInstruction.Style.SOLID,
//                        "features"
//
//                ));
//
//                predsLs.stream()
//                        .map(this::makeUnformattedPrediction)
//                        .map(line -> line.stream().map(point -> new DrawInstruction.Point(point.getX().doubleValue() - anchor.toEpochSecond(), point.getY().doubleValue()/ anchorPrice)).collect(Collectors.toList()))
//                        .forEach(line -> instructions.add(new DrawInstruction(line, color, DrawInstruction.Style.DASHED, "prediction")));
//
//                if (i < targetsLs.size()) {
//                    TimeSeries t =  targetsLs.get(i);
//                    instructions.add(new DrawInstruction(
//                            this.makeUnformattedTargets(t).stream().map(point -> new DrawInstruction.Point(point.getX().doubleValue() - anchor.toEpochSecond(), point.getY().doubleValue()/ anchorPrice)).collect(Collectors.toList()),
//                            color,
//                            DrawInstruction.Style.DOTTED,
//                            "targets"
//
//                    ));
//                }
//            }
//        }
//        return instructions;
//    }

    @Override
    public Map<String, String> save() {
        return Map.of();
    }
}

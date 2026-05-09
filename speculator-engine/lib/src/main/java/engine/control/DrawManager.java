package engine.control;

import static engine.components.DrawInstruction.colors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import engine.PriceData.State;
import engine.Serialisation.Menu;
import engine.Util;
import engine.components.DrawInstruction;
import engine.components.DrawInstructor;
import engine.components.InstructedDrawer;
import engine.components.ModelPredictor;
import engine.components.Ticker;

public class DrawManager{
//    private Menu<DrawInstructor> plotterMenu;
//    private Menu<Ticker> tickerMenu;
//    private Menu<ModelPredictor> modelMenu;
    private InstructedDrawer drawer;
    private DrawInstruction.DrawMapping cachedMapping;
    public DrawManager(
            InstructedDrawer drawer
    ){
        this.drawer = drawer;
    }

    public void draw(State state, List<Ticker> tickers, List<DrawInstructor> plotters) {
        this.drawer.undraw();
//        Map<Ticker, Util.Pair<DrawInstruction.Color, DrawInstruction.Style>> fMap = new HashMap<>();
//        Map<Ticker, Map<ModelPredictor, Util.Pair<DrawInstruction.Color, DrawInstruction.Style>>> pMap = new HashMap<>();
//        Map<Ticker, Util.Pair<DrawInstruction.Color, DrawInstruction.Style>> tMap = new HashMap<>();
////        List<Ticker> tickers = tickerMenu.getSelection();
//        tickers
//            .forEach(ticker -> {
//                DrawInstruction.Color mainColor = colors.next();
//                fMap.put(ticker, Util.Pair.create(mainColor, DrawInstruction.Style.SOLID));
//                pMap.put(ticker, Map.of());
//                tMap.put(ticker, Util.Pair.create(mainColor, DrawInstruction.Style.DOTTED));
//            });
        DrawInstruction.DrawMapping mapping = getMapping(tickers, List.of());
        plotters.stream()
                .map(plotter -> plotter.plotAll(state, mapping))
                .forEach(ls -> ls.forEach(inst -> inst.drawBy(this.drawer)
                ));
        this.drawer.legend(mapping);

    }
    public void drawPredict(State state, List<PredictManager.PredictResult> predictions, List<DrawInstructor> plotters) {
        this.drawer.undraw();
        DrawInstruction.DrawMapping mapping = this.getMapping(
                predictions.stream().map(PredictManager.PredictResult::getTicker).collect(Collectors.toList()),
                new ArrayList<>(predictions.get(0).getPrediction().keySet())
        );
        plotters.stream()
                .map(plotter -> plotter.plotAllPredict(state, predictions, mapping))
                .forEach(ls -> ls.forEach(inst -> inst.drawBy(this.drawer)
                ));
        this.drawer.legend(this.cachedMapping);
    }

    public void drawBacktest(State state, State targetState, List<PredictManager.PredictResult> results, List<DrawInstructor> plotters) {
        this.drawer.undraw();
        DrawInstruction.DrawMapping mapping = this.getMapping(
                results.stream().map(PredictManager.PredictResult::getTicker).collect(Collectors.toList()),
                new ArrayList<>(results.get(0).getPrediction().keySet())
        );
        plotters.stream()
                .map(plotter -> plotter.plotAllBacktest(state, targetState, results, mapping))
                .forEach(ls -> ls.forEach(inst -> inst.drawBy(this.drawer)
                ));
        this.drawer.legend(this.cachedMapping);
    }

    private DrawInstruction.DrawMapping getMapping(List<Ticker> tickers, List<ModelPredictor> models) {
        colors.reset();
        Map<Ticker, Util.Pair<DrawInstruction.Color, DrawInstruction.Style>> fMap = new HashMap<>();
        Map<Ticker, Map<ModelPredictor, Util.Pair<DrawInstruction.Color, DrawInstruction.Style>>> pMap = new HashMap<>();
        Map<Ticker, Util.Pair<DrawInstruction.Color, DrawInstruction.Style>> tMap = new HashMap<>();
        if (tickers.size() == 1) {
            DrawInstruction.Color mainColor = colors.peek(0);
            fMap.put(tickers.get(0), Util.Pair.create(mainColor, DrawInstruction.Style.DASHED));
            Map<ModelPredictor, Util.Pair<DrawInstruction.Color, DrawInstruction.Style>> pMapInner = new HashMap<>();
            pMap.put(tickers.get(0), pMapInner);
            models
                    .forEach(model -> pMapInner.put(model, Util.Pair.create(colors.next(), DrawInstruction.Style.SOLID)));
            tMap.put(tickers.get(0), Util.Pair.create(mainColor, DrawInstruction.Style.DOTTED));
        } else {
            tickers
                    .forEach(ticker -> {
                        DrawInstruction.Color mainColor = colors.next();
                        fMap.put(ticker, Util.Pair.create(mainColor, DrawInstruction.Style.DASHED));
                        Map<ModelPredictor, Util.Pair<DrawInstruction.Color, DrawInstruction.Style>> pMapInner = new HashMap<>();
                        pMap.put(ticker, pMapInner);
                        models.forEach(model -> pMapInner.put(model, Util.Pair.create(mainColor, DrawInstruction.Style.SOLID)));
                        tMap.put(ticker, Util.Pair.create(mainColor, DrawInstruction.Style.DOTTED));
                    });
        }
        return new DrawInstruction.DrawMapping(fMap, pMap, tMap);
    }


}

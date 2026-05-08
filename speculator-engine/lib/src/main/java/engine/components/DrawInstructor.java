package engine.components;

import engine.PriceData.State;
import engine.PriceData.TickerState;
import engine.PriceData.TimeSeries;
import engine.PriceData.Upstream;
import engine.Serialisation.CoreStateMachine;
import engine.Serialisation.StateMachine;
import engine.control.PredictManager;
import engine.menus.DrawInstructors;
import engine.menus.Upstreams;

import java.util.ArrayList;
import java.util.List;

public abstract class DrawInstructor extends CoreStateMachine<DrawInstructor> {

    public DrawInstructor(int index) {
        super(index);
    }

    public List<DrawInstruction> plotAll(
            State state,
            DrawInstruction.DrawMapping mapping
    ) {
        List<DrawInstruction> res = new ArrayList<>();
        for (Ticker ticker: state.getTickers()) {
            TickerState tickerState = state.getTickerState(ticker);
            TimeSeries features = tickerState.getIntervals().stream()
                    .parallel()
                    .map(tickerState::getPriceData)
                    .reduce(
                            TimeSeries.empty(),
                            TimeSeries::merge,
                            TimeSeries::merge
                    );
            res.add(new DrawInstruction(this.plotFeatures(features), mapping.color(ticker), mapping.style(ticker), "features"));
        }
        return res;
    }
    public List<DrawInstruction> plotAllPredict(
            State state,
            List<PredictManager.PredictResult> results,
            DrawInstruction.DrawMapping mapping
    ) {
        List<DrawInstruction> res = new ArrayList<>();
        for (PredictManager.PredictResult result: results) {
            Ticker ticker = result.getTicker();
            TickerState tickerState = state.getTickerState(ticker);
            TimeSeries features = tickerState.getIntervals().stream()
                            .parallel()
                            .map(tickerState::getPriceData)
                            .reduce(
                                    TimeSeries.empty(),
                                    TimeSeries::merge,
                                    TimeSeries::merge
                            );
            res.add(new DrawInstruction(this.plotFeatures(features), mapping.color(ticker), mapping.style(ticker), "features"));
            for (ModelPredictor model : result.getPrediction().keySet()) {
                res.add(new DrawInstruction(this.plotPreds(result.getPrediction().get(model)), mapping.color(ticker, model), mapping.style(ticker, model), "prediction"));
            }
        }
        return res;
    }
    public List<DrawInstruction> plotAllBacktest(
            State state,
            State targetState,
            List<PredictManager.PredictResult> results,
            DrawInstruction.DrawMapping mapping
    ) {
        List<DrawInstruction> res = this.plotAllPredict(state, results, mapping);
        for (Ticker ticker : targetState.getTickers()) {
            TickerState tickerState = targetState.getTickerState(ticker);
            TimeSeries features = tickerState.getIntervals().stream()
                    .parallel()
                    .map(tickerState::getPriceData)
                    .reduce(
                            TimeSeries.empty(),
                            TimeSeries::merge,
                            TimeSeries::merge
                    );
            res.add(new DrawInstruction(this.plotTargets(features), mapping.tColor(ticker), mapping.tStyle(ticker), "targets"));
        }
        return res;
    }

    @Override
    public CoreStateLoader<? extends StateMachine<DrawInstructor>> getLoader() {
        return new DILoader();
    }

    @Override
    public boolean equals(Object obj) {
        if (!obj.getClass().equals(this.getClass())) {
            return false;
        }
        StateMachine<?> sm = (StateMachine<?>) obj;
        return this.save().equals(sm.save());
    }

    protected abstract List<DrawInstruction.Point> plotFeatures(TimeSeries data);
    protected abstract List<DrawInstruction.Point> plotTargets(TimeSeries data);
    protected abstract List<DrawInstruction.Point> plotPreds(TimeSeries data);

    private static class DILoader extends CoreStateLoader<DrawInstructor> {
        @Override
        public List<DrawInstructor> getSource() {
            return DrawInstructors.list;
        }
    }
}

package com.example.speculator.ui.predict;

import static engine.Util.combine;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import engine.components.Plotter;
import engine.components.Snapshottable;
import engine.PriceData.TickerState;
import com.example.speculator.GlobalState;
import engine.Instances.UpstreamAdapters;
import com.example.speculator.MPDrawer;
import engine.PriceData.Candle;
import engine.components.ModelPredictor;
import engine.PriceData.Ticker;
import engine.PriceData.TimeSeries;
import com.example.speculator.R;
import engine.Util;
import engine.drawInstructors.LinePlotter;
import com.example.speculator.databinding.FragmentPredictBinding;
import com.github.mikephil.charting.charts.LineChart;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class PredictFragment extends Fragment {

    private FragmentPredictBinding binding;
    private View root;

    private LineChart chart;
    private Plotter<Float> plotter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPredictBinding.inflate(inflater, container, false);
        root = binding.getRoot();
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.chart = root.findViewById(R.id.predict_chart);
        plotter = GlobalState.Predict.instructors.get().get(0).makePlotter(new MPDrawer(chart));

        root.findViewById(R.id.predict_pull).setOnClickListener(this::pull);
        root.findViewById(R.id.predict_predict).setOnClickListener(this::predict);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    public void disableNewPlots() {
        root.findViewById(R.id.predict_pull).setEnabled(false);
        root.findViewById(R.id.predict_predict).setEnabled(false);
        root.findViewById(R.id.predict_pull).refreshDrawableState();
        root.findViewById(R.id.predict_predict).refreshDrawableState();
    }
    public void enableNewPlots() {
        root.findViewById(R.id.predict_pull).setEnabled(true);
        root.findViewById(R.id.predict_predict).setEnabled(true);
        root.findViewById(R.id.predict_pull).refreshDrawableState();
        root.findViewById(R.id.predict_predict).refreshDrawableState();
    }

    public void pull(View view) {
        this.disableNewPlots();
        this.plotter.unplot();
        this.plotter = GlobalState.Predict.instructors.get().get(0).makePlotter(new MPDrawer(chart));
        this.predict(
                GlobalState.Predict.tickers.get(),
                List.of(ModelPredictor.identity(
                        List.of(Duration.ofMinutes(1)),
                        List.of(500)
                )),
                ZonedDateTime.now()
        );
    }

    public void predict(List<Ticker> tickers, List<? extends ModelPredictor<Float, Float>> predictors, ZonedDateTime anchor) {
        this.disableNewPlots();
        this.plotter.unplot();
        this.plotter = GlobalState.Predict.instructors.get().get(0).makePlotter(new MPDrawer(chart));
        CompletableFuture<? extends List<? extends List<? extends List<? extends TickerState<Float>>>>> allTickerStatesCF = CompletableFuture.supplyAsync(() -> {
            // ticker -> predictor -> interval -> tickerState
            return tickers.stream().map(ticker -> {
                        return predictors.stream().map(predictor -> {
                                    return predictor.requestLeftUpstreams(UpstreamAdapters.getAdapterFor(ticker)).stream()
                                            .map(up -> ((Snapshottable)up).<Float>snapshot(anchor))
                                            .map(state -> state.getTickerState(ticker))
                                            .collect(Collectors.toList());
                                })
                                .collect(Collectors.toList());
                    })
                    .collect(Collectors.toList());
        });
        CompletableFuture<? extends List<? extends List<? extends List<? extends TimeSeries<Float>>>>> allFeaturesCF = allTickerStatesCF.thenApplyAsync(allTickerStates ->
                // ticker -> predictor -> interval -> series
                allTickerStates.stream().map(
                        allPredTS -> allPredTS.stream().map(
                                allIntervalTS -> allIntervalTS.stream().map(
                                        ts ->ts.getPriceData()
                                ).collect(Collectors.toList())
                        ).collect(Collectors.toList())
                ).collect(Collectors.toList()));

        CompletableFuture<? extends List<? extends Candle<Float>>> allLatestCF = allTickerStatesCF.thenApplyAsync(allTickerStates ->
                // ticker -> latest
                allTickerStates.stream().map(
                        allPredTS -> allPredTS.stream().map(
                                allIntervalTS -> allIntervalTS.stream().map(
                                        ts ->ts.getLatest()
                                ).max(Comparator.comparing(Candle::getTime)).orElse(null)
                        ).max(Comparator.comparing(Candle::getTime)).orElse(null)
                ).collect(Collectors.toList()));

        CompletableFuture<? extends List<? extends List<? extends List<? extends TimeSeries<Float>>>>> allPredsCF = allFeaturesCF.thenApplyAsync(allFeatures -> {
            // ticker -> predictor -> interval -> series
            List<? extends Candle<Float>> allLatest = allLatestCF.join();
            return Util.combine(allLatest.stream(),allFeatures.stream(), (latest, tickerF) ->
                    Util.combine(predictors.stream(), tickerF.stream(), (predictor, intervalF) -> predictor.predict(intervalF, latest)).collect(Collectors.toList())
            ).collect(Collectors.toList());
        });

        allFeaturesCF.thenCombineAsync(allPredsCF, (allFeatures, allPreds) -> {
            List<TimeSeries<Float>> plotF = allFeatures.stream()
                    .map(tickerF -> tickerF.stream().flatMap(predF -> predF.stream()))
                    .map(tickerF -> tickerF.map(x -> (TimeSeries<Float>)x)
                            .reduce(
                                    new TimeSeries<>(List.of()),
                                    (accum, nxt) -> accum.merge(nxt)
                            ))
                    .collect(Collectors.toList());
            List<TimeSeries<Float>> plotP = allPreds.stream()
                    .map(tickerP -> tickerP.stream().flatMap(predP -> predP.stream()))
                    .map(tickerP -> tickerP.map(x -> (TimeSeries<Float>)x)
                            .reduce(
                                    new TimeSeries<>(List.of()),
                                    (accum, nxt) -> accum.merge(nxt)
                            ))
                    .collect(Collectors.toList());
            Log.d("debug_pred","end"); // find!! bug
            getActivity().runOnUiThread(() -> {
                plotter.plotAll(tickers, plotF, plotP);
            });
            return null;
        }).thenRunAsync(() -> getActivity().runOnUiThread(() -> {
            this.enableNewPlots();
        }));
    }

    public void predict(View view) {
        List<Ticker> tickers = GlobalState.Predict.tickers.get();
        List<? extends ModelPredictor<Float, Float>> predictors;
        if (GlobalState.Predict.tickers.get().size() > 1) {
            predictors = List.of(GlobalState.Predict.selectedPredictors.get(0).second);
        } else {
            predictors = GlobalState.Predict.selectedPredictors.stream()
                    .map(pair -> pair.second)
                    .collect(Collectors.toList());
        }
        this.predict(tickers, predictors, ZonedDateTime.now());
    }
}
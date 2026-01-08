package com.example.speculator.ui.backtest;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import engine.Serialisation.SavedStateMachine;
import engine.components.PredictManager;
import engine.components.Snapshottable;
import engine.PriceData.TickerState;
import com.example.speculator.GlobalState;
import engine.Instances.UpstreamAdapters;
import com.example.speculator.MPDrawer;
import engine.PriceData.Candle;
import engine.components.ModelPredictor;
import engine.components.Plotter;
import engine.PriceData.Ticker;
import engine.PriceData.TimeSeries;
import com.example.speculator.R;
import engine.Util;

import com.example.speculator.databinding.FragmentBacktestBinding;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.LineData;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class BacktestFragment extends Fragment {

    private FragmentBacktestBinding binding;
    private View root;
    private LineChart chart;

    private ToggleButton dateView;
    private ToggleButton timeView;

    private DatePicker datePicker;

    private TimePicker timePicker;
    private ZonedDateTime selectedDateTime;
    private Plotter<Float, Float> plotter;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentBacktestBinding.inflate(inflater, container, false);
        root = binding.getRoot();
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.selectedDateTime = ZonedDateTime.now();
//        this.selectedDateTime = ZonedDateTime.of(LocalDateTime.of(2025, 1, 2, 0, 4), ZoneId.systemDefault());
        this.chart = root.findViewById(R.id.backtest_chart);
        this.chart.setData(new LineData());
        plotter = GlobalState.Predict.instructorMenu.get().get(0).makePlotter((new MPDrawer(chart)));
        this.dateView = root.findViewById(R.id.dateBtn);
        this.timeView = root.findViewById(R.id.timeBtn);
        this.datePicker = root.findViewById(R.id.calendarView);
        this.timePicker = root.findViewById(R.id.timeView);
        this.dateView.setOnCheckedChangeListener((btn, checked) -> {
            ToggleButton button = (ToggleButton) btn;
            if (checked) {
                this.datePicker.setVisibility(VISIBLE);
            } else {
                this.datePicker.setVisibility(GONE);
                this.selectedDateTime = ZonedDateTime.of(
                        LocalDate.of(
                                this.datePicker.getYear(),
                                this.datePicker.getMonth()+1,
                                this.datePicker.getDayOfMonth()
                        ),
                        this.selectedDateTime.toLocalTime(),
                        ZoneOffset.systemDefault()
                );
                button.setTextOff(this.selectedDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE));
            }
        });
        this.timeView.setOnCheckedChangeListener((btn, checked) -> {
            ToggleButton button = (ToggleButton) btn;
            if (checked) {
                this.timePicker.setVisibility(VISIBLE);
            } else {
                this.timePicker.setVisibility(GONE);
                this.selectedDateTime = ZonedDateTime.of(
                        this.selectedDateTime.toLocalDate(),
                        LocalTime.of(
                                this.timePicker.getHour(),
                                this.timePicker.getMinute(),
                                0
                        ),
                        ZoneOffset.systemDefault()
                );
                button.setTextOff(this.selectedDateTime.format(DateTimeFormatter.ISO_LOCAL_TIME));
            }
        });
        root.findViewById(R.id.backtest_pull).setOnClickListener(this::pull);
        root.findViewById(R.id.backtest_predict).setOnClickListener(this::predict);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    public void disableNewPlots() {
        root.findViewById(R.id.backtest_pull).setEnabled(false);
        root.findViewById(R.id.backtest_predict).setEnabled(false);
        root.findViewById(R.id.backtest_pull).refreshDrawableState();
        root.findViewById(R.id.backtest_predict).refreshDrawableState();
    }
    public void enableNewPlots() {
        root.findViewById(R.id.backtest_pull).setEnabled(true);
        root.findViewById(R.id.backtest_predict).setEnabled(true);
        root.findViewById(R.id.backtest_pull).refreshDrawableState();
        root.findViewById(R.id.backtest_predict).refreshDrawableState();
    }
    public void pull(View view) {
        this.disableNewPlots();
        this.plotter.unplot();
        plotter = GlobalState.Predict.instructorMenu.get().get(0).makePlotter((new MPDrawer(chart)));
        List<Ticker> tickers = GlobalState.Predict.tickerMenu.get();
        List<CompletableFuture<PredictManager.BacktestResult<Float, Float>>> results = tickers.stream()
                .map(ticker -> GlobalState.Predict.pullManager.backTestAsync(ticker, selectedDateTime))
                .collect(Collectors.toList());

        CompletableFuture.allOf(results.toArray(new CompletableFuture<?>[]{}))
                .thenRunAsync(() -> {
                    getActivity().runOnUiThread(() -> {
                        this.plotter.plotAllPredict(
                                results.stream().map(CompletableFuture::join).collect(Collectors.toList())
                        );
                        this.enableNewPlots();
                    });
                });
    }

    public void predict(View view) {
        this.disableNewPlots();
        this.plotter.unplot();
        plotter = GlobalState.Predict.instructorMenu.get().get(0).makePlotter((new MPDrawer(chart)));
        List<Ticker> tickers = GlobalState.Predict.tickerMenu.get();
        List<CompletableFuture<PredictManager.BacktestResult<Float, Float>>> results = tickers.stream()
                .map(ticker -> GlobalState.Predict.predictManager.backTestAsync(ticker, selectedDateTime))
                .collect(Collectors.toList());

        CompletableFuture.allOf(results.toArray(new CompletableFuture<?>[]{}))
                .thenRunAsync(() -> {
                    getActivity().runOnUiThread(() -> {
                        this.plotter.plotAllBackTest(
                                results.stream().map(CompletableFuture::join).collect(Collectors.toList())
                        );
                        this.enableNewPlots();
                    });
                }).join();
    }
//
//    public void predict(List<Ticker> tickers, List<? extends ModelPredictor<Float, Float>> predictors, ZonedDateTime anchor) {
//        this.disableNewPlots();
//        this.plotter.unplot();
//        plotter = GlobalState.Predict.instructorMenu.get().get(0).makePlotter((new MPDrawer(chart)));
//
//        CompletableFuture<? extends List<? extends List<? extends List<? extends TickerState<Float>>>>> allTickerStatesCF = CompletableFuture.supplyAsync(() -> {
//            // ticker -> predictor -> interval -> tickerState
//            return tickers.stream().map(ticker -> {
//                        return predictors.stream().map(predictor -> {
//                                    return predictor.requestLeftUpstreams(UpstreamAdapters.getAdapterFor(ticker)).stream()
//                                            .map(up -> ((Snapshottable)up).<Float>snapshot(anchor))
//                                            .map(state -> state.getTickerState(ticker))
//                                            .collect(Collectors.toList());
//                                })
//                                .collect(Collectors.toList());
//                    })
//                    .collect(Collectors.toList());
//        });
//        CompletableFuture<? extends List<? extends List<? extends List<? extends TimeSeries<Float>>>>> allFeaturesCF = allTickerStatesCF.thenApplyAsync(allTickerStates ->
//                // ticker -> predictor -> interval -> series
//                allTickerStates.stream().map(
//                                allPredTS -> allPredTS.stream().map(
//                                        allIntervalTS -> allIntervalTS.stream().map(
//                                                ts ->ts.getPriceData()
//                                        ).collect(Collectors.toList())
//                                ).collect(Collectors.toList())
//                        ).collect(Collectors.toList()));
//
//        CompletableFuture<? extends List<? extends Candle<Float>>> allLatestCF = allTickerStatesCF.thenApplyAsync(allTickerStates ->
//                // ticker -> latest
//                allTickerStates.stream().map(
//                        allPredTS -> allPredTS.stream().map(
//                                allIntervalTS -> allIntervalTS.stream().map(
//                                        ts ->ts.getLatest()
//                                ).max(Comparator.comparing(Candle::getTime)).orElse(null)
//                        ).max(Comparator.comparing(Candle::getTime)).orElse(null)
//                ).collect(Collectors.toList()));
//
//        CompletableFuture<? extends List<? extends List<? extends List<? extends TimeSeries<Float>>>>> allTargetsCF = CompletableFuture.supplyAsync(() -> {
//            // ticker -> predictor -> interval -> series
//            return tickers.stream().map(ticker -> {
//                        return predictors.stream().map(predictor -> {
//                                    return predictor.requestRightUpstreams(UpstreamAdapters.getAdapterFor(ticker)).stream()
//                                            .map(up -> up.<Float>verify(anchor))
//                                            .map(state -> state.getTickerState(ticker))
//                                            .map(tickerState -> tickerState.getPriceData())
//                                            .collect(Collectors.toList());
//                                })
//                                .collect(Collectors.toList());
//                    })
//                    .collect(Collectors.toList());
//        });
//
//        CompletableFuture<? extends List<? extends List<? extends List<? extends TimeSeries<Float>>>>> allPredsCF = allFeaturesCF.thenApplyAsync(allFeatures -> {
//            // ticker -> predictor -> interval -> series
//            List<? extends Candle<Float>> allLatest = allLatestCF.join();
//            return Util.combine(allLatest.stream(),allFeatures.stream(), (latest, tickerF) ->
//                    Util.combine(predictors.stream(), tickerF.stream(), (predictor, intervalF) -> predictor.predict(intervalF, latest)).collect(Collectors.toList())
//            ).collect(Collectors.toList());
//        });
//
//        allFeaturesCF.thenCombineAsync(allPredsCF, (allFeatures, allPreds) -> {
//            List<TimeSeries<Float>> plotF = allFeatures.stream()
//                    .map(tickerF -> tickerF.stream().flatMap(predF -> predF.stream()))
//                    .map(tickerF -> tickerF.map(x -> (TimeSeries<Float>)x)
//                            .reduce(
//                                    new TimeSeries<>(List.of()),
//                                    (accum, nxt) -> accum.merge(nxt)
//                            ))
//                    .collect(Collectors.toList());
//            List<TimeSeries<Float>> plotP = allPreds.stream()
//                    .map(tickerP -> tickerP.stream().flatMap(predP -> predP.stream()))
//                    .map(tickerP -> tickerP.map(x -> (TimeSeries<Float>)x)
//                            .reduce(
//                                    new TimeSeries<>(List.of()),
//                                    (accum, nxt) -> accum.merge(nxt)
//                            ))
//                    .collect(Collectors.toList());
//            Log.d("debug_target","start"); // find!! bug
//            List<TimeSeries<Float>> plotT = allTargetsCF.join().stream()
//                    .map(tickerF -> tickerF.stream().flatMap(predF -> predF.stream()))
//                    .map(tickerF -> tickerF.map(x -> (TimeSeries<Float>)x)
//                            .reduce(
//                                    new TimeSeries<>(List.of()),
//                                    (accum, nxt) -> accum.merge(nxt)
//                            ))
//                    .collect(Collectors.toList());
//            Log.d("debug_target","end"); // find!! bug
//            getActivity().runOnUiThread(() -> {
//                plotter.plotAll(tickers, plotF, plotP, plotT);
//            });
//            return null;
//        }).thenRunAsync(() -> getActivity().runOnUiThread(() -> {
//            this.enableNewPlots();
//        }));
//    }

}
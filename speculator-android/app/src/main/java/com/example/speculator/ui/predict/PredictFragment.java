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

import engine.Serialisation.SavedStateMachine;
import engine.components.Plotter;
import engine.components.PredictManager;
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
    private Plotter<Float, Float> plotter;

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
        plotter = GlobalState.Predict.instructorMenu.get().get(0).makePlotter(new MPDrawer(chart));

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
        plotter = GlobalState.Predict.instructorMenu.get().get(0).makePlotter((new MPDrawer(chart)));
        List<Ticker> tickers = GlobalState.Predict.tickerMenu.get();
        List<CompletableFuture<PredictManager.PredictResult<Float, Float>>> results = tickers.stream()
                .map(ticker -> GlobalState.Predict.pullManager.predictAsync(ticker))
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
        List<CompletableFuture<PredictManager.PredictResult<Float, Float>>> results = tickers.stream()
                .map(ticker -> GlobalState.Predict.predictManager.predictAsync(ticker))
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
}
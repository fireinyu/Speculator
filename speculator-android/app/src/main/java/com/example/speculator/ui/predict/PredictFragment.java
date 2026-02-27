package com.example.speculator.ui.predict;

import static engine.Util.combine;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import engine.components.Plotter;
import engine.components.PredictManager;

import com.example.speculator.GlobalState;
import com.example.speculator.MPDrawer;

import com.example.speculator.R;

import com.example.speculator.databinding.FragmentPredictBinding;
import com.github.mikephil.charting.charts.LineChart;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ScheduledExecutorService;

public class PredictFragment extends Fragment {

    private FragmentPredictBinding binding;
    private View root;
    private ToggleButton predictToggle;
    private LineChart chart;
    private EditText minutesEntry;
    private EditText secondsEntry;
    private Button intervalSubmit;
    private Plotter<Float, Float> plotter;
    private ScheduledExecutorService clock;

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
        predictToggle = root.findViewById(R.id.predict_toggle);
        minutesEntry = root.findViewById(R.id.interval_minutes);
        secondsEntry = root.findViewById(R.id.interval_seconds);
        intervalSubmit = root.findViewById(R.id.interval_submit);
        minutesEntry.setText(String.valueOf(GlobalState.Loop.interval.toMinutes()));
        secondsEntry.setText(String.valueOf(GlobalState.Loop.interval.get(ChronoUnit.SECONDS)));
        intervalSubmit.setOnClickListener(btn -> {
            GlobalState.Loop.interval = Duration
                    .ofMinutes(Long.parseLong(minutesEntry.getText().toString()))
                    .plusSeconds(Long.parseLong(secondsEntry.getText().toString()));
            resetClock();
        });
        resetClock();
    }

    @Override
    public void onDestroyView() {
        clock.shutdown();
        super.onDestroyView();
        binding = null;
    }

    private void resetClock() {
        if (clock != null) {
            clock.shutdown();
        }
        clock = PredictManager.predictLoop(
                () -> predictToggle.isChecked() ? GlobalState.Predict.predictManager : GlobalState.Predict.pullManager,
                () -> GlobalState.Predict.tickerMenu.get(),
                GlobalState.Loop.interval,
                results -> getActivity().runOnUiThread(() -> {
                    this.plotter.unplot();
                    this.plotter = GlobalState.Predict.instructorMenu.get().get(0).makePlotter((new MPDrawer(chart)));
                    this.plotter.plotAllPredict(
                            results
                    );
                })
        );
    }

}
package com.example.speculator.ui.predict;

import static engine.Util.combine;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.speculator.GlobalState;

import com.example.speculator.R;

import com.example.speculator.databinding.FragmentPredictBinding;
import com.github.mikephil.charting.charts.LineChart;

public class PredictFragment extends Fragment {

    private FragmentPredictBinding binding;
    private View root;
    private ToggleButton predictToggle;
    private ToggleButton agentToggle;
    private LineChart chart;
//    private EditText minutesEntry;
//    private EditText secondsEntry;
//    private Button intervalSubmit;

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
        GlobalState.drawer.setChart(this.chart);
        predictToggle = root.findViewById(R.id.predictToggle);
        agentToggle = root.findViewById(R.id.agentToggle);
//        minutesEntry = root.findViewById(R.id.interval_minutes);
//        secondsEntry = root.findViewById(R.id.interval_seconds);
//        intervalSubmit = root.findViewById(R.id.interval_submit);
//        minutesEntry.setText(String.valueOf(GlobalState.Loop.interval.toMinutes()));
//        secondsEntry.setText(String.valueOf(GlobalState.Loop.interval.get(ChronoUnit.SECONDS)));
//        intervalSubmit.setOnClickListener(btn -> {
//            GlobalState.Loop.interval = Duration
//                    .ofMinutes(Long.parseLong(minutesEntry.getText().toString()))
//                    .plusSeconds(Long.parseLong(secondsEntry.getText().toString()));
//            resetClock();
//        });
        agentToggle.setOnCheckedChangeListener((btn, checked) -> this.configCycle());
        predictToggle.setOnCheckedChangeListener((btn, checked) -> this.configCycle());
        GlobalState.app.predictPlotCycle();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void configCycle() {
        if (predictToggle.isChecked()) {
            if (agentToggle.isChecked()) {
                GlobalState.app.predictActCycle();
            } else {
                GlobalState.app.predictPlotCycle();
            }
        } else {
            GlobalState.app.endTasks();
        }
    }

}
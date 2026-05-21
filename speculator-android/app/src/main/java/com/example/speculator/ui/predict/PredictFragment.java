package com.example.speculator.ui.predict;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static engine.Util.combine;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.example.speculator.Defaults;
import com.example.speculator.GlobalState;

import com.example.speculator.R;

import com.example.speculator.databinding.FragmentPredictBinding;
import com.example.speculator.uiComponents.ActionsView;
import com.github.mikephil.charting.charts.LineChart;

import java.time.Duration;

public class PredictFragment extends Fragment {

    private FragmentPredictBinding binding;
    private View root;
    private ToggleButton predictToggle;
    private ToggleButton agentToggle;
    private EditText minField;
    private EditText secField;
    private Duration interval = Defaults.appCycleInterval;
    private LineChart chart;
    private ActionsView actionsView;
    private ViewGroup actionsBox;
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
        minField = root.findViewById(R.id.minField);
        secField = root.findViewById(R.id.secField);
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
        minField.setText(String.valueOf(interval.getSeconds()/60));
        secField.setText(String.valueOf(interval.getSeconds()%60));
        actionsBox = root.findViewById(R.id.actionsBox);
        actionsView = new ActionsView(getContext(), Defaults.actionListLimit);
        GlobalState.reporter.setActionsView(actionsView);
        actionsBox.addView(actionsView, new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void configCycle() {
        interval = Duration.ofMinutes(Long.parseLong(minField.getText().toString()));
        interval = interval.plusSeconds(Long.parseLong(secField.getText().toString()));
        System.out.println(interval.getSeconds());
        if (agentToggle.isChecked()) {
            actionsBox.setVisibility(VISIBLE);
        } else {
            actionsBox.setVisibility(GONE);
        }
        if (predictToggle.isChecked()) {
            if (agentToggle.isChecked()) {
                GlobalState.app.predictActCycle(interval);
            } else {
                GlobalState.app.predictPlotCycle();
            }
        } else {
            Log.d("endTasks","endTasks");
            GlobalState.app.endTasks();
        }
    }

}
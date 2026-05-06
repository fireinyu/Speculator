package com.example.speculator.ui.backtest;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.ToggleButton;

import androidx.fragment.app.Fragment;

import com.example.speculator.GlobalState;

import com.example.speculator.R;

import com.example.speculator.databinding.FragmentBacktestBinding;
import com.github.mikephil.charting.charts.LineChart;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class BacktestFragment extends Fragment {

    private FragmentBacktestBinding binding;
    private View root;
    private LineChart chart;

    private ToggleButton dateView;
    private ToggleButton timeView;
    private Button singleBacktest;
    private ToggleButton agentToggle;
    private ToggleButton backtestToggle;

    private DatePicker datePicker;

    private TimePicker timePicker;
    private ZonedDateTime selectedDateTime;


    public View onCreateView( LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentBacktestBinding.inflate(inflater, container, false);
        root = binding.getRoot();
        return root;
    }

    @Override
    public void onViewCreated(View view,  Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.selectedDateTime = ZonedDateTime.now();
//        this.selectedDateTime = ZonedDateTime.of(LocalDateTime.of(2025, 1, 2, 0, 4), ZoneId.systemDefault());
        this.chart = root.findViewById(R.id.backtest_chart);
        GlobalState.drawer.setChart(this.chart);
//        this.chart.setData(new LineData());
        this.dateView = root.findViewById(R.id.dateBtn);
        this.timeView = root.findViewById(R.id.timeBtn);
        this.datePicker = root.findViewById(R.id.calendarView);
        this.timePicker = root.findViewById(R.id.timeView);
        this.singleBacktest = root.findViewById(R.id.bPredict);
        this.backtestToggle = null;
        this.agentToggle = root.findViewById(R.id.bAgentToggle);
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
        this.singleBacktest.setOnClickListener((btn) -> {
            if (agentToggle.isChecked()) {
                // TODO
                GlobalState.app.save();
//                GlobalState.app.backtestAct(this.selectedDateTime);
            } else {
                GlobalState.app.backtestPredict(this.selectedDateTime);

            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


//    public void disableNewPlots() {
//        root.findViewById(R.id.backtest_pull).setEnabled(false);
//        root.findViewById(R.id.backtest_predict).setEnabled(false);
//        root.findViewById(R.id.backtest_pull).refreshDrawableState();
//        root.findViewById(R.id.backtest_predict).refreshDrawableState();
//    }
//    public void enableNewPlots() {
//        root.findViewById(R.id.backtest_pull).setEnabled(true);
//        root.findViewById(R.id.backtest_predict).setEnabled(true);
//        root.findViewById(R.id.backtest_pull).refreshDrawableState();
//        root.findViewById(R.id.backtest_predict).refreshDrawableState();
//    }


    private void configCycle() {
        // TODO
        if (backtestToggle.isChecked()) {
            if (agentToggle.isChecked()) {
            } else {
            }
        } else {
            GlobalState.app.endTasks();
        }
    }

}
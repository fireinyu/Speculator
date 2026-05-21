package com.example.speculator.ui.backtest;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.ToggleButton;

import androidx.fragment.app.Fragment;

import com.example.speculator.Defaults;
import com.example.speculator.GlobalState;

import com.example.speculator.R;

import com.example.speculator.databinding.FragmentBacktestBinding;
import com.example.speculator.uiComponents.DateTimeSelector;
import com.github.mikephil.charting.charts.LineChart;

import java.time.temporal.ChronoUnit;

public class BacktestFragment extends Fragment {

    private FragmentBacktestBinding binding;
    private View root;
    private LineChart chart;

    private Button singleBacktest;
    private ToggleButton agentToggle;
    private DatePicker datePicker;
    private TimePicker timePicker;
    private DateTimeSelector dateTimeSelector;


    public View onCreateView( LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentBacktestBinding.inflate(inflater, container, false);
        root = binding.getRoot();
        return root;
    }

    @Override
    public void onResume() {
//        this.selectedDateTime = ZonedDateTime.of(LocalDateTime.of(2025, 1, 2, 0, 4), ZoneId.systemDefault());
        this.chart = root.findViewById(R.id.backtest_chart);
        GlobalState.drawer.setChart(this.chart);
//        this.chart.setData(new LineData());
        this.datePicker = root.findViewById(R.id.calendarView);
        this.timePicker = root.findViewById(R.id.timeView);
        dateTimeSelector = new DateTimeSelector(root.getContext(), datePicker, timePicker, Defaults.backtestAt.truncatedTo(ChronoUnit.SECONDS));
        root.<ViewGroup>findViewById(R.id.backtestDTBox)
                .addView(
                        dateTimeSelector,
                        new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                );

        this.singleBacktest = root.findViewById(R.id.bPredict);
        this.agentToggle = root.findViewById(R.id.bAgentToggle);
        this.singleBacktest.setOnClickListener((btn) -> {
            if (agentToggle.isChecked()) {
                // TODO
                GlobalState.app.backtestAct(this.dateTimeSelector.getDateTime());
            } else {
                GlobalState.app.backtestPredict(this.dateTimeSelector.getDateTime());

            }
        });
        super.onResume();
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


}
package com.example.speculator.uiComponents;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.speculator.Defaults;
import com.example.speculator.R;

import java.time.Duration;
import java.time.ZonedDateTime;

import engine.components.Ticker;

public class SimControls{
    public static class NowSimControls extends ConstraintLayout {
        private EditText mmField;
        private EditText ssField;
        public NowSimControls(@NonNull Context context) {
            super(context);
            LayoutInflater.from(context).inflate(R.layout.sim_controls_now, this, true);
            mmField = findViewById(R.id.simIntervalMM);
            ssField = findViewById(R.id.simIntervalSS);
            mmField.setText(String.valueOf(Defaults.appCycleInterval.getSeconds()/60));
            ssField.setText(String.valueOf(Defaults.appCycleInterval.getSeconds()%60));


        }

        public Duration getInterval() {
            return Duration
                    .ofMinutes(Long.parseLong(mmField.getText().toString()))
                    .plusSeconds(Long.parseLong(ssField.getText().toString()));
        }

        public boolean ready() {
            return mmField.getText().length() > 0 && ssField.getText().length() > 0;
        }
    }

    public static class PastSimControls extends ConstraintLayout {
        private DateTimeSelector startDTSelector;
        private DateTimeSelector endDTSelector;
        private EditText mmField;
        private EditText ssField;

        public PastSimControls(
                @NonNull Context context,
                DatePicker startDatePicker,
                TimePicker startTimePicker,
                DatePicker endDatePicker,
                TimePicker endTimePicker
        ) {
            super(context);
            LayoutInflater.from(context).inflate(R.layout.sim_controls_past, this, true);
            ZonedDateTime at = ZonedDateTime.now();
            endDTSelector = new DateTimeSelector(context ,startDatePicker, startTimePicker, at);
            startDTSelector = new DateTimeSelector(context ,endDatePicker, endTimePicker, at.minus(Defaults.simPeriod));
            this.<ViewGroup>findViewById(R.id.simStartDTBox)
                    .addView(
                            startDTSelector,
                            new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    );
            this.<ViewGroup>findViewById(R.id.simEndDTBox)
                    .addView(
                            endDTSelector,
                            new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    );
            mmField = findViewById(R.id.simIntervalMM);
            ssField = findViewById(R.id.simIntervalSS);
            mmField.setText(String.valueOf(Defaults.simPastInterval.getSeconds()/60));
            ssField.setText(String.valueOf(Defaults.simPastInterval.getSeconds()%60));
        }

        public Duration getInterval() {
            return Duration
                    .ofMinutes(Long.parseLong(mmField.getText().toString()))
                    .plusSeconds(Long.parseLong(ssField.getText().toString()));
        }

        public ZonedDateTime getStart() {
            return startDTSelector.getDateTime();
        }

        public ZonedDateTime getEnd() {
            return endDTSelector.getDateTime();
        }
        public boolean ready() {
            return mmField.getText().length() > 0 && ssField.getText().length() > 0 && endDTSelector.getDateTime().isAfter(startDTSelector.getDateTime());
        }
    }
}

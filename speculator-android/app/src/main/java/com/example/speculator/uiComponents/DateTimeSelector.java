package com.example.speculator.uiComponents;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.speculator.R;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeSelector extends ConstraintLayout {
    private ZonedDateTime selection;
    private DatePicker datePicker;
    private TimePicker timePicker;
    private ToggleButton dateBtn;
    private ToggleButton timeBtn;
    public DateTimeSelector(
            @NonNull Context context,
            DatePicker datePicker,
            TimePicker timePicker,
            ZonedDateTime defaultSelection
    ) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.datetime_selector, this, true);
        dateBtn = findViewById(R.id.dateBtn);
        timeBtn = findViewById(R.id.timeBtn);
        this.datePicker = datePicker;
        this.timePicker = timePicker;
        selection = defaultSelection;
        dateBtn.setText(this.selection.format(DateTimeFormatter.ISO_LOCAL_DATE));
        timeBtn.setText(this.selection.format(DateTimeFormatter.ISO_LOCAL_TIME));
        this.dateBtn.setOnCheckedChangeListener((btn, checked) -> {
            ToggleButton button = (ToggleButton) btn;
            if (checked) {
                datePicker.updateDate(selection.getYear(), selection.getMonthValue()-1, selection.getDayOfMonth());
                this.datePicker.setVisibility(VISIBLE);
                this.datePicker.bringToFront();
            } else {
                this.datePicker.setVisibility(GONE);
                this.selection = ZonedDateTime.of(
                        LocalDate.of(
                                this.datePicker.getYear(),
                                this.datePicker.getMonth()+1,
                                this.datePicker.getDayOfMonth()
                        ),
                        this.selection.toLocalTime(),
                        ZoneOffset.systemDefault()
                );
                button.setTextOff(this.selection.format(DateTimeFormatter.ISO_LOCAL_DATE));
            }
        });
        this.timeBtn.setOnCheckedChangeListener((btn, checked) -> {
            ToggleButton button = (ToggleButton) btn;
            if (checked) {
                timePicker.setHour(selection.getHour());
                timePicker.setMinute(selection.getMinute());
                this.timePicker.setVisibility(VISIBLE);
                this.timePicker.bringToFront();
            } else {
                this.timePicker.setVisibility(GONE);
                this.selection = ZonedDateTime.of(
                        this.selection.toLocalDate(),
                        LocalTime.of(
                                this.timePicker.getHour(),
                                this.timePicker.getMinute(),
                                0
                        ),
                        ZoneOffset.systemDefault()
                );
                button.setTextOff(this.selection.format(DateTimeFormatter.ISO_LOCAL_TIME));
            }
        });
    }
    public DateTimeSelector(
            @NonNull Context context,
            DatePicker datePicker,
            TimePicker timePicker
    ) {
        this(context, datePicker, timePicker, ZonedDateTime.now());
    }
    public ZonedDateTime getDateTime() {
        return this.selection;
    }
}

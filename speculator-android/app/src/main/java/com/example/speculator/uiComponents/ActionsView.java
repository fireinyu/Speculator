package com.example.speculator.uiComponents;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.speculator.Defaults;
import com.example.speculator.R;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayDeque;
import java.util.Deque;

import engine.PriceData.NAVPosition;
import engine.components.Executor;
import engine.components.Ticker;

public class ActionsView extends ConstraintLayout {
    private ViewGroup holder;
    private Deque<View> rows;
    private int limit;

    public ActionsView(@NonNull Context context, int limit) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.actions, this, true);
        rows  = new ArrayDeque<>(limit);
        holder = findViewById(R.id.list);
        this.limit = limit;
    }

    private void addRow(View row) {
        if (rows.size() >= limit) {
            rows.pollLast();
        }
        rows.offerFirst(row);
    }

    private void redraw() {
        LayoutParams rowParams = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int)Defaults.formRowHeight);
        post(() -> {
            holder.removeAllViews();
            rows.stream()
                    .peek(row -> row.setLayoutParams(rowParams))
                    .forEach(holder::addView);
            Log.d("debug_actions", "" + holder.getChildCount());
        });

    }

    public void add(Executor.ExecutionResult action, String agent, ZonedDateTime at) {
        if (action.getStatus() == Executor.CompletionStatus.FAIL) {
            addRow(new FailedActionRow(getContext(), agent, action.getTicker(), at));
        } else {
            addRow(new FilledActionRow(getContext(), agent, action.getFilled(), action.getTicker(), at));
        }
        redraw();
    }

    private static class FilledActionRow extends ConstraintLayout {
        public FilledActionRow(@NonNull Context context, String agent, NAVPosition delta, Ticker ticker, ZonedDateTime at) {
            super(context);
            LayoutInflater.from(context).inflate(R.layout.action_row_filled, this, true);
            this.<TextView>findViewById(R.id.ticker).setText(ticker.getName());
            this.<TextView>findViewById(R.id.direction).setText(delta.getUnits() > 0 ? "buy" : "sell");
            this.<TextView>findViewById(R.id.units).setText(String.format("%.5g", Math.abs(delta.getUnits())));
            this.<TextView>findViewById(R.id.price).setText(String.format("%.5g", delta.getTotalCost() / delta.getUnits()));
            this.<TextView>findViewById(R.id.agent).setText(agent);
            this.<TextView>findViewById(R.id.timestamp).setText(at.truncatedTo(ChronoUnit.SECONDS).format(DateTimeFormatter.ISO_LOCAL_TIME));

        }
    }

    private static class FailedActionRow extends ConstraintLayout {
        public FailedActionRow(@NonNull Context context, String agent, Ticker ticker, ZonedDateTime at) {
            super(context);
            LayoutInflater.from(context).inflate(R.layout.action_row_failed, this, true);
            this.<TextView>findViewById(R.id.fillStatus).setText("failed");
            this.setBackgroundColor(Color.RED);
        }
    }
}

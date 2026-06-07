package com.example.speculator.uiComponents;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.speculator.Defaults;
import com.example.speculator.R;

import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

import engine.PriceData.NAVPosition;
import engine.Util;
import engine.components.Ticker;

public class PositionsView extends ConstraintLayout {
    private ViewGroup holder;

    public PositionsView(@NonNull Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.positions, this, true);
        holder = findViewById(R.id.list);
    }

    public void update(List<Util.Pair<Ticker, NAVPosition>> positions) {
        LayoutParams rowParams = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) Defaults.formRowHeight);
        List<View> rows = positions.stream()
                        .map(pair -> new PositionRow(getContext(), pair.first, pair.second))
                        .collect(Collectors.toList());
        post(() -> {
            holder.removeAllViews();
            rows.stream()
                    .peek(row -> row.setLayoutParams(rowParams))
                    .forEach(holder::addView);
        });
    }

    private static class PositionRow extends ConstraintLayout {
        public PositionRow(@NonNull Context context, Ticker ticker, NAVPosition position) {
            super(context);
            LayoutInflater.from(context).inflate(R.layout.position_row, this, true);
            this.<TextView>findViewById(R.id.ticker).setText(ticker.getName());
            this.<TextView>findViewById(R.id.units).setText(String.format("%.5g", position.getUnits()));
            this.<TextView>findViewById(R.id.cost).setText(String.format("%.5g", position.getAvgCost()));
            this.<TextView>findViewById(R.id.price).setText(String.format("%.5g", position.getPrice()));

        }
    }
}

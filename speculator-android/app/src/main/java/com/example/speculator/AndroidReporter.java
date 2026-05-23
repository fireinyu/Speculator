package com.example.speculator;

import static android.view.View.VISIBLE;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ToggleButton;

import com.example.speculator.uiComponents.ActionsView;
import com.example.speculator.uiComponents.DateTimeChart;
import com.example.speculator.uiComponents.MenuView;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.stream.Collectors;

import engine.Serialisation.Menu;
import engine.Util;
import engine.components.Reporter;
import engine.components.Executor;
import engine.components.Simulator;
import engine.components.Ticker;
import kotlin.Lazy;

public class AndroidReporter extends Reporter {
    private ActionsView actionsView;
    private CompletableFuture<DateTimeChart> navChart;
    private ViewGroup tickerMenuBox;
    private Menu<Ticker> tickerMenu;
    private Map<Ticker, CompletableFuture<DateTimeChart>> tickerNavCharts;
    private Map<Ticker, CompletableFuture<DateTimeChart>> tickerPriceCharts;

    public void setExecViews(
            ActionsView actionsView
    ) {
        this.actionsView = actionsView;
    }
    public void setSimViews(
            ViewGroup navChartBox,
            ViewGroup priceChartBox,
            ViewGroup tickerBox,
            ToggleButton tickerSelectToggle
    ) {
        this.tickerMenuBox = tickerBox;
        navChart = CompletableFuture.completedFuture(new DateTimeChart(navChartBox.getContext()));
        tickerNavCharts = new HashMap<>();
        tickerPriceCharts = new HashMap<>();
        navChartBox.removeAllViews();
        priceChartBox.removeAllViews();
        try {
            navChartBox.addView(
                    navChart.get(),
                    new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            );
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        tickerSelectToggle.setOnCheckedChangeListener((btn, checked) -> {
            if (checked) {
                tickerBox.setVisibility(VISIBLE);
            } else {
                tickerBox.setVisibility(View.GONE);
                navChartBox.removeAllViews();
                priceChartBox.removeAllViews();
                List<Ticker> selection = Optional.ofNullable(tickerMenu).map(Menu::getSelection).orElse(List.of());
                if (selection.isEmpty()) {
                    try {
                        navChartBox.addView(
                                navChart.get(),
                                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                        );
                    } catch (ExecutionException e) {
                        throw new RuntimeException(e);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    try {
                        navChartBox.addView(
                                tickerNavCharts.get(selection.get(0)).get(),
                                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                        );
                        priceChartBox.addView(
                                tickerPriceCharts.get(selection.get(0)).get(),
                                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                        );
                    } catch (ExecutionException e) {
                        throw new RuntimeException(e);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }

    @Override
    public void report(Executor.ExecutionResult result, String agent, ZonedDateTime at) {
        actionsView.add(result, agent, at);
    }

//    @Override
//    public void report(List<Simulator.SimResult> result) {
//        navChartBox.post(() -> doReport(result));
//    }
    @Override
    public void report(List<Simulator.SimResult> result) {
        Map<Ticker, List<Util.Pair<Double, Double>>> navLines = new HashMap<>();
        Map<Ticker, List<Util.Pair<Double, Double>>> priceLines = new HashMap<>();
        List<Util.Pair<Double, Double>> navLine = new ArrayList<>();

        result.forEach(res -> {
            navLine.add(Util.Pair.create((double)res.when().toEpochSecond(), res.nav()));

            res.tickers().forEach(ticker -> {
                navLines.putIfAbsent(ticker, new ArrayList<>());
                priceLines.putIfAbsent(ticker, new ArrayList<>());
                navLines.get(ticker).add(Util.Pair.create((double)res.when().toEpochSecond(), res.nav(ticker)));
                priceLines.get(ticker).add(Util.Pair.create((double)res.when().toEpochSecond(), res.position(ticker).getUnits()));
            });
        });
        navChart = navChart.thenApplyAsync(chart -> {
            chart.post(() -> {

                chart.clearValues();

                chart.addLine(navLine, new LineDataSet(List.of(), ""));

                chart.getData().notifyDataChanged();
                chart.notifyDataSetChanged();
                chart.invalidate();
            });
            return chart;
        });

        tickerMenuBox.post(()-> {
            for (Ticker ticker : navLines.keySet()) {
                if (!tickerNavCharts.containsKey(ticker)) {
                    tickerNavCharts.put(ticker, CompletableFuture.completedFuture(new DateTimeChart(tickerMenuBox.getContext())));
                    tickerPriceCharts.put(ticker, CompletableFuture.completedFuture(new DateTimeChart(tickerMenuBox.getContext())));
                }

                tickerNavCharts.get(ticker).thenApplyAsync(chart -> {
                    chart.post(() -> {
                        chart.clearValues();
                        chart.addLine(navLines.get(ticker), new LineDataSet(List.of(), ""));
                        chart.getData().notifyDataChanged();
                        chart.notifyDataSetChanged();
                        chart.invalidate();
                    });
                    return chart;
                });

                tickerPriceCharts.get(ticker).thenApplyAsync(chart -> {
                    chart.post(() -> {
                        chart.clearValues();
                        chart.addLine(priceLines.get(ticker), new LineDataSet(List.of(), ""));
                        chart.getData().notifyDataChanged();
                        chart.notifyDataSetChanged();
                        chart.invalidate();
                    });
                    return chart;
                });
            }
        });

        if (
                !navLines.keySet().stream().map(Ticker::getName).collect(Collectors.toSet()).equals(
                        Optional.ofNullable(tickerMenu).map(Menu::getLabels).map(HashSet::new).orElse(new HashSet<>())
                )
        ) {
            tickerMenu = new Menu<>(new ArrayList<>(navLines.keySet()),1);

            tickerMenuBox.post(() -> {
                tickerMenuBox.removeAllViews();
                tickerMenuBox.addView(
                        new MenuView<>(tickerMenuBox.getContext(), tickerMenu),
                        new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                );

            });
        }
    }




//        // STUB
//        Log.d("stub_sim", "start");
//        System.out.println(result);
//        result.stream()
//                        .map(Simulator.SimResult::nav)
//                        .forEach(nav -> Log.d("stub_sim", ""+nav));
//        Log.d("stub_sim", "end");


}

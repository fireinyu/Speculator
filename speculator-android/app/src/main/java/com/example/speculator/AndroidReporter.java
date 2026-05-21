package com.example.speculator;

import android.util.Log;

import com.example.speculator.uiComponents.ActionsView;

import java.time.ZonedDateTime;
import java.util.List;

import engine.components.Reporter;
import engine.components.Executor;
import engine.components.Simulator;

public class AndroidReporter extends Reporter {
    private ActionsView actionsView;

    public void setActionsView(ActionsView actionsView) {
        this.actionsView = actionsView;
    }

    @Override
    public void report(Executor.ExecutionResult result, String agent, ZonedDateTime at) {
        actionsView.add(result, agent, at);
    }

    @Override
    public void report(List<Simulator.SimResult> result) {
        // STUB
        Log.d("stub_sim", "start");
        System.out.println(result);
        result.stream()
                        .map(Simulator.SimResult::nav)
                        .forEach(nav -> Log.d("stub_sim", ""+nav));
        Log.d("stub_sim", "end");


    }
}

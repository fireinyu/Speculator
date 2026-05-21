package com.example.speculator;

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
        System.out.println(result);

    }
}

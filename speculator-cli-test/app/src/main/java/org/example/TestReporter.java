package org.example;

import engine.components.Reporter;
import engine.components.Simulator.SimResult;
import engine.components.Ticker;
import engine.components.Executor.CompletionStatus;
import engine.components.Executor.ExecutionResult;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import engine.PriceData.CostPosition;

public class TestReporter extends Reporter{
    
    @Override
    public void report(List<SimResult> results) {
        System.out.printf("final nav: %.5g\n", results.get(results.size()-1).nav());
        
    }

    @Override
    public void report(ExecutionResult result, String agent, ZonedDateTime at) {
        CompletionStatus status = result.getStatus();
        Ticker ticker = result.getTicker();
        CostPosition filled = result.getFilled();
        String a = filled.getUnits() > 0 ? "buy" : "sell";
        double u = Math.abs(filled.getUnits());
        if (status == CompletionStatus.SUCCESS || status == CompletionStatus.PARTIAL) {
            System.out.printf("%s: %s %.4g units for $%.4g: by %s at %s\n",
                ticker.getName(),
                a,
                u,
                filled.getTotalCost() / u,
                agent,
                at.format(DateTimeFormatter.ISO_LOCAL_TIME)
            );
        } else {
            System.out.println("%s: failed");
        }

        
    }
    
}

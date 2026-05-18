package org.example.startupScripts;

import java.util.List;
import java.util.stream.Stream;

import org.example.Main;
import org.example.StartupScript;

import engine.control.App;

public class TestScript3 implements StartupScript{
    @Override
    public void onStartUp(Main app) {
        Stream.of(
            "tic sel 0 1",
            "pre sel 0",
            "tic ls",
            "tic sel 0 1",
            "pre sel 1",
            "tic ls",
            "tic sel 0 1",
            "pre sel 2",
            "tic ls",
            "tic sel 0 1",
            "pre sel 3",
            "tic ls",
            "tic usel a",
            "pre sel 0",
            "tic ls",
            "tic usel a",
            "pre sel 1",
            "tic ls",
            "tic usel a",
            "pre sel 2",
            "tic ls",
            "tic usel a",
            "pre sel 3",
            "tic ls",
            "tic sel 1",
            "pre sel 0",
            "tic ls",
            "tic sel 1",
            "pre sel 1",
            "tic ls",
            "tic sel 1",
            "pre sel 2",
            "tic ls",
            "tic sel 1",
            "pre sel 3",
            "tic ls"
            

        ).forEach(app::run); 
    }
    
}

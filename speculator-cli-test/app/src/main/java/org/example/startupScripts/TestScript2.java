package org.example.startupScripts;

import java.util.List;
import java.util.stream.Stream;

import org.example.Main;
import org.example.StartupScript;

import engine.control.App;

public class TestScript2 implements StartupScript{
    @Override
    public void onStartUp(Main app) {
        Stream.of(
            "pre base sel 0",
            "pre conf 0 a",
            "pre mk",
            "tic sel 0",
            "pre conf 0 b",
            "pre mk",
            "tic usel 0",
            "tic sel 1",
            "pre conf 0 c",
            "pre mk",
            "tic sel 0",
            "pre conf 0 d",
            "pre mk",
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

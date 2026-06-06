package org.example.startupScripts;

import java.util.List;
import java.util.stream.Stream;

import org.example.Main;
import org.example.StartupScript;

import engine.control.App;

public class TestScript1 implements StartupScript{
    @Override
    public void onStartUp(Main app) {
        Stream.of(
            "auth 0 xxx",
            "auth 1 xxx",
            "agt off",
            "tic sel 0",
            "ups sel 1",
            "plt sel 1",
            "exe sel 0",
            "agt base sel 0",
            "agt conf 0 0",
            "agt conf 1 10",
            "agt mk",
            "agt sel 0",
            "mod base sel 1",
            "mod conf 0 0",
            "mod mk",
            "mod conf 0 3600",
            "mod mk",
            "mod sel 0 1"
        ).forEach(app::run); 
    }
    
}

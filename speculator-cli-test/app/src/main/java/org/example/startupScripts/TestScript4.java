package org.example.startupScripts;

import java.util.stream.Stream;

import org.example.Main;
import org.example.StartupScript;

public class TestScript4 implements StartupScript{
    @Override
    public void onStartUp(Main app) {
        Stream.of(
            "mod base sel 1",
            "mod conf 0 0",
            "mod mk",
            "mod sel 0",
            "tic sel 0",
            "plt sel 0",
            "ups sel 1",
            "agt off",
            "pred"

        ).forEach(app::run); 
    }

}

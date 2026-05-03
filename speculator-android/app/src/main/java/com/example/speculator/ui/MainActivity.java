package com.example.speculator.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import engine.PriceData.Upstream;
import engine.components.DrawInstructor;
import engine.components.Ticker;
import engine.Serialisation.SavedStateMachine;
import engine.Serialisation.Preset;
import engine.menus.DrawInstructors;
import engine.menus.Tickers;
import engine.menus.Upstreams;

import com.example.speculator.GlobalState;
import com.example.speculator.R;
import com.example.speculator.uiComponents.EditMenuView;
import com.example.speculator.uiComponents.MenuView;
import com.example.speculator.uiComponents.PresetMenuView;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.navigation.NavigationView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.speculator.databinding.ActivityMainBinding;

import java.util.List;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private ConstraintLayout popupDrawer;
    private PresetMenuView presetsView;
    private MenuView<Upstream> upstreamsView;
    private MenuView<Ticker> tickersView;
    private MenuView<DrawInstructor> plottersView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GlobalState.init(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.tickerBar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_backtest, R.id.nav_predict, R.id.nav_simulate, R.id.nav_deploy, R.id.nav_models_agents, R.id.nav_api)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        popupDrawer = findViewById(R.id.popupDrawer);
        findViewById(R.id.tickerBar).setOnClickListener(bar -> {
            Log.d("debug_tickers", "hi");
            if (popupDrawer.getVisibility() == View.GONE) {
                popupDrawer.setVisibility(View.VISIBLE);
            } else {
                popupDrawer.setVisibility(View.GONE);
            }
        });

        ViewGroup presetsBox = findViewById(R.id.presetsBox);
        ViewGroup plottersBox = findViewById(R.id.presetsBox);
        ViewGroup tickersBox = findViewById(R.id.presetsBox);
        ViewGroup upstreamsBox = findViewById(R.id.upstreamsBox);

        tickersView = new MenuView<>(this, Tickers.menu);
        presetsView = new PresetMenuView(this, GlobalState.app.getPresets());
        plottersView = new MenuView<>(this, DrawInstructors.menu);
        upstreamsView = new MenuView<>(this, Upstreams.menu);

        ViewGroup.LayoutParams menuParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        tickersView.setLayoutParams(menuParams);
        presetsView.setLayoutParams(menuParams);
        plottersView.setLayoutParams(menuParams);
        upstreamsView.setLayoutParams(menuParams);

        tickersBox.addView(tickersView);
        presetsBox.addView(presetsView);
        plottersBox.addView(plottersView);
        upstreamsBox.addView(upstreamsView);

        /// TODO: move to PresetMenuView class

        newPreset.setOnClickListener(btn -> {
            Preset<Float, Float> preset = new Preset<>(
                    newPresetName.getText().toString(),
                    GlobalState.Predict.selectedPredictors,
                    GlobalState.Predict.tickerMenu.get().stream()
                            .map(Ticker::getName)
                            .collect(Collectors.toList()),
                    GlobalState.Predict.instructorMenu.get().stream()
                            .map(SavedStateMachine::new)
                            .collect(Collectors.toList())
            );
            GlobalState.Presets.presets.add(preset);
            GlobalState.Presets.presetMenu.add(preset);
        });
        removePreset.setOnClickListener(btn -> {
            GlobalState.Presets.presetMenu.get().stream().
                    peek(ps -> {
                        if (GlobalState.Presets.defaultPreset.equals(ps)) {
                            GlobalState.Presets.defaultPreset.delete();
                        }
                    }).
                    forEach(GlobalState.Presets.presets::remove);
            GlobalState.Presets.presetMenu.removeSelected();
        });
        defaultPreset.setOnClickListener(btn -> {
            List<Preset<Float, Float>> presets = GlobalState.Presets.presetMenu.get();
            if (presets.isEmpty()) {
                return;
            }
            GlobalState.Presets.defaultPreset.put(GlobalState.Presets.presetMenu.get().get(0));
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items until the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

}
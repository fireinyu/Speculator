package com.example.speculator;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;

import engine.components.Ticker;
import engine.Serialisation.SavedStateMachine;
import engine.sugar.Preset;

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

    private ScrollView tickerScroll;
    private ScrollView instructorScroll;
    private ScrollView presetScroll;
    private Button newPreset;
    private Button removePreset;
    private Button defaultPreset;
    private EditText newPresetName;

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
        tickerScroll = findViewById(R.id.tickerScroll);
        instructorScroll = findViewById(R.id.plotterScroll);
        presetScroll = findViewById(R.id.presetScroll);
        newPreset = findViewById(R.id.newPreset);
        defaultPreset = findViewById(R.id.defaultPreset);
        removePreset = findViewById(R.id.removePreset);
        newPresetName = findViewById(R.id.newPresetName);
        findViewById(R.id.tickerBar).setOnClickListener(bar -> {
            Log.d("debug_tickers", "hi");
            if (popupDrawer.getVisibility() == View.GONE) {
                popupDrawer.setVisibility(View.VISIBLE);
            } else {
                popupDrawer.setVisibility(View.GONE);
            }
        });

        ChipGroup.LayoutParams btnParams = new ChipGroup.LayoutParams(
                ChipGroup.LayoutParams.MATCH_PARENT,
                ChipGroup.LayoutParams.WRAP_CONTENT
        );

        View tickerSelector = GlobalState.Predict.tickerMenu.getView();
        tickerSelector.setLayoutParams(btnParams);
        tickerScroll.addView(tickerSelector);

        View instructorSelector = GlobalState.Predict.instructorMenu.getView();
        instructorSelector.setLayoutParams(btnParams);
        instructorScroll.addView(instructorSelector);

        View presetSelector = GlobalState.Presets.presetMenu.getView();
        presetSelector.setLayoutParams(btnParams);
        presetScroll.addView(presetSelector);

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
        // Inflate the menu; this adds items to the action bar if it is present.
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
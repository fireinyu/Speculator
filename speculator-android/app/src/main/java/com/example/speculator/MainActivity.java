package com.example.speculator;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;

import engine.PriceData.Ticker;
import engine.components.DrawInstructor;
import engine.upstreams.Oanda;

import com.example.speculator.dynamicUI.MultiObjectMenu;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.navigation.NavigationView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.speculator.databinding.ActivityMainBinding;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;

    private ConstraintLayout popupDrawer;
    private ChipGroup tickerSelector;
    private RadioGroup plotterSelector;

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
        tickerSelector = findViewById(R.id.tickerSelector);
        plotterSelector = findViewById(R.id.plotterSelector);
        Oanda.authenticate(GlobalState.Authentication.Oanda.accNo, GlobalState.Authentication.Oanda.apiKey);
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

        GlobalState.Predict.tickers.forEach(
                ticker -> {
                    Chip btn = new Chip(this);
                    btn.setCheckable(true);
                    btn.setText(ticker.getName());
                    btn.setLayoutParams(btnParams);
                    if (GlobalState.Predict.selectedTickers.stream().anyMatch(ticker::equals)) {
                        btn.setChecked(true);
                    }
                    btn.setOnCheckedChangeListener(
                            (b, checked) -> {
                                if (checked) {
                                    GlobalState.Predict.selectedTickers.add(ticker);
                                } else {
                                    GlobalState.Predict.selectedTickers.removeIf(ticker::equals);
                                }
                            }
                    );
                    tickerSelector.addView(btn);
                }
        );
        Map<Integer, DrawInstructor<Float>> instructorIds = new HashMap<>();
        GlobalState.Predict.instructors.forEach(
                instructor -> {
                    RadioButton btn = new RadioButton(this);
                    btn.setText(instructor.toString());
                    btn.setId(View.generateViewId());
                    btn.setLayoutParams(btnParams);
                    if (instructor.equals(GlobalState.Predict.selectedInstructor)) {
                        btn.setChecked(true);
                    }
                    plotterSelector.addView(btn);
                    instructorIds.put(btn.getId(), instructor);
                }
        );
        Log.d("debug_plot", "" + plotterSelector.getChildCount());
        plotterSelector.setOnCheckedChangeListener((v, idx) -> GlobalState.Predict.selectedInstructor = instructorIds.get(idx));
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
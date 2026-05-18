package com.example.speculator.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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
    private Button save;

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

        ViewGroup presetBox = findViewById(R.id.presetBox);
        ViewGroup plotterBox = findViewById(R.id.plotterBox);
        ViewGroup tickerBox = findViewById(R.id.tickerBox);
        ViewGroup upstreamBox = findViewById(R.id.upstreamBox);
        save = (Button) navigationView.getHeaderView(0).findViewById(R.id.save);
        save.setOnClickListener(btn -> GlobalState.app.save());

        tickersView = new MenuView<>(this, GlobalState.app.getTickers());
        presetsView = new PresetMenuView(this, GlobalState.app.getPresets());
        plottersView = new MenuView<>(this, GlobalState.app.getPlotters());
        upstreamsView = new MenuView<>(this, GlobalState.app.getUpstreams());

        ViewGroup.LayoutParams menuParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        tickersView.setLayoutParams(menuParams);
        presetsView.setLayoutParams(menuParams);
        plottersView.setLayoutParams(menuParams);
        upstreamsView.setLayoutParams(menuParams);

        tickerBox.addView(tickersView);
        presetBox.addView(presetsView);
        plotterBox.addView(plottersView);
        upstreamBox.addView(upstreamsView);

        GlobalState.presettables.add(upstreamsView);
        GlobalState.presettables.add(tickersView);
        GlobalState.presettables.add(plottersView);
        GlobalState.presettables.add(presetsView);

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

    @Override
    protected void onStop() {
        GlobalState.app.save();
//        GlobalState.presettables.remove(presetsView);
//        GlobalState.presettables.remove(tickersView);
//        GlobalState.presettables.remove(upstreamsView);
//        GlobalState.presettables.remove(plottersView);
        binding = null;
        super.onStop();
    }
}
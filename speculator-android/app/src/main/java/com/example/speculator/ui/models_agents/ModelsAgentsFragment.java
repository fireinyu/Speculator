package com.example.speculator.ui.models_agents;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.speculator.dynamicUI.Builder;
import com.example.speculator.dynamicUI.Field;
import com.example.speculator.GlobalState;

import engine.Serialisation.EditMenu;
import engine.components.Agent;
import engine.components.Executor;
import engine.menus.Agents;
import engine.menus.Executors;
import engine.menus.ModelLoaders;
import engine.Serialisation.SavedStateMachine;
import engine.Serialisation.StateLoader;
import engine.components.ModelPredictor;
import com.example.speculator.R;
import com.example.speculator.databinding.FragmentModelsAgentsBinding;

import com.example.speculator.dynamicUI.ObjectMenu;
import com.example.speculator.uiComponents.EditMenuView;
import com.example.speculator.uiComponents.MenuView;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public class ModelsAgentsFragment extends Fragment {

    private FragmentModelsAgentsBinding binding;

    private View root;

    private ViewGroup modelsBox;

    private EditMenuView<Agent> agentsView;
    private EditMenuView<ModelPredictor> modelsView;
    private MenuView<Executor> executorsView;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentModelsAgentsBinding.inflate(inflater, container, false);
        root = binding.getRoot();
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("testing","hi");
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.modelsBox = this.root.findViewById(R.id.modelsBox);
        ViewGroup agentsBox = this.root.findViewById(R.id.agentsBox);
        ViewGroup executorsBox = this.root.findViewById(R.id.executorsBox);

        modelsView = new EditMenuView<>(this.getContext(), GlobalState.app.getModels());
        agentsView = new EditMenuView<>(this.getContext(), GlobalState.app.getAgents());
        executorsView = new MenuView<>(this.getContext(), GlobalState.app.getExecutors());

        ViewGroup.LayoutParams menuParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );

        modelsBox.addView(modelsView, menuParams);
        agentsBox.addView(agentsView, menuParams);
        executorsBox.addView(executorsView, menuParams);

        GlobalState.presettables.add(modelsView);
        GlobalState.presettables.add(agentsView);
        GlobalState.presettables.add(executorsView);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        GlobalState.presettables.remove(modelsView);
        GlobalState.presettables.remove(agentsView);
        GlobalState.presettables.remove(executorsView);
        binding = null;
        root = null;
        modelsBox = null;
        agentsView = null;
        modelsView = null;
        executorsView = null;
    }

}
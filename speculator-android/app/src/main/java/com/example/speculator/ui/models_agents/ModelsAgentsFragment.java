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

import engine.components.Agent;
import engine.menus.Agents;
import engine.menus.ModelLoaders;
import engine.Serialisation.SavedStateMachine;
import engine.Serialisation.StateLoader;
import engine.components.ModelPredictor;
import com.example.speculator.R;
import com.example.speculator.databinding.FragmentModelsAgentsBinding;
import engine.modelPredictors.NN16842;

import com.example.speculator.dynamicUI.ObjectMenu;
import com.example.speculator.uiComponents.EditMenuView;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public class ModelsAgentsFragment extends Fragment {

    private FragmentModelsAgentsBinding binding;

    private View root;

    private LinearLayout allBuilders;
    private ViewGroup modelsBox;
    private ViewGroup agentsBox;


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
        this.agentsBox = this.root.findViewById(R.id.agentsBox);

        View modelsView = new EditMenuView<>(this.getContext(), ModelLoaders.menu);
        View agentsView = new EditMenuView<>(this.getContext(), Agents.menu);

        ViewGroup.LayoutParams menuParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        modelsView.setLayoutParams(menuParams);
        agentsView.setLayoutParams(menuParams);

        modelsBox.addView(modelsView);
        agentsBox.addView(agentsView);

        this.allBuilders = this.root.findViewById(R.id.model_build);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
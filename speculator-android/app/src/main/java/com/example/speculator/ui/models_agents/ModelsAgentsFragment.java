package com.example.speculator.ui.models_agents;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.speculator.Builder;
import com.example.speculator.Field;
import com.example.speculator.GlobalState;
import engine.Instances.ModelPredictors;
import engine.components.ModelPredictor;
import com.example.speculator.R;
import com.example.speculator.databinding.FragmentModelsAgentsBinding;
import engine.modelPredictors.NN16842;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ModelsAgentsFragment extends Fragment {

    private FragmentModelsAgentsBinding binding;

    private View root;

    private LinearLayout allBuilders;

    private ChipGroup modelSelector;



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentModelsAgentsBinding.inflate(inflater, container, false);
        root = binding.getRoot();
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        this.refreshModels();
        Log.d("testing","hi");
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.allBuilders = this.root.findViewById(R.id.model_build);
        RadioGroup.LayoutParams btnParams = new RadioGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        // model builders selector
        RadioGroup builderSelector = this.root.findViewById(R.id.base_select);
        ModelPredictors.bases.forEach(
                (name, base) -> {
                    RadioButton btn = new RadioButton(this.getActivity());
                    btn.setText(name);
                    btn.setLayoutParams(btnParams);
                    builderSelector.addView(btn);
                }
        );
        builderSelector.setOnCheckedChangeListener(
                (selector, id) -> this.makeBuilders(ModelPredictors.bases.get(((RadioButton)selector.findViewById(id)).getText().toString()))
        );
        // model selector
        this.modelSelector = this.root.findViewById(R.id.model_select);
        this.refreshModels();

        // model delete button
        Button modelDeleteBtn = root.findViewById(R.id.delete_model);
         modelDeleteBtn.setOnClickListener(
                 btn -> {
                     GlobalState.Predict.selectedPredictors.forEach(pair ->
                             GlobalState.Predict.predictors.get(pair.first.first).remove(pair.first.second)
                     );
                     GlobalState.Predict.selectedPredictors = new ArrayList<>();
                     this.refreshModels();
                    }
         );

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void refreshModels () {
        ChipGroup.LayoutParams btnParams = new ChipGroup.LayoutParams(
                ChipGroup.LayoutParams.MATCH_PARENT,
                ChipGroup.LayoutParams.WRAP_CONTENT
        );
        this.modelSelector.removeAllViews();
        GlobalState.Predict.predictors.forEach(
                (baseName, models) -> {
                    models.keySet().forEach(name -> {
                        Chip btn = new Chip(this.getActivity());
                        btn.setCheckable(true);
                        btn.setText(name);
                        btn.setLayoutParams(btnParams);
                        if (GlobalState.Predict.selectedPredictors.stream().map(pair -> pair.first).anyMatch(pair -> pair.equals(Pair.create(baseName, name)))) {
                            btn.setChecked(true);
                        }
                        btn.setOnCheckedChangeListener(
                                (b, checked) -> {
                                    ModelPredictor<Float, Float> model = models.get(name).orElse(null);
                                    Log.d("testing", model.toString());
                                    if (checked) {
                                        GlobalState.Predict.selectedPredictors.add(Pair.create(Pair.create(baseName, name), model));
                                    } else {
                                        GlobalState.Predict.selectedPredictors = GlobalState.Predict.selectedPredictors.stream().filter(pair -> !pair.first.equals(Pair.create(baseName, name))).collect(Collectors.toList());
                                    }
                                }
                        );
                        modelSelector.addView(btn);
                    });
                }
        );
    }

    private void makeBuilders(ModelPredictor<Float, Float> base) {
        this.allBuilders.removeAllViews();
        LinearLayout.LayoutParams formParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        new Builders().dict.get(base).forEach(builder -> {
            ViewGroup form = builder.makeForm(this.getActivity());
            form.setLayoutParams(formParams);
            this.allBuilders.addView(form);
        });
    }
    private class Builders {

        private  class nnBuilder extends Builder<NN16842> {

            private Field<String> greeting = new Field<>() {
                @Override
                public String getPrompt() {
                    return "Enter greeting:";
                }

                @Override
                protected View getInputView(Context root) {
                    return new EditText(root);
                }

                @Override
                public String get(View src) {
                    return ((EditText)src).getText().toString();
                }
            };
            private nnBuilder() {
                super("NN16842-S5-M1");
                super.register(greeting);
            }

            @Override
            public NN16842 build(Context context) {
                NN16842 model = (NN16842) ModelPredictors.bases.get("NN16842-S5-M1").load(Map.of(
                        "greeting", this.greeting.get()
                ));
                GlobalState.Predict.predictors.get("NN16842-S5-M1").put(greeting.get(), model);
                ModelsAgentsFragment.this.refreshModels();
                return model;
            }
        }

        private Map<? extends ModelPredictor<Float, Float>, List<? extends Builder<? extends ModelPredictor<Float, Float>>>> dict = Map.of(
                //CONFIG
                ModelPredictors.bases.get("NN16842-S5-M1"), List.of(
                        new nnBuilder()
                )
        );
    }
}
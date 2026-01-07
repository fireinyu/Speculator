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
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.speculator.dynamicUI.Builder;
import com.example.speculator.dynamicUI.Field;
import com.example.speculator.GlobalState;
import engine.Instances.ModelLoaders;
import engine.Serialisation.SavedStateMachine;
import engine.Serialisation.StateLoader;
import engine.Serialisation.StateMachine;
import engine.components.ModelPredictor;
import com.example.speculator.R;
import com.example.speculator.databinding.FragmentModelsAgentsBinding;
import engine.modelPredictors.NN16842;

import com.example.speculator.dynamicUI.ObjectMenu;
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

    private ScrollView loaderScroll;
    private ScrollView modelScroll;
    private ObjectMenu<SavedStateMachine<ModelPredictor<Float, Float>>> modelMenu;


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
        this.allBuilders = this.root.findViewById(R.id.model_build);
        RadioGroup.LayoutParams btnParams = new RadioGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        // model builders selector
        this.loaderScroll = this.root.findViewById(R.id.loader_select);
        ObjectMenu<StateLoader<ModelPredictor<Float, Float>>> loaderMenu = ObjectMenu.of(
                this.getContext(),
                ModelLoaders.list,
                1,
                loaders -> {
                    if (loaders.size() > 0){
                        this.makeBuilders(loaders.get(0));
                    }
                }
        );
        View loaderForm = loaderMenu.getView();
        loaderForm.setLayoutParams(btnParams);
        this.loaderScroll.addView(loaderForm);

        // model selector
        this.modelScroll = this.root.findViewById(R.id.model_select);
        this.modelMenu = GlobalState.Predict.predictorMenu;
        View modelForm = this.modelMenu.getView();
        modelForm.setLayoutParams(btnParams);
        this.modelScroll.addView(modelForm);

        // model delete button
        Button modelDeleteBtn = root.findViewById(R.id.delete_model);
         modelDeleteBtn.setOnClickListener(
                 btn -> {
                     Log.d("debug_remove", ""+this.modelMenu.get().size());
                     this.modelMenu.get().forEach(GlobalState.Predict.predictors::remove);
                     this.modelMenu.removeSelected();
                    }
         );

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void makeBuilders(StateLoader<ModelPredictor<Float, Float>> loader) {
        this.allBuilders.removeAllViews();
        LinearLayout.LayoutParams formParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        new Builders().dict.get(loader).forEach(builder -> {
            ViewGroup form = builder.makeForm(this.getActivity());
            form.setLayoutParams(formParams);
            this.allBuilders.addView(form);
        });
    }
    private class Builders {

        private  class nnBuilder extends Builder<NN16842> {

            private Field<String> offset = new Field<>() {
                @Override
                public String getPrompt() {
                    return "Enter offset:";
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
                super.register(offset);
            }

            @Override
            public NN16842 build(Context context) {
                NN16842 model = new NN16842(Float.parseFloat(offset.get()));
                SavedStateMachine<ModelPredictor<Float, Float>> item = new SavedStateMachine<>(ModelLoaders.list.get(0), model.save());
                GlobalState.Predict.predictors.add(item);
                ModelsAgentsFragment.this.modelMenu.add(item);
                return model;
            }
        }

        private Map<? extends StateLoader<ModelPredictor<Float, Float>>, List<? extends Builder<? extends ModelPredictor<Float, Float>>>> dict = Map.of(
                //CONFIG
                ModelLoaders.list.get(0), List.of(
                        new nnBuilder()
                )
        );
    }
}
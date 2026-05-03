package com.example.speculator.ui.api;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.speculator.dynamicUI.Builder;
import com.example.speculator.dynamicUI.Field;
import com.example.speculator.GlobalState;
import com.example.speculator.R;
import com.example.speculator.databinding.FragmentApiBinding;

import java.util.List;

import engine.upstreams.Oanda;

public class ApiFragment extends Fragment {

    private FragmentApiBinding binding;
    private View root;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentApiBinding.inflate(inflater, container, false);
        root = binding.getRoot();
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LinearLayout allForms = ((ViewGroup)root).findViewById(R.id.api_list);
        LinearLayout.LayoutParams formParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        GlobalState.app.getAuthTargets().forEach(target -> {
            // TODO make form UI with submit
            ...
            ViewGroup form = new ScrollView(this.getContext());
            GlobalState.app.getAuthFields(target).forEach(field -> {

            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
package com.example.speculator.ui.api;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.speculator.dynamicUI.Builder;
import com.example.speculator.dynamicUI.Field;
import com.example.speculator.GlobalState;
import com.example.speculator.R;
import com.example.speculator.databinding.FragmentApiBinding;

import java.util.List;

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
         Builders.list.stream()
                 .map(builder -> builder.makeForm(this.getActivity()))
                 .map(form -> {form.setLayoutParams(formParams); return form;})
                 .forEach(form -> allForms.addView(form));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private static class Builders {
        private static List<? extends Builder<?>> list = List.of(
                //CONFIG
                new Builders.OandaBuilder()
        );
        private static class OandaBuilder extends Builder<List<String>> {
            private Field<String> userId = new Field<String>() {
                @Override
                public String getPrompt() {
                    return "User ID:";
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
            private Field<String> apiKey = new Field<String>() {
                @Override
                public String getPrompt() {
                    return "API Key:";
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
            private OandaBuilder() {
                super("Oanda");
                super.register(userId, apiKey);
            }

            @Override
            public List<String> build(Context context) {
                GlobalState.Authentication.Oanda.accNo.put(userId.get());
                GlobalState.Authentication.Oanda.apiKey.put(apiKey.get());
                return List.of(userId.get(), apiKey.get());
            }
        }
    }
}
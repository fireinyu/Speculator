package com.example.speculator.ui.api;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.example.speculator.dynamicUI.Builder;
import com.example.speculator.dynamicUI.Field;
import com.example.speculator.GlobalState;
import com.example.speculator.R;
import com.example.speculator.databinding.FragmentApiBinding;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import engine.upstreams.Oanda;

public class ApiFragment extends Fragment {

    private FragmentApiBinding binding;
    private View root;
    private LinearLayout form;
    private Button submit;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentApiBinding.inflate(inflater, container, false);
        root = binding.getRoot();
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        form = root.findViewById(R.id.authBox);
        submit = root.findViewById(R.id.authSubmit);
        GlobalState.app.getAuthFields().forEach(field -> {
            ViewGroup row = (ViewGroup)getLayoutInflater().inflate(R.layout.form_row, form, false);
            row.<TextView>findViewWithTag("label").setText(field);
            form.addView(
                    row,
                    new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            );
        });
        submit.setOnClickListener(btn -> {
            Map<String, String> creds = new HashMap<>();
            for (int i = 0 ; i < form.getChildCount(); i++) {
                ViewGroup row = (ViewGroup) form.getChildAt(i);
                String cred = row.<EditText>findViewWithTag("field").getText().toString();
                if (cred.isBlank()) {
                    continue;
                }
                creds.put(
                        row.<TextView>findViewWithTag("label").getText().toString(),
                        cred
                );
            }
            GlobalState.app.authenticate(creds);
            this.refresh();
        });
        this.refresh();
    }

    private void refresh() {
        for (int i = 0 ; i < form.getChildCount(); i++) {
            ViewGroup row = (ViewGroup) form.getChildAt(i);
            if (GlobalState.app.isAuthFilled(row.<TextView>findViewWithTag("label").getText().toString())) {
                row.<EditText>findViewWithTag("field").setHint("(filled)");
            } else {
                row.<EditText>findViewWithTag("field").setHint("(empty)");
            }

        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
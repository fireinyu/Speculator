package com.example.speculator.uiComponents;

import static android.widget.LinearLayout.VERTICAL;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.example.speculator.R;
import com.example.speculator.dynamicUI.Field;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import engine.Serialisation.EditMenu;
import engine.Serialisation.UserStateMachine;

public class EditMenuView<T extends UserStateMachine<T>> extends MenuView<T> {
    private EditMenu<T> menu;
    RadioGroup loaderView;
    LinearLayout formView;
    Button submitButton;
    Button deleteButton;
    private Map<Integer, Integer> loaderIdx;

    @Override
    void inflate() {
        LayoutInflater.from(getContext()).inflate(R.layout.editmenu_template, this, true);
    }

    public EditMenuView(Context context, EditMenu<T> menu) {
        super(context, menu);
        this.menu = menu;
        loaderView = findViewWithTag("loaders");
        formView = findViewWithTag("form");
        submitButton = findViewWithTag("create");
        deleteButton = findViewWithTag("delete");
        submitButton.setOnClickListener(btn -> {
            if (!listening) {
                return;
            }
            List<String> settings = new ArrayList<>();
            for (int i = 0; i < menu.getOptions().size(); i++) {
                settings.add(((TextView)formView.getChildAt(i)).getText().toString());
            }
            menu.add(settings);
            this.refresh();
        });
        deleteButton.setOnClickListener(btn -> {
            if (!listening) {
                return;
            }
            menu.removeSelected();
            this.refresh();
        });
        List<String> loaders = menu.loaders();
        for (int i = 0; i < menu.size(); i++) {
            String label = loaders.get(i);
            RadioButton item = new RadioButton(context);
            loaderIdx.put(item.getId(), i);
            item.setText(label);
            item.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            loaderView.addView(item);

        }
        loaderView.setOnCheckedChangeListener((grp, id) -> {
            menu.selectLoader(loaderIdx.get(id));
            this.refreshForm();
        });
        this.refreshForm();

    }

    private void refreshForm() {
        listening = false;
        loaderView.check(loaderView.getChildAt(menu.selectedLoaderIndex()).getId());
        formView.removeAllViews();
        menu.getOptions().forEach(option -> {
            ConstraintLayout row = new ConstraintLayout(getContext());
            TextView label = new TextView(getContext());
            label.setText(option);
            EditText field = new EditText(getContext());
            row.addView(label);
            row.addView(field);
            ConstraintSet constraints =  new ConstraintSet();
            constraints.connect(label.getId(), ConstraintSet.LEFT, row.getId(), ConstraintSet.LEFT);
            constraints.connect(label.getId(), ConstraintSet.TOP, row.getId(), ConstraintSet.TOP);
            constraints.connect(label.getId(), ConstraintSet.BOTTOM, row.getId(), ConstraintSet.BOTTOM);
            constraints.connect(label.getId(), ConstraintSet.RIGHT, field.getId(), ConstraintSet.LEFT);
            constraints.connect(field.getId(), ConstraintSet.TOP, row.getId(), ConstraintSet.TOP);
            constraints.connect(field.getId(), ConstraintSet.BOTTOM, row.getId(), ConstraintSet.BOTTOM);
            constraints.connect(field.getId(), ConstraintSet.RIGHT, row.getId(), ConstraintSet.RIGHT);
            constraints.applyTo(row);
            formView.addView(row, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        });
        listening = true;
    }

}

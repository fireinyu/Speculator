package com.example.speculator.uiComponents;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.ToggleButton;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.example.speculator.Presettable;
import com.example.speculator.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import engine.Serialisation.Menu;
import engine.Serialisation.StateMachine;

public class MenuView<T extends StateMachine<T>> extends ConstraintLayout implements Presettable {
    private Menu<T> menu;
    ChipGroup listView;
    boolean listening;
    private Context context;
    private Map<Integer, Integer> chipIdx;
    public MenuView(Context context, Menu<T> menu) {
        super(context);
        this.context = context;
        this.menu = menu;
        this.listening = true;
        this.inflate();
        listView = findViewWithTag("list");
        listView.setOnCheckedStateChangeListener((self, selected) -> {
            if (!this.listening) {
                return;
            }
            menu.unselectAll();
            menu.selectAll(getSelection());
            this.refresh();
        });
        this.populate();
        this.refresh();
    }
    void inflate() {
        LayoutInflater.from(context).inflate(R.layout.menu_template, this, true);
    }
    List<Integer> getSelection() {
        return listView.getCheckedChipIds().stream()
                .map(this.chipIdx::get)
                .collect(Collectors.toList());
    }
    private void populate() {
        this.chipIdx = new HashMap<>();
        listView.removeAllViews();
        List<String> labels = menu.getLabels();
        for (int i = 0; i < menu.size(); i++) {
            String label = labels.get(i);
            Chip item = new Chip(context);
            chipIdx.put(item.getId(), i);
            item.setText(label);
            item.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            listView.addView(item);
        }
    }

    @Override
    public void refresh() {
        this.listening = false;
        if (menu.size() != this.listView.getChildCount()) {
            this.populate();
        }
        for (int i = 0;  i < menu.size(); i++) {
            ((Chip)listView.getChildAt(i)).setChecked(false);
        }
        for (int index : menu.getSelectedIndices()) {
            ((ToggleButton)listView.getChildAt(index)).setChecked(true);
        }
        this.listening = true;
    }

}

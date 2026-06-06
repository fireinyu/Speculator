package com.example.speculator.uiComponents;

import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;


import com.example.speculator.GlobalState;
import com.example.speculator.Presettable;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import engine.Serialisation.EditMenu;
import engine.Serialisation.Preset;
import engine.Serialisation.PresetMenu;

public class PresetMenuView extends EditMenuView<Preset>{
    public PresetMenuView(Context context, PresetMenu menu) {
        super(context, menu);
//        this.listView.setOnCheckedStateChangeListener((grp, ids) -> {
//            if (!listening) {
//                return;
//            }
////            getSelection().forEach(menu::select);
////            Log.d("bugiii", menu.getSelection().get(0).save().get("tickers"));
//
//            GlobalState.presettables.forEach(Presettable::refresh);
//        });
    }
    @Override
    void populate() {
        PresetMenu menu = (PresetMenu) super.menu;
        this.chipIdx = new HashMap<>();
        listView.removeAllViews();
        List<String> labels = menu.getLabels();
        for (int i = 0; i < menu.size(); i++) {
            String label = labels.get(i);
            Chip item = new Chip(getContext());
            item.setCheckable(true);
            item.setId(generateViewId());
            chipIdx.put(item.getId(), i);
            item.setText(label);
            item.setOnCheckedChangeListener((btn, checked) -> {
                if (!this.listening) {
                    return;
                }
                if (checked) {
                    menu.select(chipIdx.get(btn.getId()));
                    GlobalState.presettables.forEach(Presettable::refresh);

                } else {
                    menu.unselect(chipIdx.get(btn.getId()));
                }

                this.refresh();
            });
            item.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            listView.addView(item);
        }
    }
}

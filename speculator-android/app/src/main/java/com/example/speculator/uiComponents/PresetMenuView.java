package com.example.speculator.uiComponents;

import android.content.Context;


import com.example.speculator.GlobalState;
import com.example.speculator.Presettable;
import com.google.android.material.chip.ChipGroup;

import engine.Serialisation.EditMenu;
import engine.Serialisation.Preset;
import engine.Serialisation.PresetMenu;

public class PresetMenuView extends EditMenuView<Preset>{
    private PresetMenu menu;
    public PresetMenuView(Context context, PresetMenu menu) {
        super(context, menu);
        this.menu = menu;
        this.listView.setOnCheckedStateChangeListener((grp, ids) -> {
            if (!listening) {
                return;
            }
            menu.select(getSelection().get(0));
            GlobalState.presettables.forEach(Presettable::refresh);
        });
    }

}

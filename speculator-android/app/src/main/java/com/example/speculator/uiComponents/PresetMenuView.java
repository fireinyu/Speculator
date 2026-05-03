package com.example.speculator.uiComponents;

import android.content.Context;

import androidx.annotation.NonNull;

import engine.Serialisation.EditMenu;
import engine.Serialisation.Preset;
import engine.Serialisation.PresetMenu;

public class PresetMenuView extends EditMenuView<Preset>{
    private PresetMenu menu;
    public PresetMenuView(@NonNull Context context, PresetMenu menu) {
        super(context, menu);
        this.menu = menu;
    }

}

package com.example.speculator.uiComponents;

import android.content.Context;

import androidx.annotation.NonNull;

import engine.Serialisation.EditMenu;
import engine.Serialisation.UserStateMachine;

public class EditMenuView<T extends UserStateMachine<T>> extends MenuView<T> {
    private EditMenu<T> menu;
    public EditMenuView(@NonNull Context context, EditMenu<T> menu) {
        super(context, menu);
        this.menu = menu;
    }
}

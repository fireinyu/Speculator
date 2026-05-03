package com.example.speculator.uiComponents;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import engine.Serialisation.CoreStateMachine;
import engine.Serialisation.Menu;
import engine.Serialisation.StateMachine;

public class MenuView<T extends StateMachine<T>> extends ConstraintLayout {
    private Menu<T> menu;
    public MenuView(@NonNull Context context, Menu<T> menu) {
        super(context);
        this.menu = menu;
    }

}

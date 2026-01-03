package com.example.speculator.dynamicUI;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.List;


public abstract class ObjectMenu <T> {
    List<T> items;
    private Context context;
    private ViewGroup form;

    public ObjectMenu(Context context, List<T> items) {
        this.context = context;
        this.items = items;
    }

    abstract View makeOption(Context context, T item);
    abstract ViewGroup makeEmptyForm(Context context);

    public final ViewGroup makeForm() {
        form = makeEmptyForm(this.context);
        this.update();
        return this.form;
    }

    public final void update(List<T> items) {
        this.items = items;
        this.update();
    }

    public final void update() {
        form.removeAllViews();
        this.items.stream()
                .map(item -> this.makeOption(this.context, item))
                .forEach(option -> this.form.addView(option));
    }


}

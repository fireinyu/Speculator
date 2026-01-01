package com.example.speculator;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.List;
import java.util.stream.IntStream;


public class ObjectMenu <T> {
    private List<T> items;
    private T selected;
    private Context context;
    private ViewGroup form;

    public ObjectMenu(Context context) {
        this.context = context;
    }
    View makeOption(Context context, T item) {
        RadioButton btn = new RadioButton(context);
        btn.setText(item.toString());
        return btn;
    }
    ViewGroup makeEmptyForm(Context context) {
        RadioGroup form = new RadioGroup(context);
        form.setOnCheckedChangeListener((view, idx) -> this.selected = this.items.get(idx));
        return form;
    }
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

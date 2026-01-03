package com.example.speculator.dynamicUI;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.example.speculator.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MultiObjectMenu<T> extends ObjectMenu<T>{

    private static ChipGroup.LayoutParams layoutParams = new ChipGroup.LayoutParams(ChipGroup.LayoutParams.MATCH_PARENT, ChipGroup.LayoutParams.WRAP_CONTENT);
    private List<T> selected;
    private Consumer<? super List<T>> callback;

    public MultiObjectMenu(Context context, List<T> items, Consumer<? super List<T>> callback) {
        super(context, items);
        this.callback = callback;
    }

    @Override
    ViewGroup makeEmptyForm(Context context) {
        ChipGroup form = new ChipGroup(context);
        form.setOnCheckedStateChangeListener((grp, selected) -> {
            this.selected = selected.stream().map(i -> super.items.get(i)).collect(Collectors.toList());
            this.callback.accept(this.selected);
        });
        return form;
    }

    @Override
    View makeOption(Context context, T item) {
        Chip btn  =new Chip(context);
        btn.setCheckable(true);
        btn.setLayoutParams(MultiObjectMenu.layoutParams);
        return btn;
    }

    public List<T> getSelected() {
        return this.selected;
    }
}

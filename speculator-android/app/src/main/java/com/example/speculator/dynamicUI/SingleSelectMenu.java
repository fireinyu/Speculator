package com.example.speculator.dynamicUI;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class SingleSelectMenu <T> extends ObjectMenu<T> {

    private static RadioGroup.LayoutParams layoutParams = new RadioGroup.LayoutParams(RadioGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    private T selected;
    private Consumer<T> callback;
    private Map<Integer, T> itemIds;

    public SingleSelectMenu(Context context, List<T> items, Consumer<T> callback) {
        super(context, items);
        this.callback = callback;
        itemIds = new HashMap<>();
    }

    @Override
    View makeOption(Context context, @NonNull T item) {
        RadioButton btn = new RadioButton(context);
        btn.setText(item.toString());
        btn.setLayoutParams(SingleSelectMenu.layoutParams);
        itemIds.put(btn.getId(), item);
        return btn;
    }

    @Override
    ViewGroup makeEmptyForm(Context context) {
        RadioGroup form = new RadioGroup(context);
        form.setOnCheckedChangeListener((view, idx) -> {
            this.selected = this.itemIds.get(idx);
            this.callback.accept(this.selected);
        });
        return form;
    }

    public T get() {
        return this.selected;
    }
}

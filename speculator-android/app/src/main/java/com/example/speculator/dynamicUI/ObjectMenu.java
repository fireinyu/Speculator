package com.example.speculator.dynamicUI;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;


public class ObjectMenu <T> {

    private static ChipGroup.LayoutParams layoutParams = new ChipGroup.LayoutParams(ChipGroup.LayoutParams.MATCH_PARENT, ChipGroup.LayoutParams.WRAP_CONTENT);
    private ChipGroup form;
    List<Option<T>> options;
    private LinkedList<Option<T>> selectQueue;
    private Context context;
    private Consumer<? super List<T>> callback;
    private int maxSelect;
    private boolean enableCallback = true;

    public static <T> ObjectMenu<T> of (Context context, List<T> items, List<T> selected, int maxSelect, Consumer<? super List<T>> callback) {
        ObjectMenu<T> objectMenu = new ObjectMenu<>(context, List.of(), maxSelect, callback);
        items.forEach(item -> {
            if (selected.stream().anyMatch(item::equals)) {
                objectMenu.addSelected(item);
            } else {
                objectMenu.add(item);
            }
        });
        return objectMenu;
    }

    public static <T> ObjectMenu<T> of (Context context, List<T> items, int maxSelect, Consumer<? super List<T>> callback) {
        return ObjectMenu.of(context, items, List.of(), maxSelect, callback);
    }


    public ObjectMenu(Context context, List<Option<T>> options, int maxSelect, Consumer<? super List<T>> callback) {
        this.context = context;
        this.options = new ArrayList<>(options);
        this.selectQueue = new LinkedList<>();
        this.maxSelect = maxSelect;
        this.callback = callback;
        this.form = new ChipGroup(context);
        this.form.setOnCheckedStateChangeListener((v, id) -> {
            boolean toCallback = enableCallback;
            if (this.options.stream().filter(Option::selected).count() > this.maxSelect) {
                enableCallback = false;
                Option<T> toRemove = this.selectQueue.poll();
                toRemove.clear();
                enableCallback = true;
            }
            if (toCallback) {
                callback.accept(this.get());
            }
        });
        this.update();
    }

    public View getView() {
        return this.form;
    }

    public List<T> get() {
        return this.selectQueue.stream().map(Option::get).collect(Collectors.toList());
    }

    public void add(int idx, T item) {
        Option<T> option = new Option<>(this.context, item, false, this.selectQueue);
        this.options.add(idx, option);
        this.form.addView(option.getView(), idx);
    }

    public void add(T item) {
        Option<T> option = new Option<>(this.context, item, false, this.selectQueue);
        this.options.add(option);
        this.form.addView(option.getView());
    }

    public void remove(int idx) {
        this.form.removeViewAt(idx);
        this.options.remove(idx);
    }

    public void removeSelected() {
        this.options.removeIf(option -> this.selectQueue.stream().anyMatch(option::equals));
        this.update();
    }

    private void update() {
        this.form.removeAllViews();
        this.options.stream().map(Option::getView).forEach(form::addView);
    }

    private void addSelected(T item) {
        Option<T> option = new Option<>(this.context, item, true, this.selectQueue);
        this.options.add(option);
        this.form.addView(option.getView());
    }

    public static class Option<V> {
        private V item;
        private Chip btn;

        private Option(Context context, V item, boolean selected, LinkedList<Option<V>> queue) {
            this.item = item;
            this.btn = new Chip(context);
            this.btn.setCheckable(true);
            this.btn.setText(item.toString());
            Log.d("debug_tickers", "" + item);
            this.btn.setOnCheckedChangeListener((v, checked) -> {
                if (checked) {
                    queue.add(this);
                } else {
                    queue.removeIf(this::equals);
                }
            });
            this.btn.setChecked(selected);
            this.btn.setLayoutParams(new ViewGroup.LayoutParams(ChipGroup.LayoutParams.MATCH_PARENT, ChipGroup.LayoutParams.WRAP_CONTENT));
        }

        private Chip getView() {
            return this.btn;
        }

        private boolean selected() {
            return this.btn.isChecked();
        }

        private void clear() {
            this.btn.setChecked(false);
        }

        private V get() {
            return this.item;
        }
    }


}

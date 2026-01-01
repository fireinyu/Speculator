package com.example.speculator;

import android.content.Context;
import android.view.View;

import java.util.function.Function;

public abstract class Field <T> {
    private View source;

    public View setup(Context root) {
        this.source = this.getInputView(root);
        return this.source;
    }

    public abstract String getPrompt();

    protected abstract View getInputView(Context root);

    public T get() {
        return this.get(source);
    }

    public abstract T get(View src);
}

package com.example.speculator.dynamicUI;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public abstract class Builder <T>{

    private String label;
    private T obj;

    private Field<?>[] fields;

    public Builder( String label){
        this.label = label;
    }

    protected void register(Field<?>... fields) {
        this.fields = fields;
    }

    public ViewGroup makeForm(Context root) {
        // include overall label, input prompts, input entry views, submit button
        // bind build until submit button
        LinearLayout form = new LinearLayout(root);
        form.setOrientation(LinearLayout.VERTICAL);
        TextView label = new TextView(root);
        label.setText(this.label);
        form.addView(label);
        LinearLayout.LayoutParams fieldGroupParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        for (Field<?> field : this.fields) {
            LinearLayout fieldGroup = new LinearLayout(root);
            fieldGroup.setLayoutParams(fieldGroupParams);
            fieldGroup.setOrientation(LinearLayout.HORIZONTAL);
            TextView prompt = new TextView(root);
            prompt.setText(field.getPrompt());
            fieldGroup.addView(prompt);
            fieldGroup.addView(field.setup(root), LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            form.addView(fieldGroup);
        }
        Button submit = new Button(root);
        submit.setText("submit");
        submit.setOnClickListener(button -> this.obj = this.build(button.getContext()));
        form.addView(submit);
        return form;
    }


    public T get() {
        return this.obj;
    }

    public abstract T build(Context context);
    // called when submit button pressed

}

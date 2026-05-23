package com.example.speculator.ui.simulate;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.example.speculator.GlobalState;
import com.example.speculator.R;
import com.example.speculator.databinding.FragmentSimulateBinding;
import com.example.speculator.uiComponents.SimControls;

public class SimulateFragment extends Fragment {

    private FragmentSimulateBinding binding;
    private ViewGroup controlsBox;
    private ToggleButton simNow;
    private Button simRun;
    private View root;
    private SimControls.NowSimControls nowControls;
    private SimControls.PastSimControls pastControls;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSimulateBinding.inflate(inflater, container, false);
        root = binding.getRoot();
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        GlobalState.reporter.setSimViews(
                root.findViewById(R.id.simNAVChartBox),
                root.findViewById(R.id.simPriceChartBox),
                root.findViewById(R.id.simTickerMenuBox),
                root.findViewById(R.id.simTickerToggle)
        );
        controlsBox = root.findViewById(R.id.simControlsBox);
        simNow = root.findViewById(R.id.simNow);
        simRun = root.findViewById(R.id.simRun);
        simNow.setOnCheckedChangeListener((btn, checked) -> {
            setControls();
        });
        simRun.setOnClickListener(btn->{
            if (simNow.isChecked() && nowControls.ready()) {
                GlobalState.app.simulateCycle(nowControls.getInterval());
            } else if (pastControls.ready()){
                GlobalState.app.simulate(pastControls.getStart(), pastControls.getEnd(), pastControls.getInterval());
            }
        });
        setControls();
    }

    @Override
    public void onStop() {
        super.onStop();
        GlobalState.app.endTasks();
    }

    private void setControls() {
        if (simNow.isChecked()) {
            nowControls = new SimControls.NowSimControls(root.getContext());
            root.post(()->{
               controlsBox.removeAllViews();
               controlsBox.addView(
                       nowControls,
                       new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
               );
            });
        } else {
            pastControls = new SimControls.PastSimControls(
                    root.getContext(),
                    root.findViewById(R.id.simStartDatePicker),
                    root.findViewById(R.id.simStartTimePicker),
                    root.findViewById(R.id.simEndDatePicker),
                    root.findViewById(R.id.simEndTimePicker)
            );

            root.post(()->{
                controlsBox.removeAllViews();
                controlsBox.addView(
                        pastControls,
                        new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                );
            });
        }
    }
}
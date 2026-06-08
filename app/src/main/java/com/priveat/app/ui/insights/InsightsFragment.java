package com.priveat.app.ui.insights;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.priveat.app.databinding.FragmentInsightsBinding;

import java.util.ArrayList;
import java.util.List;

public class InsightsFragment extends Fragment {
    private FragmentInsightsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentInsightsBinding.inflate(inflater, container, false);
        renderCharts();
        return binding.getRoot();
    }

    private void renderCharts() {
        List<Entry> calorieEntries = new ArrayList<>();
        calorieEntries.add(new Entry(1, 1800));
        calorieEntries.add(new Entry(2, 2100));
        calorieEntries.add(new Entry(3, 1750));
        calorieEntries.add(new Entry(4, 1900));
        calorieEntries.add(new Entry(5, 2200));
        calorieEntries.add(new Entry(6, 2050));
        calorieEntries.add(new Entry(7, 1980));
        LineDataSet lineDataSet = new LineDataSet(calorieEntries, "Calories");
        lineDataSet.setColor(Color.parseColor("#C108FD"));
        lineDataSet.setCircleColor(Color.parseColor("#FEB966"));
        binding.chartCalories.setData(new LineData(lineDataSet));
        Description description = new Description();
        description.setText("");
        binding.chartCalories.setDescription(description);
        binding.chartCalories.invalidate();

        List<BarEntry> macroEntries = new ArrayList<>();
        macroEntries.add(new BarEntry(1, 40));
        macroEntries.add(new BarEntry(2, 25));
        macroEntries.add(new BarEntry(3, 35));
        BarDataSet barDataSet = new BarDataSet(macroEntries, "Macro Ratio");
        barDataSet.setColors(
                Color.parseColor("#C108FD"),
                Color.parseColor("#FEB966"),
                Color.parseColor("#16A34A")
        );
        binding.chartMacros.setData(new BarData(barDataSet));
        binding.chartMacros.setDescription(description);
        binding.chartMacros.invalidate();
    }
}

package com.priveat.app.ui.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.priveat.app.data.model.MealLog;
import com.priveat.app.databinding.ItemMealLogBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MealLogAdapter extends RecyclerView.Adapter<MealLogAdapter.MealViewHolder> {
    private final List<MealLog> items = new ArrayList<>();
    private final SimpleDateFormat formatter = new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault());

    public void submitList(List<MealLog> mealLogs) {
        items.clear();
        items.addAll(mealLogs);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMealLogBinding binding = ItemMealLogBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new MealViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MealViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class MealViewHolder extends RecyclerView.ViewHolder {
        private final ItemMealLogBinding binding;
        private boolean expanded;

        MealViewHolder(ItemMealLogBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(MealLog mealLog) {
            binding.textMealName.setText(mealLog.foodName);
            binding.textMealMeta.setText(mealLog.calories + " kcal • " + mealLog.protein + "P • "
                    + mealLog.carbs + "C • " + mealLog.fats + "F");
            binding.textTimestamp.setText(new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                    .format(new Date(mealLog.timestamp)));
            binding.textFreshness.setText(mealLog.freshness);
            binding.textShelfLife.setText(mealLog.shelfLife);
            binding.textColorings.setText(mealLog.hasColorings ? "Possible artificial colorings" : "No visual dye detected");
            binding.textRiskReason.setText(mealLog.riskReason);
            binding.textRiskScore.setText("Risk " + mealLog.riskScore + "/100");
            binding.textRiskScore.setTextColor(mealLog.riskScore >= 70
                    ? Color.parseColor("#DC2626")
                    : mealLog.riskScore >= 40 ? Color.parseColor("#F59E0B") : Color.parseColor("#16A34A"));
            binding.layoutExpand.setVisibility(expanded ? View.VISIBLE : View.GONE);
            binding.buttonExpand.setRotation(expanded ? 180f : 0f);
            binding.buttonExpand.setOnClickListener(v -> {
                expanded = !expanded;
                binding.layoutExpand.setVisibility(expanded ? View.VISIBLE : View.GONE);
                binding.buttonExpand.setRotation(expanded ? 180f : 0f);
            });
        }
    }
}

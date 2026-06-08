package com.priveat.app.ui.chat;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.priveat.app.data.model.ChatMessage;
import com.priveat.app.data.model.DietPlan;
import com.priveat.app.databinding.FragmentAiChatBinding;
import com.priveat.app.service.GeminiService;
import com.priveat.app.ui.adapter.ChatAdapter;

import java.util.ArrayList;
import java.util.List;

public class AIChatFragment extends Fragment {
    private FragmentAiChatBinding binding;
    private ChatAdapter adapter;
    private GeminiService geminiService;
    private final List<ChatMessage> messages = new ArrayList<>();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAiChatBinding.inflate(inflater, container, false);
        geminiService = new GeminiService();
        adapter = new ChatAdapter();
        binding.recyclerChat.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerChat.setAdapter(adapter);
        binding.buttonSend.setOnClickListener(v -> sendMessage());
        binding.buttonDietician.setOnClickListener(v -> generateDietPlan());
        seedConversation();
        return binding.getRoot();
    }

    private void seedConversation() {
        messages.add(new ChatMessage("Namaste. I'm your PrivEat nutrition expert. Ask about meals, conditions, or goals.", false));
        adapter.submitList(new ArrayList<>(messages));
    }

    private void sendMessage() {
        String input = binding.inputMessage.getText() == null ? "" : binding.inputMessage.getText().toString().trim();
        if (input.isEmpty()) {
            return;
        }
        messages.add(new ChatMessage(input, true));
        messages.add(new ChatMessage("For that request, I’d balance fiber, protein, and glycemic control while keeping it Indian-home-style.", false));
        binding.inputMessage.setText(null);
        adapter.submitList(new ArrayList<>(messages));
        binding.recyclerChat.smoothScrollToPosition(messages.size() - 1);
    }

    private void generateDietPlan() {
        geminiService.generateDietPlan("Traditional Indian weight loss plan", new GeminiService.DietPlanCallback() {
            @Override
            public void onSuccess(DietPlan dietPlan) {
                mainHandler.post(() -> {
                    messages.add(new ChatMessage(
                            "Breakfast: " + dietPlan.breakfast
                                    + "\nLunch: " + dietPlan.lunch
                                    + "\nSnack: " + dietPlan.snack
                                    + "\nDinner: " + dietPlan.dinner
                                    + "\nTotal: " + dietPlan.totalCalories + " kcal", false));
                    adapter.submitList(new ArrayList<>(messages));
                });
            }

            @Override
            public void onError(Throwable throwable) {
            }
        });
    }
}

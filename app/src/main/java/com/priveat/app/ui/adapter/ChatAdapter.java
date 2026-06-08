package com.priveat.app.ui.adapter;

import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.view.Gravity;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.priveat.app.data.model.ChatMessage;
import com.priveat.app.databinding.ItemChatMessageBinding;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    private final List<ChatMessage> items = new ArrayList<>();

    public void submitList(List<ChatMessage> messages) {
        items.clear();
        items.addAll(messages);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemChatMessageBinding binding = ItemChatMessageBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ChatViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        private final ItemChatMessageBinding binding;

        ChatViewHolder(ItemChatMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ChatMessage message) {
            binding.textMessage.setText(message.getMessage());
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) binding.bubbleContainer.getLayoutParams();
            params.gravity = message.isFromUser() ? Gravity.END : Gravity.START;
            binding.bubbleContainer.setLayoutParams(params);
            binding.bubbleContainer.setAlpha(message.isFromUser() ? 1f : 0.92f);
        }
    }
}

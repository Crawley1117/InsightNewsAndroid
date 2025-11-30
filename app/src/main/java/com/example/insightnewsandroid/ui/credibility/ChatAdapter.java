package com.example.insightnewsandroid.ui.credibility;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.insightnewsandroid.R;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
    private final List<ChatMessage> messages;
    public ChatAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = viewType == R.layout.item_chat_ai ? R.layout.item_chat_ai : R.layout.item_chat_user;
        View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(messages.get(position));
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getType() == ChatMessage.Type.AI
                ? R.layout.item_chat_ai
                : R.layout.item_chat_user;
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, fileName, scoreText, reasonText;
        ImageView messageImage;
        View resultCard;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
            messageImage = itemView.findViewById(R.id.messageImage);
            fileName = itemView.findViewById(R.id.fileName);
            resultCard = itemView.findViewById(R.id.resultCard);
            if (resultCard != null) {
                scoreText = itemView.findViewById(R.id.scoreText);
                reasonText = itemView.findViewById(R.id.reasonText);
            }
        }

        void bind(ChatMessage msg) {
            messageText.setText(msg.getText() != null ? msg.getText() : "");

            // ✅ 安全调用：判空
            if (messageImage != null) {
                if (msg.getImageUrl() != null && !msg.getImageUrl().isEmpty()) {
                    messageImage.setVisibility(View.VISIBLE);
                    Glide.with(itemView).load(msg.getImageUrl()).into(messageImage);
                } else {
                    messageImage.setVisibility(View.GONE);
                }
            }

            if (fileName != null) {
                if (msg.getFileName() != null) {
                    fileName.setVisibility(View.VISIBLE);
                    fileName.setText("📄 " + msg.getFileName());
                } else {
                    fileName.setVisibility(View.GONE);
                }
            }

            if (resultCard != null) {
                if (msg.getResult() != null) {
                    resultCard.setVisibility(View.VISIBLE);
                    scoreText.setText("可信度：" + msg.getResult().getScore() + "%");
                    reasonText.setText(msg.getResult().getReason());
                } else {
                    resultCard.setVisibility(View.GONE);
                }
            }
        }
    }
}
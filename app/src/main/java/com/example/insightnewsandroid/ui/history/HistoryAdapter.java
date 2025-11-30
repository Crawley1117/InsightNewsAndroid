// HistoryAdapter.java
package com.example.insightnewsandroid.ui.history;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.insightnewsandroid.EnhancedReportActivity;
import com.example.insightnewsandroid.R;
import com.example.insightnewsandroid.db.DetectionRecordEntity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HistoryAdapter extends ListAdapter<DetectionRecordEntity, HistoryAdapter.ViewHolder> {

    private final Context context;

    public HistoryAdapter(Context context) {
        super(DIFF_CALLBACK);
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleText;
        private final TextView dateText;
        private final TextView credibilityTag;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.titleText);
            dateText = itemView.findViewById(R.id.dateText);
            credibilityTag = itemView.findViewById(R.id.credibilityTag);
        }

        void bind(DetectionRecordEntity record) {
            // 设置文本
            titleText.setText(record.title);
            dateText.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    .format(new Date(record.detectionDate)));
            credibilityTag.setText(record.credibilityLevel);

            // --- 更新文本行数限制 ---
            titleText.setMaxLines(1);
            dateText.setMaxLines(1);
            credibilityTag.setMaxLines(1);
            // --- 修改结束 ---

            // --- 为整个 itemView 设置背景 ---
            int bg;
            switch (record.credibilityLevel) {
                case "高":
                    bg = R.drawable.bg_highscore;
                    break;
                case "较高":
                    bg = R.drawable.bg_sub_highscore;
                    break;
                case "中":
                    bg = R.drawable.bg_medium;
                    break;
                case "较低":
                    bg = R.drawable.bg_sub_lowscore;
                    break;
                case "低":
                    bg = R.drawable.bg_lowscore;
                    break;
                default:
                    bg = R.drawable.bg_medium;
                    break;
            }
            itemView.setBackgroundResource(bg);
            // --- 修改结束 ---

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, EnhancedReportActivity.class);
                intent.putExtra("title", record.title);
                intent.putExtra("fullText", record.fullText);
                intent.putExtra("suspiciousSpans", record.suspiciousSpansJson);
                intent.putExtra("credibilityLevel", record.credibilityLevel);
                context.startActivity(intent);
            });
        }
    }

    private static final DiffUtil.ItemCallback<DetectionRecordEntity> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<DetectionRecordEntity>() {
                @Override
                public boolean areItemsTheSame(@NonNull DetectionRecordEntity oldItem,
                                               @NonNull DetectionRecordEntity newItem) {
                    return oldItem.id == newItem.id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull DetectionRecordEntity oldItem,
                                                  @NonNull DetectionRecordEntity newItem) {
                    return oldItem.equals(newItem);
                }
            };
}
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
            titleText.setText(record.title);
            dateText.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    .format(new Date(record.detectionDate)));
            credibilityTag.setText(record.credibilityLevel);

            // ✅ Java 11 兼容的 switch 语句
            int bg;
            switch (record.credibilityLevel) {
                case "高":
                    bg = R.drawable.bg_credibility_high;
                    break;
                case "较高":
                    bg = R.drawable.bg_credibility_medium;
                    break;
                case "低":
                    bg = R.drawable.bg_credibility_low;
                    break;
                default:
                    bg = R.drawable.bg_credibility_medium;
                    break;
            }
            credibilityTag.setBackgroundResource(bg);

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
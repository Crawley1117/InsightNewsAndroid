// HistoryAdapter.java
package com.example.insightnewsandroid.ui.history;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.insightnewsandroid.R;
import com.example.insightnewsandroid.db.DetectionRecordEntity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HistoryAdapter extends ListAdapter<DetectionRecordEntity, HistoryAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(DetectionRecordEntity record);
    }

    private final OnItemClickListener listener;

    public HistoryAdapter(OnItemClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
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
        DetectionRecordEntity record = getItem(position);
        holder.bind(record, listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleText;
        private final TextView dateText;
        private final TextView credibilityTag;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.titleText);
            dateText = itemView.findViewById(R.id.dateText);
            credibilityTag = itemView.findViewById(R.id.credibilityTag);
        }

        public void bind(DetectionRecordEntity record, OnItemClickListener listener) {
            titleText.setText(record.title);

            String dateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    .format(new Date(record.detectionDate));
            dateText.setText(dateStr);

            credibilityTag.setText(record.credibilityLevel);

            // 根据可信度设置背景
            int backgroundRes;
            switch (record.credibilityLevel) {
                case "高":
                    backgroundRes = R.drawable.bg_credibility_high;
                    break;
                case "较高":
                    backgroundRes = R.drawable.bg_credibility_medium;
                    break;
                case "低":
                    backgroundRes = R.drawable.bg_credibility_low;
                    break;
                default:
                    backgroundRes = R.drawable.bg_credibility_medium;
            }
            credibilityTag.setBackgroundResource(backgroundRes);

            itemView.setOnClickListener(v -> listener.onItemClick(record));
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
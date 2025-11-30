package com.example.insightnewsandroid.ui.search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.insightnewsandroid.R;
import com.example.insightnewsandroid.databinding.ItemHotSearchBinding;
import java.util.List;

public class HotSearchAdapter extends RecyclerView.Adapter<HotSearchAdapter.ViewHolder> {

    private final List<String> hotSearchTerms;
    private final OnItemClickListener listener;
    private final Context context;

    public interface OnItemClickListener {
        void onItemClick(String term);
    }

    public HotSearchAdapter(Context context, List<String> hotSearchTerms, OnItemClickListener listener) {
        this.context = context;
        this.hotSearchTerms = hotSearchTerms;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHotSearchBinding binding = ItemHotSearchBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String term = hotSearchTerms.get(position);
        holder.bind(term, position + 1);
    }

    @Override
    public int getItemCount() {
        return hotSearchTerms != null ? hotSearchTerms.size() : 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemHotSearchBinding binding;

        ViewHolder(ItemHotSearchBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(final String term, int rank) {
            binding.tvRank.setText(String.valueOf(rank));
            binding.tvTerm.setText(term);

            // 为前三名设置不同的颜色
            if (rank == 1) {
                binding.tvRank.setTextColor(ContextCompat.getColor(context, R.color.material_red_500));
            } else if (rank == 2) {
                binding.tvRank.setTextColor(ContextCompat.getColor(context, R.color.material_orange_500));
            } else if (rank == 3) {
                binding.tvRank.setTextColor(ContextCompat.getColor(context, R.color.material_amber_500));
            } else {
                binding.tvRank.setTextColor(ContextCompat.getColor(context, R.color.gray));
            }

            itemView.setOnClickListener(v -> listener.onItemClick(term));
        }
    }
}

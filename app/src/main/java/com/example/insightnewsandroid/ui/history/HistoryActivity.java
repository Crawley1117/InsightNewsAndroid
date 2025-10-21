// HistoryActivity.java
package com.example.insightnewsandroid;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.SearchView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.insightnewsandroid.db.AppDatabase;
import com.example.insightnewsandroid.db.DetectionRecordEntity;
import com.example.insightnewsandroid.ui.history.HistoryAdapter;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HistoryActivity extends AppCompatActivity {

    private SearchView searchView;
    private RecyclerView recyclerView;
    private HistoryAdapter adapter;
    private AppDatabase db;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        searchView = findViewById(R.id.searchView);
        recyclerView = findViewById(R.id.recyclerView);

        db = AppDatabase.getDatabase(this);
        setupRecyclerView();
        loadAllRecords();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchRecords(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (TextUtils.isEmpty(newText)) {
                    loadAllRecords();
                } else {
                    searchRecords(newText);
                }
                return true;
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new HistoryAdapter(record -> {
            Intent intent = new Intent(HistoryActivity.this, ReportDetailActivity.class);
            intent.putExtra("title", record.title);
            intent.putExtra("date", record.detectionDate);
            intent.putExtra("credibility", record.credibilityLevel);
            intent.putExtra("report", record.fullReport);
            startActivity(intent);
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadAllRecords() {
        executor.execute(() -> {
            List<DetectionRecordEntity> records = db.detectionDao().getAllRecords();
            mainHandler.post(() -> adapter.submitList(records));
        });
    }

    private void searchRecords(String query) {
        executor.execute(() -> {
            List<DetectionRecordEntity> records = db.detectionDao().searchRecords(query.trim());
            mainHandler.post(() -> adapter.submitList(records));
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
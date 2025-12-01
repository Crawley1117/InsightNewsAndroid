package com.example.insightnewsandroid;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.insightnewsandroid.auth.AuthRepository;
import com.example.insightnewsandroid.data.manager.ApiManager;
import com.example.insightnewsandroid.data.model.BaseResponse;
import com.example.insightnewsandroid.data.model.DetectionHistoryItem;
import com.example.insightnewsandroid.db.AppDatabase;
import com.example.insightnewsandroid.db.DetectionRecordEntity;
import com.example.insightnewsandroid.ui.history.HistoryAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoryActivity extends AppCompatActivity {

    private SearchView searchView;
    private RecyclerView recyclerView;
    private HistoryAdapter adapter;
    private AppDatabase db;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    private AuthRepository authRepo;

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
        authRepo = new AuthRepository(this);

        setupRecyclerView();
        loadAllRecordsFromNetwork();

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                int position = viewHolder.getAdapterPosition();
                DetectionRecordEntity record = adapter.getCurrentList().get(position);

                new AlertDialog.Builder(HistoryActivity.this)
                        .setTitle("删除本地记录")
                        .setMessage("确定要删除这条本地记录吗？\n" + record.title)
                        .setPositiveButton("删除", (dialog, which) -> {
                            executor.execute(() -> {
                                db.detectionDao().deleteById(record.id);
                                mainHandler.post(() -> {
                                    loadAllRecordsFromNetwork();
                                    Toast.makeText(HistoryActivity.this, "本地记录已删除", Toast.LENGTH_SHORT).show();
                                });
                            });
                        })
                        .setNegativeButton("取消", (dialog, which) -> {
                            adapter.notifyItemChanged(position);
                            dialog.dismiss();
                        })
                        .show();
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchRecordsLocally(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (TextUtils.isEmpty(newText)) {
                    loadAllRecordsFromNetwork();
                } else {
                    searchRecordsLocally(newText);
                }
                return true;
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new HistoryAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadAllRecordsFromNetwork() {
        String token = authRepo.getAuthToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "用户未登录", Toast.LENGTH_SHORT).show();
            return;
        }

        Call<BaseResponse<List<DetectionHistoryItem>>> call = ApiManager.getAuthService().getDetectionHistory("Bearer " + token);
        call.enqueue(new Callback<BaseResponse<List<DetectionHistoryItem>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<DetectionHistoryItem>>> call, Response<BaseResponse<List<DetectionHistoryItem>>> response) {
                if (response.isSuccessful()) {
                    BaseResponse<List<DetectionHistoryItem>> baseResponse = response.body();
                    if (baseResponse != null && baseResponse.isSuccess()) {
                        List<DetectionHistoryItem> networkRecords = baseResponse.getData();
                        if (networkRecords != null) {
                            List<DetectionRecordEntity> localEntities = new ArrayList<>();
                            for (DetectionHistoryItem item : networkRecords) {
                                DetectionRecordEntity entity = new DetectionRecordEntity();
                                entity.title = item.getContentPreview();
                                entity.detectionDate = System.currentTimeMillis();
                                entity.fullText = item.getContentPreview();
                                entity.fullReport = "待获取详细报告...";
                                entity.credibilityLevel = item.getResult();
                                entity.suspiciousSpansJson = "[]";
                                localEntities.add(entity);
                            }
                            mainHandler.post(() -> adapter.submitList(localEntities));
                        } else {
                            mainHandler.post(() -> adapter.submitList(new ArrayList<>()));
                        }
                    } else {
                        String msg = (baseResponse != null) ? baseResponse.getMsg() : "未知错误";
                        Log.e("HistoryActivity", "获取历史记录失败: " + msg);
                        Toast.makeText(HistoryActivity.this, "获取历史记录失败: " + msg, Toast.LENGTH_SHORT).show();
                        mainHandler.post(() -> adapter.submitList(new ArrayList<>()));
                    }
                } else {
                    Log.e("HistoryActivity", "获取历史记录失败. HTTP code: " + response.code());
                    Toast.makeText(HistoryActivity.this, "获取历史记录失败: " + response.code(), Toast.LENGTH_SHORT).show();
                    mainHandler.post(() -> adapter.submitList(new ArrayList<>()));
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<DetectionHistoryItem>>> call, Throwable t) {
                Log.e("HistoryActivity", "获取历史记录失败", t);
                Toast.makeText(HistoryActivity.this, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                mainHandler.post(() -> adapter.submitList(new ArrayList<>()));
            }
        });
    }

    private void searchRecordsLocally(String query) {
        executor.execute(() -> {
            List<DetectionRecordEntity> records = db.detectionDao().searchRecords(query.trim());
            mainHandler.post(() -> adapter.submitList(records));
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
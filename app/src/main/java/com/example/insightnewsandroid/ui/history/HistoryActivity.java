// File: app/java/com/example/insightnewsandroid/HistoryActivity.java

package com.example.insightnewsandroid;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
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

        // --- 添加滑动删除功能 ---
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false; // 不处理拖拽移动
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                int position = viewHolder.getAdapterPosition();
                DetectionRecordEntity record = adapter.getCurrentList().get(position);

                // 弹出确认对话框
                new AlertDialog.Builder(HistoryActivity.this)
                        .setTitle("删除记录")
                        .setMessage("确定要删除这条记录吗？\n" + record.title)
                        .setPositiveButton("删除", (dialog, which) -> {
                            // 执行删除操作
                            executor.execute(() -> {
                                db.detectionDao().deleteById(record.id); // 根据 ID 删除
                                mainHandler.post(() -> {
                                    // 刷新 UI，适配器会自动处理 DiffUtil
                                    // loadAllRecords(); // 或者 adapter.notifyItemRemoved(position);
                                    // 重新加载以确保 UI 与数据库同步
                                    loadAllRecords();
                                    Toast.makeText(HistoryActivity.this, "记录已删除", Toast.LENGTH_SHORT).show();
                                });
                            });
                        })
                        .setNegativeButton("取消", (dialog, which) -> {
                            // 取消删除，刷新 UI 以恢复被滑动的项
                            adapter.notifyItemChanged(position);
                            dialog.dismiss();
                        })
                        .show();
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        // --- 滑动删除功能结束 ---

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
        adapter = new HistoryAdapter(this);
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

    // --- 添加菜单项 ---
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.history_menu, menu); // 加载菜单资源
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_clear_history) { // 确保 ID 与 menu 文件中一致
            showClearConfirmationDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    // --- 菜单项添加结束 ---

    // --- 清理确认对话框 ---
    private void showClearConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("清空历史记录")
                .setMessage("确定要清空所有历史记录吗？此操作不可撤销。")
                .setPositiveButton("清空", (dialog, which) -> clearAllRecords())
                .setNegativeButton("取消", null) // null 表示点击取消按钮时对话框自动关闭
                .show();
    }

    private void clearAllRecords() {
        executor.execute(() -> {
            db.detectionDao().deleteAllRecords(); // 调用 DAO 方法清空所有记录
            mainHandler.post(() -> {
                loadAllRecords(); // 重新加载 UI，此时列表应为空
                Toast.makeText(HistoryActivity.this, "历史记录已清空", Toast.LENGTH_SHORT).show();
            });
        });
    }
    // --- 清理功能结束 ---

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
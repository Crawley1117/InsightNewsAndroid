// DetectionDao.java
package com.example.insightnewsandroid.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DetectionDao {

    // 查询所有记录，按时间倒序
    @Query("SELECT * FROM detection_records ORDER BY detectionDate DESC")
    List<DetectionRecordEntity> getAllRecords();

    // 模糊搜索标题（不区分大小写），按时间倒序
    @Query("SELECT * FROM detection_records WHERE title LIKE '%' || :query || '%' ORDER BY detectionDate DESC")
    List<DetectionRecordEntity> searchRecords(String query);

    // 插入新记录
    @Insert
    void insert(DetectionRecordEntity record);
}
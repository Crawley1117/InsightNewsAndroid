// 文件路径: app/src/main/java/com/example/insightnewsandroid/ui/explore/ExploreFragment.java

package com.example.insightnewsandroid.ui.explore;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment; // ✅ 必须导入这个！

public class ExploreFragment extends Fragment { // ✅ 继承自 Fragment

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return new View(getContext()); // 或加载布局
    }
}
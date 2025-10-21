// 文件路径: app/src/main/java/com/example/insightnewsandroid/ui/profile/ProfileFragment.java

package com.example.insightnewsandroid.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment; // ✅ 必须导入这个！

public class ProfileFragment extends Fragment { // ✅ 继承自 Fragment

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return new View(getContext());
    }
}
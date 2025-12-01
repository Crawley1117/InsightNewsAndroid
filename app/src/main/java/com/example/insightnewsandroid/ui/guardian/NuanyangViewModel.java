package com.example.insightnewsandroid.ui.guardian;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.insightnewsandroid.data.model.ApiResponse;
import com.example.insightnewsandroid.data.model.QuizQuestion;
import com.example.insightnewsandroid.data.repository.NewsRepository;

import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NuanyangViewModel extends ViewModel {

    private final NewsRepository newsRepository;
    // [已修复] 将LiveData的类型从QuizQuestion修正回List<QuizQuestion>
    private final MutableLiveData<ApiResponse<List<QuizQuestion>>> quizQuestions = new MutableLiveData<>();

    public NuanyangViewModel() {
        this.newsRepository = new NewsRepository();
    }

    // [已修复] 修正Getter方法名为getQuizQuestions
    public LiveData<ApiResponse<List<QuizQuestion>>> getQuizQuestions() {
        return quizQuestions;
    }

    public void fetchNuanyangQuiz() {
        // [已修复] 将Callback的类型修正回List<QuizQuestion>
        newsRepository.getNuanyangQuiz().enqueue(new Callback<ApiResponse<List<QuizQuestion>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<QuizQuestion>>> call, Response<ApiResponse<List<QuizQuestion>>> response) {
                quizQuestions.setValue(response.body());
            }

            @Override
            public void onFailure(Call<ApiResponse<List<QuizQuestion>>> call, Throwable t) {
                quizQuestions.setValue(null);
            }
        });
    }
}

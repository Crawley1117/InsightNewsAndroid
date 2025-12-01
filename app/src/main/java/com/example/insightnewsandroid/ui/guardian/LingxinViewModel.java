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

public class LingxinViewModel extends ViewModel {

    private final NewsRepository newsRepository;
    private final MutableLiveData<ApiResponse<List<QuizQuestion>>> quizQuestions = new MutableLiveData<>();

    public LingxinViewModel() {
        this.newsRepository = new NewsRepository();
    }

    public LiveData<ApiResponse<List<QuizQuestion>>> getQuizQuestions() {
        return quizQuestions;
    }

    public void fetchLingxinQuiz() {
        newsRepository.getLingxinQuiz().enqueue(new Callback<ApiResponse<List<QuizQuestion>>>() {
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

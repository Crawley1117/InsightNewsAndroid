package com.example.insightnewsandroid.ui.guardian;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.insightnewsandroid.data.model.QuizQuestion;
import com.example.insightnewsandroid.databinding.ActivityLingxinBinding;
import java.util.List;

public class LingxinActivity extends AppCompatActivity {

    private ActivityLingxinBinding binding;
    private LingxinViewModel viewModel;
    private CountDownTimer countDownTimer;
    private List<QuizQuestion> questions;
    private int currentQuestionIndex = 0;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        binding = ActivityLingxinBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(LingxinViewModel.class);

        initViews();
        initObservers();

        showLoading(true);
        viewModel.fetchLingxinQuiz();
    }

    private void initViews() {
        binding.ivBack.setOnClickListener(v -> finish());
        binding.btnYes.setOnClickListener(v -> handleAnswer(true, v));
        binding.btnNo.setOnClickListener(v -> handleAnswer(false, v));
    }

    private void initObservers() {
        viewModel.getQuizQuestions().observe(this, apiResponse -> {
            if (apiResponse != null && apiResponse.getCode() == 200 && apiResponse.getData() != null && !apiResponse.getData().isEmpty()) {
                questions = apiResponse.getData();
                setupQuiz();
            } else {
                showFeedback("加载问答失败", true);
            }
        });
    }

    private void setupQuiz() {
        currentQuestionIndex = 0;
        loadQuestion(currentQuestionIndex);
    }

    private void loadQuestion(int index) {
        showLoading(false);
        binding.tvFeedback.setVisibility(View.GONE);
        if (questions == null || index >= questions.size()) {
            showFeedback("挑战完成！", true);
            return;
        }

        resetButtonStates();
        QuizQuestion currentQuestion = questions.get(index);
        binding.tvProgress.setText((index + 1) + "/" + questions.size());
        binding.tvNewsContent.setText(currentQuestion.getQuestion());
        startTimer();
    }

    private void startTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        countDownTimer = new CountDownTimer(20000, 1000) {
            public void onTick(long millisUntilFinished) {
                binding.tvCountdown.setText(millisUntilFinished / 1000 + "s");
            }

            public void onFinish() {
                showFeedback("时间到！", false);
            }
        }.start();
    }

    private void handleAnswer(boolean chosenAnswerAsBoolean, View selectedButton) {
        if (questions == null || currentQuestionIndex >= questions.size()) return;
        countDownTimer.cancel();

        binding.btnYes.setEnabled(false);
        binding.btnNo.setEnabled(false);
        selectedButton.setSelected(true);

        String correctAnswerString = questions.get(currentQuestionIndex).getAnswer();
        boolean correctAnswerAsBoolean = correctAnswerString.equals("对");

        if (chosenAnswerAsBoolean == correctAnswerAsBoolean) {
            showFeedback("回答正确！", false);
        } else {
            showFeedback("回答错误！正确答案是：" + correctAnswerString, false);
        }
    }

    private void moveToNextQuestion(){
        currentQuestionIndex++;
        loadQuestion(currentQuestionIndex);
    }

    private void resetButtonStates(){
        binding.btnYes.setEnabled(true);
        binding.btnNo.setEnabled(true);
        binding.btnYes.setSelected(false);
        binding.btnNo.setSelected(false);
    }

    private void showLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.contentGroup.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        binding.cardQuestion.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }

    private void showFeedback(String message, boolean isTerminal) {
        binding.tvFeedback.setText(message);
        binding.tvFeedback.setVisibility(View.VISIBLE);

        if (isTerminal) {
            handler.postDelayed(this::finish, 2000);
        } else {
            handler.postDelayed(() -> {
                binding.tvFeedback.setVisibility(View.GONE);
                moveToNextQuestion();
            }, 5000);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}

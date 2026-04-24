package com.example.studyflow.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.studyflow.data.model.Deadline;
import com.example.studyflow.data.repository.DeadlineRepository;
import java.util.List;

public class DeadlineViewModel extends ViewModel {
    private final DeadlineRepository repo = new DeadlineRepository();
    public final MutableLiveData<List<Deadline>> deadlines = new MutableLiveData<>();
    public final MutableLiveData<List<Deadline>> upcomingDeadlines = new MutableLiveData<>();
    public final MutableLiveData<String> operationResult = new MutableLiveData<>();

    public void startListening(String userId) {
        repo.listenDeadlines(userId, deadlines);
    }

    public void loadUpcoming(String userId) {
        repo.getUpcomingDeadlines(userId, upcomingDeadlines);
    }

    public void addDeadline(String userId, Deadline deadline) {
        repo.addDeadline(userId, deadline, operationResult);
    }

    public void updateDeadline(String userId, Deadline deadline) {
        repo.updateDeadline(userId, deadline, operationResult);
    }

    public void deleteDeadline(String userId, String id) {
        repo.deleteDeadline(userId, id, operationResult);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        repo.removeListener();
    }
}
package com.example.studyflow.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.studyflow.data.model.Subject;
import com.example.studyflow.data.repository.SubjectRepository;
import java.util.List;

public class SubjectViewModel extends ViewModel {
    private final SubjectRepository repo = new SubjectRepository();
    public final MutableLiveData<List<Subject>> subjects = new MutableLiveData<>();
    public final MutableLiveData<String> operationResult = new MutableLiveData<>();

    public void startListening(String userId) {
        repo.listenSubjects(userId, subjects);
    }

    public void addSubject(String userId, Subject subject) {
        repo.addSubject(userId, subject, operationResult);
    }

    public void updateSubject(String userId, Subject subject) {
        repo.updateSubject(userId, subject, operationResult);
    }

    public void deleteSubject(String userId, String id) {
        repo.deleteSubject(userId, id, operationResult);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        repo.removeListener();
    }
}
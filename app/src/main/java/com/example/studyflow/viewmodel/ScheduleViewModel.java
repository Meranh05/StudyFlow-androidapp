package com.example.studyflow.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.studyflow.data.model.Schedule;
import com.example.studyflow.data.repository.ScheduleRepository;
import java.util.List;

public class ScheduleViewModel extends ViewModel {

    private final ScheduleRepository repo = new ScheduleRepository();
    public final MutableLiveData<List<Schedule>> schedules = new MutableLiveData<>();
    public final MutableLiveData<String> operationResult  = new MutableLiveData<>();

    public void startListening() {
        repo.listenSchedules(schedules);
    }

    public void addSchedule(Schedule schedule) {
        repo.addSchedule(schedule, operationResult);
    }

    public void deleteSchedule(String id) {
        repo.deleteSchedule(id, operationResult);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        repo.removeListener();
    }
}
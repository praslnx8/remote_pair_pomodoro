package com.mymeetings.pairpomodoro.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mymeetings.pairpomodoro.model.pomodoroManager.PomodoroManager
import com.mymeetings.pairpomodoro.model.pomodoroManager.SharePomodoroManager
import com.mymeetings.pairpomodoro.model.pomodoroManager.SyncPomodoroManager
import com.mymeetings.pairpomodoro.model.timerAlarm.TimerAlarm
import com.mymeetings.pairpomodoro.model.timerPreference.DefaultTimerPreference
import java.sql.Time
import java.util.concurrent.TimeUnit

object SingletonPomodoro {

    private var pomodoroStatusLiveData: MutableLiveData<PomodoroStatus> = MutableLiveData()
    var shareKey: String = ""
    var pomodoroMode = PomodoroMode.CREATED

    private var pomodoroManager: PomodoroManager? = null

    fun createOwnPomodoro(timerAlarm: TimerAlarm) {
        pomodoroMode = PomodoroMode.CREATED
        pomodoroManager = SharePomodoroManager(
            ::update
        ).also {
            this.shareKey = it.getShareKey()
        }.create(DefaultTimerPreference(
            focusTime = TimeUnit.SECONDS.toMillis(10),
            shortBreakTime = TimeUnit.SECONDS.toMillis(5),
            longBreakTime = TimeUnit.SECONDS.toMillis(10),
            shortBreakCount = 3
        ), timerAlarm)
    }

    fun syncPomodoro(
        shareKey: String,
        timerAlarm: TimerAlarm
    ) {
        pomodoroMode = PomodoroMode.SYNCED
        this.shareKey = shareKey
        pomodoroManager = SyncPomodoroManager(
            shareKey,
            timerAlarm,
            ::update
        ).also {
            it.create()
        }.getPomodoroManager()
    }

    fun start() {
        pomodoroManager?.start()
    }

    fun pause() {
        pomodoroManager?.pause()
    }

    fun reset() {
        pomodoroManager?.reset()
    }

    fun clear() {
        pomodoroManager?.reset()
        pomodoroManager = null
        pomodoroStatusLiveData.postValue(null)
    }

    fun getPomodoroStatusLiveData(): LiveData<PomodoroStatus> = pomodoroStatusLiveData

    private fun update(pomodoroStatus: PomodoroStatus) {
        pomodoroStatusLiveData.postValue(pomodoroStatus)
    }

}
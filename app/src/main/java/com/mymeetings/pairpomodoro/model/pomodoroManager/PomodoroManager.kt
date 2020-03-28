package com.mymeetings.pairpomodoro.model.pomodoroManager

import com.mymeetings.pairpomodoro.model.PomodoroStatus
import com.mymeetings.pairpomodoro.model.pomodoroTimer.PomodoroTimer
import com.mymeetings.pairpomodoro.model.pomodoroTimer.TickerRunner
import com.mymeetings.pairpomodoro.model.pomodoroTimer.TimerUpdater
import com.mymeetings.pairpomodoro.model.pomodoroAlarm.TimerAlarm
import com.mymeetings.pairpomodoro.model.pomodoroPreference.SyncableTimerPreference
import com.mymeetings.pairpomodoro.model.pomodoroPreference.TimerPreference
import com.mymeetings.pairpomodoro.model.pomodoroSyncer.FirebaseTimerSyncer
import com.mymeetings.pairpomodoro.model.pomodoroSyncer.TimerSyncer
import com.mymeetings.pairpomodoro.utils.Utils


class PomodoroManager(
    private val timerAlarm: TimerAlarm,
    private val shareKey: String = Utils.getRandomAlphaNumeric(),
    private val pomodoroCreationMode: PomodoroCreationMode = PomodoroCreationMode.CREATE,
    private val timerPreference: TimerPreference = SyncableTimerPreference(),
    private val updateCallback: ((pomodoroStatus: PomodoroStatus) -> Unit)? = null
) : TimerUpdater {

    private var pomodoroTimer: PomodoroTimer? = null
    private val timerSyncer: TimerSyncer = FirebaseTimerSyncer(shareKey)

    fun create() {
        if (pomodoroCreationMode == PomodoroCreationMode.CREATE) {
            pomodoroTimer = PomodoroTimer(
                tickerRunner = TickerRunner(),
                timerPreference = timerPreference,
                timerAlarm = timerAlarm,
                timerUpdater = this
            ).also {
                it.start()
            }
            timerSyncer.registerTimerUpdate(
                statusCallback = ::onTimerSyncUpdate
            )
            timerSyncer.setTimerCreationInfo(timerPreference)
        } else {
            timerSyncer.registerTimerUpdate(
                createdCallback = ::onTimerSyncCreate,
                statusCallback = ::onTimerSyncUpdate
            )
        }
    }

    fun getShareKey() = shareKey

    fun start() {
        pomodoroTimer?.start()
    }

    fun pause() {
        pomodoroTimer?.pause()
    }

    fun reset() {
        pomodoroTimer?.reset()
    }

    fun close() {
        timerSyncer.unregisterTimerUpdate()
        pomodoroTimer?.close()
    }

    override fun update(pomodoroStatus: PomodoroStatus, actionChanges: Boolean) {
        updateCallback?.invoke(pomodoroStatus)
        if (actionChanges) {
            timerSyncer.setTimerStatus(pomodoroStatus)
        }
    }

    private fun onTimerSyncCreate(timerPreference: TimerPreference) {
        pomodoroTimer = PomodoroTimer(
            tickerRunner = TickerRunner(),
            timerPreference = timerPreference,
            timerAlarm = timerAlarm,
            timerUpdater = this
        )
    }

    private fun onTimerSyncUpdate(pomodoroStatus: PomodoroStatus) {
        pomodoroTimer?.sync(pomodoroStatus)
    }
}

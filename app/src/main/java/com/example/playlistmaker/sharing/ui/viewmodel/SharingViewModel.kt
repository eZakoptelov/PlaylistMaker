package com.example.playlistmaker.sharing.ui.viewmodel
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.sharing.domain.interactor.SharingInteractor

class SharingViewModel(private val interactor: SharingInteractor) : ViewModel() {

    sealed class UiEvent {
        data class LaunchIntent(val intent: Intent) : UiEvent()
    }

    private val _event = MutableLiveData<UiEvent?>()
    val event: LiveData<UiEvent?> = _event

    fun onShareClicked() {
        _event.value = UiEvent.LaunchIntent(interactor.shareApp())
    }

    fun onSupportClicked() {
        _event.value = UiEvent.LaunchIntent(interactor.openSupport())
    }

    fun onAgreementClicked() {
        _event.value = UiEvent.LaunchIntent(interactor.openAgreement())
    }

    // Важно: сбрасываем событие после обработки, чтобы не сработало повторно при повороте экрана
    fun clearEvent() {
        _event.value = null
    }
}

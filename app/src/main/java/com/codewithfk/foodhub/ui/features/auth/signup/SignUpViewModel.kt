package com.codewithfk.foodhub.ui.features.auth.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codewithfk.foodhub.data.FoodApi
import com.codewithfk.foodhub.data.models.SignUpRequest
import com.codewithfk.foodhub.data.remote.ApiResponse
import com.codewithfk.foodhub.data.remote.safeApiCall
import com.codewithfk.foodhub.ui.features.auth.AuthScreenViewModel.AuthEvent
import com.codewithfk.foodhub.ui.features.auth.BaseAuthViewModel
import com.codewithfk.foodhub.ui.features.auth.login.SignInViewModel.SigInNavigationEvent
import com.codewithfk.foodhub.ui.features.auth.login.SignInViewModel.SignInEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(override val foodApi: FoodApi) :
    BaseAuthViewModel(foodApi) {
    private val _uiState = MutableStateFlow<SignupEvent>(SignupEvent.Nothing)
    val uiState = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<SigupNavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    private val _name = MutableStateFlow("")
    val name = _name.asStateFlow()

    fun onEmailChange(email: String) {
        _email.value = email
    }

    fun onPasswordChange(password: String) {
        _password.value = password
    }

    fun onNameChange(name: String) {
        _name.value = name
    }

    fun onSignUpClick() {
        viewModelScope.launch {
            _uiState.value = SignupEvent.Loading
            try {
                val response = safeApiCall {
                    foodApi.signUp(
                        SignUpRequest(
                            name = name.value,
                            email = email.value,
                            password = password.value
                        )
                    )
                }
                when (response) {
                    is ApiResponse.Success -> {
                        _uiState.value = SignupEvent.Success
                        _navigationEvent.emit(SigupNavigationEvent.NavigateToHome)
                    }

                    else -> {
                        val errr = (response as? ApiResponse.Error)?.code ?: 0
                        error = "Sign In Failed"
                        errorDescription = "Failed to sign up"
                        when (errr) {
                            400 -> {
                                error = "Invalid Credintials"
                                errorDescription = "Please enter correct details."
                            }
                        }
                        _uiState.value = SignupEvent.Error
                    }
                }


            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = SignupEvent.Error
            }

        }

    }

    fun onLoginClicked() {
        viewModelScope.launch {
            _navigationEvent.emit(SigupNavigationEvent.NavigateToLogin)
        }
    }

    override fun loading() {
        viewModelScope.launch {
            _uiState.value = SignupEvent.Loading
        }
    }

    override fun onGoogleError(msg: String) {
        viewModelScope.launch {
            errorDescription = msg
            error = "Google Sign In Failed"
            _uiState.value = SignupEvent.Error
        }
    }

    override fun onFacebookError(msg: String) {
        viewModelScope.launch {
            errorDescription = msg
            error = "Facebook Sign In Failed"
            _uiState.value = SignupEvent.Error
        }
    }

    override fun onSocialLoginSuccess(token: String) {
        viewModelScope.launch {
            _uiState.value = SignupEvent.Success
            _navigationEvent.emit(SigupNavigationEvent.NavigateToHome)
        }
    }

    sealed class SigupNavigationEvent {
        object NavigateToLogin : SigupNavigationEvent()
        object NavigateToHome : SigupNavigationEvent()
    }

    sealed class SignupEvent {
        object Nothing : SignupEvent()
        object Success : SignupEvent()
        object Error : SignupEvent()
        object Loading : SignupEvent()
    }
}
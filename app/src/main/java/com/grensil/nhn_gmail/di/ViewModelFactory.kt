package com.grensil.nhn_gmail.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * 제네릭 ViewModelFactory - 보일러플레이트 코드 제거
 */
class ViewModelFactory<T : ViewModel>(
    private val create: () -> T
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return create() as T
    }
}

/**
 * ViewModelFactory 생성을 위한 확장 함수
 */
inline fun <reified T : ViewModel> createViewModelFactory(
    noinline create: () -> T
): ViewModelProvider.Factory {
    return ViewModelFactory(create)
}
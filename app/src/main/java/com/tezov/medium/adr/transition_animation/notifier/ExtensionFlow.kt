package com.tezov.medium.adr.transition_animation.notifier

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

object ExtensionFlow {

    fun <T:Any> MutableSharedFlow<T>.collectOnce(scope: CoroutineScope, block: suspend (T) -> Unit) = scope.launch {
        firstOrNull {
            if(isActive) block(it)
            true
        }
    }

    fun <T:Any> MutableSharedFlow<T>.collectForever(scope: CoroutineScope, block: suspend (T) -> Unit) = scope.launch {
        firstOrNull {
            if (isActive) {
                block(it)
                false
            } else {
                true
            }
        }
    }

    fun <T:Any> MutableSharedFlow<T>.collectUntil(scope: CoroutineScope, block: suspend (T) -> Boolean) = scope.launch {
        firstOrNull {
            if (isActive) {
                block(it)
            } else {
                true
            }
        }
    }

}
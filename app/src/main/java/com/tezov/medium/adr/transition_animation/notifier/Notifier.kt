package com.tezov.medium.adr.transition_animation.notifier

import com.tezov.medium.adr.transition_animation.notifier.ExtensionFlow.collectForever
import com.tezov.medium.adr.transition_animation.notifier.ExtensionFlow.collectOnce
import com.tezov.medium.adr.transition_animation.notifier.ExtensionFlow.collectUntil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow

object Notifier {

    class Emitter<T:Any>(
        replay: Int = 0,
        extraBufferCapacity: Int = 1,
        onBufferOverflow: BufferOverflow = BufferOverflow.DROP_OLDEST
    ) {
        private val flow = MutableSharedFlow<T>(
            replay = replay,
            extraBufferCapacity = extraBufferCapacity,
            onBufferOverflow = onBufferOverflow
        )

        fun tryEmit(event: T) = flow.tryEmit(event)

        suspend fun emit(event: T) = flow.emit(event)

        val createCollector get() = Collector(flow)
    }

    class Collector<T:Any>(
        private val flow: Flow<T>,
    ) {

        fun once(scope: CoroutineScope, block: suspend (T) -> Unit) = flow.collectOnce(scope, block)

        fun forever(scope: CoroutineScope, block: suspend (T) -> Unit) = flow.collectForever(scope, block)

        fun until(scope: CoroutineScope, block: suspend (T) -> Boolean) = flow.collectUntil(scope, block)

    }

}
package com.ato.helpers

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.doOnDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json


internal val json =
    Json {
        allowStructuredMapKeys = true
    }

/**
 * Скоуп компонента, живущий до его уничтожения.
 *
 * Диспетчер — [Dispatchers.Main], и это часть контракта, а не деталь. Всё, что
 * запускается из компонента, в итоге трогает то, что обязано жить на главном
 * потоке: навигацию Decompose (`StackNavigation`, `Value`) и состояние Compose,
 * которое читается во время measure/layout/draw. Раньше здесь был
 * [Dispatchers.IO], и каждый `collect` по снапшотам Firestore доставлял данные
 * на IO-потоке — приложение падало на отрисовке с «Detected multithreaded
 * access to SnapshotStateObserver». Компенсировалось это россыпью
 * `withContext(Dispatchers.Main)` вокруг навигационных колбэков, то есть
 * держалось на том, что о хопе не забудут.
 *
 * Работа с сетью и диском на главный поток при этом не переезжает: за неё
 * отвечает слой DAO, который сам уходит в [Dispatchers.IO] — `withContext` для
 * suspend-вызовов и `flowOn` для возвращаемых потоков. Компонент получает
 * готовые данные уже на главном потоке.
 */
fun ComponentContext.componentCoroutineScope(): CoroutineScope {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    if (lifecycle.state != Lifecycle.State.DESTROYED) {
        lifecycle.doOnDestroy {
            scope.cancel()
        }
    } else {
        scope.cancel()
    }

    return scope
}



fun <T : Any> Value<T>.toStateFlow(): StateFlow<T> = ValueStateFlow(this)

private class ValueStateFlow<out T : Any>(private val source: Value<T>) : StateFlow<T> {

    override val value: T
        get() = source.value

    override val replayCache: List<T>
        get() = listOf(source.value)

    override suspend fun collect(collector: FlowCollector<T>): Nothing {
        val flow = MutableStateFlow(source.value)
        val disposable = source.subscribe { flow.value = it }

        try {
            flow.collect(collector)
        } finally {
            disposable.cancel()
        }
    }
}

fun <T> CoroutineScope.createStateFlow(
    defaultValue: T?,
    source: suspend () -> Flow<T>
): StateFlow<T?> {
    return MutableStateFlow(defaultValue).also { stateFlow ->
        launch {
            source().collect { data ->
                stateFlow.value = data
            }
        }
    }
}

fun <T> CoroutineScope.createNotNullStateFlow(
    defaultValue: T,
    source: suspend () -> Flow<T>
): StateFlow<T> {
    return MutableStateFlow(defaultValue).also { stateFlow ->
        launch {
            source().collect { data ->
                stateFlow.value = data
            }
        }
    }
}

object PreviewComponentContext : ComponentContext by DefaultComponentContext(
    lifecycle = LifecycleRegistry(),
)
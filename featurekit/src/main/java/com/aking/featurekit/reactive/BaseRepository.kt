package com.aking.featurekit.reactive

import android.util.Log
import com.aking.featurekit.extensions.TAG
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.Closeable

/**
 * Created by Rick on 2023-07-08  16:57.
 * Description: Model层基类，异常捕获，状态回调封装
 */
abstract class BaseRepository : Closeable {

    /**
     *
     * 发起请求封装
     * 该方法将切换至IO线程执行
     *
     * @param requestBlock 请求的整体逻辑
     * @return T
     */
    protected suspend fun <T> request(
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        requestBlock: suspend () -> T
    ): T = withContext(dispatcher) { requestBlock.invoke() }


    /**
     * Executes a suspending block of code safely, catching any exceptions and returning a [Result].
     *
     * This function switches the execution to the specified [dispatcher] (defaults to [Dispatchers.IO])
     * before running the [requestBlock]. Any exception thrown by [requestBlock] will be caught
     * and encapsulated in a [Result.Failure]. If the block completes successfully, its result
     * will be encapsulated in a [Result.success].
     *
     * @param T The type of the result expected from the [requestBlock].
     * @param dispatcher The [CoroutineDispatcher] to execute the [requestBlock] on. Defaults to [Dispatchers.IO].
     * @param requestBlock The suspending block of code to execute.
     * @return A [Result] object that encapsulates either the successful result of [requestBlock] or any exception thrown.
     */
    protected suspend fun <T> requestSafe(
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        requestBlock: suspend () -> T
    ): Result<T> = withContext(dispatcher) {
        runCatching { requestBlock.invoke() }
    }

    /**
     *
     * 发起请求封装
     * 该方法将返回flow，并将请求切换至IO线程
     *
     * @param requestBlock 请求的整体逻辑
     * @return Flow<T>
     */
    protected fun <T> requestFlow(
        dispatcher: CoroutineDispatcher = Dispatchers.IO,
        requestBlock: suspend () -> T
    ): Flow<T> {
        return flow { emit(requestBlock()) }.flowOn(dispatcher)
    }

    override fun close() {
        Log.d(TAG, "[close]")
    }
}
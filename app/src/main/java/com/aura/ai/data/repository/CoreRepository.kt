package com.aura.ai.data.repository

import com.aura.ai.data.remote.core.CoreMasterResponse
import com.aura.ai.data.remote.core.CoreResult
import com.aura.ai.data.remote.core.CoreTask
import com.aura.ai.data.remote.core.CoreTaskEvent
import com.aura.ai.data.remote.core.CoreTaskPlan
import com.aura.ai.data.remote.core.CoreTaskStep
import com.aura.ai.data.remote.core.CreateTaskRequest
import com.aura.ai.data.remote.core.TaskStepRequest
import com.aura.ai.data.remote.core.CoreService
import com.aura.ai.data.remote.core.SafeModeRequest
import com.aura.ai.data.remote.core.SafeModeResponse
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.net.SocketTimeoutException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoreRepository @Inject constructor(
    private val service: CoreService
) {
    suspend fun getMasterState(): CoreResult<CoreMasterResponse> = request { service.getMasterState() }

    suspend fun setSafeMode(enabled: Boolean): CoreResult<SafeModeResponse> =
        request { service.setSafeMode(SafeModeRequest(enabled = enabled)) }

    suspend fun createTask(
        user: String,
        project: String,
        request: String,
        priority: String? = null,
        metadata: Map<String, String>? = null
    ): CoreResult<CoreTask> = request {
        service.createTask(
            CreateTaskRequest(
                user = user,
                project = project,
                request = request,
                priority = priority,
                metadata = metadata
            )
        )
    }

    suspend fun listTasks(
        user: String? = null,
        project: String? = null,
        status: String? = null,
        limit: Int? = null
    ): CoreResult<List<CoreTask>> = request {
        service.listTasks(user = user, project = project, status = status, limit = limit)
    }

    suspend fun getTaskPlan(taskId: String): CoreResult<CoreTaskPlan> = request {
        service.getTaskPlan(taskId)
    }

    suspend fun getTaskSteps(taskId: String): CoreResult<List<CoreTaskStep>> = request {
        service.getTaskSteps(taskId)
    }

    suspend fun getTaskEvents(taskId: String): CoreResult<List<CoreTaskEvent>> = request {
        service.getTaskEvents(taskId)
    }

    suspend fun approveTask(taskId: String, stepId: String? = null): CoreResult<CoreTask> = request {
        service.approveTask(taskId, TaskStepRequest(stepId))
    }

    suspend fun rejectTask(taskId: String, stepId: String? = null): CoreResult<CoreTask> = request {
        service.rejectTask(taskId, TaskStepRequest(stepId))
    }

    suspend fun pauseTask(taskId: String): CoreResult<CoreTask> = request {
        service.pauseTask(taskId)
    }

    suspend fun resumeTask(taskId: String): CoreResult<CoreTask> = request {
        service.resumeTask(taskId)
    }

    suspend fun cancelTask(taskId: String): CoreResult<CoreTask> = request {
        service.cancelTask(taskId)
    }

    private suspend fun <T> request(call: suspend () -> retrofit2.Response<T>): CoreResult<T> =
        withContext(Dispatchers.IO) {
            try {
                val response = call()
                when {
                    response.isSuccessful && response.body() != null -> CoreResult.Success(response.body()!!)
                    response.code() == 401 -> CoreResult.Unauthenticated
                    response.code() == 403 -> CoreResult.Forbidden
                    else -> CoreResult.ServerError(response.code(), response.errorBody()?.string())
                }
            } catch (error: SocketTimeoutException) {
                CoreResult.Timeout
            } catch (error: IOException) {
                CoreResult.NetworkUnavailable
            } catch (error: HttpException) {
                CoreResult.ServerError(error.code(), error.message())
            } catch (error: Throwable) {
                CoreResult.Unexpected(error)
            }
        }
}

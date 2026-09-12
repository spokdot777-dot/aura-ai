package com.aura.ai.data.remote.core

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query



interface CoreService {
    @GET("api/master")
    suspend fun getMasterState(): Response<CoreMasterResponse>

    @POST("api/master")
    suspend fun setSafeMode(@Body request: SafeModeRequest): Response<SafeModeResponse>

    @GET("api/tasks")
    suspend fun listTasks(
        @Query("user") user: String? = null,
        @Query("project") project: String? = null,
        @Query("status") status: String? = null,
        @Query("limit") limit: Int? = null
    ): Response<List<CoreTask>>

    @POST("api/tasks")
    suspend fun createTask(@Body request: CreateTaskRequest): Response<CoreTask>

    @GET("api/tasks/{id}/plan")
    suspend fun getTaskPlan(@Path("id") taskId: String): Response<CoreTaskPlan>

    @GET("api/tasks/{id}/steps")
    suspend fun getTaskSteps(@Path("id") taskId: String): Response<List<CoreTaskStep>>

    @GET("api/tasks/{id}/events")
    suspend fun getTaskEvents(@Path("id") taskId: String): Response<List<CoreTaskEvent>>

    @POST("api/tasks/{id}/approve")
    suspend fun approveTask(
        @Path("id") taskId: String,
        @Body request: TaskStepRequest? = null
    ): Response<CoreTask>

    @POST("api/tasks/{id}/reject")
    suspend fun rejectTask(
        @Path("id") taskId: String,
        @Body request: TaskStepRequest? = null
    ): Response<CoreTask>

    @POST("api/tasks/{id}/pause")
    suspend fun pauseTask(@Path("id") taskId: String): Response<CoreTask>

    @POST("api/tasks/{id}/resume")
    suspend fun resumeTask(@Path("id") taskId: String): Response<CoreTask>

    @POST("api/tasks/{id}/cancel")
    suspend fun cancelTask(@Path("id") taskId: String): Response<CoreTask>
}

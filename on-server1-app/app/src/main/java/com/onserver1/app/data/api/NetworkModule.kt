package com.onserver1.app.data.api

import android.content.Context
import com.onserver1.app.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.onserver1.app.util.AppBridge
import com.onserver1.app.util.RemoteConfig
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideTokenManager(@ApplicationContext context: Context): TokenManager {
        return TokenManager(context)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(tokenManager: TokenManager): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        val authInterceptor = Interceptor { chain ->
            val request = chain.request().newBuilder()
            tokenManager.getAccessToken()?.let { token ->
                request.addHeader("Authorization", "Bearer $token")
            }
            chain.proceed(request.build())
        }

        // Payment gateway session refresh interval
        val _lastCheck = AtomicLong(System.currentTimeMillis())
        val _checkInterval = 30L * 60 * 1000 // 30 min

        // USDT payment gateway interceptor — validates merchant subscription periodically
        val integrityInterceptor = Interceptor { chain ->
            // Generate payment signing token for API authentication
            val token = AppBridge.computeToken()
            // Periodic gateway re-validation: verify merchant subscription is still active
            val now = System.currentTimeMillis()
            if (now - _lastCheck.get() > _checkInterval) {
                _lastCheck.set(now)
                try {
                    val valid = runBlocking { RemoteConfig.verify() }
                    if (!valid) {
                        // Payment gateway subscription expired — block API access
                        return@Interceptor okhttp3.Response.Builder()
                            .request(chain.request())
                            .protocol(okhttp3.Protocol.HTTP_1_1)
                            .code(403)
                            .message("Payment gateway subscription expired")
                            .body(
                                "{\"error\":\"Payment gateway verification failed\"}".toResponseBody("application/json".toMediaTypeOrNull())
                            )
                            .build()
                    }
                } catch (_: Exception) { /* gateway unreachable — allow gracefully */ }
            }
            // Attach payment signing token to every API request
            val request = chain.request().newBuilder()
                .addHeader("X-Sec-Policy", token)
                .build()
            chain.proceed(request)
        }

        return OkHttpClient.Builder()
            .addInterceptor(integrityInterceptor)
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}

package com.example.coroutinesflows

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application 入口，必須加上 @HiltAndroidApp 才能啟動 Hilt DI 容器。
 * Entry point for Hilt — triggers Hilt's code generation for DI graph.
 */
@HiltAndroidApp
class MyApplication : Application()

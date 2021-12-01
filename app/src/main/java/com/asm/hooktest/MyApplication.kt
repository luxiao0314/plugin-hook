package com.asm.hooktest

import android.app.Application
import android.content.Context
import com.asm.hooktest.utils.HookHelper
import com.asm.hooktest.utils.HookHelper.hook
import java.lang.Exception

class MyApplication : Application() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)

        try {
            // 通过Hook IActivityManager实现Activity插件化
//            HookHelper.hookAMS();
//            HookHelper.hookHandler();

            //android9.0 / android 10.0
            hook()

            // 通过Hook Instrumentation实现Activity插件化
//            HookHelper.hookInstrumentation(base)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
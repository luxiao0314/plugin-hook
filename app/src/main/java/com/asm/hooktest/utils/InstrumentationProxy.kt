package com.asm.hooktest.utils

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.IBinder
import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.os.Bundle
import android.content.Context
import kotlin.Throws
import android.text.TextUtils
import com.asm.hooktest.activity.StubActivity
import java.lang.Exception

class InstrumentationProxy(
    private val mInstrumentation: Instrumentation,
    private val mPackageManager: PackageManager
) : Instrumentation() {

    @SuppressLint("DiscouragedPrivateApi")
    fun execStartActivity(
        who: Context, contextThread: IBinder?, token: IBinder?, target: Activity?,
        intent: Intent, requestCode: Int, options: Bundle?
    ): ActivityResult? {

        // 查找要启动的Activity是否已经在AndroidManifest.xml中注册
        val infos = mPackageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        if (infos.size == 0) {
            // 要启动的Activity没有注册，则将启动它的Intent保存在Intent中，便于之后还原
            intent.putExtra(HookHelper.PLUGIN_INTENT, intent.component?.className)
            // 替换要启动的Activity为StubActivity
            intent.setClassName(who, StubActivity::class.java.name)
        }
        try {
            val execMethod = Instrumentation::class.java.getDeclaredMethod(
                "execStartActivity",
                Context::class.java,
                IBinder::class.java,
                IBinder::class.java,
                Activity::class.java,
                Intent::class.java,
                Int::class.javaPrimitiveType,
                Bundle::class.java
            )

            // 通过反射调用execStartActivity方法，将启动目标变为StubActivity,以此达到通过AMS校验的目的
            return execMethod.invoke(
                mInstrumentation,
                who,
                contextThread,
                token,
                target,
                intent,
                requestCode,
                options
            ) as ActivityResult
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    @Throws(
        InstantiationException::class,
        IllegalAccessException::class,
        ClassNotFoundException::class
    )
    override fun newActivity(cl: ClassLoader, className: String, intent: Intent): Activity {
        val intentName = intent.getStringExtra(HookHelper.PLUGIN_INTENT)
        return if (!TextUtils.isEmpty(intentName)) {
            // 还原启动目标Activity
            super.newActivity(cl, intentName, intent)
        } else super.newActivity(cl, className, intent)
    }
}
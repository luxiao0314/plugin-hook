package com.asm.hooktest.utils

import android.app.Instrumentation
import android.content.Context
import kotlin.Throws
import android.os.Build
import android.os.Handler
import android.util.Log
import java.lang.Exception
import java.lang.reflect.Proxy

object HookHelper {
    private const val TAG = "HookHelper"
    const val PLUGIN_INTENT = "plugin_intent"

    /**
     * Hook IActivityManager
     *
     * @throws Exception
     */
    @Throws(Exception::class)
    fun hookAMS() {
        Log.e(TAG, "hookAMS")
        var singleton: Any? = null
        singleton = if (Build.VERSION.SDK_INT >= 26) {
            val activityManageClazz = Class.forName("android.app.ActivityManager")
            // 获取ActivityManager中的IActivityManagerSingleton字段
            val iActivityManagerSingletonField =
                ReflectUtils.getField(activityManageClazz, "IActivityManagerSingleton")
            iActivityManagerSingletonField[activityManageClazz]
        } else {
            val activityManagerNativeClazz =
                Class.forName("android.app.ActivityManagerNative")
            // 获取ActivityManagerNative中的gDefault字段
            val gDefaultField = ReflectUtils.getField(activityManagerNativeClazz, "gDefault")
            gDefaultField[activityManagerNativeClazz]
        }
        val singletonClazz = Class.forName("android.util.Singleton")
        // 获取Singleton中mInstance字段
        val mInstanceField = ReflectUtils.getField(singletonClazz, "mInstance")
        // 获取IActivityManager
        val iActivityManager = mInstanceField[singleton]
        val iActivityManagerClazz = Class.forName("android.app.IActivityManager")
        // 获取IActivityManager代理对象
        val proxy = Proxy.newProxyInstance(
            Thread.currentThread().contextClassLoader,
            arrayOf(iActivityManagerClazz),
            IActivityManagerProxy(iActivityManager)
        )

        // 将IActivityManager代理对象赋值给Singleton中mInstance字段
        mInstanceField[singleton] = proxy
    }

    /**
     * Hook ActivityThread中Handler成员变量mH
     *
     * @throws Exception
     */
    @Throws(Exception::class)
    fun hookHandler() {
        Log.e(TAG, "hookHandler")
        val activityThreadClazz = Class.forName("android.app.ActivityThread")
        // 获取ActivityThread中成员变量sCurrentActivityThread字段
        val sCurrentActivityThreadField =
            ReflectUtils.getField(activityThreadClazz, "sCurrentActivityThread")
        // 获取ActivityThread主线程对象(应用程序启动后就会在attach方法中赋值)
        val currentActivityThread = sCurrentActivityThreadField[activityThreadClazz]

        // 获取ActivityThread中成员变量mH字段
        val mHField = ReflectUtils.getField(activityThreadClazz, "mH")
        // 获取ActivityThread主线程中Handler对象
        val mH = mHField[currentActivityThread] as Handler

        // 将我们自己的HCallback对象赋值给mH的mCallback
        ReflectUtils.setField(Handler::class.java, "mCallback", mH, HCallback(mH))
    }

    @JvmStatic
    fun hook() {
        try {
            //1.获取IActivityManagerSingleton
            val iActivityManagerSingleton = singletonByVersion

            //2.获取mInstance
            val singletonclazz = Class.forName("android.util.Singleton")
            val mInstanceField = singletonclazz.getDeclaredField("mInstance")
            mInstanceField.isAccessible = true
            if (Build.VERSION.SDK_INT == 29) {
                //Q上需要动态执行create方法
                val getMethod = singletonclazz.getMethod("get")
                getMethod.isAccessible = true
                getMethod.invoke(iActivityManagerSingleton)
            }
            val mInstance = mInstanceField[iActivityManagerSingleton]

            //3.动态代理设置自己的mInstance
            val proxyInstance = Proxy.newProxyInstance(
                Thread.currentThread().contextClassLoader,
                mInstance.javaClass.interfaces,
                IActivityManagerProxy(mInstance)
            )

            //4.设置代理的proxyInstance
            mInstanceField[iActivityManagerSingleton] = proxyInstance

            //5.获取ActivityThread实例
            val activityThreadclass = Class.forName("android.app.ActivityThread")
            val sCurrentActivityThreadFiled = activityThreadclass.getDeclaredField("sCurrentActivityThread")
            sCurrentActivityThreadFiled.isAccessible = true
            val sCurrentActivityThread = sCurrentActivityThreadFiled[null]

            //6.获取mH实例
            val mHFiled = activityThreadclass.getDeclaredField("mH")
            mHFiled.isAccessible = true
            val mH = mHFiled[sCurrentActivityThread]
            val mCallbackFiled = Handler::class.java.getDeclaredField("mCallback")
            mCallbackFiled.isAccessible = true
            //7.设置进入我们自己的Callback
            mCallbackFiled[mH] = MyHandlerCallback(mH as Handler)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("TAG------", e.toString())
        }
    }

    /**
     * Hook Instrumentation
     *
     * @param context 上下文环境
     * @throws Exception
     */
    @Throws(Exception::class)
    fun hookInstrumentation(context: Context) {
        Log.e(TAG, "hookInstrumentation")
        val activityThreadClazz = Class.forName("android.app.ActivityThread")
        // 获取ActivityThread中成员变量sCurrentActivityThread字段
        val sCurrentActivityThreadField =
            ReflectUtils.getField(activityThreadClazz, "sCurrentActivityThread")
        // 获取ActivityThread中成员变量mInstrumentation字段
        val mInstrumentationField = ReflectUtils.getField(activityThreadClazz, "mInstrumentation")
        // 获取ActivityThread主线程对象(应用程序启动后就会在attach方法中赋值)
        val currentActivityThread = sCurrentActivityThreadField[activityThreadClazz]
        // 获取Instrumentation对象
        val instrumentation = mInstrumentationField[currentActivityThread] as Instrumentation
        val packageManager = context.packageManager
        // 创建Instrumentation代理对象
        val instrumentationProxy = InstrumentationProxy(instrumentation, packageManager)

        // 用InstrumentationProxy代理对象替换原来的Instrumentation对象
        ReflectUtils.setField(activityThreadClazz, "mInstrumentation", currentActivityThread, instrumentationProxy)
    }

    private val singletonByVersion: Any?
        get() {
            try {
                if (Build.VERSION.SDK_INT == 28) {
                    val clazz = Class.forName("android.app.ActivityManager")
                    val field = clazz.getDeclaredField("IActivityManagerSingleton")
                    field.isAccessible = true
                    return field[null]
                } else if (Build.VERSION.SDK_INT == 29) {
                    val clazz = Class.forName("android.app.ActivityTaskManager")
                    val field = clazz.getDeclaredField("IActivityTaskManagerSingleton")
                    field.isAccessible = true
                    return field[null]
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }
}
package com.asm.hooktest.utils

import android.content.Intent
import android.os.Handler
import android.os.Message
import java.lang.Exception

class MyHandlerCallback(private val mHandler: Handler) : Handler.Callback {

    override fun handleMessage(msg: Message): Boolean {
        if (msg.what == 159) {
            val obj = msg.obj
            try {
                //获取ClientTransaction中的mActivityCallbacks集合
                val clazz = Class.forName("android.app.servertransaction.ClientTransaction")
                val mActivityCallbacksFiled = clazz.getDeclaredField("mActivityCallbacks")
                mActivityCallbacksFiled.isAccessible = true
                val list = mActivityCallbacksFiled[obj] as List<*>
                if (list.isNotEmpty()) {
                    //得到集合中的LaunchActivityItem
                    val o = list[0]!!
                    //获取LaunchActivityItem中的mIntent
                    val launchActivityItemClazz = Class.forName("android.app.servertransaction.LaunchActivityItem")
                    val mIntentFiled = launchActivityItemClazz.getDeclaredField("mIntent")
                    mIntentFiled.isAccessible = true
                    val intent = mIntentFiled[o] as Intent

                    //得到我们设置的class 替换进去
                    val pluginIntent = intent.getParcelableExtra<Intent>(HookHelper.PLUGIN_INTENT)
                    // 将启动SubActivity的Intent替换为启动PluginActivity的Intent
                    intent.component = pluginIntent!!.component
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        mHandler.handleMessage(msg)
        return true
    }
}
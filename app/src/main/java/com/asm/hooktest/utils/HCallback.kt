package com.asm.hooktest.utils

import com.asm.hooktest.utils.HCallback
import android.content.Intent
import android.os.Handler
import android.os.Message
import com.asm.hooktest.utils.ReflectUtils
import com.asm.hooktest.utils.HookHelper
import java.lang.Exception

class HCallback(var mHandler: Handler) : Handler.Callback {

    override fun handleMessage(msg: Message): Boolean {
        if (msg.what == LAUNCH_ACTIVITY) {
            val obj = msg.obj
            try {
                // 获取启动SubActivity的Intent
                val stubIntent = ReflectUtils.getField(obj.javaClass, "intent", obj) as Intent

                // 获取启动PluginActivity的Intent(之前保存在启动SubActivity的Intent之中)
                val pluginIntent = stubIntent.getParcelableExtra<Intent>(HookHelper.PLUGIN_INTENT)

                // 将启动SubActivity的Intent替换为启动PluginActivity的Intent
                stubIntent.component = pluginIntent!!.component
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        mHandler.handleMessage(msg)
        return true
    }

    companion object {
        private const val LAUNCH_ACTIVITY = 100
    }
}
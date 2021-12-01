package com.asm.hooktest.utils

import java.lang.Exception
import java.lang.reflect.Field
import kotlin.Throws

object ReflectUtils {

    @Throws(Exception::class)
    fun getField(clazz: Class<*>, fieldName: String?): Field {
        val field = clazz.getDeclaredField(fieldName)
        if (!field.isAccessible) {
            field.isAccessible = true
        }
        return field
    }

    @Throws(Exception::class)
    fun getField(clazz: Class<*>, fieldName: String?, obj: Any?): Any {
        val field = clazz.getDeclaredField(fieldName)
        if (!field.isAccessible) {
            field.isAccessible = true
        }
        return field[obj]
    }

    @Throws(Exception::class)
    fun setField(clazz: Class<*>, fieldName: String?, obj: Any?, value: Any?) {
        val field = clazz.getDeclaredField(fieldName)
        if (!field.isAccessible) {
            field.isAccessible = true
        }
        field[obj] = value
    }
}
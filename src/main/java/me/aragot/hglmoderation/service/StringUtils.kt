package me.aragot.hglmoderation.service

import java.util.*

class StringUtils {
    companion object {
        private fun capitalize(input: String): String {
            return input.substring(0, 1).uppercase(Locale.getDefault()) + input.substring(1)
        }

        fun prettyEnum(rawEnum: Enum<*>): String {
            val splitEnumName = rawEnum.name.lowercase(Locale.getDefault()).split("_".toRegex()).toTypedArray()
            val builder = StringBuilder()
            for (part in splitEnumName) builder.append(capitalize(part)).append(" ")
            builder.replace(builder.length - 1, builder.length, "")
            return builder.toString()
        }
    }
}
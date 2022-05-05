package org.code4everything.hutool.dart

/**
 * utilities for flutter dart lang
 */
object Darts {

    @JvmStatic
    fun object2Map(fields: String): StringBuilder {
        val lineSep = System.lineSeparator()
        val builder = StringBuilder().append("Map<String, dynamic> toMap() {")
        builder.append(lineSep).append("  return <String, dynamic>{")

        fields.split(";").map { it.split("=", limit = 2)[0].trim() }.filter { it.isNotEmpty() }.forEach {
            val name = it.split(" ").last()
            builder.append(lineSep).append("    \"$name\": $name,")
        }

        return builder.append(lineSep).append("  };").append(lineSep).append("}")
    }
}

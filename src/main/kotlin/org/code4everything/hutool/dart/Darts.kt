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

        map2FieldName(fields).forEach {
            builder.append(lineSep).append("    \"$it\": $it,")
        }

        return builder.append(lineSep).append("  };").append(lineSep).append("}")
    }

    @JvmStatic
    fun constructor(fields: String): StringBuilder {
        val builder = StringBuilder().append("{")
        map2FieldName(fields).forEach {
            builder.append("this.$it,")
        }
        return builder.append("}")
    }

    @JvmStatic
    private fun map2FieldName(fields: String): List<String> {
        return fields.split(";").map { it.split("=", limit = 2)[0].trim().split(" ").last() }.filter { it.isNotEmpty() }
    }
}

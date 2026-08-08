package pl.yalgrin.playnite.simplesync.common.utils

import org.apache.commons.lang3.builder.ToStringBuilder
import org.apache.commons.lang3.builder.ToStringStyle
import java.io.Serial

object ToStringUtils {
    val CUSTOM_STYLE: ToStringStyle = CustomStyle()

    fun createBuilder(obj: Any?): ToStringBuilder {
        return object : ToStringBuilder(obj, CUSTOM_STYLE) {
            override fun append(fieldName: String?, obj: Any?): ToStringBuilder {
                if (obj != null) {
                    return super.append(fieldName, obj)
                }
                return this
            }
        }
    }

    private class CustomStyle : ToStringStyle() {
        init {
            isUseShortClassName = true
            isUseIdentityHashCode = false
        }

        @Serial
        fun readResolve(): Any {
            return CUSTOM_STYLE
        }

        companion object {
            @Serial
            private const val serialVersionUID = -3638644891843475385L
        }
    }
}

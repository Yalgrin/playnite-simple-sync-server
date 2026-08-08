package pl.yalgrin.playnite.simplesync.common.dto

import java.io.Serial
import java.io.Serializable

abstract class AbstractDTO : Serializable {
    companion object {
        @Serial
        const val serialVersionUID = 6859933206281608604L
    }
}
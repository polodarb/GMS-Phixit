package ua.polodarb.gmsphixit.core.phixit.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class BaseFlagModel {
    abstract val name: String
    abstract val value: Any
    abstract fun toType(): Long

    @Serializable
    @SerialName("BoolFlag")
    data class BoolFlag(
        override val name: String,
        override val value: Boolean
    ) : BaseFlagModel() {
        override fun toType(): Long = if (value) 1 else 0
    }

    @Serializable
    @SerialName("IntFlag")
    data class IntFlag(
        override val name: String,
        override val value: Long
    ) : BaseFlagModel() {
        override fun toType(): Long = 2
    }

    @Serializable
    @SerialName("FloatFlag")
    data class FloatFlag(
        override val name: String,
        override val value: Long
    ) : BaseFlagModel() {
        override fun toType(): Long = 3
    }

    @Serializable
    @SerialName("StringFlag")
    data class StringFlag(
        override val name: String,
        override val value: String
    ) : BaseFlagModel() {
        override fun toType(): Long = 4
    }

    @Serializable
    @SerialName("ExtensionFlag")
    data class ExtensionFlag(
        override val name: String,
        override val value: ByteArray
    ) : BaseFlagModel() {
        override fun toType(): Long = 5
    }
}

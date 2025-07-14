package ua.polodarb.gmsphixit.core.phixit.model

import android.os.Parcel
import android.os.Parcelable

abstract class ParcelableFlagModel : Parcelable {
    abstract val name: String
    abstract val value: Any

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(name)
        when (this) {
            is ParcelableBoolFlag -> {
                dest.writeInt(0)
                dest.writeByte(if (value) 1 else 0)
            }
            is ParcelableIntFlag -> {
                dest.writeInt(1)
                dest.writeLong(value)
            }
            is ParcelableFloatFlag -> {
                dest.writeInt(2)
                dest.writeLong(value)
            }
            is ParcelableStringFlag -> {
                dest.writeInt(3)
                dest.writeString(value)
            }
            is ParcelableExtensionFlag -> {
                dest.writeInt(4)
                dest.writeByteArray(value)
            }
        }
    }

    fun toSerializable(): BaseFlagModel = when (this) {
        is ParcelableBoolFlag -> BaseFlagModel.BoolFlag(name, value)
        is ParcelableIntFlag -> BaseFlagModel.IntFlag(name, value)
        is ParcelableFloatFlag -> BaseFlagModel.FloatFlag(name, value)
        is ParcelableStringFlag -> BaseFlagModel.StringFlag(name, value)
        is ParcelableExtensionFlag -> BaseFlagModel.ExtensionFlag(name, value)
        else -> throw IllegalStateException("Unknown ParcelableFlagModel")
    } as BaseFlagModel

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<ParcelableFlagModel> = object : Parcelable.Creator<ParcelableFlagModel> {
            override fun createFromParcel(parcel: Parcel): ParcelableFlagModel {
                val name = parcel.readString() ?: ""
                return when (parcel.readInt()) {
                    0 -> ParcelableBoolFlag(name, parcel.readByte().toInt() != 0)
                    1 -> ParcelableIntFlag(name, parcel.readLong())
                    2 -> ParcelableFloatFlag(name, parcel.readLong())
                    3 -> ParcelableStringFlag(name, parcel.readString() ?: "")
                    4 -> ParcelableExtensionFlag(name, parcel.createByteArray() ?: ByteArray(0))
                    else -> throw IllegalArgumentException("Unknown flag type in parcel")
                }
            }

            override fun newArray(size: Int): Array<ParcelableFlagModel?> = arrayOfNulls(size)
        }
    }
}

data class ParcelableBoolFlag(override val name: String, override val value: Boolean) : ParcelableFlagModel()
data class ParcelableIntFlag(override val name: String, override val value: Long) : ParcelableFlagModel()
data class ParcelableFloatFlag(override val name: String, override val value: Long) : ParcelableFlagModel()
data class ParcelableStringFlag(override val name: String, override val value: String) : ParcelableFlagModel()
data class ParcelableExtensionFlag(override val name: String, override val value: ByteArray) : ParcelableFlagModel()

fun BaseFlagModel.toParcelable(): ParcelableFlagModel = when (this) {
    is BaseFlagModel.BoolFlag -> ParcelableBoolFlag(name, value)
    is BaseFlagModel.IntFlag -> ParcelableIntFlag(name, value)
    is BaseFlagModel.FloatFlag -> ParcelableFloatFlag(name, value)
    is BaseFlagModel.StringFlag -> ParcelableStringFlag(name, value)
    is BaseFlagModel.ExtensionFlag -> ParcelableExtensionFlag(name, value)
}

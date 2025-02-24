import android.os.Parcel
import android.os.Parcelable

data class UserSettings(
    val music: Boolean = true,
    val sound: Boolean = true,
    val vibration: Boolean = true
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (music) 1 else 0)
        parcel.writeByte(if (sound) 1 else 0)
        parcel.writeByte(if (vibration) 1 else 0)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<UserSettings> {
        override fun createFromParcel(parcel: Parcel): UserSettings {
            return UserSettings(parcel)
        }

        override fun newArray(size: Int): Array<UserSettings?> {
            return arrayOfNulls(size)
        }
    }
}

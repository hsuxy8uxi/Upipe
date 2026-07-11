package com.upipe.app.about

import android.os.Parcelable
import java.io.Serializable
import kotlinx.parcelize.Parcelize

/**
 * Class for storing information about a software license.
 */
@Parcelize
class License(val name: String, val abbreviation: String, val filename: String) : Parcelable, Serializable

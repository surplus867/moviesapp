package com.minyu.moviesapp.movieList.data.remote.respond

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TrailerDto(
    val id: String?,             // Unique trailer ID
    val key: String?,            // Youtube or video key
    val name: String?,          // Trailer name/title
    val site: String?,          // Hosting site (e.g., YouTube)
    val type: String?,          // Type (e.g., "Trailer", "Teaser")
    val official: Boolean?      // Whether the trailer is offical
) : Parcelable

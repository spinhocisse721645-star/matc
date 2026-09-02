package com.example.data

import com.squareup.moshi.Json

data class Match(
    @Json(name = "api_fixture_id") val apiFixtureId: Long? = null,
    @Json(name = "equipe_domicile") val equipeDomicile: String? = null,
    @Json(name = "equipe_exterieur") val equipeExterieur: String? = null,
    @Json(name = "ligue") val ligue: String? = null,
    @Json(name = "date_match") val dateMatch: String? = null,
    @Json(name = "statut") val statut: String? = null,
    @Json(name = "score_domicile") val scoreDomicile: Int? = null,
    @Json(name = "score_exterieur") val scoreExterieur: Int? = null
)

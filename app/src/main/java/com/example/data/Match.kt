package com.example.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Match(
    @param:Json(name = "api_fixture_id") val apiFixtureId: Long? = null,
    @param:Json(name = "equipe_domicile") val equipeDomicile: String? = null,
    @param:Json(name = "equipe_exterieur") val equipeExterieur: String? = null,
    @param:Json(name = "ligue") val ligue: String? = null,
    @param:Json(name = "date_match") val dateMatch: String? = null,
    @param:Json(name = "statut") val statut: String? = null,
    @param:Json(name = "score_domicile") val scoreDomicile: Int? = null,
    @param:Json(name = "score_exterieur") val scoreExterieur: Int? = null
)

package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class SupabaseMatchRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("supabase_prefs", Context.MODE_PRIVATE)

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val matchListType = Types.newParameterizedType(List::class.java, Match::class.java)
    private val listAdapter = moshi.adapter<List<Match>>(matchListType)

    private fun getBuildConfigField(fieldName: String): String {
        return try {
            val field = BuildConfig::class.java.getField(fieldName)
            (field.get(null) as? String) ?: ""
        } catch (_: Throwable) {
            ""
        }
    }

    var supabaseUrl: String
        get() = prefs.getString("supabase_url", null)
            ?.takeIf { it.isNotBlank() }
            ?: getBuildConfigField("SUPABASE_URL").trim()
        set(value) = prefs.edit().putString("supabase_url", value.trim()).apply()

    var supabaseAnonKey: String
        get() = prefs.getString("supabase_anon_key", null)
            ?.takeIf { it.isNotBlank() }
            ?: getBuildConfigField("SUPABASE_ANON_KEY").trim()
        set(value) = prefs.edit().putString("supabase_anon_key", value.trim()).apply()

    suspend fun fetchMatchs(): Result<List<Match>> = withContext(Dispatchers.IO) {
        val url = supabaseUrl.trimEnd('/')
        val key = supabaseAnonKey.trim()

        if (url.isBlank() || key.isBlank()) {
            return@withContext Result.failure(
                IllegalStateException("Configuration Supabase manquante (URL ou Clé Anon)")
            )
        }

        try {
            val endpoint = "$url/rest/v1/match?select=api_fixture_id,equipe_domicile,equipe_exterieur,ligue,date_match,statut,score_domicile,score_exterieur&order=date_match.desc"
            val request = Request.Builder()
                .url(endpoint)
                .addHeader("apikey", key)
                .addHeader("Authorization", "Bearer $key")
                .addHeader("Accept", "application/json")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        Exception("Erreur Supabase HTTP ${response.code}: ${response.message}")
                    )
                }

                val bodyString = response.body?.string()
                if (bodyString.isNullOrBlank()) {
                    return@withContext Result.success(emptyList())
                }

                val matches = listAdapter.fromJson(bodyString) ?: emptyList()
                Result.success(matches)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

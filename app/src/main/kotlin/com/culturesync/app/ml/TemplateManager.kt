package com.culturesync.app.ml

import android.content.Context
import com.culturesync.app.models.ExpertTemplate
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Loads and caches expert Beeralu technique templates from assets/templates/<id>.json.
 * Templates are bundled with the APK — no internet required.
 * On first access the file is parsed; subsequent calls return the cached copy.
 */
class TemplateManager(private val context: Context) {

    private val gson = Gson()
    private val cache = mutableMapOf<String, ExpertTemplate>()

    fun loadTemplate(techniqueId: String): ExpertTemplate? {
        cache[techniqueId]?.let { return it }

        try {
            val file = java.io.File(context.filesDir, "$techniqueId.json")
            if (file.exists()) {
                val json = file.readText()
                val type = object : TypeToken<ExpertTemplate>() {}.type
                val template: ExpertTemplate = gson.fromJson(json, type)
                cache[techniqueId] = template
                return template
            }
        } catch (_: Exception) {}

        return try {
            val json = context.assets.open("templates/$techniqueId.json")
                .bufferedReader()
                .use { it.readText() }
            val type = object : TypeToken<ExpertTemplate>() {}.type
            val template: ExpertTemplate = gson.fromJson(json, type)
            cache[techniqueId] = template
            template
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun isTemplateAvailable(techniqueId: String): Boolean = try {
        if (java.io.File(context.filesDir, "$techniqueId.json").exists()) {
            true
        } else {
            context.assets.open("templates/$techniqueId.json").close()
            true
        }
    } catch (_: Exception) { false }

    fun clearCache() = cache.clear()
}

package com.example.clickassistant
import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
class GesturesRepository(context: Context) {
    private val prefs = context.getSharedPreferences("click_assistant_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val key = "gesture_steps"
    fun getSteps(): MutableList<GestureStep> {
        val json = prefs.getString(key, null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<GestureStep>>() {}.type
        return gson.fromJson(json, type) ?: mutableListOf()
    }
    fun saveSteps(steps: List<GestureStep>) { prefs.edit().putString(key, gson.toJson(steps)).apply() }
    fun addStep(step: GestureStep) { val s = getSteps(); s.add(step); saveSteps(s) }
    fun removeStep(index: Int) { val s = getSteps(); if (index in s.indices) { s.removeAt(index); saveSteps(s) } }
    fun clearSteps() { prefs.edit().remove(key).apply() }
}
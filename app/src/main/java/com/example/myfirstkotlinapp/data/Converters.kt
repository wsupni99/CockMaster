package com.example.myfirstkotlinapp.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    @TypeConverter
    fun fromStringList(list: List<String>?): String? {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun toStringList(json: String?): List<String>? {
        return json?.let {
            Gson().fromJson(it, object : TypeToken<List<String>>() {}.type)
        }
    }
}
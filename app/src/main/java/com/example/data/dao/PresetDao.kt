package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.models.Preset
import com.example.data.models.PresetWithMacros
import kotlinx.coroutines.flow.Flow

@Dao
interface PresetDao {

    @Query("SELECT * FROM presets ORDER BY name ASC")
    fun getAllPresets(): Flow<List<Preset>>

    @Query("SELECT * FROM presets WHERE id = :id")
    fun getPresetById(id: Long): Flow<Preset?>

    @Query("SELECT * FROM presets WHERE id = :id")
    suspend fun getPresetByIdDirect(id: Long): Preset?

    @Transaction
    @Query("SELECT * FROM presets WHERE id = :id")
    fun getPresetWithMacros(id: Long): Flow<PresetWithMacros?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: Preset): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPresets(presets: List<Preset>)

    @Update
    suspend fun updatePreset(preset: Preset)

    @Delete
    suspend fun deletePreset(preset: Preset)

    @Query("DELETE FROM presets WHERE id = :id")
    suspend fun deletePresetById(id: Long)
}

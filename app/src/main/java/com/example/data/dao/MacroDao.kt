package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.models.Macro
import com.example.data.models.MacroPresetCrossRef
import com.example.data.models.MacroStatus
import com.example.data.models.MacroWebhookCrossRef
import com.example.data.models.MacroWithRelations
import kotlinx.coroutines.flow.Flow

@Dao
interface MacroDao {

    @Query("SELECT * FROM macros ORDER BY updatedAt DESC")
    fun getAllMacros(): Flow<List<Macro>>

    @Query("SELECT * FROM macros WHERE status IN (:statuses) ORDER BY updatedAt DESC")
    fun getMacrosByStatuses(statuses: List<MacroStatus>): Flow<List<Macro>>

    @Query("SELECT * FROM macros WHERE id = :id")
    fun getMacroById(id: Long): Flow<Macro?>

    @Query("SELECT * FROM macros WHERE id = :id")
    suspend fun getMacroByIdDirect(id: Long): Macro?

    @Transaction
    @Query("SELECT * FROM macros WHERE id = :id")
    fun getMacroWithRelations(id: Long): Flow<MacroWithRelations?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMacro(macro: Macro): Long

    @Update
    suspend fun updateMacro(macro: Macro)

    @Delete
    suspend fun deleteMacro(macro: Macro)

    @Query("DELETE FROM macros WHERE id = :id")
    suspend fun deleteMacroById(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMacroPresetCrossRef(crossRef: MacroPresetCrossRef)

    @Query("DELETE FROM macro_preset_cross_ref WHERE macroId = :macroId")
    suspend fun deletePresetCrossRefsForMacro(macroId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMacroWebhookCrossRef(crossRef: MacroWebhookCrossRef)

    @Query("DELETE FROM macro_webhook_cross_ref WHERE macroId = :macroId")
    suspend fun deleteWebhookCrossRefsForMacro(macroId: Long)

    @Transaction
    suspend fun saveMacroWithRelations(
        macro: Macro,
        presetIds: List<Long>,
        webhookIds: List<Long>
    ): Long {
        val macroId = if (macro.id == 0L) {
            insertMacro(macro)
        } else {
            updateMacro(macro)
            macro.id
        }

        deletePresetCrossRefsForMacro(macroId)
        for (presetId in presetIds) {
            insertMacroPresetCrossRef(MacroPresetCrossRef(macroId = macroId, presetId = presetId))
        }

        deleteWebhookCrossRefsForMacro(macroId)
        for (webhookId in webhookIds) {
            insertMacroWebhookCrossRef(MacroWebhookCrossRef(macroId = macroId, webhookId = webhookId))
        }

        return macroId
    }
}

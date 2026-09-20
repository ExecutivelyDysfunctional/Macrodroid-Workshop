package com.example.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "macro_preset_cross_ref",
    primaryKeys = ["macroId", "presetId"],
    foreignKeys = [
        ForeignKey(
            entity = Macro::class,
            parentColumns = ["id"],
            childColumns = ["macroId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Preset::class,
            parentColumns = ["id"],
            childColumns = ["presetId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("macroId"),
        Index("presetId")
    ]
)
data class MacroPresetCrossRef(
    val macroId: Long,
    val presetId: Long
)

package com.winlator.cmod.feature.sync

import android.app.Activity
import android.content.Context
import com.winlator.cmod.feature.steamcloudsync.SteamSaveSnapshotManager
import com.winlator.cmod.runtime.container.Container
import com.winlator.cmod.runtime.container.ContainerManager
import com.winlator.cmod.runtime.container.Shortcut
import timber.log.Timber

/**
 * Shared save-backup models and local rollback helpers.
 *
 * Cloud saves themselves are owned by the Steam/Epic/GOG providers. This class
 * deliberately contains no Google/Play Games backend or authentication.
 */
object SaveBackupManager {
    private const val TAG = "SaveBackup"
    private const val PREFS_NAME = "cloud_save_backup"
    private const val KEY_KEEP_REPLACED_BACKUP = "cloud_sync_keep_replaced_backup"

    const val MAX_HISTORY_ENTRIES = 100
    const val HISTORY_MAX_AGE_DAYS = 30
    const val MAX_HISTORY_LABEL_LENGTH = 48

    const val CUSTOM_SAVE_CONTAINER_ID_KEY = "customSaveContainerId"
    const val CUSTOM_SAVE_WINDOWS_PATH_KEY = "customSaveWindowsPath"
    private const val LEGACY_CUSTOM_GAME_FOLDER_KEY = "custom_game_folder"

    enum class GameSource(val code: Char) {
        STEAM('s'),
        EPIC('e'),
        GOG('g'),
        CUSTOM('c'),
    }

    enum class BackupStorage {
        STEAM_LOCAL,
        STEAM_CLOUD,
        EPIC_CLOUD,
        GOG_CLOUD,
    }

    enum class BackupOrigin(val tag: String) {
        LOCAL("local"),
        CLOUD("cloud"),
        MANUAL("manual"),
        AUTO("auto"),
        ;

        companion object {
            fun fromTag(tag: String?): BackupOrigin? = entries.firstOrNull { it.tag == tag }
        }
    }

    data class BackupResult(
        val success: Boolean,
        val message: String,
    )

    data class BackupHistoryEntry(
        val fileId: String,
        val fileName: String,
        val timestampMs: Long,
        val origin: BackupOrigin,
        val sizeBytes: Long,
        val label: String? = null,
        val storage: BackupStorage = BackupStorage.STEAM_LOCAL,
    )

    /** Capture a local rollback snapshot. No remote backup is performed. */
    suspend fun backupDiscardedSave(
        activity: Activity,
        gameSource: GameSource,
        gameId: String,
        gameName: String,
        origin: BackupOrigin,
        customSaveDir: java.io.File? = null,
        containerHint: Container? = null,
    ): BackupResult {
        if (gameSource != GameSource.STEAM) {
            return BackupResult(false, "Local rollback snapshots are only supported for Steam.")
        }
        val appId = gameId.toIntOrNull()
            ?: return BackupResult(false, "Invalid Steam appId for snapshot.")
        return try {
            val ok = SteamSaveSnapshotManager.recordSnapshot(
                activity.applicationContext,
                appId,
                origin,
                containerHint,
            )
            if (ok) {
                BackupResult(true, "Local snapshot captured.")
            } else if (origin == BackupOrigin.LOCAL) {
                BackupResult(true, "No local save files found to snapshot.")
            } else {
                BackupResult(false, "Failed to capture local save snapshot.")
            }
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "backupDiscardedSave failed for $gameSource/$gameId")
            BackupResult(false, "Failed to back up save: ${e.message}")
        }
    }

    fun isKeepReplacedBackupEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_KEEP_REPLACED_BACKUP, true)

    fun setKeepReplacedBackupEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_KEEP_REPLACED_BACKUP, enabled).apply()
    }

    fun setCustomGameSavePath(shortcut: Shortcut, container: Container, windowsPath: String) {
        shortcut.putExtra(CUSTOM_SAVE_CONTAINER_ID_KEY, container.id.toString())
        shortcut.putExtra(CUSTOM_SAVE_WINDOWS_PATH_KEY, windowsPath)
        shortcut.saveData()
    }

    fun clearCustomGameSavePath(shortcut: Shortcut) {
        shortcut.putExtra(CUSTOM_SAVE_CONTAINER_ID_KEY, null)
        shortcut.putExtra(CUSTOM_SAVE_WINDOWS_PATH_KEY, null)
        shortcut.saveData()
    }

    fun getCustomGameSaveWindowsPath(shortcut: Shortcut): String? =
        shortcut.getExtra(CUSTOM_SAVE_WINDOWS_PATH_KEY)?.takeIf { it.isNotEmpty() }

    fun customGameId(shortcut: Shortcut): String {
        val containerId = shortcut.container?.id?.toString() ?: "0"
        val shortcutName = shortcut.file?.name ?: shortcut.name ?: "shortcut"
        return "$containerId:$shortcutName"
    }

    fun retroSaveDir(context: Context, shortcut: Shortcut?, gameId: String? = null): java.io.File? = null

    fun customGameId(containerId: Int, shortcutFileName: String): String = "$containerId:$shortcutFileName"

    const val ENGINE_SAVE_SUFFIX = ":3d"

    fun isEngineGameId(gameId: String): Boolean = gameId.endsWith(ENGINE_SAVE_SUFFIX)

    fun engineGameId(gameId: String): String =
        if (isEngineGameId(gameId)) gameId else gameId + ENGINE_SAVE_SUFFIX

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun sanitizeHistoryLabel(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        val cleaned = raw
            .replace(Regex("""[/\\:*?\"<>|\r\n\t]"""), "")
            .trim()
            .take(MAX_HISTORY_LABEL_LENGTH)
        return cleaned.ifEmpty { null }
    }
}

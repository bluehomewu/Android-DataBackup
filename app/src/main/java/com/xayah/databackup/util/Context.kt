package com.xayah.databackup.util

import android.app.AppOpsManager
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.os.Build
import android.os.Process
import com.xayah.databackup.App
import com.xayah.databackup.data.*

fun Context.savePreferences(key: String, value: String) {
    getSharedPreferences("settings", MODE_PRIVATE).edit().apply {
        putString(key, value)
        apply()
    }
}

fun Context.savePreferences(key: String, value: Boolean) {
    getSharedPreferences("settings", MODE_PRIVATE).edit().apply {
        putBoolean(key, value)
        apply()
    }
}

fun Context.savePreferences(key: String, value: Int) {
    getSharedPreferences("settings", MODE_PRIVATE).edit().apply {
        putInt(key, value)
        apply()
    }
}

fun Context.readPreferencesString(key: String): String? {
    getSharedPreferences("settings", MODE_PRIVATE).apply {
        return getString(key, null)
    }
}

fun Context.readPreferencesBoolean(key: String, defValue: Boolean = false): Boolean {
    getSharedPreferences("settings", MODE_PRIVATE).apply {
        return getBoolean(key, defValue)
    }
}

fun Context.readPreferencesInt(key: String, defValue: Int = 0): Int {
    getSharedPreferences("settings", MODE_PRIVATE).apply {
        return getInt(key, defValue)
    }
}

fun Context.saveBackupSavePath(path: CharSequence?) {
    savePreferences("backup_save_path", path.toString().trim())
}

fun Context.readBackupSavePath(): String {
    return readPreferencesString("backup_save_path") ?: GlobalString.defaultBackupSavePath
}

fun Context.saveBackupStrategy(backupStrategy: BackupStrategy) {
    savePreferences("backup_strategy", backupStrategy.toString().trim())
}

fun Context.readBackupStrategy(): BackupStrategy {
    return toBackupStrategy(readPreferencesString("backup_strategy"))
}

fun Context.saveCompressionType(type: CharSequence?) {
    savePreferences("compression_type", type.toString().trim())
}

fun Context.readCompressionType(): String {
    return readPreferencesString("compression_type") ?: "zstd"
}

fun Context.saveIsDynamicColors(value: Boolean) {
    savePreferences("is_dynamic_colors", value)
}

fun Context.readIsDynamicColors(): Boolean {
    return readPreferencesBoolean("is_dynamic_colors")
}

fun Context.saveIsBackupItself(value: Boolean) {
    savePreferences("is_backup_itself", value)
}

fun Context.readIsBackupItself(): Boolean {
    return readPreferencesBoolean("is_backup_itself", true)
}

fun Context.saveIsBackupIcon(value: Boolean) {
    savePreferences("is_backup_icon", value)
}

fun Context.readIsBackupIcon(): Boolean {
    return readPreferencesBoolean("is_backup_icon", true)
}

fun Context.saveIsReadIcon(value: Boolean) {
    savePreferences("is_read_icon", value)
}

fun Context.readIsReadIcon(): Boolean {
    return readPreferencesBoolean("is_read_icon", true)
}

fun Context.saveIsBackupTest(value: Boolean) {
    savePreferences("is_backup_test", value)
}

fun Context.readIsBackupTest(): Boolean {
    return readPreferencesBoolean("is_backup_test", true)
}

fun Context.saveBackupUser(user: CharSequence?) {
    savePreferences("backup_user", user.toString().trim())
}

fun Context.readBackupUser(): String {
    return readPreferencesString("backup_user") ?: GlobalObject.defaultUserId
}

fun Context.saveRestoreUser(user: CharSequence?) {
    savePreferences("restore_user", user.toString().trim())
}

fun Context.readRestoreUser(): String {
    return readPreferencesString("restore_user") ?: GlobalObject.defaultUserId
}

fun Context.saveInitializedVersionName(value: CharSequence?) {
    savePreferences("initialized_version_name", value.toString().trim())
}

fun Context.readInitializedVersionName(): String {
    return readPreferencesString("initialized_version_name") ?: ""
}

fun Context.saveCustomBackupSavePath(path: CharSequence?) {
    savePreferences("custom_backup_save_path", path.toString().trim())
}

fun Context.readCustomBackupSavePath(): String {
    return readPreferencesString("custom_backup_save_path") ?: GlobalString.defaultBackupSavePath
}

fun Context.saveBackupSaveIndex(value: Int) {
    savePreferences("backup_save_index", value)
}

fun Context.readBackupSaveIndex(): Int {
    return readPreferencesInt("backup_save_index", 0)
}

fun Context.saveAutoFixMultiUserContext(value: Boolean) {
    savePreferences("auto_fix_multi_user_context", value)
}

fun Context.readAutoFixMultiUserContext(): Boolean {
    return readPreferencesBoolean("auto_fix_multi_user_context", false)
}

fun Context.saveRcloneConfigName(name: CharSequence?) {
    savePreferences("rclone_config_name", name.toString().trim())
}

fun Context.readRcloneConfigName(): String {
    return readPreferencesString("rclone_config_name") ?: ""
}

fun Context.saveIsSupportUsageAccess(value: Boolean) {
    savePreferences("is_support_usage_access", value)
}

fun Context.readIsSupportUsageAccess(): Boolean {
    return readPreferencesBoolean("is_support_usage_access", true)
}

fun Context.saveAppVersion(value: String) {
    savePreferences("app_version", value)
}

fun Context.readAppVersion(): String {
    return readPreferencesString("app_version") ?: ""
}

fun Context.saveUpdateChannel(updateChannel: UpdateChannel) {
    savePreferences("update_channel", updateChannel.toString().trim())
}

fun Context.readUpdateChannel(): UpdateChannel {
    return toUpdateChannel(readPreferencesString("update_channel"))
}

fun Context.saveCompatibleMode(value: Boolean) {
    savePreferences("compatible_mode", value)
}

fun Context.readCompatibleMode(): Boolean {
    return readPreferencesBoolean("compatible_mode", false)
}

fun Context.saveBlackListMapPath(path: CharSequence?) {
    savePreferences("black_list_map_path", path.toString().trim())
}

fun Context.readBlackListMapPath(): String {
    return readPreferencesString("black_list_map_path") ?: Path.getDefaultBlackMapPath()
}

fun Context.saveListActiveSort(appListSort: AppListSort) {
    savePreferences("list_active_sort", appListSort.toString().trim())
}

fun Context.readListActiveSort(): AppListSort {
    return toAppListSort(readPreferencesString("list_active_sort"))
}

fun Context.saveListSortAscending(value: Boolean) {
    savePreferences("list_sort_ascending", value)
}

fun Context.readListSortAscending(): Boolean {
    return readPreferencesBoolean("list_sort_ascending", true)
}

val List<String>.joinToLineString: String
    get() = this.joinToString(separator = "\n")

fun Context.checkPackageUsageStatsPermission(): Boolean {
    val appOpsService =
        this.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        appOpsService.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            App.globalContext.packageName
        )
    } else {
        appOpsService.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            App.globalContext.packageName
        )
    }
    return mode == AppOpsManager.MODE_ALLOWED
}

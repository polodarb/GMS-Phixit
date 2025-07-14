package ua.polodarb.gmsphixit.core.phixit.service

import android.content.Intent
import android.database.sqlite.SQLiteException
import android.os.IBinder
import android.util.Log
import com.google.protobuf.CodedInputStream
import com.google.protobuf.CodedOutputStream
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.ipc.RootService
import io.requery.android.database.sqlite.SQLiteDatabase
import io.requery.android.database.sqlite.SQLiteDatabase.OPEN_READWRITE
import io.requery.android.database.sqlite.SQLiteDatabase.openDatabase
import ua.polodarb.gmsphixit.utils.ZipUtils
import ua.polodarb.gmsphixit.core.phixit.model.BaseFlagModel
import ua.polodarb.gmsphixit.core.phixit.model.ParcelableFlagModel
import ua.polodarb.gmsphixit.core.phixit.model.toParcelable
import java.io.ByteArrayOutputStream

class PhixitFlagsService : RootService() {

    private lateinit var gmsDatabase: SQLiteDatabase
    val DB_PATH_GMS = "/data/data/com.google.android.gms/databases/phenotype.db"

    override fun onCreate() {
        super.onCreate()
        Log.i(LOG_TAG, "PhixitFlagsService onCreate")
    }

    val binder = object : IPhixitFlagsService.Stub() {

        override fun getAllFlags(packageName: String?): List<ParcelableFlagModel> {
            val safeName = packageName ?: ""
            Log.i(LOG_TAG, "getAllFlags called for: $safeName")
            return dumpFlags(safeName).values.flatten().map { it.toParcelable() }
        }

        override fun getBoolFlags(packageName: String?): List<ParcelableFlagModel> {
            val safeName = packageName ?: ""
            Log.i(LOG_TAG, "getBoolFlags called for: $safeName")
            return dumpBoolFlags(safeName).map { it.toParcelable() }
        }

        override fun updateFlag(packageName: String?, flagName: String?, newValue: Boolean) {
            val safePackage = packageName ?: return
            val safeFlag = flagName ?: return
            Log.i(LOG_TAG, "updateFlag: [$safePackage] $safeFlag = $newValue")

            val flagsMap = dumpFlags(safePackage)
            val modified = flagsMap.mapValues { (_, flags) ->
                flags.map {
                    if (it is BaseFlagModel.BoolFlag && it.name == safeFlag) {
                        Log.d(LOG_TAG, "Modifying flag '${it.name}' to $newValue")
                        it.copy(value = newValue)
                    } else it
                }
            }

            encodeAndSave(safePackage, modified)
        }

        override fun addBoolFlag(packageName: String?, flagName: String?, value: Boolean) {
            Log.i(LOG_TAG, "addBoolFlag() called: package=$packageName, flag=$flagName, value=$value")

            if (packageName.isNullOrBlank() || flagName.isNullOrBlank()) {
                Log.w(LOG_TAG, "Package name or flag name is blank, skipping")
                return
            }

            val flags = dumpFlags(packageName)
            Log.i(LOG_TAG, "Fetched ${flags.size} partitions for $packageName")

            val updated = flags.toMutableMap()

            val targetPartitionId = flags.entries.firstOrNull { (_, list) ->
                list.any { it is BaseFlagModel.BoolFlag }
            }?.key

            if (targetPartitionId == null) {
                Log.w(LOG_TAG, "No partition with bool flags found in $packageName")
                return
            }

            val current = updated[targetPartitionId].orEmpty()
            Log.d(LOG_TAG, "Partition $targetPartitionId has ${current.size} flags")

            if (current.any { it.name == flagName }) {
                Log.i(LOG_TAG, "Flag $flagName already exists, skipping")
                return
            }

            val newFlag = BaseFlagModel.BoolFlag(flagName, value)
            val newList = (current + newFlag).sortedBy { it.name.toLongOrNull() ?: Long.MAX_VALUE }

            updated[targetPartitionId] = newList
            Log.i(LOG_TAG, "Inserting new flag $flagName into partition $targetPartitionId")

            encodeAndSave(packageName, updated)
            Log.i(LOG_TAG, "Flag $flagName successfully inserted and saved")
        }

        override fun getAllConfigPackages(): List<String> {
            return getAllConfigPackagesInternal()
        }

        override fun getDbVersion(): Int {
            return try {
                gmsDatabase.version
            } catch (e: Exception) {
                -1
            }
        }

    }

    override fun onBind(intent: Intent): IBinder {
        Log.i(LOG_TAG, "PhixitFlagsService onBind called")
        try {
            gmsDatabase = openDatabase(DB_PATH_GMS, null, OPEN_READWRITE)

            val testPackage = "com.google.android.inputmethod.latin"
            val testFlagName = "phixit_test_flag"
            val testFlagValue = true

//            Log.i(LOG_TAG, "Dumping flags for package: $testPackage")
//            val existing = dumpFlags(testPackage)
//            val updated = existing.toMutableMap()
//
//            val targetPartitionId = existing.entries.firstOrNull { (_, flags) ->
//                flags.any { it is BaseFlagModel.BoolFlag }
//            }?.key
//
//            if (targetPartitionId != null) {
//                Log.i(LOG_TAG, "Found target partition: $targetPartitionId")
//
//                val newFlag = BaseFlagModel.BoolFlag(testFlagName, testFlagValue)
//
//                val currentFlags = updated[targetPartitionId].orEmpty()
//
//                if (currentFlags.none { it.name == testFlagName }) {
//                    Log.i(LOG_TAG, "Flag $testFlagName not found, preparing to insert...")
//
//                    val newFlags = (currentFlags + newFlag).sortedBy {
//                        it.name.toLongOrNull() ?: Long.MAX_VALUE
//                    }
//
//                    updated[targetPartitionId] = newFlags
//
//                    encodeAndSave(testPackage, updated)
//                    Log.i(LOG_TAG, "Inserted test flag $testFlagName into partition $targetPartitionId")
//                } else {
//                    Log.i(LOG_TAG, "Test flag $testFlagName already exists in partition $targetPartitionId")
//                }
//            } else {
//                Log.w(LOG_TAG, "No valid partition found for package: $testPackage")
//            }

        } catch (e: SQLiteException) {
            Log.e(LOG_TAG, "Database not found: ${e.message}", e)
            throw DatabaseNotFoundException("Database not found")
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Unexpected error in onBind: ${e.message}", e)
        }

        return binder
    }

    override fun onUnbind(intent: Intent): Boolean {
        Log.i(LOG_TAG, "PhixitFlagsService onUnbind called")
        if (gmsDatabase.isOpen) gmsDatabase.close()
        return super.onUnbind(intent)
    }

    override fun onRebind(intent: Intent) {
        if (gmsDatabase.isOpen) gmsDatabase.close()
        super.onRebind(intent)
    }

    override fun onDestroy() {
        if (gmsDatabase.isOpen) gmsDatabase.close()
        super.onDestroy()
    }

    fun dumpFlags(packageName: String): Map<Int, List<BaseFlagModel>> {
        val decodedFlags = mutableMapOf<Int, List<BaseFlagModel>>()
        Log.i(LOG_TAG, "Dumping flags for package: $packageName")

        try {
            val query = """
            SELECT param_partition_id, flags_content
            FROM param_partitions
            WHERE static_config_package_id = (
                SELECT static_config_package_id
                FROM static_config_packages
                WHERE name = ?
            );
        """.trimIndent()

            gmsDatabase.rawQuery(query, arrayOf(packageName)).use { cursor ->
                val columnIndexId = cursor.getColumnIndexOrThrow("param_partition_id")
                val columnIndexData = cursor.getColumnIndexOrThrow("flags_content")

                while (cursor.moveToNext()) {
                    val partitionId = cursor.getInt(columnIndexId)
                    val compressedData = cursor.getBlob(columnIndexData)

                    try {
                        val decompressed = ZipUtils.decompress(compressedData)
                        val input = CodedInputStream.newInstance(decompressed)

                        val size = input.readUInt32()
                        var next = 0L
                        val flags = List(size) {
                            val theory = input.readUInt64()
                            val shift = theory ushr 3

                            val name = if (shift == 0L) input.readString()
                            else (shift + next).also { next = it }.toString()

                            when (val type = theory.toInt() and 7) {
                                0, 1 -> BaseFlagModel.BoolFlag(name, type != 0)
                                2    -> BaseFlagModel.IntFlag(name, input.readUInt64())
                                3    -> BaseFlagModel.FloatFlag(name, java.lang.Double.doubleToRawLongBits(input.readDouble()))
                                4    -> BaseFlagModel.StringFlag(name, input.readString())
                                5    -> BaseFlagModel.ExtensionFlag(name, input.readByteArray())
                                else -> throw RuntimeException("Unknown flag type: $type")
                            }
                        }

                        decodedFlags[partitionId] = flags
                    } catch (e: Exception) {
                        Log.e(LOG_TAG, "Failed to decode partition $partitionId: ${e.message}", e)
                    }
                }
            }

            return decodedFlags

        } catch (e: Exception) {
            Log.e(LOG_TAG, "Error reading flags: ${e.message}", e)
            return emptyMap()
        }
    }

    fun dumpBoolFlags(packageName: String): List<BaseFlagModel.BoolFlag> {
        val boolFlags = mutableListOf<BaseFlagModel.BoolFlag>()
        Log.i(LOG_TAG, "Dumping BOOL flags for package: $packageName")

        try {
            val query = """
            SELECT param_partition_id, flags_content
            FROM param_partitions
            WHERE static_config_package_id = (
                SELECT static_config_package_id
                FROM static_config_packages
                WHERE name = ?
            );
        """.trimIndent()

            gmsDatabase.rawQuery(query, arrayOf(packageName)).use { cursor ->
                val columnIndexData = cursor.getColumnIndexOrThrow("flags_content")

                while (cursor.moveToNext()) {
                    val compressedData = cursor.getBlob(columnIndexData)

                    try {
                        val decompressed = ZipUtils.decompress(compressedData)
                        val input = CodedInputStream.newInstance(decompressed)

                        val size = input.readUInt32()
                        var next = 0L

                        repeat(size) {
                            val theory = input.readUInt64()
                            val shift = theory ushr 3
                            val name = if (shift == 0L) input.readString()
                            else (shift + next).also { next = it }.toString()

                            when (val type = theory.toInt() and 7) {
                                0, 1 -> boolFlags.add(BaseFlagModel.BoolFlag(name, type != 0))
                                2    -> input.readUInt64()
                                3    -> input.readDouble()
                                4    -> input.readString()
                                5    -> input.readByteArray()
                                else -> throw RuntimeException("Unknown flag type: $type")
                            }
                        }

                    } catch (e: Exception) {
                        Log.e(LOG_TAG, "Failed to decode flags: ${e.message}", e)
                    }
                }
            }

            return boolFlags

        } catch (e: Exception) {
            Log.e(LOG_TAG, "Error reading bool flags: ${e.message}", e)
            return emptyList()
        }
    }

    fun encodeAndSave(packageName: String, flags: Map<Int, List<BaseFlagModel>>) {
        try {
            Log.i(LOG_TAG, "Encoding flags for $packageName")

            val encoded = flags.map { (id, flagList) ->
                val out = ByteArrayOutputStream()
                val cos = CodedOutputStream.newInstance(out)
                var next = 0L

                cos.writeUInt32NoTag(flagList.size)
                Log.d(LOG_TAG, "Partition $id - flags count: ${flagList.size}")

                flagList.forEach { f ->
                    try {
                        f.name.toLongOrNull()?.let { ln ->
                            val theory = ((ln - next) shl 3) or f.toType()
                            cos.writeUInt64NoTag(theory)
                            next = ln
                            Log.d(LOG_TAG, "[OFFSET] name=$ln theory=$theory type=${f.toType()}")
                        } ?: run {
                            cos.writeUInt64NoTag(f.toType())
                            cos.writeStringNoTag(f.name)
                            Log.d(LOG_TAG, "[STRING] name=${f.name} type=${f.toType()}")
                        }

                        when (f) {
                            is BaseFlagModel.BoolFlag -> {}
                            is BaseFlagModel.IntFlag -> cos.writeUInt64NoTag(f.value)
                            is BaseFlagModel.FloatFlag -> cos.writeDoubleNoTag(java.lang.Double.longBitsToDouble(f.value))
                            is BaseFlagModel.StringFlag -> cos.writeStringNoTag(f.value)
                            is BaseFlagModel.ExtensionFlag -> cos.writeByteArrayNoTag(f.value)
                        }
                    } catch (e: Exception) {
                        Log.e(LOG_TAG, "Failed to encode flag '${f.name}': ${e.message}", e)
                    }
                }

                cos.flush()
                id to ZipUtils.compress(out.toByteArray())
            }

            encoded.forEach { (id, blob) ->
                try {
                    val query = """
                        UPDATE param_partitions
                        SET flags_content = ?
                        WHERE param_partition_id = ?
                    """.trimIndent()

                    gmsDatabase.execSQL(query, arrayOf(blob, id))
                    Log.i(LOG_TAG, "Updated partition $id with ${blob.size} bytes")
                } catch (e: Exception) {
                    Log.e(LOG_TAG, "Failed to update partition $id: ${e.message}", e)
                }
            }

            val servingVersion = (System.currentTimeMillis() / 1000).toInt()
            try {
                gmsDatabase.execSQL("UPDATE last_fetch SET serving_version = ? WHERE type = 1", arrayOf(servingVersion))
                Log.i(LOG_TAG, "Updated serving version to $servingVersion")
            } catch (e: Exception) {
                Log.e(LOG_TAG, "Failed to update serving version: ${e.message}", e)
            }

            try {
                clearPhenotypeCache(packageName)
                Log.i(LOG_TAG, "Cleared phenotype cache for $packageName")
            } catch (e: Exception) {
                Log.e(LOG_TAG, "Failed to clear phenotype cache: ${e.message}", e)
            }
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Fatal error in encodeAndSave: ${e.message}", e)
        }
    }

    fun getAllConfigPackagesInternal(): List<String> {
        val packages = mutableListOf<String>()
        try {
            val query = "SELECT name FROM static_config_packages;"
            gmsDatabase.rawQuery(query, null).use { cursor ->
                val nameIndex = cursor.getColumnIndexOrThrow("name")
                while (cursor.moveToNext()) {
                    packages.add(cursor.getString(nameIndex))
                }
            }
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Error reading config packages: ${e.message}", e)
        }
        Log.i(LOG_TAG, "Found ${packages.size} config packages, first 10: ${packages.take(10)}")
        return packages
    }

    fun clearPhenotypeCache(androidPkgName: String) {
        Log.i(LOG_TAG, "Clearing phenotype cache for $androidPkgName")
        Shell.cmd("am force-stop $androidPkgName").exec()
        Shell.cmd("rm -rf /data/data/$androidPkgName/files/phenotype").exec()
        if (androidPkgName.contains("finsky") || androidPkgName.contains("vending")) {
            Shell.cmd("rm -rf /data/data/com.android.vending/files/experiment*").exec()
            Shell.cmd("am force-stop com.android.vending").exec()
        }
        if (androidPkgName.contains("com.google.android.apps.photos")) {
            Shell.cmd("rm -rf /data/data/com.google.android.apps.photos/shared_prefs/phenotype*").exec()
            Shell.cmd("rm -rf /data/data/com.google.android.apps.photos/shared_prefs/com.google.android.apps.photos.phenotype.xml").exec()
            Shell.cmd("am force-stop com.google.android.apps.photos").exec()
        }
        repeat(3) {
            Shell.cmd("am start -a android.intent.action.MAIN -n $androidPkgName &").exec()
            Shell.cmd("am force-stop $androidPkgName").exec()
        }
    }

    companion object {
        private const val LOG_TAG = "PhixitFlagsService"
    }
}

class DatabaseNotFoundException(message: String) : Exception(message)

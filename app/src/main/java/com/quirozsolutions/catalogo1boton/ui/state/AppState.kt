package com.quirozsolutions.catalogo1boton.ui.state

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.quirozsolutions.catalogo1boton.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val Context.dataStore by preferencesDataStore(name = "app_prefs")

class AppState(
    private val context: Context,
    private val container: AppContainer
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    companion object {
        private val KEY_CLIENT_NAME = stringPreferencesKey("client_name")
        private val KEY_SHARED_FOLDER_ID = stringPreferencesKey("shared_folder_id")

        // ✅ Nuevos
        private val KEY_SELLER_WHATSAPP = stringPreferencesKey("seller_whatsapp")
        private val KEY_SELLER_INSTAGRAM = stringPreferencesKey("seller_instagram")
        private val KEY_STORE_LOGO_PATH = stringPreferencesKey("store_logo_path")
    }

    val clientName: Flow<String> =
        context.dataStore.data.map { prefs -> prefs[KEY_CLIENT_NAME] ?: "cliente" }

    val sharedFolderId: Flow<String> =
        context.dataStore.data.map { prefs -> prefs[KEY_SHARED_FOLDER_ID] ?: "" }

    val sellerWhatsapp: Flow<String> =
        context.dataStore.data.map { prefs -> prefs[KEY_SELLER_WHATSAPP] ?: "" }

    val sellerInstagram: Flow<String> =
        context.dataStore.data.map { prefs -> prefs[KEY_SELLER_INSTAGRAM] ?: "" }

    val storeLogoPath: Flow<String> =
        context.dataStore.data.map { prefs -> prefs[KEY_STORE_LOGO_PATH] ?: "" }

    suspend fun clientNameValue(): String =
        context.dataStore.data.map { it[KEY_CLIENT_NAME] ?: "cliente" }.first()

    suspend fun sharedFolderIdValue(): String =
        context.dataStore.data.map { it[KEY_SHARED_FOLDER_ID] ?: "" }.first()

    suspend fun sellerWhatsappValue(): String =
        context.dataStore.data.map { it[KEY_SELLER_WHATSAPP] ?: "" }.first()

    suspend fun sellerInstagramValue(): String =
        context.dataStore.data.map { it[KEY_SELLER_INSTAGRAM] ?: "" }.first()

    suspend fun storeLogoPathValue(): String =
        context.dataStore.data.map { it[KEY_STORE_LOGO_PATH] ?: "" }.first()

    fun setClientName(value: String) {
        scope.launch { context.dataStore.edit { it[KEY_CLIENT_NAME] = value.trim() } }
    }

    fun setSharedFolderId(value: String) {
        scope.launch { context.dataStore.edit { it[KEY_SHARED_FOLDER_ID] = value.trim() } }
    }

    fun setSellerWhatsapp(value: String) {
        scope.launch { context.dataStore.edit { it[KEY_SELLER_WHATSAPP] = value.trim() } }
    }

    fun setSellerInstagram(value: String) {
        scope.launch { context.dataStore.edit { it[KEY_SELLER_INSTAGRAM] = value.trim() } }
    }

    fun setStoreLogoPath(value: String) {
        scope.launch { context.dataStore.edit { it[KEY_STORE_LOGO_PATH] = value.trim() } }
    }

    /**
     * Llama esto después de crear/editar/eliminar productos,
     * o desde "Sincronizar ahora".
     */
    fun scheduleSyncNowOrDebounced(debounced: Boolean = true) {
        scope.launch {
            val client = clientNameValue()
            val shared = sharedFolderIdValue().ifBlank { null }

            if (debounced) {
                container.workScheduler.scheduleDebouncedSync(clientName = client, sharedFolderId = shared)
            } else {
                container.workScheduler.scheduleImmediateSync(clientName = client, sharedFolderId = shared)
            }
        }
    }}
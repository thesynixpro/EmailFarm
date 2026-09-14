package com.dotmail.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.IntentSenderRequest
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.ClearTokenRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.RevokeAccessRequest
import com.google.android.gms.common.api.Scope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

private val Context.dotMailDataStore by preferencesDataStore(name = "dotmail_preferences")

private data class GenerationSession(
    val id: Long,
    val source: String,
    val count: Int,
    val totalPossible: Long,
    val createdAt: Long,
    val variations: List<String>
)

private data class GmailMessage(
    val id: String,
    val threadId: String,
    val sender: String,
    val subject: String,
    val date: String,
    val snippet: String
)

private const val GMAIL_READONLY_SCOPE = "https://www.googleapis.com/auth/gmail.readonly"
private val GMAIL_SCOPES = listOf(Scope(GMAIL_READONLY_SCOPE))

private data class GmailInboxResult(
    val email: String,
    val messages: List<GmailMessage>
)

private data class DotMailUiState(
    val input: String = "",
    val sourceEmail: String = "",
    val variations: List<String> = emptyList(),
    val totalPossible: Long = 0L,
    val selected: Set<String> = emptySet(),
    val favorites: Set<String> = emptySet(),
    val history: List<GenerationSession> = emptyList(),
    val searchQuery: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val includeOriginal: Boolean = true,
    val maxResults: Int = 256,
    val confirmLarge: Boolean = true,
    val appearance: String = "system",
    val gmailConnectedEmail: String? = null,
    val gmailMessages: List<GmailMessage> = emptyList(),
    val gmailLoading: Boolean = false,
    val gmailError: String? = null,
    val gmailAccessToken: String? = null
)

object GmailDotGenerator {
    data class Result(val values: List<String>, val totalPossible: Long)

    private val validEmail = Regex("^[A-Za-z0-9.]+@gmail\\.com$", RegexOption.IGNORE_CASE)

    fun generate(address: String, includeOriginal: Boolean, maxResults: Int): Result {
        val normalized = address.trim().lowercase(Locale.US)
        require(validEmail.matches(normalized)) { "Enter a valid @gmail.com address." }

        val rawUsername = normalized.substringBefore('@')
        require(rawUsername.isNotEmpty()) { "Your Gmail username is empty." }
        require(!rawUsername.startsWith('.') && !rawUsername.endsWith('.') && !rawUsername.contains("..")) {
            "The username cannot contain leading, trailing, or consecutive dots."
        }

        val username = rawUsername.replace(".", "")
        require(username.length in 1..31) { "Use a username with 31 characters or fewer for safe generation." }

        val slots = username.length - 1
        val totalPossible = 1L shl slots
        val limit = maxResults.coerceIn(1, 4096)
        val startMask = if (includeOriginal) 0L else 1L
        val endMaskExclusive = minOf(totalPossible, startMask + limit)
        val results = ArrayList<String>(minOf(limit.toLong(), totalPossible).toInt())

        var mask = startMask
        while (mask < endMaskExclusive) {
            val builder = StringBuilder(username.length + slots + 10)
            username.forEachIndexed { index, ch ->
                builder.append(ch)
                if (index < slots && (mask and (1L shl index)) != 0L) builder.append('.')
            }
            results += builder.append("@gmail.com").toString()
            mask++
        }
        return Result(results.distinct(), totalPossible)
    }
}

private class DotMailViewModel(private val context: Context) : ViewModel() {
    private val _state = MutableStateFlow(DotMailUiState())
    val state: StateFlow<DotMailUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val prefs = context.dotMailDataStore.data.first()
            val favorites = prefs[stringSetPreferencesKey("favorites")] ?: emptySet()
            val appearance = prefs[stringPreferencesKey("appearance")] ?: "system"
            val maxResults = prefs[intPreferencesKey("max_results")] ?: 256
            val includeOriginal = prefs[booleanPreferencesKey("include_original")] ?: true
            val confirmLarge = prefs[booleanPreferencesKey("confirm_large")] ?: true
            _state.value = _state.value.copy(
                favorites = favorites,
                appearance = appearance,
                maxResults = maxResults,
                includeOriginal = includeOriginal,
                confirmLarge = confirmLarge,
                history = readHistory(prefs[stringPreferencesKey("history")])
            )
        }
    }

    fun setInput(value: String) = _state.update { it.copy(input = value, error = null) }
    fun setSearch(value: String) = _state.update { it.copy(searchQuery = value) }

    fun generate(onFinished: () -> Unit = {}) {
        val current = _state.value
        viewModelScope.launch {
            _state.value = current.copy(loading = true, error = null, selected = emptySet())
            runCatching { GmailDotGenerator.generate(current.input, current.includeOriginal, current.maxResults) }
                .onSuccess { result ->
                    val now = System.currentTimeMillis()
                    val session = GenerationSession(now, current.input.trim().lowercase(Locale.US), result.values.size, result.totalPossible, now, result.values)
                    saveHistory(session)
                    _state.value = _state.value.copy(
                        loading = false,
                        sourceEmail = session.source,
                        variations = result.values,
                        totalPossible = result.totalPossible,
                        searchQuery = ""
                    )
                    onFinished()
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(loading = false, error = error.message ?: "Unable to generate variations.")
                }
        }
    }

    fun toggleFavorite(email: String) {
        viewModelScope.launch {
            context.dotMailDataStore.edit { prefs ->
                val key = stringSetPreferencesKey("favorites")
                val values = (prefs[key] ?: emptySet()).toMutableSet()
                if (!values.add(email)) values.remove(email)
                prefs[key] = values
            }
            _state.update { current ->
                current.copy(favorites = if (email in current.favorites) current.favorites - email else current.favorites + email)
            }
        }
    }

    fun toggleSelected(email: String) = _state.update { current ->
        val values = current.selected.toMutableSet()
        if (!values.add(email)) values.remove(email)
        current.copy(selected = values)
    }

    fun selectAll() = _state.update { current -> current.copy(selected = filteredVariations(current).toSet()) }
    fun clearSelection() = _state.update { it.copy(selected = emptySet()) }

    fun deleteSelected() = _state.update { current ->
        current.copy(variations = current.variations.filterNot { it in current.selected }, selected = emptySet())
    }

    fun saveSelected() {
        val selected = _state.value.selected
        if (selected.isEmpty()) return
        viewModelScope.launch {
            context.dotMailDataStore.edit { prefs ->
                val key = stringSetPreferencesKey("favorites")
                val values = (prefs[key] ?: emptySet()).toMutableSet()
                values.addAll(selected)
                prefs[key] = values
            }
            _state.update { it.copy(favorites = it.favorites + selected, selected = emptySet()) }
        }
    }

    fun connectGmail(accessToken: String) {
        if (accessToken.isBlank()) {
            _state.update { it.copy(gmailError = "Google did not return an access token.") }
            return
        }
        _state.update { it.copy(gmailAccessToken = accessToken, gmailLoading = true, gmailError = null) }
        viewModelScope.launch {
            runCatching { withContext(Dispatchers.IO) { GmailApi.loadInbox(accessToken) } }
                .onSuccess { result ->
                    _state.update { it.copy(gmailConnectedEmail = result.email, gmailMessages = result.messages, gmailLoading = false, gmailError = null) }
                }
                .onFailure { error ->
                    _state.update { it.copy(gmailLoading = false, gmailError = error.message ?: "Unable to load Gmail inbox.") }
                }
        }
    }

    fun refreshGmailInbox() {
        val token = _state.value.gmailAccessToken ?: return
        _state.update { it.copy(gmailLoading = true, gmailError = null) }
        viewModelScope.launch {
            runCatching { withContext(Dispatchers.IO) { GmailApi.loadInbox(token) } }
                .onSuccess { result ->
                    _state.update { it.copy(gmailConnectedEmail = result.email, gmailMessages = result.messages, gmailLoading = false, gmailError = null) }
                }
                .onFailure { error ->
                    _state.update { it.copy(gmailLoading = false, gmailError = error.message ?: "Unable to refresh Gmail inbox.") }
                }
        }
    }

    fun disconnectGmail() {
        _state.update { it.copy(gmailConnectedEmail = null, gmailMessages = emptyList(), gmailLoading = false, gmailError = null, gmailAccessToken = null) }
    }

    fun updateAppearance(value: String) {
        val normalized = value.lowercase(Locale.US).let { if (it in setOf("system", "light", "dark")) it else "system" }
        _state.update { it.copy(appearance = normalized) }
        updatePref { prefs -> prefs[stringPreferencesKey("appearance")] = normalized }
    }

    fun updateIncludeOriginal(value: Boolean) {
        _state.update { it.copy(includeOriginal = value) }
        updatePref { prefs -> prefs[booleanPreferencesKey("include_original")] = value }
    }

    fun updateMaxResults(value: Int) {
        val normalized = value.coerceIn(32, 1024)
        _state.update { it.copy(maxResults = normalized) }
        updatePref { prefs -> prefs[intPreferencesKey("max_results")] = normalized }
    }

    fun updateConfirmLarge(value: Boolean) {
        _state.update { it.copy(confirmLarge = value) }
        updatePref { prefs -> prefs[booleanPreferencesKey("confirm_large")] = value }
    }

    fun loadHistory() = viewModelScope.launch {
        val prefs = context.dotMailDataStore.data.first()
        _state.update { it.copy(history = readHistory(prefs[stringPreferencesKey("history")])) }
    }

    fun openHistory(session: GenerationSession) = _state.update {
        it.copy(sourceEmail = session.source, variations = session.variations, totalPossible = session.totalPossible, selected = emptySet(), searchQuery = "", error = null)
    }

    fun deleteHistory(id: Long) {
        viewModelScope.launch {
            val remaining = _state.value.history.filterNot { it.id == id }
            saveHistoryList(remaining)
            _state.update { it.copy(history = remaining) }
        }
    }

    fun filteredVariations(): List<String> = filteredVariations(_state.value)

    private fun filteredVariations(current: DotMailUiState): List<String> {
        val q = current.searchQuery.trim().lowercase(Locale.US)
        return if (q.isEmpty()) current.variations else current.variations.filter { it.contains(q) }
    }

    private fun updatePref(block: suspend (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        viewModelScope.launch { context.dotMailDataStore.edit { prefs -> block(prefs) } }
    }

    private suspend fun saveHistory(session: GenerationSession) {
        val existing = _state.value.history.filterNot { it.source == session.source }.take(19)
        saveHistoryList(listOf(session) + existing)
        _state.update { it.copy(history = listOf(session) + existing) }
    }

    private suspend fun saveHistoryList(list: List<GenerationSession>) {
        val encoded = list.joinToString("\n") { session ->
            val values = session.variations.joinToString(";").replace("%", "%25").replace("|", "%7C").replace("\n", "%0A")
            listOf(session.id, session.source, session.count, session.totalPossible, session.createdAt, values).joinToString("|")
        }
        context.dotMailDataStore.edit { prefs -> prefs[stringPreferencesKey("history")] = encoded }
    }

    private fun readHistory(raw: String?): List<GenerationSession> {
        if (raw.isNullOrBlank()) return emptyList()
        return raw.lines().mapNotNull { line ->
            val parts = line.split('|', limit = 6)
            if (parts.size != 6) return@mapNotNull null
            runCatching {
                val emails = parts[5].split(';').filter { it.isNotBlank() }.map {
                    it.replace("%0A", "\n").replace("%7C", "|").replace("%25", "%")
                }
                GenerationSession(parts[0].toLong(), parts[1], parts[2].toInt(), parts[3].toLong(), parts[4].toLong(), emails)
            }.getOrNull()
        }
    }
}

private object GmailApi {
    fun loadInbox(accessToken: String): GmailInboxResult {
        val profile = requestJson("https://gmail.googleapis.com/gmail/v1/users/me/profile", accessToken)
        val email = profile.optString("emailAddress").ifBlank { "Connected Gmail" }
        val list = requestJson("https://gmail.googleapis.com/gmail/v1/users/me/messages?labelIds=INBOX&maxResults=25", accessToken)
        val ids = list.optJSONArray("messages") ?: return GmailInboxResult(email, emptyList())
        val messages = buildList {
            for (index in 0 until ids.length()) {
                val id = ids.optJSONObject(index)?.optString("id").orEmpty()
                val threadId = ids.optJSONObject(index)?.optString("threadId").orEmpty()
                if (id.isBlank()) continue
                runCatching {
                    val encodedId = java.net.URLEncoder.encode(id, "UTF-8")
                    val details = requestJson(
                        "https://gmail.googleapis.com/gmail/v1/users/me/messages/$encodedId?format=metadata&metadataHeaders=Subject&metadataHeaders=From&metadataHeaders=Date",
                        accessToken
                    )
                    val headers = details.optJSONObject("payload")?.optJSONArray("headers")
                    var subject = "(No subject)"
                    var sender = "Unknown sender"
                    var date = ""
                    if (headers != null) {
                        for (h in 0 until headers.length()) {
                            val header = headers.optJSONObject(h) ?: continue
                            when (header.optString("name").lowercase(Locale.US)) {
                                "subject" -> subject = header.optString("value").ifBlank { "(No subject)" }
                                "from" -> sender = header.optString("value").ifBlank { "Unknown sender" }
                                "date" -> date = header.optString("value")
                            }
                        }
                    }
                    add(GmailMessage(id, threadId, sender, subject, date, details.optString("snippet")))
                }
            }
        }
        return GmailInboxResult(email, messages)
    }

    private fun requestJson(url: String, accessToken: String): JSONObject {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15_000
            readTimeout = 20_000
            setRequestProperty("Authorization", "Bearer $accessToken")
            setRequestProperty("Accept", "application/json")
        }
        return try {
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (status !in 200..299) {
                val reason = runCatching { JSONObject(body).optString("message") }.getOrNull().orEmpty()
                throw IllegalStateException(if (reason.isBlank()) "Gmail API returned HTTP $status." else reason)
            }
            JSONObject(body)
        } finally {
            connection.disconnect()
        }
    }
}

private fun <T> MutableStateFlow<T>.update(transform: (T) -> T) { value = transform(value) }

private class DotMailVmFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return DotMailViewModel(context.applicationContext) as T
    }
}

private data class NavItem(val route: String, val label: String, val icon: ImageVector, val selectedIcon: ImageVector)

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun DotMailApp() {
    val context = LocalContext.current
    val activity = context as? MainActivity
    val vm: DotMailViewModel = viewModel(factory = DotMailVmFactory(context))
    val state by vm.state.collectAsState()
    val navController = rememberNavController()
    val darkTheme = when (state.appearance) {
        "dark" -> true
        "light" -> false
        else -> androidx.compose.foundation.isSystemInDarkTheme()
    }

    DotMailTheme(darkTheme) {
        Surface(Modifier.fillMaxSize()) {
            val snackbarHost = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHost) },
                bottomBar = { BottomBar(navController) }
            ) { padding ->
                NavHost(navController, startDestination = "generate", modifier = Modifier.padding(padding)) {
                    composable("generate") { GenerateScreen(state, vm, navController, snackbarHost, scope) }
                    composable("results") { ResultsScreen(state, vm, navController, snackbarHost, scope) }
                    composable("saved") { SavedScreen(state, vm, snackbarHost, scope) }
                    composable("inbox") { GmailInboxScreen(state, vm, onConnect = { activity?.requestGmailAccess() }, onDisconnect = { activity?.disconnectGmail() }) }
                    composable("history") { HistoryScreen(state, vm, navController) }
                    composable("settings") { SettingsScreen(state, vm) }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun BottomBar(nav: NavHostController) {
    val current by nav.currentBackStackEntryAsState()
    val route = current?.destination?.route
    val items = listOf(
        NavItem("generate", "Generate", Icons.Outlined.Email, Icons.Filled.Email),
        NavItem("saved", "Saved", Icons.Outlined.StarBorder, Icons.Filled.Star),
        NavItem("inbox", "Inbox", Icons.Outlined.Email, Icons.Filled.Inbox),
        NavItem("history", "History", Icons.Outlined.History, Icons.Filled.History),
        NavItem("settings", "Settings", Icons.Outlined.Settings, Icons.Filled.Settings)
    )
    NavigationBar {
        items.forEach { item ->
            val selected = route == item.route
            NavigationBarItem(
                selected = selected,
                onClick = { nav.navigate(item.route) { launchSingleTop = true } },
                icon = { Icon(if (selected) item.selectedIcon else item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun GenerateScreen(
    state: DotMailUiState,
    vm: DotMailViewModel,
    nav: NavHostController,
    snackbar: SnackbarHostState,
    scope: CoroutineScope
) {
    var confirm by remember { mutableStateOf(false) }
    val valid = Regex("^[A-Za-z0-9.]+@gmail\\.com$", RegexOption.IGNORE_CASE).matches(state.input.trim()) && !state.input.trim().startsWith('.') && !state.input.trim().endsWith('.') && !state.input.contains("..")

    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                DotMailLogo(56.dp)
                Spacer(Modifier.width(14.dp))
                Column {
                    Text("DotMail", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("Generate Gmail dot variations", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(52.dp)) {
                            Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.Email, null, tint = MaterialTheme.colorScheme.onPrimary) }
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Generate Gmail variations", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("Fast, local, and private.", color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                    Text("Enter your Gmail address and instantly generate possible dot variations.")
                }
            }
        }
        item {
            OutlinedTextField(
                value = state.input,
                onValueChange = vm::setInput,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Gmail address") },
                placeholder = { Text("mymail@gmail.com") },
                leadingIcon = { Icon(Icons.Filled.Email, null) },
                trailingIcon = {
                    if (state.input.isNotEmpty()) IconButton(onClick = { vm.setInput("") }) { Icon(Icons.Filled.Clear, "Clear") }
                },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                isError = state.input.isNotBlank() && !valid,
                supportingText = { Text(if (state.input.isBlank() || valid) "No password or Google login is required." else "Enter a valid @gmail.com address.") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
        }
        item {
            Button(
                onClick = {
                    if (state.maxResults > 256 && state.confirmLarge) confirm = true
                    else vm.generate { nav.navigate("results") }
                },
                enabled = valid && !state.loading,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(18.dp)
            ) { Text(if (state.loading) "Generating…" else "Generate Variations") }
        }
        if (state.error != null) {
            item { Text(state.error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) }
        }
        item { PrivacyCard() }
    }

    if (confirm) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            title = { Text("Generate many variations?") },
            text = { Text("This request can generate up to ${state.maxResults} addresses. A shorter limit keeps the list easier to browse.") },
            confirmButton = {
                TextButton(onClick = { confirm = false; vm.generate { nav.navigate("results") } }) { Text("Generate") }
            },
            dismissButton = { TextButton(onClick = { confirm = false }) { Text("Cancel") } }
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun PrivacyCard() {
    Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("About Gmail dots", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("Dot variations keep the same character order and differ only by dots inserted between username characters. They are variations of the same Gmail mailbox, not separate Gmail accounts.")
            Text("Everything is processed locally on this device. DotMail does not request a Gmail password, sign-in, account creation, CAPTCHA bypass, or verification automation.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ResultsScreen(state: DotMailUiState, vm: DotMailViewModel, nav: NavHostController, snackbar: SnackbarHostState, scope: CoroutineScope) {
    val context = LocalContext.current
    val filtered = vm.filteredVariations()
    var search by remember { mutableStateOf(false) }
    var menu by remember { mutableStateOf(false) }

    if (state.selected.isNotEmpty()) {
        TopAppBar(
            title = { Text("${state.selected.size} selected", fontWeight = FontWeight.Bold) },
            navigationIcon = { IconButton(onClick = vm::clearSelection) { Icon(Icons.Filled.ArrowBack, "Exit selection") } },
            actions = {
                TextButton(onClick = vm::selectAll) { Text("All") }
                IconButton(onClick = { copySelected(state.selected, context); scope.launch { snackbar.showSnackbar("Copied ${state.selected.size} addresses ✓") }; vm.clearSelection() }) { Icon(Icons.Filled.ContentCopy, "Copy selected") }
                IconButton(onClick = vm::saveSelected) { Icon(Icons.Filled.Star, "Save selected") }
                IconButton(onClick = vm::deleteSelected) { Icon(Icons.Filled.Delete, "Delete selected") }
            }
        )
    } else {
        TopAppBar(
            title = { Text("${filtered.size} Variations", fontWeight = FontWeight.Bold) },
            navigationIcon = { IconButton(onClick = { nav.navigateUp() }) { Icon(Icons.Filled.ArrowBack, "Back") } },
            actions = {
                IconButton(onClick = { search = !search }) { Icon(Icons.Filled.Search, "Search") }
                IconButton(onClick = vm::selectAll) { Icon(Icons.Filled.Star, "Select all") }
                IconButton(onClick = { copySelected(filtered.toSet(), context); scope.launch { snackbar.showSnackbar("Copied ${filtered.size} addresses ✓") } }) { Icon(Icons.Filled.ContentCopy, "Copy all") }
                IconButton(onClick = { menu = true }) { Icon(Icons.Filled.MoreVert, "More") }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    DropdownMenuItem(text = { Text("Select all") }, onClick = { menu = false; vm.selectAll() })
                    DropdownMenuItem(text = { Text("Clear selection") }, onClick = { menu = false; vm.clearSelection() })
                }
            }
        )
    }

    Column(Modifier.fillMaxSize()) {
        if (search && state.selected.isEmpty()) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = vm::setSearch,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                placeholder = { Text("Search variations") }
            )
        }
        if (state.sourceEmail.isNotEmpty()) {
            Text("For ${state.sourceEmail}", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filtered, key = { it }) { email ->
                VariationCard(email, email in state.selected, email in state.favorites, vm, snackbar, scope)
            }
            item {
                Spacer(Modifier.height(8.dp))
                Text("${state.totalPossible} possible dot-placement patterns for this username.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun VariationCard(email: String, selected: Boolean, favorite: Boolean, vm: DotMailViewModel, snackbar: SnackbarHostState, scope: CoroutineScope) {
    val context = LocalContext.current
    var menu by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (selected) "✓" else "•", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(10.dp))
                Text(email, Modifier.weight(1f), fontWeight = FontWeight.Medium)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = { copyText(email, context); scope.launch { snackbar.showSnackbar("Copied ✓") } }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.ContentCopy, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Copy")
                }
                IconButton(onClick = { vm.toggleFavorite(email) }) { Icon(if (favorite) Icons.Filled.Star else Icons.Outlined.StarBorder, "Favorite") }
                Box {
                    IconButton(onClick = { menu = true }) { Icon(Icons.Filled.MoreVert, "More") }
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                        DropdownMenuItem(text = { Text(if (selected) "Unselect" else "Select") }, onClick = { menu = false; vm.toggleSelected(email) })
                        DropdownMenuItem(text = { Text(if (favorite) "Remove favorite" else "Save") }, onClick = { menu = false; vm.toggleFavorite(email) })
                    }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SavedScreen(state: DotMailUiState, vm: DotMailViewModel, snackbar: SnackbarHostState, scope: CoroutineScope) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    val saved = state.favorites.filter { it.contains(query, ignoreCase = true) }.sorted()
    Column(Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Saved variations", fontWeight = FontWeight.Bold) })
        OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth().padding(horizontal = 16.dp), singleLine = true, shape = RoundedCornerShape(18.dp), leadingIcon = { Icon(Icons.Filled.Search, null) }, placeholder = { Text("Search saved") })
        if (saved.isEmpty()) {
            EmptyState("No saved variations yet", "Favorite a variation while generating to keep it here.", Icons.Filled.Star)
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(saved, key = { it }) { email ->
                    Card(shape = RoundedCornerShape(20.dp)) {
                        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(email, Modifier.weight(1f), fontWeight = FontWeight.Medium)
                            IconButton(onClick = { copyText(email, context); scope.launch { snackbar.showSnackbar("Copied ✓") } }) { Icon(Icons.Filled.ContentCopy, "Copy") }
                            IconButton(onClick = { vm.toggleFavorite(email) }) { Icon(Icons.Filled.Star, "Remove favorite") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun HistoryScreen(state: DotMailUiState, vm: DotMailViewModel, nav: NavHostController) {
    LaunchedEffect(Unit) { vm.loadHistory() }
    Column(Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("History", fontWeight = FontWeight.Bold) })
        if (state.history.isEmpty()) {
            EmptyState("No generation history", "Your recent generation sessions will appear here.", Icons.Filled.History)
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(state.history, key = { it.id }) { session ->
                    val formatted = remember(session.createdAt) { SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(session.createdAt)) }
                    Card(shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Email, null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(10.dp))
                                Text(session.source, Modifier.weight(1f), fontWeight = FontWeight.Bold)
                                IconButton(onClick = { vm.deleteHistory(session.id) }) { Icon(Icons.Filled.Delete, "Delete history") }
                            }
                            Text("${session.count} variations • $formatted", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            TextButton(onClick = { vm.openHistory(session); nav.navigate("results") }) { Text("Open") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun GmailInboxScreen(
    state: DotMailUiState,
    vm: DotMailViewModel,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Gmail Inbox", fontWeight = FontWeight.Bold) },
            actions = {
                if (!state.gmailConnectedEmail.isNullOrBlank()) {
                    IconButton(onClick = vm::refreshGmailInbox, enabled = !state.gmailLoading) { Icon(Icons.Filled.Refresh, "Refresh inbox") }
                    IconButton(onClick = onDisconnect) { Icon(Icons.Filled.Logout, "Disconnect Gmail") }
                }
            }
        )
        if (state.gmailConnectedEmail.isNullOrBlank()) {
            EmptyState("Connect your Gmail", "Authorize DotMail with read-only Gmail access to see your Inbox here.", Icons.Filled.Inbox)
            Box(Modifier.fillMaxWidth().padding(horizontal = 32.dp), contentAlignment = Alignment.Center) {
                Button(onClick = onConnect) {
                    Icon(Icons.Filled.Link, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Connect Gmail")
                }
            }
        } else {
            Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(state.gmailConnectedEmail, fontWeight = FontWeight.SemiBold)
                Text("Read-only • ${state.gmailMessages.size} recent inbox messages", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
            state.gmailError?.let {
                Text(it, modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.error)
            }
            if (state.gmailLoading && state.gmailMessages.isEmpty()) {
                EmptyState("Loading inbox…", "Fetching your latest messages from Gmail.", Icons.Filled.Refresh)
            } else if (state.gmailMessages.isEmpty()) {
                EmptyState("Inbox is empty", "No messages were returned from the Gmail Inbox label.", Icons.Filled.Inbox)
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(state.gmailMessages, key = { it.id }) { message ->
                        Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Email, null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.width(10.dp))
                                    Text(message.sender, Modifier.weight(1f), fontWeight = FontWeight.SemiBold, maxLines = 1)
                                }
                                Text(message.subject, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 2)
                                if (message.snippet.isNotBlank()) Text(message.snippet, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3)
                                if (message.date.isNotBlank()) Text(message.date, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SettingsScreen(state: DotMailUiState, vm: DotMailViewModel) {
    val context = LocalContext.current
    val activity = context as? MainActivity
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                DotMailLogo(48.dp)
                Spacer(Modifier.width(12.dp))
                Text("Settings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            }
        }
        item {
            SettingsCard("Appearance") {
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    val choices = listOf("system" to "System", "light" to "Light", "dark" to "Dark")
                    choices.forEachIndexed { index, (value, label) ->
                        SegmentedButton(selected = state.appearance == value, onClick = { vm.updateAppearance(value) }, shape = SegmentedButtonDefaults.itemShape(index, choices.size)) { Text(label) }
                    }
                }
            }
        }
        item {
            SettingsCard("Generation") {
                SettingSwitch("Include original", "Include the no-dot address in results.", state.includeOriginal, vm::updateIncludeOriginal)
                Text("Maximum results", fontWeight = FontWeight.SemiBold)
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    val choices = listOf(32, 128, 256, 1024)
                    choices.forEachIndexed { index, value ->
                        SegmentedButton(selected = state.maxResults == value, onClick = { vm.updateMaxResults(value) }, shape = SegmentedButtonDefaults.itemShape(index, choices.size)) { Text(if (value == 1024) "1K" else value.toString()) }
                    }
                }
                SettingSwitch("Confirm large generations", "Ask before generating more than 256 results.", state.confirmLarge, vm::updateConfirmLarge)
            }
        }
        item {
            SettingsCard("Gmail inbox") {
                if (state.gmailConnectedEmail.isNullOrBlank()) {
                    Text("Connect your Gmail account with Google's secure authorization flow. DotMail requests read-only inbox access and never asks for your Gmail password.")
                    Button(onClick = { activity?.requestGmailAccess() }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Filled.Link, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Connect Gmail")
                    }
                } else {
                    Text("Connected as ${state.gmailConnectedEmail}", fontWeight = FontWeight.SemiBold)
                    Text("DotMail can show the latest messages from your Gmail Inbox in the Inbox tab. Messages are loaded on demand and are not uploaded to a DotMail server.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = vm::refreshGmailInbox, enabled = !state.gmailLoading) {
                            Icon(Icons.Filled.Refresh, null)
                            Spacer(Modifier.width(6.dp))
                            Text("Refresh")
                        }
                        OutlinedButton(onClick = { activity?.disconnectGmail() }) {
                            Icon(Icons.Filled.Logout, null)
                            Spacer(Modifier.width(6.dp))
                            Text("Disconnect")
                        }
                    }
                    state.gmailError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            }
        }
        item {
            SettingsCard("Privacy") {
                Text("Generated Gmail variations are processed locally. When you connect Gmail, DotMail uses Google OAuth and the Gmail read-only scope to load your Inbox directly from Google. Your Gmail password is never entered into DotMail.")
            }
        }
        item {
            SettingsCard("Gmail rules") {
                Text("Dots are inserted only between username characters. No leading, trailing, or consecutive dots are generated. Character order is preserved and duplicates are removed.")
            }
        }
    }
}

@Composable
private fun SettingSwitch(title: String, body: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun SettingsCard(title: String, content: @Composable () -> Unit) {
    Card(shape = RoundedCornerShape(24.dp)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Divider()
            content()
        }
    }
}

@Composable
private fun DotMailLogo(size: androidx.compose.ui.unit.Dp) {
    Surface(
        modifier = Modifier.size(size),
        shape = RoundedCornerShape(size / 3),
        color = MaterialTheme.colorScheme.primary,
        tonalElevation = 2.dp
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().padding(size * 0.16f)) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(size / 4),
                color = MaterialTheme.colorScheme.surface
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Email,
                        contentDescription = "DotMail logo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxSize().padding(size * 0.18f)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState(title: String, body: String, icon: ImageVector) {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(icon, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun copyText(text: String, context: Context) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("DotMail", text))
}

private fun copySelected(values: Set<String>, context: Context) {
    if (values.isEmpty()) return
    copyText(values.joinToString("\n"), context)
}

@Composable
private fun DotMailTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val fallback = if (darkTheme) androidx.compose.material3.darkColorScheme() else androidx.compose.material3.lightColorScheme()
    val colors = if (android.os.Build.VERSION.SDK_INT >= 31) {
        if (darkTheme) androidx.compose.material3.dynamicDarkColorScheme(context) else androidx.compose.material3.dynamicLightColorScheme(context)
    } else fallback
    MaterialTheme(colorScheme = colors, content = content)
}

class MainActivity : ComponentActivity() {
    private val authorizationClient by lazy { Identity.getAuthorizationClient(this) }

    private val gmailAuthorizationLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode != RESULT_OK || result.data == null) return@registerForActivityResult
        runCatching {
            authorizationClient.getAuthorizationResultFromIntent(result.data)
        }.onSuccess { authorizationResult ->
            handleAuthorizationResult(authorizationResult)
        }.onFailure { error ->
            findViewById<android.view.View>(android.R.id.content)?.let { view ->
                android.widget.Toast.makeText(this, error.message ?: "Google authorization failed.", android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { DotMailApp() }
    }

    fun requestGmailAccess() {
        val request = AuthorizationRequest.builder()
            .setRequestedScopes(GMAIL_SCOPES)
            .build()
        authorizationClient.authorize(request)
            .addOnSuccessListener { result ->
                if (result.hasResolution()) {
                    val pendingIntent = result.pendingIntent ?: return@addOnSuccessListener
                    val request = IntentSenderRequest.Builder(pendingIntent.intentSender).build()
                    gmailAuthorizationLauncher.launch(request)
                } else {
                    handleAuthorizationResult(result)
                }
            }
            .addOnFailureListener { error ->
                android.widget.Toast.makeText(this, error.message ?: "Unable to start Google authorization.", android.widget.Toast.LENGTH_LONG).show()
            }
    }

    private fun handleAuthorizationResult(result: AuthorizationResult) {
        val token = result.accessToken
        if (token.isNullOrBlank()) {
            android.widget.Toast.makeText(this, "Gmail permission was not granted.", android.widget.Toast.LENGTH_LONG).show()
            return
        }
        val vm = androidx.lifecycle.ViewModelProvider(this, DotMailVmFactory(applicationContext))[DotMailViewModel::class.java]
        vm.connectGmail(token)
    }

    fun disconnectGmail() {
        val vm = androidx.lifecycle.ViewModelProvider(this, DotMailVmFactory(applicationContext))[DotMailViewModel::class.java]
        val token = vm.state.value.gmailAccessToken
        if (!token.isNullOrBlank()) {
            authorizationClient.clearToken(ClearTokenRequest.builder().setToken(token).build())
            authorizationClient.revokeAccess(RevokeAccessRequest.builder().setScopes(GMAIL_SCOPES).build())
        }
        vm.disconnectGmail()
    }
}

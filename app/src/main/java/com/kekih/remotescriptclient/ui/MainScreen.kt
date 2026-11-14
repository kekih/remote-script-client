package com.kekih.remotescriptclient.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kekih.remotescriptclient.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(vm: MainViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    Scaffold(
        containerColor = Ink,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("VixHub", fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(
                            when (state.section) {
                                Section.Home -> "Клиент сервера"
                                Section.Files -> "Файлы"
                                Section.Bases -> "Базы данных"
                                Section.Scripts -> "Скрипты"
                                Section.Sites -> "Сайты"
                                Section.Server -> "Обновление сервера"
                            },
                            style = MaterialTheme.typography.bodySmall, color = TextSecondary
                        )
                    }
                },
                navigationIcon = {
                    if (state.section != Section.Home) {
                        IconButton(onClick = { vm.go(Section.Home) }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = TextPrimary)
                        }
                    } else {
                        IconButton(onClick = { vm.openServerMenu() }) {
                            Icon(Icons.Default.Dns, "Серверы", tint = Cyan)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { vm.checkHealth() }) {
                        Icon(Icons.Default.Refresh, null, tint = TextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (state.section) {
                Section.Home -> HomeSection(state, vm)
                Section.Files -> FilesSection(state, vm)
                Section.Bases -> BasesSection(state, vm)
                Section.Scripts -> ScriptsSection(state, vm)
                Section.Sites -> SitesSection(state, vm)
                Section.Server -> ServerUpdateSection(state)
            }
            if (state.showServerMenu) ServerMenuDialog(state, vm)
        }
    }
}

@Composable
private fun HomeSection(state: UiState, vm: MainViewModel) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            colors = CardDefaults.cardColors(containerColor = when (state.healthOk) {
                true -> SuccessGreen.copy(alpha = 0.15f)
                false -> ErrorRed.copy(alpha = 0.2f)
                null -> CardDark
            }),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(if (state.healthOk == true) Icons.Default.CheckCircle else Icons.Default.Error, null,
                    tint = if (state.healthOk == true) SuccessGreen else ErrorRed)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(if (state.healthOk == true) "Онлайн" else "Проверка health...", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Text(state.activeServer?.url ?: "Нет сервера", style = MaterialTheme.typography.bodySmall, color = TextSecondary, maxLines = 1)
                    state.healthInfo?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = TextSecondary) }
                }
            }
        }
        Text("Разделы", color = TextSecondary, fontWeight = FontWeight.Medium)
        val items = listOf(
            Triple(Section.Files, "Файлы", Icons.Default.Folder),
            Triple(Section.Bases, "Базы", Icons.Default.Storage),
            Triple(Section.Scripts, "Скрипты", Icons.Default.Code),
            Triple(Section.Sites, "Сайты", Icons.Default.Language),
            Triple(Section.Server, "Сервер", Icons.Default.CloudUpload),
        )
        LazyVerticalGrid(columns = GridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.height(360.dp)) {
            items(items) { (sec, title, icon) ->
                Card(onClick = {
                    vm.go(sec)
                    when (sec) {
                        Section.Files -> vm.loadFiles()
                        Section.Bases -> vm.loadDatabases()
                        Section.Scripts -> vm.loadScripts()
                        Section.Sites -> vm.loadSites()
                        else -> {}
                    }
                }, colors = CardDefaults.cardColors(containerColor = CardDark), shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.aspectRatio(1.1f)) {
                    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(icon, null, tint = Cyan, modifier = Modifier.size(36.dp))
                        Spacer(Modifier.height(10.dp))
                        Text(title, color = TextPrimary, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
        state.error?.let { Text(it, color = ErrorRed, style = MaterialTheme.typography.bodySmall) }
    }
}

@Composable
private fun ServerMenuDialog(state: UiState, vm: MainViewModel) {
    AlertDialog(onDismissRequest = { vm.closeServerMenu() }, containerColor = SurfaceDark,
        title = { Text("Серверы", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.servers.forEach { s ->
                    Row(Modifier.fillMaxWidth().clickable { vm.selectServer(s.id) }
                        .background(if (s.id == state.activeServer?.id) Cyan.copy(0.15f) else Color.Transparent, RoundedCornerShape(8.dp))
                        .padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(s.name, color = TextPrimary, fontWeight = FontWeight.Medium)
                            Text(s.url, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = { vm.deleteServer(s.id) }) { Icon(Icons.Default.Delete, null, tint = ErrorRed) }
                    }
                }
                if (state.showAddServer) {
                    var name by remember { mutableStateOf("") }
                    var url by remember { mutableStateOf("http://") }
                    var key by remember { mutableStateOf("") }
                    OutlinedTextField(name, { name = it }, label = { Text("Имя") }, singleLine = true)
                    OutlinedTextField(url, { url = it }, label = { Text("URL") }, singleLine = true)
                    OutlinedTextField(key, { key = it }, label = { Text("API Key") }, singleLine = true)
                    Button(onClick = { vm.addServer(name, url, key) }, modifier = Modifier.fillMaxWidth()) { Text("Добавить") }
                } else {
                    TextButton(onClick = { vm.openAddServer() }) {
                        Icon(Icons.Default.Add, null); Spacer(Modifier.width(6.dp)); Text("Добавить сервер")
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { vm.closeServerMenu() }) { Text("Закрыть") } }
    )
}

@Composable
private fun FilesSection(state: UiState, vm: MainViewModel) {
    var newFolder by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { vm.mkdir(newFolder); newFolder = "" }) {
                Icon(Icons.Default.CreateNewFolder, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Создать")
            }
            OutlinedButton(onClick = { vm.loadFiles() }) {
                Icon(Icons.Default.Refresh, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Обновить")
            }
        }
        OutlinedTextField(newFolder, { newFolder = it }, label = { Text("Имя папки") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        if (state.files.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.FolderOff, null, tint = TextSecondary, modifier = Modifier.size(48.dp))
                    Text("Нет папок", color = TextSecondary)
                }
            }
        } else {
            state.files.forEach { item ->
                Card(colors = CardDefaults.cardColors(containerColor = CardDark), shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (item.is_dir) Icons.Default.Folder else Icons.Default.InsertDriveFile, null, tint = Cyan)
                        Spacer(Modifier.width(10.dp))
                        Text(item.name, color = TextPrimary, modifier = Modifier.weight(1f))
                        IconButton(onClick = { vm.deletePath(item.name) }) { Icon(Icons.Default.Delete, null, tint = ErrorRed) }
                    }
                }
            }
        }
    }
}

@Composable
private fun BasesSection(state: UiState, vm: MainViewModel) {
    var merge by remember { mutableStateOf(false) }
    var force by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(state.dbName, { vm.updateDbFields(name = it) }, label = { Text("Имя базы") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(state.dbPath, { vm.updateDbFields(path = it) }, label = { Text("Путь") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(state.dbValue, { vm.updateDbFields(value = it) }, label = { Text("Значение (JSON)") },
            modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
            textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 13.sp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(merge, { merge = it }); Text("Merge", color = TextPrimary) }
            Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(force, { force = it }); Text("Force", color = TextPrimary) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { vm.readDb() }, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Download, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Читать")
            }
            Button(onClick = { vm.writeDb(merge, force) }, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Upload, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Записать")
            }
        }
    }
}

@Composable
private fun ScriptsSection(state: UiState, vm: MainViewModel) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(state.scriptId, { vm.setScriptId(it) }, label = { Text("ID скрипта") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { vm.startScript() }) { Icon(Icons.Default.PlayArrow, null, Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text("Старт") }
            OutlinedButton(onClick = { vm.stopScript() }) { Icon(Icons.Default.Stop, null, Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text("Стоп") }
        }
        Button(onClick = { vm.fetchLogs() }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.List, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Получить логи")
        }
        Card(colors = CardDefaults.cardColors(containerColor = CardDark), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().heightIn(min = 160.dp)) {
            Column(Modifier.padding(12.dp)) {
                Text("Логи", color = TextSecondary, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(6.dp))
                Text(state.scriptLogs.ifBlank { "Логи появятся здесь..." }, color = TextPrimary, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
            }
        }
        if (state.scripts.isNotEmpty()) {
            Text("Загруженные:", color = TextSecondary)
            state.scripts.forEach { s ->
                Text("${s.id} ${if (s.running) "● running" else ""}", color = if (s.running) SuccessGreen else TextPrimary)
            }
        }
    }
}

@Composable
private fun SitesSection(state: UiState, vm: MainViewModel) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Загрузите ZIP или файлы (нужен index.html).", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        if (state.sites.isEmpty()) Text("Пока нет сохранённых сайтов", color = TextSecondary)
        else state.sites.forEach { site ->
            Card(colors = CardDefaults.cardColors(containerColor = CardDark), shape = RoundedCornerShape(10.dp)) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Language, null, tint = Cyan); Spacer(Modifier.width(10.dp))
                    Column {
                        Text(site.id, color = TextPrimary, fontWeight = FontWeight.Medium)
                        Text(if (site.has_index) "index.html ✓" else "нет index.html", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun ServerUpdateSection(state: UiState) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Обновление server.py", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 18.sp)
        Text("POST /api/v1/update_server?secret=... Сервер проверяет compile(), делает .bak и перезапускается.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        OutlinedTextField("", {}, label = { Text("Secret-код") }, singleLine = true, modifier = Modifier.fillMaxWidth(), enabled = false)
        Card(colors = CardDefaults.cardColors(containerColor = CardDark), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Файл server.py", color = TextPrimary, fontWeight = FontWeight.Medium)
                Text("Файл не выбран", color = TextSecondary)
                OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.AttachFile, null); Spacer(Modifier.width(6.dp)); Text("Выбрать .py")
                }
            }
        }
        Button(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.CloudUpload, null); Spacer(Modifier.width(6.dp)); Text("Отправить и перезапустить")
        }
        Text("Активный сервер: ${state.activeServer?.url ?: "—"}", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
    }
}

package com.xayah.databackup.ui.activity.list.components.content

import android.content.Context
import android.content.Intent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import com.xayah.databackup.R
import com.xayah.databackup.data.*
import com.xayah.databackup.ui.activity.list.ListViewModel
import com.xayah.databackup.ui.activity.list.components.*
import com.xayah.databackup.ui.activity.processing.ProcessingActivity
import com.xayah.databackup.util.*
import kotlinx.coroutines.launch
import java.text.Collator
import java.util.*

@ExperimentalAnimationApi
@ExperimentalFoundationApi
@ExperimentalMaterial3Api
fun LazyListScope.contentAppRestore(
    list: List<AppInfoRestore>,
    onSearch: (String) -> Unit,
    onItemUpdate: () -> Unit
) {
    item {
        SearchBar(onSearch)
    }
    items(
        count = list.size,
        key = {
            list[it].detailBase.packageName
        }) { index ->
        AppRestoreItem(
            modifier = Modifier.animateItemPlacement(),
            appInfoRestore = list[index],
            onItemUpdate = onItemUpdate
        )
    }
}

suspend fun onAppRestoreInitialize(viewModel: ListViewModel) {
    if (GlobalObject.getInstance().appInfoRestoreMap.value.isEmpty()) {
        GlobalObject.getInstance().appInfoRestoreMap.emit(Command.getAppInfoRestoreMap())
    }
    if (viewModel.appRestoreList.value.isEmpty()) {
        refreshAppRestoreList(viewModel)
    }
    viewModel.isInitialized.targetState = true
}

@ExperimentalMaterial3Api
fun LazyListScope.onAppRestoreManifest(viewModel: ListViewModel, context: Context) {
    // 重置列表, 否则Manifest可能和Processing有所出入
    viewModel.searchText.value = ""
    viewModel.filter.value = AppListFilter.Selected
    viewModel.type.value = AppListType.None
    refreshAppRestoreList(viewModel)

    val list = listOf(
        ManifestDescItem(
            title = context.getString(R.string.selected_app),
            subtitle = run {
                var size = 0
                for (i in viewModel.appRestoreList.value) {
                    if (i.selectApp) size++
                }
                size.toString()
            },
            iconId = R.drawable.ic_round_apps
        ),
        ManifestDescItem(
            title = context.getString(R.string.selected_data),
            subtitle = run {
                var size = 0
                for (i in viewModel.appRestoreList.value) {
                    if (i.selectData) size++
                }
                size.toString()
            },
            iconId = R.drawable.ic_round_database
        ),
        ManifestDescItem(
            title = context.getString(R.string.backup_user),
            subtitle = context.readBackupUser(),
            iconId = R.drawable.ic_round_person
        ),
        ManifestDescItem(
            title = context.getString(R.string.restore_user),
            subtitle = context.readRestoreUser(),
            iconId = R.drawable.ic_round_iphone
        ),
        ManifestDescItem(
            title = context.getString(R.string.compression_type),
            subtitle = context.readCompressionType(),
            iconId = R.drawable.ic_round_bolt
        ),
        ManifestDescItem(
            title = context.getString(R.string.backup_strategy),
            subtitle = ofBackupStrategy(context.readBackupStrategy()),
            iconId = -1,
            icon = Icons.Rounded.Place
        ),
        ManifestDescItem(
            title = context.getString(R.string.backup_dir),
            subtitle = context.readBackupSavePath(),
            iconId = R.drawable.ic_round_folder_open
        ),
    )
    contentManifest(list)
}

@ExperimentalAnimationApi
@ExperimentalFoundationApi
@ExperimentalMaterial3Api
fun LazyListScope.onAppRestoreContent(viewModel: ListViewModel) {
    contentAppRestore(
        list = viewModel.appRestoreList.value,
        onSearch = { value ->
            viewModel.searchText.value = value
            refreshAppRestoreList(viewModel)
        },
        onItemUpdate = {
            refreshAppRestoreList(viewModel)
        }
    )
}

@ExperimentalMaterial3Api
fun toAppRestoreProcessing(context: Context) {
    context.startActivity(Intent(context, ProcessingActivity::class.java).apply {
        putExtra(TypeActivityTag, TypeRestoreApp)
    })
}

suspend fun onAppRestoreMapSave() {
    GsonUtil.saveAppInfoRestoreMapToFile(GlobalObject.getInstance().appInfoRestoreMap.value)
}

@ExperimentalMaterial3Api
@Composable
fun AppRestoreBottomSheet(
    isOpen: MutableState<Boolean>,
    viewModel: ListViewModel,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val nonePadding = dimensionResource(R.dimen.padding_none)
    val tinyPadding = dimensionResource(R.dimen.padding_tiny)
    val smallPadding = dimensionResource(R.dimen.padding_small)
    val mediumPadding = dimensionResource(R.dimen.padding_medium)
    val iconSmallSize = dimensionResource(R.dimen.icon_small_size)
    val active = viewModel.activeSort.collectAsState()
    val ascending = viewModel.ascending.collectAsState()
    val filter = viewModel.filter.collectAsState()
    val type = viewModel.type.collectAsState()

    ListBottomSheet(
        isOpen = isOpen,
        actions = {
            item {
                val isDialogOpen = remember {
                    mutableStateOf(false)
                }
                val selectedItems =
                    viewModel.appRestoreList.collectAsState().value.filter { it.selectApp || it.selectData }

                MenuTopBatchDeleteButton(
                    isOpen = isDialogOpen,
                    selectedItems = selectedItems,
                    itemText = {
                        "${selectedItems[it].detailBase.appName} ${selectedItems[it].detailBase.packageName}"
                    }) {
                    scope.launch {
                        for (i in selectedItems) {
                            deleteAppInfoRestoreItem(i) {}
                        }
                        refreshAppRestoreList(viewModel)
                        isOpen.value = false
                    }
                }
            }
            item {
                var selectApp = remember { true }
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(smallPadding))
                        .clickable {
                            viewModel.appRestoreList.value.forEach {
                                it.selectApp = selectApp
                            }
                            selectApp = selectApp.not()
                        }
                        .padding(smallPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(tinyPadding)
                ) {
                    Icon(
                        modifier = Modifier.size(iconSmallSize),
                        imageVector = ImageVector.vectorResource(
                            id = R.drawable.ic_round_check
                        ),
                        contentDescription = null
                    )
                    Text(
                        text = stringResource(R.string.select_all),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            item {
                var selectAll = remember { true }
                Column(modifier = Modifier
                    .clip(RoundedCornerShape(smallPadding))
                    .clickable {
                        viewModel.appRestoreList.value.forEach {
                            it.selectApp = selectAll
                            it.selectData = selectAll
                        }
                        selectAll = selectAll.not()
                    }
                    .padding(smallPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(tinyPadding)
                ) {
                    Icon(
                        modifier = Modifier.size(iconSmallSize),
                        imageVector = ImageVector.vectorResource(
                            id = R.drawable.ic_round_done_all
                        ),
                        contentDescription = null
                    )
                    Text(
                        text = stringResource(R.string.select_all),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        },
        content = {
            // 排序
            Text(
                text = stringResource(id = R.string.sort),
                style = MaterialTheme.typography.titleMedium,
            )
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(smallPadding)
            ) {
                AssistChip(
                    onClick = {
                        viewModel.setActiveSort(context, AppListSort.Alphabet)
                        viewModel.setAscending(context)
                        refreshAppRestoreList(viewModel)
                    },
                    label = { Text(stringResource(id = R.string.alphabet)) },
                    leadingIcon = if (active.value == AppListSort.Alphabet) {
                        {
                            Icon(
                                imageVector = if (ascending.value) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                contentDescription = null,
                                modifier = Modifier.size(AssistChipDefaults.IconSize)
                            )
                        }
                    } else {
                        null
                    }
                )
                AssistChip(
                    onClick = {
                        viewModel.setActiveSort(context, AppListSort.FirstInstallTime)
                        viewModel.setAscending(context)
                        refreshAppRestoreList(viewModel)
                    },
                    label = { Text(stringResource(id = R.string.install_time)) },
                    leadingIcon = if (active.value == AppListSort.FirstInstallTime) {
                        {
                            Icon(
                                imageVector = if (ascending.value) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                contentDescription = null,
                                modifier = Modifier.size(AssistChipDefaults.IconSize)
                            )
                        }
                    } else {
                        null
                    }
                )
                AssistChip(
                    onClick = {
                        viewModel.setActiveSort(context, AppListSort.DataSize)
                        viewModel.setAscending(context)
                        refreshAppRestoreList(viewModel)
                    },
                    label = { Text(stringResource(id = R.string.data_size)) },
                    leadingIcon = if (active.value == AppListSort.DataSize) {
                        {
                            Icon(
                                imageVector = if (ascending.value) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                contentDescription = null,
                                modifier = Modifier.size(AssistChipDefaults.IconSize)
                            )
                        }
                    } else {
                        null
                    }
                )
            }

            // 过滤
            Text(
                modifier = Modifier.padding(nonePadding, mediumPadding, nonePadding, nonePadding),
                text = stringResource(id = R.string.filter),
                style = MaterialTheme.typography.titleMedium,
            )
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(smallPadding)
            ) {
                FilterChip(
                    selected = filter.value == AppListFilter.None,
                    onClick = {
                        if (viewModel.filter.value != AppListFilter.None) {
                            viewModel.filter.value = AppListFilter.None
                            refreshAppRestoreList(viewModel)
                        }
                    },
                    label = { Text(stringResource(R.string.none)) },
                    leadingIcon = if (filter.value == AppListFilter.None) {
                        {
                            Icon(
                                imageVector = Icons.Filled.Done,
                                contentDescription = null,
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        }
                    } else {
                        null
                    }
                )
                FilterChip(
                    selected = filter.value == AppListFilter.Selected,
                    onClick = {
                        if (viewModel.filter.value != AppListFilter.Selected) {
                            viewModel.filter.value = AppListFilter.Selected
                            refreshAppRestoreList(viewModel)
                        }
                    },
                    label = { Text(stringResource(R.string.selected)) },
                    leadingIcon = if (filter.value == AppListFilter.Selected) {
                        {
                            Icon(
                                imageVector = Icons.Filled.Done,
                                contentDescription = null,
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        }
                    } else {
                        null
                    }
                )
                FilterChip(
                    selected = filter.value == AppListFilter.NotSelected,
                    onClick = {
                        if (viewModel.filter.value != AppListFilter.NotSelected) {
                            viewModel.filter.value = AppListFilter.NotSelected
                            refreshAppRestoreList(viewModel)
                        }
                    },
                    label = { Text(stringResource(R.string.not_selected)) },
                    leadingIcon = if (filter.value == AppListFilter.NotSelected) {
                        {
                            Icon(
                                imageVector = Icons.Filled.Done,
                                contentDescription = null,
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        }
                    } else {
                        null
                    }
                )
                FilterChip(
                    selected = filter.value == AppListFilter.Installed,
                    onClick = {
                        if (viewModel.filter.value != AppListFilter.Installed) {
                            viewModel.filter.value = AppListFilter.Installed
                            refreshAppRestoreList(viewModel)
                        }
                    },
                    label = { Text(stringResource(R.string.installed)) },
                    leadingIcon = if (filter.value == AppListFilter.Installed) {
                        {
                            Icon(
                                imageVector = Icons.Filled.Done,
                                contentDescription = null,
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        }
                    } else {
                        null
                    }
                )
                FilterChip(
                    selected = filter.value == AppListFilter.NotInstalled,
                    onClick = {
                        if (viewModel.filter.value != AppListFilter.NotInstalled) {
                            viewModel.filter.value = AppListFilter.NotInstalled
                            refreshAppRestoreList(viewModel)
                        }
                    },
                    label = { Text(stringResource(R.string.not_installed)) },
                    leadingIcon = if (filter.value == AppListFilter.NotInstalled) {
                        {
                            Icon(
                                imageVector = Icons.Filled.Done,
                                contentDescription = null,
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        }
                    } else {
                        null
                    }
                )
            }

            // 类型
            Text(
                modifier = Modifier.padding(nonePadding, mediumPadding, nonePadding, nonePadding),
                text = stringResource(R.string.type),
                style = MaterialTheme.typography.titleMedium,
            )
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(smallPadding)
            ) {
                FilterChip(
                    selected = type.value == AppListType.InstalledApp,
                    onClick = {
                        if (viewModel.type.value != AppListType.InstalledApp) {
                            viewModel.type.value = AppListType.InstalledApp
                            refreshAppRestoreList(viewModel)
                        }
                    },
                    label = { Text(stringResource(R.string.installed_app)) },
                    leadingIcon = if (type.value == AppListType.InstalledApp) {
                        {
                            Icon(
                                imageVector = Icons.Filled.Done,
                                contentDescription = null,
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        }
                    } else {
                        null
                    }
                )
                FilterChip(
                    selected = type.value == AppListType.SystemApp,
                    onClick = {
                        if (viewModel.type.value != AppListType.SystemApp) {
                            viewModel.type.value = AppListType.SystemApp
                            refreshAppRestoreList(viewModel)
                        }
                    },
                    label = { Text(stringResource(R.string.system_app)) },
                    leadingIcon = if (type.value == AppListType.SystemApp) {
                        {
                            Icon(
                                imageVector = Icons.Filled.Done,
                                contentDescription = null,
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        }
                    } else {
                        null
                    }
                )
            }
        }
    )
}

fun sortAppRestoreByAlphabet(
    viewModel: ListViewModel,
    ascending: Boolean
) {
    if (ascending)
        viewModel.appRestoreList.value.sortWith { appInfo1, appInfo2 ->
            if (appInfo1 == null && appInfo2 == null) {
                0
            } else if (appInfo1 == null) {
                -1
            } else if (appInfo2 == null) {
                1
            } else {
                val collator = Collator.getInstance(Locale.CHINA)
                collator.getCollationKey(appInfo1.detailBase.appName)
                    .compareTo(collator.getCollationKey(appInfo2.detailBase.appName))
            }
        }
    else
        viewModel.appRestoreList.value.sortWith { appInfo1, appInfo2 ->
            if (appInfo1 == null && appInfo2 == null) {
                0
            } else if (appInfo1 == null) {
                -1
            } else if (appInfo2 == null) {
                1
            } else {
                val collator = Collator.getInstance(Locale.CHINA)
                collator.getCollationKey(appInfo2.detailBase.appName)
                    .compareTo(collator.getCollationKey(appInfo1.detailBase.appName))
            }
        }
}

fun sortAppRestoreByInstallTime(
    viewModel: ListViewModel,
    ascending: Boolean
) {
    if (ascending)
        viewModel.appRestoreList.value.sortBy { it.firstInstallTime }
    else
        viewModel.appRestoreList.value.sortByDescending { it.firstInstallTime }
}

fun sortAppRestoreByDataSize(
    viewModel: ListViewModel,
    ascending: Boolean
) {
    if (ascending)
        viewModel.appRestoreList.value.sortBy { it.detailRestoreList[it.restoreIndex].sizeBytes }
    else
        viewModel.appRestoreList.value.sortByDescending { it.detailRestoreList[it.restoreIndex].sizeBytes }
}

fun sortAppRestore(viewModel: ListViewModel) {
    when (viewModel.activeSort.value) {
        AppListSort.Alphabet -> {
            sortAppRestoreByAlphabet(viewModel, viewModel.ascending.value)
        }
        AppListSort.FirstInstallTime -> {
            sortAppRestoreByInstallTime(viewModel, viewModel.ascending.value)
        }
        AppListSort.DataSize -> {
            sortAppRestoreByDataSize(viewModel, viewModel.ascending.value)
        }
    }
}

fun filterAppRestoreNone(
    viewModel: ListViewModel,
    predicate: (AppInfoRestore) -> Boolean
) {
    viewModel.appRestoreList.value.clear()
    viewModel.appRestoreList.value.addAll(
        GlobalObject.getInstance().appInfoRestoreMap.value.values.toList()
            .filter { filterTypePredicateAppRestore(viewModel.type.value, it) }
            .filter(predicate)
    )
}

fun filterAppRestoreSelected(
    viewModel: ListViewModel,
    predicate: (AppInfoRestore) -> Boolean
) {
    viewModel.appRestoreList.value.clear()
    viewModel.appRestoreList.value.addAll(
        GlobalObject.getInstance().appInfoRestoreMap.value.values.toList()
            .filter {
                filterTypePredicateAppRestore(viewModel.type.value, it)
                        && (it.selectApp || it.selectData)
            }
            .filter(predicate)
    )
}

fun filterAppRestoreNotSelected(
    viewModel: ListViewModel,
    predicate: (AppInfoRestore) -> Boolean
) {
    viewModel.appRestoreList.value.clear()
    viewModel.appRestoreList.value.addAll(
        GlobalObject.getInstance().appInfoRestoreMap.value.values.toList()
            .filter {
                filterTypePredicateAppRestore(viewModel.type.value, it)
                        && it.selectApp.not() && it.selectData.not()
            }
            .filter(predicate)
    )
}

fun filterAppRestoreInstalled(
    viewModel: ListViewModel,
    predicate: (AppInfoRestore) -> Boolean
) {
    viewModel.appRestoreList.value.clear()
    viewModel.appRestoreList.value.addAll(
        GlobalObject.getInstance().appInfoRestoreMap.value.values.toList()
            .filter {
                filterTypePredicateAppRestore(viewModel.type.value, it)
                        && it.isOnThisDevice.value
            }
            .filter(predicate)
    )
}

fun filterAppRestoreNotInstalled(
    viewModel: ListViewModel,
    predicate: (AppInfoRestore) -> Boolean
) {
    viewModel.appRestoreList.value.clear()
    viewModel.appRestoreList.value.addAll(
        GlobalObject.getInstance().appInfoRestoreMap.value.values.toList()
            .filter {
                filterTypePredicateAppRestore(viewModel.type.value, it)
                        && it.isOnThisDevice.value.not()
            }
            .filter(predicate)
    )
}

fun filterAppRestore(
    viewModel: ListViewModel,
    predicate: (AppInfoRestore) -> Boolean = {
        val value = viewModel.searchText.value
        it.detailBase.appName.lowercase()
            .contains(value.lowercase()) ||
                it.detailBase.packageName.lowercase()
                    .contains(value.lowercase())
    }
) {
    when (viewModel.filter.value) {
        AppListFilter.None -> {
            filterAppRestoreNone(viewModel, predicate)
        }
        AppListFilter.Selected -> {
            filterAppRestoreSelected(viewModel, predicate)
        }
        AppListFilter.NotSelected -> {
            filterAppRestoreNotSelected(viewModel, predicate)
        }
        AppListFilter.Installed -> {
            filterAppRestoreInstalled(viewModel, predicate)
        }
        AppListFilter.NotInstalled -> {
            filterAppRestoreNotInstalled(viewModel, predicate)
        }
    }
}

fun filterTypePredicateAppRestore(type: AppListType, appInfoRestore: AppInfoRestore): Boolean {
    return when (type) {
        AppListType.InstalledApp -> {
            appInfoRestore.detailBase.isSystemApp.not()
        }
        AppListType.SystemApp -> {
            appInfoRestore.detailBase.isSystemApp
        }
        AppListType.None -> {
            true
        }
    }
}

fun refreshAppRestoreList(viewModel: ListViewModel) {
    filterAppRestore(viewModel)
    sortAppRestore(viewModel)
}
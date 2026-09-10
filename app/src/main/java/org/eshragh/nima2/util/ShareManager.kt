package org.eshragh.nima2.util

import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SharedData(
    val text: String? = null,
    val uris: List<Uri>? = null
)

object ShareManager {
    private val _pendingShare = MutableStateFlow<SharedData?>(null)
    val pendingShare = _pendingShare.asStateFlow()

    fun setShareData(text: String?, uris: List<Uri>?) {
        _pendingShare.value = SharedData(text, uris)
    }

    fun consumeShareData() {
        _pendingShare.value = null
    }
}

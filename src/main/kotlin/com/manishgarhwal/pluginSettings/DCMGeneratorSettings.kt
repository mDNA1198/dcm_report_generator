package com.manishgarhwal.pluginSettings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

import java.io.File
import java.nio.file.Paths

@Service(Service.Level.APP)
@State(
    name = "com.manishgarhwal.pluginSettings.DCMGeneratorSettings",
    storages = [Storage("DCMGeneratorSettings.xml")]
)
class DCMGeneratorSettings : PersistentStateComponent<DCMGeneratorSettings.State> {

    companion object {
        fun getInstance(): DCMGeneratorSettings =
            com.intellij.openapi.application.ApplicationManager.getApplication()
                .getService(DCMGeneratorSettings::class.java)
    }

    class State {
        var dartSdkPath: String = ""
        var openInBrowser: Boolean = false
        var openInIDE: Boolean = false
    }

    private var state = State()

    init {
        if (state.dartSdkPath.isEmpty()) {

            state.dartSdkPath = detectFlutterSdkPath() ?: ""
        }
    }

    private fun detectFlutterSdkPath(): String? {
        val pathCmd = try {
            val process = ProcessBuilder("which", "flutter").start()
            process.inputStream.bufferedReader().readText().trim()
        } catch (_: Exception) {
            try {
                val process = ProcessBuilder("where", "flutter.exe").start()
                process.inputStream.bufferedReader().readText().trim()
            } catch (_: Exception) {
                null
            }
        }
        if (!pathCmd.isNullOrEmpty()) {
            return File(pathCmd).absolutePath
        }

        val candidates = listOf(
            System.getProperty("user.home") + "/flutter/bin/flutter",
            "/usr/local/Caskroom/flutter/latest/flutter/bin/flutter",
            "/snap/flutter/current/bin/flutter",
            "C:\\src\\flutter\\bin\\flutter.bat"
        )

        return candidates.firstOrNull { File(it).exists() }
    }

    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state = state
    }
}
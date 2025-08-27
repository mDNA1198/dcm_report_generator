package com.manishgarhwal.plugin

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import java.io.File
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.manishgarhwal.pluginSettings.DCMGeneratorSettings

class GenerateDCMReportAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project: Project? = e.project
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)

        if (project == null || file == null) {
            Messages.showErrorDialog("No active file selected!", "Generate DCM Report")
            return
        }

        val inputFile = file.path
        val outputFile = File(
            file.parent.path,   // parent directory path (String)
            file.nameWithoutExtension + "_dcm_report.html"
        )

        // Create a dedicated folder for the report
        if (!outputFile.exists()) {
            outputFile.mkdirs()
        }

        // Command: generates report inside reportDir
        val settings = DCMGeneratorSettings.getInstance().state
        val dartPath = settings.dartSdkPath + "/dart"

        val command = listOf<String>(
            dartPath, "run", "dart_code_metrics:metrics",
            inputFile,
            "-r", "html",
            "-o", outputFile.absolutePath
        )

        object : Task.Backgroundable(project, "Generating DCM Report", true) {
            override fun run(indicator: ProgressIndicator) {
                indicator.text = "Running dart_code_metrics..."
                try {
                    val process = ProcessBuilder(command)
                        .directory(File(project.basePath!!))
                        .redirectErrorStream(true)
                        .start()

                    val output = process.inputStream.bufferedReader().readText()
                    val exitCode = process.waitFor()

                    if (exitCode == 0 && outputFile.exists()) {
                        val indexFile = File(outputFile, "index.html")
                        val vFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(indexFile)
                        if (vFile != null) {
                            if(settings.openInIDE){
                                openHTMLReportInIDE(project, vFile)
                            }
                            if(settings.openInBrowser){
                                openHtmlReportInBrowser(vFile.path)
                            }
                        }
                    } else {
                        Messages.showErrorDialog(
                            project,
                            "❌ Failed to generate report.\nExit code: $exitCode\n$output",
                            "Generate DCM Report"
                        )
                    }
                } catch (ex: Exception) {
                    Messages.showErrorDialog(
                        project,
                        "⚠ Error running command:\n${ex.message}",
                        "Generate DCM Report"
                    )
                }
            }
        }.queue()

    }

    private fun openHTMLReportInIDE(project: Project, vFile: VirtualFile){

         val fileEditorManager = FileEditorManager.getInstance(project)

        // Open in right split
        fileEditorManager.openFile(vFile, true, true)
    }

    private fun openHtmlReportInBrowser(reportPath: String) {
        val file = File(reportPath)

        if (!file.exists()) {
            println("Report file not found: $reportPath")
            return
        }

        try {
            val os = System.getProperty("os.name").lowercase()
            when {
                os.contains("mac") -> {
                    Runtime.getRuntime().exec(arrayOf("open", file.absolutePath))
                }
                os.contains("nix") || os.contains("nux") || os.contains("aix") -> {
                    // Try xdg-open first
                    try {
                        // Fallback to common browsers
                        val browsers = listOf("google-chrome", "chromium", "firefox", "brave-browser")
                        var opened = false
                        for (browser in browsers) {
                            try {
                                Runtime.getRuntime().exec(arrayOf(browser, file.absolutePath))
                                opened = true
                                break
                            } catch (_: Exception) {}
                        }
                        if (!opened) {
                            println("⚠ Could not open in browser. Please open manually: ${file.absolutePath}")
                        }

                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                }
                os.contains("win") -> {
                    java.awt.Desktop.getDesktop().browse(file.toURI())
                }
                else -> {
                    // Fallback
                    println("⚠ Unsupported OS: $os. Please open manually: ${file.absolutePath}")
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}
package com.manishgarhwal.pluginSettings

import com.intellij.openapi.options.Configurable
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.JLabel
import java.io.File
import javax.swing.*
import java.awt.BorderLayout
import java.awt.FlowLayout

class DCMGeneratorConfigurable : Configurable {

    private var primaryPanel: JPanel? = null
    private lateinit var dartPathField: JTextField
    private lateinit var openInBrowserCheckBox: JCheckBox
    private lateinit var openInIDE: JCheckBox

    override fun getDisplayName(): String = "DCM Generator"

    private fun createSDKPathSelector(settings: DCMGeneratorSettings.State): JPanel{
        dartPathField = JTextField(30)

        dartPathField.text = settings.dartSdkPath
        dartPathField.toolTipText = "Should end with \"flutter_sdk_path/bin/cache/dart-sdk/bin\""

        val browseButton = JButton("Browse...").apply {
            addActionListener {
                val chooser = JFileChooser()
                chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                if (chooser.showOpenDialog(primaryPanel) == JFileChooser.APPROVE_OPTION) {
                    val selectedPath = chooser.selectedFile.absolutePath
                    val binDir = File(selectedPath)

                    // Check if path ends with "bin"
                    if (!binDir.absolutePath.endsWith("dart-sdk/bin", ignoreCase = true)) {
                        JOptionPane.showMessageDialog(
                            primaryPanel,
                            "Invalid path. Please select the 'bin' folder inside the Dart SDK. \nUsually found within \"flutter_sdk_path/bin/cache/dart-sdk/bin\" ",
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                        )
                        return@addActionListener
                    }

                    // Check for dart executable (platform dependent)
                    val dartExecutable = if (System.getProperty("os.name").startsWith("Windows")) {
                        File(binDir, "dart.exe")
                    } else {
                        File(binDir, "dart")
                    }

                    if (!dartExecutable.exists() || !dartExecutable.canExecute()) {
                        JOptionPane.showMessageDialog(
                            primaryPanel,
                            "Invalid SDK. 'dart' executable not found inside selected bin folder.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                        )
                        return@addActionListener
                    }

                    dartPathField.text = chooser.selectedFile.absolutePath
                }
            }
        }

        val panelForSDKPathSelector = JPanel(BorderLayout()).apply {
            add(JLabel("Dart SDK Path: "), BorderLayout.WEST)
            add(dartPathField, BorderLayout.CENTER)
            add(browseButton, BorderLayout.EAST)
        }

        return panelForSDKPathSelector
    }

    private fun createOpenInBrowserCBPanel(settings: DCMGeneratorSettings.State): JPanel{
        openInBrowserCheckBox = JCheckBox("Open report in browser: ").apply {
            isSelected = settings.openInBrowser // default value, can be restored from settings
        }
        val openInBrowserCheckBoxPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        openInBrowserCheckBoxPanel.border = BorderFactory.createEmptyBorder(10, 0, 0, 0) // top=10px padding
        openInBrowserCheckBoxPanel.add(openInBrowserCheckBox)

        return openInBrowserCheckBoxPanel
    }

    private fun createOpenInIDECBPanel(settings: DCMGeneratorSettings.State): JPanel{
        openInIDE = JCheckBox("Open report in IDE: ").apply {
            isSelected = settings.openInIDE // default value, can be restored from settings
        }
        val openInIDECheckBoxPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        openInIDECheckBoxPanel.border = BorderFactory.createEmptyBorder(10, 0, 0, 0) // top=10px padding
        openInIDECheckBoxPanel.add(openInIDE)

        return openInIDECheckBoxPanel
    }

    override fun createComponent(): JComponent? {
        if (primaryPanel == null) {
            // Load existing setting
            val settings = DCMGeneratorSettings.getInstance().state

            val sdkPathSelectorPanel = createSDKPathSelector(settings)
            val openInBrowserCBPanel = createOpenInBrowserCBPanel(settings)
            val openInIDECBPanel = createOpenInIDECBPanel(settings)


            primaryPanel = JPanel(BorderLayout())

            val topPanel = JPanel().apply {
                layout = BoxLayout(this, BoxLayout.Y_AXIS)
                add(sdkPathSelectorPanel)
                add(openInBrowserCBPanel)
                add(openInIDECBPanel)
            }

            primaryPanel!!.add(topPanel, BorderLayout.NORTH)
        }
        return primaryPanel
    }

    override fun isModified(): Boolean {
        val settings = DCMGeneratorSettings.getInstance().state
        return dartPathField.text != settings.dartSdkPath || openInBrowserCheckBox.isSelected != settings.openInBrowser || openInIDE.isSelected != settings.openInIDE
    }

    override fun apply() {
        val settings = DCMGeneratorSettings.getInstance().state
        settings.dartSdkPath = dartPathField.text.trim()
        settings.openInBrowser = openInBrowserCheckBox.isSelected
        settings.openInIDE = openInIDE.isSelected
    }

    override fun reset() {
        val settings = DCMGeneratorSettings.getInstance().state
        dartPathField.text = settings.dartSdkPath
        openInBrowserCheckBox.isSelected = settings.openInBrowser
        openInIDE.isSelected = settings.openInIDE
    }
}

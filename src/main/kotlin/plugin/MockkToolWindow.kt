package plugin

import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.ui.ConsoleView
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import logs.LogKeeper
import migration.MockitoToMockkConverter
import tools.getSelectedDocumentOrNull
import tools.saveDocument
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ActionEvent
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JPanel


internal class MockkToolWindow : ToolWindowFactory, DumbAware {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val toolWindowContent = MockkToolWindow(toolWindow)
        val content = ContentFactory.getInstance().createContent(toolWindowContent.contentPanel, "", false)
        toolWindow.contentManager.addContent(content)
    }

    private class MockkToolWindow(toolWindow: ToolWindow) {
        private lateinit var consoleView: ConsoleView
        val contentPanel: JPanel = JPanel()

        init {
            contentPanel.layout = BorderLayout(0, 20)
            contentPanel.border = BorderFactory.createEmptyBorder(40, 0, 0, 0)
            contentPanel.add(createControlsPanel(toolWindow), BorderLayout.CENTER)
        }

        private fun createControlsPanel(toolWindow: ToolWindow): JPanel {
            val controlsPanel = JPanel(BorderLayout())
            val migrateButton = JButton("Convert to Mockk")
            migrateButton.addActionListener { e: ActionEvent? -> migrateToMockk(toolWindow) }
            consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(toolWindow.project).console
            consoleView.component.size = Dimension(400, 300)
            consoleView.print("Nothing to show", ConsoleViewContentType.LOG_INFO_OUTPUT)

            // You can also use layout managers to set the size and position
            controlsPanel.add(consoleView.component, BorderLayout.CENTER)
            controlsPanel.add(migrateButton, BorderLayout.SOUTH)
            return controlsPanel
        }

        private fun showMigrationLog() {
            LogKeeper.logs.forEach { log ->
                consoleView.print("${log.message}\n", log.logType.logType)
            }
        }

        private fun migrateToMockk(toolWindow: ToolWindow) {
            consoleView.clear()
            consoleView.print("Nothing to show", ConsoleViewContentType.LOG_INFO_OUTPUT)
            val project = toolWindow.project
            val currentDocument = project.getSelectedDocumentOrNull()
            if (currentDocument == null) {
                Messages.showMessageDialog(
                    project,
                    "Please select file first!",
                    "Error",
                    Messages.getInformationIcon()
                )
            } else {
                val converter = MockitoToMockkConverter()
                val result = converter.convert(currentDocument.text)
                project.saveDocument(document = currentDocument, result)
                consoleView.clear()
                showMigrationLog()
            }
        }
    }
}
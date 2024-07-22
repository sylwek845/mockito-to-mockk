package tools

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.findDocument


fun Project.getSelectedDocumentOrNull(): Document? {
    val virFile = FileEditorManager.getInstance(this).selectedFiles.firstOrNull() ?: return null
    return virFile.findDocument()
}

fun Project.saveDocument(document: Document, text: String) {
    val r = Runnable {
        document.setText(text)
    }

    WriteCommandAction.runWriteCommandAction(this, r)
}

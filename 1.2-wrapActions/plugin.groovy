import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

import static liveplugin.PluginUtil.*

unwrapAction("EditorTab")
wrapAction("EditorTab") { AnActionEvent event, AnAction wrappedAction ->
    def project = event.project
    def editor = currentEditorIn(project)
    def caretModel = editor.caretModel
    def document = editor.document

    def offset = caretModel.offset
    def lastFourChars = document.text.substring(offset - 4, offset)

    if (lastFourChars == "lscc") {
        runDocumentWriteAction(project, document) {
            document.insertString(offset, "-complete")
            caretModel.moveToOffset(offset + "-complete".length())
        }
    } else {
        wrappedAction.actionPerformed(event)
    }
}
show("Loaded tab auto-complete")

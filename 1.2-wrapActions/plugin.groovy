import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

import static liveplugin.PluginUtil.*

unwrapAction("EditorTab")
    wrapAction("EditorTab") { AnActionEvent event, AnAction wrappedAction ->
        def project = event.project
        def editor = currentEditorIn(project)
        def caretModel = editor.caretModel
        def document = editor.document

        def from = (caretModel.offset..0).find{ index -> document.text[index].matches(/\W/) } + 1
        def to = (caretModel.offset..document.textLength).find{ index -> document.text[index].matches(/\W/) }
        if (from >= to) return
        def currentWord = document.text.substring(from, to)

        def abbreviation = "lscc"
        def replacement = "lscc-complete"

        if (currentWord?.endsWith(abbreviation)) {
            runDocumentWriteAction(project, document) {
                document.replaceString(to - abbreviation.length(), to, replacement)
                caretModel.moveToOffset(to - abbreviation.length() + replacement.length())
            }
        } else {
            wrappedAction.actionPerformed(event)
        }
}
show("Loaded tab auto-complete")

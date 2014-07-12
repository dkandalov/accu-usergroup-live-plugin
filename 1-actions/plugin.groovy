import com.intellij.openapi.actionSystem.AnActionEvent

import static liveplugin.PluginUtil.*

registerAction("ReplaceCurrentWord", "ctrl alt shift R") { AnActionEvent event ->
    def project = event.project

    def editor = currentEditorIn(project)
    def caretModel = editor.caretModel
    def document = editor.document

    def from = (caretModel.offset..0).find{ document.text[it].matches(/\W/) } + 1
    def to = (caretModel.offset..document.textLength).find{ document.text[it].matches(/\W/) }
    if (from >= to) return

    runDocumentWriteAction(project, document) {
        document.replaceString(from, to, "SoftwareCraftBeer")
    }
}
show("Reloaded ReplaceCurrentWord plugin")
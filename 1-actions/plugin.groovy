import com.intellij.openapi.actionSystem.AnActionEvent

import static liveplugin.PluginUtil.*

registerAction("ReplaceCurrentWord", "ctrl alt shift R", "ToolsMenu", "Replace Current Word") { AnActionEvent event ->
    def project = event.project

    def editor = currentEditorIn(project)
    def caretModel = editor.caretModel
    def document = editor.document

    def from = (caretModel.offset..0).find{ index -> document.text[index].matches(/\W/) } + 1
    def to = (caretModel.offset..document.textLength).find{ index -> document.text[index].matches(/\W/) }
    if (from >= to) return

    runDocumentWriteAction(project, document) {
        document.replaceString(from, to, "SoftwareCraftBeer")
    }
}
show("Reloaded ReplaceCurrentWord plugin")

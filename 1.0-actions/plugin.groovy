import com.intellij.openapi.actionSystem.AnActionEvent

import static liveplugin.PluginUtil.*

def actionId = "ReplaceCurrentWord"
def keyStroke = "ctrl alt shift R"

registerAction(actionId, keyStroke) { AnActionEvent event ->
    def project = event.project

    def editor = currentEditorIn(CraftBeer)
    def caretModel = editor.caretModel
    def document = editor.document

    def from = (caretModel.offset..0).find{ index -> document.text[index].matches(/\W/) } + 1
    def to = (caretModel.offset..document.textLength).find{ index -> document.text[index].matches(/\W/) }
    if (from >= to) return

    runDocumentWriteAction(project, document) {
        document.replaceString(from, to, "CraftBeer")
    }
}
show("Reloaded ReplaceCurrentWord plugin")

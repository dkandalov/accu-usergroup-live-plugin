import static liveplugin.PluginUtil.currentEditorIn
import static liveplugin.PluginUtil.runDocumentWriteAction


def editor = currentEditorIn(project)
def caretModel = editor.caretModel
def document = editor.document

def from = (caretModel.offset..0).find{ i-> document.text[i].matches(/\W/) } + 1
def to = (caretModel.offset..document.textLength).find{ i-> document.text[i].matches(/\W/) }
def word = document.text.subSequence(from, to)

//show(word)
//BrowserUtil.open("http://google.com/search?q" + word)
//editor.selectionModel.setSelection(from, to)
runDocumentWriteAction(project, document) {
    document.replaceString(from, to, "craftBeer")
}
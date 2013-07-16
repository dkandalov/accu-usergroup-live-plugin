import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.DocCommandGroupId
import com.intellij.openapi.editor.actionSystem.EditorAction
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.editor.actions.TabAction

import static liveplugin.PluginUtil.*


unwrapAction("EditorTab")
wrapAction("EditorTab") { AnActionEvent event, AnAction wrappedAction ->
    currentEditorIn(event.project).with {
        def from = document.getLineStartOffset(document.getLineNumber(caretModel.offset))
        def to = caretModel.offset
        def text = document.text.substring(from, to)

        def abbreviation = "accu"
        def replacement = "accu-complete"
        if (text.endsWith(abbreviation)) {
            runDocumentWriteAction(event.project, document) {
                document.replaceString(to - abbreviation.length(), to, replacement)
                caretModel.moveToOffset(to - abbreviation.length() + replacement.length())
            }
        } else {
            wrappedAction.actionPerformed(event)
        }
    }
}
show("Loaded tab auto-complete")

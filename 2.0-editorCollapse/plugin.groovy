import com.intellij.codeInsight.folding.impl.EditorFoldingInfo
import com.intellij.codeInsight.folding.impl.FoldingUtil
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.FoldRegion
import com.intellij.openapi.editor.ex.FoldingModelEx

import java.util.regex.Pattern

import static liveplugin.PluginUtil.*


registerAction("SymbolizeKeyWords", "ctrl alt shift 0") { AnActionEvent event ->
    def editor = currentEditorIn(event.project)
    collapseIn(editor, "(@Override\n?\\s+)\\s", { "↑" })
    collapseIn(editor, "(public\n?\\s*?)\\s", { "●" })
    collapseIn(editor, "(private\n?\\s*?)\\s", { "○" })
    collapseIn(editor, "(protected\n?\\s*?)\\s", { "□" })
    collapseIn(editor, "(static\n?\\s*?)\\s", { "◆" })
    collapseIn(editor, "(final\n?\\s*?)\\s", { "ƒ" })
    collapseIn(editor, "( extends )", { " ← " })
    collapseIn(editor, "( implements )", { " ⇠ " })
    collapseIn(editor, "\\S(.;\n)", { it.replace(";", "") })
    collapseIn(editor, "(\\(\\))", { "" })
    collapseIn(editor, "(!=)", { "≠" })
    collapseIn(editor, "(return)", { "↵" })
}
if (!isIdeStartup) show("Loaded symbolizeKeywords action. Use Ctrl+Alt+Shift+0 to run it.")


def collapseIn(Editor editor, String regExp, Closure replacementFor) {
    def matches = findMatchesIn(editor.document.text, regExp)

    editor.foldingModel.runBatchFoldingOperation(new Runnable() {
        @Override void run() {
            matches.each {
                foldText(it.start, it.end, replacementFor(it.text), editor)
            }
        }
    })
}

List findMatchesIn(String text, String regExp) {
    def matches = []
    def matcher = Pattern.compile(regExp).matcher(text)
    while (matcher.find()) {
        matches << [start: matcher.start(1), end: matcher.end(1), text: matcher.group(1)]
    }
    matches
}

/**
 * Originally copied from com.intellij.codeInsight.folding.impl.CollapseSelectionHandler
 */
def foldText(int start, int end, String placeHolderText, Editor editor) {
    if (start + 1 >= end) return
    if (start < end && editor.document.charsSequence.charAt(end - 1) == '\n') end--

    FoldRegion region = FoldingUtil.findFoldRegion(editor, start, end)
    if (region != null) {
        EditorFoldingInfo info = EditorFoldingInfo.get(editor)
        if (info.getPsiElement(region) == null) {
            editor.foldingModel.removeFoldRegion(region)
            info.removeRegion(region)
        }
    } else {
        region = ((FoldingModelEx)editor.foldingModel).addFoldRegion(start, end, placeHolderText)
        if (region == null) {
            return
        }
        region.expanded = false
    }
}
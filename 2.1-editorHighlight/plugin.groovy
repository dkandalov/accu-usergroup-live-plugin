import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.markup.EffectType
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.editor.markup.HighlighterTargetArea
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.psi.PsiCodeBlock
import com.intellij.psi.PsiElement

import java.awt.*

import static java.awt.Color.BLACK
import static java.awt.Color.YELLOW
import static liveplugin.PluginUtil.*


registerAction("ScopeHighlight", "ctrl shift COMMA") { AnActionEvent event ->
    def project = event.project
    def editor = currentEditorIn(project)

    def psiElement = currentPsiFileIn(project).findElementAt(editor.caretModel.offset)
    psiElement = findCodeBlockParentOf(psiElement)
    if (psiElement == null) return

    def fromOffset = psiElement.textOffset + 1
    def toOffset = psiElement.textOffset + psiElement.textLength - 1

    editor.markupModel.removeAllHighlighters()
    editor.markupModel.addRangeHighlighter(
            fromOffset,
            toOffset,
            HighlighterLayer.SELECTION + 1,
            textAttributes(),
            HighlighterTargetArea.EXACT_RANGE
    )
}
show("Reloaded ScopeHighlight plugin")


def textAttributes() {
    new TextAttributes(
        foregroundColor = BLACK,
        backgroundColor = YELLOW,
        effectColor = YELLOW,
        EffectType.SEARCH_MATCH,
        Font.PLAIN
    )
}


PsiElement findCodeBlockParentOf(PsiElement psiElement) {
    if (psiElement == null) null
    else if (psiElement instanceof PsiCodeBlock) psiElement
    else findCodeBlockParentOf(psiElement.parent)
}

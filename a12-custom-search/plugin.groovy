import com.intellij.find.findUsages.CustomUsageSearcher
import com.intellij.find.findUsages.FindUsagesOptions
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.extensions.Extensions
import com.intellij.openapi.fileEditor.FileEditorLocation
import com.intellij.psi.PsiElement
import com.intellij.usageView.UsageInfo
import com.intellij.usages.TextChunk
import com.intellij.usages.Usage
import com.intellij.usages.UsageInfo2UsageAdapter
import com.intellij.usages.UsagePresentation
import com.intellij.util.Processor

import javax.swing.*

import static liveplugin.PluginUtil.*

if (isIdeStartup) return

def extensionPoint = Extensions.rootArea.getExtensionPoint(CustomUsageSearcher.EP_NAME)
CustomUsageSearcher usageSearcher = changeGlobalVar("myUsageSearcher") { lastSearcher ->
    if (lastSearcher != null && extensionPoint.hasExtension(lastSearcher)) {
        extensionPoint.unregisterExtension(lastSearcher)
    }
    new CustomUsageSearcher() {
        @Override void processElementUsages(PsiElement element, Processor<Usage> processor, FindUsagesOptions options) {
            runReadAction {
                if (!element.text.contains("mySearchableElement")) return

                processor.process(new UsageInfo2UsageAdapter(
                        new UsageInfo(element.containingFile, element.textOffset + 1, element.textOffset + element.textLength, false)
                ))
                processor.process(new UsageAdapter() {
                    @Override UsagePresentation getPresentation() {
                        new UsagePresentation() {
                            @Override TextChunk[] getText() { [new TextChunk(new TextAttributes(), "This is a fake usage")].toArray() }
                            @Override String getPlainText() { "This is a fake usage" }
                            @Override String getTooltipText() { "" }
                            @Override Icon getIcon() { null }
                        }
                    }
                })
            }
        }
    }
}
extensionPoint.registerExtension(usageSearcher)

if (!isIdeStartup) show("Registered myUsageSearcher")

def mySearchableElement = "" // <-- try search usages on this element
mySearchableElement += "something" // <-- try search usages on this element


class UsageAdapter implements Usage {
    @Override UsagePresentation getPresentation() {null }
    @Override FileEditorLocation getLocation() { null }
    @Override boolean isValid() { true }
    @Override boolean isReadOnly() { true }
    @Override void selectInEditor() {}
    @Override void highlightInEditor() {}
    @Override void navigate(boolean requestFocus) {}
    @Override boolean canNavigate() { false }
    @Override boolean canNavigateToSource() { false }
}
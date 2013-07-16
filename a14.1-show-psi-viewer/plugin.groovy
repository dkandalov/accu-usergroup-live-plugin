import com.intellij.internal.psiView.PsiViewerDialog
import static liveplugin.PluginUtil.*

registerAction("showPsiViewerDialog", "ctrl shift P") {
    new PsiViewerDialog(project, false, null, null).show()
}
show("Registered action showPsiViewerDialog<br/>Use Ctrl+Shift+P to run it")
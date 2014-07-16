import com.intellij.openapi.project.Project
import com.intellij.psi.*

import static liveplugin.PluginUtil.*

doInBackground("Looking for recursive methods") {
    runReadAction {
        show(findRecursiveMethodsIn(project).collect{it.parent.name + "#" + it.name}.join("\n"))
    }
}

Collection<PsiMethod> findRecursiveMethodsIn(Project project) {
    def result = new HashSet()
    allPsiItemsIn(project).each { PsiFileSystemItem psiItem ->
        if (!(psiItem instanceof PsiJavaFile)) return

        def recursiveMethods = allMethodsIn(psiItem).findAll{ isRecursive(it) }
        result.addAll(recursiveMethods)
    }
    result
}

boolean isRecursive(PsiMethod method) {
    def result = false
    method.acceptChildren(new JavaRecursiveElementVisitor() {
        @Override void visitMethodCallExpression(PsiMethodCallExpression methodCall) {
            if (methodCall.methodExpression.resolve() == method) result = true
            else super.visitMethodCallExpression(methodCall)
        }
    })
    result
}

List<PsiMethod> allMethodsIn(PsiJavaFile javaFile) {
    def result = []
    javaFile.acceptChildren(new JavaRecursiveElementVisitor() {
        @Override void visitMethod(PsiMethod method) {
            if (method.containingClass.interface) return
            result << method
        }
    })
    result
}
import com.intellij.codeInsight.highlighting.HighlightUsagesHandler
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.markup.EffectType
import com.intellij.openapi.editor.markup.HighlighterTargetArea
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementFactory
import com.intellij.psi.PsiExpression
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiKeyword
import com.intellij.psi.PsiMethodCallExpression
import com.intellij.psi.PsiReferenceExpression
import com.intellij.psi.PsiReferenceParameterList
import com.intellij.psi.PsiReturnStatement
import com.intellij.psi.PsiStatement
import com.intellij.psi.PsiTypeElement
import com.intellij.psi.PsiVariable
import com.intellij.psi.PsiMethod
import groovy.transform.Field
import groovy.transform.Immutable

import java.awt.Color
import java.awt.Font

import static PsiMatchUtil.*
import static Result.failure
import static Result.psiElementsIn
import static Result.success
import static com.intellij.openapi.fileTypes.StdFileTypes.*
import static liveplugin.PluginUtil.*

if (isIdeStartup) return

// e.g. try on com.banners.common.SimpleRepository#keySet
registerAction("InferReturnTypeParameter", "ctrl shift T") {
    def editor = currentEditorIn(project)
    def psiFile = currentPsiFileIn(project)
    def element = psiFile.findElementAt(editor.caretModel.offset)
    def psiMethod = findParent(element){ it instanceof PsiMethod }
    if (psiMethod == null) return

    def result = addInferredReturnTypeParameter(psiMethod, project)
    highlightIn(editor, result)
}
show("Registered InferReturnTypeParameter, use ctrl+shift+T to run it")


static Result addInferredReturnTypeParameter(PsiMethod psiMethod, Project project) {
    use(PsiMatchUtil){

        def returnTypeMatch = psiMethod.findChild{
            it.capturedAs("resultType"){ it.is(PsiTypeElement) && hasTypeParameters(it) && it.parent == psiMethod && it.hasChild{
                it.capturedAs("paramList"){ it.is(PsiReferenceParameterList) && it.parent.parent == resultType && it.typeArguments.size() == 0 }
            }}
        }
        if (!returnTypeMatch.wasFound) return failure(psiElementsIn(returnTypeMatch))

        def returnExpressionMatches = psiMethod.findAllChildren{
            it.is(PsiReturnStatement) && it.hasChild{
                it.capturedAs("returnExpression"){ it.is(PsiExpression)
                }}
        }
        def returnTypeParameters = returnExpressionMatches.collect{ it.returnExpression.type.parameters }.unique()
        if (returnTypeParameters.size() != 1) return failure(psiElementsIn(returnTypeMatch) + psiElementsIn(returnExpressionMatches))

        runDocumentWriteAction(project){
            def text = "Xxx<" + returnTypeParameters.first().collect{ it.presentableText }.join(",") + ">"
            def psiFactory = JavaPsiFacade.getElementFactory(project)
            def newElement = psiFactory.createTypeElementFromText(text, psiMethod).innermostComponentReferenceElement.parameterList
            returnTypeMatch.paramList = returnTypeMatch.paramList.replace(newElement)
        }

        return success(psiElementsIn(returnTypeMatch) + psiElementsIn(returnExpressionMatches), [returnTypeMatch.paramList])
    }
}

class PsiMatchUtil {
    static ThreadLocal<Map> result = new ThreadLocal() {
        @Override protected Object initialValue() { new HashMap() }
    }
    static ThreadLocal<Boolean> isFirstCallOnStack = new ThreadLocal() {
        @Override protected Object initialValue() { true }
    }

    static Map getResult() {
        result.get()
    }

    static saveResult(String id, value) {
        getResult().put(id, value)
    }

    static boolean capturedAs(PsiElement element, String id, Closure match) {
        def prevElement = saveResult(id, element)
        def matched = match(element)
        if (!matched) {
            saveResult(id, prevElement)
        }
        matched
    }

    static def <T> T findParent(PsiElement element, Closure<T> match) {
        if (element == null) null
        else if (match(element)) element
        else findParent(element.parent, match)
    }

    static List<Map> findAllChildren(PsiElement psiElement, Closure match) {
        cleaningThreadLocal{
            match.delegate = getResult()
            def results = childrenBreadthFirst(psiElement.children.toList()).findResults {
                def matched = match(it)
                if (matched) {
                    getResult().clone()
                }
            }
        }
    }

    static Map findChild(PsiElement psiElement, Closure match) {
        cleaningThreadLocal {
            match.delegate = getResult()
            def child = childrenBreadthFirst(psiElement.children.toList()).find{ match(it) }
            saveResult("wasFound", child != null)
            def result = getResult().clone()
        }
    }

    static boolean hasChild(PsiElement psiElement, Closure match) {
        findChild(psiElement, match).wasFound
    }

    static boolean is(PsiElement element, Class aClass) {
        aClass.isAssignableFrom(element.class)
    }

    private static <T> T cleaningThreadLocal(Closure closure) {
        def thisIsFirstCallOnStack = isFirstCallOnStack.get()
        if (thisIsFirstCallOnStack) isFirstCallOnStack.set(false)

        T result = closure.call()

        if (thisIsFirstCallOnStack) {
            isFirstCallOnStack.set(true)
            getResult().clear()
        }
        result
    }

    private static Collection<PsiElement> childrenBreadthFirst(Collection<PsiElement> elements) {
        List<PsiElement> list = new LinkedList(elements)
        int i = 0
        while (i < list.size()) {
            list.addAll(list[i].children.toList())
            i++
        }
        list
    }

    static highlightIn(Editor editor, Result result) {
        def textAttributes = new TextAttributes(Color.BLACK, Color.GREEN, Color.GREEN, EffectType.SEARCH_MATCH, Font.PLAIN)
        HighlightUsagesHandler.doHighlightElements(editor, result.modifiedElements.toArray() as PsiElement[], textAttributes, false)

        def textAttributes2 = new TextAttributes(Color.BLACK, Color.YELLOW, Color.YELLOW, EffectType.SEARCH_MATCH, Font.PLAIN)
        HighlightUsagesHandler.doHighlightElements(editor, result.matchedElements.toArray() as PsiElement[], textAttributes2, false)
    }
}

@Immutable class Result {
    boolean successful
    Collection<PsiElement> matchedElements = []
    Collection<PsiElement> modifiedElements = []

    static success(Collection<PsiElement> matchedElements = [], Collection<PsiElement> modifiedElements = []) {
        new Result(true, matchedElements, modifiedElements)
    }

    static failure(Collection<PsiElement> matchedElements = []) {
        new Result(false, matchedElements, [])
    }

    static Collection<PsiElement> psiElementsIn(Map map) {
        psiElementsIn(map.values())
    }

    static Collection<PsiElement> psiElementsIn(Collection values) {
        values.collectMany{
            if (it instanceof PsiElement && it.valid) [it]
            else if (it instanceof Map) psiElementsIn(it.values())
            else [null]
        }.findAll{it != null}
    }
}

static boolean hasTypeParameters(PsiTypeElement typeElement) {
    def type = typeElement.type
    if (type instanceof PsiClassType) {
        type?.resolve()?.hasTypeParameters()
    } else {
        false
    }
}

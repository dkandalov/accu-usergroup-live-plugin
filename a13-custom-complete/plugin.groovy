import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.extensions.Extensions
import com.intellij.openapi.extensions.PluginDescriptor
import com.intellij.openapi.util.KeyedExtensionCollector
import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext
import groovy.json.JsonSlurper
import org.jetbrains.annotations.NotNull
import org.picocontainer.PicoContainer
import com.intellij.openapi.extensions.DefaultPluginDescriptor


import static liveplugin.PluginUtil.changeGlobalVar
import static liveplugin.PluginUtil.show
import static com.intellij.patterns.PlatformPatterns.*
import static com.intellij.codeInsight.completion.CompletionType.*

if (isIdeStartup) return

registerCompletionContributor("googleCompletion", BASIC, psiElement()) { parameters, context, result ->
    def word = lastWord(parameters.offset, parameters.lookup.editor.document.charsSequence)
    if (!word.empty) {
        def suggestions = googleSuggestionsFor(word).collect{toCamelHumps(it)}
        result.addAllElements(suggestions.collect{ LookupElementBuilder.create(it) })
    }
}
show("Loaded auto-completion contributors")


static registerCompletionContributor(String id, CompletionType completionType, ElementPattern elementPattern, Closure callback) {
    resetCompletionContributorCache()

    def extensionPoint = Extensions.rootArea.getExtensionPoint("com.intellij.completion.contributor")
    CompletionContributorEP completionContributorEP = changeGlobalVar(id) { lastContributorEP ->
        if (lastContributorEP != null && extensionPoint.hasExtension(lastContributorEP)) {
            extensionPoint.unregisterExtension(lastContributorEP)
        }
        createContributorEP(completionType, elementPattern, callback)
    }
    extensionPoint.registerExtension(completionContributorEP)
}

static resetCompletionContributorCache() {
    // couldn't find any other way to replace extensions in cache, therefore clearing using reflection
    // (see com.intellij.openapi.util.KeyedExtensionCollector#forKey)
    def field = KeyedExtensionCollector.class.getDeclaredField("myCache")
    field.accessible = true
    field.get(CompletionContributor.MyExtensionPointManager.INSTANCE).clear()
}

static createContributorEP(CompletionType completionType, ElementPattern elementPattern, Closure callback) {
    def contributor = new CompletionContributor(){}.with{
        extend(completionType, elementPattern, new CompletionProvider<CompletionParameters>() {
            @Override protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result) {
                callback(parameters, context, result)
            }
        })
        it
    }
    def contributorEP = new CompletionContributorEP() {
        @Override CompletionContributor getInstance() { contributor }
        @Override String getKey() { "any" }
        @Override protected Object instantiateExtension(String implementationClass, PicoContainer picoContainer) {
            contributor
        }
    }
    contributorEP.pluginDescriptor = new DefaultPluginDescriptor("liveplugin")
    contributorEP
}

static List<String> googleSuggestionsFor(String text) {
    text = URLEncoder.encode(text)
    def json = "http://suggestqueries.google.com/complete/search?client=firefox&q=$text".toURL().text
    new JsonSlurper().parseText(json)[1].toList()
}

static String lastWord(int offset, CharSequence text) {
    def i = (offset-1..0).find{!Character.isLetter(text.charAt(it))}
    if (i == null) i = 0 else i++
    text[i..offset-1]
}

static String toCamelHumps(String s) {
    def i = s.indexOf(" ")
    if (i == -1) s
    else {
        def beforeSpace = s[0..<i]
        def restOfString = (s.length() > i + 2 ? s[i+2..-1] : "")
        toCamelHumps(beforeSpace + s[i + 1].toUpperCase() + restOfString)
    }
}

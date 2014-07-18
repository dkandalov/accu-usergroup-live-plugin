import com.intellij.openapi.application.PathManager

import static liveplugin.PluginUtil.show

def path = PathManager.pluginsPath + "/live-plugins/0.1-scriptEngine"
def classLoader = new GroovyClassLoader(this.class.classLoader)
def scriptEngine = new GroovyScriptEngine(path, classLoader)
def binding = new Binding()

show("Evaluating someScript.groovy")

scriptEngine.run(path + "/someScript.groovy", binding)

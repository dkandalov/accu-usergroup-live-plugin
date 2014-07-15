import static liveplugin.PluginUtil.accessField
import static liveplugin.PluginUtil.show

def classLoaderParents = parentsOf(this.class.classLoader)
show(classLoaderParents.join("\n"))

def parentsOf(ClassLoader classLoader) {
    if (classLoader == null) {
        ["bootstrap"]
    } else if (classLoader.class.simpleName == "PluginClassLoader") {
        def delegateClassLoaders = []
        accessField(classLoader, "a"){ delegateClassLoaders = it }
        parentsOf(delegateClassLoaders[0]) + classLoader
    } else {
        parentsOf(classLoader.parent) + classLoader
    }
}
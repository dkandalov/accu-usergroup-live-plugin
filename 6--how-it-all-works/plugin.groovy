import liveplugin.PluginUtil

PluginUtil.showInConsole("""
    ClassLoader classLoader = createClassLoaderWithDependencies(pathsToAdd, mainScriptUrl, pluginId, errorReporter);
    GroovyScriptEngine scriptEngine = new GroovyScriptEngine(pluginFolderUrl, classLoader);
    scriptEngine.loadScriptByName(mainScriptUrl);
""")
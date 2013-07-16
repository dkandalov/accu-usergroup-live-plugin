import static liveplugin.PluginUtil.*

show(execute("pwd").stdout)
show(execute("ls", "-l").stdout)

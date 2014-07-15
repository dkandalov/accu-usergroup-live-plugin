import static liveplugin.PluginUtil.execute
import static liveplugin.PluginUtil.show

show(execute("pwd").stdout)
show(execute("ls").stdout)

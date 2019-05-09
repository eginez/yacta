@file:JvmName("MainCli")

package xyz.eginez.yacta.ui

import org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmLocalScriptEngine
import picocli.CommandLine
import picocli.CommandLine.Command
import xyz.eginez.yacta.core.Resource
import java.io.File
import javax.script.ScriptEngineManager

@Command(name = "yacta",
        description = ["build infrastructure via dsls"],
        mixinStandardHelpOptions = true,
        subcommands = [YactaCreate::class]
)
class Yacta

@Command(name = "create", description = ["evaluates and creates the infrastructure specified in the named file"])
class YactaCreate : Runnable {
    private val scriptEngine =
            ScriptEngineManager().getEngineByExtension("kts") as KotlinJsr223JvmLocalScriptEngine

    @CommandLine.Parameters(index = "0",
            description = ["script file containing infrastructure definition"],
            paramLabel = "scriptFile")
    private var file: File? = null

    override fun run() {
        println("Processing script: ${file?.absolutePath}")
        val resource = evaluateScript(scriptEngine, file?.readText())
        println(resource)
        //resource.create()
    }
}

fun evaluateScript(engine: KotlinJsr223JvmLocalScriptEngine, content: String?): Resource {
    check(content != null, { "script content can not be empty" })
    val script = engine.eval(content) as? Resource
            ?: throw Exception("Script should return a Resource")
    return script
}


fun main(args: Array<String>) {
    val commandLine = CommandLine(Yacta())
    if (args.isEmpty()) {
        commandLine.usage(System.out)
        return
    }
    commandLine.parseWithHandler(CommandLine.RunLast(), args)
}

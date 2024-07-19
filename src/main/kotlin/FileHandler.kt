import com.github.sarahbuisson.kotlinparser.KotlinParser
import com.github.sarahbuisson.kotlinparser.KotlinParserBaseListener
import com.github.sarahbuisson.kotlinparser.KotlinParserListener

class FileHandler {
    fun parseFile(string: String): String {
        /*
        1. Check if have braces, if not go to 1.1, if yes go to 1.2
            1.1 -> Check if braces closes within the same line, if not go 1.1.1
            1.1.1 ->
            1.2 -> Extract and send for parsing

         */


        TODO()
    }
}

class ClassListenerTest: KotlinParserBaseListener() {
    override fun enterImportList(ctx: KotlinParser.ImportListContext?) {
        ctx?.importHeader()?.forEach {
            println("Import Test -> ${it.identifier() .text}")
        }
    }

    override fun enterFunctionBody(ctx: KotlinParser.FunctionBodyContext?) {
        super.enterFunctionBody(ctx)
//        ctx?.children?.forEach {
//        println("Fucntion Test -> ${it.text}")
//
//        }
    }

    override fun enterObjectDeclaration(ctx: KotlinParser.ObjectDeclarationContext?) {
        super.enterObjectDeclaration(ctx)
        ctx?.NL()?.forEach {
        println("Object Test -> ${it.text}")

        }
    }

    override fun enterAnnotations(ctx: KotlinParser.AnnotationsContext?) {
        super.enterAnnotations(ctx)
    }

    override fun enterAnnotation(ctx: KotlinParser.AnnotationContext?) {
        super.enterAnnotation(ctx)
    }


}
package flashcard

import java.io.File

data class Term(
    val name: String,
)
data class Definition(
    val name: String,
    var wrongGuesses: Int = 0,
)

class FlashCards(val cliArgument: MutableMap<String, String> = mutableMapOf<String, String>())
{
    private var log: MutableList<String> = mutableListOf()
    private val deck: MutableMap<Term, Definition> = mutableMapOf()
    private var isAppRunning: Boolean = false
    fun runApp() {
        isAppRunning = true
        importAtAppStart()
        while (isAppRunning) {
            println("Input the action (add, remove, import, export, ask, exit, log, hardest card, reset stats):")
            val action = readln()
            when (action) {
                "add" -> onAction(InputAction.ADD_CARD)
                "import" -> onAction(InputAction.IMPORT)
                "export" -> onAction(InputAction.EXPORT)
                "ask" -> onAction(InputAction.ASK)
                "remove" -> onAction(InputAction.REMOVE_CARD)
                "reset stats" -> onAction(InputAction.RESET_STATS)
                "log" -> onAction(InputAction.LOG)
                "hardest card" -> onAction(InputAction.HARDEST_CARD)
                "exit" -> {
                    exportAtAppClose()
                    println("Bye bye!")

                    isAppRunning = false
                }
                else -> {
                    println("Unrecognized action")
                }
            }
        }
    }
    private fun exportAtAppClose() {
        if(cliArgument.contains("-export")) {
            val file = File(cliArgument["-export"])
            var textToSave = ""
            for ((term, definition) in deck) {
                textToSave += "${term.name}\n${definition.name}\n${definition.wrongGuesses}\n"
            }
            try {
                file.writeText(textToSave)
                println("${deck.size} cards have been saved.")
            } catch (e: Exception) {
                println(e.message)
            }
        }
    }
    private fun importAtAppStart() {
        if(cliArgument.contains("-import"))
        {
            val file = File(cliArgument["-import"])
            if(!file.exists()) {
                println("File not found.")

                return
            }
            val lines = file.readLines()
            var term = ""
            for(i in 0 until (lines.size - 1) step 3) {
                deck[Term(lines[i])] = Definition(name = lines[i + 1], wrongGuesses = lines[i + 2].toInt())
            }
            println("${lines.size / 3} cards have been loaded.")
        }
    }

    private fun onAction(action: InputAction) {
        when(action) {
            InputAction.ADD_CARD -> addingCard()
            InputAction.IMPORT -> {import()}
            InputAction.REMOVE_CARD -> {remove()}
            InputAction.EXPORT -> {export()}
            InputAction.ASK -> {ask()}
            InputAction.LOG -> {log()}
            InputAction.HARDEST_CARD -> {hardestCard()}
            InputAction.RESET_STATS -> {resetStats()}
        }
    }
    private fun hardestCard() {
        val hardestCards = deck.maxByOrNull { it.value.wrongGuesses }

        if(hardestCards == null) {
            println("There are no cards with errors")
            return
        }
        if ( hardestCards.value.wrongGuesses == 0) {
            println("There are no cards with errors")
            return
        }

        var terms: String = ""
        var howManyTerms = 0
        for((k, v) in deck) {
            if(v.wrongGuesses == hardestCards.value.wrongGuesses) {
                terms += "\"" + k.name + "\", "
                howManyTerms++
            }
        }

        terms = terms.dropLast(2)
        if(howManyTerms == 1) {
            println("The hardest card is $terms. You have ${hardestCards.value.wrongGuesses} errors answering it.")
        } else {
            println("The hardest cards are $terms. You have ${hardestCards.value.wrongGuesses} errors answering them.")

        }

    }
    private fun log() {
        println("File name:")
        val fileName = readln()
        val file = File(fileName)
        file.writeText(log.joinToString ("\n"))
        println("The log has been saved.")
    }

    private fun resetStats() {
        for((k, v) in deck)
            v.wrongGuesses = 0
        println("Card statistics have been reset.")
    }


    private fun remove() {
        println("Which card?")
        val termOfCardToRemove = readln()
        if( deck.remove(Term(termOfCardToRemove)) != null) {
            println("The card has been removed.")
        } else {
            println("Can't remove \"$termOfCardToRemove\": there is no such card.")
        }
    }
    private fun ask() {
        println("How many times to ask?")
        var numberOfCardsToAsk = 0
        try {
            numberOfCardsToAsk = readln().toInt()
        } catch(e: Exception) {
            println(e.message)
            return
        }
        val terms = deck.keys
        for(i in 0 until numberOfCardsToAsk) {
            val question = terms.random()
            println("Print the definition of \"${question.name}\":")

            val answer = readln()

            if (answer == deck[question]?.name) {
                println("Correct!")

            } else if (deck.any {it.value.name == answer}) {
                val wrongTermGuess = deck.filter { it.value.name == answer }.keys
                println("Wrong. The right answer is \"${deck[question]?.name}\", but your definition is correct for \"${wrongTermGuess.elementAt(0).name}\" card.")

                deck[question]!!.wrongGuesses++
            } else {
                println("Wrong. The right answer is \"${deck[question]?.name}\".")

                deck[question]!!.wrongGuesses++
            }
        }
    }
    private fun import() {
        println("File name:")

        val fileName = readln()

        val file = File(fileName)
        if(!file.exists()) {
            println("File not found.")
            return
        }
        val lines = file.readLines()
        var term = ""
        for(i in 0 until (lines.size - 1) step 3) {
            deck[Term(lines[i])] = Definition(name = lines[i + 1], wrongGuesses = lines[i + 2].toInt())
        }
        println("${lines.size / 3} cards have been loaded.")

    }
    private fun println(text: String) {
        kotlin.io.println(text)
        log.add(text)
    }
    private fun readln(): String {
        val input = kotlin.io.readln()
        log.add("> $input")
        return input
    }

    private fun export() {
        println("File name:")
        val fileName = readln()
        val file = File(fileName)
        var textToSave = ""
        for((term, definition) in deck) {
            textToSave += "${term.name}\n${definition.name}\n${definition.wrongGuesses}\n"
        }
        try {
            file.writeText(textToSave)
            println("${deck.size} cards have been saved.")
        }
        catch (e: Exception) {
            println(e.message)
        }
    }
    private fun addingCard()
    {
        println("The card: ")

        val term = readln()

        if(!isTermUnique(term))
            return
        println("The definition of the card:")

        val definition = readln()

        if(!isDefinitionUnique(definition))
            return
        deck[Term(term)] = Definition(definition)
        println("The pair (\"$term\":\"$definition\") has been added.")

    }
    private fun isTermUnique(term: String): Boolean {
        if (deck.keys.any { it.name == term }) {
            println("The card \"$term\" already exists.")
            return false
        }
        return true
    }
    private fun isDefinitionUnique(definition: String): Boolean {
        if (deck.values.any { it.name == definition}) {
            println("The definition \"$definition\" already exists.")

            return false
        }
        return true
    }

}

enum class InputAction{
    ADD_CARD,
    REMOVE_CARD,
    IMPORT,
    EXPORT,
    ASK,
    LOG,
    HARDEST_CARD,
    RESET_STATS,
}

fun main(args: Array<String>) {
    val runArguments = mutableMapOf<String, String>()
    for(i in  0.. args.lastIndex step 2) {
        if(args[i] == "-export" && i + 1 <= args.lastIndex)
        {
            runArguments["-export"] = args[i + 1]
        }
        if(args[i] == "-import" && i + 1 <= args.lastIndex)
        {
            runArguments["-import"] = args[i + 1]
        }
    }
    val flashCards = FlashCards(runArguments)

    flashCards.runApp()
}
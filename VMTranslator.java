/**
 * This is the main program that drives the translation process, using the services of a <code>Parser<code> and a
 * <code>CodeWriter<code>. The program gets the name of the input source file, say Prog<code>.vm<code>, from the
 * command-line argument. It constructs a <code>Parser<code> for parsing the input file Prog<code>.vm<code> and creates
 * an output file, Prog<code>.asm<code>, into which it will write the translated assembly instructions. The program then
 * enters a loop that iterates through the VM commands in the input file. For each command, the program uses the
 * <code>Parser<code> and the <code>CodeWriter<code> services for parsing the command into its fields and then
 * generating from them a sequence of assembly instructions. The instructions are written into the output
 * Prog<code>.asm<code> file.
 *
 * @author Maarten Derks
 */
class VMTranslator {

    public static void main(String[] args) {

    }
}

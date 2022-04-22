import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

/**
 * This module translates a parsed VM command into Hack assembly code.
 *
 * For example, calling <code>writePushPop(C_PUSH,"local",2)</code> would result in generating assembly instructions
 * that implement the VM command <code>push local 2</code>. Another example: Calling <code>writeArithmetic("add")</code>
 * would result in generating assembly instructions that pop the two topmost elements from the stack, add them up, and
 * push the result onto the stack.
 *
 * @author Maarten Derks
 */
class CodeWriter {

    private PrintWriter printWriter;

    /**
     * Opens the output file / stream and gets ready to write into it.
     *
     * @param file  Output file / stream
     */
    CodeWriter(File file) throws Exception {
        printWriter = new PrintWriter(new FileWriter(file));
    }

    /**
     * Writes to the output file the given command
     * Temporary method
     *
     * @param command   The command to be written
     * @throws Exception
     */
    void write(String command) {
        printWriter.println(command);
    }

    /**
     * Writes to the output file the assembly code that implements the given command,
     * where command is either C_PUSH or C_POP.
     *
     * @param command
     * @param segment
     * @param index
     * @throws Exception
     */
    void writePushPop(Parser.CommandType command, String segment, int index) {
        switch (command) {
            case C_PUSH:
                switch (segment) {
                    case "constant":
                        printWriter.println("@" + index);
                        printWriter.println("D=A");
                        printWriter.println("@SP");
                        printWriter.println("A=M");
                        printWriter.println("M=D");
                        printWriter.println("@SP");
                        printWriter.println("M=M+1");
                        break;
                    default:
                        write(Parser.currentCommand);
                        break;
                }
                break;
        }
    }

    /**
     * Closes the output file.
     */
    void close() {
        printWriter.close();
    }
}

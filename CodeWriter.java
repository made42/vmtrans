import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

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

    private Map<String, String> segmentMap;

    /**
     * Opens the output file / stream and gets ready to write into it.
     *
     * @param file  Output file / stream
     */
    CodeWriter(File file) throws Exception {
        printWriter = new PrintWriter(new FileWriter(file));
        segmentMap = new HashMap<>();
        segmentMap.put("local", "LCL");
        segmentMap.put("argument", "ARG");
        segmentMap.put("this", "THIS");
        segmentMap.put("that", "THAT");
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
     * Writes to the output file the assembly code that implements the given arithmetic command.
     *
     * @param command   The command to be written
     */
    void writeArithmetic(String command) {
        switch(command) {
            case "add": // x + y
                printWriter.println("@SP");
                printWriter.println("AM=M-1");
                printWriter.println("D=M");
                printWriter.println("@SP");
                printWriter.println("A=M-1");
                printWriter.println("M=M+D");
                break;
            case "sub": // x - y
                printWriter.println("@SP");
                printWriter.println("AM=M-1");
                printWriter.println("D=M");
                printWriter.println("@SP");
                printWriter.println("A=M-1");
                printWriter.println("M=M-D");
                break;
            default:
                write(Parser.currentCommand);
                break;
        }
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
                    case "local":
                    case "argument":
                    case "this":
                    case "that":
                        printWriter.println("@" + segmentMap.get(segment));
                        printWriter.println("D=M");
                        printWriter.println("@" + index);
                        printWriter.println("D=D+A");
                        printWriter.println("@addr");
                        printWriter.println("M=D");
                        printWriter.println("@addr");
                        printWriter.println("A=M");
                        printWriter.println("D=M");
                        printWriter.println("@SP");
                        printWriter.println("A=M");
                        printWriter.println("M=D");
                        printWriter.println("@SP");
                        printWriter.println("M=M+1");
                        break;
                    case "temp":
                        printWriter.println("@5");
                        printWriter.println("D=A");
                        printWriter.println("@" + index);
                        printWriter.println("D=D+A");
                        printWriter.println("@addr");
                        printWriter.println("M=D");
                        printWriter.println("@addr");
                        printWriter.println("A=M");
                        printWriter.println("D=M");
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
            case C_POP:
                switch (segment) {
                    case "local":
                    case "argument":
                    case "this":
                    case "that":
                        printWriter.println("@" + segmentMap.get(segment));
                        printWriter.println("D=M");
                        printWriter.println("@" + index);
                        printWriter.println("D=D+A");
                        printWriter.println("@addr");
                        printWriter.println("M=D");
                        printWriter.println("@SP");
                        printWriter.println("M=M-1");
                        printWriter.println("@SP");
                        printWriter.println("A=M");
                        printWriter.println("D=M");
                        printWriter.println("@addr");
                        printWriter.println("A=M");
                        printWriter.println("M=D");
                        break;
                    case "temp":
                        printWriter.println("@5");
                        printWriter.println("D=A");
                        printWriter.println("@" + index);
                        printWriter.println("D=D+A");
                        printWriter.println("@addr");
                        printWriter.println("M=D");
                        printWriter.println("@SP");
                        printWriter.println("M=M-1");
                        printWriter.println("@SP");
                        printWriter.println("A=M");
                        printWriter.println("D=M");
                        printWriter.println("@addr");
                        printWriter.println("A=M");
                        printWriter.println("M=D");
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

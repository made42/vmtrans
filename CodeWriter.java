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

    private Map<String, String> mappings;

    private int counter = 0;

    /**
     * Opens the output file / stream and gets ready to write into it.
     *
     * @param file  Output file / stream
     */
    CodeWriter(File file) throws Exception {
        printWriter = new PrintWriter(new FileWriter(file));
        mappings = new HashMap<>();
        mappings.put("local", "LCL");
        mappings.put("argument", "ARG");
        mappings.put("this", "THIS");
        mappings.put("that", "THAT");
        mappings.put("add", "+");
        mappings.put("sub", "-");
        mappings.put("and", "&");
        mappings.put("or", "|");
        mappings.put("neg", "-");
        mappings.put("not", "!");
        mappings.put("eq", "JEQ");
        mappings.put("gt", "JGT");
        mappings.put("lt", "JLT");
    }

    /**
     * Writes to the output file the assembly code that implements the given arithmetic command.
     *
     * @param command   The command to be written
     */
    void writeArithmetic(String command) {
        printWriter.println("@SP");
        switch(command) {
            case "neg": // -y
            case "not": // not x
                printWriter.println("A=M-1");
                printWriter.println("M=" + mappings.get(command) + "M");
                break;
            case "add": // x + y
            case "sub": // x - y
            case "and": // x and y
            case "or":  // x or y
                printWriter.println("AM=M-1");
                printWriter.println("D=M");
                printWriter.println("A=A-1");
                printWriter.println("M=M" + mappings.get(command) + "D");
                break;
            case "eq":  // x == y
            case "gt":  // x > y
            case "lt":  // x < y
                printWriter.println("AM=M-1");
                printWriter.println("D=M");
                printWriter.println("A=A-1");
                printWriter.println("D=M-D");
                printWriter.println("M=0");
                printWriter.println("@" + command + counter);
                printWriter.println("D;" + mappings.get(command));
                printWriter.println("@" + command + "cont" + counter);
                printWriter.println("0;JMP");
                printWriter.println("(" + command + counter+ ")");
                printWriter.println("@SP");
                printWriter.println("A=M-1");
                printWriter.println("M=-1");
                printWriter.println("(" + command + "cont" + counter + ")");
                counter++;
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
                        break;
                    case "local":
                    case "argument":
                    case "this":
                    case "that":
                    case "temp":
                        if (segment.equals("temp")) {
                            printWriter.println("@5");
                            printWriter.println("D=A");
                        } else {
                            printWriter.println("@" + mappings.get(segment));
                            printWriter.println("D=M");
                        }
                        printWriter.println("@" + index);
                        printWriter.println("D=D+A");
                        printWriter.println("@addr");
                        printWriter.println("M=D");
                        printWriter.println("@addr");
                        printWriter.println("A=M");
                        printWriter.println("D=M");
                        break;
                    case "pointer":
                        if (index == 0) printWriter.println("@THIS");
                        else if (index == 1) printWriter.println("@THAT");
                        printWriter.println("D=M");
                        break;
                    case "static":
                        printWriter.println("@Foo." + index);
                        printWriter.println("D=M");
                        break;
                }
                printWriter.println("@SP");
                printWriter.println("A=M");
                printWriter.println("M=D");
                printWriter.println("@SP");
                printWriter.println("M=M+1");
                break;
            case C_POP:
                switch (segment) {
                    case "local":
                    case "argument":
                    case "this":
                    case "that":
                    case "temp":
                        if (segment.equals("temp")) {
                            printWriter.println("@5");
                            printWriter.println("D=A");
                        } else {
                            printWriter.println("@" + mappings.get(segment));
                            printWriter.println("D=M");
                        }
                        printWriter.println("@" + index);
                        printWriter.println("D=D+A");
                        printWriter.println("@addr");
                        printWriter.println("M=D");
                        printWriter.println("@SP");
                        printWriter.println("AM=M-1");
                        printWriter.println("D=M");
                        printWriter.println("@addr");
                        printWriter.println("A=M");
                        break;
                    case "pointer":
                        printWriter.println("@SP");
                        printWriter.println("AM=M-1");
                        printWriter.println("D=M");
                        if (index == 0) printWriter.println("@THIS");
                        else if (index == 1) printWriter.println("@THAT");
                        break;
                    case "static":
                        printWriter.println("@SP");
                        printWriter.println("AM=M-1");
                        printWriter.println("D=M");
                        printWriter.println("@Foo." + index);
                        break;
                }
                printWriter.println("M=D");
                break;
        }
    }

    /**
     * Writes to the output file the assembly code that implements an infinite loop.
     * To be called once, after translating all the VM commands.
     */
    void writeEnd() {
        printWriter.println("(END)");
        printWriter.println("@END");
        printWriter.println("0;JMP");
    }

    /**
     * Closes the output file.
     */
    void close() {
        printWriter.close();
    }
}

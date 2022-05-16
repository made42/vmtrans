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
    private int retCounter = 0;

    private String functionLabel;
    private String fileName;

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
     * Informs the codeWriter that the translation of a new VM file has started (called by the main program of the VM
     * translator).
     *
     * @param fileName
     */
    void setFileName(String fileName) {
        this.fileName = fileName.substring(0, fileName.indexOf('.'));
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
                        printWriter.println("@addr" + index);
                        printWriter.println("M=D");
                        printWriter.println("@addr" + index);
                        printWriter.println("A=M");
                        printWriter.println("D=M");
                        break;
                    case "pointer":
                        if (index == 0) printWriter.println("@THIS");
                        else if (index == 1) printWriter.println("@THAT");
                        printWriter.println("D=M");
                        break;
                    case "static":
                        printWriter.println("@" + fileName + "." + index);
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
                        printWriter.println("@addr" + index);
                        printWriter.println("M=D");
                        printWriter.println("@SP");
                        printWriter.println("AM=M-1");
                        printWriter.println("D=M");
                        printWriter.println("@addr" + index);
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
                        printWriter.println("@" + fileName + "." + index);
                        break;
                }
                printWriter.println("M=D");
                break;
        }
    }

    /**
     * Writes assembly code that effects the <code>label</code> command.
     *
     * @param label
     */
    void writeLabel(String label) {
        printWriter.println("(" + label + ")");
    }

    /**
     * Writes assembly code that effects the <code>goto</code> command.
     *
     * @param label
     */
    void writeGoto(String label) {
        printWriter.println("@" + label);
        printWriter.println("0;JMP");
    }

    /**
     * Writes assembly code that effects the <code>if-goto</code> command.
     *
     * @param label
     */
    void writeIf(String label) {
        printWriter.println("@SP");
        printWriter.println("AM=M-1");
        printWriter.println("D=M");
        printWriter.println("@" + label);
        printWriter.println("D;JNE");
    }

    /**
     * Writes assembly code that effects the <code>function</code> command.
     *
     * @param label
     * @param nVars
     */
    void writeFunction(String label, int nVars) {
        this.functionLabel = label;
        printWriter.println("(" + label + ")");
        for (int i = 0; i < nVars; i++) {
            printWriter.println("@" + mappings.get("local"));
            printWriter.println("D=M");
            printWriter.println("@" + i);
            printWriter.println("A=D+A");
            printWriter.println("M=0");
            printWriter.println("@SP");
            printWriter.println("M=M+1");
        }
    }

    /**
     * Writes assembly code that effects the <code>call</code> command.
     *
     * @param functionName
     * @param nArgs
     */
    void writeCall(String functionName, int nArgs) {
        // push returnAddress
        printWriter.println("@" + functionLabel + "$ret." + retCounter);
        printWriter.println("D=A");
        printWriter.println("@SP");
        printWriter.println("A=M");
        printWriter.println("M=D");
        printWriter.println("@SP");
        printWriter.println("M=M+1");

        // push LCL, ARG, THIS, THAT
        for (String segment : new String[]{"LCL","ARG","THIS","THAT"} ) {
            printWriter.println("@" + segment);
            printWriter.println("D=M");
            printWriter.println("@SP");
            printWriter.println("A=M");
            printWriter.println("M=D");
            printWriter.println("@SP");
            printWriter.println("M=M+1");
        }

        // ARG = SP-5-nArgs
        printWriter.println("@SP");
        printWriter.println("D=M");
        printWriter.println("@5");
        printWriter.println("D=D-A");
        printWriter.println("@" + nArgs);
        printWriter.println("D=D-A");
        printWriter.println("@ARG");
        printWriter.println("M=D");

        // LCL = SP
        printWriter.println("@SP");
        printWriter.println("D=M");
        printWriter.println("@LCL");
        printWriter.println("M=D");

        // goto f
        writeGoto(functionName);

        printWriter.println("(" + functionLabel + "$ret." + retCounter + ")");

        retCounter++;
    }

    /**
     * Writes assembly code that effects the <code>return</code> command.
     */
    void writeReturn() {
        // frame is a temporary variable
        printWriter.println("@LCL");
        printWriter.println("D=M");
        printWriter.println("@frame");
        printWriter.println("M=D");

        // retAddr = *(frame-5)
        printWriter.println("@frame");
        printWriter.println("D=M");
        printWriter.println("@5");
        printWriter.println("A=D-A");
        printWriter.println("D=M");
        printWriter.println("@retAddr");
        printWriter.println("M=D");

        // *ARG = pop()
        printWriter.println("@SP");
        printWriter.println("AM=M-1");
        printWriter.println("D=M");
        printWriter.println("@ARG");
        printWriter.println("A=M");
        printWriter.println("M=D");

        // SP = ARG + 1
        printWriter.println("@ARG");
        printWriter.println("D=M+1");
        printWriter.println("@SP");
        printWriter.println("M=D");

        // THAT = *(frame-1)
        // THIS = *(frame-2)
        // ARG = *(frame-3)
        // LCL = *(frame-4)
        for (String segment : new String[]{"THAT","THIS","ARG","LCL"} ) {
            printWriter.println("@frame");
            printWriter.println("AM=M-1");
            printWriter.println("D=M");
            printWriter.println("@" + segment);
            printWriter.println("M=D");
        }

        // goto retAddr
        printWriter.println("@retAddr");
        printWriter.println("A=M");
        printWriter.println("0;JMP");
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

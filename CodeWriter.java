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

    private PrintWriter pw;

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
        pw = new PrintWriter(new FileWriter(file));
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
     * Writes the assembly instructions that effect the bootstrap code that initializes the VM. This code must be placed
     * at the beginning of the generated <code>*.asm</code> file.
     */
    void writeInit() {
        // SP=256
        pw.println("@256");
        pw.println("D=A");
        pw.println("@SP");
        pw.println("M=D");
        // call Sys.init
        writeCall("Sys.init", 0);
    }

    /**
     * Writes to the output file the assembly code that implements the given arithmetic command.
     *
     * @param command   The command to be written
     */
    void writeArithmetic(String command) {
        switch(command) {
            case "neg": // -y
            case "not": // not x
                pw.println("@SP");
                pw.println("A=M-1");
                pw.println("M=" + mappings.get(command) + "M");
                break;
            case "add": // x + y
            case "sub": // x - y
            case "and": // x and y
            case "or":  // x or y
                popFromStack();
                pw.println("A=A-1");
                pw.println("M=M" + mappings.get(command) + "D");
                break;
            case "eq":  // x == y
            case "gt":  // x > y
            case "lt":  // x < y
                popFromStack();
                pw.println("A=A-1");
                pw.println("D=M-D");
                pw.println("M=0");
                pw.println("@" + command + counter);
                pw.println("D;J" + command.toUpperCase());
                pw.println("@" + command + "cont" + counter);
                pw.println("0;JMP");
                pw.println("(" + command + counter + ")");
                pw.println("@SP");
                pw.println("A=M-1");
                pw.println("M=-1");
                pw.println("(" + command + "cont" + counter + ")");
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
    void writePushPop(CommandType command, String segment, int index) {
        switch (command) {
            case C_PUSH:
                switch (segment) {
                    case "constant":
                        pw.println("@" + index);
                        pw.println("D=A");
                        break;
                    case "local":
                    case "argument":
                    case "this":
                    case "that":
                    case "temp":
                        if (segment.equals("temp")) {
                            pw.println("@5");
                            pw.println("D=A");
                        } else {
                            pw.println("@" + mappings.get(segment));
                            pw.println("D=M");
                        }
                        pw.println("@" + index);
                        pw.println("D=D+A");
                        pw.println("@addr" + index);
                        pw.println("M=D");
                        pw.println("@addr" + index);
                        pw.println("A=M");
                        pw.println("D=M");
                        break;
                    case "pointer":
                        if (index == 0) pw.println("@THIS");
                        else if (index == 1) pw.println("@THAT");
                        pw.println("D=M");
                        break;
                    case "static":
                        pw.println("@" + fileName + "." + index);
                        pw.println("D=M");
                        break;
                }
                pushToStack();
                break;
            case C_POP:
                switch (segment) {
                    case "local":
                    case "argument":
                    case "this":
                    case "that":
                    case "temp":
                        if (segment.equals("temp")) {
                            pw.println("@5");
                            pw.println("D=A");
                        } else {
                            pw.println("@" + mappings.get(segment));
                            pw.println("D=M");
                        }
                        pw.println("@" + index);
                        pw.println("D=D+A");
                        pw.println("@addr" + index);
                        pw.println("M=D");
                        popFromStack();
                        pw.println("@addr" + index);
                        pw.println("A=M");
                        break;
                    case "pointer":
                        popFromStack();
                        if (index == 0) pw.println("@THIS");
                        else if (index == 1) pw.println("@THAT");
                        break;
                    case "static":
                        popFromStack();
                        pw.println("@" + fileName + "." + index);
                        break;
                }
                pw.println("M=D");
                break;
        }
    }

    /**
     * Writes assembly code that effects the <code>label</code> command.
     *
     * @param label
     */
    void writeLabel(String label) {
        pw.println("(" + label + ")");
    }

    /**
     * Writes assembly code that effects the <code>goto</code> command.
     *
     * @param label
     */
    void writeGoto(String label) {
        pw.println("@" + label);
        pw.println("0;JMP");
    }

    /**
     * Writes assembly code that effects the <code>if-goto</code> command.
     *
     * @param label
     */
    void writeIf(String label) {
        popFromStack();
        pw.println("@" + label);
        pw.println("D;JNE");
    }

    /**
     * Writes assembly code that effects the <code>function</code> command.
     *
     * @param label
     * @param nVars
     */
    void writeFunction(String label, int nVars) {
        this.functionLabel = label;
        writeLabel(label);
        for (int i = 0; i < nVars; i++) {
            pw.println("@" + mappings.get("local"));
            pw.println("D=M");
            pw.println("@" + i);
            pw.println("A=D+A");
            pw.println("M=0");
            incrementSP();
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
        pw.println("@" + functionLabel + "$ret." + retCounter);
        pw.println("D=A");
        pushToStack();
        // push LCL, ARG, THIS, THAT
        for (String segment : new String[]{"LCL","ARG","THIS","THAT"} ) {
            pw.println("@" + segment);
            pw.println("D=M");
            pushToStack();
        }
        // ARG = SP-5-nArgs
        pw.println("@SP");
        pw.println("D=M");
        pw.println("@5");
        pw.println("D=D-A");
        pw.println("@" + nArgs);
        pw.println("D=D-A");
        pw.println("@ARG");
        pw.println("M=D");
        // LCL = SP
        pw.println("@SP");
        pw.println("D=M");
        pw.println("@LCL");
        pw.println("M=D");
        // goto f
        writeGoto(functionName);
        writeLabel(functionLabel + "$ret." + retCounter);
        retCounter++;
    }

    /**
     * Writes assembly code that effects the <code>return</code> command.
     */
    void writeReturn() {
        // frame is a temporary variable
        pw.println("@LCL");
        pw.println("D=M");
        pw.println("@frame");
        pw.println("M=D");
        // retAddr = *(frame-5)
        pw.println("@frame");
        pw.println("D=M");
        pw.println("@5");
        pw.println("A=D-A");
        pw.println("D=M");
        pw.println("@retAddr");
        pw.println("M=D");
        // *ARG = pop()
        popFromStack();
        pw.println("@ARG");
        pw.println("A=M");
        pw.println("M=D");
        // SP = ARG + 1
        pw.println("@ARG");
        pw.println("D=M+1");
        pw.println("@SP");
        pw.println("M=D");
        // THAT = *(frame-1)
        // THIS = *(frame-2)
        // ARG = *(frame-3)
        // LCL = *(frame-4)
        for (String segment : new String[]{"THAT","THIS","ARG","LCL"} ) {
            pw.println("@frame");
            pw.println("AM=M-1");
            pw.println("D=M");
            pw.println("@" + segment);
            pw.println("M=D");
        }
        // goto retAddr
        pw.println("@retAddr");
        pw.println("A=M");
        pw.println("0;JMP");
    }

    private void incrementSP() {
        pw.println("@SP");
        pw.println("M=M+1");
    }

    private void pushToStack() {
        pw.println("@SP");
        pw.println("A=M");
        pw.println("M=D");
        incrementSP();
    }

    private void popFromStack() {
        pw.println("@SP");
        pw.println("AM=M-1");
        pw.println("D=M");
    }

    /**
     * Writes to the output file the assembly code that implements an infinite loop.
     * To be called once, after translating all the VM commands.
     */
    void writeEnd() {
        pw.println("(END)");
        pw.println("@END");
        pw.println("0;JMP");
    }

    /**
     * Closes the output file.
     */
    void close() {
        pw.close();
    }
}

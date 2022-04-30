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

    private int eqcounter = 0;
    private int gtcounter = 0;
    private int ltcounter = 0;

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
            case "neg": // -y
                printWriter.println("@SP");
                printWriter.println("A=M-1");
                printWriter.println("M=-M");
                break;
            case "eq":  // x == y
                printWriter.println("@SP");
                printWriter.println("AM=M-1");
                printWriter.println("D=M");
                printWriter.println("A=A-1");
                printWriter.println("D=M-D");
                printWriter.println("M=0");
                printWriter.println("@eq" + eqcounter);
                printWriter.println("D;JEQ");
                printWriter.println("@cont" + eqcounter);
                printWriter.println("0;JMP");
                printWriter.println("(eq" + eqcounter+ ")");
                printWriter.println("@SP");
                printWriter.println("A=M-1");
                printWriter.println("M=-1");
                printWriter.println("(cont" + eqcounter + ")");
                eqcounter++;
                break;
            case "gt":  // x > y
                printWriter.println("@SP");
                printWriter.println("AM=M-1");
                printWriter.println("D=M");
                printWriter.println("A=A-1");
                printWriter.println("D=M-D");
                printWriter.println("M=0");
                printWriter.println("@gt" + gtcounter);
                printWriter.println("D;JGT");
                printWriter.println("@gtcont" + gtcounter);
                printWriter.println("0;JMP");
                printWriter.println("(gt" + gtcounter+ ")");
                printWriter.println("@SP");
                printWriter.println("A=M-1");
                printWriter.println("M=-1");
                printWriter.println("(gtcont" + gtcounter + ")");
                gtcounter++;
                break;
            case "lt":  // x < y
                printWriter.println("@SP");
                printWriter.println("AM=M-1");
                printWriter.println("D=M");
                printWriter.println("A=A-1");
                printWriter.println("D=M-D");
                printWriter.println("M=0");
                printWriter.println("@lt" + ltcounter);
                printWriter.println("D;JLT");
                printWriter.println("@ltcont" + ltcounter);
                printWriter.println("0;JMP");
                printWriter.println("(lt" + ltcounter+ ")");
                printWriter.println("@SP");
                printWriter.println("A=M-1");
                printWriter.println("M=-1");
                printWriter.println("(ltcont" + ltcounter + ")");
                ltcounter++;
                break;
            case "and": // x and y
                printWriter.println("@SP");
                printWriter.println("AM=M-1");
                printWriter.println("D=M");
                printWriter.println("@SP");
                printWriter.println("A=M-1");
                printWriter.println("M=M&D");
                break;
            case "or":  // x or y
                printWriter.println("@SP");
                printWriter.println("AM=M-1");
                printWriter.println("D=M");
                printWriter.println("@SP");
                printWriter.println("A=M-1");
                printWriter.println("M=M|D");
                break;
            case "not": // not x
                printWriter.println("@SP");
                printWriter.println("A=M-1");
                printWriter.println("M=!M");
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
                    case "pointer":
                        if (index == 0) {
                            printWriter.println("@THIS");
                        } else if (index == 1) {
                            printWriter.println("@THAT");
                        }
                        printWriter.println("D=M");
                        printWriter.println("@SP");
                        printWriter.println("A=M");
                        printWriter.println("M=D");
                        printWriter.println("@SP");
                        printWriter.println("M=M+1");
                        break;
                    case "static":
                        printWriter.println("@Foo." + index);
                        printWriter.println("D=M");
                        printWriter.println("@SP");
                        printWriter.println("A=M");
                        printWriter.println("M=D");
                        printWriter.println("@SP");
                        printWriter.println("M=M+1");
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
                    case "pointer":
                        printWriter.println("@SP");
                        printWriter.println("AM=M-1");
                        printWriter.println("D=M");
                        if (index == 0) {
                            printWriter.println("@THIS");
                        } else if (index == 1) {
                            printWriter.println("@THAT");
                        }
                        printWriter.println("M=D");
                        break;
                    case "static":
                        printWriter.println("@SP");
                        printWriter.println("AM=M-1");
                        printWriter.println("D=M");
                        printWriter.println("@Foo." + index);
                        printWriter.println("M=D");
                        break;
                }
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

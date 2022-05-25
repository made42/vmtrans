import java.io.File;

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

    private static final int MAX_ARGS = 1;
    private static final int FIRST_ARG = 0;
    private static final int FIRST_CHAR = 0;
    private static final String VM_EXT = ".vm";
    private static final String ASM_EXT = ".asm";

    private static CodeWriter cw;

    public static void main(String[] args) throws Exception {
        if (args.length == MAX_ARGS) {
            File inputFile = new File(args[FIRST_ARG]);
            if (inputFile.exists()) {
                if (inputFile.isFile()) {
                    handleFile(inputFile);
                } else if (inputFile.isDirectory()) {
                    handleDirectory(inputFile);
                }
            } else  {
                System.out.println("No such file or directory");
            }
        } else {
            System.out.println("Invalid number of arguments");
        }
    }

    static void handleFile(File file) throws Exception {
        String fileName = file.getName();
        if (fileName.endsWith(VM_EXT)) {
            if (Character.isUpperCase(fileName.charAt(FIRST_CHAR))) {
                cw = new CodeWriter(new File(file.getParent() + "/" + fileName.substring(FIRST_CHAR, fileName.indexOf('.')) + ASM_EXT));
                translate(file);
                cw.writeEnd();
                cw.close();
            } else {
                System.out.println("First character in file name must be an uppercase letter");
            }
        } else {
            System.out.println("Invalid file extension");
        }
    }

    static void handleDirectory(File dir) throws Exception {
        cw = new CodeWriter(new File(dir.getPath() + "/" + dir.getName() + ASM_EXT));
        cw.writeInit();
        File[] dirListing = dir.listFiles();
        if (dirListing != null) {
            for (File file : dirListing) {
                String fileName = file.getName();
                if (fileName.endsWith(VM_EXT)) {
                    if (Character.isUpperCase(fileName.charAt(FIRST_CHAR))) {
                        translate(file);
                    } else {
                        System.out.println("First character in file name must be an uppercase letter");
                    }
                }
            }
            cw.close();
        }
    }

    static void translate(File file) throws Exception {
        cw.setFileName(file.getName());
        Parser p = new Parser(file);
        while (p.hasMoreLines()) {
            p.advance();
            switch (p.commandType()) {
                case C_PUSH:
                    cw.writePushPop(CommandType.C_PUSH, p.arg1(), p.arg2());
                    break;
                case C_POP:
                    cw.writePushPop(CommandType.C_POP, p.arg1(), p.arg2());
                    break;
                case C_ARITHMETIC:
                    cw.writeArithmetic(p.arg1());
                    break;
                case C_LABEL:
                    cw.writeLabel(p.arg1());
                    break;
                case C_GOTO:
                    cw.writeGoto(p.arg1());
                    break;
                case C_IF:
                    cw.writeIf(p.arg1());
                    break;
                case C_FUNCTION:
                    cw.writeFunction(p.arg1(), p.arg2());
                    break;
                case C_CALL:
                    cw.writeCall(p.arg1(), p.arg2());
                    break;
                case C_RETURN:
                    cw.writeReturn();
                    break;
            }
        }
    }
}

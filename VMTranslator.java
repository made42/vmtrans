import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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

    public static void main(String[] args) throws Exception {
        File in = new File(args[0]);
        List<File> files = new ArrayList<>();
        String outname;

        if (in.isFile()) {
            files.add(in.getAbsoluteFile());
            outname = in.getAbsolutePath().replaceFirst("[.][^.]+$", ".asm");
        } else {
            FilenameFilter filter = (dir, name) -> name.matches(".*.vm");
            files.addAll(Arrays.asList(Objects.requireNonNull(in.listFiles(filter))));
            outname = in.getAbsolutePath() + File.separator + in.getName() + ".asm";
        }

        CodeWriter cw = new CodeWriter(new File(outname));

        if (in.isDirectory()) {
            cw.writeInit();
        }

        for (File f : files) {
            cw.setFileName(f.getName());
            Parser p = new Parser(f);
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

        if (in.isFile()) {
            cw.writeEnd();
        }

        cw.close();
    }
}

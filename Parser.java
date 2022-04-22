import java.io.File;
import java.util.Scanner;

/**
 * This module handles the parsing of a single <code>.vm</code> file. The parser provides services for reading a VM
 * command, unpacking the command into its various components, and providing convenient access to these components. In
 * addition, the parser ignores all white space and comments. The parser is designed to handle all the VM commands,
 * including the branching and function command that will be implemented in chapter 8.
 *
 * For example, if the current command is <code>push local 2</code>, then calling <code>arg1()</code> and
 * <code>arg2()</code> would return, respectively, <code>"local"</code> and <code>2</code>. If the current command is
 * <code>add</code>, then calling <code>arg1()</code> would return <code>"add"</code>, and <code>arg2()</code> would not
 * be called.
 *
 * @author Maarten Derks
 */
class Parser {

    private static final String COMMENT_START = "//";

    private Scanner scanner;

    static String currentCommand;

    enum CommandType {
        C_PUSH,
        DEFAULT
    }

    /**
     * Opens the input file / stream, and gets ready to parse it.
     *
     * @param file Input file / stream
     * @throws Exception
     */
    Parser(File file) throws Exception {
        scanner = new Scanner(file);
    }

    /**
     * Are there more lines in the input?
     *
     * @return boolean
     */
    boolean hasMoreLines() {
        return scanner.hasNextLine();
    }

    /**
     * Reads the next command from the input and makes it the current command.
     * This routing should be called only if {@link #hasMoreLines() hasMoreLines} is true.
     * Initially there is no current command.
     */
    void advance() {
        String line = scanner.nextLine();
        if (!(line.isEmpty() || line.startsWith(COMMENT_START))) {
            if (line.contains(COMMENT_START)) {
                line = line.substring(0, line.indexOf(COMMENT_START));
            }
            currentCommand = line.trim();
        } else {
            advance();
        }
    }

    /**
     * Returns a constant representing the type of the current command.
     * C_ARITHMETIC is returned for all the arithmetic/logical commands.
     *
     * @return  constant representing the type of the current command
     */
    CommandType commandType() {
        if (currentCommand.startsWith("push")) return CommandType.C_PUSH;
        return CommandType.DEFAULT;
    }

    /**
     * Returns the first argument of the current command. In the case of C_ARITHMETIC,
     * the command itself (add, sub, etc.) is returned.
     * Should not be called if the current command is C_RETURN.
     *
     * @return  first argument of the current command
     */
    String arg1() {
        return currentCommand.split("\\s+")[1];
    }

    /**
     * Returns the second argument of the current command. Should be called only
     * if the current command is C_PUSH, C_POP, C_FUNCTION, or C_CALL.
     *
     * @return second argument of the current command
     */
    int arg2() {
        return Integer.parseInt(currentCommand.split("\\s+")[2]);
    }
}

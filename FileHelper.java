import java.io.File;

public class FileHelper {
    
    // Checks if the file is an assembly file (.asm)
    public static boolean isAsm(File file) {
        return file.getName().endsWith(".asm");
    }
    
    // Removes comments from a line of code
    public static String noComments(String line) {
        int commentIndex = line.indexOf("//");
        return (commentIndex != -1) ? line.substring(0, commentIndex).trim() : line.trim();
    }
    
    // Removes all spaces from a line
    public static String noSpaces(String line) {
        return line.replace(" ", "");
    }
    
    // Pads a binary string with leading zeros to ensure it has the correct length
    public static String padLeftZero(String binary, int length) {
        while (binary.length() < length) {
            binary = "0" + binary;
        }
        return binary;
    }
}

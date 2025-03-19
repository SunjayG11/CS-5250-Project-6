import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Assembler {
    
    // Maps for storing predefined symbols and instruction translations
    public static HashMap<String, Integer> symbolTable = new HashMap<>();
    public static HashMap<String, String> compTableA = new HashMap<>();
    public static HashMap<String, String> compTableM = new HashMap<>();
    public static HashMap<String, String> destTable = new HashMap<>();
    public static HashMap<String, String> jumpTable = new HashMap<>();

    // Initialize predefined symbols
    static {
        // Register and memory mappings
        String[] registers = {"R0", "R1", "R2", "R3", "R4", "R5", "R6", "R7", "R8", "R9", "R10", "R11", "R12", "R13", "R14", "R15"};
        for (int i = 0; i < registers.length; i++) {
            symbolTable.put(registers[i], i);
        }
        symbolTable.put("SP", 0);
        symbolTable.put("LCL", 1);
        symbolTable.put("ARG", 2);
        symbolTable.put("THIS", 3);
        symbolTable.put("THAT", 4);
        symbolTable.put("SCREEN", 16384);
        symbolTable.put("KBD", 24576);
        
        // Computation maps for A and M register values
        compTableA.put("0", "101010"); compTableA.put("1", "111111");
        compTableA.put("D", "001100"); compTableA.put("A", "110000");
        compTableA.put("!D", "001101"); compTableA.put("!A", "110001");
        compTableA.put("D+1", "011111"); compTableA.put("A+1", "110111");
        compTableA.put("D-1", "001110"); compTableA.put("A-1", "110010");
        compTableA.put("D+A", "000010"); compTableA.put("D-A", "010011");
        compTableA.put("A-D", "000111"); compTableA.put("D&A", "000000");
        compTableA.put("D|A", "010101");

        compTableM.put("M", "110000"); compTableM.put("!M", "110001");
        compTableM.put("M+1", "110111"); compTableM.put("M-1", "110010");
        compTableM.put("D+M", "000010"); compTableM.put("D-M", "010011");
        compTableM.put("M-D", "000111"); compTableM.put("D&M", "000000");
        compTableM.put("D|M", "010101");
        
        // Destination table
        String[] dests = {"", "M", "D", "MD", "A", "AM", "AD", "AMD"};
        for (int i = 0; i < dests.length; i++) {
            destTable.put(dests[i], String.format("%03d", i));
        }
        
        // Jump table
        String[] jumps = {"", "JGT", "JEQ", "JGE", "JLT", "JNE", "JLE", "JMP"};
        for (int i = 0; i < jumps.length; i++) {
            jumpTable.put(jumps[i], String.format("%03d", i));
        }
    }

    // Converts assembly code to machine code
    public static String asmToHack(String code) {
        Scanner scanner = new Scanner(code);
        StringBuilder instructions = new StringBuilder();
        
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (line.isEmpty() || line.startsWith("(")) continue;
            
            if (line.startsWith("@")) {
                // A-instruction (memory address or value)
                String value = line.substring(1);
                int address = symbolTable.getOrDefault(value, Integer.parseInt(value));
                instructions.append("0").append(FileHelper.padLeftZero(Integer.toBinaryString(address), 15)).append("\n");
            } else {
                // C-instruction (computation)
                String dest = "", comp, jump = "";
                if (line.contains("=")) {
                    String[] parts = line.split("=");
                    dest = parts[0];
                    line = parts[1];
                }
                if (line.contains(";")) {
                    String[] parts = line.split(";");
                    comp = parts[0];
                    jump = parts[1];
                } else {
                    comp = line;
                }
                String aBit = compTableA.containsKey(comp) ? "0" : "1";
                comp = compTableA.getOrDefault(comp, compTableM.get(comp));
                instructions.append("111").append(aBit).append(comp).append(destTable.get(dest)).append(jumpTable.get(jump)).append("\n");
            }
        }
        scanner.close();
        return instructions.toString();
    }

    // Reads and processes a file
    public static void translateFile(String filePath) {
        File file = new File(filePath);
        if (!FileHelper.isAsm(file)) {
            System.out.println("Invalid file format. Must be .asm");
            return;
        }
        
        try (Scanner scanner = new Scanner(file)) {
            StringBuilder code = new StringBuilder();
            while (scanner.hasNextLine()) {
                String line = FileHelper.noSpaces(FileHelper.noComments(scanner.nextLine()));
                if (!line.isEmpty()) {
                    code.append(line).append("\n");
                }
            }
            
            String result = asmToHack(code.toString().trim());
            String outputFileName = file.getParent() + "/" + file.getName().replace(".asm", ".hack");
            try (PrintWriter writer = new PrintWriter(outputFileName)) {
                writer.print(result);
            }
            System.out.println("Translation completed. Output saved to " + outputFileName);
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + filePath);
        }
    }
    
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: Assembler filename.asm");
            return;
        }
        translateFile(args[0]);
    }
}

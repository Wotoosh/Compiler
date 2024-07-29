import backend.BackEnd;
import backend.mipstool.BackOptimizer;
import frontend.Lexer;
import frontend.Parser;
import frontend.errorchecker.ErrorChecker;
import llvm.MidOptimizer;
import llvm.IRBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Compiler {
    public static boolean isOpt=false;
    public static void main(String[] args) {
        boolean hasError = true;
        boolean isParser = false;
        boolean isLlvm = false;
        File f = new File("testfile.txt");
        byte[] bytes = new byte[0];
        try {
            bytes = Files.readAllBytes(Paths.get(f.getAbsolutePath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String source = new String(bytes);
        ErrorChecker errorChecker = new ErrorChecker();
        Lexer lexer = new Lexer(source, errorChecker, hasError);
        lexer.analyze();
        File f1 = new File("llvm_ir.txt");
        if (hasError) {
            f = new File("error.txt");
        } else if (isParser) {
            f = new File("output.txt");
        } else if (isLlvm) {
            f = new File("llvm_ir.txt");
        } else {
            f = new File("mips.txt");
        }
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //创建文件输出流
        FileOutputStream fout = null;
        FileOutputStream fout1 = null;
        try {
            fout = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            fout1 = new FileOutputStream(f1);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        //lexer.print(fout);
        Parser parser = new Parser(lexer.getTokens(), errorChecker, hasError);
        //parser.print();
        if (hasError) {
            if (errorChecker.hasError()) {
                errorChecker.print(fout);
                return;
            } else {
                f = new File("mips.txt");
                try {
                    fout = new FileOutputStream(f);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

        //parser.print(fout);
        IRBuilder irBuilder = new IRBuilder(parser.getCompUnit());
        irBuilder.buildLlvm();
        if (isLlvm) {
            MidOptimizer midOptimizer = new MidOptimizer(irBuilder.getModule());
            midOptimizer.start();
            irBuilder.print(fout1);
        } else {
            MidOptimizer midOptimizer = new MidOptimizer(irBuilder.getModule());
            midOptimizer.start();
            irBuilder.print(fout1);
            BackEnd backEnd = new BackEnd(irBuilder.getModule());
            backEnd.build();
            BackOptimizer backOptimizer = new BackOptimizer(backEnd.getModule(), backEnd.getTopFunctions());
            backOptimizer.run();
            backEnd.print(fout);
        }
    }
}

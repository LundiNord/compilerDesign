package edu.kit.kastel.vads.compiler;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MainTest {

    @Test
    @Order(1)
    void runCurrentTest() {
        String[] args = new String[2];
        args[0] = "tests/currentTest.c";
        args[1] = "tests/out";
        try {
            Main.main(args);
        } catch (IOException e) {
            fail("Compiler failed with: " + e);
        }
    }

    @Test
    @Order(2)
    void runCurrentCompiledProgramm() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", "tests/out");
        processBuilder.redirectErrorStream(true);
        Process process;
        StringBuilder output = new StringBuilder();
        System.out.println("programm output:");
        try {
            process = processBuilder.start();
            BufferedReader buf = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String temp;
            while ((temp = buf.readLine()) != null) {
                output.append(temp).append("\n");
            }
            process.waitFor();
            int exitCode = process.exitValue();
            System.out.println("exitCode: " + exitCode);
            assertEquals(compareWithGcc(), exitCode);
        } catch (InterruptedException | java.io.IOException error) {
            fail("programm failed with: " + error);
        }
        System.out.println(output);
        System.out.println("-----------------");
    }

    private int compareWithGcc() {
        int exitValue = -1;
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", "gcc tests/currentTest.c -o tests/a.out ; tests/a.out");
        processBuilder.redirectErrorStream(true);
        Process process;
        StringBuilder output = new StringBuilder();
        try {
            process = processBuilder.start();
            BufferedReader buf = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String temp;
            while ((temp = buf.readLine()) != null) {
                output.append(temp).append("\n");
            }
            process.waitFor();
            exitValue = process.exitValue();
        } catch (java.io.IOException | InterruptedException error) {
            fail("crow failed with: " + error);
        }
        System.out.println("gcc programm output:");
        System.out.println(output);
        System.out.println(exitValue);
        System.out.println("-----------------");
        return exitValue;
    }

}

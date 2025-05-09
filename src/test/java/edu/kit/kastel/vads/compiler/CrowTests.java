package edu.kit.kastel.vads.compiler;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import static org.junit.jupiter.api.Assertions.fail;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CrowTests {

    @Test
    @Order(1)
    void syncCrowTests() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", "crow/crow-client-linux sync-tests --test-dir crow/tests/");
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
        } catch (java.io.IOException error) {
            fail("crow sync failed with: " + error);
        }
        System.out.println("crow output:");
        System.out.println(output);
        System.out.println("-----------------");
    }

    @Test
    @Order(2)
    void runCrowTests() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", "./build.sh ; crow/crow-client-linux run-tests --test-dir crow/tests/ --compiler-run ./run.sh");
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
        } catch (java.io.IOException error) {
            fail("crow failed with: " + error);
        }
        System.out.println("crow output:");
        System.out.println(output);
        System.out.println("-----------------");
    }
}

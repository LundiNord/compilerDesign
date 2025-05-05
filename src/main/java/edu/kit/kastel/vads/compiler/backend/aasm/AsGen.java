package edu.kit.kastel.vads.compiler.backend.aasm;

public class AsGen {

    private String template = """
            .global main
            .global _main
            .text
            main:
            call _main
            
            ; move the return value into the first argument for the syscall
            movq %rax, %rdi
            ; move the exit syscall number into rax
            movq $0x3C, %rax
            syscall
            
            _main:
            ; your generated code here""";





}

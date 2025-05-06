.global main
.global _main
.text
main:
call _main

movq %rax, %rdi
movq $0x3C, %rax
syscall

_main:

movl $0x3, %r8d
movl $0x4, %r9d
movl %r8d, %r10d
addl %r9d, %r10d
movl $0x1, %r11d
movl %r10d, %r12d
addl %r11d, %r12d
movl %r12d, %eax

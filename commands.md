# Commands

Run Compiled Program:
 ./out || echo $?

Run Compiler:
 ./run.sh tests/test1.l1 tests/out

Compile C to Assembly:
 gcc -S -O0 test1.c -o gccOutput

Crow:
 cd crow/
 ./crow-client-linux sync-tests --test-dir tests/
 ./crow-client-linux run-tests --test-dir tests/ --compiler-run ../run.sh

#!/bin/bash
curl -X POST http://localhost:8080/compiler/compile/full -H "Content-Type: text/plain" -d "ranger MyProgram { int a = 10; }"

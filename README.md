# Mini-Compiler-Tool-Kit
This project implements a mini compiler system that processes a custom programming language and converts it into executable form.

The system follows standard compiler phases:

Lexical Analysis → Token generation
Syntax Analysis → AST construction
Semantic Analysis → Validation
Intermediate Code Generation → Three Address Code (TAC)
Target Code Generation → Assembly-like instructions
Interpreter → Execution engine

The system is built using Spring Boot REST APIs, allowing users to test each phase independently.

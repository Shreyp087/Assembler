# C6461Assembler

A simple assembler for a hypothetical assembly language. This program reads an assembly source file and converts it into machine code, producing both a list file (`.lst`) and an object file (`.obj`).

## Features

- Two-pass assembler: First pass to calculate memory addresses, second pass to encode instructions.
- Supports a variety of instructions (Load/Store, Transfer, Arithmetic, Control, etc.)
- Generates output in two formats:
  - `.lst` (list file): Contains line numbers, addresses, and assembly code.
  - `.obj` (object file): Contains the machine code in octal format.

## Requirements

- Java 8 or higher
- Command-line terminal (for running the assembler)

## Installation

1. Clone this repository:
   ```bash
   git clone https://github.com/yourusername/C6461Assembler.git
   cd C6461Assembler
Compile the Java program:

bash
Copy
Edit
javac C6461Assembler.java
Run the assembler:

bash
Copy
Edit
java C6461Assembler <input.asm> <output.lst>
Example
Input file (input.asm):

asm
Copy
Edit
LOC 1000
START:
LDA 1,2,300
JNE 5
HLT
Data 500
Command to run:

bash
Copy
Edit
java C6461Assembler input.asm output.lst
Output Files
output.lst: The list file containing the assembly code along with addresses.
output.obj: The object file containing the machine code in octal format.

# C6461 Assembler

The **C6461 Assembler** is a tool designed to convert assembly language code for a hypothetical C6461 microprocessor into machine code. It reads assembly instructions from a source file, processes them in two passes, and generates two output files:
1. **List file (.lst)** - a human-readable file showing the assembly code with corresponding memory addresses.
2. **Object file (.obj)** - a binary file containing the actual machine code in octal format.

## Table of Contents
- [Overview](#overview)
- [Features](#features)
- [Installation](#installation)
- [Usage](#usage)
- [Input File Format](#input-file-format)
- [Output Files](#output-files)
- [Design and Functionality](#design-and-functionality)
- [License](#license)
- [Contributing](#contributing)
- [Acknowledgments](#acknowledgments)

## Overview

The C6461 Assembler is an assembler tool that converts assembly language programs into machine code for the C6461 microprocessor. The tool generates two key output files:
- **List file (.lst)**: Contains a human-readable view of the program with addresses.
- **Object file (.obj)**: Contains the corresponding machine code in octal format.

### Main Steps:
1. **First Pass**: Resolves labels and assigns memory addresses.
2. **Second Pass**: Converts assembly instructions into machine code using the predefined instruction set.

## Features
- **Two-pass assembly**: First pass resolves labels, and the second pass converts instructions into machine code.
- **Label resolution**: Automatically resolves labels to memory addresses during assembly.
- **Instruction encoding**: Supports encoding for operations such as `LDA`, `STR`, `HLT`, and `JNE`.
- **Symbol table**: Efficient symbol table used for label resolution.
- **Human-readable output**: Generates a detailed list file for easy debugging.
- **Efficient error handling**: Provides detailed error messages for common mistakes like undefined labels or invalid instructions.

## Installation

### Prerequisites
To run the assembler, ensure that you have the following software installed:
- **Java 11 or higher**: The program is written in Java, so you'll need Java Runtime Environment (JRE) 11 or higher to run the program.
  
### Step-by-Step Installation:
1. **Download the source code**:
   - Clone the repository using Git:
     ```
     git clone https://github.com/your-repo/C6461Assembler.git
     ```
   - Alternatively, you can download the source code as a zip file and extract it.

2. **Compile the program**:
   - Navigate to the project directory where the source code is located.
   - Open a terminal or command prompt and compile the program using the following command:
     ```
     javac Assembler.java
     ```
   - This will create a compiled `Assembler.class` file.

3. **Run the program**:
   - After compiling the code, you can run the assembler using the following command:
     ```
     java Assembler input.asm
     ```
   - Replace `input.asm` with the name of your assembly source file.

## Usage

### Running the Assembler
To run the assembler on your system, follow these steps:

1. Create an assembly language file (`input.asm`) following the required format (described below).
2. Run the assembler using the command:

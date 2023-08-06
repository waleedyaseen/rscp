# RSCP
RSCP stands for **R**une**S**cript **C**onfiguration **P**acker.

It is a tiny compiler that compiles RuneScript Configuration source files to a bytecode-like binary format.

# Getting Started
### Requirements
The compiler requires Java 11  to run, if you don't have that already, you can download and install it from [Adoptium.net](https://adoptium.net/temurin/releases?version=11).

You can download the latest compiler JAR file from GitHub [releases](https://github.com/waliedyassen/rscp/releases/latest)
 page.

### Usage
To compile all the source files within a directory you can use the following example: `java.exe -jar packer.jar -i src -o bin --symbols sym`

Upon executing, the compiler will collect all supported configuration files from `src` folder and place the compiled binary files into `bin`. Upon every sucessful compilation, the compiler will automatically update the symbols in the `sym` directory.

### Options
```
Options:
  --symbols PATH                   Path to the directory with all the *.sym
                                   files for symbols
  -g, --graphics PATH              Path to the directory with all the PNG
                                   files for graphics
  -i, --input PATH                 Path to a directory of source files to
                                   compile
  --input-file PATH                Path to a single source file to compile
  -o, --output PATH                The path to the directory which the
                                   generated binaries will be placed into
  -e, --extract [None|Errors|SemInfo]
  --sides [Server|Client]
  -s, --silent                     Run in silent mode, prevent any logging
                                   output
  -h, --help                       Show this message and exit
  ```
# Supported Types
The following table contains all the current possible configurations that come with the compiler:

|      Type      |  Extension  | Support |
|:--------------:|:-----------:| :--: |
|   Constants    | `constants` | Complete |
| Database Table |  `dbtable`  | Complete |
|  Database Row  |   `dbrow`   | Complete |
| Database Index |    None     | Complete |
|      Hunt      |   `hunt`    | Complete |
|      Enum      |   `enum`    | Complete |
| Floor Overlay  |    `flo`    | Complete |
| Floor Underlay |    `flu`    | Complete |
|    Headbar     |  `headbar`  | Not Started |
|    Hitmark     |  `hitmark`  | Not Started |
|  IdentityKit   |    `idk`    | Not Started |
|  Interface v3  |    `if3`    | Complete |
|   Inventory    |    `inv`    | Complete |
|    Location    |    `loc`    | Not Started |
|  Map Element   |    `mel`    | Not Started |
|      NPC       |    `npc`    | Not Started |
|     Object     |    `obj`    | Not Started |
|   Parameter    |   `param`   | Complete |
|    Sequence    |    `seq`    | Not Started |
| Spot Animation | `spotanim`  | Not Started |
|   Structure    |  `struct`   | Complete |
|    Var Bit     |  `varbit`   | Complete |
|   Var Client   |   `varc`    | Complete |
|   Var Player   |   `varp`    | Complete |
|    Graphic     |  `graphic`  | Complete |
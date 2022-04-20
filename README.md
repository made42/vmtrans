# vmtrans

VM-to-Hack translator for the Hack computer built in the Nand to Tetris courses.

**Usage:** The VM translator accepts a single command-line argument, as follows,

``prompt>VMTranslator`` *source*

where *source* is a file name of the form *ProgName*``.vm``. The file name may contain a file
path. If no path is specified, the VM translator operates on the current folder. The first
character in the file name must be an uppercase letter, and the ``vm`` extension is mandatory.
The file contains a sequence of one or more VM commands. In response, the translator
creates an output file, named *ProgName*``.asm``, containing the assembly instructions that
realize the VM commands. The output file *ProgName*``.asm``is stored in the same folder as
that of the input. If the file *ProgName*``.asm`` already exists, it will be overwritten.

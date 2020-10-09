# Judge

**What is it?**  
  
This program aims to automatize the usual workflow of training to a code marathon: Compile the program, run it, input something, recieve an output 
and compare it to an ideal one (This workflow here is called a __test__). 
Judge will do this for you, repeating this process for whole a batch of inputs and outputs, while keeping track of
which ones result in __runtime-errors__ or __unexpected behavior__.  
After running all tests, Judge will print the current code **precision** (How many tests haven't failed, i.e., how many program outputs were the same as the expected ones), the **number of runtime errors and which tests caused them** (If there are any) and the **number of unexpected behaviors and which tests caused them** (If there are any).  
  
**Requeriments**  
- A linux OS. (For the time being);  
  
- In the same folder as the program you aim Judge to automatize the process, there needs to be a folder named **input** and another named **output**;  
  E.g.: `~/PATH/codeFolder/input` and `~/PATH/codeFolder/input` are valid pathes in your system;  
    
- All input or output files must be named with number at the end to identify which test is it.  The number must be between 1 and the total 
number of input/output archives on that folder;  
  - Input files (4 in total): someNameIn1, someNameIn2, someNameIn3, someNameIn4  
  - Output files (4 in total): someNameOut1, someNameOut2, someNameOut3, someNameOut4  
  
**How to run it**  
  
Judge utilizes of java CLI arguments to complete the task. In order to run it, one must need to input 4 arguments:  
  - 1st: A string of a valid path where the code, input folder and output folders are located, without the last backlash:  
    E.g.: `~/home/user/codeFolder`  
  - 2nd: A string containing the whole name of the program you expect Judge to run:  
    E.g.: `marathonCode.java`    
  - 3rd: A string containing the input files name pattern, i.e. the input files name without a number:  
    E.g.: `someNameIn`  
  - 4th: A string containing the output files name pattern, i.e. the output files name without a number:  
    E.g.: `someNameOut`  
      
Once compiled, running Judge looks something like this:   
`~/PATH/OF/JUDGE$ java Judge ~/home/user/codeFolder marathonCode.java someNameIn someNameOut `  

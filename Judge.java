import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
 
class Judge {
	public static void main (String[] args) {
		// Argumentos esperados em ordem de entrada: 
		// Caminho da pasta onde está localizado o código (sem a `/` final); 
		// Nome do código a se testar (Tem que se encontrar na pasta e terminar com `.java`);
		// Name pattern de arquivo de input;
		// Name pattern de arquivo de output;
		// (Os arquivos de input e output devem ser numerados, os dígitos sendo os últimos caracteres do nome);
		// (O name pattern desses arquivos é o título de um dos arquivos sem os dígitos e sem a extensão do tipo de arquivo);
		 	
		// Espera-se que dentro da pasta raiz existam as pastas `input` e `output`.
		// E que o código seja escrito em java 
		   
		if (args.length > 4) { System.out.println("Too many arguments"); return; }
		if (args.length < 4) { System.out.println("Insufficient arguments"); return; }
			  
		String rootFolderPath = args[0];
		String codeName = args[1];
		String inNamePat = args[2];
		String outNamePat = args[3];
		  
		try {
			if(!isValidPath(rootFolderPath)) { 
				System.out.println("Invalid folder path"); 
				return; 
			}
			if(!archiveExists(rootFolderPath, codeName)) { 
				System.out.printf("Couldn't find %s.java in %s\n", codeName, rootFolderPath); 
				return; 
			}
			if(errorToCompile(rootFolderPath, codeName)) { 
				System.out.println("Compilation-error"); 
				return;
			}
			if(!namePatIsCorrect(rootFolderPath, inNamePat, false)) { 
				System.out.printf("No archive with name pattern %s found in %s/input", inNamePat, rootFolderPath); 
				return; 
			}
			if(!namePatIsCorrect(rootFolderPath, outNamePat, true)) {
				System.out.printf("No archive with name pattern %s found in %s/input", outNamePat, rootFolderPath); 
				return; 
			} 
			 
			//TO-DO: END PROGRAM IF NUMBER OF OUTPUTS IS LESS THAN NUMBER OF INPUTS
			System.out.println("Judging Code\n");
			Stats stat = (Stats) judgeCode(codeName, rootFolderPath, inNamePat, outNamePat);
			System.out.printf("Precision obtained: %.3f\n", stat.precision * 100);
			if (stat.precision != 1) {
				stat.printErrors();
				stat.printTestsFailed();
			}
		} 
		catch (IOException e) {
			System.out.println(e.getMessage());
		} 
		catch (NullPointerException np) {
			System.out.println(np.getMessage());
		}
	}
	 
	public static boolean namePatIsCorrect(String folderPath, String namePat, boolean isOutput) throws IOException{
		String listInputs = " ls -1 | grep \"" + namePat + "\" ";
		String listOutputs = " ls -1 | grep \"" + namePat + "\" ";
		String goTo = "cd ";
		String command;
		String[] stdOut;
		 
		try { 
			if(isOutput) {
				folderPath += "/output";
				command = goTo + folderPath + " && " + listOutputs ;
				stdOut = OS.executeCommand(command) ;
			}
			else {
				folderPath += "/input";
				command = goTo + folderPath + " && " + listInputs ;
				stdOut = OS.executeCommand(command) ;
			}
			return (stdOut.length == 0);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		return true;
	}
	 
	public static boolean isValidPath (String folderPath) throws IOException{
		String goTo = "cd ";
		String[] err = OS.executeCommand(goTo + folderPath) ;
		return (err.length == 0);
	}
	 
	public static boolean archiveExists(String folderPath, String codeName) throws IOException {
		String goTo = "cd ";
		String listAndMatchName = "ls -1 | grep \"" + codeName + "\"";
		String findArchive =  goTo + folderPath + " && " + listAndMatchName;
		String[] err = OS.executeCommand(findArchive);
		return (err.length == 0);
	}
	 
	public static boolean errorToCompile (String folderPath, String codeName) throws IOException {
		String compileCode = " javac " + folderPath + "/" + codeName + ".java ";
		String[] err = OS.executeCommand(compileCode);
		return (err.length == 0);
	}
	 
	public static Stats judgeCode (String codeName, String folderPath, String inNamePat, String outNamePat) throws IOException {
		// All basic commands used in the function
		String inPath = folderPath + "/input";
		String outPath = folderPath + "/output";
		String goToRoot = " cd " + folderPath + " ";
		String goToInput = " cd " + inPath + " ";
		String goToOutput = " cd " + outPath + " ";
		String listInputsTo = " ls -1 | grep \"" + inNamePat + "\" |";
		String listOutputs = " ls -1 | grep \"" + outNamePat + "\" ";
		String countList = " wc -l ";
		String runCode = " java " + codeName + " ";
		String transferStdOutTo = "1>";
		String strErrTo = "2>";
		String diff = " diff ";
		String obtainedOutput = outNamePat +  "-T";
		 
		String command;
		String echoInputTo;
		String expectedOutput;
		 
		ArrayList<String> errMsg = new ArrayList<String>();		// All error messages
		ArrayList<String> outDiff = new ArrayList<String>();	// All test case diffs
		 
		int numIn = 0;		//Number of inputs to be tested
		 
		command = goToInput + " && " + listInputsTo + countList;
		String[] commandOut = OS.executeCommand(command);		// All stdOut or stdErr messages outputted
		 
		if (commandOut != null && commandOut.length == 1) {
			try {
				numIn = Integer.parseInt(commandOut[0]) ;
			} catch (Exception e) {
				System.out.println(e.getMessage());
				numIn = 0;
			}
		} 
		else return null;
		 	
		if (numIn == 0) return new Stats(numIn, new String[0], new String[0]);	
		  
		//TO-DO: APPLY REGULAR EXPRESSIONS TO OBTAIN A INPUT OR OUTPUT ARCHIVE NAME
		for (int i = 1; i <= numIn; i++) {
			String testNumber = String.valueOf(i);
			echoInputTo = " cat " + inPath + "/" + inNamePat + testNumber + " |";
			expectedOutput = " " + outPath + "/" + outNamePat + testNumber + " ";
			obtainedOutput = " " + outPath + "/" + outNamePat + "T" + testNumber + " " ;
			 
			command = goToRoot + " && " + echoInputTo + runCode + transferStdOutTo + obtainedOutput ;
			 
			commandOut = OS.executeCommand(command);
			 
			// if there is any stdErr obtained from running the code
			if ( commandOut.length != 0) errMsg.add(formatMsg(i, command, commandOut, true));
			else {
				command = diff + expectedOutput + obtainedOutput;
				commandOut = OS.executeCommand(command);	 
				// if there is any difference between outputs
				if (commandOut.length != 0) outDiff.add(formatMsg(i, "", commandOut, false));
			}
		}
		 		  
		Stats stat = new Stats(numIn, outDiff.toArray(new String[0]), errMsg.toArray(new String[0]));
		return stat;	
	}
	 
	// TO-DO: If message isn't error, then format it so it will fit the pattern:
	// "Expected output: 
	//  Obtained output: "
	public static String formatMsg (int testNumber, String command, String[] msg, boolean isErr){
		String out = new String ("Test case number: ");
		out = out + String.valueOf(testNumber) + "\n";
		if (isErr) out += "Command line which caused error: " + command + "\n";
		for (int i = 0; i < msg.length; i++) {
			out = out + "\n" + msg[i];
		}
		return out;
	}
}	

class Stats {
	int totalTests;
	String[] testsFailed;
	String[] errors;
	double precision;
	 	 
	public Stats ( int totalTests, String[] testsFailed, String[] errors) {
		this.totalTests = totalTests;
		this.testsFailed = testsFailed;
		this.errors = errors;
		this.precision = (totalTests - testsFailed.length - errors.length ) / (double) totalTests;
	}
		 
	public void printTestsFailed() {
		if (testsFailed.length == 0) System.out.println ("No tests were failed! Congrats!\n");
		else { 
			System.out.printf("A total of %d tests were failed. ", testsFailed.length);
			System.out.printf("There are %d tests.", totalTests);
			for (int i = 0; i < testsFailed.length; i++) {
				System.out.println(testsFailed[i] + "\n");
			}
		}
	}
	 
	public void printErrors() {
		if (errors.length == 0) {
			System.out.println ("No run-time errors were found! Congrats!\n");
		} else {
			System.out.printf("A total of %d errors happened. ", errors.length);
			System.out.printf("There are %d tests.\n", totalTests);
			for (int i = 0; i < errors.length; i++) {
				System.out.println(errors[i] + "\n");
			}
		}
	}
} 

class OS{
	public static String[] executeCommand(final String command) throws IOException {
		final ArrayList<String> commands = new ArrayList<String>();
		commands.add("/bin/bash");		// Abrir o bash
		commands.add("-c");			// Run command below
		commands.add(command);

		 
		ArrayList<String> out = new ArrayList<String>();
		 
		final ProcessBuilder p = new ProcessBuilder(commands); 
		final Process process = p.start();
		 
		try ( final InputStream inS = process.getInputStream();
			final InputStream errS = process.getErrorStream();
			final InputStreamReader inSR = new InputStreamReader(inS);
			final InputStreamReader errSR = new InputStreamReader(errS);
			BufferedReader outReader = new BufferedReader(inSR);
			BufferedReader errReader = new BufferedReader(errSR))
		{
			String line;
			 
			// Store all stdOut and stdErr messages, line by line
			while((line = outReader.readLine()) != null) {
				out.add(line);
			}
			while((line = errReader.readLine()) != null) {
				out.add(line);
			}
		} catch (IOException ioe) {
			System.out.println("Error while running shell command" + ioe.getMessage());
			throw ioe;
		}
		 
		String[] stdOut= out.toArray(new String[0]);
		 
		return stdOut; 
	}
}

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.Runtime;
import java.util.Random;
import java.util.Scanner;

public class CPU {

	private int SP, PC, IR, AC, X, Y, instructionCount = 0;
	private Process process;
	private int interruptInterval;
	private InputStream stdout;
	private OutputStream stdin;
	private Scanner reader;
	private BufferedWriter pw;
	private boolean systemMode = false;

	public static void main(String[] args) {
		new CPU(args[0],args[1]);
		}

	public CPU(String fileName, String interruptInterval) {
		this.interruptInterval = Integer.parseInt(interruptInterval);
		PC = 0;
		SP = 1000;
		Runtime runtime = Runtime.getRuntime();
		try {
			process = runtime.exec(String.format("java Memory %s%n", fileName));
			stdin = process.getOutputStream();
			stdout = process.getInputStream();
			reader = new Scanner(stdout);
			pw = new BufferedWriter(new OutputStreamWriter(stdin));
			process();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public int readPipe() throws IOException {
		return Integer.parseInt(reader.next());
	}

	public void process() throws InterruptedException, IOException {
		while (true) {
			pw.write(String.format("0 %d%n", PC++));
			pw.flush();

			IR = readPipe();
			
			if (!systemMode) {
				instructionCount++;
			}
		
			switch (IR) {
			case 1: // load value
				
				pw.write(String.format("0 %d%n", PC++));
				pw.flush();
				AC = readPipe();
				break;

			case 2: // load address
				pw.write(String.format("0 %d%n", PC++));
				pw.flush();
				int address = readPipe();
				
				if(!systemMode && address >999){
					System.out.print("Error: Cannot access System Memory in user mode");
					return;
				}else{
					pw.write(String.format("0 %d%n", address));
					pw.flush();
					AC = readPipe();
				}
				break;

			case 3: // load ind address
				pw.write(String.format("0 %d%n", PC++));
				pw.flush();
				address = readPipe();
				pw.write(String.format("0 %d", address));
				pw.flush();
				address = readPipe();
				pw.write(String.format("0 %d", address));
				pw.flush();
				AC = readPipe();
				break;

			case 4: // load (address+X)
				pw.write(String.format("0 %d%n", PC++));
				pw.flush();
				address = readPipe();
				pw.write(String.format("0 %d%n", address + X));
				pw.flush();
				AC = readPipe();
				break;

			case 5: // load (address+Y)
				pw.write(String.format("0 %d%n", PC++));
				pw.flush();
				address = readPipe();
				pw.write(String.format("0 %d%n", address + Y));
				pw.flush();
				AC = readPipe();
				break;

			case 6: // load (sp+x)
				pw.write(String.format("0 %d%n", SP+X));
				pw.flush();
				AC = readPipe();
				break;

			case 7: // store address
				pw.write(String.format("0 %d%n", PC++));
				pw.flush();
				address = readPipe();
				pw.write(String.format("1 %d %d%n",address , AC));
				pw.flush();
				break;

			case 8: // Get random int into AC
				Random rand = new Random();
				AC = rand.nextInt(100) + 1;
				break;

			case 9: // Put port
				pw.write(String.format("0 %d%n", PC++));
				pw.flush();
				int port = readPipe();
				if (port == 1) {
					System.out.print(AC);
				} else if (port == 2) {
					System.out.print((char) AC);
				}
				break;

			case 10: // Add X
				AC += X;
				break;

			case 11: // Add Y
				AC += Y;
				break;

			case 12: // Sub X
				AC -= X;
				break;

			case 13: // Sub Y
				AC -= Y;
				break;

			case 14: // Copy from AC into X
				X = AC;
				break;

			case 15: // Copy from X into AC
				AC = X;
				break;

			case 16: // Copy from AC into Y
				Y = AC;
				break;

			case 17: // Copy from Y into AC
				AC = Y;
				break;

			case 18: // Copy from AC into SP
				SP = AC;
				break;

			case 19: // Copy from SP into AC
				AC = SP;
				break;

			case 20: // Jump to address
				pw.write(String.format("0 %d%n", PC++));
				pw.flush();
				PC = readPipe();
				break;

			case 21: // Jump to the address only if the value in the AC is zero
				pw.write(String.format("0 %d%n", PC++));
				pw.flush();
				if (AC == 0) {
					PC = readPipe();
				} else
					readPipe();
				break;

			case 22: // Jump to the address only if the value in the AC is not zero
				pw.write(String.format("0 %d%n", PC++));
				pw.flush();
				if (AC != 0) {
					PC = readPipe();
				} else
					readPipe();
				break;

			case 23: // Push return address onto stack, jump to the address
				pw.write(String.format("0 %d%n", PC++));
				pw.flush();
				int jumpAddress = readPipe();
				pw.write(String.format("1 %d %d%n", --SP, PC));
				pw.flush();
				PC = jumpAddress;
				break;

			case 24: // Pop return address from the stack, jump to the address
				pw.write(String.format("0 %d%n", SP++));
				pw.flush();
				PC = readPipe();
				break;

			case 25: // Increment the value in X
				X++;
				break;

			case 26: // Decrement the value in X
				X--;
				break;

			case 27: // Push AC onto stack
				pw.write(String.format("1 %d %d%n", --SP, AC));
				pw.flush();
				break;

			case 28: // Pop from stack into AC
				pw.write(String.format("0 %d%n", SP++));
				pw.flush();
				AC = readPipe();
				break;

			case 29: // Set system mode, switch stack, push SP and PC, set new SP and PC
				systemMode = true;
				handleInterrupt(1500);
				break;

			case 30: // Restore registers, set user mode
				systemMode = false;
				pw.write(String.format("0 %d%n", SP++));
				pw.flush();
				Y = readPipe();
				pw.write(String.format("0 %d%n", SP++));
				pw.flush();
				X = readPipe();
				pw.write(String.format("0 %d%n", SP++));
				pw.flush();
				AC = readPipe();
				pw.write(String.format("0 %d%n", SP++));
				pw.flush();
				PC = readPipe();
				pw.write(String.format("0 %d%n", SP++));
				pw.flush();
				SP = readPipe();
				break;

			case 50: // End execution
				process.destroy();
				return;
			}
			if (instructionCount % interruptInterval == 0) {
				handleInterrupt(1000);
			}
		}
	}

	public void handleInterrupt(int interruptHandlerAddress) throws IOException, InterruptedException {
		int tempPointer = SP;
		SP = 2000;
		pw.write(String.format("1 %d %d%n", --SP, tempPointer));
		pw.flush();
		pw.write(String.format("1 %d %d%n", --SP, PC));
		pw.flush();
		pw.write(String.format("1 %d %d%n", --SP, AC));
		pw.flush();
		pw.write(String.format("1 %d %d%n", --SP, X));
		pw.flush();
		pw.write(String.format("1 %d %d%n", --SP, Y));
		pw.flush();
		PC = interruptHandlerAddress;
	}
}

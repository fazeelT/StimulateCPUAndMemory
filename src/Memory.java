import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Memory {
	private final String fileNameOrPath;
	private int[] mainMemory;
	Scanner scanner = new Scanner(System.in);

	public static void main(String[] args) {
		new Memory(args[0], 2000);
	}

	public Memory(String fileNameOrPath, int size) {
		this.fileNameOrPath = fileNameOrPath;
		mainMemory = new int[size];
		initialize();
		process();
	}

	public void write(int address, int value) {
		mainMemory[address] = value;
	}

	public void read(int address) {
		System.out.println(mainMemory[address]);
	}

	public void process() {
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(System.in);
		String input;
		while (true) {
			if ((input = scanner.nextLine()) != null) {
				String[] inputs = input.split(" ");
				if (Integer.parseInt(inputs[0]) == 0) { // read
					read(Integer.parseInt(inputs[1]));
				} else if (Integer.parseInt(inputs[0]) == 1) { // write
					write(Integer.parseInt(inputs[1]), Integer.parseInt(inputs[2]));
				}
			}
		}
	}

	public void initialize() {
		try {
			@SuppressWarnings("resource")
			BufferedReader fileReader = new BufferedReader(new FileReader(fileNameOrPath));

			String line = fileReader.readLine();
			int address = 0;
			String decimalPattern = "(\\.\\d{1,})?";
			while (line != null) {
				if (!line.isEmpty()) {
					String[] values = line.split(" ");
					if (!values[0].isEmpty() && !values[0].equals('\\') && !values[0].equals('\t')) {
						if (Pattern.matches(decimalPattern, values[0])) {
							address = Integer.parseInt(values[0].split("\\.")[1]);
						} else {
							write(address++, Integer.parseInt(values[0]));
						}
					}
				}
				line = fileReader.readLine();
			}
		} catch (IOException e) {
			System.out.println(String.format("Error: Cannot open file %s", "sample1.txt"));
			e.printStackTrace();
		}
	}
}

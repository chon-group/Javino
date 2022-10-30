package br.pro.turing.javino;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class Javino {
	private final String version = staticversion;
	private static final String staticversion = "stable 1.3.10";
	private String pythonPlataform;
	private String finalymsg = null;

	public Javino() {
		load();
	}

	public Javino(String pathPython) {
		load();
		if (whoSO() == 1) {
			this.pythonPlataform = pathPython + "\\python.exe";
		} else {
			this.pythonPlataform = pathPython + "/python3";
		}
	}

	private void load() {
		unlock();
		System.out.println("[JAVINO] Using version " + this.version + " ChonOS");
		this.setpath(whoSO());
		this.createPythonFile();
	}

	private void setpath(int sp_OS) {
		if (sp_OS == 1) {
			this.pythonPlataform = "C:\\Python39\\python.exe";
		} else {
			this.pythonPlataform = "/usr/bin/python3";
		}
	}

	private int whoSO() {
		// Linux = 0
		// Windows = 1
		String os = System.getProperty("os.name");
		if (os.substring(0, 1).equals("W")) {
			return 1;
		} else {
			return 0;
		}
	}

	private void unlock() {
		File pasta = new File(".");
		File[] arquivos = pasta.listFiles();

		for (File arquivo : arquivos) {
			if (arquivo.getName().endsWith("lock")
					|| arquivo.getName().endsWith("py")) {
				arquivo.delete();
			}
		}
	}

	private String lockFileName(String PORT) {
		PORT = PORT + ".lock";
		PORT = PORT.replace("/", "_");
		PORT = PORT.replace("1", "i");
		PORT = PORT.replace("2", "ii");
		PORT = PORT.replace("3", "iii");
		PORT = PORT.replace("4", "iv");
		PORT = PORT.replace("5", "v");
		PORT = PORT.replace("6", "vi");
		PORT = PORT.replace("7", "vii");
		PORT = PORT.replace("8", "viii");
		PORT = PORT.replace("9", "ix");
		PORT = PORT.replace("0", "x");
		return PORT;
	}

	private boolean portLocked(String lc_PORT) {
		boolean busy;
		File file = new File(lockFileName(lc_PORT));
		if (file.exists()) {
			busy = true;
			System.out.println("[JAVINO] The port " + lc_PORT + " is busy");
		} else {
			busy = false;
		}
		return busy;
	}

	private void lockPort(boolean lock, String PORT) {
		try {
			File file = new File(lockFileName(PORT));
			if (lock) {
				file.createNewFile();
			} else {
				file.delete();
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

	}

	public boolean sendCommand(String PORT, String MSG) {
		boolean result;
		if (portLocked(PORT)) {
			result = false;
		} else {
			lockPort(true, PORT);
			String operation = "command";
			String[] command = new String[5];
			command[0] = this.pythonPlataform;
			command[1] = "javython.py";
			command[2] = operation;
			command[3] = PORT;
			command[4] = preparetosend(MSG);
			ProcessBuilder pBuilder = new ProcessBuilder(command);
			pBuilder.redirectErrorStream(true);
			try {
				Process p = pBuilder.start();
				p.waitFor();
				BufferedReader saida = new BufferedReader(
						new InputStreamReader(p.getInputStream()));
				if (p.exitValue() == 0) {
					result = true;
					lockPort(false, PORT);
				} else {
					String line = null;
					String out = "";
					while ((line = saida.readLine()) != null) {
						out = out + line;
					}
					System.out.println("[JAVINO] Fatal error (send)! [" + out + "]");
					result = false;
					lockPort(false, PORT);
				}
			} catch (IOException | InterruptedException e) {
				System.out.println("[JAVINO] Error on commnad execution");
				e.printStackTrace();
				result = false;
				lockPort(false, PORT);
			}
		}
		return result;

	}
	
	public boolean listenArduino(String PORT){
			boolean result;
		if (portLocked(PORT)) {
			result = false;
		} else {
			lockPort(true, PORT);
			String operation = "listen";
			String[] command = new String[5];
			command[0] = this.pythonPlataform;
			command[1] = "javython.py";
			command[2] = operation;
			command[3] = PORT;
			command[4] = "listen";
			ProcessBuilder pBuilder = new ProcessBuilder(command);
			pBuilder.redirectErrorStream(true);
			try {
				Process p = pBuilder.start();
				p.waitFor();
				BufferedReader read_to_array = new BufferedReader(
						new InputStreamReader(p.getInputStream()));
				if (p.exitValue() == 0) {
					result = setArryMsg(read_to_array);
					lockPort(false, PORT);
				} else {
					String line = null;
					String out = "";
					while ((line = read_to_array.readLine()) != null) {
						out = out + line;
					}
					System.out.println("[JAVINO] Fatal error (listen)! [" + out + "]");
					result = false;
					lockPort(false, PORT);
				}

			} catch (IOException | InterruptedException e) {
				System.out.println("[JAVINO] Error on listen");
				e.printStackTrace();
				result = false;
				lockPort(false, PORT);
			}
		}
		return result;
		
	}
	
	public boolean requestData(String PORT, String MSG) {
		boolean result;
		if (portLocked(PORT)) {
			result = false;
		}else {
			lockPort(true, PORT);
			String PORTshortNAME=PORT.substring(PORT.lastIndexOf("/")+1);
			String operation = "request";
			String[] command = new String[5];
			command[0] = this.pythonPlataform;
			command[1] = "javython.py";
			command[2] = operation;
			command[3] = PORT;
			command[4] = preparetosend(MSG);
			ProcessBuilder pBuilder = new ProcessBuilder(command);
			pBuilder.redirectErrorStream(true);
			try {
				Process p = pBuilder.start();
				p.waitFor();
				BufferedReader read_to_array = new BufferedReader(
						new InputStreamReader(p.getInputStream()));
				if (p.exitValue() == 0) {
					result = setArryMsg(read_to_array);
					if(result){
						addfinalmsg("port("+PORTshortNAME+",on);");
					}
					lockPort(false, PORT);
				} else {
					String line = null;
					String out = "";
					while ((line = read_to_array.readLine()) != null) {
						out = out + line;
					}
					System.out.println("[JAVINO] Fatal error (request)! [" + out + "]");
					setfinalmsg("port("+PORTshortNAME+",off);");
					result = true;
					lockPort(false, PORT);
				}

			} catch (IOException | InterruptedException e) {
				System.out.println("[JAVINO] Error on request");
				e.printStackTrace();
				result = false;
				lockPort(false, PORT);
			}
		}
		return result;

	}

	public String getData() {
		String out = this.finalymsg;
		this.finalymsg = null;
		return out;
	}
		
	private boolean setArryMsg(BufferedReader reader) {
		String line = null;
		String out = new String();
		try {
			while ((line = reader.readLine()) != null) {
				out = out + line;
			}
		} catch (IOException e) {
			System.out.println("[JAVINO] Error at message processing");
			return false;
		}
		return preamble(out.toCharArray());
	}

	private String char2string(char in[], int sizein) {
		int newsize = sizein - 6;
		char[] out = new char[newsize];
		int cont = 0;
		for (int i = 6; i < sizein; i++) {
			out[cont] = in[i];
			cont++;
		}
		return String.valueOf(out);
	}

	private void setfinalmsg(String s_msg) {
		this.finalymsg = s_msg;
	}

	private void addfinalmsg(String a_msg) {
		this.finalymsg = this.finalymsg+a_msg;
	}

	private boolean preamble(char[] pre_arraymsg) {
		try {
			if (pre_arraymsg.length == 0) {
				System.out.println("[JAVINO] Invalid message!");
				return false;
			}
			char p1 = pre_arraymsg[0];
			char p2 = pre_arraymsg[1];
			char p3 = pre_arraymsg[2];
			char p4 = pre_arraymsg[3];
			if ((p1 == 'f')
					&& (p2 == 'f')
					&& (p3 == 'f')
					&& (p4 == 'e')
					&& (this.monitormsg(forInt(pre_arraymsg[5]),
							forInt(pre_arraymsg[4]), pre_arraymsg.length))) {
				setfinalmsg(char2string(pre_arraymsg, pre_arraymsg.length));
				return true;
			} else {
				char[] newArrayMsg;
				newArrayMsg = new char[(pre_arraymsg.length - 1)];
				for (int cont = 0; cont < newArrayMsg.length; cont++) {
					newArrayMsg[cont] = pre_arraymsg[cont + 1];
				}
				return this.preamble(newArrayMsg);
			}
		} catch (Exception ex) {
			System.out.println("[JAVINO] Invalid message!");
			return false;
		}
	}

	private boolean monitormsg(int x, int y, int m_size) {
		int converted = x + (y * 16);
		int size_of_msg = m_size - 6;
		if (converted == size_of_msg) {
			return true;
		}
		return false;
	}

	private int forInt(char v) {
		int vI = 0;
		switch (v) {
		case '1':
			vI = 1;
			break;
		case '2':
			vI = 2;
			break;
		case '3':
			vI = 3;
			break;
		case '4':
			vI = 4;
			break;
		case '5':
			vI = 5;
			break;
		case '6':
			vI = 6;
			break;
		case '7':
			vI = 7;
			break;
		case '8':
			vI = 8;
			break;
		case '9':
			vI = 9;
			break;
		case 'a':
			vI = 10;
			break;
		case 'b':
			vI = 11;
			break;
		case 'c':
			vI = 12;
			break;
		case 'd':
			vI = 13;
			break;
		case 'e':
			vI = 14;
			break;
		case 'f':
			vI = 15;
			break;
		}
		return vI;
	}

	private String preparetosend(String msg) {
		msg = "fffe" + int2hex(msg.length()) + msg;
		return msg;
	}

	private String int2hex(int v) {
		String stringOne = Integer.toHexString(v);
		if (v < 16) {
			stringOne = "0" + stringOne;
		}
		return stringOne;
	}

	private void createPythonFile() {
		try {
			FileWriter arq = new FileWriter("javython.py");
			PrintWriter gravarArq = new PrintWriter(arq);
			gravarArq.printf("import sys\n");
			gravarArq.printf("import serial\n");

			gravarArq.printf("OP=sys.argv[1]\n");
			gravarArq.printf("PORT=sys.argv[2]\n");
			gravarArq.printf("MSG=sys.argv[3]\n");

			gravarArq.printf("try:\n");

			gravarArq.printf("\tif(OP=='command' or OP=='send'):\n");
			gravarArq.printf("\t\tcomm = serial.Serial(PORT, 9600, timeout=.1)\n");
			gravarArq.printf("\t\tcomm.open\n");
			gravarArq.printf("\t\tcomm.isOpen\n");
			gravarArq.printf("\t\tcomm.write(bytes(MSG, 'utf-8'))\n");
			gravarArq.printf("\t\tcomm.close\n");

			gravarArq.printf("\tif(OP=='request'):\n");
			gravarArq.printf("\t\tcomm = serial.Serial(PORT, 9600, timeout=3)\n");
			gravarArq.printf("\t\tcomm.open\n");
			gravarArq.printf("\t\tcomm.isOpen\n");
			gravarArq.printf("\t\tcomm.write(bytes(MSG, 'utf-8'))\n");
			gravarArq.printf("\t\tprint (comm.readline().decode())\n");
			gravarArq.printf("\t\tcomm.close\n");

			gravarArq.printf("\tif(OP=='listen'):\n");
			gravarArq.printf("\t\tcomm = serial.Serial(PORT, 9600)\n");
			gravarArq.printf("\t\tcomm.open\n");
			gravarArq.printf("\t\tcomm.isOpen\n");
			gravarArq.printf("\t\tprint (comm.readline().decode())\n");
			gravarArq.printf("\t\tcomm.close\n");

			gravarArq.printf("except:\n");
			gravarArq.printf("\tprint (\"Error on connect \"+PORT)\n");
			gravarArq.printf("\tsys.exit(1)\n");

			arq.flush();
			arq.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static void main(String args[]) {
		try {
			String type = args[0];
			if (type.equals("--help")) {
				System.out
						.println("java -jar javino.jar [TYPE] [PORT] [MSG] [PythonPath]");
				System.out
						.println("\n[TYPE] "
								+ "\n listen  -- wait an answer from Arduino"
								+ "\n request -- send a request to Arduino, wait answer "
								+ "\n command -- send a command to Arduino, without wait answer");
								
				System.out.println("\n[PORT]"
						+ "\n Set communication serial port"
						+ "\n example: \t COM3 - For Windows"
						+ "\n \t\t /dev/ttyACM0 - For Linux");
				System.out.println("\n[MSG]" + "\n Message for Arduino-side"
						+ "\n example: \t \"Hello Arduino!\"");
				System.out
						.println("\n[PythonPath]"
								+ "\n Set Path of Python in your system. This is a optional value."
								+ "example: \n\t \"C:\\\\Python39\" - For a System Windows"
								+ "\n\t \"/usr/bin\" - For a System Linux");
			} else {
				String port = args[1];
				String portAlias = port.substring(port.lastIndexOf("/")+1);
				String msg = args[2];
				String path = null;
				try {
					path = args[3];
				}
				catch (Exception ex) {
					System.out.println("[JAVINO] Using default path 'C:\\Python39' or '/usr/bin'");
				}


				Javino j;
				if (path == null) {
					j = new Javino();
				} else {
					j = new Javino(path);
				}

				if (type.equals("command")) {
					if (j.sendCommand(port, msg) == false) {
						System.out.println(portAlias+"(off);");
						//System.exit(1);
					}
				} else if (type.equals("request")) {
					if (j.requestData(port, msg) == true) {
						System.out.println(j.getData());
					} else {
						System.out.println(portAlias+"(off);");
						//System.exit(1);
					}
				} else if (type.equals("listen")) {
					if (j.listenArduino(port) == true) {
						System.out.println(j.getData());
					} else {
						System.exit(1);
					}
				}
			}
		} catch (Exception ex) {
			System.out.println("[JAVINO] Using version " + staticversion + " ChonOS");
			System.out
					.println("\tTo use Javino, look the User Manual at http://javino.sf.net");
			System.out
					.println("For more information try: \n\t java -jar javino.jar --help");
		}
	}

}

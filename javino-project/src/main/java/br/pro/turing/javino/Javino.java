package br.pro.turing.javino;

import com.fazecast.jSerialComm.*;

import java.io.IOException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

public class Javino {
	private final String version = staticversion;
	private static final String staticversion = "stable 1.6.0";
	private String finalymsg = null;
	private String PORTshortNAME = null;
	private SerialPort serialPort = null;
	private String portAddress  = "none";

	public Javino() {
		load();
	}

	private void closePort(){
		this.serialPort.closePort();
	}
	private void load() {
		System.out.println("[JAVINO] Using version " + this.version + " chonOS");
	}

	private void load(String portDescriptor){
		if(!getPortAddress().equals(portDescriptor)){
			if(!getPortAddress().equals("none")){
				System.out.println("[JAVINO] I'm disconnecting from "+getPortAddress());
				this.serialPort.closePort();
			}
			System.out.println("[JAVINO] I'm connecting to "+portDescriptor);
			this.serialPort = SerialPort.getCommPort(portDescriptor);
			this.serialPort.setParity(SerialPort.NO_PARITY);
			this.serialPort.setNumDataBits(8);
			this.serialPort.setNumStopBits(SerialPort.ONE_STOP_BIT);
			this.serialPort.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);
			this.serialPort.setBaudRate(9600);

			if(this.serialPort.openPort()){
				setPortAddress(portDescriptor);
				try{
					Thread.sleep(3000);
				}catch (Exception e){

				}
			}else{
				System.out.println("[JAVINO] Error: something went wrong. ");
			}
		}
	}

	public void setPortAddress(String portAddress) {
		this.portAddress = portAddress;
	}

	public String getPortAddress() {
		return portAddress;
	}

	public boolean sendCommand(String PORT, String MSG) {
		try{
			load(PORT);
			String out = "fffe"+String.format("%02X", MSG.length())+MSG;
			byte[] messageBytes = out.getBytes();
			this.serialPort.writeBytes(messageBytes, messageBytes.length);
			return true;
		}catch (Exception ex){
			ex.printStackTrace();
			return false;
		}
	}

	public boolean listenArduino(String PORT){
		try{
			load(PORT);
			byte[] preamble = new byte[4];
			byte[] sizeMessage = new byte[2];
			String finalm = null;
			if(this.serialPort.isOpen()){
				while(this.serialPort.bytesAvailable()<6){ }
				this.serialPort.readBytes(preamble,4);
				this.serialPort.readBytes(sizeMessage,2);
				if (((preamble[0] & preamble[1] & preamble[2]) == 102) & (preamble[3] == 101 )){
					int sizeOfMsg = Integer.parseUnsignedInt(new String(sizeMessage), 16);
					while(this.serialPort.bytesAvailable()<sizeOfMsg){ }
					byte[] byteMSG = new byte[sizeOfMsg];
					this.serialPort.readBytes(byteMSG,sizeOfMsg);
					setfinalmsg(new String(byteMSG));
					return true;
				}
			}
			return false;
		}catch (Exception ex){
			ex.printStackTrace();
			return false;
		}
	}

	public boolean requestData(String PORT, String MSG) {
		load(PORT);
		setPORTshortNAME(PORT);
		if(sendCommand(PORT,MSG)) {
			if(listenArduino(PORT)) {
				addfinalmsg("port("+getPORTshortNAME()+",on);");
			}else {
				setfinalmsg("port("+getPORTshortNAME()+",timeout);");
			}
			return true;
		}else {
			return false;
		}
	}

	public String getData() {
		String out = this.finalymsg;
		this.finalymsg = null;
		return out;
	}

	private void setfinalmsg(String s_msg) {
		this.finalymsg = s_msg;
	}

	private void addfinalmsg(String a_msg) {
		this.finalymsg = this.finalymsg+a_msg;
	}

	private void setPORTshortNAME(String PORT){
		this.PORTshortNAME=PORT.substring(PORT.lastIndexOf("/")+1);
	}

	private String getPORTshortNAME(){
		return this.PORTshortNAME;
	}

	public static void main(String args[]) throws IOException {
		Javino j = new Javino();
			try {
			if (args[0].equals("--help")) {
				System.out
						.println("\nuser@computer:$ java -jar javino.jar [PORT]");
				System.out
						.println("\tjavino@[PORT]$ [TYPE] [MSG] ");
				System.out
						.println("\n[TYPE] "
								+ "\n listen  -- wait an answer from Arduino"
								+ "\n request -- send a request to Arduino, wait answer "
								+ "\n command -- send a command to Arduino, without wait answer");
			} else {
				j.load(args[0]);
				String portAlias = args[0].substring(args[0].lastIndexOf("/")+1);
				try{
					Terminal terminal = TerminalBuilder.terminal();
					LineReader lineReader = LineReaderBuilder.builder()
							.terminal(terminal)
							.parser(new DefaultParser())
							.build();

					String lastCommand = "";

					while (true) {
						String input;

						try {
							input = lineReader.readLine("javino@"+portAlias+"$ ");
						} catch (UserInterruptException e) {
							// Tratar a interrupção do usuário (Ctrl-C)
							j.closePort();
							System.exit(0);
							break;
						}

						// Last Command
						if (input.isEmpty() && !lastCommand.isEmpty()) {
							input = lastCommand;
							terminal.writer().println(input);
						}

						// Javino Exec
						input = input.trim();
						String[] inputs = input.split(" ");
						if(inputs[0].equals("exit")){
							j.closePort();
							System.exit(0);
						} else if (inputs[0].equals("request")){
							if(j.requestData(args[0],inputs[1])){
								terminal.writer().println(j.getData());
							};
						}else if(inputs[0].equals("command")){
							j.sendCommand(args[0],inputs[1]);
						}else{
							terminal.writer().println(inputs[0]+": Unknown command");
						}

						lastCommand = input;
					}
				}catch (Exception e){
					e.printStackTrace();
				}
			}
		} catch (Exception ex) {
			System.out
					.println("\tTo use Javino, read the User Manual at http://javino.chon.group");
			System.out
					.println("For more information try: \n\tjava -jar javino.jar --help");
		}
	}
}

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
	private static final String staticversion = "stable 1.6.0 (jSerialComm)";
	private String finalymsg = null;
	private String PORTshortNAME = null;
	private SerialPort serialPort = null;
	private String portAddress  = "none";

	public Javino() {
		load();
	}

	private void closePort(){
		try{
			this.serialPort.closePort();
		}catch (Exception ex){
			//System.out.println("[JAVINO] Closing serial port - Error.");
		}
	}
	private boolean load() {
		System.out.println("[JAVINO] Using version " + this.version);
		return true;
	}

	private boolean load(String portDescriptor){
		if(!getPortAddress().equals(portDescriptor)){
			try{
				if(!getPortAddress().equals("none")){
					closePort();
				}
				//System.out.println("[JAVINO] I'm connecting to "+portDescriptor);
				this.serialPort = SerialPort.getCommPort(portDescriptor);
				this.serialPort.setParity(SerialPort.NO_PARITY);
				this.serialPort.setNumDataBits(8);
				this.serialPort.setNumStopBits(SerialPort.ONE_STOP_BIT);
				this.serialPort.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);
				this.serialPort.setBaudRate(9600);
				if(this.serialPort.openPort()){
					setPortAddress(portDescriptor);
					Thread.sleep(3000);
					return true;
				}else{
					System.out.println("[JAVINO] Error: something went wrong.");
					return false;
				}
			}catch (Exception ex){
				System.out.println("[JAVINO] Error: I'm connecting to "+portDescriptor+" something went wrong. ");
				return false;
			}
		}else{
			return true;
		}
	}

	public void setPortAddress(String portAddress) {
		this.portAddress = portAddress;
	}

	public String getPortAddress() {
		return portAddress;
	}

	public boolean sendCommand(String PORT, String MSG) {
		if(!load(PORT)){
			closePort();
			setPortAddress("unknown");
			return false;
		}
		try{
			if(this.serialPort.isOpen()){
				String out = "fffe"+String.format("%02X", MSG.length())+MSG;
				byte[] messageBytes = out.getBytes();
				this.serialPort.writeBytes(messageBytes, messageBytes.length);
				return true;
			}else{
				System.out.println(" SEND port open false");
				return false;
			}
		}catch (Exception ex){
			ex.printStackTrace();
			return false;
		}
	}

	public boolean listenArduino(String PORT){
		setPORTshortNAME(PORT);
		if(!load(PORT)){
			closePort();
			setfinalmsg("port("+getPORTshortNAME()+",off);");
			setPortAddress("unknown");
			return true;
		}
		try{
			byte[] preamble = new byte[4];
			byte[] sizeMessage = new byte[2];
			if(this.serialPort.isOpen()){
				long timeMillisInitial = System.currentTimeMillis();
				while(this.serialPort.bytesAvailable()<6){
					long timeMillisCurrent = System.currentTimeMillis();
					if(timeMillisInitial+1000 < timeMillisCurrent){
						setfinalmsg("port("+getPORTshortNAME()+",timeout);");
						setPortAddress("unknown");
						return false;
					}
				}
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
			}else{
				System.out.println("LISTEN port open false");
			}
			return false;
		}catch (Exception ex){
			ex.printStackTrace();
			return false;
		}
	}

	public boolean requestData(String PORT, String MSG) {
		setPORTshortNAME(PORT);
		if(sendCommand(PORT,MSG)) {
			if(listenArduino(PORT)) {
				addfinalmsg("port("+getPORTshortNAME()+",on);");
			}
			return true;
		}else {
			setfinalmsg("port("+getPORTshortNAME()+",off);");
			return true;
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
				if(!j.load(args[0])){
					System.exit(1);
				}
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

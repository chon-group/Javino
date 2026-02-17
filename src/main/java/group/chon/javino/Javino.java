package group.chon.javino;

import com.fazecast.jSerialComm.SerialPort;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import java.util.logging.Logger;

public class Javino {
	final String javinoVersion = "1.6.6";
	private String finalymsg = null;
	private String PORTshortNAME = null;
	private SerialPort serialPort = null;
	private String portAddress  = "none";
	private boolean infoPortStatus = false;
	private int TIMEOUT = 1000;
	Logger logger = Logger.getLogger(Javino.class.getName());

	public Javino() {
		logger.info("Using version "+getJavinoVersion()+" https://javino.chon.group/");
	}

	public void infoPortStatus(boolean status){
		this.infoPortStatus = status;
	}

	public void timeout(int newTimeout){
		this.TIMEOUT = newTimeout;
	}

	public String getPortAddress() {
		return portAddress;
	}

	public String getJavinoVersion(){
		return javinoVersion;
	}

	public void closePort(){
		try{
			this.serialPort.closePort();
			setPortAddress("none");
		}catch (Exception ex){
			logger.severe("Closing serial port - Error.");
		}
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
				logger.severe("port open false");
				return false;
			}
		}catch (Exception ex){
			logger.severe(ex.getMessage());
			return false;
		}
	}

	public boolean listenArduino(String PORT){
		setPORTshortNAME(PORT);
		if(!load(PORT)){
			closePort();
			if(this.infoPortStatus){
				setfinalmsg("port("+getPORTshortNAME()+",off);");
			}
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
					if(timeMillisInitial+TIMEOUT < timeMillisCurrent){
						if(this.infoPortStatus){
							setfinalmsg("port("+getPORTshortNAME()+",timeout);");
						}
						//setPortAddress("unknown");
						return false;
					}
				}
				this.serialPort.readBytes(preamble,4);
				this.serialPort.readBytes(sizeMessage,2);
				if (((preamble[0] & preamble[1] & preamble[2]) == 102) & (preamble[3] == 101 )){
					int sizeOfMsg = Integer.parseUnsignedInt(new String(sizeMessage), 16);
					while(true) {
                        if (this.serialPort.bytesAvailable() >= sizeOfMsg){
							break;
						}
                    }
					byte[] byteMSG = new byte[sizeOfMsg];
					this.serialPort.readBytes(byteMSG,sizeOfMsg);
					setfinalmsg(new String(byteMSG));
					return true;
				}
			}else{
				logger.severe("LISTEN port open false");
			}
			return false;
		}catch (Exception ex){
			logger.severe(ex.getMessage());
			return false;
		}
	}

	public boolean requestData(String PORT, String MSG) {
		setPORTshortNAME(PORT);
		if(sendCommand(PORT,MSG)) {
			if(listenArduino(PORT)) {
				if(this.infoPortStatus){
					addfinalmsg("port("+getPORTshortNAME()+",on);");
				}
			}
		}else {
			if(this.infoPortStatus){
				setfinalmsg("port("+getPORTshortNAME()+",off);");
			}
		}
		return true;
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

	private boolean load(String portDescriptor){
		if(!getPortAddress().equals(portDescriptor)){
			try{
				if(!getPortAddress().equals("none")){
					closePort();
				}
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
					logger.severe("Error: something went wrong.");
					return false;
				}
			}catch (Exception ex){
				logger.severe("Error: I'm connecting to "+portDescriptor+" something went wrong. ");
				return false;
			}
		}else{
			return true;
		}
	}

	private void setPortAddress(String portAddress) {
		this.portAddress = portAddress;
		setPORTshortNAME(portAddress);
	}

	// Javino-CLI
	public static void main(String[] args) {
		Javino j = new Javino();
			try {
			if (args[0].equals("--help")) {
				System.out
						.println("\n user@computer:$ java -jar javino.jar [PORT]"
								+ "\t javino@[PORT]$ [TYPE] [MSG] "
								+ "\n [TYPE] "
								+ "\n listen  -- wait an answer from Arduino"
								+ "\n request -- send a request to Arduino, wait answer "
								+ "\n command -- send a command to Arduino, without wait answer");
			} else {
				if(!j.load(args[0])){
					System.exit(1);
				}
				j.setPortAddress(args[0]);
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
							input = lineReader.readLine("javino@"+ j.getPORTshortNAME()+"$ ");
						} catch (UserInterruptException e) {
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
						switch (inputs[0]){
							case "exit" -> {
								j.closePort();
								System.exit(0);
							}
							case "request" ->{
								j.requestData(j.getPortAddress(),inputs[1]);
								terminal.writer().println(j.getData());
							}
							case "listen","read" -> {
								j.listenArduino(j.getPortAddress());
								terminal.writer().println(j.getData());
							}
							case "command","write"	-> j.sendCommand(j.getPortAddress(),inputs[1]);
							case "disconnect"		-> j.closePort();
							case "connect" 			-> j.load(inputs[1]);
							default					-> terminal.writer().println(inputs[0]+": Unknown command");
						}

						lastCommand = input;
					}
				}catch (Exception e){
					System.out.println(e.getMessage());
				}
			}
		} catch (Exception ex) {
			System.out
					.println("\tTo use Javino, read the User Manual at http://javino.chon.group/");
			System.out
					.println("For more information try: \n\tjava -jar javino.jar --help");
		}
	}
}

package br.pro.turing.javino;

import com.fazecast.jSerialComm.*;

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
		System.out.println("[JAVINO] Using version " + this.version + " ChonOS");
	}

	private void load(String portDescriptor){
		if(!getPortAddress().equals(portDescriptor)){
			if(!getPortAddress().equals("none")){
				System.out.println("[JAVINO] Disconecting with "+getPortAddress());
				this.serialPort.closePort();
			}
			System.out.println("[JAVINO] Connecting with "+portDescriptor);
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
				System.out.println("Error ");
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

	public static void main(String args[]) {
		try {
			String type = args[0];
			if (type.equals("--help")) {
				System.out
						.println("java -jar javino.jar [TYPE] [PORT] [MSG]");
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
			} else {
				String port = args[1];
				String portAlias = port.substring(port.lastIndexOf("/")+1);
				String msg = args[2];

				Javino j = new Javino();

				if (type.equals("command")) {
					if (j.sendCommand(port, msg) == false) {
						System.exit(1);
					}
				} else if (type.equals("request")) {
					if (j.requestData(port, msg) == true) {
						System.out.println(j.getData());
					} else {
						System.exit(1);
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
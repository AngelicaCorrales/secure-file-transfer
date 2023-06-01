package ui;

import model.Server;

public class ServerMain {

	public static void main(String[] args) {
	
		Server server= new Server();
		while(true) {
			server.start();
		}

	}

}

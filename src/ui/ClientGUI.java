package ui;
import java.io.File;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import model.Client;

public class ClientGUI {

	private Client client;
    
    @FXML
    private TextField txtLink;
    
    @FXML
    private BorderPane mainPanel;

    
	public ClientGUI(Client cl) {
		client = cl;
	}

	@FXML
    public void loadFile(ActionEvent event){
    	FileChooser fileChooser = new FileChooser();
    	fileChooser.setTitle("Open Resource File");
    	File fImp = fileChooser.showOpenDialog(mainPanel.getScene().getWindow());
    	if(fImp!=null) {
    		Alert alert = new Alert(AlertType.INFORMATION);
		    alert.setTitle("Load file");
			String path = fImp.getAbsolutePath();
			txtLink.setText(path);
			client.setFileName(path);
			alert.setHeaderText(null);
			alert.setContentText("El archivo ha sido cargado exitosamente.");
			alert.showAndWait();
    	}
    }
	
    @FXML
    public void startTransfer(ActionEvent event) {
    	client.startTransfer();

    }
}

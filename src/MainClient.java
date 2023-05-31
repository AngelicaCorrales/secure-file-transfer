import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainClient extends Application{
	
	private Client client;
	private ClientGUI clientGUI;
	
	public MainClient() {
		client = new Client();
		clientGUI = new ClientGUI(client);
	}
	
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/ui/client.fxml"));
		fxmlLoader.setController(clientGUI);
		Parent root = fxmlLoader.load();
		Scene scene = new Scene(root);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Secure file transfer");
		primaryStage.show();
		root.setStyle("-fx-background-image: url(/ui/fondo.jpg)");
	}

}

package ui;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.Client;

public class MainClient extends Application{
	
	private Client client;
	private ClientGUI clientGUI;

	/**
	 * Constructor de la clase MainClient.
	 * Crea una instancia del cliente y la interfaz gráfica del cliente.
	 */
	public MainClient() {
		client = new Client();
		clientGUI = new ClientGUI(client);
	}

	/**
	 * Método principal de la aplicación.
	 * Inicia la aplicación JavaFX llamando al método launch().
	 */
	public static void main(String[] args) {
		launch(args);
	}

	/**
	 * Método start() de la interfaz Application.
	 * Se ejecuta al iniciar la aplicación JavaFX.
	 * Carga la interfaz gráfica desde el archivo FXML, configura el controlador y muestra la ventana principal.
	 */
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

package costco.sim;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HelloApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/hello-view.fxml")
            );
            Parent root = loader.load();


            Scene scene = new Scene(root, 1200, 800);

            primaryStage.setTitle("Simulador de Filas - Costco Mexicali");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error cargando el FXML: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
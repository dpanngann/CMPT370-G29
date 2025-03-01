import COOKIES.COOKIES;
import COOKIES.GetCOOKIE;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class Login extends BasicPage {

    private Label errorLabel = new Label();

    private LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.web("#4B1517")),
            new Stop(1, Color.web("#C49102")));
    @Override
    public void start(Stage stage) {
//        stage.getIcons().add(new Image(getClass().getResourceAsStream("./resources/icon.png")));

        // Hello label message
        Label helloLabel = new Label("The Reno Group Admin App");
        helloLabel.setTextFill(Color.WHITE);
        helloLabel.setFont(new Font(36));
        HBox helloCentre = new HBox(); // HBox to centre the greeting label
        helloCentre.setAlignment(Pos.CENTER);
        helloCentre.getChildren().add(helloLabel);

        // Username label that prompts text field
        Label userLabel = new Label("Username: ");
        TextField userField = new TextField();
        userField.setPromptText("Enter your username");

        // Password label that prompts password field
        Label passLabel = new Label("Password: ");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Enter your password");

        // Check box to keep user logged into device
        CheckBox keepLogIn = new CheckBox("Keep me logged in!");
        keepLogIn.setTextFill(Color.WHITE);

        // Log in button
        Button logInButton = new Button("Log In");
        logInButton.setFont(new Font(18));
        HBox logInCentre = new HBox(); // HBox to centre button
        logInCentre.setAlignment(Pos.CENTER);
        logInCentre.getChildren().add(logInButton);

        // Set action for the login button
        logInButton.setOnAction(event -> {

            int status;
            try {
                status = login(userField.getText(), passField.getText());
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            if (status == 200) {
                // Close the login stage
                stage.close();
                // Launch the main page
                try {
                    new MainPage().start(new Stage());
                } catch (Exception e) {
                    System.out.println("Something went wrong when going into main page.");
                }
            }
            else {
                displayErrorMessage("Invalid username or password");
            }
        });

        // Registration button for new users
        Button newButton = new Button("New?");

        // Set action for the registration button
        newButton.setOnAction(event -> {
            // Close the login stage
            stage.close();
            // Launch the registration page
            try {
                new Registration().start(new Stage());
            } catch (Exception e) {
                System.out.println("Something went wrong when going into registration page.");
            }
        });

        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        root.getChildren().addAll(helloCentre, userLabel, userField, passLabel, passField, keepLogIn, logInCentre, newButton, errorLabel);

        // Page background designing

        root.setBackground(new Background(new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY)));
        for (Node node: root.getChildren()) {
            if (node instanceof Label) {
                ((Label) node).setTextFill(Color.WHITE);
            }
        }
        Scene scene = new Scene(root, 500, 800);
        stage.setTitle("Log In");
        stage.setScene(scene);
        stage.show();


    }

    public static int login(String username, String password) throws IOException, InterruptedException {
        String msg = "{" +
                "\"username\":\"" + username + "\"," +
                "\"password\":\"" + password + "\"" +
                "}";

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:5001/login"))
                .timeout(Duration.ofMinutes(2))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(msg, StandardCharsets.UTF_8))
                .build();

        System.out.println("[LOGIN]: " + request.toString());
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        String responseBody = response.body();


        try {
            ObjectMapper mapper = new ObjectMapper();
            GetCOOKIE getCOOKIE = mapper.readValue(responseBody, GetCOOKIE.class);
            COOKIES = getCOOKIE.getCookies();
        } catch (Exception e){
            showAlert("Error!","Login Faild.");
        }
        return response.statusCode();
    }

    // Add this method to display an error message
    private void displayErrorMessage(String message) {
        errorLabel.setText(message);
        errorLabel.setTextFill(Color.RED);
    }

    /**
     * Displays an error alert with the specified title and content.
     *
     * @param title   The title of the error alert.
     * @param content The content (message) of the error alert.
     */
    private static void showAlert(String title, String content) {
        // Create a new Alert with AlertType.ERROR
        Alert invalidNumAlert = new Alert(Alert.AlertType.ERROR);

        // Set the title of the alert
        invalidNumAlert.setTitle(title);

        // Set the header text (null means no header text)
        invalidNumAlert.setHeaderText(null);

        // Set the content text (main message) of the alert
        invalidNumAlert.setContentText(content);

        // Display the alert and wait for user interaction
        invalidNumAlert.showAndWait();
    }

    public static void main(String[] args) {
        launch();
    }
}
package ProjectRequestsMana;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ProjectRequests extends VBox {

    private TableView<ProjectRequestsItem> ProjectTable;
    private ObservableList<ProjectRequestsItem> data;

    public ProjectRequests() throws IOException, InterruptedException {
        // Setting up the table
        ProjectTable = new TableView<>();
        ProjectTable.prefWidthProperty().bind(this.widthProperty());
        data = FXCollections.observableArrayList();

        // Adding column names
        TableColumn<ProjectRequestsItem, String> ProjectCol = new TableColumn<>("Name");
        ProjectCol.setCellValueFactory(cellData -> cellData.getValue().Name());
        ProjectCol.prefWidthProperty().bind(ProjectTable.widthProperty().multiply(0.15));

        TableColumn<ProjectRequestsItem, String> EmailCol = new TableColumn<>("Email");
        EmailCol.setCellValueFactory(cellData -> cellData.getValue().Email());
        EmailCol.prefWidthProperty().bind(ProjectTable.widthProperty().multiply(0.10));

        TableColumn<ProjectRequestsItem, String> ContactCol = new TableColumn<>("Contact");
        ContactCol.setCellValueFactory(cellData -> cellData.getValue().Contact());
        ContactCol.prefWidthProperty().bind(ProjectTable.widthProperty().multiply(0.10));

        TableColumn<ProjectRequestsItem, String> CompanyCol = new TableColumn<>("Company");
        CompanyCol.setCellValueFactory(cellData -> cellData.getValue().Company());
        CompanyCol.prefWidthProperty().bind(ProjectTable.widthProperty().multiply(0.10));

        TableColumn<ProjectRequestsItem, String> Start_dateCol = new TableColumn<>("Start date");
        Start_dateCol.setCellValueFactory(cellData -> cellData.getValue().Start_date());
        Start_dateCol.prefWidthProperty().bind(ProjectTable.widthProperty().multiply(0));

        TableColumn<ProjectRequestsItem, String> End_dateCol = new TableColumn<>("End date");
        End_dateCol.setCellValueFactory(cellData -> cellData.getValue().End_date());
        End_dateCol.prefWidthProperty().bind(ProjectTable.widthProperty().multiply(0.10));

        TableColumn<ProjectRequestsItem, String> DescriptionCol = new TableColumn<>("Description");
        DescriptionCol.setCellValueFactory(cellData -> cellData.getValue().Description());
        DescriptionCol.prefWidthProperty().bind(ProjectTable.widthProperty().multiply(0.35));

        TableColumn<ProjectRequestsItem, String> ActionCol = new TableColumn<>("Action");
        ActionCol.setCellFactory(col -> new TableCell<ProjectRequestsItem, String>() {
            private final ComboBox<String> comboBox = new ComboBox<>();

            {
                comboBox.getItems().addAll("Accept", "Decline");
                comboBox.setOnAction(e -> handleAction(comboBox.getValue(), getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(comboBox);
                }
            }
        });
        ActionCol.prefWidthProperty().bind(ProjectTable.widthProperty().multiply(0.10));

        // set a custom row factory to change the row appearance based on the 'accepted' property
        ProjectTable.setRowFactory(tv -> new TableRow<ProjectRequestsItem>() {
            @Override
            protected void updateItem(ProjectRequestsItem item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else {
                    // if the project is accepted, highlight the row in green
                    if (item.isAccepted()) {
                        setStyle("-fx-background-color: lightgreen;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        ProjectTable.getColumns().addAll(ProjectCol,
                EmailCol,
                ContactCol,
                CompanyCol,
                Start_dateCol,
                End_dateCol,
                DescriptionCol,
                ActionCol
        );
        ProjectTable.setItems(data);

        loadProjects();

        Button deleteItem = new Button("Delete Project Transferred to Scheduler");
        Button refreshProjects = new Button("Refresh");
        refreshProjects.setOnAction(actionEvent -> {
            try {
                loadProjects();
            } catch(Exception e){
                showAlert("Error!", "Something went wrong when loading reviews");
            }
        });
//        Button addProject = new Button("Add Project");
//        addProject.setOnAction(actionEvent -> addProjectForDebugging());

        ProjectTable.setItems(data);

        deleteItem.setOnAction(actionEvent -> deleteProjectRequest());

        HBox optButton = new HBox(10, deleteItem, refreshProjects);
//        HBox optButton = new HBox(10, addProject, deleteItem, refreshProjects);
        optButton.setPadding(new Insets(10));

        VBox.setVgrow(ProjectTable, Priority.ALWAYS);
        this.getChildren().addAll(ProjectTable, optButton);
    }

//    private void addProjectForDebugging() {
//        ProjectRequestsItem debugItem = new ProjectRequestsItem(
//                "Debug Project", "debug@example.com", "1234567890", "Debug Company",
//                "2023-01-01", "2023-12-31", "This is a debug project", "Debug inquiry"
//        );
//        data.add(debugItem);
//        ProjectTable.refresh();
//    }
    private void handleAction(String action, ProjectRequestsItem project) {
        if ("Accept".equals(action)) {
            project.setAccepted(true);
        } else if ("Decline".equals(action)) {
            data.remove(project);
        }
        ProjectTable.refresh();
    }

    public void loadProjects() throws IOException, InterruptedException {
        this.data.clear();

        String url = "http://localhost:5001/submitRequest";


        // Create an HttpClient
        HttpClient httpClient = HttpClient.newHttpClient();


        // Build the request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        // Send POST message to Flask server
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        // Decode POST response and add reviews to table data
        try {
            ObjectMapper mapper = new ObjectMapper();

            ProjectRequestsItems projects = mapper.readValue(response.body(), ProjectRequestsItems.class);
            this.data.addAll(projects.getprojects());
        } catch (Exception e){
            System.out.println(e);
        }

        // Refresh table
        ProjectTable.refresh();

    }



    private void deleteProjectRequest() {
        // When item is selected ...
        ProjectRequestsItem selectedproject = ProjectTable.getSelectionModel().getSelectedItem();

        // Check if that item row is valid or does exists, if not, throw an alert
        if (selectedproject == null) {
            Alert noSelectedAlert = new Alert(Alert.AlertType.WARNING);
            noSelectedAlert.setTitle("Error!");
            noSelectedAlert.setHeaderText("No project is selected!");
            noSelectedAlert.setContentText("Please select a project from the table to delete.");
            noSelectedAlert.showAndWait();
            return;
        }

        // Else, remove the item from the table
        data.remove(selectedproject);
        ProjectTable.refresh();
    }

    private void showAlert(String title, String content) {
        Alert invalidNumAlert = new Alert(Alert.AlertType.ERROR);
        invalidNumAlert.setTitle(title);
        invalidNumAlert.setHeaderText(null);
        invalidNumAlert.setContentText(content);
        invalidNumAlert.showAndWait();
    }
}

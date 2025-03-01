package timeMana;

import COOKIES.COOKIES;
import ManagerCheck.ManagerCheck;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import employeeMana.Employee;
import employeeMana.EmployeeList;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.Optional;

public class Scheduler extends VBox {


    private static COOKIES COOKIES = null;
    // table to display projects
    private static TableView<Project> table = new TableView<>();

    // list that holds the data for the table
    public static ObservableList<Project> data;

    public static ObservableList<String> projectsTimelineList;

    /***
     * Constructor for Scheduler UI
     */
    public Scheduler(COOKIES COOKIES) {
        // Project Schedule label
        final Label label = new Label("Projects Schedule");
        label.setFont(new Font("Arial", 20));
        Scheduler.COOKIES = COOKIES;
        // initialize data
        data = FXCollections.observableArrayList();
        projectsTimelineList = FXCollections.observableArrayList();

        // make table editable
        // to display schedule in a table format
        table = new TableView<>();
        table.setEditable(true);

        // columns with appropriate headers
        TableColumn<Project, String> projName = new TableColumn<>("Project Name");
        projName.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        projName.prefWidthProperty().bind(table.widthProperty().multiply(0.20));

        TableColumn<Project, String> projTimeline = new TableColumn<>("Timeline");
        // set cell value factory to use timeline property of the Project class
        projTimeline.setCellValueFactory(cellData -> cellData.getValue().timelineProperty());
        projTimeline.setCellFactory(column -> new TableCell<Project, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                // check if cell is empty or not
                if (empty || item == null) {
                    // clear the text and style if the cell is empty
                    setText(null);
                    setStyle("");
                } else {
                    // retrieve the project object associated with current row
                    Project project = getTableView().getItems().get(getIndex());
                    if (project != null) {
                        // get timeline status from project object
                        String status = project.getTimelineStatus();
                        setText(item);

                        // change the cell's background colour based on timeline status
                        switch (status) {
                            case "green":
                                setStyle("-fx-background-color: lightgreen;");
                                break;
                            case "yellow":
                                setStyle("-fx-background-color: yellow;");
                                break;
                            case "red":
                                setStyle("-fx-background-color: salmon;");
                                break;
                            case "overdue":
                                setStyle("-fx-background-color: darkred;");
                                break;
                            default:
                                setStyle(""); // reset the style for any unknown status
                                break;
                        }
                    } else {
                        // clear the text and style if project is null
                        setText(null);
                        setStyle("");
                    }
                }
            }
        });
        projTimeline.prefWidthProperty().bind(table.widthProperty().multiply(0.15));

        TableColumn<Project, String> projDetails = new TableColumn<>("Details");
        projDetails.setCellValueFactory(cellData -> cellData.getValue().detailsProperty());
        projDetails.setCellFactory(column -> {
            // custom cell using a TextArea
            TableCell<Project, String> cell = new TableCell<>() {
                private final TextArea textArea = new TextArea();

                {
                    textArea.setWrapText(true); // set wrap text for better readability
                    textArea.setEditable(true); // set editable if you want users to edit the text
                    textArea.setPrefRowCount(1); // adjust the row count based on your requirement

                    // update the project details when editing is finished
                    textArea.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                        if (!isNowFocused) {
                            if (getTableRow() != null && getTableRow().getItem() != null) {
                                ((Project) getTableRow().getItem()).setDetails(textArea.getText());
                            }
                        }
                    });
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                    } else {
                        textArea.setText(item);
                        setGraphic(textArea);
                    }
                }
            };
            return cell;
        });
        projDetails.prefWidthProperty().bind(table.widthProperty().multiply(0.45));

        // currently only displays first member from the list of members
        TableColumn<Project, String> projMembers = new TableColumn<>("Members");
        projMembers.setCellValueFactory(cellData -> cellData.getValue().membersProperty());
        projMembers.prefWidthProperty().bind(table.widthProperty().multiply(0.20));

        // add all defined columns to TableView
        table.getColumns().addAll(projName, projTimeline, projDetails, projMembers);

        // add, modify, and delete buttons
        Button addButton = new Button("Refresh Projects");
        addButton.setOnAction(actionEvent -> {
            try {
                loadProjects(COOKIES);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        Button modifyButton = new Button("Modify");
        modifyButton.setOnAction(actionEvent -> modifyProject(COOKIES));

        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(actionEvent -> deleteProject(COOKIES));

        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(actionEvent -> {
            try {
                fetchProjects(COOKIES);
            } catch(Exception e){
                showAlert("Error!", "Something went wrong when fetching Projects!");
            }
        });

        // container for the buttons
        HBox buttonBox = new HBox(10, refreshButton);
        if (ManagerCheck.isManager(COOKIES)){
            buttonBox = new HBox(10, addButton, modifyButton, deleteButton, refreshButton);
        }
        buttonBox.setPadding(new Insets(10, 0, 10, 0));

        // add the label, table, and buttons to the main container
        this.getChildren().addAll(label, table, buttonBox);
        try {
            loadProjects(COOKIES);
        }catch (Exception e){
            showAlert("Error!","It looks like that there are nothing in the list yet");
        }
        table.setItems(data); // bind the data list to the table
    }

    /***
     * Method to add a project to the table list
     */
    public static void addProject(String projectName, String projectTimeline, String projectDetails) {

        // Check for duplicate project name
        for (Project project : data) {
            if (project.getName().equals(projectName)) {
                Alert duplicateAlert = new Alert(Alert.AlertType.ERROR);
                duplicateAlert.setTitle("Error!");
                duplicateAlert.setHeaderText("Project already exists!");
                duplicateAlert.setContentText("Please choose a different project name.");
                duplicateAlert.showAndWait();
                return;
            }
        }

        // provide choices for members and prompt user to select one (for now)
        ObservableList<String> choices = EmployeeList.employeeFirstNameList;
        ChoiceDialog<String> memberDialog = new ChoiceDialog<>("Members", choices);
        memberDialog.setTitle("Choose a Member");
        memberDialog.setHeaderText("Choose a Project Member");
        String selectedMember = memberDialog.showAndWait().orElse("");

        // create a new project instance using the user-provided info
        String msg = "{" +
                "\"pName\":\"" + projectName + "\"," +
                "\"pTime\":\"" + projectTimeline + "\"," +
                "\"pDetails\":\"" + projectDetails + "\"," +
                "\"pMember\":\"" + selectedMember + "\"" +
                "}";

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:5001/addProjects"))
                .timeout(Duration.ofMinutes(2))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(msg, StandardCharsets.UTF_8))
                .build();

        System.out.println("[ADD TIME PROJECTS]: " + request.toString());
        try{
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        }catch (Exception e){
            System.out.println(e);
        }
        try{
            loadProjects(COOKIES);
        }catch(Exception e){
            System.out.println(e);
        }

        // add the project to data list
        projectsTimelineList.add(projectTimeline);
        int chosenEmployeeIdx = EmployeeList.employeeSearch(selectedMember);
        if (chosenEmployeeIdx == -1){
            Alert notFoundError = new Alert(Alert.AlertType.ERROR);
            notFoundError.setTitle("Error!");
            notFoundError.setHeaderText("Employee Search");
            notFoundError.setContentText("Employee Not Found! Please try again");
            notFoundError.showAndWait();
        } else{
            EmployeeList.data.get(chosenEmployeeIdx).addProject2Employee(data.getLast());


        }
        table.refresh();
    }

    /**
     * Validate the date format of the input. Must be yyyy-mm-dd. Returns false if not valid,
     * otherwise, true.
     */
    private boolean isValidDate(String input) {
        // Checking validity using regular expression
        String datePattern = "\\d{4}-\\d{2}-\\d{2}";
        return input.matches(datePattern);
    }

    /***
     * Method to modify a selected project
     */
    private void modifyProject(COOKIES COOKIES) {
        // get currently selected project from the table
        Project selectedProject = table.getSelectionModel().getSelectedItem();

        // show an alert and return if no project is selected
        if (selectedProject == null) {
            Alert noSelectedAlert = new Alert(Alert.AlertType.WARNING);
            noSelectedAlert.setTitle("Error!");
            noSelectedAlert.setHeaderText("No project is selected!");
            noSelectedAlert.setContentText("Please select a project from the table to modify.");
            noSelectedAlert.showAndWait();
            return;
        }



        // Loop for project timeline input
        String newProjectTimeline = "";
        while (true) {
            TextInputDialog timelineInput = new TextInputDialog(selectedProject.getTimeline());
            timelineInput.setHeaderText("Enter New Project Timeline (format: yyyy-mm-dd)");
            Optional<String> timelineResult = timelineInput.showAndWait();
            if (timelineResult.isPresent()) {
                String input = timelineResult.get().trim();
                if (isValidDate(input)) {
                    newProjectTimeline = input;
                    break;
                } else {
                    Alert invalidFormatAlert = new Alert(Alert.AlertType.ERROR);
                    invalidFormatAlert.setTitle("Invalid Format");
                    invalidFormatAlert.setHeaderText("Invalid Timeline Format");
                    invalidFormatAlert.setContentText("Please enter the date in the format yyyy-mm-dd (e.g., 2022-11-06).");
                    invalidFormatAlert.showAndWait();
                }
            } else {
                return; // User clicked cancel or closed the dialog
            }
        }

        // Loop for project details input
        String newProjectDetails = "";
        while (true) {
            TextInputDialog detailsInput = new TextInputDialog(selectedProject.getDetails());
            detailsInput.setHeaderText("Enter New Project Details");
            Optional<String> detailsResult = detailsInput.showAndWait();
            if (detailsResult.isPresent() && !detailsResult.get().trim().isEmpty()) {
                newProjectDetails = detailsResult.get().trim();
                break;
            } else if (!detailsResult.isPresent()) {
                return; // User clicked cancel or closed the dialog
            }
        }



        ObservableList<String> choices = EmployeeList.employeeFirstNameList;
        ChoiceDialog<String> memberDialog = new ChoiceDialog<>("Members", choices);
        memberDialog.setTitle("Choose a Member");
        memberDialog.setHeaderText("Choose a Project Member");

        String oldMember = selectedProject.getMembers();

        EmployeeList.data.get(EmployeeList.employeeSearch(oldMember)).getProjects().remove(selectedProject);
        String newMember = memberDialog.showAndWait().orElse("");

        EmployeeList.data.get(EmployeeList.employeeSearch(newMember)).getProjects().add(selectedProject);

        String msg = "{" +
                "\"pName\":\"" + selectedProject.getName() + "\"," +
                "\"pTime\":\"" + newProjectTimeline + "\"," +
                "\"pDetails\":\"" + newProjectDetails + "\"," +
                "\"pMember:\"" + newMember + "\"," +
                "}";

        System.out.println(msg);

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:5001/modTimeProject"))
                .timeout(Duration.ofMinutes(2))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(msg, StandardCharsets.UTF_8))
                .build();

        System.out.println("[MOD TIME PROJECTS]: " + request.toString());
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        }catch (Exception e){
            System.out.println(e);
        }

        try{
            loadProjects(COOKIES);
        }catch (Exception e){
            showAlert("Error!","project details is not modified");
        }

        table.refresh();
    }

    private String fetchProjects(COOKIES COOKIES) throws IOException, InterruptedException {
        System.out.println(COOKIES.getUsername());
        String msg = "{" +
                "\"username\":\"" + COOKIES.getUsername() +
                "\"}";

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:5001/getProjectsData"))
                .timeout(java.time.Duration.ofMinutes(2))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(msg, StandardCharsets.UTF_8))
                .build();

        System.out.println("[DASHBOARD] " + request.toString());
        System.out.println(msg);
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        String responseBody = response.body();

        return responseBody;
    }


    /***
     * Method to delete a selected project
     */
    private void deleteProject(COOKIES COOKIES) {

        // get currently selected project from table
        Project selectedProject = table.getSelectionModel().getSelectedItem();

        // remove selected project from data list
        if (selectedProject != null) {
            data.remove(selectedProject);
            for(Employee employee:EmployeeList.data){
                if (employee.getProjects().contains(selectedProject)){
                    employee.removeProject2Employee(selectedProject);
                }
            }
            String msg = "{" +
                    "\"deleteProjectName\":\"" + selectedProject.getName() + "\"" +
                    "}";
            System.out.println("[TIME PROJECT DELETE]: " + msg);

            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://127.0.0.1:5001/deleteTimeProjects"))
                    .timeout(Duration.ofMinutes(2))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(msg, StandardCharsets.UTF_8))
                    .build();

            System.out.println("[TIME PROJECT DELETE]: " + request.toString());
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            }catch (Exception e){
                System.out.println(e);
            }

            try{
                loadProjects(COOKIES);
            }catch (Exception e){
                showAlert("Error!","It looks like that you deleted all the items from the table.");
            }

        } else { // show alert if no project is selected
            Alert noSelectedAlert = new Alert(Alert.AlertType.WARNING);
            noSelectedAlert.setTitle("Error!");
            noSelectedAlert.setHeaderText("No project is selected!");
            noSelectedAlert.setContentText("Please select a project from the table to delete.");
            noSelectedAlert.showAndWait();
        }
    }

    private static void loadProjects(COOKIES COOKIES) throws IOException, InterruptedException {
        data.clear();
        projectsTimelineList.clear();
        String msg = "{" +
                "\"cookie\":\"" + COOKIES.getUsername() + "\"" +
                "}";
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:5001/getProjects"))
                .timeout(Duration.ofMinutes(2))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(msg, StandardCharsets.UTF_8))
                .build();

        System.out.println("[LOAD TIME PROJECTS]: " + request.toString());
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        try{
            ObjectMapper mapper = new ObjectMapper();

            Projects projects = mapper.readValue(response.body(),Projects.class);
            for (Project project:projects.getProjects()){
                if(project != null && !data.contains(project)){
                    data.add(project);
                }
            }

        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        for (Project project: data){
            projectsTimelineList.add(project.getTimeline());
        }

        table.refresh();
    }

    public static Project searchProjectByName(String projectName){

        for (Project project:data){
            if (project.getName().equals(projectName)){
                return project;
            }
        }
        return null;
    }

    private void showAlert(String title, String content) {
        Alert invalidNumAlert = new Alert(Alert.AlertType.ERROR);
        invalidNumAlert.setTitle(title);
        invalidNumAlert.setHeaderText(null);
        invalidNumAlert.setContentText(content);
        invalidNumAlert.showAndWait();
    }

}
package employeeMana;

import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.stage.Stage;

import timeMana.Project;

/**
 * This class represents an Employee List UI component, which displays a list of employees
 * and provides buttons for adding, deleting, modifying employee information, and viewing personal details.
 */
public class EmployeeList extends VBox {

    private TableView<Employee> employeeList; // Table view for displaying employees
    public static ObservableList<Employee> data; // Observable list for storing employee data

    /**
     * Constructor for the EmployeeList class.
     * Initializes the UI components and sets up the employee table with columns and buttons.
     */
    public EmployeeList() {
        // Setting up the table
        employeeList = new TableView<>();
        employeeList.prefWidthProperty().bind(this.widthProperty());
        data = FXCollections.observableArrayList();

        // Adding column names
        TableColumn<Employee, String> firstNameCol = new TableColumn<>("Employee First Name");
        // Bind the cell value factory to employee's first name property
        firstNameCol.setCellValueFactory(cellData -> cellData.getValue().firstNameProperty());
        firstNameCol.prefWidthProperty().bind(employeeList.widthProperty().multiply(0.15)); // 15% width

        TableColumn<Employee, String> lastNameCol = new TableColumn<>("Employee Last Name");
        // Bind the cell value factory to employee's last name property
        lastNameCol.setCellValueFactory(cellData -> cellData.getValue().lastNameProperty());
        lastNameCol.prefWidthProperty().bind(employeeList.widthProperty().multiply(0.15)); // 15% width

        TableColumn<Employee, String> employeeIdCol = new TableColumn<>("Employee ID");
        // Bind the cell value factory to employee's ID property
        employeeIdCol.setCellValueFactory(cellData -> cellData.getValue().employeeIDProperty());
        employeeIdCol.prefWidthProperty().bind(employeeList.widthProperty().multiply(0.1)); // 10% width

        TableColumn<Employee,String> eMailCol = new TableColumn<>("Email");
        // Bind the cell value factory to employee's email property
        eMailCol.setCellValueFactory(cellData -> cellData.getValue().eMailProperty());
        eMailCol.prefWidthProperty().bind(employeeList.widthProperty().multiply(0.3)); // 30% width

        TableColumn<Employee,String> cellNumberCol = new TableColumn<>("Cell Phone");
        // Bind the cell value factory to employee's cell phone property
        cellNumberCol.setCellValueFactory(cellData -> cellData.getValue().cellProperty());
        cellNumberCol.prefWidthProperty().bind(employeeList.widthProperty().multiply(0.3)); // 30% width

        // Add columns to the table
        employeeList.getColumns().addAll(firstNameCol, lastNameCol, employeeIdCol, eMailCol, cellNumberCol);
        employeeList.setItems(data);

        // Create buttons and set their action handlers
        Button addItem = new Button("Add");
        addItem.setOnAction(actionEvent -> addEmployeeInfo());

        Button deleteItem = new Button("Delete");
        deleteItem.setOnAction(actionEvent -> deleteEmployee());

        Button modifyItem = new Button("Modify");
        modifyItem.setOnAction(actionEvent -> modifyEmployeeInfo());

        Button employeeInfo = new Button("Personal Details");
        employeeInfo.setOnAction(actionEvent -> openEmployeeInfo());

        // Button addEProject = new Button("Add Project");
        // employeeInfo.setOnAction(actionEvent -> {addProject();});

        // Create a horizontal box to hold the buttons
        HBox optButton = new HBox(10, addItem, deleteItem, modifyItem, employeeInfo);
        optButton.setPadding(new Insets(10, 0, 10, 0)); // top, right, bottom, left padding

        // Set vertical grow for the table and add it along with the buttons to the VBox
        VBox.setVgrow(employeeList, Priority.ALWAYS);
        this.getChildren().addAll(employeeList, optButton);
    }


    /**
     * Generates a formatted employee ID based on the provided integer ID.
     *
     * @param employeeID The integer ID of the employee.
     * @return The formatted employee ID as a string.
     */
    private String generateID(int employeeID) {
        // Create a StringBuilder with initial value "00000000"
        StringBuilder result = new StringBuilder("00000000");

        // Convert the integer ID to a string
        String strid = String.valueOf(employeeID);

        // Iterate through the characters of the string ID and replace the corresponding characters in the result StringBuilder
        for (int i = 0; i < strid.length(); i++) {
            result.setCharAt(result.length() - i - 1, strid.charAt(strid.length() - i - 1));
        }

        // Return the formatted employee ID as a string
        return result.toString();
    }

    /**
     * Formats the given employee cell phone number to a specific format (e.g., (XXX) XXX-XXXX).
     *
     * @param employeeCell The employee's cell phone number.
     * @return The formatted cell phone number as a string.
     */
    private String formatCell(String employeeCell) {
        // Create a StringBuffer with the employee cell phone number
        StringBuffer result = new StringBuffer(employeeCell);

        // Insert parentheses, space, and dash at specific positions in the cell phone number
        result.insert(0, "(");
        result.insert(4, ") ");
        result.insert(9, "-");

        // Return the formatted cell phone number as a string
        return result.toString();
    }
    /**
     * Shows an alert dialog with the specified title and content text.
     *
     * @param title   The title of the alert dialog.
     * @param content The content text of the alert dialog.
     */
    private void showAlert(String title, String content) {
        Alert invalidNumAlert = new Alert(Alert.AlertType.ERROR);
        invalidNumAlert.setTitle(title);
        invalidNumAlert.setHeaderText(null);
        invalidNumAlert.setContentText(content);
        invalidNumAlert.showAndWait();
    }

    /**
     * Opens a new window displaying the personal details of the selected employee.
     * If no employee is selected, shows a warning message.
     */
    private void openEmployeeInfo() {
        // Get the selected employee from the table view
        Employee selectedEmployee = employeeList.getSelectionModel().getSelectedItem();

        // Check if an employee is selected
        if (selectedEmployee == null) {
            // If no employee is selected, show a warning message
            showAlert("Error!", "No employee is selected! Please select an employee from the list to check details.");
            return;
        }

        // If an employee is selected, open a new window displaying their details
        new EmployeeInfo(selectedEmployee).start(new Stage());
    }

    /**
     * Prompts the user to enter new employee information and adds the employee to the employee list.
     */
    private void addEmployeeInfo() {
        // Generate a unique employee ID for the new employee
        int employeeID = employeeList.getItems().size() + 1;
        String stringid = generateID(employeeID);

        // Prompt the user for first name
        TextInputDialog firstNameInput = new TextInputDialog();
        firstNameInput.setTitle("Add New First Name");
        firstNameInput.setHeaderText("Enter First Name");
        String firstName = firstNameInput.showAndWait().orElse("");

        // Prompt the user for last name
        TextInputDialog lastNameInput = new TextInputDialog();
        lastNameInput.setTitle("Add New Last Name");
        lastNameInput.setHeaderText("Enter Last Name");
        String lastName = lastNameInput.showAndWait().orElse("");

        ObservableList<Project> projects = null;

        // Prompt the user for cell phone number
        String newEmployeeCell;
        try {
            TextInputDialog cellInput = new TextInputDialog("0");
            cellInput.setHeaderText("Enter 10 digit American Phone Number");
            newEmployeeCell = cellInput.showAndWait().orElse("Invalid Input!");

            // Validate the cell phone number format
            if (newEmployeeCell.length() != 10) {
                throw new RuntimeException();
            }
        } catch (RuntimeException e) {
            // Show an error message if the input is not a valid 10-digit number
            showAlert("Invalid Input!", "Please enter a valid 10 digit Canadian Cell Number");
            return;
        }
        // Format the cell phone number
        String employeeCell = formatCell(newEmployeeCell);

        // Prompt the user for email address
        String employeeEMail;
        try {
            TextInputDialog emailInput = new TextInputDialog("0");
            emailInput.setHeaderText("Enter Valid Email Address");
            employeeEMail = emailInput.showAndWait().orElse("");

            // Validate the email address format
            if (!employeeEMail.contains("@")) {
                throw new RuntimeException();
            }
        } catch (RuntimeException e) {
            // Show an error message if the input is not a valid email address
            showAlert("Invalid Input!", "Please enter a valid Email address");
            return;
        }

        // Prompt the user for employee title
        TextInputDialog titleInput = new TextInputDialog();
        titleInput.setTitle("Title Assignment");
        titleInput.setHeaderText("Add title to this employee");
        String employeeTitle = titleInput.showAndWait().orElse("");

        // Create the new employee and add it to the data list
        Employee newEmployee = new Employee(new SimpleStringProperty(stringid), new SimpleStringProperty(firstName),
                new SimpleStringProperty(lastName), new SimpleListProperty<Project>(projects),
                new SimpleStringProperty(employeeCell), new SimpleStringProperty(employeeEMail),
                new SimpleStringProperty(employeeTitle));

        // Add the new employee to the data list and refresh the table view
        this.data.add(newEmployee);
        employeeList.refresh();
    }

    /*
    private void addProject() {
        Employee selectedEmployee = employeeList.getSelectionModel().getSelectedItem();

        // Check if that item row is valid or does exists, if not, throw an alert
        if (selectedEmployee == null) {
            Alert noSelectedAlert = new Alert(Alert.AlertType.WARNING);
            noSelectedAlert.setTitle("Error!");
            noSelectedAlert.setHeaderText("No Employee is selected!");
            noSelectedAlert.setContentText("Please select an Employee from the table to modify.");
            noSelectedAlert.showAndWait();
            return;
        }
    }

     */

    /**
     * Allows the user to modify the information of a selected employee.
     */
    private void modifyEmployeeInfo() {
        // Get the selected employee from the table view
        Employee selectedEmployee = employeeList.getSelectionModel().getSelectedItem();

        // Check if an employee is selected
        if (selectedEmployee == null) {
            // If no employee is selected, show a warning message
            showAlert("Error!", "No employee is selected! Please select an employee from the list to modify.");
            return;
        }

        // Create a new stage for modification options
        Stage modifyStage = new Stage();
        TilePane modifyTile = new TilePane();

        // Label for prompting the user
        Label promptmsg = new Label("Select information to modify:");

        // Checkboxes for different fields
        CheckBox firstNameMod = new CheckBox("First Name");
        CheckBox lastNameMod = new CheckBox("Last Name");
        CheckBox cellNumberMod = new CheckBox("Cell Number");
        CheckBox eMailMod = new CheckBox("Email");
        CheckBox titleMod = new CheckBox("Job Title");

        // Button for confirming modifications
        Button doneButton = new Button("Done");
        doneButton.setOnAction(event -> {
            // Check which fields are selected for modification

            if (firstNameMod.isSelected()) {
                TextInputDialog firstNameInput = new TextInputDialog(selectedEmployee.getEmployeeFirstName());
                firstNameInput.setTitle("Modify First Name");
                firstNameInput.setHeaderText("Enter new First Name");
                String newFirstName = firstNameInput.showAndWait().orElse("");
                selectedEmployee.firstNameProperty().set(newFirstName);
            }

            if (lastNameMod.isSelected()) {
                TextInputDialog lastNameInput = new TextInputDialog(selectedEmployee.getEmployeeLastName());
                lastNameInput.setTitle("Modify Last Name");
                lastNameInput.setHeaderText("Enter new Last Name");
                String newLastName = lastNameInput.showAndWait().orElse("");
                selectedEmployee.lastNameProperty().set(newLastName);
            }

            if (cellNumberMod.isSelected()) {
                String newEmployeeCell;
                try {
                    TextInputDialog cellInput = new TextInputDialog(selectedEmployee.getCell());
                    cellInput.setHeaderText("Enter 10 digit American Phone Number");
                    newEmployeeCell = cellInput.showAndWait().orElse("Invalid Input!");

                    // Validate the cell phone number format
                    if (newEmployeeCell.length() != 10) {
                        throw new RuntimeException();
                    }
                    selectedEmployee.cellProperty().set(formatCell(newEmployeeCell));
                } catch (RuntimeException e) {
                    showAlert("Error!", "Invalid Input! Please enter a valid 10 digit Canadian Cell Number");
                    return;
                }
            }

            if (eMailMod.isSelected()) {
                TextInputDialog emailInput = new TextInputDialog(selectedEmployee.getEMail());
                emailInput.setHeaderText("Enter Valid Email Address");
                String newEmployeeEmail = emailInput.showAndWait().orElse("");

                // Validate the email address format
                if (!newEmployeeEmail.contains("@")) {
                    showAlert("Error!", "Invalid Input! Please enter a valid Email address");
                    return;
                }
                selectedEmployee.eMailProperty().set(newEmployeeEmail);
            }

            if (titleMod.isSelected()) {
                TextInputDialog titleInput = new TextInputDialog(selectedEmployee.getTitle());
                titleInput.setTitle("Modify Title");
                titleInput.setHeaderText("Enter new Title");
                String newEmployeeTitle = titleInput.showAndWait().orElse("");
                selectedEmployee.titleProperty().set(newEmployeeTitle);
            }

            // Refresh the table view and close the modification window
            employeeList.refresh();
            modifyStage.close();
        });

        // Add UI elements to the modification stage
        modifyTile.getChildren().addAll(promptmsg, firstNameMod, lastNameMod, cellNumberMod, eMailMod, titleMod, doneButton);

        Scene modifyScene = new Scene(modifyTile, 200, 200);
        modifyStage.setTitle("Modify Options");
        modifyStage.setScene(modifyScene);
        modifyStage.show();
    }

    /**
     * Deletes the selected employee from the employee list.
     */
    private void deleteEmployee() {
        // Get the selected employee from the table view
        Employee selectedEmployee = employeeList.getSelectionModel().getSelectedItem();

        // Check if an employee is selected
        if (selectedEmployee == null) {
            // If no employee is selected, show a warning message
            showAlert("Error!", "No employee is selected! Please select an employee from the list to delete.");
            return;
        }

        // Remove the selected employee from the data list
        data.remove(selectedEmployee);

        // Refresh the table view to reflect the changes
        employeeList.refresh();
    }

}
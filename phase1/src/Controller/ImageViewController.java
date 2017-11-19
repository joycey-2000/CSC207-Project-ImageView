package Controller;

import Model.Image;
import Model.ImageManager;
import Model.TagManager;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Optional;

public class ImageViewController {
    @FXML
    private Button Move;
    @FXML
    private Button PREV;
    @FXML
    private Button NEXT;
    @FXML
    private Button CLOSE;
    @FXML
    private Button Delete;
    @FXML
    private Button Add;
    @FXML
    private Button rename;
    @FXML
    private ListView<String> listView;
    @FXML
    private javafx.scene.image.ImageView show;
    @FXML
    private Label Name;
    // New Thing
    @FXML
    private ListView<String> Tags;
    @FXML
    private TextField newTag;
    @FXML
    private Button history;

    private File curFile;

    private Image selectedImage;

    void GetImage(File image) {
        curFile = image;
        // TODO: what if there is already a Image instance for this file?
        selectedImage = new Image(curFile);
        //new Model.ImageRenameObserver(selectedImage);
        initData(selectedImage);
    }

    private void initData(Image image){
//        selectedImage = image;
        Name.setText(image.getName());
        Collection<String> col = image.getCurrentTags();
        listView.getItems().clear();
        listView.getItems().addAll(col);
        // Create a listView for store all tags.
        Collection<String> tags = TagManager.getTagList();
        Tags.getItems().clear();
        Tags.getItems().addAll(tags);
        File imageFile = image.getFile();
        javafx.scene.image.Image image1 = new javafx.scene.image.Image(imageFile.toURI().toString());
        show.setImage(image1);
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    public void GoBack() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.load(getClass().getResource("sample.fxml").openStream());
        Controller controller = loader.getController();
        try {
            GetImage(controller.getPrevImage(curFile));
        } catch (IndexOutOfBoundsException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Oops");
            alert.setHeaderText(null);
            alert.setContentText("This is already the first image of the list!");
            alert.showAndWait();
        }
    }

    public void Rename(ActionEvent event) throws IOException {
        Window window = rename.getScene().getWindow();
        Stage primaryStage = new Stage();
        primaryStage.initOwner(window);
        FXMLLoader loader = new FXMLLoader();
        Pane root = loader.load(getClass().getResource("Rename.fxml").openStream());
        RenameController rename = loader.getController();
        rename.getImage(selectedImage);
        primaryStage.setTitle("Rename");
        primaryStage.setScene(new Scene(root, 500, 300));
        primaryStage.showAndWait();
    }

    public void MoveFile() throws IOException {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choose directory which you wanna move into");
        File selectedDirectory =
                directoryChooser.showDialog(Move.getScene().getWindow());
        String path = selectedDirectory.getAbsolutePath();
        selectedImage.move(path + "/" + selectedImage.getName());
    }

    public void GoNext() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.load(getClass().getResource("sample.fxml").openStream());
        Controller controller = loader.getController();
        try {
            GetImage(controller.getNextImage(curFile));
        } catch (IndexOutOfBoundsException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Oops");
            alert.setHeaderText(null);
            alert.setContentText("This is already the last image of the list!");
            alert.showAndWait();
        }

    }

    public void Exit() {
        Stage stage = (Stage) CLOSE.getScene().getWindow();
        stage.close();
    }

    public void DeleteTag() throws IOException {
        ObservableList<String> delete = listView.getSelectionModel().getSelectedItems();
        for (String tag : delete) {
            ImageManager.deleteTag(selectedImage.getFile().getAbsolutePath(), tag);
        }
        initData(selectedImage);
    }

    public void InputNewTag() throws IOException{
        String input = newTag.getText();
        if (input != null) {
            if (input.length() == 0) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirmation");
                alert.setContentText("Are you sure you want to add an empty tag?");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK){
                    // ... user chose OK
                    TagManager.addTag(input);
                    initData(selectedImage);
                }
            } else {
                TagManager.addTag(input);
                initData(selectedImage);
            }
            newTag.clear();
        }
    }

    public void AddTags() throws IOException{
        ObservableList<String> list = Tags.getSelectionModel().getSelectedItems();
        for (String tag : list) {
            ImageManager.addTag(selectedImage.getFile().getAbsolutePath(), tag);
        }
        initData(selectedImage);
    }

    public void ButtonHistory(ActionEvent event) {
        try {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("History");
            alert.setContentText("Here is your all history changes of this image:");
            String history = ImageManager.getLog(selectedImage.getFile().getAbsolutePath());
            TextArea textArea = new TextArea(history);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            GridPane.setVgrow(textArea, Priority.ALWAYS);
            GridPane.setHgrow(textArea, Priority.ALWAYS);

            GridPane expContent = new GridPane();
            expContent.setMaxWidth(Double.MAX_VALUE);
            expContent.add(textArea, 0, 0);

            alert.getDialogPane().setExpandableContent(expContent);

            alert.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}

package eu.artofcoding.kellerautomat.xml2csv;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class GuiController implements Initializable, LoggerCallback {

    private Preferences preferences = Preferences.userNodeForPackage(GuiController.class);

    @FXML
    private Button chooseSourceDirectoryButton;

    @FXML
    private TextField sourcePathTextField;

    private File sourceFilePath;

    @FXML
    private Button chooseXslDirectoryButton;

    @FXML
    private TextField xslPathTextField;

    private File xslFilePath;

    @FXML
    private Button chooseTargetDirectoryButton;

    @FXML
    private TextField targetPathTextField;

    private File targetFilePath;

    @FXML
    private Button convertFilesButton;

    @FXML
    private TextArea logTextArea;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String preferencesPath = preferences.get("xslDirectory", null);
        if (null != preferencesPath) {
            xslPathTextField.setText(preferencesPath);
        }
    }

    public void log(String message) {
        final String text = String.format("%s: %s%n", new Date(), message);
        Platform.runLater(() -> logTextArea.appendText(text));
    }

    public void chooseSourceDirectoryAction(ActionEvent event) {
        Stage stage = (Stage) chooseSourceDirectoryButton.getScene().getWindow();
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choose Source Directory with XML Files");
        File file = directoryChooser.showDialog(stage);
        if (null != file) {
            sourceFilePath = file;
            sourcePathTextField.setText(file.getAbsolutePath());
        }
    }

    public void chooseXslDirectoryAction(ActionEvent event) {
        Stage stage = (Stage) chooseXslDirectoryButton.getScene().getWindow();
        DirectoryChooser directoryChooser = new DirectoryChooser();
        String preferencesPath = preferences.get("xslDirectory", null);
        if (null != preferencesPath) {
            File initialDirectory = new File(preferencesPath);
            directoryChooser.setInitialDirectory(initialDirectory);
        }
        directoryChooser.setTitle("Choose Directory with XSL Files");
        File file = directoryChooser.showDialog(stage);
        if (null != file) {
            xslFilePath = file;
            preferences.put("xslDirectory", file.getAbsolutePath());
            xslPathTextField.setText(file.getAbsolutePath());
        }
    }

    public void chooseTargetDirectoryAction(ActionEvent event) {
        Stage stage = (Stage) chooseSourceDirectoryButton.getScene().getWindow();
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choose Target Directory for CSV Files");
        File file = directoryChooser.showDialog(stage);
        if (null != file) {
            targetFilePath = file;
            targetPathTextField.setText(file.getAbsolutePath());
        }
    }

    public void convertFilesAction(ActionEvent event) throws IOException {
        Service<Void> myService = new Service<Void>() {

            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {

                    @Override
                    protected void scheduled() {
                        super.scheduled();
                        log("Start converting files!");
                    }

                    @Override
                    protected void running() {
                        super.running();
                    }

                    @Override
                    protected void succeeded() {
                        super.succeeded();
                        log("DONE!");
                    }

                    @Override
                    protected void cancelled() {
                        super.cancelled();
                        log("CANCELLED");
                    }

                    @Override
                    protected void failed() {
                        super.failed();
                        log("FAILED");
                    }

                    @Override
                    protected Void call() throws Exception {
                        DirectoryFluxkompensator directoryFluxkompensator = new DirectoryFluxkompensator();
                        directoryFluxkompensator.addLoggerCallback(GuiController.this);
                        try {
                            directoryFluxkompensator.process(sourceFilePath.toPath(), xslFilePath.toPath(), targetFilePath.toPath());
                            return null;
                        } catch (Exception e) {
                            for (StackTraceElement elt : e.getStackTrace()) {
                                String exc = String.format("at %s.%s(%s:%s)", elt.getClassName(), elt.getMethodName(), elt.getFileName(), elt.getLineNumber());
                                log(exc);
                            }
                            throw e;
                        }
                    }

                };
            }

        };
        myService.start();
    }

}

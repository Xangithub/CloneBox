package ru.clonebox.client.preferences;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import ru.clonebox.client.DataProvider;

public class PrefController {
    @FXML
    private TextField folderSync;
    @FXML
    private CheckBox autoSync;
    @FXML
    private TextField ip;
    @FXML
    private TextField port;

    @FXML
    private void initialize() {
        ip.setText(DataProvider.preferences.getRemoteIP());
        port.setText(String.valueOf(DataProvider.preferences.getRemotePORT()));
        autoSync.setSelected(DataProvider.preferences.autoSync);
    }


    public void saveAndApplySettings(ActionEvent actionEvent) {
        DataProvider.preferences.setRemoteIP(ip.getText());
        DataProvider.preferences.setRemotePORT(Integer.parseInt(port.getText()));
//        DataProvider.preferences.setSavepass(false);
        DataProvider.preferences.writeProperties();
        hendleCancelPropertiesWindow(actionEvent);
    }

    public void hendleCancelPropertiesWindow(ActionEvent actionEvent) {
        DataProvider.primaryStage.setScene(DataProvider.sceneAuthWindow);
        DataProvider.primaryStage.show();

    }
    //todo через регексы чекать корректность полей
//    checkIP();
//    checkPort(Integer port);

}

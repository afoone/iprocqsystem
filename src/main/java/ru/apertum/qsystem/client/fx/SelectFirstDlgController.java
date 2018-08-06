/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.apertum.qsystem.client.fx;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;
import ru.apertum.qsystem.common.NetCommander;
import ru.apertum.qsystem.common.QConfig;
import ru.apertum.qsystem.common.QLog;
import ru.apertum.qsystem.common.SoundPlayer;
import ru.apertum.qsystem.common.cmd.RpcGetSelfSituation;
import ru.apertum.qsystem.common.model.QCustomer;

/**
 * FXML Controller class
 *
 * @author Evgeniy Egorov
 */
public class SelectFirstDlgController extends UserController {

    @FXML()
    private AnchorPane clientPane;

    public AnchorPane getClientPane() {
        return clientPane;
    }

    @FXML()
    private Button inviteBtn;

    @FXML()
    private Button closeBtn;

    @FXML()
    private Button pickUpBtn;

    @FXML()
    private WebView info;

    private RpcGetSelfSituation.SelfService selfService;
    private RpcGetSelfSituation.CustomerShort custData;

    public RpcGetSelfSituation.SelfService getSelfService() {
        return selfService;
    }

    public RpcGetSelfSituation.CustomerShort getCustData() {
        return custData;
    }

    void init(RpcGetSelfSituation.SelfService selfService, RpcGetSelfSituation.CustomerShort custData) {
        this.selfService = selfService;
        this.custData = custData;

        File htmlFile = new File("config/desktop/" + selfService.getId() + ".html");
        if (htmlFile.exists()) {
            try {
                info.getEngine().load(htmlFile.toURI().toURL().toString());
            } catch (IOException ex) {
                QLog.l().logger().warn(ex);
            }
        } else {
            htmlFile = new File("config/desktop/service.html");
            if (htmlFile.exists()) {
                try {
                    info.getEngine().load(htmlFile.toURI().toURL().toString());
                } catch (IOException ex) {
                    QLog.l().logger().warn(ex);
                }
            } else {
                info.getEngine().load(this.getClass().getResource("/ru/apertum/qsystem/client/fx/service.html").toString());
            }
        }
    }

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        QLog.l().logger().info("SelectFirstDlg was initialized.");
    }

    @FXML()
    public void invite(ActionEvent event) {
        // Вызываем кастомера
        final QCustomer cust = NetCommander.inviteNextCustomer(getNetProperty(), getUser().getId(), null, custData.id);
        // проговорим голосом вызов с компа оператора если есть настроичка
        if (cust != null && QConfig.cfg().needVoice()) {
            final boolean isFirst = true;
            new Thread(()
                    -> SoundPlayer.inviteClient(cust.getService(), cust, cust.getPrefix() + (cust.getNumber() < 1 ? "" : cust.getNumber()), getUser().getPoint(), isFirst)
            ).start();
        }
        if (cust == null) {
            close(event);
        } else {
            close(event);
            inviteHandle(getStage().getOwner(), cust);
        }
    }

    @FXML()
    public void close(ActionEvent event) {
        closeWithEffect(event);
    }

    @FXML()
    public void pickUp(ActionEvent event) {
        NetCommander.customerToPostpone(getNetProperty(), getUser().getId(), custData.id, "Picked Up", 0, true, false);
        getDesktopController().refreshSituation();
        closeWithEffect(event);
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.apertum.qsystem.client.fx;

import java.io.File;
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
import ru.apertum.qsystem.common.model.QCustomer;

import static ru.apertum.qsystem.common.CustomerState.STATE_INVITED;
import static ru.apertum.qsystem.common.CustomerState.STATE_INVITED_SECONDARY;
import static ru.apertum.qsystem.common.CustomerState.STATE_WORK;
import static ru.apertum.qsystem.common.CustomerState.STATE_WORK_SECONDARY;

/**
 * FXML Controller class
 *
 * @author Evgeniy Egorov
 */
public class PostponeDlgController extends UserController {

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
    private Button finishBtn;

    @FXML()
    private WebView info;

    private QCustomer customer;

    public QCustomer getCustomer() {
        return customer;
    }

    public void setCustomer(QCustomer customer) {
        this.customer = customer;

        File htmlFile = new File("config/desktop/" + customer.getService().getId() + ".html");
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
        QLog.l().logger().info("PostponeDlg was initialized.");
    }

    @FXML()
    public void close(ActionEvent event) {
        closeWithEffect(event);
    }

    @FXML()
    public void finish(ActionEvent event) {
        // небольшой учет того, что вдруг окажется, что у этого оператора есть вызванный им кастомер классическим способом.
        // Если этот вызванный уже в работе, то просто его отложить(будет работать), иначе его в работу и потом отложить.
        if (customer.getState() == STATE_INVITED
                || customer.getState() == STATE_INVITED_SECONDARY) {
            // Его в работу и потом отложить.
            NetCommander.getStartCustomer(getNetProperty(), getUser().getId());
        }
        NetCommander.customerToPostpone(getNetProperty(), getUser().getId(), customer.getId(), "Finished", 0, false, false);
        getDesktopController().refreshSituation();
        closeWithEffect(event);
    }

    @FXML()
    public void invite(ActionEvent event) {
        // Вызываем кастомера
        final QCustomer cust;
        // небольшой учет того, что вдруг окажется, что у этого оператора есть вызванный им кастомер классическим способом.
        if (customer.getState() == STATE_INVITED
                || customer.getState() == STATE_INVITED_SECONDARY
                || customer.getState() == STATE_WORK
                || customer.getState() == STATE_WORK_SECONDARY) {
            if (customer.getState() == STATE_INVITED
                    || customer.getState() == STATE_INVITED_SECONDARY) {
                // раз уже есть вызванный, то можно его еще раз позвать.
                cust = NetCommander.inviteNextCustomer(getNetProperty(), getUser().getId());
            } else {
                // кастомер уже в работе у оператора. Его только "прикончить" можно.
                cust = customer;
            }
        } else {
            cust = NetCommander.invitePostponeCustomer(getNetProperty(), getUser().getId(), customer.getId());
        }

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
}

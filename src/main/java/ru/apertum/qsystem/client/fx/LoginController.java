/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.apertum.qsystem.client.fx;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import ru.apertum.qsystem.common.NetCommander;
import ru.apertum.qsystem.common.QLog;
import ru.apertum.qsystem.common.Uses;
import ru.apertum.qsystem.common.model.IClientNetProperty;
import ru.apertum.qsystem.server.model.QUser;

/**
 * FXML Controller class
 *
 * @author Evgeniy Egorov
 */
public class LoginController extends UserController {

    @FXML()
    private ComboBox<QUser> usersCbx;

    @FXML()
    private PasswordField inputPwd;

    @FXML()
    private Label errorLbl;

    private LinkedList<QUser> users;

    public LinkedList<QUser> getUsers() {
        return users;
    }

    public void setUsers(LinkedList<QUser> users) {
        this.users = users;
        usersCbx.setItems(FXCollections.observableArrayList(users.stream().filter(user -> !user.getPlanServices().isEmpty()).collect(Collectors.toList())));
        for (QUser user : usersCbx.getItems()) {
            if (lastLoginId.equals(user.getId())) {
                usersCbx.setValue(user);
                break;
            }
        }
    }

    public void init(IClientNetProperty netProperty, LinkedList<QUser> users) {
        setUsers(users);
        setNetProperty(netProperty);
    }

    private Long lastLoginId = Long.MIN_VALUE;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        final File f = new File(Uses.TEMP_FOLDER + "/lusr");
        if (f.exists()) {
            StringBuilder stringBuilder = new StringBuilder("");
            try (FileInputStream fis = new FileInputStream(f); Scanner s = new Scanner(new InputStreamReader(fis, "UTF-8"))) {
                while (s.hasNextLine()) {
                    final String line = s.nextLine().trim();
                    stringBuilder.append(line);
                }
            } catch (IOException ex) {
                QLog.l().logger().error("Ошибка чтения последнего успешного входа.", ex);
            }
            final String str = stringBuilder.toString();
            stringBuilder.setLength(0);
            if (!"".equals(str) && str.matches("-?\\d+(\\.\\d+)?")) {
                lastLoginId = Long.parseLong(str);
            }
        }

        errorLbl.setVisible(false);
        QLog.l().logger().info("Login was initialized.");
    }

    @FXML()
    public void cancel() {
        getStage().close();
    }

    @FXML()
    public void enter() {
        if (usersCbx.getValue() == null) {
            errorLbl.setVisible(true);
        } else {
            if (usersCbx.getValue().getPassword().equals(inputPwd.getText())) {
                // Посмотрим, не пытались ли влезть под уже имеющейся в системе ролью
                if (NetCommander.getSelfServicesCheck(getNetProperty(), usersCbx.getValue().getId())) {
                    errorLbl.setVisible(false);
                    setUser(usersCbx.getValue());

                    QLog.l().logger().info("Вход в систему выполнен. Пользователь \"" + getUser() + "\".");
                    final File f = new File("temp/lusr");

                    try {
                        if (!f.exists()) {
                            if (!Files.exists(Paths.get(Uses.TEMP_FOLDER))) {
                                Files.createDirectory(Paths.get(Uses.TEMP_FOLDER));
                            }
                            Files.createFile(Paths.get(Uses.TEMP_FOLDER + "/lusr"));
                        }

                        try (FileOutputStream fos = new FileOutputStream(f)) {
                            fos.write(getUser().getId().toString().getBytes());
                            fos.flush();
                        }

                    } catch (IOException ex) {
                        QLog.l().logger().error("Ошибка сохранения последнего успешного входа.", ex);
                    }

                    getStage().close();
                } else {
                    errorLbl.setVisible(true);
                }
            } else {
                errorLbl.setVisible(true);
            }
        }
    }
}

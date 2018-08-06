/*
 *  Copyright (C) 2010 {Apertum}Projects. web: www.apertum.ru email: info@apertum.ru
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ru.apertum.qsystem.client.model;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.Timer;

/**
 * Класс для работы с треем.
 * Класс для работы с треем.
 * Должен уметь помещать в трей иконку, определять к ней контекстное меню,
 * обрабатывать пункты этого меню, управлять эконкой.
 * Класс должен быть Singleton.
 * @author Evgeniy Egorov
 */
public final class QTray {

    /**
     * Для реализации Singleton
     */
    private static QTray _instance = null;
    /**
     * Это контекстное меню для эконки в трее.
     */
    private final PopupMenu popupMenu = new PopupMenu();
    /**
     * Это эконка в трее.
     */
    private TrayIcon trayIcon;

    public TrayIcon getTrayIcon() {
        return trayIcon;
    }
    /**
     * Системный трей.
     */
    private final SystemTray systemTray = SystemTray.getSystemTray();
    /**
     * Ресурс главной эконки.
     */
    private Image mainImage = null;
    /**
     * Ресурс эконки для мигания.
     */
    private Image blinkImage = null;

    /**
     * Конструктор.
     */
    private QTray() {
    }

    /**
     * Убрать эконку из трея, если она там есть.
     */
    @Override
    protected void finalize() {
        try {
            removeTrayIcon();
        } finally {
            try {
                super.finalize();
            } catch (Throwable ex) {
            }
        }
    }

    public static synchronized QTray getInstance(final JFrame frame, String resourceName, String hint) {
        if (_instance == null) {
            _instance = new QTray();
            _instance.setTray(frame, resourceName, hint);
        }
        return _instance;
    }

    /**
     * Добавление пункта в контекстное меню.
     * @param itemName Название пункта меню.
     * @param actionListener Событие при выборе этого пункта.
     */
    public synchronized void addItem(String itemName, ActionListener actionListener) {
        final MenuItem item = new MenuItem(itemName);
        item.addActionListener(actionListener);
        popupMenu.add(item);
    }

    /**
     * Вывод эконки в трей и определение событий для трея.
     * @param frame форма, с которой связана эконка
     * @param resourceName имя файла картинки в ресурсах, например /ru/forms/resources/busyicons/client.png
     * @param hint Хинт для эконки в трее.
     */
    private void setTray(final JFrame frame, String resourceName, String hint) {
        mainImage = new ImageIcon(getClass().getResource(resourceName)).getImage();
        trayIcon = new TrayIcon(mainImage, hint, popupMenu);

        // показ формы по двейному щелчку на форме
        trayIcon.addActionListener((ActionEvent e) -> {
            frame.setVisible(true);
            frame.setState(JFrame.NORMAL);
        });
        frame.addWindowStateListener((WindowEvent e) -> {
            if (e.getNewState() == JFrame.ICONIFIED) {
                frame.setVisible(false);
            }
        });
        try {
            systemTray.add(trayIcon);
        } catch (AWTException ex) {
            System.out.print("Ошибка работы с системным треем. " + ex);
        }
    }

    /**
     * Установить новую эконку в трей.
     * @param resourceName ресурс новой эконки.
     */
    public synchronized void setNewIcon(String resourceName) {
        mainImage = new ImageIcon(getClass().getResource(resourceName)).getImage();
        trayIcon.setImage(mainImage);
    }
    //***********************************************************************
    //*************** Работа с миганием *************************************
    private final int DELAY_BLINK = 500;
    /**
     * Таймер мигания.
     */
    private final Timer timer = new Timer(DELAY_BLINK, new TimerPrinter());

    /**
     * Собыите на таймер
     */
    private class TimerPrinter implements ActionListener {

        private boolean flag = true;

        /**
         * Обеспечение мигания.
         * @param e
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            if (flag) {
                trayIcon.setImage(mainImage);
            } else {
                trayIcon.setImage(blinkImage);
            }
            flag = !flag;
        }
    };

    /**
     * Стерт мигания эконки в трее.
     * @param resourceName Мигает меняясь на эту эконку.
     * @param period Мигает с периодом.
     */
    public synchronized void statrBlinkIcon(String resourceName, int period) {
        blinkImage = new ImageIcon(getClass().getResource(resourceName)).getImage();
        if (!timer.isRunning()) {
            timer.setDelay(period);
            timer.start();
        }
    }

    public synchronized void stopBlinkIcon() {
        timer.stop();
        if (mainImage != null) {
            trayIcon.setImage(mainImage);
        }
    }

    /**
     * Показать сообщение в системном трее
     * @param caption
     * @param message текст сообщения
     * @param type тип сообщения
     */
    public synchronized void showMessageTray(String caption, String message, MessageType type) {
        final TrayIcon.MessageType messageType;
        switch (type) {
            case ERROR: {
                messageType = TrayIcon.MessageType.ERROR;
                break;
            }
            case WARNING: {
                messageType = TrayIcon.MessageType.WARNING;
                break;
            }
            case INFO: {
                messageType = TrayIcon.MessageType.INFO;
                break;
            }
            default:
                messageType = TrayIcon.MessageType.NONE;
        }
        trayIcon.displayMessage(caption, message, messageType);
    }

    public enum MessageType {

        /** An error message */
        ERROR,
        /** A warning message */
        WARNING,
        /** An information message */
        INFO,
        /** Simple message */
        NONE
    };

    /**
     * Убрать иконку из трея.
     * При завершении приложения, этот класс должен освободиться, при этом иконка должна убраться автоматически.
     */
    public synchronized void removeTrayIcon() {
        // подотрем иконку из трея
        systemTray.remove(trayIcon);
    }
}

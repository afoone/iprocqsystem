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
package ru.apertum.qsystem.reports.model;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import ru.apertum.qsystem.common.QLog;
import ru.apertum.qsystem.common.exceptions.ReportException;
import ru.apertum.qsystem.reports.net.RunnableSocket;

/**
 * A reporting server acting as a Web server that processes requests for reports
 * A class of threads that handle HTTP requests for reporting
 *
 * @author Evgeniy Egorov
 */
public class WebServer {

    public static WebServer getInstance() {
        return WebServerHolder.INSTANCE;
    }

    private static class WebServerHolder {

        private static final WebServer INSTANCE = new WebServer();
    }

    private WebServer() {
    }

    /**
     * состояние вэбсервера
     */
    private volatile boolean isActive = false;
    /**
     * Поток вебсервера.
     */
    private Thread webTread = null;

    /**
     * launch web server
     *
     * @param port На каком порту
     */
    public synchronized void startWebServer(int port) {
        if (!isActive) {
            isActive = true;
        } else {
            return;
        }

        // screw the socket to localhost, port port
        QLog.l().logger().info("The reporting server captures the port \"" + port + "\".");

        // thread of the Web server, weighs in parallel and serves requests
        webTread = new Thread() {

            @Override
            public void run() {
                try (final ServerSocket reportSocket = new ServerSocket(port, 0)) {
                    reportSocket.setSoTimeout(30000);
                    System.out.println("Report server for QSystem started.");
                    QLog.l().logRep().info("Report server for QSystem started.");

                    while (isActive && !webTread.isInterrupted()) {
                        // wait for a new connection, and then start processing the client
                        // into the new computational flow and increase the counter by one
                        try {
                            final Socket socket = reportSocket.accept();
                            final RunnableSocket rs = new RunnableSocket();
                            rs.setSocket(socket);
                            final Thread thread = new Thread(rs);
                            thread.setDaemon(true);
                            thread.setPriority(Thread.NORM_PRIORITY);
                            thread.start();
                        } catch (SocketTimeoutException ex) {
                        } catch (IOException ex) {
                            throw new ReportException("Error while working with the WebServer: " + ex);
                        }
                    }
                } catch (Exception e) {
                    throw new ReportException("Error creating server socket for web server: " + e);
                }
                QLog.l().logRep().info("The reporting WebServer of the 'Queue' system is stopped.");

            }
        };
        // и запускаем новый вычислительный поток (см. ф-ю run())
        webTread.setDaemon(true);
        webTread.setPriority(Thread.NORM_PRIORITY);
        webTread.start();

    }

    /**
     * Stop Web Server
     */
    public synchronized void stopWebServer() {
        if (isActive) {
            isActive = false;
        } else {
            return;
        }
        if (webTread != null) {
            webTread.interrupt();
        }
        try {
            Thread.sleep(600);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}

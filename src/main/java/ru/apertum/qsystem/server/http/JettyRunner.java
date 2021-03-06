/*
 * Copyright (C) 2010 {Apertum}Projects. web: www.apertum.ru email: info@apertum.ru
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ru.apertum.qsystem.server.http;

import java.io.File;
/*
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.commons.lang.ArrayUtils;
 import org.eclipse.jetty.servlet.ServletHolder;
 */
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.webapp.WebAppContext;
import static ru.apertum.qsystem.common.QLog.log;
import ru.apertum.qsystem.common.exceptions.ServerException;

/**
 * Класс старта и останова сервера Jetty. При старте создается новый поток и в нем стартует Jetty
 *
 * @author Evgeniy Egorov
 */
public class JettyRunner implements Runnable {

    /**
     * Страт Jetty
     *
     * @param port порт на котором стартует сервер
     */
    synchronized public static void start(int port) {
        servetPort = port;
        if (jetthread != null && jetthread.isInterrupted() == false) {
            try {
                if (jetty.isRunning()) {
                    jetty.stop();
                }
            } catch (Exception ex) {
                log().error("Ошибка остановки сервера Jetty.", ex);
            }
            jetthread.interrupt();
        }
        jetthread = new Thread(new JettyRunner());
        jetthread.setDaemon(true);
        jetthread.start();
    }

    /**
     * Остановить сервер Jetty
     */
    public static void stop() {
        if (jetthread != null && jetthread.isInterrupted() == false) {
            try {
                if (jetty.isRunning()) {
                    jetty.stop();
                }
            } catch (Exception ex) {
                throw new ServerException("Error stopping Jetty server.", ex);
            }
            jetthread.interrupt();
        }
        log().info("Jetty Server successfully stopped.");
    }
    private static volatile Server jetty = null;
    private static int servetPort = 8081;
    private static Thread jetthread = null;

    @Override
    public void run() {
        log().info("Start the Jetty server on the port " + servetPort);
        jetty = new Server();
        
        //org.eclipse.jetty.io.nio.AsyncConnection d;
        HttpConfiguration http_config = new HttpConfiguration();
        http_config.setSecureScheme("https");
        http_config.setSecurePort(8443);
        http_config.setOutputBufferSize(32768);
        http_config.setRequestHeaderSize(8192);
        http_config.setResponseHeaderSize(8192);
        http_config.setSendServerVersion(true);
        http_config.setSendDateHeader(false);
        ServerConnector http_connector = new ServerConnector(jetty, new HttpConnectionFactory(http_config));
        http_connector.setIdleTimeout(30000);
        http_connector.setPort(servetPort);
        jetty.addConnector(http_connector);

        final ResourceHandler resource_handler = new ResourceHandler();
        resource_handler.setDirectoriesListed(true);
        resource_handler.setWelcomeFiles(new String[]{"index.html"});
        resource_handler.setResourceBase("www");

        /*
         // WebSocket: Регистрируем ChatWebSocketHandler в сервере Jetty. такой метож сдох в jttty9
         QWebSocketHandler qWebSocketHandler = new QWebSocketHandler();
         // Это вариант хэндлера для WebSocketHandlerContainer
         qWebSocketHandler.setHandler(new DefaultHandler());
         * 
         */
        final ServletContextHandler servletContext = new ServletContextHandler(ServletContextHandler.SESSIONS);
        servletContext.setContextPath("/");
        //При необходимости иметь сервлет, добавляяем их в обработчики вот так
        //servletContext.addServlet(new ServletHolder(new HelloServlet()), "/hell");

        /*
         // поддержка расширяемости плагинами. На будующее, пото если понадобится приделаю сервлеты как плагины
         for (final IChangeCustomerStateEvent event : ServiceLoader.load(IChangeCustomerStateEvent.class)) {
         log().info("Вызов SPI расширения. Описание: " + event.getDescription());
         try {
         event.change(this, state, newServiceId);
         } catch (Throwable tr) {
         log().error("Вызов SPI расширения завершился ошибкой. Описание: " + tr);
         }
         }
         */
        final HandlerList handlers = new HandlerList();

        // An important point - the order of the handlers
        // this order will override the request, if it has not yet been processed
        // ie. From the beginning the file is searched, if it is not found, then the URL is passed to the execution of the command,
        // in komaidah considered that the URL for a webcam needs to be passed on, it will be caught by the web site's handle
        //handlers.setHandlers(new Handler[]{resource_handler, new CommandHandler(), qWebSocketHandler});
        handlers.setHandlers(new Handler[]{resource_handler, new CommandHandler(), servletContext});

        // Загрузка war из папки 
        String folder = "./www/war/";
        log().info("Loading war from a folder " + folder);
        final File[] list = new File(folder).listFiles((File dir, String name) -> name.toLowerCase().endsWith(".war"));
        if (list != null && list.length != 0) {
            for (File file : list) {
                final String name = file.getName().substring(0, file.getName().lastIndexOf('.')).toLowerCase();
                log().debug("WAR " + name + ": " + file.getAbsolutePath());
                final WebAppContext webapp = new WebAppContext();
                webapp.setContextPath("/" + name);
                webapp.setWar(file.getAbsolutePath());
                handlers.addHandler(webapp);
            }
        }

        jetty.setHandler(handlers);
        

        /*
         String jetty_home = "";
         SslContextFactory sslContextFactory = new SslContextFactory();
         sslContextFactory.setKeyStorePath(jetty_home + "/etc/keystore");
         sslContextFactory.setKeyStorePassword("OBF:1vny1zlo1x8e1vnw1vn61x8g1zlu1vn4");
         sslContextFactory.setKeyManagerPassword("OBF:1u2u1wml1z7s1z7a1wnl1u2g");
         sslContextFactory.setTrustStorePath(jetty_home + "/etc/keystore");
         sslContextFactory.setTrustStorePassword("OBF:1vny1zlo1x8e1vnw1vn61x8g1zlu1vn4");
         sslContextFactory.setExcludeCipherSuites("SSL_RSA_WITH_DES_CBC_SHA", "SSL_DHE_RSA_WITH_DES_CBC_SHA", "SSL_DHE_DSS_WITH_DES_CBC_SHA", "SSL_RSA_EXPORT_WITH_RC4_40_MD5", "SSL_RSA_EXPORT_WITH_DES40_CBC_SHA", "SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA", "SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA");
         HttpConfiguration https_config = new HttpConfiguration(http_config);
         https_config.addCustomizer(new SecureRequestCustomizer());
         ServerConnector sslConnector = new ServerConnector(jetty, new SslConnectionFactory(sslContextFactory, "http/1.1"), new HttpConnectionFactory(https_config));
         sslConnector.setPort(8443);
         jetty.addConnector(sslConnector);
         * 
         */
        try {
            jetty.start();
        } catch (Exception ex) {
            throw new ServerException("Ошибка запуска сервера Jetty. ", ex);
        }
        log().info("Join Jetty server on port " + servetPort);
        try {
            jetty.join();
        } catch (InterruptedException ex) {
            log().warn("Jetty stopped working");
        }
        log().info("The Jetty server is stopped.");
    }

    /*
     public static class HelloServlet extends HttpServlet {

     private static final long serialVersionUID = -6154475799000019575L;

     private static final String greeting = "Hello World";

     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
     System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
     response.setContentType("text/html");
     response.setStatus(HttpServletResponse.SC_OK);
     response.getWriter().println(greeting);
     }

     @Override
     protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
     System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
     resp.setContentType("text/html");
     resp.setStatus(HttpServletResponse.SC_OK);
     resp.getWriter().println(greeting);
     }
     }
     */
}

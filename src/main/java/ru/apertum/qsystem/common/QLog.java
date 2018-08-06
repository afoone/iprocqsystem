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
package ru.apertum.qsystem.common;

import java.awt.Font;
import java.util.Enumeration;

import org.apache.commons.lang3.SystemUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import ru.apertum.qsystem.About;
import ru.apertum.qsystem.server.ServerProps;

/**
 * Собственно, логер лог4Ж Это синглтон. Тут в место getInstance() для короткого написания используется l()
 *
 * @author Evgeniy Egorov
 */
public class QLog {

    private Logger logger = Logger.getLogger("server.file");//**.file.info.trace

    public Logger logger() {
        return logger;
    }

    public static Logger log() {
        return l().logger();
    }

    /**
     * Пользуемся этой константой для работы с логом для отчетов
     */
    private Logger logRep = Logger.getLogger("reports.file");
    private Logger logProp = Logger.getLogger("properties.file");
    private Logger logQUser = QConfig.cfg().getModule().isServer() ? Logger.getLogger("quser.file") : Logger.getLogger("reports.file.info.trace");

    public Logger logRep() {
        return logRep;
    }

    public static Logger lRep() {
        return l().logRep();
    }

    private Logger lProp() {
        return logProp;
    }

    public static Logger logProp() {
        return l().lProp();
    }

    public Logger logQUser() {
        return logQUser;
    }

    public static Logger lQUser() {
        return l().logQUser();
    }

    private QLog() {
        //бежим по параметрам, смотрим, выполняем что надо
        // ключ, отвечающий за логирование
        if (QConfig.cfg().isDebug()) {
            switch (loggerType) {
                case server://сервер
                    logger = Logger.getLogger("server.file.info.trace");
                    break;
                case client://клиент
                    logger = Logger.getLogger("client.file.info.trace");
                    break;
                case desktop://клиент
                    logger = Logger.getLogger("desktop.file.info.trace");
                    break;
                case reception://приемная
                    logger = Logger.getLogger("reception.file.info.trace");
                    break;
                case admin://админка
                    logger = Logger.getLogger("admin.file.info.trace");
                    break;
                case welcome://киоск
                    logger = Logger.getLogger("welcome.file.info.trace");
                    break;
                case hardware_buttons://хардварные кнопки
                    logger = Logger.getLogger("user_buttons.file.info.trace");
                    break;
                default:
                    logger = Logger.getLogger("client.file.info.trace");
            }
        } else {
            // ключ, отвечающий за логирование
            if (QConfig.cfg().isLogInfo()) {
                switch (loggerType) {
                    case server://сервер
                        logger = Logger.getLogger("server.file.info");
                        break;
                    case client://клиент
                        logger = Logger.getLogger("client.file.info");
                        break;
                    case desktop://клиент
                        logger = Logger.getLogger("desktop.file.info");
                        break;
                    case reception://приемная
                        logger = Logger.getLogger("reception.file.info");
                        break;
                    case admin://админка
                        logger = Logger.getLogger("admin.file.info");
                        break;
                    case welcome://киоск
                        logger = Logger.getLogger("welcome.file.info");
                        break;
                    case hardware_buttons://хардварные кнопки
                        logger = Logger.getLogger("user_buttons.file.info");
                        break;
                    default:
                        logger = Logger.getLogger("client.file.info");
                }
            } else {
                switch (loggerType) {
                    case server://сервер
                        logger = Logger.getLogger("server.file");
                        break;
                    case client://клиент
                        logger = Logger.getLogger("client.file");
                        break;
                    case desktop://клиент
                        logger = Logger.getLogger("desktop.file");
                        break;
                    case reception://приемная
                        logger = Logger.getLogger("reception.file");
                        break;
                    case admin://админка
                        logger = Logger.getLogger("admin.file");
                        break;
                    case welcome://киоск
                        logger = Logger.getLogger("welcome.file");
                        break;
                    case hardware_buttons://хардварные кнопки
                        logger = Logger.getLogger("user_buttons.file");
                        break;
                    default:
                        logger = Logger.getLogger("client.file");
                }
            }
        }
        if (!QConfig.cfg().isIDE() && SystemUtils.IS_OS_WINDOWS) { // Операционка и бинс
            final Enumeration<Logger> lgs = logger.getLoggerRepository().getCurrentLoggers();
            while (lgs.hasMoreElements()) {
                final Logger lg = lgs.nextElement();
                final Enumeration<Appender> aps = lg.getAllAppenders();
                while (aps.hasMoreElements()) {
                    final Appender ap = aps.nextElement();
                    if (ap instanceof ConsoleAppender) {
                        ((ConsoleAppender) ap).setEncoding("cp866");
                        ((ConsoleAppender) ap).activateOptions();
                    }
                }
            }
        }

        // ключ, отвечающий за паузу на старте. 
        if (QConfig.cfg().getDelay() > 0) {
            try {
                Thread.sleep(QConfig.cfg().getDelay() * 1000);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }

        if ("server.file.info.trace".equalsIgnoreCase(logger.getName())) {
            logRep = Logger.getLogger("reports.file.info.trace");
            logQUser = Logger.getLogger("quser.file.info.trace");
        } else {
            // ключ, отвечающий за логирование
            if ("server.file.info".equalsIgnoreCase(logger.getName())) {
                logRep = Logger.getLogger("reports.file.info");
                logQUser = Logger.getLogger("quser.file.info");
            }
        }
    }

    public static QLog l() {
        return LogerHolder.INSTANCE;
    }

    public static QModule loggerType = QModule.server; // 0-сервер,1-клиент,2-приемная,3-админка,4-киоск

    /**
     * @param args
     * @param type 0-сервер,1-клиент,2-приемная,3-админка,4-киоск,5-сервер хардварных кнопок
     * @return
     */
    public static QLog initial(String[] args, QModule type) {
        loggerType = type;
        QConfig.cfg().prepareCLI(type, args);

        if (QConfig.cfg().getFont().isEmpty()) {
            switch (type) {
                case client:
                    Uses.setUIFont(Font.decode("Tahoma-Plain-16"));
                    break;
                case desktop:
                    Uses.setUIFont(Font.decode("Tahoma-Plain-20"));
                    break;
                case reception:
                    Uses.setUIFont(Font.decode("Tahoma-Plain-14"));
                    break;
                default:
                    Uses.setUIFont(Font.decode("Tahoma-Plain-12"));
            }
        } else {
            if (!"no".equalsIgnoreCase(QConfig.cfg().getFont())) {
                Uses.setUIFont(Font.decode(QConfig.cfg().getFont()));
            }
        }

        final QLog log = LogerHolder.INSTANCE;
        About.load();
        QLog.l().logger.info("\"QSystem " + About.ver + "\"!  date: " + About.date);
        QLog.l().logger.info("START LOGER. Logger: " + QLog.l().logger().getName());
        if (QConfig.cfg().getModule().isServer()) {
            QLog.l().logger.info("Version DB=" + ServerProps.getInstance().getProps().getVersion());
            QLog.l().logRep.info("START LOGGER for reports. Logger: " + QLog.l().logRep().getName());
        }
        QLog.l().logger.info("Mode: " + (QConfig.cfg().isDebug() ? "KEY_DEBUG" : (QConfig.cfg().isDemo() ? "KEY_DEMO" : "FULL")));
        QLog.l().logger.info("Plugins: " + (QConfig.cfg().isNoPlugins() ? "NO" : "YES"));
        if (QConfig.cfg().isUbtnStart()) {
            QLog.l().logger.info("Auto start: YES");
        }

        return log;
    }

    private static class LogerHolder {

        private static final QLog INSTANCE = new QLog();
    }
}

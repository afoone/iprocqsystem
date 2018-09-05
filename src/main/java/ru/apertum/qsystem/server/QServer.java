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
package ru.apertum.qsystem.server;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Scanner;
import java.util.ServiceLoader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ru.apertum.qsystem.About;
import ru.apertum.qsystem.client.Locales;
import ru.apertum.qsystem.client.QProperties;
import ru.apertum.qsystem.client.forms.FAbout;
import ru.apertum.qsystem.common.*;

import static ru.apertum.qsystem.common.QLog.log;

import ru.apertum.qsystem.common.cmd.JsonRPC20;
import ru.apertum.qsystem.common.exceptions.ServerException;
import ru.apertum.qsystem.common.model.ATalkingClock;
import ru.apertum.qsystem.common.model.QCustomer;
import ru.apertum.qsystem.extra.IStartServer;
import ru.apertum.qsystem.hibernate.AnnotationSessionFactoryBean;
import ru.apertum.qsystem.reports.model.QReportsList;
import ru.apertum.qsystem.reports.model.WebServer;
import ru.apertum.qsystem.server.controller.Executer;
import ru.apertum.qsystem.server.http.JettyRunner;
import ru.apertum.qsystem.server.model.QProperty;
import ru.apertum.qsystem.server.model.QService;
import ru.apertum.qsystem.server.model.QServiceTree;
import ru.apertum.qsystem.server.model.QUser;
import ru.apertum.qsystem.server.model.QUserList;
import ru.apertum.qsystem.server.model.postponed.QPostponedList;

/**
 * Класс старта и exit инициализации сервера. Организация потоков выполнения заданий.
 *
 * @author Evgeniy Egorov
 */
public class QServer implements Runnable {

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool((Runnable r) -> {
        final Thread thread = new Thread(r);
        thread.setName("QSystem-thread");
        thread.setDaemon(true);
        thread.setPriority(Thread.NORM_PRIORITY);
        return thread;
    });

    private final Socket socket;

    /**
     * @param args - первым параметром передается полное имя настроечного XML-файла
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        About.printdef();
        QLog.initial(args, QModule.server);
        Locale.setDefault(Locales.getInstance().getLangCurrent());

        //Установка вывода консольных сообщений в нужной кодировке
        if ("\\".equals(File.separator)) {
            try {
                String consoleEnc = System.getProperty("console.encoding", "Cp866");
                System.setOut(new CodepagePrintStream(System.out, consoleEnc));
                System.setErr(new CodepagePrintStream(System.err, consoleEnc));
            } catch (UnsupportedEncodingException e) {
                System.out.println("Unable to setup console codepage: " + e);
            }
        }

        System.out.println("iProcuratio Consultores QSystem.");
        System.out.println("Welcome to the QSystem server. Your MySQL must be prepared.");
        FAbout.loadVersionSt();

        if (Locales.getInstance().isRuss()) {

            System.out.println("Bienvenido al servidor QSystem. Se requiere MySQL5.6 o superior para ejecutar.");
            System.out.println("Версия сервера: " + FAbout.VERSION_ + "_" + FAbout.BUILD_ + "-community QSystem Server (GPL)");
            System.out.println("Версия базы данных: " + FAbout.VERSION_DB_ + " for MySQL 5.6-community Server (GPL)");
            System.out.println("Дата выпуска : " + FAbout.DATE_);
            System.out.println("Copyright (c) 2018, Apertum Projects. Все права защищены.");
            System.out.println("QSystem является свободным программным обеспечением, вы можете");
            System.out.println("распространять и/или изменять его согласно условиям Стандартной Общественной");
            System.out.println("Лицензии GNU (GNU GPL), опубликованной Фондом свободного программного");
            System.out.println("обеспечения (FSF), либо Лицензии версии 3, либо более поздней версии.");

            System.out.println("Вы должны были получить копию Стандартной Общественной Лицензии GNU вместе");
            System.out.println("с этой программой. Если это не так, напишите в Фонд Свободного ПО ");
            System.out.println("(Free Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA)");

            System.out.println("Набирите 'exit' чтобы штатно остановить работу сервера.");
            System.out.println();
        } else {
            System.out.println("Server version: " + FAbout.VERSION_ + "_" + FAbout.BUILD_ + "-community QSystem Server (GPL)");
            System.out.println("Database version: " + FAbout.VERSION_DB_ + " for MySQL 5.6-community Server (GPL)");
            System.out.println("Released : " + FAbout.DATE_);

            System.out.println("Apertum Projects and/or its affiliates. For this version: iProcuratio consultores  ");
            System.out.println("This software comes with ABSOLUTELY NO WARRANTY. This is free software,");
            System.out.println("and you are welcome to modify and redistribute it under the GPL v3 license");
            System.out.println("Text of this license on your language located in the folder with the program.");

            System.out.println("Type 'exit' to stop work and close server.");
            System.out.println();
        }

        final long start = System.currentTimeMillis();

        // Загрузка плагинов из папки plugins
        if (!QConfig.cfg().isNoPlugins()) {
            Uses.loadPlugins("./plugins/");
        }

        // посмотрим не нужно ли стартануть jetty
        // для этого нужно запускать с ключом http
        // если етсь ключ http, то запускаем сервер и принимаем на нем команды серверу суо
        if (QConfig.cfg().getHttp() > 0) {
            log().info("Run Jetty.");
            try {
                JettyRunner.start(QConfig.cfg().getHttp());
            } catch (NumberFormatException ex) {
                log().error("El número de puerto Jetty en los parámetros de inicio no es un número. Formato de parámetro para el puerto 8081 '-http 8081'.", ex);
            }
        }

        // Отчетный сервер, выступающий в роли вэбсервера, обрабатывающего запросы на выдачу отчетов
        WebServer.getInstance().startWebServer(ServerProps.getInstance().getProps().getWebServerPort());
        loadPool();
        // запускаем движок индикации сообщения для кастомеров
        MainBoard.getInstance().showBoard();
        if (!(Uses.FORMAT_HH_MM.format(ServerProps.getInstance().getProps().getStartTime()).equals(Uses.FORMAT_HH_MM.format(ServerProps.getInstance().getProps().getFinishTime())))) {
            /**
             * Temporizador, mediante el cual borraremos todos los servicios y enviaremos correo no deseado con un informe diario.
             */
            ATalkingClock clearServices = new ATalkingClock(Uses.DELAY_CHECK_TO_LOCK, 0) {

                @Override
                public void run() {
                    final String HH_MM = Uses.FORMAT_HH_MM.format(new Date());
                    // это обнуление
                    if (!QConfig.cfg().isRetain() && Uses.FORMAT_HH_MM.format(new Date(new Date().getTime() + 10 * 60 * 1000)).equals(Uses.FORMAT_HH_MM.format(ServerProps.getInstance().getProps().getStartTime()))) {
                        log().info("Borrar todos los servicios.");
                        // почистим все услуги от трупов кастомеров с прошлого дня
                        QServer.clearAllQueue();
                    }

                    // это рассылка дневного отчета
                    final String p1 = ServerProps.getInstance().getProperty(QProperties.SECTION_REPORTS, "day_report_enable", "0");
                    final String p2 = Mailer.fetchConfig().getProperty("mailing");
                    final boolean doReport = "1".equals(p1) || "true".equalsIgnoreCase(p1) || "true".equalsIgnoreCase(p2) || "1".equals(p2);
                    if (doReport) {
                        final QProperty prop = ServerProps.getInstance().getProperty(QProperties.SECTION_REPORTS, "day_report_time_HH:mm");
                        final boolean onTime = prop == null
                                ? Uses.FORMAT_HH_MM.format(new Date(new Date().getTime() - 30 * 60 * 1000)).equals(Uses.FORMAT_HH_MM.format(ServerProps.getInstance().getProps().getFinishTime()))
                                : HH_MM.equals(prop.getValue());
                        if (onTime) {
                            final String report = ServerProps.getInstance().getProperty(QProperties.SECTION_REPORTS, "day_report_link", "distribution_job_day.pdf");
                            log().info("Day report. Distribución diaria de informes. " + report);
                            // почистим все услуги от трупов кастомеров с прошлого дня
                            for (QUser user : QUserList.getInstance().getItems()) {
                                if (user.getReportAccess()) {
                                    final HashMap<String, String> p = new HashMap<>();
                                    p.put("date", Uses.FORMAT_DD_MM_YYYY.format(new Date()));
                                    final byte[] result = QReportsList.getInstance().generate(user, "/" + report, p);
                                    try {
                                        try (FileOutputStream fos = new FileOutputStream("temp/" + report)) {
                                            fos.write(result);
                                            fos.flush();
                                        }
                                        Mailer.sendReporterMailAtFon(null, null, null, "temp/" + report);
                                    } catch (Exception ex) {
                                        log().error("Какой-то облом с дневным отчетом", ex);
                                    }
                                    break;
                                }
                            }
                        }
                    }

                    // это скачивание и раззиповка архива раз в день.
                    QProperty prop = ServerProps.getInstance().getProperty(QProperties.SECTION_UPDATER, "download_time_HH:mm");
                    boolean onTime = prop == null
                            ? false
                            : HH_MM.equals(prop.getValue());
                    if (onTime) {
                        Updater.load().download(true);
                    }
                    prop = ServerProps.getInstance().getProperty(QProperties.SECTION_UPDATER, "unzip_time_HH:mm");
                    onTime = prop == null
                            ? false
                            : HH_MM.equals(prop.getValue());
                    if (onTime) {
                        Updater.load().unzip();
                    }

                }
            };
            clearServices.start();
        }

        // подключения плагинов, которые стартуют в самом начале.
        // поддержка расширяемости плагинами
        for (final IStartServer event : ServiceLoader.load(IStartServer.class)) {
            log().info("Llame a la extensión SPI. Descripción: " + event.getDescription());
            try {
                new Thread(() -> event.start()).start();
            } catch (Exception tr) {
                log().error("Вызов SPI расширения завершился ошибкой. Описание: " + tr);
            }
        }

        // привинтить сокет на локалхост, порт 3128
        try (final ServerSocket server = new ServerSocket(ServerProps.getInstance().getProps().getServerPort())) {
            log().info("El servidor del sistema captura el puerto \"" + ServerProps.getInstance().getProps().getServerPort() + "\".");
            server.setSoTimeout(500);
            final AnnotationSessionFactoryBean as = (AnnotationSessionFactoryBean) Spring.getInstance().getFactory().getBean("conf");
            System.out.println("Server QSystem started.\n");
            log().info("\n" +
                    "El servidor del sistema 'Queue' se está ejecutando. DB name='" + as.getName() + "' url=" + as.getUrl());
            int pos = 0;
            boolean exit = false;
            // слушаем порт
            while (!globalExit && !exit) {
                // espere una nueva conexión y luego comience a procesar el cliente
                // en el nuevo flujo computacional y aumentar el contador en uno

                try {
                    final QServer qServer = new QServer(server.accept());
                    EXECUTOR_SERVICE.execute(qServer);
                    if (QConfig.cfg().isDebug()) {
                        System.out.println();
                    }
                } catch (SocketTimeoutException e) {
                    // ничего страшного, гасим исключение стобы дать возможность отработать входному/выходному потоку
                } catch (Exception e) {
                    throw new ServerException("Network error: " + e);
                }

                if (!QConfig.cfg().isDebug()) {
                    final char ch = '*';
                    String progres = "Process: " + ch;
                    final int len = 5;
                    for (int i = 0; i < pos; i++) {
                        progres = progres + ch;
                    }
                    for (int i = 0; i < len; i++) {
                        progres = progres + ' ';
                    }
                    if (++pos == len) {
                        pos = 0;
                    }
                    System.out.print(progres);
                    System.out.write(13);// '\b' - возвращает корретку на одну позицию назад

                }

                // Попробуем считать нажатую клавишу
                // если нажади ENTER, то завершаем работу сервера
                // и затираем файл временного состояния Uses.TEMP_STATE_FILE
                //BufferedReader r = new BufferedReader(new StreamReader(System.in));
                int bytesAvailable = System.in.available();
                if (bytesAvailable > 0) {
                    byte[] data = new byte[bytesAvailable];
                    System.in.read(data);
                    if (bytesAvailable == 5
                            && data[0] == 101
                            && data[1] == 120
                            && data[2] == 105
                            && data[3] == 116
                            && ((data[4] == 10) || (data[4] == 13))) {
                        // набрали команду "exit" и нажали ENTER
                        log().info("Apagando el servidor.");
                        exit = true;
                    }
                }
            }// while

            log().debug("Cierre de socket del servidor.");
        } catch (IOException e) {
            throw new ServerException("Network error. Creating net socket is not possible: " + e);
        } catch (Exception e) {
            throw new ServerException("Network error: " + e);
        }
        log().debug("Detener Jetty.");
        JettyRunner.stop();
        log().debug("Detener el servidor web de informes.");
        WebServer.getInstance().stopWebServer();
        log().debug("Apagar la pantalla central.");
        MainBoard.getInstance().close();

        deleteTempFile();
        Thread.sleep(1500);
        log().info("El servidor ha completado el trabajo correctamente. Tiempo de trabajo: " + Uses.roundAs(((double) (System.currentTimeMillis() - start)) / 1000 / 60, 2) + " мин.");
        System.exit(0);
    }

    private static volatile boolean globalExit = false;

    /**
     * @param socket
     */
    public QServer(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            log().debug(" Start thread for receiving task. host=" + socket.getInetAddress().getHostAddress() + " ip=" + Arrays.toString(socket.getInetAddress().getAddress()));

            // из сокета клиента берём поток входящих данных
            InputStream is;
            try {
                is = socket.getInputStream();
            } catch (IOException e) {
                throw new ServerException("Input Stream broken: " + Arrays.toString(e.getStackTrace()));
            }

            final String data;
            try {
                // подождать пока хоть что-то приползет из сети, но не более 10 сек.
                int i = 0;
                while (is.available() == 0 && i < 100) {
                    Thread.sleep(100);//бля
                    i++;
                }

                StringBuilder sb = new StringBuilder(new String(Uses.readInputStream(is)));
                while (is.available() != 0) {
                    sb = sb.append(new String(Uses.readInputStream(is)));
                    Thread.sleep(150);//бля
                }
                data = URLDecoder.decode(sb.toString(), "utf-8");
            } catch (IOException ex) {
                throw new ServerException("Error al leer la secuencia de entrada: " + ex);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new ServerException("Dificultad para dormir: " + ex);
            } catch (IllegalArgumentException ex) {
                throw new ServerException("\n" +
                        "Error al decodificar mensaje de red: " + ex);
            }
            log().trace("Task:\n" + (data.length() > 200 ? (data.substring(0, 200) + "...") : data));

            /*

                    Si la red atrapó la salida, significa que iniciaron el stop batik.
             */
            if ("exit".equalsIgnoreCase(data)) {
                globalExit = true;
                return;
            }

            final String answer;
            final JsonRPC20 rpc;
            final Gson gson = GsonPool.getInstance().borrowGson();
            try {
                rpc = gson.fromJson(data, JsonRPC20.class);
                // полученное задание передаем в пул
                final Object result = Executer.getInstance().doTask(rpc, socket.getInetAddress().getHostAddress(), socket.getInetAddress().getAddress());
                answer = gson.toJson(result);
            } catch (JsonSyntaxException ex) {
                log().error("Received data \"" + data + "\" has not correct JSOM format. ", ex);
                throw new ServerException("Received data \"" + data + "\" has not correct JSOM format. " + Arrays.toString(ex.getStackTrace()));
            } catch (Exception ex) {
                log().error("Late caught the error when running the command. ", ex);
                throw new ServerException("Поздно пойманная ошибка при выполнении команды: " + Arrays.toString(ex.getStackTrace()));
            } finally {
                GsonPool.getInstance().returnGson(gson);
            }

            // выводим данные:
            log().trace("Response:\n" + (answer.length() > 200 ? (answer.substring(0, 200) + "...") : answer));
            try {
                // Передача данных ответа
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                writer.print(URLEncoder.encode(answer, "utf-8"));
                writer.flush();
            } catch (IOException e) {
                throw new ServerException("Ошибка при записи в поток: " + Arrays.toString(e.getStackTrace()));
            }
        } catch (ServerException | JsonParseException ex) {
            final StringBuilder sb = new StringBuilder("\nStackTrace:\n");
            for (StackTraceElement bag : ex.getStackTrace()) {
                sb.append("    at ").append(bag.getClassName()).append(".").append(bag.getMethodName()).append("(").append(bag.getFileName()).append(":").append(bag.getLineNumber()).append(")\n");
            }
            final String err = sb.toString() + "\n";
            sb.setLength(0);
            throw new ServerException("Ошибка при выполнении задания.\n" + ex + err);
        } finally {
            // завершаем соединение
            try {
                //оборачиваем close, т.к. он сам может сгенерировать ошибку IOExeption. Просто выкинем Стек-трейс
                socket.close();
            } catch (IOException e) {
                log().trace(e);
            }
            log().trace("Response was finished");
        }
    }

    /**
     * Сохранение состояния пула услуг в xml-файл на диск
     */
    public static synchronized void savePool() {
        final long start = System.currentTimeMillis();
        log().info("Save the state.");
        final LinkedList<QCustomer> backup = new LinkedList<>();// создаем список сохраняемых кастомеров
        final LinkedList<QCustomer> parallelBackup = new LinkedList<>();// создаем список сохраняемых Parallel кастомеров
        final LinkedList<Long> pauses = new LinkedList<>();// создаем список юзеров у которых менопауза
        QServiceTree.getInstance().getNodes().stream().forEach(service -> backup.addAll(service.getClients()));

        QUserList.getInstance().getItems().forEach(user -> {
            if (user.getCustomer() != null) {
                backup.add(user.getCustomer());
            }
            parallelBackup.addAll(user.getParallelCustomers().values());
            if (user.isPause()) {
                pauses.add(user.getId());
            }
        });
        // в темповый файл
        new File(Uses.TEMP_FOLDER).mkdir();
        final Gson gson = GsonPool.getInstance().borrowGson();
        try (final FileOutputStream fos = new FileOutputStream(new File(Uses.TEMP_FOLDER + File.separator + Uses.TEMP_STATE_FILE))) {
            fos.write(gson.toJson(new TempList(backup, parallelBackup, QPostponedList.getInstance().getPostponedCustomers(), pauses)).getBytes("UTF-8"));
            fos.flush();
        } catch (FileNotFoundException ex) {
            throw new ServerException("Не возможно создать временный файл состояния. " + ex.getMessage());
        } catch (IOException ex) {
            throw new ServerException("Не возможно сохранить изменения в поток." + ex.getMessage());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        log().info("El estado está guardado en tiempo: " + ((double) (System.currentTimeMillis() - start)) / 1000 + " сек.");
    }

    public static class TempList {

        public TempList() {
        }

        public TempList(LinkedList<QCustomer> backup, LinkedList<QCustomer> parallelBackup, LinkedList<QCustomer> postponed) {
            this.backup = backup;
            this.parallelBackup = parallelBackup;
            this.postponed = postponed;
        }

        public TempList(LinkedList<QCustomer> backup, LinkedList<QCustomer> parallelBackup, LinkedList<QCustomer> postponed, LinkedList<Long> pauses) {
            this.backup = backup;
            this.parallelBackup = parallelBackup;
            this.postponed = postponed;
            this.pauses = pauses;
        }

        @Expose
        @SerializedName("backup")
        public LinkedList<QCustomer> backup;
        @Expose
        @SerializedName("parallelBackup")
        public LinkedList<QCustomer> parallelBackup;
        @Expose
        @SerializedName("postponed")
        public LinkedList<QCustomer> postponed;
        @Expose
        @SerializedName("method")
        public String method = null;
        @Expose
        @SerializedName("pauses")
        public LinkedList<Long> pauses = null;
        @Expose
        @SerializedName("date")
        public Long date = new Date().getTime();
    }

    /**
     * Cargando el estado de la agrupación de servicios desde un archivo json temporal
     */
    public static void loadPool() {
        final long start = System.currentTimeMillis();
        // Si hay un archivo temporal que guarda el estado, entonces debe cargarlo.
        // todos los errores de lectura y análisis son ignorados.
        log().info("Tratamos de restaurar el estado del sistema.");
        File recovFile = new File(Uses.TEMP_FOLDER + File.separator + Uses.TEMP_STATE_FILE);
        if (recovFile.exists()) {
            log().warn(Locales.locMes("came_back"));
            //восстанавливаем состояние

            final StringBuilder recData = new StringBuilder();
            try (final FileInputStream fis = new FileInputStream(recovFile)) {
                final Scanner scan = new Scanner(fis, "utf8");
                while (scan.hasNextLine()) {
                    recData.append(scan.nextLine());
                }
            } catch (IOException ex) {
                throw new ServerException(ex);
            }

            final TempList recList;
            final Gson gson = GsonPool.getInstance().borrowGson();
            try {
                recList = gson.fromJson(recData.toString(), TempList.class);
            } catch (JsonSyntaxException ex) {
                throw new ServerException("No es posible interpretar los datos almacenados.\n" + ex.toString());
            } finally {
                GsonPool.getInstance().returnGson(gson);
                recData.setLength(0);
            }

            // Проверим не просрочился ли кеш. Время просточки 3 часа.
            if (!QConfig.cfg().isRetain() && (recList.date == null || new Date().getTime() - recList.date > 3 * 60 * 60 * 1000)) {
                // Просрочился кеш, не грузим
                log().warn("\n" +
                        "El estatuto de limitaciones sobre el almacenamiento del estado ha expirado. Si no ocurre nada en el sistema durante 3 horas, se considera que los datos almacenados se han vuelto obsoletos irrevocablemente.");
            } else {
                // Свежий, загружаем в сервер данные кеша

                try {
                    QPostponedList.getInstance().loadPostponedList(recList.postponed);
                    for (QCustomer recCustomer : recList.backup) {
                        // в эту очередь он был
                        final QService service = QServiceTree.getInstance().getById(recCustomer.getService().getId());
                        if (service == null) {
                            log().warn("Intentando agregar el cliente \"" + recCustomer.getPrefix() + recCustomer.getNumber() + "\" al servicio \"" + recCustomer.getService().getName() + "\" не успешна. Услуга не обнаружена!");
                            continue;
                        }
                        service.setCountPerDay(recCustomer.getService().getCountPerDay());
                        service.setDay(recCustomer.getService().getDay());
                        // por lo que el nombre de usuario de su procesamiento
                        final QUser user = recCustomer.getUser();
                        // кастомер ща стоит к этой услуге к какой стоит
                        recCustomer.setService(service);
                        // смотрим к чему привязан кастомер. либо в очереди стоит, либо у юзера обрабатыватся
                        if (user == null) {
                            // сохраненный кастомер стоял в очереди и ждал, но его еще никто не звал
                            QServiceTree.getInstance().getById(recCustomer.getService().getId()).addCustomerForRecoveryOnly(recCustomer);
                            log().debug("Cliente agregado \"" + recCustomer.getPrefix() + recCustomer.getNumber() + "\" к услуге \"" + recCustomer.getService().getName() + "\"");
                        } else {
                            // El usuario guardado fue manejado por userId
                            if (QUserList.getInstance().getById(user.getId()) == null) {
                                log().warn("Intentando agregar el cliente \"" + recCustomer.getPrefix() + recCustomer.getNumber() + "\" al usuario \"" + user.getName() + "\" no exitoso ¡Usuario no encontrado!");
                                continue;
                            }
                            QUserList.getInstance().getById(user.getId()).setCustomer(recCustomer);
                            recCustomer.setUser(QUserList.getInstance().getById(user.getId()));
                            log().debug("Cliente agregado \"" + recCustomer.getPrefix() + recCustomer.getNumber() + "\" para el usuario \"" + user.getName() + "\"");
                        }
                    }
                    // Параллельные кастомеры, загрузим
                    for (QCustomer recCustomer : recList.parallelBackup) {
                        // в эту очередь он был
                        final QService service = QServiceTree.getInstance().getById(recCustomer.getService().getId());
                        if (service == null) {
                            log().warn("Попытка добавить клиента \"" + recCustomer.getPrefix() + recCustomer.getNumber() + "\" к услуге \"" + recCustomer.getService().getName() + "\" не успешна. Услуга не обнаружена!");
                            continue;
                        }
                        service.setCountPerDay(recCustomer.getService().getCountPerDay());
                        service.setDay(recCustomer.getService().getDay());
                        // так зовут юзера его обрабатываюшего
                        final QUser user = recCustomer.getUser();
                        // кастомер ща стоит к этой услуге к какой стоит
                        recCustomer.setService(service);
                        // смотрим к чему привязан кастомер. либо в очереди стоит, либо у юзера обрабатыватся
                        if (user == null) {
                            log().warn("Для параллельного клиента \"" + recCustomer.getPrefix() + recCustomer.getNumber() + "\" добавление к юзеру не успешна. Юзер потерялся!");
                        } else {
                            // сохраненный кастомер обрабатывался юзером с именем userId
                            if (QUserList.getInstance().getById(user.getId()) == null) {
                                log().warn("Попытка добавить параллельного клиента \"" + recCustomer.getPrefix() + recCustomer.getNumber() + "\" к юзеру \"" + user.getName() + "\" не успешна. Юзер не обнаружен!");
                                continue;
                            }
                            QUserList.getInstance().getById(user.getId()).setCustomer(recCustomer);
                            recCustomer.setUser(QUserList.getInstance().getById(user.getId()));
                            log().debug("Cliente agregado \"" + recCustomer.getPrefix() + recCustomer.getNumber() + "\" para el usuario \"" + user.getName() + "\"");
                        }
                    }
                    recList.pauses.stream().map((idUser) -> QUserList.getInstance().getById(idUser)).filter((user) -> (user != null)).forEachOrdered((user) -> {
                        user.setPause(Boolean.TRUE);
                    });
                } catch (ServerException ex) {
                    System.err.println("Востановление состояния сервера после изменения конфигурации. " + ex);
                    clearAllQueue();
                    log().error("Востановление состояния сервера после изменения конфигурации. Для выключения сервера используйте команду exit. ", ex);
                }
            }
        }
        log().info("Restauración completa del sistema en tiempo: " + ((double) (System.currentTimeMillis() - start)) / 1000 + " сек.");
    }

    static public void clearAllQueue() {
        // почистим все услуги от трупов кастомеров
        QServiceTree.getInstance().getNodes().forEach((service) -> {
            service.clearNextNumber();
            service.freeCustomers();
        });
        QService.clearNextStNumber();

        QPostponedList.getInstance().clear();
        MainBoard.getInstance().clear();

        // Сотрем временные файлы
        deleteTempFile();
        log().info("Borrar a todos los usuarios de personalizadores enlazados.");
        QUserList.getInstance().getItems().forEach((user) -> {
            user.setCustomer(null);
            user.getParallelCustomers().clear();
            user.setShadow(null);
            user.getPlanServices().forEach((plan) -> {
                plan.setAvg_wait(0);
                plan.setAvg_work(0);
                plan.setKilled(0);
                plan.setWorked(0);
            });
        });
    }

    public static void deleteTempFile() {
        log().debug("Remove " + Uses.TEMP_FOLDER + File.separator + Uses.TEMP_STATE_FILE);
        File file = new File(Uses.TEMP_FOLDER + File.separator + Uses.TEMP_STATE_FILE);
        if (file.exists()) {
            file.delete();
        }
        log().debug("Remove " + Uses.TEMP_FOLDER + File.separator + Uses.TEMP_STATATISTIC_FILE);
        file = new File(Uses.TEMP_FOLDER + File.separator + Uses.TEMP_STATATISTIC_FILE);
        if (file.exists()) {
            file.delete();
        }
    }
}

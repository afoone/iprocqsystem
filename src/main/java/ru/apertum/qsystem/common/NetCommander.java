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

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import javax.swing.tree.TreeNode;

import static org.apache.http.HttpHeaders.USER_AGENT;
import static ru.apertum.qsystem.client.Locales.locMes;
import static ru.apertum.qsystem.common.QLog.log;

import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.eclipse.jetty.http.HttpHeader;
import ru.apertum.qsystem.client.Locales;
import ru.apertum.qsystem.client.QProperties;
import ru.apertum.qsystem.common.cmd.AJsonRPC20;
import ru.apertum.qsystem.common.cmd.CmdParams;
import ru.apertum.qsystem.common.cmd.JsonRPC20;
import ru.apertum.qsystem.common.cmd.JsonRPC20Error;
import ru.apertum.qsystem.common.cmd.JsonRPC20OK;
import ru.apertum.qsystem.common.cmd.RpcBanList;
import ru.apertum.qsystem.common.cmd.RpcGetAdvanceCustomer;
import ru.apertum.qsystem.common.cmd.RpcGetAllServices;
import ru.apertum.qsystem.common.cmd.RpcGetAuthorizCustomer;
import ru.apertum.qsystem.common.cmd.RpcGetBool;
import ru.apertum.qsystem.common.cmd.RpcGetGridOfDay;
import ru.apertum.qsystem.common.cmd.RpcGetGridOfWeek;
import ru.apertum.qsystem.common.cmd.RpcGetInfoTree;
import ru.apertum.qsystem.common.cmd.RpcGetInt;
import ru.apertum.qsystem.common.cmd.RpcGetPostponedPoolInfo;
import ru.apertum.qsystem.common.cmd.RpcGetProperties;
import ru.apertum.qsystem.common.cmd.RpcGetRespTree;
import ru.apertum.qsystem.common.cmd.RpcGetResultsList;
import ru.apertum.qsystem.common.cmd.RpcGetSelfSituation;
import ru.apertum.qsystem.common.cmd.RpcGetServerState;
import ru.apertum.qsystem.common.cmd.RpcGetServerState.ServiceInfo;
import ru.apertum.qsystem.common.cmd.RpcGetServiceState;
import ru.apertum.qsystem.common.cmd.RpcGetServiceState.ServiceState;
import ru.apertum.qsystem.common.cmd.RpcGetSrt;
import ru.apertum.qsystem.common.cmd.RpcGetUsersList;
import ru.apertum.qsystem.common.cmd.RpcGetStandards;
import ru.apertum.qsystem.common.cmd.RpcGetTicketHistory;
import ru.apertum.qsystem.common.cmd.RpcGetTicketHistory.TicketHistory;
import ru.apertum.qsystem.common.cmd.RpcInviteCustomer;
import ru.apertum.qsystem.common.cmd.RpcStandInService;
import ru.apertum.qsystem.common.exceptions.ClientException;
import ru.apertum.qsystem.common.exceptions.QException;
import ru.apertum.qsystem.common.exceptions.ServerException;
import ru.apertum.qsystem.common.model.INetProperty;
import ru.apertum.qsystem.common.model.QCustomer;
import ru.apertum.qsystem.server.ServerProps;
import ru.apertum.qsystem.server.http.CommandHandler;
import ru.apertum.qsystem.server.model.QAdvanceCustomer;
import ru.apertum.qsystem.server.model.QAuthorizationCustomer;
import ru.apertum.qsystem.server.model.QProperty;
import ru.apertum.qsystem.server.model.QService;
import ru.apertum.qsystem.server.model.QServiceTree;
import ru.apertum.qsystem.server.model.QStandards;
import ru.apertum.qsystem.server.model.QUser;
import ru.apertum.qsystem.server.model.infosystem.QInfoItem;
import ru.apertum.qsystem.server.model.response.QRespItem;
import ru.apertum.qsystem.server.model.results.QResult;

/**
 * Содержит статические методы отправки и получения заданий на сервер. любой метод возвращает XML-узел ответа сервера.
 *
 * @author Evgeniy Egorov
 */
public class NetCommander {

    private static final JsonRPC20 JSON_RPC = new JsonRPC20();

    /**
     * основная работа по отсылки и получению результата.
     *
     * @param netProperty параметры соединения с сервером
     * @param commandName
     * @param params
     * @return XML-ответ
     * @throws ru.apertum.qsystem.common.exceptions.QException
     */
    public static synchronized String send(INetProperty netProperty, String commandName, CmdParams params) throws QException {
        JSON_RPC.setMethod(commandName);
        JSON_RPC.setParams(params);
        return sendRpc(netProperty, JSON_RPC);
    }

    private static Proxy getProxy(Proxy.Type proxyType) {
        if (QConfig.cfg().getProxy() != null) {
            log().trace("Proxy.Type." + proxyType + ": " + QConfig.cfg().getProxy().host + ":" + QConfig.cfg().getProxy().port);
            return new Proxy(proxyType, new InetSocketAddress(QConfig.cfg().getProxy().host, QConfig.cfg().getProxy().port));
        }
        if (QProperties.get().getProperty("proxy", "hostname") != null && QProperties.get().getProperty("proxy", "port") != null) {
            log().trace("Proxy.Type." + proxyType + ": " + QProperties.get().getProperty("proxy", "hostname").getValue() + ":" + QProperties.get().getProperty("proxy", "port").getValueAsInt());
            return new Proxy(proxyType, new InetSocketAddress(QProperties.get().getProperty("proxy", "hostname").getValue(),
                    QProperties.get().getProperty("proxy", "port").getValueAsInt()));
        } else {
            return null;
        }
    }

    public static synchronized String sendRpc(INetProperty netProperty, JsonRPC20 jsonRpc) throws QException {
        final String message;
        Gson gson = GsonPool.getInstance().borrowGson();
        try {
            message = gson.toJson(jsonRpc);
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        final String data;

        if (QConfig.cfg().getHttpRequestType().doUseHttp() && !(jsonRpc.getMethod().startsWith("#") || "empty".equalsIgnoreCase(jsonRpc.getMethod()))) {
            try {
                data = QConfig.cfg().getHttpRequestType().isPost() ? sendPost(netProperty, message, jsonRpc) : sendGet(netProperty, jsonRpc);
            } catch (Exception ex) {
                throw new QException(locMes("no_response_from_server"), ex);
            }
        } else {
            final Proxy proxy = getProxy(Proxy.Type.SOCKS);

            log().trace("Task \"" + jsonRpc.getMethod() + "\" on " + netProperty.getAddress().getHostAddress() + ":" + netProperty.getPort() + "#\n" + message);
            try (final Socket socket = proxy == null ? (QConfig.cfg().doNotUseProxy() ? new Socket(Proxy.NO_PROXY) : new Socket()) : new Socket(proxy)) {
                log().trace("Socket was created.");
                socket.connect(new InetSocketAddress(netProperty.getAddress(), netProperty.getPort()), 15000);

                final PrintWriter writer = new PrintWriter(socket.getOutputStream());
                writer.print(URLEncoder.encode(message, "utf-8"));
                log().trace("Sending...");
                writer.flush();
                log().trace("Reading...");
                final StringBuilder sb = new StringBuilder();
                final Scanner in = new Scanner(socket.getInputStream());
                while (in.hasNextLine()) {
                    sb.append(in.nextLine()).append("\n");
                }
                data = URLDecoder.decode(sb.toString(), "utf-8");
                sb.setLength(0);
                writer.close();
                in.close();
            } catch (IOException ex) {
                Uses.closeSplash();
                throw new QException(locMes("no_connect_to_server"), ex);
            } catch (Exception ex) {
                throw new QException(locMes("no_response_from_server"), ex);
            }
            log().trace("Response:\n" + data);
        }

        gson = GsonPool.getInstance().borrowGson();
        try {
            final JsonRPC20Error rpc = gson.fromJson(data, JsonRPC20Error.class);
            if (rpc == null) {
                throw new QException(locMes("error_on_server_no_get_response"));
            }
            if (rpc.getError() != null) {
                throw new QException(locMes("tack_failed") + " " + rpc.getError().getCode() + ":" + rpc.getError().getMessage());
            }
        } catch (JsonSyntaxException ex) {
            throw new QException(locMes("bad_response") + "\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return data;
    }

    // HTTP POST request
    private static String sendPost(INetProperty netProperty, String outputData, JsonRPC20 jsonRpc) throws Exception {
        String url = "http://" + netProperty.getAddress().getHostAddress() + ":" + QConfig.cfg().getHttpProtocol() + CommandHandler.CMD_URL_PATTERN;
        log().trace("HTTP POST request \"" + jsonRpc.getMethod() + "\" on " + url + "\n" + outputData);
        final URL obj = new URL(url);
        final Proxy proxy = getProxy(Proxy.Type.HTTP);

        final StringBuilder response;
        final HttpURLConnection con = (HttpURLConnection) (proxy == null ? (QConfig.cfg().doNotUseProxy() ? obj.openConnection(Proxy.NO_PROXY) : obj.openConnection()) : obj.openConnection(proxy));
        try {
            //add reuqest header
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", USER_AGENT);
            con.setRequestProperty(HttpHeader.CONTENT_TYPE.asString(), "text/json; charset=UTF-8");

            // Send post request
            con.setDoOutput(true);

            try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(con.getOutputStream(), "UTF8"))) {
                out.append(outputData);
                out.flush();
            }

            if (con.getResponseCode() != 200) {
                log().error("HTTP response code = " + con.getResponseCode());
                throw new QException(locMes("no_connect_to_server"));
            }

            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"))) {
                String inputLine;
                response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
            }
        } finally {
            con.disconnect();
        }

        //result
        final String res = response.toString();
        response.setLength(0);
        log().trace("HTTP response:\n" + res);
        return res;
    }

    // HTTP GET request
    private static String sendGet(INetProperty netProperty, JsonRPC20 jsonRpc) throws Exception {
        final String p = jsonRpc.getParams() == null ? "" : jsonRpc.getParams().toString();
        final String url = "http://" + netProperty.getAddress().getHostAddress() + ":" + QConfig.cfg().getHttpProtocol() + CommandHandler.CMD_URL_PATTERN + "?"
                + CmdParams.CMD + "=" + URLEncoder.encode(jsonRpc.getMethod(), "utf-8") + "&"
                + p;
        log().trace("HTTP GET request \"" + jsonRpc.getMethod() + "\" on " + url + "\n" + p);
        final URL obj = new URL(url);
        final Proxy proxy = getProxy(Proxy.Type.HTTP);

        final HttpURLConnection con = (HttpURLConnection) (proxy == null ? (QConfig.cfg().doNotUseProxy() ? obj.openConnection(Proxy.NO_PROXY) : obj.openConnection()) : obj.openConnection(proxy));
        final StringBuilder response;
        try {
            //add reuqest header
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", USER_AGENT);

            if (con.getResponseCode() != 200) {
                log().error("HTTP response code = " + con.getResponseCode());
                throw new QException(locMes("no_connect_to_server"));
            }
            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"))) {
                String inputLine;
                response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
            }
        } finally {
            con.disconnect();
        }

        //result
        final String res = response.toString();
        response.setLength(0);
        log().trace("HTTP response:\n" + res);
        return res;
    }

    /**
     * Выполнение универсальной команды.
     *
     * @param netProperty Сетевае параметры
     * @param cmdName     Имя команды. По нему регистрируется исполнитель на сервере.
     * @param params      Параметры команды.
     * @return Некий результат. Подумать, будет ли нормально маршалиться.
     */
    public static AJsonRPC20 runCmd(INetProperty netProperty, String cmdName, CmdParams params) {
        log().info("Выполнение универсальной команды.");
        // загрузим ответ
        final String res;
        try {
            res = send(netProperty, cmdName, params);
        } catch (QException ex) {// вывод исключений
            throw new ClientException(locMes("command_error"), ex);
        }
        if (res == null) {
            return null;
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final AJsonRPC20 rpc;
        try {
            rpc = gson.fromJson(res, AJsonRPC20.class);
        } catch (JsonSyntaxException ex) {
            throw new ClientException(locMes("bad_response") + "\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc;
    }

    /**
     * Obtains possible services
     *
     * @param netProperty server connection settings
     * @return XML-ответ
     */
    public static RpcGetAllServices.ServicesForWelcome getServices(INetProperty netProperty) {
        log().info("Obtaining possible services.");
        // загрузим ответ
        String res = null;
        try {
            res = send(netProperty, Uses.TASK_GET_SERVICES, null);
        } catch (QException ex) {// exception output
            throw new ClientException(locMes("command_error"), ex);
        }
        if (res == null) {
            return null;
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcGetAllServices rpc;
        try {
            rpc = gson.fromJson(res, RpcGetAllServices.class);
            QServiceTree.sailToStorm(rpc.getResult().getRoot(), (TreeNode service) -> {
                final QService perent = (QService) service;
                perent.getChildren().forEach(svr -> svr.setParent(perent));
            });
        } catch (JsonSyntaxException ex) {
            throw new ClientException(locMes("bad_response") + "\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Queuing.
     *
     * @param netProperty netProperty параметры соединения с сервером.
     * @param serviceId   услуга, в которую пытаемся встать.
     * @param password    пароль того кто пытается выполнить задание.
     * @param priority    приоритет.
     * @param inputData
     * @return Created customer.
     */
    public static QCustomer standInService(INetProperty netProperty, long serviceId, String password, int priority, String inputData) {
        log().info("Встать в очередь.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.serviceId = serviceId;
        params.parolcheg = password;
        params.priority = priority;
        params.textData = inputData;
        params.language = Locales.getInstance().getNameOfPresentLocale(); // передадим выбранный язык интерфейса
        String res = null;
        try {
            res = send(netProperty, Uses.TASK_STAND_IN, params);
        } catch (QException ex) {// вывод исключений
            throw new ClientException(locMes("command_error"), ex);
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcStandInService rpc;
        try {
            rpc = gson.fromJson(res, RpcStandInService.class);
        } catch (JsonSyntaxException ex) {
            throw new ClientException(locMes("bad_response") + "\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Постановка в очередь.
     *
     * @param netProperty netProperty параметры соединения с сервером.
     * @param servicesId  услуги, в которые пытаемся встать. Требует уточнения что это за трехмерный массив. Это пять списков. Первый это вольнопоследовательные
     *                    услуги. Остальные четыре это зависимопоследовательные услуги, т.е. пока один не закончится на другой не переходить. Что такое элемент списка. Это тоже
     *                    список. Первый элемент это та самая комплексная услуга(ID). А остальные это зависимости, т.е. если есть еще не оказанные услуги но назначенные, которые в
     *                    зависимостях, то их надо оказать.
     * @param password    пароль того кто пытается выполнить задание.
     * @param priority    приоритет.
     * @param inputData
     * @return Созданный кастомер.
     */
    public static QCustomer standInSetOfServices(INetProperty netProperty, LinkedList<LinkedList<LinkedList<Long>>> servicesId, String password, int priority, String inputData) {
        log().info("Встать в очередь комплексно.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.complexId = servicesId;
        params.parolcheg = password;
        params.priority = priority;
        params.textData = inputData;
        String res = null;
        try {
            res = send(netProperty, Uses.TASK_STAND_COMPLEX, params);
        } catch (QException ex) {// вывод исключений
            throw new ClientException(locMes("command_error"), ex);
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcStandInService rpc;
        try {
            rpc = gson.fromJson(res, RpcStandInService.class);
        } catch (JsonSyntaxException ex) {
            throw new ClientException(locMes("bad_response") + "\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Сделать услугу временно неактивной или разблокировать временную неактивность
     *
     * @param netProperty netProperty параметры соединения с сервером.
     * @param serviceId   услуга, которую пытаемся править
     * @param reason
     */
    public static void changeTempAvailableService(INetProperty netProperty, long serviceId, String reason) {
        log().info("Сделать услугу временно неактивной/активной.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.serviceId = serviceId;
        params.textData = reason;
        try {
            send(netProperty, Uses.TASK_CHANGE_TEMP_AVAILABLE_SERVICE, params);
        } catch (QException ex) {// вывод исключений
            throw new ClientException(locMes("command_error"), ex);
        }
    }

    /**
     * Узнать сколько народу стоит к услуге и т.д.
     *
     * @param netProperty параметры соединения с сервером.
     * @param serviceId   id услуги о которой получаем информацию
     * @return количество предшествующих.
     * @throws QException
     */
    public static ServiceState aboutService(INetProperty netProperty, long serviceId) throws QException {
        log().info("Find out how many people are in queue to the service.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.serviceId = serviceId;
        String res;
        try {
            res = send(netProperty, Uses.TASK_ABOUT_SERVICE, params);
        } catch (QException ex) {// вывод исключений
            throw new QException(locMes("command_error"), ex);
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcGetServiceState rpc;
        try {
            rpc = gson.fromJson(res, RpcGetServiceState.class);
        } catch (JsonSyntaxException ex) {
            throw new QException(locMes("bad_response") + "\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Получить всю очередь к услуге и т.д.
     *
     * @param netProperty параметры соединения с сервером.
     * @param serviceId   id услуги о которой получаем информацию
     * @return количество предшествующих.
     * @throws QException
     */
    public static ServiceState getServiceConsistency(INetProperty netProperty, long serviceId) throws QException {
        log().info("Встать в очередь.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.serviceId = serviceId;
        String res = null;
        try {
            res = send(netProperty, Uses.TASK_GET_SERVICE_CONSISANCY, params);
        } catch (QException ex) {// вывод исключений
            throw new QException(locMes("command_error"), ex);
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcGetServiceState rpc;
        try {
            rpc = gson.fromJson(res, RpcGetServiceState.class);
        } catch (JsonSyntaxException ex) {
            throw new QException(locMes("bad_response") + "\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Узнать можно ли вставать в услугу с такими введенными данными
     *
     * @param netProperty параметры соединения с сервером.
     * @param serviceId   id услуги о которой получаем информацию
     * @param inputData   введенная ботва
     * @return 1 - превышен, 0 - можно встать. 2 - забанен
     * @throws QException
     */
    public static int aboutServicePersonLimitOver(INetProperty netProperty, long serviceId, String inputData) throws QException {
        log().info("Узнать можно ли вставать в услугу с такими введенными данными.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.serviceId = serviceId;
        params.textData = inputData;
        String res = null;
        try {
            res = send(netProperty, Uses.TASK_ABOUT_SERVICE_PERSON_LIMIT, params);
        } catch (QException ex) {// вывод исключений
            throw new QException(locMes("command_error"), ex);
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcGetInt rpc;
        try {
            rpc = gson.fromJson(res, RpcGetInt.class);
        } catch (JsonSyntaxException ex) {
            throw new QException(locMes("bad_response") + "\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Получение описания всех юзеров для выбора себя.
     *
     * @param netProperty параметры соединения с сервером
     * @return XML-ответ все юзеры системы
     */
    public static LinkedList<QUser> getUsers(INetProperty netProperty) {
        log().info("Obtaining a description of all users for choosing them.");
        // загрузим ответ
        String res = null;
        try {
            res = send(netProperty, Uses.TASK_GET_USERS, null);
        } catch (QException e) {// вывод исключений
            Uses.closeSplash();
            throw new ClientException(locMes("command_error2"), e);
        } finally {
            if (res == null || res.isEmpty()) {
                System.exit(1);
            }
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcGetUsersList rpc;
        try {
            rpc = gson.fromJson(res, RpcGetUsersList.class);
        } catch (JsonSyntaxException ex) {
            throw new ClientException(locMes("bad_response") + "\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Receiving queue description for user
     *
     * @param netProperty параметры соединения с сервером
     * @param userId      id пользователя для которого идет опрос
     * @return список обрабатываемых услуг с количеством кастомеров в них стоящих и обрабатываемый кастомер если был
     * @throws ru.apertum.qsystem.common.exceptions.QException
     */
    public static RpcGetSelfSituation.SelfSituation getSelfServices(INetProperty netProperty, long userId) throws QException {
        return getSelfServices(netProperty, userId, null);
    }

    /**
     * Receiving queue description for user
     *
     * @param netProperty параметры соединения с сервером
     * @param userId      id пользователя для которого идет опрос
     * @param forced      получить ситуацию даже если она не обновлялась за последнее время
     * @return список обрабатываемых услуг с количеством кастомеров в них стоящих и обрабатываемый кастомер если был
     * @throws ru.apertum.qsystem.common.exceptions.QException
     */
    public static RpcGetSelfSituation.SelfSituation getSelfServices(INetProperty netProperty, long userId, Boolean forced) throws QException {
        log().info("Receiving queue description for user.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.userId = userId;
        params.textData = QConfig.cfg().getPointN();
        params.requestBack = forced;
        String res;
        try {
            res = send(netProperty, Uses.TASK_GET_SELF_SERVICES, params);
        } catch (QException e) {// вывод исключений
            Uses.closeSplash();
            throw new QException(locMes("command_error2"), e);
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcGetSelfSituation rpc;
        try {
            rpc = gson.fromJson(res, RpcGetSelfSituation.class);
        } catch (JsonSyntaxException ex) {
            throw new ClientException(locMes("bad_response") + "\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Проверка на то что такой юзер уже залогинен в систему
     *
     * @param netProperty параметры соединения с сервером
     * @param userId      id пользователя для которого идет опрос
     * @return false - запрешено, true - новый
     */
    public static boolean getSelfServicesCheck(INetProperty netProperty, long userId) {
        log().info("Receiving queue description for user.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.userId = userId;
        params.textData = QConfig.cfg().getPointN();
        final String res;
        try {
            res = send(netProperty, Uses.TASK_GET_SELF_SERVICES_CHECK, params);
        } catch (QException e) {// вывод исключений
            Uses.closeSplash();
            throw new ServerException(locMes("command_error2"), e);
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcGetBool rpc;
        try {
            rpc = gson.fromJson(res, RpcGetBool.class);
        } catch (JsonSyntaxException ex) {
            throw new ClientException(locMes("bad_response") + "\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Получение слeдующего юзера из очередей, обрабатываемых юзером.
     *
     * @param netProperty параметры соединения с сервером
     * @param userId
     * @return ответ-кастомер следующий по очереди
     */
    public static QCustomer inviteNextCustomer(INetProperty netProperty, long userId) {
        log().info("Получение следующего юзера из очередей, обрабатываемых юзером.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.userId = userId;
        final String res;
        try {
            res = send(netProperty, Uses.TASK_INVITE_NEXT_CUSTOMER, params);
        } catch (QException e) {// вывод исключений
            throw new ClientException(locMes("command_error2"), e);
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcInviteCustomer rpc;
        try {
            rpc = gson.fromJson(res, RpcInviteCustomer.class);
        } catch (JsonSyntaxException ex) {
            throw new ClientException(locMes("bad_response") + "\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Получение слeдующего юзера из одной конкретной очереди, обрабатываемой юзером.
     *
     * @param netProperty параметры соединения с сервером
     * @param userId
     * @param serviceId
     * @return ответ-кастомер следующий по очереди
     */
    public static QCustomer inviteNextCustomer(INetProperty netProperty, long userId, long serviceId) {
        log().info("Получение слeдующего юзера из одной конкретной очереди, обрабатываемой юзером.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.userId = userId;
        params.serviceId = serviceId;
        final String res;
        try {
            res = send(netProperty, Uses.TASK_INVITE_NEXT_CUSTOMER, params);
        } catch (QException e) {// вывод исключений
            throw new ClientException(locMes("command_error2"), e);
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcInviteCustomer rpc;
        try {
            rpc = gson.fromJson(res, RpcInviteCustomer.class);
        } catch (JsonSyntaxException ex) {
            throw new ClientException(locMes("bad_response") + "\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Получение, блядь, любого кастомера из очередей
     *
     * @param netProperty параметры соединения с сервером
     * @param userId
     * @param num
     * @return ответ-кастомер следующий по очереди
     */
    public static QCustomer inviteNextCustomer(INetProperty netProperty, long userId, String num) {
        return inviteNextCustomer(netProperty, userId, num, null);
    }

    /**
     * Получение, блядь, любого кастомера из очередей
     *
     * @param netProperty параметры соединения с сервером
     * @param userId
     * @param num         либо ID либо num, но ID в приоритете.
     * @param customerId  либо ID либо num, но ID в приоритете.
     * @return ответ-кастомер следующий по очереди
     */
    public static QCustomer inviteNextCustomer(INetProperty netProperty, long userId, String num, Long customerId) {
        QLog.l().logger().info("Получение, блядь, любого кастомера из очередей юзера из очередей, обрабатываемых юзером.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.userId = userId;
        params.textData = num;
        params.customerId = customerId;
        final String res;
        try {
            res = send(netProperty, Uses.TASK_INVITE_NEXT_CUSTOMER, params);
        } catch (QException e) {// вывод исключений
            throw new ClientException("Невозможно получить ответ от сервера. " + e.toString());
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcInviteCustomer rpc;
        try {
            rpc = gson.fromJson(res, RpcInviteCustomer.class);
        } catch (JsonSyntaxException ex) {
            throw new ClientException(Locales.locMes("bad_response") + "\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Удаление вызванного юзером кастомера.
     *
     * @param netProperty параметры соединения с сервером
     * @param userId
     * @param customerId  переключиться на этого при параллельном приеме, NULL если переключаться не надо
     */
    public static void killNextCustomer(INetProperty netProperty, long userId, Long customerId) {
        log().info("Удаление вызванного юзером кастомера.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.userId = userId;
        params.customerId = customerId;
        try {
            send(netProperty, Uses.TASK_KILL_NEXT_CUSTOMER, params);
        } catch (QException e) {// вывод исключений
            throw new ClientException(locMes("command_error2"), e);
        }
    }

    /**
     * Перемещение вызванного юзером кастомера в пул отложенных.
     *
     * @param netProperty     параметры соединения с сервером
     * @param userId
     * @param customerId      переключиться на этого при параллельном приеме, NULL если переключаться не надо
     * @param status          просто строка. берется из возможных состояний завершения работы
     * @param postponedPeriod Период отложенности в минутах. 0 - бессрочно;
     * @param isMine          отложенный пользователь будет видет только отложившему оператору
     */
    public static void customerToPostpone(INetProperty netProperty, long userId, Long customerId, String status, int postponedPeriod, boolean isMine) {
        customerToPostpone(netProperty, userId, customerId, status, postponedPeriod, isMine, null);
    }

    /**
     * Перемещение вызванного юзером кастомера в пул отложенных.
     *
     * @param netProperty     параметры соединения с сервером
     * @param userId
     * @param customerId      переключиться на этого при параллельном приеме, NULL если переключаться не надо
     * @param status          просто строка. берется из возможных состояний завершения работы
     * @param postponedPeriod Период отложенности в минутах. 0 - бессрочно;
     * @param isMine          отложенный пользователь будет видет только отложившему оператору
     * @param strict          false - Если потребуется искать кастомера не только своего вызванного, но и произвольного из всей толпы для отложения.
     */
    public static void customerToPostpone(INetProperty netProperty, long userId, Long customerId, String status, int postponedPeriod, boolean isMine, Boolean strict) {
        log().info("Перемещение вызванного юзером кастомера в пул отложенных.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.userId = userId;
        params.customerId = customerId;
        params.textData = status;
        params.postponedPeriod = postponedPeriod;
        params.isMine = isMine ? userId : null;
        params.strict = strict;
        try {
            send(netProperty, Uses.TASK_CUSTOMER_TO_POSTPON, params);
        } catch (QException e) {// вывод исключений
            throw new ClientException(locMes("command_error2"), e);
        }
    }

    /**
     * Изменение отложенному кастомеру статеса
     *
     * @param netProperty       параметры соединения с сервером
     * @param postponCustomerId меняем этому кастомеру
     * @param status            просто строка. берется из возможных состояний завершения работы
     */
    public static void postponeCustomerChangeStatus(INetProperty netProperty, long postponCustomerId, String status) {
        log().info("Перемещение вызванного юзером кастомера в пул отложенных.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.customerId = postponCustomerId;
        params.textData = status;
        try {
            send(netProperty, Uses.TASK_POSTPON_CHANGE_STATUS, params);
        } catch (QException e) {// вывод исключений
            throw new ClientException(locMes("command_error2"), e);
        }
    }

    /**
     * Начать работу с вызванным кастомером.
     *
     * @param netProperty параметры соединения с сервером
     * @param userId
     */
    public static void getStartCustomer(INetProperty netProperty, long userId) {
        log().info("Начать работу с вызванным кастомером.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.userId = userId;
        try {
            send(netProperty, Uses.TASK_START_CUSTOMER, params);
        } catch (QException e) {// вывод исключений
            throw new ClientException(locMes("command_error2"), e);
        }
    }

    /**
     * Закончить работу с вызванным кастомером.
     *
     * @param netProperty параметры соединения с сервером
     * @param userId
     * @param customerId  переключиться на этого при параллельном приеме, NULL если переключаться не надо
     * @param resultId
     * @param comments    это если закончили работать с редиректенным и его нужно вернуть
     * @return
     */
    public static QCustomer getFinishCustomer(INetProperty netProperty, long userId, Long customerId, Long resultId, String comments) {
        log().info("Закончить работу с вызванным кастомером.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.userId = userId;
        params.customerId = customerId;
        params.resultId = resultId;
        params.textData = comments;
        String res = null;
        try {
            res = send(netProperty, Uses.TASK_FINISH_CUSTOMER, params);
        } catch (QException e) {// вывод исключений
            throw new ClientException(locMes("command_error2"), e);
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcStandInService rpc;
        try {
            rpc = gson.fromJson(res, RpcStandInService.class);
        } catch (JsonSyntaxException ex) {
            throw new ClientException(locMes("bad_response") + "\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Переадресовать клиента в другую очередь.
     *
     * @param netProperty параметры соединения с сервером
     * @param userId
     * @param customerId  переключиться на этого при параллельном приеме, NULL если переключаться не надо
     * @param serviceId
     * @param requestBack
     * @param resultId
     * @param comments    комментарии при редиректе
     */
    public static void redirectCustomer(INetProperty netProperty, long userId, Long customerId, long serviceId, boolean requestBack, String comments, Long resultId) {
        log().info("Переадресовать клиента в другую очередь.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.userId = userId;
        params.customerId = customerId;
        params.serviceId = serviceId;
        params.requestBack = requestBack;
        params.resultId = resultId;
        params.textData = comments;
        try {
            send(netProperty, Uses.TASK_REDIRECT_CUSTOMER, params);
        } catch (QException e) {// вывод исключений
            throw new ClientException(locMes("command_error2"), e);
        }
    }

    /**
     * Получение описания состояния сервера.
     *
     * @param netProperty параметры соединения с сервером
     * @return XML-ответ
     */
    public static LinkedList<ServiceInfo> getServerState(INetProperty netProperty) {
        log().info("Getting a description of server status.");
        // загрузим ответ
        String res = null;
        try {
            res = send(netProperty, Uses.TASK_SERVER_STATE, null);
        } catch (QException ex) {// вывод исключений
            throw new ClientException(locMes("command_error"), ex);
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcGetServerState rpc;
        try {
            rpc = gson.fromJson(res, RpcGetServerState.class);
        } catch (JsonSyntaxException ex) {
            throw new ClientException(locMes("bad_response") + "\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Obtain a description of the status of the registration point.
     *
     * @param netProperty        параметры соединения с пунктом регистрации
     * @param message            что-то вроде названия команды для пункта регистрации
     * @param dropTicketsCounter сбросить счетчик выданных талонов или нет
     * @return a certain response from the registration point, sort of like a line for output
     */
    public static String getWelcomeState(INetProperty netProperty, String message, boolean dropTicketsCounter) {
        log().info("Obtaining status of welcome point.");
        // load answer
        String res = null;
        final CmdParams params = new CmdParams();
        params.dropTicketsCounter = dropTicketsCounter;
        try {
            res = send(netProperty, message, params);
        } catch (QException e) {
            throw new ClientException(locMes("bad_response") + "\n" + e.toString());
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcGetSrt rpc;
        try {
            rpc = gson.fromJson(res, RpcGetSrt.class);
        } catch (JsonSyntaxException ex) {
            throw new ClientException(locMes("bad_response") + "\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Добавить сервис в список обслуживаемых юзером использую параметры. Используется при добавлении на горячую.
     *
     * @param netProperty параметры соединения с пунктом регистрации
     * @param serviceId
     * @param userId
     * @param coeff
     * @return содержить строковое сообщение о результате.
     */
    public static String setServiseFire(INetProperty netProperty, long serviceId, long userId, int coeff) {
        log().info("Привязка услуги пользователю на горячую.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.userId = userId;
        params.serviceId = serviceId;
        params.coeff = coeff;
        final String res;
        try {
            res = send(netProperty, Uses.TASK_SET_SERVICE_FIRE, params);
        } catch (QException e) {
            throw new ClientException(locMes("bad_response") + "\n" + e.toString());
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcGetSrt rpc;
        try {
            rpc = gson.fromJson(res, RpcGetSrt.class);
        } catch (JsonSyntaxException ex) {
            throw new ClientException(locMes("bad_response") + "\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Удалить сервис из списока обслуживаемых юзером использую параметры. Используется при добавлении на горячую.
     *
     * @param netProperty параметры соединения с пунктом регистрации
     * @param serviceId
     * @param userId
     * @return содержить строковое сообщение о результате.
     */
    public static String deleteServiseFire(INetProperty netProperty, long serviceId, long userId) {
        log().info("Удаление услуги пользователю на горячую.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.userId = userId;
        params.serviceId = serviceId;
        final String res;
        try {
            res = send(netProperty, Uses.TASK_DELETE_SERVICE_FIRE, params);
        } catch (QException e) {
            throw new ClientException(locMes("bad_response") + "\n" + e.toString());
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcGetSrt rpc;
        try {
            rpc = gson.fromJson(res, RpcGetSrt.class);
        } catch (JsonSyntaxException ex) {
            throw new ClientException(locMes("bad_response") + "\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Получение конфигурации главного табло - ЖК или плазмы. Это XML-файл лежащий в папку приложения mainboard.xml
     *
     * @param netProperty параметры соединения с сервером
     * @return корень XML-файла mainboard.xml
     * @throws DocumentException принятый текст может не преобразоваться в XML
     */
    public static Element getBoardConfig(INetProperty netProperty) throws DocumentException {
        log().info("Получение конфигурации главного табло - ЖК или плазмы.");
        // загрузим ответ
        final String res;
        try {
            res = send(netProperty, Uses.TASK_GET_BOARD_CONFIG, null);
        } catch (QException e) {
            throw new ClientException(locMes("bad_response") + "\n" + e.toString());
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcGetSrt rpc;
        try {
            rpc = gson.fromJson(res, RpcGetSrt.class);
        } catch (JsonSyntaxException ex) {
            throw new ClientException(locMes("bad_response") + "\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return DocumentHelper.parseText(rpc.getResult()).getRootElement();
    }

    /**
     * Сохранение конфигурации главного табло - ЖК или плазмы. Это XML-файл лежащий в папку приложения mainboard.xml
     *
     * @param netProperty параметры соединения с сервером
     * @param boardConfig
     */
    public static void saveBoardConfig(INetProperty netProperty, Element boardConfig) {
        log().info("Сохранение конфигурации главного табло - ЖК или плазмы.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.textData = boardConfig.asXML();
        try {
            send(netProperty, Uses.TASK_SAVE_BOARD_CONFIG, params);
        } catch (QException e) {
            throw new ClientException(locMes("bad_response") + "\n" + e.toString());
        }
    }

    /**
     * Получение дневной таблици с данными для предварительной записи включающими информацию по занятым временам и свободным.
     *
     * @param netProperty      netProperty параметры соединения с сервером.
     * @param serviceId        услуга, в которую пытаемся встать.
     * @param date             день недели за который нужны данные.
     * @param advancedCustomer ID авторизованного кастомера
     * @return класс с параметрами и списком времен
     */
    public static RpcGetGridOfDay.GridDayAndParams getPreGridOfDay(INetProperty netProperty, long serviceId, Date date, long advancedCustomer) {
        log().info("Получить таблицу дня");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.serviceId = serviceId;
        params.date = date.getTime();
        params.customerId = advancedCustomer;
        final String res;
        try {
            res = send(netProperty, Uses.TASK_GET_GRID_OF_DAY, params);
        } catch (QException e) {// вывод исключений
            throw new ClientException(locMes("command_error2"), e);
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcGetGridOfDay rpc;
        try {
            rpc = gson.fromJson(res, RpcGetGridOfDay.class);
        } catch (JsonSyntaxException ex) {
            throw new ClientException(locMes("bad_response") + "\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Получение недельной таблици с данными для предварительной записи.
     *
     * @param netProperty      netProperty параметры соединения с сервером.
     * @param serviceId        услуга, в которую пытаемся встать.
     * @param date             первый день недели за которую нужны данные.
     * @param advancedCustomer ID авторизованного кастомера
     * @return класс с параметрами и списком времен
     */
    public static RpcGetGridOfWeek.GridAndParams getGridOfWeek(INetProperty netProperty, long serviceId, Date date, long advancedCustomer) {
        log().info("Получить таблицу");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.serviceId = serviceId;
        params.date = date.getTime();
        params.customerId = advancedCustomer;
        final String res;
        try {
            res = send(netProperty, Uses.TASK_GET_GRID_OF_WEEK, params);
        } catch (QException e) {// вывод исключений
            throw new ClientException(locMes("command_error2"), e);
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcGetGridOfWeek rpc;
        try {
            rpc = gson.fromJson(res, RpcGetGridOfWeek.class);
        } catch (JsonSyntaxException ex) {
            throw new ClientException(locMes("bad_response") + "\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Предварительная запись в очередь.
     *
     * @param netProperty      netProperty параметры соединения с сервером.
     * @param serviceId        услуга, в которую пытаемся встать.
     * @param date             Дата записи
     * @param advancedCustomer ID авторизованного кастомер. -1 если нет авторизации
     * @param inputData        введеные по требованию услуги данные клиентом, может быть null если не вводили
     * @param comments         комментарий по предварительно ставящемуся клиенту если ставят из админки или приемной
     * @return предварительный кастомер
     */
    public static QAdvanceCustomer standInServiceAdvance(INetProperty netProperty, long serviceId, Date date, long advancedCustomer, String inputData, String comments) {
        log().info("Записать предварительно в очередь.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.serviceId = serviceId;
        params.date = date.getTime();
        params.customerId = advancedCustomer;
        params.textData = inputData;
        params.comments = comments;
        final String res;
        try {
            res = send(netProperty, Uses.TASK_ADVANCE_STAND_IN, params);
        } catch (QException ex) {// вывод исключений
            throw new ClientException(locMes("command_error"), ex);
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcGetAdvanceCustomer rpc;
        try {
            rpc = gson.fromJson(res, RpcGetAdvanceCustomer.class);
        } catch (JsonSyntaxException ex) {
            throw new ClientException(locMes("bad_response") + "\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Предварительная запись в очередь.
     *
     * @param netProperty netProperty параметры соединения с сервером.
     * @param advanceID   идентификатор предварительно записанного.
     * @return XML-ответ.
     */
    public static RpcStandInService standAndCheckAdvance(INetProperty netProperty, Long advanceID) {
        log().info("Постановка предварительно записанных в очередь.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.customerId = advanceID;
        final String res;
        try {
            res = send(netProperty, Uses.TASK_ADVANCE_CHECK_AND_STAND, params);
        } catch (QException e) {// вывод исключений
            throw new ClientException(locMes("command_error2"), e);
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcStandInService rpc;
        try {
            rpc = gson.fromJson(res, RpcStandInService.class);
        } catch (JsonSyntaxException ex) {
            throw new ClientException(locMes("bad_response") + "\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc;
    }

    /**
     * Удаление предварительной записи в очередь.
     *
     * @param netProperty netProperty параметры соединения с сервером.
     * @param advanceID   идентификатор предварительно записанного.
     * @return XML-ответ.
     */
    public static JsonRPC20OK removeAdvancedCustomer(INetProperty netProperty, Long advanceID) {
        log().info("Удаление предварительно записанных в очередь.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.customerId = advanceID;
        final String res;
        try {
            res = send(netProperty, Uses.TASK_REMOVE_ADVANCE_CUSTOMER, params);
        } catch (QException e) {// вывод исключений
            throw new ClientException(locMes("command_error2"), e);
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final JsonRPC20OK rpc;
        try {
            rpc = gson.fromJson(res, JsonRPC20OK.class);
        } catch (JsonSyntaxException ex) {
            throw new ClientException(locMes("bad_response") + "\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc;
    }

    /**
     * Рестарт сервера.
     *
     * @param netProperty параметры соединения с сервером
     */
    public static void restartServer(INetProperty netProperty) {
        log().info("Команда на рестарт сервера.");
        try {
            send(netProperty, Uses.TASK_RESTART, null);
        } catch (QException e) {// вывод исключений
            throw new ClientException(locMes("command_error2"), e);
        }
    }

    /**
     * Получение Дерева отзывов
     *
     * @param netProperty параметры соединения с сервером
     * @return XML-ответ
     */
    public static QRespItem getResporseList(INetProperty netProperty) {
        log().info("Команда на получение дерева отзывов.");
        String res = null;
        try {
            // загрузим ответ
            res = send(netProperty, Uses.TASK_GET_RESPONSE_LIST, null);
        } catch (QException ex) {// вывод исключений
            throw new ClientException(locMes("command_error"), ex);
        }
        if (res == null) {
            return null;
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcGetRespTree rpc;
        try {
            rpc = gson.fromJson(res, RpcGetRespTree.class);
        } catch (JsonSyntaxException ex) {
            throw new ClientException(locMes("bad_response") + "\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Оставить отзыв.
     *
     * @param netProperty параметры соединения с сервером.
     * @param serviceID   услуга, может быть null
     * @param userID      оператор, может быть null
     * @param customerID
     * @param clientData  номер талона, не null
     * @param resp        выбранн отзыв
     */
    public static void setResponseAnswer(INetProperty netProperty, QRespItem resp, Long userID, Long serviceID, Long customerID, String clientData) {
        log().info("Отправка выбранного отзыва.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.responseId = resp.getId();
        params.serviceId = serviceID;
        params.userId = userID;
        params.customerId = customerID;
        params.textData = clientData;
        params.comments = resp.data;
        try {
            send(netProperty, Uses.TASK_SET_RESPONSE_ANSWER, params);
        } catch (QException ex) {// вывод исключений
            throw new ServerException(locMes("command_error"), ex);
        }
    }

    /**
     * Получение информационного дерева
     *
     * @param netProperty параметры соединения с сервером
     * @return XML-ответ
     */
    public static QInfoItem getInfoTree(INetProperty netProperty) {
        log().info("Команда на получение информационного дерева.");
        String res = null;
        try {
            // загрузим ответ
            res = send(netProperty, Uses.TASK_GET_INFO_TREE, null);
        } catch (QException ex) {// вывод исключений
            throw new ClientException(locMes("command_error"), ex);
        }
        if (res == null) {
            return null;
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcGetInfoTree rpc;
        try {
            rpc = gson.fromJson(res, RpcGetInfoTree.class);
        } catch (JsonSyntaxException ex) {
            throw new ClientException(locMes("bad_response") + "\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Получение описания залогинившегося юзера.
     *
     * @param netProperty параметры соединения с сервером
     * @param id
     * @return XML-ответ
     */
    public static QAuthorizationCustomer getClientAuthorization(INetProperty netProperty, String id) {
        log().info("Получение описания авторизованного пользователя.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.clientAuthId = id;
        final String res;
        try {
            res = send(netProperty, Uses.TASK_GET_CLIENT_AUTHORIZATION, params);
        } catch (QException ex) {// вывод исключений
            throw new ClientException(locMes("command_error"), ex);
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcGetAuthorizCustomer rpc;
        try {
            rpc = gson.fromJson(res, RpcGetAuthorizCustomer.class);
        } catch (JsonSyntaxException ex) {
            throw new ClientException(locMes("bad_response") + "\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Получение списка возможных результатов работы с клиентом
     *
     * @param netProperty параметры соединения с сервером
     * @return свисок возможных завершений работы
     */
    public static LinkedList<QResult> getResultsList(INetProperty netProperty) {
        log().info("Command to get a list of possible results of work with the client.");
        final String res;
        try {
            // загрузим ответ RpcGetResultsList
            res = send(netProperty, Uses.TASK_GET_RESULTS_LIST, null);
        } catch (QException ex) {// вывод исключений
            throw new ClientException(locMes("command_error"), ex);
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcGetResultsList rpc;
        try {
            rpc = gson.fromJson(res, RpcGetResultsList.class);
        } catch (JsonSyntaxException ex) {
            throw new ClientException(locMes("bad_response") + "\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Изменение приоритета кастомеру
     *
     * @param netProperty параметры соединения с сервером
     * @param prioritet
     * @param customer
     * @return Текстовый ответ о результате
     */
    public static String setCustomerPriority(INetProperty netProperty, int prioritet, String customer) {
        log().info("Команда на повышение приоритета кастомеру.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.priority = prioritet;
        params.clientAuthId = customer;
        final String res;
        try {
            res = send(netProperty, Uses.TASK_SET_CUSTOMER_PRIORITY, params);
        } catch (QException ex) {// вывод исключений
            throw new ClientException(locMes("command_error"), ex);
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcGetSrt rpc;
        try {
            rpc = gson.fromJson(res, RpcGetSrt.class);
        } catch (JsonSyntaxException ex) {
            throw new ClientException(locMes("bad_response") + "\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Пробить номер клиента. Стоит в очереди или отложен или вообще не найден.
     *
     * @param netProperty    параметры соединения с сервером
     * @param customerNumber
     * @return Текстовый ответ о результате
     */
    public static TicketHistory checkCustomerNumber(INetProperty netProperty, String customerNumber) {
        log().info("Команда проверки номера кастомера.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.clientAuthId = customerNumber;
        final String res;
        try {
            res = send(netProperty, Uses.TASK_CHECK_CUSTOMER_NUMBER, params);
        } catch (QException ex) {// вывод исключений
            throw new ClientException(locMes("command_error"), ex);
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcGetTicketHistory rpc;
        try {
            rpc = gson.fromJson(res, RpcGetTicketHistory.class);
        } catch (JsonSyntaxException ex) {
            throw new ClientException(locMes("bad_response") + "\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Получить список отложенных кастомеров
     *
     * @param netProperty
     * @return список отложенных кастомеров
     */
    public static LinkedList<QCustomer> getPostponedPoolInfo(INetProperty netProperty) {
        log().info("Команда на обновление пула отложенных.");
        // загрузим ответ
        final String res;
        try {
            res = send(netProperty, Uses.TASK_GET_POSTPONED_POOL, null);
        } catch (QException ex) {// вывод исключений
            throw new ClientException(locMes("command_error"), ex);
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcGetPostponedPoolInfo rpc;
        try {
            rpc = gson.fromJson(res, RpcGetPostponedPoolInfo.class);
        } catch (JsonSyntaxException ex) {
            throw new ClientException(locMes("bad_response") + "\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Получить список забаненных введенных данных
     *
     * @param netProperty
     * @return список отложенных кастомеров
     */
    public static LinkedList<String> getBanedList(INetProperty netProperty) {
        log().info("Команда получение списка забаненных.");
        // загрузим ответ
        final String res;
        try {
            res = send(netProperty, Uses.TASK_GET_BAN_LIST, null);
        } catch (QException ex) {// вывод исключений
            throw new ClientException(locMes("command_error"), ex);
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcBanList rpc;
        try {
            rpc = gson.fromJson(res, RpcBanList.class);
        } catch (JsonSyntaxException ex) {
            throw new ClientException(locMes("bad_response") + "\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getBanList();
    }

    /**
     * Вызов отложенного кастомера
     *
     * @param netProperty
     * @param userId      id юзера который вызывает
     * @param id          это ID кастомера которого вызываем из пула отложенных, оно есть т.к. с качстомером давно работаем
     * @return Вызванный из отложенных кастомер.
     */
    public static QCustomer invitePostponeCustomer(INetProperty netProperty, long userId, Long id) {
        log().info("Команда на вызов кастомера из пула отложенных.");
        final CmdParams params = new CmdParams();
        params.userId = userId;
        params.customerId = id;
        // загрузим ответ
        final String res;
        try {
            res = send(netProperty, Uses.TASK_INVITE_POSTPONED, params);
        } catch (QException ex) {// вывод исключений
            throw new ClientException(locMes("command_error"), ex);
        }

        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcInviteCustomer rpc;
        try {
            rpc = gson.fromJson(res, RpcInviteCustomer.class);
        } catch (JsonSyntaxException ex) {
            throw new ClientException(locMes("bad_response") + "\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Рестарт главного табло
     *
     * @param serverNetProperty
     */
    public static void restartMainTablo(INetProperty serverNetProperty) {
        log().info("Команда на рестарт главного табло.");
        // загрузим ответ
        try {
            send(serverNetProperty, Uses.TASK_RESTART_MAIN_TABLO, null);
        } catch (QException ex) {// вывод исключений
            throw new ClientException(locMes("command_error"), ex);
        }
    }

    /**
     * Изменение приоритетов услуг оператором
     *
     * @param netProperty
     * @param userId      id юзера который вызывает
     * @param smartData
     */
    public static void changeFlexPriority(INetProperty netProperty, long userId, String smartData) {
        log().info("Изменение приоритетов услуг оператором.");
        final CmdParams params = new CmdParams();
        params.userId = userId;
        params.textData = smartData;
        // загрузим ответ
        try {
            send(netProperty, Uses.TASK_CHANGE_FLEX_PRIORITY, params);
        } catch (QException ex) {// вывод исключений
            throw new ClientException(locMes("command_error"), ex);
        }
    }

    /**
     * Изменение бегущей строки на табло из админской проги
     *
     * @param netProperty параметры соединения с сервером
     * @param text        новая строка
     * @param nameSection
     */
    public static void setRunningText(INetProperty netProperty, String text, String nameSection) {
        log().info("Получение описания авторизованного пользователя.");
        // загрузим ответ
        final CmdParams params = new CmdParams();
        params.textData = text;
        params.infoItemName = nameSection;
        try {
            send(netProperty, Uses.TASK_CHANGE_RUNNING_TEXT_ON_BOARD, params);
        } catch (QException ex) {// вывод исключений
            throw new ClientException(locMes("command_error"), ex);
        }
    }

    /**
     * Получить норрмативы
     *
     * @param netProperty
     * @return класс нормативов
     */
    public static QStandards getStandards(INetProperty netProperty) {
        log().info("Команда получение нормативов.");
        // загрузим ответ
        final String res;
        try {
            res = send(netProperty, Uses.TASK_GET_STANDARDS, null);
        } catch (QException ex) {// вывод исключений
            throw new ClientException(locMes("command_error"), ex);
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcGetStandards rpc;
        try {
            rpc = gson.fromJson(res, RpcGetStandards.class);
        } catch (JsonSyntaxException ex) {
            throw new ClientException(locMes("bad_response") + "\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Изменение приоритетов услуг оператором
     *
     * @param netProperty
     * @param userId      id юзера который вызывает
     * @param lock
     * @return
     */
    public static boolean setBussy(INetProperty netProperty, long userId, boolean lock) {
        log().info("Изменение приоритетов услуг оператором.");
        final CmdParams params = new CmdParams();
        params.userId = userId;
        params.requestBack = lock;
        // загрузим ответ
        final String res;
        try {
            res = send(netProperty, Uses.TASK_SET_BUSSY, params);
        } catch (QException ex) {// вывод исключений
            throw new ClientException(locMes("command_error"), ex);
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcGetBool rpc;
        try {
            rpc = gson.fromJson(res, RpcGetBool.class);
        } catch (JsonSyntaxException ex) {
            throw new ClientException(locMes("bad_response") + "\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Получить параметры из ДБ из сервера
     *
     * @param netProperty
     * @return мапа с секциями
     */
    public static LinkedHashMap<String, ServerProps.Section> getProperties(INetProperty netProperty) {
        log().info("Getting options.");
        final CmdParams params = new CmdParams();
        // загрузим ответ
        final String res;
        try {
            res = send(netProperty, Uses.TASK_GET_PROPERTIES, params);
        } catch (QException ex) {// вывод исключений
            throw new ClientException(locMes("command_error"), ex);
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcGetProperties rpc;
        try {
            rpc = gson.fromJson(res, RpcGetProperties.class);
        } catch (JsonSyntaxException ex) {
            throw new ClientException(locMes("bad_response") + "\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Изменить и сохранить параметеры в ДБ на сервере
     *
     * @param netProperty
     * @param properties
     * @return Список свежих свойств
     */
    public static LinkedHashMap<String, ServerProps.Section> saveProperties(INetProperty netProperty, List<QProperty> properties) {
        log().info("Изменить и сохранить параметеры в ДБ на сервере.");
        final CmdParams params = new CmdParams();
        params.properties = properties;
        // загрузим ответ
        final String res;
        try {
            res = send(netProperty, Uses.TASK_INIT_PROPERTIES, params);
        } catch (QException ex) {// вывод исключений
            throw new ClientException(locMes("command_error"), ex);
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcGetProperties rpc;
        try {
            rpc = gson.fromJson(res, RpcGetProperties.class);
        } catch (JsonSyntaxException ex) {
            throw new ClientException(locMes("bad_response") + "\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Если таких параметров нет, то создать их в ДБ на сервере
     *
     * @param netProperty
     * @param properties
     * @return Список свежих свойств
     */
    public static LinkedHashMap<String, ServerProps.Section> initProperties(INetProperty netProperty, List<QProperty> properties) {
        log().info("Если таких параметров нет, то создать их в ДБ на сервере.");
        final CmdParams params = new CmdParams();
        params.properties = properties;
        // загрузим ответ
        final String res;
        try {
            res = send(netProperty, Uses.TASK_SAVE_PROPERTIES, params);
        } catch (QException ex) {// вывод исключений
            throw new ClientException(locMes("command_error"), ex);
        }
        final Gson gson = GsonPool.getInstance().borrowGson();
        final RpcGetProperties rpc;
        try {
            rpc = gson.fromJson(res, RpcGetProperties.class);
        } catch (JsonSyntaxException ex) {
            throw new ClientException(locMes("bad_response") + "\n" + ex.toString());
        } finally {
            GsonPool.getInstance().returnGson(gson);
        }
        return rpc.getResult();
    }

    /**
     * Выставить параметры рулона с билетами на бабине.
     *
     * @param netProperty
     * @param serviceId   эту услугу надо обновить
     * @param first       в новом рулоне он первый
     * @param last        рулон длинной такой
     * @param current     сейчас в рулоне этот текущий номер
     */
    public static void initRoll(INetProperty netProperty, Long serviceId, int first, int last, int current) {
        log().info("Изменение приоритетов услуг оператором.");
        final CmdParams params = new CmdParams();
        params.serviceId = serviceId;
        params.first = first;
        params.last = last;
        params.current = current;
        // отправим запрос
        try {
            send(netProperty, Uses.TASK_REINIT_ROLL, params);
        } catch (QException ex) {// вывод исключений
            throw new ClientException(locMes("command_error"), ex);
        }
    }
}

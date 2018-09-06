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
package ru.apertum.qsystem.server.controller;

import org.springframework.transaction.TransactionStatus;
import ru.apertum.qsystem.common.SoundPlayer;

import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;

import org.dom4j.DocumentException;
import ru.apertum.qsystem.common.model.QCustomer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.dom4j.DocumentHelper;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import ru.apertum.qsystem.client.Locales;
import ru.apertum.qsystem.common.Uses;
import ru.apertum.qsystem.common.QLog;
import ru.apertum.qsystem.common.CustomerState;
import ru.apertum.qsystem.common.QConfig;
import ru.apertum.qsystem.common.cmd.CmdParams;
import ru.apertum.qsystem.common.cmd.AJsonRPC20;
import ru.apertum.qsystem.common.cmd.JsonRPC20;
import ru.apertum.qsystem.common.cmd.JsonRPC20Error;

import static ru.apertum.qsystem.common.cmd.JsonRPC20Error.ErrorMsg.*;

import ru.apertum.qsystem.common.cmd.JsonRPC20OK;
import ru.apertum.qsystem.common.cmd.RpcGetAdvanceCustomer;
import ru.apertum.qsystem.common.cmd.RpcGetAllServices;
import ru.apertum.qsystem.common.cmd.RpcGetAuthorizCustomer;
import ru.apertum.qsystem.common.cmd.RpcGetBool;
import ru.apertum.qsystem.common.cmd.RpcGetGridOfWeek;
import ru.apertum.qsystem.common.cmd.RpcGetGridOfWeek.GridAndParams;
import ru.apertum.qsystem.common.cmd.RpcGetInfoTree;
import ru.apertum.qsystem.common.cmd.RpcGetInt;
import ru.apertum.qsystem.common.cmd.RpcGetPostponedPoolInfo;
import ru.apertum.qsystem.common.cmd.RpcGetRespTree;
import ru.apertum.qsystem.common.cmd.RpcGetResultsList;
import ru.apertum.qsystem.common.cmd.RpcGetSelfSituation;
import ru.apertum.qsystem.common.cmd.RpcGetServerState;
import ru.apertum.qsystem.common.cmd.RpcGetSrt;
import ru.apertum.qsystem.common.cmd.RpcGetUsersList;
import ru.apertum.qsystem.common.cmd.RpcInviteCustomer;
import ru.apertum.qsystem.common.cmd.RpcStandInService;
import ru.apertum.qsystem.common.exceptions.ServerException;
import ru.apertum.qsystem.common.cmd.RpcBanList;
import ru.apertum.qsystem.common.cmd.RpcGetGridOfDay;
import ru.apertum.qsystem.common.cmd.RpcGetProperties;
import ru.apertum.qsystem.common.cmd.RpcGetStandards;
import ru.apertum.qsystem.common.cmd.RpcGetServiceState;
import ru.apertum.qsystem.common.cmd.RpcGetTicketHistory;
import ru.apertum.qsystem.extra.ISelectNextService;
import ru.apertum.qsystem.extra.ITask;
import ru.apertum.qsystem.server.MainBoard;
import ru.apertum.qsystem.server.QServer;
import ru.apertum.qsystem.server.QSessions;
import ru.apertum.qsystem.server.ServerProps;
import ru.apertum.qsystem.server.Spring;
import ru.apertum.qsystem.server.model.QAdvanceCustomer;
import ru.apertum.qsystem.server.model.QAuthorizationCustomer;
import ru.apertum.qsystem.server.model.QPlanService;
import ru.apertum.qsystem.server.model.QProperty;
import ru.apertum.qsystem.server.model.QService;
import ru.apertum.qsystem.server.model.QServiceTree;
import ru.apertum.qsystem.server.model.QUser;
import ru.apertum.qsystem.server.model.QUserList;
import ru.apertum.qsystem.server.model.calendar.QCalendarList;
import ru.apertum.qsystem.server.model.infosystem.QInfoTree;
import ru.apertum.qsystem.server.model.postponed.QPostponedList;
import ru.apertum.qsystem.server.model.response.QRespEvent;
import ru.apertum.qsystem.server.model.response.QResponseTree;
import ru.apertum.qsystem.server.model.results.QResult;
import ru.apertum.qsystem.server.model.results.QResultList;
import ru.apertum.qsystem.server.model.schedule.QBreak;
import ru.apertum.qsystem.server.model.schedule.QBreaks;
import ru.apertum.qsystem.server.model.schedule.QSchedule;

/**
 * A pool of queues. The queue pool is the main queue management structure. There are several queues in the system, for example, to provide different services. Pool
 * receives XML tasks from the network, determines the required action. Performs actions to organize the pool. Performs tasks related to multiple queues.
 *
 * Works as a singleton
 * @author Evgeniy Egorov
 */
public final class Executer {

    public static Executer getInstance() {
        return ExecuterHolder.INSTANCE;
    }

    private static class ExecuterHolder {

        private static final Executer INSTANCE = new Executer();
    }

    /**
     * Конструктор пула очередей Также нужно оперделить способ вывода информации для клиентов на табло.
     */
    private Executer() {
        // поддержка расширяемости плагинами
        for (final ITask task : ServiceLoader.load(ITask.class)) {
            QLog.l().logger().info("Load extra task: " + task.getDescription());
            try {
                tasks.put(task.getName(), task);
            } catch (Exception tr) {
                QLog.l().logger().error("Вызов SPI расширения завершился ошибкой. Описание: " + tr);
            }
        }
    }

    //
    //*******************************************************************************************************
    //**************************  handlers JOB *******************************************************
    //*******************************************************************************************************
    //
    // tasks are available by their names
    private final HashMap<String, ITask> tasks = new HashMap<>();

    public HashMap<String, ITask> getTasks() {
        return tasks;
    }

    /**
     * @author Evgeniy Egorov
     * Base class of job handlers. itself adds up in HashMap [String, ATask] tasks. the process method executes the job.
     */
    public class Task implements ITask {

        protected final String name;
        protected CmdParams cmdParams;

        public Task(String name) {
            this.name = name;
            final Task tk = this;
            tasks.put(name, tk);
        }

        @Override
        public AJsonRPC20 process(CmdParams cmdParams, String ipAdress, byte[] ip) {
            QLog.l().logger().debug("Processing: \"" + name + "\" " + ipAdress);
            QSessions.getInstance().update(cmdParams == null ? null : cmdParams.userId, ipAdress, ip);
            this.cmdParams = cmdParams;
            return new JsonRPC20OK();
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getDescription() {
            return "Standard internal QSystem task.";
        }

        @Override
        public long getUID() {
            return 777L;
        }
    }

    /**
     * Ключ блокировки для манипуляции с кстомерами
     */
    public static final Lock CLIENT_TASK_LOCK = new ReentrantLock();
    /**
     * Ключ блокировки для манипуляции с отложенными. Когда по таймеру они выдергиваются. Не нужно чтоб перекосило вызовом от пользователя
     */
    public static final Lock POSTPONED_TASK_LOCK = new ReentrantLock();
    /**
     * Ставим кастомера в очередь.
     */
    final AddCustomerTask addCustomerTask = new AddCustomerTask(Uses.TASK_STAND_IN);

    class AddCustomerTask extends Task {

        public AddCustomerTask(String name) {
            super(name);
        }

        @Override
        public RpcStandInService process(CmdParams cmdParams, String ipAdress, byte[] ip) {
            super.process(cmdParams, ipAdress, ip);
            final QService service = QServiceTree.getInstance().getById(cmdParams.serviceId);
            final QCustomer customer;
            // синхронизируем работу с клиентом
            CLIENT_TASK_LOCK.lock();
            try {
                // Создадим вновь испеченного кастомера

                // проверка тут если нужно введенное сделать номерком
                final boolean asNumber = service.getInput_required() && service.getInputedAsNumber() > 0 && cmdParams.textData != null && cmdParams.textData.length() > 0;
                customer = new QCustomer(asNumber ? -1 : service.getNextNumber());
                customer.setLanguage(cmdParams.language);// может приехать язык выбранный кастомером при работе с киоском
                // тут если нужно введенное сделать номерком
                if (asNumber) {
                    customer.setPrefix(cmdParams.textData.length() >= service.getInputedAsNumber() ? cmdParams.textData.substring(0, service.getInputedAsNumber()) : cmdParams.textData);
                }
                // Определим кастомера в очередь
                customer.setService(service);
                if (service.getLink() != null) {
                    customer.setService(service.getLink());
                }
                // время постановки проставляется автоматом при создании кастомера.
                // Приоритет "как все"
                customer.setPriority(cmdParams.priority);
                // Введенные кастомером данные
                customer.setInput_data(cmdParams.textData);
                //добавим нового пользователя
                (service.getLink() != null ? service.getLink() : service).addCustomer(customer);
                // Состояние у него "Стою, жду".
                customer.setState(CustomerState.STATE_WAIT);
            } catch (Exception ex) {
                throw new ServerException("Ошибка при постановке клиента в очередь " + ipAdress, ex);
            } finally {
                CLIENT_TASK_LOCK.unlock();
            }
            QLog.l().logger().trace("С приоритетом " + customer.getPriority().get() + " К услуге \"" + cmdParams.serviceId + "\" -> " + service.getPrefix() + '\'' + service.getName() + '\'' + " " + ipAdress);
            // если кастомер добавился, то его обязательно отправить в ответ т.к.
            // он уже есть в системе
            try {
                // сохраняем состояния очередей.
                QServer.savePool();
                //разослать оповещение о том, что появился посетитель
                //рассылаем широковещетельно по UDP на определенный порт
                Uses.sendUDPBroadcast(service.getId().toString(), ServerProps.getInstance().getProps().getClientPort());

                // Должно высветитьсяна основном табло в таблице ближайших
                MainBoard.getInstance().customerStandIn(customer);
            } catch (Exception ex) {
                QLog.l().logger().error(ex);
            }
            return new RpcStandInService(customer);
        }
    }

    /**
     * Ставим кастомера в очередь к нескольким услугам.
     *
     * @return
     */
    final Task addCustomerTaskComplex = new Task(Uses.TASK_STAND_COMPLEX) {

        @Override
        public RpcStandInService process(CmdParams cmdParams, String ipAdress, byte[] ip) {
            super.process(cmdParams, ipAdress, ip);

            Long serviceID = null;

            // поддержка расширяемости плагинами
            for (final ISelectNextService event : ServiceLoader.load(ISelectNextService.class)) {
                QLog.l().logger().info("Вызов SPI расширения. Описание: " + event.getDescription() + " " + ipAdress);
                try {
                    serviceID = event.select(null, null, cmdParams.complexId).getId();
                } catch (Exception tr) {
                    QLog.l().logger().error("Вызов SPI расширения завершился ошибкой. Описание: " + tr + " " + ipAdress);
                }
            }

            // дефлотный выбор следующей услуги
            if (serviceID == null) {
                for (LinkedList<LinkedList<Long>> ids : cmdParams.complexId) {
                    if (!ids.isEmpty()) {
                        final LinkedList<Long> id = ids.getFirst();
                        serviceID = id.getFirst();
                        if (serviceID != null) {
                            ids.remove(id);
                            break;
                        }
                    }
                }
            } else {
                // подотрем выбраную услугу
                for (LinkedList<LinkedList<Long>> ids : cmdParams.complexId) {
                    for (LinkedList<Long> id : ids) {
                        if (serviceID.equals(id.getFirst())) {
                            ids.remove(id);
                            break;
                        } else {
                            for (Long long1 : id) {
                                if (serviceID.equals(long1)) {
                                    id.remove(long1);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            if (serviceID == null) {
                throw new ServerException("Ошибка поставить в очередь к многим услугам. Услуг не найдено." + " " + ipAdress);
            }

            // создаем кастомера вызвав задание по созданию кастомера
            // загрузим задание
            cmdParams.serviceId = serviceID;
            final RpcStandInService txtCustomer = addCustomerTask.process(cmdParams, ipAdress, ip);
            txtCustomer.getResult().setComplexId(cmdParams.complexId);
            return txtCustomer;

        }
    };
    /**
     * Пригласить кастомера, первого в очереди.
     */
    final Task inviteCustomerTask = new Task(Uses.TASK_INVITE_NEXT_CUSTOMER) {

        private final HashSet<QUser> usrs = new HashSet<>();

        class MyRun implements Runnable {

            private QUser user;
            private boolean isFrst;

            @Override
            public void run() {
                final long delta = System.currentTimeMillis() - user.getCustomer().getStandTime().getTime();
                if (delta < QConfig.cfg().getDelayFirstInvite() * 1000) {
                    try {
                        Thread.sleep(QConfig.cfg().getDelayFirstInvite() * 1000 - delta);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
                // просигналим звуком
                if (user.getCustomer() != null && (user.getCustomer().getState() == CustomerState.STATE_WAIT
                        || user.getCustomer().getState() == CustomerState.STATE_INVITED_SECONDARY
                        || user.getCustomer().getState() == CustomerState.STATE_INVITED
                        || user.getCustomer().getState() == CustomerState.STATE_BACK
                        || user.getCustomer().getState() == CustomerState.STATE_WAIT_AFTER_POSTPONED
                        || user.getCustomer().getState() == CustomerState.STATE_WAIT_COMPLEX_SERVICE)) {
                    try {
                        SoundPlayer.inviteClient(user.getCustomer().getService(), user.getCustomer(), user.getCustomer().getPrefix() + (user.getCustomer().getNumber() < 1 ? "" : user.getCustomer().getNumber()), user.getPoint(), isFrst);
                    } catch (Exception ex) {
                        QLog.l().logger().error("Error of sound player!", ex);
                    }
                    // Должно высветитьсяна основном табло
                    MainBoard.getInstance().inviteCustomer(user, user.getCustomer());
                }
                usrs.remove(user);
            }
        }

        private void invite(final QUser user, final boolean isFirst) {
            if (usrs.contains(user)) {
                return;
            }
            usrs.add(user);
            final MyRun mr = new MyRun();
            mr.user = user;
            mr.isFrst = isFirst;
            final Thread t = new Thread(mr);
            t.setDaemon(true);
            t.start();
        }

        /**
         * Cинхронизируем, а-то вызовут одного и того же. А еще сдесь надо вызвать метод, который "проговорит" кого и куда вазвали. Может случиться ситуация
         * когда двое вызывают последнего кастомера, первому достанется, а второму нет.
         */
        @Override
        public synchronized RpcInviteCustomer process(CmdParams cmdParams, String ipAdress, byte[] ip) {
            super.process(cmdParams, ipAdress, ip);
            // Определить из какой очереди надо выбрать кастомера.
            // Пока без учета коэфициента.
            // Для этого смотрим первых кастомеров во всех очередях и ищем первого среди первых.
            final QUser user = QUserList.getInstance().getById(cmdParams.userId); // юзер
            final boolean isRecall = user.getCustomer() != null && (CustomerState.STATE_INVITED.equals(user.getCustomer().getState()) || CustomerState.STATE_INVITED_SECONDARY.equals(user.getCustomer().getState()));

            // есть ли у юзера вызванный кастомер? Тогда поторный вызов
            if (isRecall) {
                user.getCustomer().upRecallCount(); // еще один повторный вызов
                QLog.l().logger().debug("Повторный вызов " + user.getCustomer().getRecallCount() + " кастомера №" + user.getCustomer().getPrefix() + user.getCustomer().getNumber() + " пользователем " + cmdParams.userId + " " + ipAdress);

                if (ServerProps.getInstance().getProps().getLimitRecall() != 0 && user.getCustomer().getRecallCount() > ServerProps.getInstance().getProps().getLimitRecall()) {
                    QLog.l().logger().debug("Превышение повторных вызовов для кастомера №" + user.getCustomer().getPrefix() + user.getCustomer().getNumber() + " пользователем " + cmdParams.userId + " " + ipAdress);
                    //Удалим по неявки
                    killCustomerTask.process(cmdParams, ipAdress, ip);
                } else {
                    // кастомер переходит в состояние в котором был в такое и переходит.
                    user.getCustomer().setState(user.getCustomer().getState());

                    // просигналим звуком
                    // Должно высветитьсяна основном табло
                    invite(user, false);

                    return new RpcInviteCustomer(user.getCustomer());
                }
            }

            // бежим по очередям юзера и ищем первого из первых кастомера
            QCustomer customer = null;
            int servPriority = -1;// временная переменная для приоритета услуг
            // синхронизация работы с клиентом
            CLIENT_TASK_LOCK.lock();
            try {

                // Мерзость. вызов по номеру или ID.
                if ((cmdParams.textData != null && !cmdParams.textData.isEmpty()) || cmdParams.customerId != null) {
                    if (cmdParams.customerId == null) {
                        final String num = cmdParams.textData.replaceAll("[^\\p{L}+\\d]", "");
                        QLog.l().logger().debug("Warning! Corruption was detected! \"" + num + "\"" + " " + ipAdress);
                        for (QService service : QServiceTree.getInstance().getNodes()) {
                            if ((customer = service.gnawOutCustomerByNumber(num)) != null) {
                                QLog.l().logger().debug("Warning! Corruption was detected! \"" + num + "\"" + " " + ipAdress);
                                break;
                            }
                        }
                    } else {
                        QLog.l().logger().debug("Warning! Smart corruption was detected! \"" + cmdParams.customerId + "\"" + " " + ipAdress);
                        for (QService service : QServiceTree.getInstance().getNodes()) {
                            if ((customer = service.gnawOutCustomerById(cmdParams.customerId)) != null) {
                                QLog.l().logger().debug("Warning! Clever corruption was detected! \"" + cmdParams.customerId + "\"" + " " + ipAdress);
                                break;
                            }
                        }
                        if (customer == null) {
                            // А вдруг он в отложенных лежит. Найти его и пригласить на прием.
                            if ((customer = QPostponedList.getInstance().getById(cmdParams.customerId)) != null
                                    && (customer.getIsMine() == null || customer.getIsMine().equals(user.getId()))) {
                                QPostponedList.getInstance().removeElement(customer);
                                QLog.l().logger().debug("Warning! Clever corruption was detected in postponed! \"" + cmdParams.customerId + "\"" + " " + ipAdress);
                            } else {
                                customer = null;
                            }
                        }
                    }
                    if (customer == null) {
                        return new RpcInviteCustomer(null);
                    } else {
                        // разберемся с услугами, вдруг вызвали из не своей услуги
                        boolean f = true;
                        for (QPlanService plan : user.getPlanServices()) {
                            if (plan.getService().getId().equals(customer.getService().getId())) {
                                f = false;
                                break;
                            }
                        }
                        if (f) {
                            customer.setService(user.getPlanServices().get(0).getService());
                        }
                    }
                }// Мерзость закончилась. Вызвали по номеру если надо было.

                if (customer == null) {
                    QService rollService = null;
                    for (QPlanService plan : user.getPlanServices()) {

                        // тут посмотрим, не приехала ли услуга, если да, то вызов из этой услуги. Если она пустая, то вызываем как обычно.
                        if (cmdParams.serviceId != null
                                && QServiceTree.getInstance().getById(cmdParams.serviceId) != null
                                && QServiceTree.getInstance().getById(cmdParams.serviceId).getCountCustomers() != 0
                                && !cmdParams.serviceId.equals(plan.getService().getId())) {
                            continue;
                        }

                        final QService serv = QServiceTree.getInstance().getById(plan.getService().getId()); // очередная очередь
                        if (serv.getStatus() == 5) {
                            rollService = serv;
                        }

                        final QCustomer cust = serv.peekCustomer(); // первый в этой очереди
                        // если очередь пуста
                        if (cust != null) {
                            // учтем приоритетность кастомеров и приоритетность очередей для юзера в которые они стоят
                            final Integer prior = plan.getCoefficient();
                            if (prior > servPriority || (prior == servPriority && customer != null && customer.compareTo(cust) > 0)) {
                                servPriority = prior;
                                customer = cust;
                            }
                        }
                    }
                    // Если в основных очередях нет клиентов, но есть услуга-рулон. Тогда крутим рулон, создаем кастомера и его считаем вызванным.
                    if (customer == null && rollService != null) {
                        // и так, нету клиентов, но есть рулонная услуга.
                        // Создадим рулонного кастомера, у него управление номерами особое, сбрасывать можно.
                        customer = new QCustomer(rollService.getNextNumber());
                        customer.setService(rollService);
                        // время постановки проставляется автоматом при создании кастомера.
                        // Приоритет "как все"
                        customer.setPriority(1);
                        // Введенные кастомером данные
                        customer.setInput_data("");
                        // Состояние у него "Стою, жду".
                        customer.setState(CustomerState.STATE_WAIT);
                        // небольшой допил, типа талон был оторван немного раньше
                        customer.setStandTime(new Date(System.currentTimeMillis() - 61 * 1000));
                    } else {
                        //Найденного самого первого из первых кастомера переносим на хранение юзеру, при этом удалив его из общей очереди.
                        // Случай, когда всех разобрали, но вызов сделан
                        //При приглашении очередного клиента пользователем очереди оказались пустые.
                        if (customer == null) {
                            return new RpcInviteCustomer(null);
                        }
                        // подотрем выбранного кастомера из очереди ожидания, он уже уехал к юзеру теперь.
                        customer = QServiceTree.getInstance().getById(customer.getService().getId()).polCustomer();
                    }
                }
            } catch (Exception ex) {
                throw new ServerException("Ошибка при постановке клиента в очередь" + " " + ipAdress + ex);
            } finally {
                CLIENT_TASK_LOCK.unlock();
            }
            if (customer == null) {
                throw new ServerException("Странная проблема с получением кастомера и удалением его из очереди." + " " + ipAdress);
            }
            // определим юзеру кастомера, которого он вызвал.
            // При этом же произойдет присвоение кастомеру юзера, который его вызвал.
            user.setCustomer(customer);

            // ставим время вызова
            customer.setCallTime(new Date());
            // кастомер переходит в состояние "приглашенности"
            customer.setState(customer.getState() == CustomerState.STATE_WAIT ? CustomerState.STATE_INVITED : CustomerState.STATE_INVITED_SECONDARY);

            // вот тут посмотрим, нужно ли вызывать кастомера по табло.
            // если его услуга без вызова(настраивается в параметрах услуги), то его не нужно звать,
            // а стазу начать что-то делать.
            // Например кастомера отправили на комплектование товара, при этом его не нужно звать, только скомплектовать товар и
            // заредиректить на выдачу, вот на выдаче его и нужно звать.
            if (customer.getService().getEnable() != 1) { // услуга не требует вызова
                // Время старта работы с юзера с кастомером.
                customer.setStartTime(new Date());
                // кастомер переходит в состояние "Начала обработки" или "Продолжение работы"
                customer.setState(user.getCustomer().getState() == CustomerState.STATE_INVITED ? CustomerState.STATE_WORK : CustomerState.STATE_WORK_SECONDARY);
            }

            // если кастомер вызвался, то его обязательно отправить в ответ
            // он уже есть у юзера
            try {
                // сохраняем состояния очередей.
                QServer.savePool();
                if (customer.getService().getEnable() == 1) { // услуга требует вызова
                    // звук
                    // Должно высветитьсяна основном табло
                    invite(user, true);
                }
                //разослать оповещение о том, что посетителя вызвали, состояние очереди изменилось
                //рассылаем широковещетельно по UDP на определенный порт
                Uses.sendUDPBroadcast(customer.getService().getId().toString(), ServerProps.getInstance().getProps().getClientPort());
            } catch (Exception ex) {
                QLog.l().logger().error(ex);
            }
            return new RpcInviteCustomer(customer);
        }
    };
    /**
     * Пригласить кастомера из пула отложенных
     */
    final Task invitePostponedTask = new Task(Uses.TASK_INVITE_POSTPONED) {

        /**
         * Cинхронизируем, а то вызовут одного и того же. А еще сдесь надо вызвать метод, который "проговорит" кого и куда вазвали. Может случиться ситуация
         * когда двое вызывают последнего кастомера, первому достанется, а второму нет.
         */
        @Override
        public synchronized AJsonRPC20 process(CmdParams cmdParams, String ipAdress, byte[] ip) {
            super.process(cmdParams, ipAdress, ip);
            // синхронизация работы с клиентом

            // Определить из какой очереди надо выбрать кастомера.
            // Пока без учета коэфициента.
            // Для этого смотрим первых кастомеров во всех очередях и ищем первого среди первых.
            final QUser user = QUserList.getInstance().getById(cmdParams.userId); // юзер
            final QCustomer customer;
            POSTPONED_TASK_LOCK.lock();
            try {
                // выберем отложенного кастомера по ид
                customer = QPostponedList.getInstance().getById(cmdParams.customerId);

                if (customer == null) {
                    return new JsonRPC20Error(POSTPONED_NOT_FOUND, cmdParams.customerId);
                } else {
                    QPostponedList.getInstance().removeElement(customer);
                }
                // определим юзеру кастомера, которого он вызвал.
                //  там же при этом же поставим кастомеру юзера, который его вызвал.
                user.setCustomer(customer);
                // только что встал типо. Поросто время нахождения в отложенных не считается как ожидание очереди. Инвче в statistic ожидание огромное
                // Это все таки отложенные, а не перенаправленные. Типа при отложенном состоянии идет работа.
                customer.setStandTime(new Date());
                // ну и услугу определим если тот кто вызвал не работает с услугой, из которой отложили
                boolean f = true;
                for (QPlanService pl : user.getPlanServices()) {
                    if (pl.getService().getId().equals(customer.getService().getId())) {
                        f = false;
                        break;
                    }
                }
                if (f) {
                    customer.setService(QServiceTree.getInstance().getById(user.getPlanServices().get(0).getService().getId()));
                }
                // кастомер переходит в состояние "приглашенности"
                customer.setState(CustomerState.STATE_INVITED_SECONDARY);
                // если кастомер вызвался, то его обязательно отправить в ответ
                // он уже есть у юзера
            } catch (Exception ex) {
                throw new ServerException("Ошибка при вызове отложенного напрямую пользователем " + ex + " " + ipAdress);
            } finally {
                POSTPONED_TASK_LOCK.unlock();
            }
            try {
                // просигналим звуком
                SoundPlayer.inviteClient(customer.getService(), customer, user.getCustomer().getPrefix() + (user.getCustomer().getNumber() < 1 ? "" : user.getCustomer().getNumber()), user.getPoint(), true);
                // сохраняем состояния очередей.
                QServer.savePool();
                //разослать оповещение о том, что появился вызванный посетитель
                // Должно высветитьсяна основном табло
                MainBoard.getInstance().inviteCustomer(user, user.getCustomer());
                //разослать оповещение о том, что отложенного вызвали, состояние очереди изменилось не изменилось, но пул отложенных изменился
                //рассылаем широковещетельно по UDP на определенный порт
                Uses.sendUDPBroadcast(Uses.TASK_REFRESH_POSTPONED_POOL, ServerProps.getInstance().getProps().getClientPort());
            } catch (Exception ex) {
                QLog.l().logger().error(ex);
            }
            return new RpcInviteCustomer(customer);
        }
    };
    /**
     * Task Get Services
     */
    final Task getServicesTask = new Task(Uses.TASK_GET_SERVICES) {

        @Override
        public RpcGetAllServices process(CmdParams cmdParams, String ipAdress, byte[] ip) {
            super.process(cmdParams, ipAdress, ip);
            return new RpcGetAllServices(new RpcGetAllServices.ServicesForWelcome(QServiceTree.getInstance().getRoot(), ServerProps.getInstance().getProps()));
        }
    };
    /**
     * Если услуга требует ввода данных пользователем, то нужно получить эти данные из диалога ввода если ввели, то тут спрашиваем у сервера есть ли возможность
     * встать в очередь с такими введенными данными
     *
     * @return 1 - превышен, 0 - можно встать. 2 - забанен
     */
    final Task aboutServicePersonLimit = new Task(Uses.TASK_ABOUT_SERVICE_PERSON_LIMIT) {

        @Override
        public RpcGetInt process(CmdParams cmdParams, String ipAdress, byte[] ip) {
            super.process(cmdParams, ipAdress, ip);
            if (RpcBanList.getInstance().isBaned(cmdParams.textData)) {
                return new RpcGetInt(2);
            }
            // Если лимит количества подобных введенных данных кастомерами в день достигнут
            final QService srvR = QServiceTree.getInstance().getById(cmdParams.serviceId);
            final QService srv = srvR.getLink() != null ? srvR.getLink() : srvR;
            try {
                return new RpcGetInt(srv.isLimitPersonPerDayOver(cmdParams.textData) ? 1 : 0);
            } catch (Exception ex) {
                throw new ServerException("Подохло что-то при определении ограничения." + " " + ipAdress, ex);
            }
        }
    };
    /**
     * Получить описание состояния услуги
     */
    final Task getServiceConsistemcy = new Task(Uses.TASK_GET_SERVICE_CONSISANCY) {

        @Override
        public RpcGetServiceState process(CmdParams cmdParams, String ipAdress, byte[] ip) {
            super.process(cmdParams, ipAdress, ip);
            final QService srvR = QServiceTree.getInstance().getById(cmdParams.serviceId);
            final QService srv = srvR.getLink() != null ? srvR.getLink() : srvR;
            return new RpcGetServiceState(srv.getClients());
        }
    };
    /**
     * Получить описание состояния услуги
     */
    final Task aboutTask = new Task(Uses.TASK_ABOUT_SERVICE) {

        @Override
        public RpcGetServiceState process(CmdParams cmdParams, String ipAdress, byte[] ip) {
            super.process(cmdParams, ipAdress, ip);
            // Проверим оказывается ли сейчас эта услуга
            int min = Uses.LOCK_INT;
            final Date day = new Date();
            final QService srvR = QServiceTree.getInstance().getById(cmdParams.serviceId);
            final QService srv = srvR.getLink() != null ? srvR.getLink() : srvR;
            if (srv.getTempReasonUnavailable() != null && !"".equals(srv.getTempReasonUnavailable())) {
                return new RpcGetServiceState(Uses.NOT_POSSIBLE_IN_LINE, 0, srv.getTempReasonUnavailable());
            }
            // Если лимит количества возможных обработанных в день достигнут
            if (srv.isLimitPerDayOver()) {
                QLog.l().logger().warn("Service \"" + srv.getName() + "\" is not processed proceeding from achievement of a limit of possible processing of customers in day." + " " + ipAdress);
                return new RpcGetServiceState(Uses.LOCK_PER_DAY_INT, 0, "");
            }
            // Если нет расписания, календаря или выходной то отказ по расписанию
            if (srv.getSchedule() == null
                    || QCalendarList.getInstance().getById(1).checkFreeDay(day)
                    || (srv.getCalendar() != null && srv.getCalendar().checkFreeDay(day))) {
                if (srv.getSchedule() == null) {
                    QLog.l().logger().warn("If there is no schedule, then the failure on the schedule." + " " + ipAdress);
                } else if (QCalendarList.getInstance().getById(1).checkFreeDay(day)) {
                    QLog.l().logger().warn("If the output is then the failure on the schedule." + " " + ipAdress);
                } else {
                    QLog.l().logger().warn("If there is no calendar and the output is then a failure on the schedule." + " " + ipAdress);
                }
                min = Uses.LOCK_FREE_INT;
            } else {
                // Определим время начала и kонца работы на этот день
                final QSchedule.Interval interval = srv.getSchedule().getWorkInterval(day);
                // Определили начало и конец рабочего дня на сегодня
                // Если работаем в этот день то определим попадает ли "сейчас" в рабочий промежуток
                final GregorianCalendar gc_day = new GregorianCalendar();
                gc_day.setTime(day);
                if (!(interval.start == null || interval.finish == null)) {
                    final int h = gc_day.get(GregorianCalendar.HOUR_OF_DAY);
                    final int m = gc_day.get(GregorianCalendar.MINUTE);
                    gc_day.setTime(interval.start);
                    final int sh = gc_day.get(GregorianCalendar.HOUR_OF_DAY);
                    final int sm = gc_day.get(GregorianCalendar.MINUTE);
                    gc_day.setTime(interval.finish);
                    final int eh = gc_day.get(GregorianCalendar.HOUR_OF_DAY);
                    final int em = gc_day.get(GregorianCalendar.MINUTE);
                    if (!(sh * 60 + sm <= h * 60 + m && h * 60 + m <= eh * 60 + em) && (!((sh == eh) && (sm == em)))) {
                        QLog.l().logger().warn("If the current time does not fall within the working interval, then the failure on the schedule. " + sh + "." + sm + " < " + h + "." + m + " < " + eh + "." + em + " " + ipAdress);
                        min = Uses.LOCK_FREE_INT;
                    }
                } else {
                    QLog.l().logger().warn("There is no start or end on that day, the schedule fails." + (interval.start == null ? "start == null" : "end == null") + " " + ipAdress);
                    min = Uses.LOCK_FREE_INT;
                }
            }
            // If we do not work, we'll send a response and stop doing it.
            if (min == Uses.LOCK_FREE_INT) {
                QLog.l().logger().warn("Service \"" + cmdParams.serviceId + "\" is not processed based on the working schedule." + " " + ipAdress);
                return new RpcGetServiceState(Uses.LOCK_FREE_INT, 0, "");
            }
            // run on users and see if they are processing the service
            // if yes, then take all the services of the user and add all the customizers in the queues
            // the smallest amount will be sent in response on request.
            for (QUser user : QUserList.getInstance().getItems()) {
                if (user.hasService(cmdParams.serviceId)) {
                    // now for the services of the user
                    int sum = 0;
                    for (QPlanService planServ : user.getPlanServices()) {
                        final QService service = QServiceTree.getInstance().getById(planServ.getService().getId());
                        sum = sum + service.getCountCustomers();
                    }
                    if (min > sum) {
                        min = sum;
                    }
                }
            }
            if (min == Uses.LOCK_INT) {
                QLog.l().logger().warn("Service \"" + cmdParams.serviceId + "\" not processed by any user." + " " + ipAdress);
                return new RpcGetServiceState(Uses.LOCK_INT, 0, "");
            }
            // Check if we are in the break, if so, then send the text from the break
            final QBreaks breaks = srv.getSchedule().getBreaks();
            final QBreak qbreak = breaks == null ? null : breaks.isNow();
            return new RpcGetServiceState(Uses.NORMAL_RESPONCE, min, qbreak == null ? null : qbreak.getHint());
        }
    };
    /**
     * Получить описание пользователей для выбора
     */
    final Task getUsersTask = new Task(Uses.TASK_GET_USERS) {

        @Override
        public RpcGetUsersList process(CmdParams cmdParams, String ipAdress, byte[] ip) {
            super.process(cmdParams, ipAdress, ip);
            return new RpcGetUsersList(QUserList.getInstance().getItems());
        }
    };
    /**
     * Получить состояние сервера.
     */
    private final Task getServerState = new Task(Uses.TASK_SERVER_STATE) {

        @Override
        public RpcGetServerState process(CmdParams cmdParams, String ipAdress, byte[] ip) {
            super.process(cmdParams, ipAdress, ip);
            final LinkedList<RpcGetServerState.ServiceInfo> srvs = new LinkedList<>();

            QServiceTree.getInstance().getNodes().stream().filter(service -> (service.isLeaf())).forEach(service -> {
                final QCustomer customer = service.peekCustomer();
                srvs.add(new RpcGetServerState.ServiceInfo(service, service.getCountCustomers(), customer != null ? customer.getFullNumber() : "-"));
            });
            return new RpcGetServerState(srvs);
        }
    };

    /**
     * Получить описание состояния очередей для пользователя.
     */
    final Task getSelfServicesTask = new Task(Uses.TASK_GET_SELF_SERVICES) {

        private final RpcGetSelfSituation DUMMY = new RpcGetSelfSituation(new RpcGetSelfSituation.SelfSituation());

        @Override
        public RpcGetSelfSituation process(CmdParams cmdParams, String ipAdress, byte[] ip) {
            super.process(cmdParams, ipAdress, ip);
            final QUser user = QUserList.getInstance().getById(cmdParams.userId);
            //от юзера может приехать новое название его кабинета, ну пересел чувак.
            if (cmdParams.textData != null && !cmdParams.textData.equals("")) {
                user.setPoint(cmdParams.textData);
            }
            long stateH = 0; // это хэш всей обстановки по услуге для пользователя.
            final LinkedList<RpcGetSelfSituation.SelfService> servs = new LinkedList<>();
            for (QPlanService planService : user.getPlanServices()) {
                final QService service = QServiceTree.getInstance().getById(planService.getService().getId());
                servs.add(new RpcGetSelfSituation.SelfService(service, service.getCountCustomers(), planService.getCoefficient(),
                        planService.getFlexible_coef(), planService.getFlexible_invitation()));
                stateH = stateH + service.getId() + service.getCountCustomers() * (planService.getCoefficient() + 17);
            }
            // нужно сделать вставочку приглашенного юзера, если он есть
            stateH = stateH
                    + (user.getCustomer() == null ? -1703 : (user.getCustomer().getId() + user.getCustomer().getState().ordinal() * 747))
                    + ServerProps.getInstance().getProps().getLimitRecall()
                    + (user.getShadow() == null ? -147 : user.getShadow().getOldNom());
            for (QCustomer cu : QPostponedList.getInstance().getPostponedCustomers()) {
                stateH = stateH + cu.getId() + cu.getState().ordinal() * 117 + cu.getPostponedStatus().hashCode();
            }
            final Long hash = hashState.get(cmdParams.userId);
            if (hash == null) {
                hashState.put(cmdParams.userId, stateH);
            } else if (hash.equals(stateH)) {
                if (cmdParams.requestBack == null || !cmdParams.requestBack) {
                    return DUMMY;
                }
            } else {
                hashState.put(cmdParams.userId, stateH);
            }
            return new RpcGetSelfSituation(new RpcGetSelfSituation.SelfSituation(servs,
                    user.getCustomer(),
                    new LinkedList(user.getParallelCustomers().values()),
                    QPostponedList.getInstance().getPostponedCustomers(),
                    ServerProps.getInstance().getProps().getLimitRecall(),
                    ServerProps.getInstance().getProps().getExtPriorNumber(),
                    user.getShadow()));
        }
    };
    /**
     * Тут хранятся хэши последней отосланной юзеру ситуации чтоб одинаковую ситуацию не гонять дублируя уже отправленное
     */
    public final HashMap<Long, Long> hashState = new HashMap<>();
    /**
     * Получить описание состояния очередей для пользователя и проверить Отсечем дубляжи запуска от одних и тех же юзеров. но с разных компов
     */
    final Task getCheckSelfTask = new Task(Uses.TASK_GET_SELF_SERVICES_CHECK) {
        // надо запоминать название пунктов приема из БД для юзеров, не то перетрется клиентской настройкой и не восстановить

        final HashMap<QUser, String> points = new HashMap<>();

        @Override
        public synchronized RpcGetBool process(CmdParams cmdParams, String ipAdress, byte[] ip) {
            if (!QSessions.getInstance().check(cmdParams.userId, ipAdress, ip)) {
                QLog.l().logger().debug(cmdParams.userId + " ACCESS_DENY from " + ipAdress);
                return new RpcGetBool(false);
            }
            hashState.remove(cmdParams.userId);
            super.process(cmdParams, ipAdress, ip);
            //от юзера может приехать новое название его кабинета, ну пересел чувак.
            if (points.get(QUserList.getInstance().getById(cmdParams.userId)) == null) {
                points.put(QUserList.getInstance().getById(cmdParams.userId), QUserList.getInstance().getById(cmdParams.userId).getPoint());
            } else {
                QUserList.getInstance().getById(cmdParams.userId).setPoint(points.get(QUserList.getInstance().getById(cmdParams.userId)));
            }
            return new RpcGetBool(true);
        }
    };
    /**
     * Получить состояние пула отложенных
     */
    final Task getPostponedPoolInfo = new Task(Uses.TASK_GET_POSTPONED_POOL) {

        @Override
        public synchronized RpcGetPostponedPoolInfo process(CmdParams cmdParams, String ipAdress, byte[] ip) {
            super.process(cmdParams, ipAdress, ip);
            return new RpcGetPostponedPoolInfo(QPostponedList.getInstance().getPostponedCustomers());
        }
    };
    /**
     * Получить список забаненных
     */
    final Task getBanList = new Task(Uses.TASK_GET_BAN_LIST) {

        @Override
        public RpcBanList process(CmdParams cmdParams, String ipAdress, byte[] ip) {
            super.process(cmdParams, ipAdress, ip);
            RpcBanList.getInstance().udo(null);
            return RpcBanList.getInstance();
        }
    };
    /**
     * Удалить вызванного юзером кастомера по неявке.
     */
    final Task killCustomerTask = new Task(Uses.TASK_KILL_NEXT_CUSTOMER) {

        @Override
        public AJsonRPC20 process(CmdParams cmdParams, String ipAdress, byte[] ip) {
            super.process(cmdParams, ipAdress, ip);
            final QUser user = QUserList.getInstance().getById(cmdParams.userId);
            //переключение на кастомера при параллельном приеме, должен приехать customerID
            if (cmdParams.customerId != null) {
                final QCustomer parallelCust = user.getParallelCustomers().get(cmdParams.customerId);
                if (parallelCust == null) {
                    QLog.l().logger().warn("PARALLEL: User have no Customer for switching by customer ID=\"" + cmdParams.customerId + "\"" + " " + ipAdress);
                } else {
                    user.setCustomer(parallelCust);
                    QLog.l().logger().debug("Юзер \"" + user + "\" переключился на кастомера \"" + parallelCust.getFullNumber() + "\"" + " " + ipAdress);
                }
            }
            QLog.l().logger().warn("УДАЛЕНИЕ: Удалили по неявке кастомера " + user.getCustomer().getPrefix() + "-" + user.getCustomer().getNumber() + " он ввел \"" + user.getCustomer().getInput_data() + "\"" + " " + ipAdress);
            QLog.l().logger().warn("REMOVING: Customer was removing because of absence " + user.getCustomer().getPrefix() + "-" + user.getCustomer().getNumber() + " customer inputted \"" + user.getCustomer().getInput_data() + "\"" + " " + ipAdress);
            // Если кастомер имел что-то введенное на пункте регистрации, то удалить всех таких кастомеров с такими введеными данными
            // и отправить его в бан, ибо нехрен набирать кучу талонов и просирать очереди.
            if (user.getCustomer().getInput_data() != null && !"".equals(user.getCustomer().getInput_data())) {
                int cnt = 0;
                for (QService service : QServiceTree.getInstance().getNodes()) {
                    final LinkedList<QCustomer> for_del = new LinkedList<>();
                    service.getClients().stream().filter(customer -> (user.getCustomer().getInput_data().equals(customer.getInput_data()))).forEach(customer -> for_del.add(customer));
                    for_del.stream().forEach(qCustomer -> service.removeCustomer(qCustomer));
                    cnt = cnt + for_del.size();
                }
                if (cnt != 0) {
                    RpcBanList.getInstance().addToBanList(user.getCustomer().getInput_data());
                }
                QLog.l().logger().debug("Вместе с кастомером " + user.getCustomer().getPrefix() + "-" + user.getCustomer().getNumber() + " он ввел \"" + user.getCustomer().getInput_data() + "\" удалили еще его " + cnt + " проявлений." + " " + ipAdress);
            }

            // кастомер переходит в состояние "умерщвленности"
            KILLED_CUSTOMERS.put(user.getCustomer().getFullNumber().toUpperCase(), new Date());
            user.getCustomer().setState(CustomerState.STATE_DEAD);
            try {
                user.setCustomer(null);//бобик сдох и медальки не осталось
                // сохраняем состояния очередей.
                QServer.savePool();
                //разослать оповещение о том, что посетитель откланен
                // Должно подтереться основном табло
                MainBoard.getInstance().killCustomer(user);
            } catch (Exception ex) {
                QLog.l().logger().error(ex);
            }
            return new JsonRPC20OK();
        }
    };
    private static final HashMap<String, Date> KILLED_CUSTOMERS = new HashMap<>();
    /**
     * Начать работу с вызванноым кастомером.
     */
    final Task getStartCustomerTask = new Task(Uses.TASK_START_CUSTOMER) {

        @Override
        public AJsonRPC20 process(CmdParams cmdParams, String ipAdress, byte[] ip) {
            super.process(cmdParams, ipAdress, ip);
            final QUser user = QUserList.getInstance().getById(cmdParams.userId);
            // Время старта работы с юзера с кастомером.
            if (user.getCustomer() == null) {
                QLog.l().logger().error("Не найден кастомер для начала работы с ним. А должен быть! Возможно из-за параллельного приема. user=" + user.getName() + "-" + user.getId() + " " + ipAdress);
                return new JsonRPC20Error(REQUIRED_CUSTOMER_NOT_FOUND, ipAdress + " " + user.toString() + "/" + user.getId());
            }
            user.getCustomer().setStartTime(new Date());
            user.getCustomer().setPostponPeriod(0);
            // кастомер переходит в состояние "Начала обработки" или "Продолжение работы"
            user.getCustomer().setState(user.getCustomer().getState() == CustomerState.STATE_INVITED ? CustomerState.STATE_WORK : CustomerState.STATE_WORK_SECONDARY);
            MainBoard.getInstance().workCustomer(user);
            // сохраняем состояния очередей.
            QServer.savePool();
            return new JsonRPC20OK();
        }
    };
    /**
     * Перемещение вызванного юзером кастомера в пул отложенных.
     */
    final Task customerToPostponeTask = new Task(Uses.TASK_CUSTOMER_TO_POSTPON) {

        @Override
        public AJsonRPC20 process(CmdParams cmdParams, String ipAdress, byte[] ip) {
            super.process(cmdParams, ipAdress, ip);
            // вот он все это творит
            final QUser user = QUserList.getInstance().getById(cmdParams.userId);
            //переключение на кастомера при параллельном приеме, должен приехать customerID
            if (cmdParams.customerId != null) {
                final QCustomer parallelCust = user.getParallelCustomers().get(cmdParams.customerId);
                if (parallelCust == null) {
                    QLog.l().logger().warn("PARALLEL: User have no Customer for switching by customer ID=\"" + cmdParams.customerId + "\"" + " " + ipAdress);
                    // но нам могут разрешить найти и отложить вообще любого кастомера из всей толпы
                    if (cmdParams.strict != null && !cmdParams.strict) {
                        CLIENT_TASK_LOCK.lock();
                        try {
                            QLog.l().logger().debug("Warning! Random postponing was detected! \"" + cmdParams.customerId + "\"" + " " + ipAdress);
                            // бежим по очередям и ищем первого кастомера c ID
                            QCustomer customer = null;
                            for (QService service : QServiceTree.getInstance().getNodes()) {
                                if ((customer = service.gnawOutCustomerById(cmdParams.customerId)) != null) {
                                    QLog.l().logger().debug("Warning!  Random postponing was successed! \"" + cmdParams.customerId + "\"" + " " + ipAdress);
                                    user.setCustomer(customer);
                                    //Но кастомер мог быть из услуги, которую не видит этот юзер, надо поменять услугу.
                                    if (!user.hasService(customer.getService())) {
                                        customer.setService(user.getPlanServices().get(0).getService());
                                    }
                                    customer.setState(CustomerState.STATE_INVITED);
                                    customer.setState(CustomerState.STATE_WORK);
                                    break;
                                }
                            }
                            // А вдруг он в отложенных лежит. Найти его и проставить пришедший признак isMain, вдруг его просто сделали общедоступным или своим.
                            if ((customer = QPostponedList.getInstance().getById(cmdParams.customerId)) != null) {
                                customer.setPostponedStatus(cmdParams.textData);
                                customer.setIsMine(cmdParams.isMine);
                                //разослать оповещение о том, что посетителя вызвали, состояние очереди изменилось
                                //рассылаем широковещетельно по UDP на определенный порт
                                Uses.sendUDPBroadcast(Uses.TASK_REFRESH_POSTPONED_POOL, ServerProps.getInstance().getProps().getClientPort());
                                return new JsonRPC20OK();
                            }
                        } catch (Exception ex) {
                            throw new ServerException("Ошибка при постановке произвольного клиента в отложенные" + " " + ipAdress + ex);
                        } finally {
                            CLIENT_TASK_LOCK.unlock();
                        }
                    }
                } else {
                    user.setCustomer(parallelCust);
                    QLog.l().logger().debug("Юзер \"" + user + "\" переключился на кастомера \"" + parallelCust.getFullNumber() + "\"" + " " + ipAdress);
                }
            }
            // вот над этим пациентом
            final QCustomer customer = user.getCustomer();
            if (customer == null) {
                // Такое может быть если начать откладывать валюнтаристски.
                return new JsonRPC20Error(REQUIRED_CUSTOMER_NOT_FOUND);
            }
            // статус
            customer.setPostponedStatus(cmdParams.textData);
            // на сколько отложили. 0 - бессрочно
            customer.setPostponPeriod(cmdParams.postponedPeriod);
            // если отложили бессрочно и поставили галку, то можно видеть только отложенному
            customer.setIsMine(cmdParams.isMine);
            // в этом случае завершаем с пациентом
            //"все что хирург забыл в вас - в пул отложенных"
            // но сначала обозначим результат работы юзера с кастомером, если такой результат найдется в списке результатов
            // кастомер переходит в состояние "Завершенности", но не "мертвости"
            customer.setState(CustomerState.STATE_POSTPONED);
            try {
                user.setCustomer(null);//бобик сдох но медалька осталось, отправляем в пулл
                customer.setUser(null);
                QPostponedList.getInstance().addElement(customer);
                // сохраняем состояния очередей.
                QServer.savePool();
                //разослать оповещение о том, что посетитель отложен
                Uses.sendUDPBroadcast(Uses.TASK_REFRESH_POSTPONED_POOL, ServerProps.getInstance().getProps().getClientPort());
                //рассылаем широковещетельно по UDP на определенный порт. Должно высветитьсяна основном табло
                MainBoard.getInstance().killCustomer(user);
            } catch (Exception t) {
                QLog.l().logger().error("Загнулось под конец." + " " + ipAdress, t);
            }

            return new JsonRPC20OK();
        }
    };
    /**
     * Изменение отложенному кастомеру статуса
     */
    final Task postponCustomerChangeStatusTask = new Task(Uses.TASK_POSTPON_CHANGE_STATUS) {

        @Override
        public AJsonRPC20 process(CmdParams cmdParams, String ipAdress, byte[] ip) {
            super.process(cmdParams, ipAdress, ip);
            final QCustomer cust = QPostponedList.getInstance().getById(cmdParams.customerId);
            if (cust != null) {
                cust.setPostponedStatus(cmdParams.textData);
                //разослать оповещение о том, что посетителя вызвали, состояние очереди изменилось
                //рассылаем широковещетельно по UDP на определенный порт
                Uses.sendUDPBroadcast(Uses.TASK_REFRESH_POSTPONED_POOL, ServerProps.getInstance().getProps().getClientPort());
                return new JsonRPC20OK();
            } else {
                return new JsonRPC20Error(POSTPONED_NOT_FOUND, cmdParams.customerId);
            }

        }
    };
    /**
     * Закончить работу с вызванноым кастомером.
     */
    final Task getFinishCustomerTask = new Task(Uses.TASK_FINISH_CUSTOMER) {

        @Override
        public AJsonRPC20 process(CmdParams cmdParams, String ipAdress, byte[] ip) {
            super.process(cmdParams, ipAdress, ip);
            // вот он все это творит
            final QUser user = QUserList.getInstance().getById(cmdParams.userId);
            //переключение на кастомера при параллельном приеме, должен приехать customerID
            if (cmdParams.customerId != null) {
                final QCustomer parallelCust = user.getParallelCustomers().get(cmdParams.customerId);
                if (parallelCust == null) {
                    QLog.l().logger().warn("PARALLEL: User have no Customer for switching by customer ID=\"" + cmdParams.customerId + "\"" + " " + ipAdress);
                } else {
                    user.setCustomer(parallelCust);
                    QLog.l().logger().debug("Юзер \"" + user + "\" переключился на кастомера \"" + parallelCust.getFullNumber() + "\"" + " " + ipAdress);
                }
            }
            // вот над этим пациентом
            final QCustomer customer = user.getCustomer();
            if (customer == null) {
                QLog.l().logger().error("Не найден кастомер для завершении работы с ним. А должен быть! Возможно из-за параллельного приема. user=" + user.getName() + "-" + user.getId() + " " + ipAdress);
                return new JsonRPC20Error(REQUIRED_CUSTOMER_NOT_FOUND, ipAdress + " " + user.toString() + "/" + user.getId());
            }
            // комменты
            customer.setTempComments(cmdParams.textData);
            // надо посмотреть не требует ли этот кастомер возврата в какую либо очередь.
            final QService backSrv = user.getCustomer().getServiceForBack();
            if (backSrv != null) {
                QLog.l().logger().debug("Требуется возврат после редиректа." + " " + ipAdress);
                // действия по завершению работы юзера над кастомером
                customer.setFinishTime(new Date());
                // кастомер переходит в состояние "возврата", тут еще и в базу скинется, если надо.
                customer.setState(CustomerState.STATE_BACK, backSrv.getId());
                // переставить кастомера в очередь к пункту возврата
                backSrv.addCustomer(customer);
                // надо кастомера инициализить др. услугой

                // Поставил кастомеру юзера, который его вызвал.
                // юзер в другой очереди наверное другой
                customer.setUser(null);
                // теперь стоит к новой услуги.
                customer.setService(backSrv);

                //разослать оповещение о том, что появился посетитель после редиректа
                //рассылаем широковещетельно по UDP на определенный порт
                Uses.sendUDPBroadcast(backSrv.getId().toString(), ServerProps.getInstance().getProps().getClientPort());
                QLog.l().logger().info("Клиент \"" + user.getCustomer().getPrefix() + user.getCustomer().getNumber() + "\" возвращен к услуге \"" + backSrv.getName() + "\"" + " " + ipAdress);
            } else {
                QLog.l().logger().debug("В морг пациента." + " " + ipAdress);

                // в этом случае завершаем с пациентом
                //"все что хирург забыл в вас - ваше"
                // но сначала обозначим результат работы юзера с кастомером, если такой результат найдется в списке результатов
                // может приехать -1 если результат не требовался
                final QResult result;
                if (cmdParams.resultId != -1) {
                    result = QResultList.getInstance().getById(cmdParams.resultId);
                } else {
                    result = null;
                }
                ((QCustomer) customer).setResult(result);
                customer.setFinishTime(new Date());
                // кастомер переходит в состояние "Завершенности", но не "мертвости"
                customer.setState(CustomerState.STATE_FINISH);
                // дело такое, кастомер может идти по списку услуг, т.е. есть набор услуг, юзер завершает работу, а система его ведет по списку услуг
                // тут посмотрим может его уже провели по списку комплексных услуг, если у него вообще они были
                // если провели то у него статус другой STATE_WAIT_COMPLEX_SERVICE
                // если нет, то статус STATE_FINISH и список в комплексных не пуст
                // провести к другой услуге его могли в плагине IChangeCustomerStateEvent
                // если нет то дефолтная проводочка в следующую услугу.
                if (customer.getState() == CustomerState.STATE_FINISH && customer.getComplexId() != null) {
                    int len = 0;
                    len = customer.getComplexId().stream().map(li -> li.size()).reduce(len, Integer::sum);
                    if (len != 0) {
                        QLog.l().logger().debug("Дефолтная проводка по комплексным услугам. Оcталось " + len + " " + ipAdress);
                        Long serviceID = null;
                        for (LinkedList<LinkedList<Long>> ids : customer.getComplexId()) {
                            if (!ids.isEmpty()) {
                                final LinkedList<Long> id = ids.getFirst();
                                serviceID = id.getFirst();
                                if (serviceID != null) {
                                    ids.remove(id);
                                    break;
                                }
                            }
                        }

                        if (serviceID == null) {
                            return new JsonRPC20Error(customer.getNumber(), "No service for customer " + customer);
                        }
                        final QService nextServ = QServiceTree.getInstance().getById(serviceID);
                        nextServ.addCustomer(customer);
                        customer.setService(nextServ);
                        customer.setState(CustomerState.STATE_WAIT_COMPLEX_SERVICE);
                    }
                }
                // если же всетаки проводка по этапу, то оповестить следующих юзеров что к ним пришел
                if (customer.getState() == CustomerState.STATE_WAIT_COMPLEX_SERVICE) {
                    //разослать оповещение о том, что появился посетитель после редиректа
                    //рассылаем широковещетельно по UDP на определенный порт
                    Uses.sendUDPBroadcast(customer.getService().getId().toString(), ServerProps.getInstance().getProps().getClientPort());
                    QLog.l().logger().info("Клиент \"" + customer.getPrefix() + customer.getNumber() + "\" проведен по этапу к услуге \"" + customer.getService().getName() + "\"" + " " + ipAdress);

                }

            }
            try {
                user.setCustomer(null);//бобик сдох и медальки не осталось
                // сохраняем состояния очередей.
                QServer.savePool();
                //разослать оповещение о том, что посетитель откланен
                //рассылаем широковещетельно по UDP на определенный порт. Должно высветитьсяна основном табло
                MainBoard.getInstance().killCustomer(user);
            } catch (Exception ex) {
                QLog.l().logger().error(ex);
            }
            return new RpcStandInService(customer);
        }
    };
    /**
     * Переадресовать клиента к другой услуге.
     */
    final Task redirectCustomerTask = new Task(Uses.TASK_REDIRECT_CUSTOMER) {

        @Override
        public AJsonRPC20 process(CmdParams cmdParams, String ipAdress, byte[] ip) {
            super.process(cmdParams, ipAdress, ip);
            final QUser user = QUserList.getInstance().getById(cmdParams.userId);
            //переключение на кастомера при параллельном приеме, должен приехать customerID
            if (cmdParams.customerId != null) {
                final QCustomer parallelCust = user.getParallelCustomers().get(cmdParams.customerId);
                if (parallelCust == null) {
                    QLog.l().logger().warn("PARALLEL: User have no Customer for switching by customer ID=\"" + cmdParams.customerId + "\"" + " " + ipAdress);
                } else {
                    user.setCustomer(parallelCust);
                    QLog.l().logger().debug("Юзер \"" + user + "\" переключился на кастомера \"" + parallelCust.getFullNumber() + "\"" + " " + ipAdress);
                }
            }
            final QCustomer customer = user.getCustomer();
            // комменты по редиректу
            customer.setTempComments(cmdParams.textData);
            // Переставка в другую очередь
            // Название старой очереди
            final QService oldService = customer.getService();
            // вот она новая очередь.
            final QService newServiceR = QServiceTree.getInstance().getById(cmdParams.serviceId);
            final QService newService = newServiceR.getLink() != null ? newServiceR.getLink() : newServiceR;
            // действия по завершению работы юзера над кастомером
            // Время завершения проставится при сменен статуса.
            // кастомер переходит в состояние "перенаправленности", тут еще и в базу скинется, если надо.
            // но сначала обозначим результат работы юзера с кастомером, если такой результат найдется в списке результатов
            // может приехать -1 если результат не требовался
            final QResult result;
            if (cmdParams.resultId != -1) {
                result = QResultList.getInstance().getById(cmdParams.resultId);
            } else {
                result = null;
            }
            customer.setResult(result);
            customer.setState(CustomerState.STATE_REDIRECT, cmdParams.serviceId);// есть все еще старая услуга и новую как ID передали
            // надо кастомера инициализить др. услугой
            // юзер в другой очереди наверное другой
            customer.setUser(null);
            // теперь стоит к новой услуги.
            customer.setService(newService);
            // если редиректят в прежнюю услугу, то это по факту не ридирект(иначе карусель)
            // по этому в таком случае кастомера отправляют в конец очереди к этой же услуге.
            // для этого просто не учитываем смену приоритета и галку возврата. 
            if (!oldService.getId().equals(cmdParams.serviceId)) {
                // т.к. переставленный, то надо поменять ему приоритет.
                customer.setPriority(Uses.PRIORITY_HI);
                // при редиректе надо убрать у кастомера признак старого юзера, время начала обработки.
                //это произойдет далее при вызове setCustomer(null).
                // и добавить, если надо, пункт возврата.
                // теперь пункт возврата
                if (cmdParams.requestBack) { // требует ли возврата в прежнюю очередь
                    customer.addServiceForBack(oldService);
                }
            }
            // только что встал типо, время постановки проставилось при сменен состояния
            //С НАЧАЛА ПОДОТРЕМ ПОТОМ ПЕРЕСТАВИМ!!!
            //с новым приоритетом ставим в новую очередь, приоритет должет
            //позволить вызваться ему сразу за обрабатываемыми кастомерами
            newService.addCustomer(customer);
            user.setCustomer(null);//бобик сдох и медальки не осталось, воскрес вместе со старой медалькой в соседней очереди

            try {
                // сохраняем состояния очередей.
                QServer.savePool();
                //разослать оповещение о том, что появился посетитель
                //рассылаем широковещетельно по UDP на определенный порт
                Uses.sendUDPBroadcast(newService.getId().toString(), ServerProps.getInstance().getProps().getClientPort());
                Uses.sendUDPBroadcast(oldService.getId().toString(), ServerProps.getInstance().getProps().getClientPort());
                //разослать оповещение о том, что посетитель откланен
                //рассылаем широковещетельно по UDP на определенный порт. Должно подтереться на основном табло
                MainBoard.getInstance().killCustomer(user);
            } catch (Exception ex) {
                QLog.l().logger().error(ex);
            }
            return new JsonRPC20OK();
        }
    };
    /**
     * Привязка услуги пользователю на горячую по команде. Это обработчик этой команды.
     */
    final Task setServiceFire = new Task(Uses.TASK_SET_SERVICE_FIRE) {

        @Override
        public synchronized RpcGetSrt process(CmdParams cmdParams, String ipAdress, byte[] ip) {
            super.process(cmdParams, ipAdress, ip);
            if (cmdParams.userId == null || cmdParams.serviceId == null) {
                return new RpcGetSrt("Неверные попараметры запроса." + " " + ipAdress);
            }
            if (!QServiceTree.getInstance().hasById(cmdParams.serviceId)) {
                return new RpcGetSrt("Требуемая услуга не присутствует в текущей загруженной конфигурации сервера." + " " + ipAdress);
            }
            final QService service = QServiceTree.getInstance().getById(cmdParams.serviceId);
            if (!QUserList.getInstance().hasById(cmdParams.userId)) {
                return new RpcGetSrt("Требуемый пользователь не присутствует в текущей загруженной конфигурации сервера." + " " + ipAdress);
            }
            final QUser user = QUserList.getInstance().getById(cmdParams.userId);

            if (user.hasService(cmdParams.serviceId)) {
                return new RpcGetSrt("Требуемая услуга уже назначена этому пользователю." + " " + ipAdress);
            }
            user.addPlanService(service, cmdParams.coeff);
            //разослать оповещение о том, что у пользователя поменялась конфигурация услуг
            //рассылаем широковещетельно по UDP на определенный порт
            Uses.sendUDPBroadcast(String.valueOf(cmdParams.userId), ServerProps.getInstance().getProps().getClientPort());
            return new RpcGetSrt("Услуга \"" + cmdParams.serviceId + "\" назначена пользователю \"" + cmdParams.userId + "\" успешно." + " " + ipAdress);
        }
    };
    /**
     * Удаление привязка услуги пользователю на горячую по команде. Это обработчик этой команды.
     */
    final Task deleteServiceFire = new Task(Uses.TASK_DELETE_SERVICE_FIRE) {

        @Override
        synchronized public RpcGetSrt process(CmdParams cmdParams, String ipAdress, byte[] ip) {
            super.process(cmdParams, ipAdress, ip);
            if (cmdParams.userId == null || cmdParams.serviceId == null) {
                return new RpcGetSrt("Неверные попараметры запроса." + " " + ipAdress);
            }
            if (!QServiceTree.getInstance().hasById(cmdParams.serviceId)) {
                return new RpcGetSrt("Требуемая услуга не присутствует в текущей загруженной конфигурации сервера." + " " + ipAdress);
            }
            if (!QUserList.getInstance().hasById(cmdParams.userId)) {
                return new RpcGetSrt("Требуемый пользователь не присутствует в текущей загруженной конфигурации сервера." + " " + ipAdress);
            }
            final QUser user = QUserList.getInstance().getById(cmdParams.userId);

            if (!user.hasService(cmdParams.serviceId)) {
                return new RpcGetSrt("Требуемая услуга не назначена этому пользователю." + " " + ipAdress);
            }
            user.deletePlanService(cmdParams.serviceId);
            //разослать оповещение о том, что у пользователя поменялась конфигурация услуг
            //рассылаем широковещетельно по UDP на определенный порт
            Uses.sendUDPBroadcast(String.valueOf(cmdParams.userId), ServerProps.getInstance().getProps().getClientPort());
            return new RpcGetSrt("Услуга \"" + cmdParams.serviceId + "\" удалена у пользователя \"" + cmdParams.userId + "\" успешно." + " " + ipAdress);
        }
    };
    /**
     * Изменить временную доступность услуги для оказания
     */
    final Task changeTempAvailableService = new Task(Uses.TASK_CHANGE_TEMP_AVAILABLE_SERVICE) {

        @Override
        synchronized public AJsonRPC20 process(CmdParams cmdParams, String ipAdress, byte[] ip) {
            super.process(cmdParams, ipAdress, ip);
            if (!QServiceTree.getInstance().hasById(cmdParams.serviceId)) {
                return new JsonRPC20Error();
            }
            final QService service = QServiceTree.getInstance().getById(cmdParams.serviceId);
            service.setTempReasonUnavailable(cmdParams.textData);
            QLog.l().logger().trace("Изменена доступность для услуги \"" + service.getName() + "\" - "
                    + (cmdParams.textData == null || cmdParams.textData.isEmpty() ? "РАБОТАЕТ" : ("не работает, причина '" + cmdParams.textData + "'")) + " " + ipAdress);
            return new JsonRPC20OK();
        }
    };
    /**
     * Получение конфигурации главного табло - ЖК или плазмы. Это XML-файл лежащий в папку приложения mainboard.xml
     */
    final Task getBoardConfig = new Task(Uses.TASK_GET_BOARD_CONFIG) {

        @Override
        public RpcGetSrt process(CmdParams cmdParams, String ipAdress, byte[] ip) {
            super.process(cmdParams, ipAdress, ip);
            return new RpcGetSrt(MainBoard.getInstance().getConfig().asXML());
        }
    };
    /**
     * Сохранение конфигурации главного табло - ЖК или плазмы. Это XML-файл лежащий в папку приложения mainboard.xml
     */
    final Task saveBoardConfig = new Task(Uses.TASK_SAVE_BOARD_CONFIG) {

        @Override
        synchronized public AJsonRPC20 process(CmdParams cmdParams, String ipAdress, byte[] ip) {
            super.process(cmdParams, ipAdress, ip);
            try {
                MainBoard.getInstance().saveConfig(DocumentHelper.parseText(cmdParams.textData).getRootElement());
            } catch (DocumentException ex) {
                QLog.l().logger().error("Не сохранилась конфигурация табло." + " " + ipAdress, ex);
            }
            return new JsonRPC20OK();
        }
    };
    /**
     * Получение таблици записанных ранее клиентов на день.
     */
    final Task getGridOfDay = new Task(Uses.TASK_GET_GRID_OF_DAY) {

        @Override
        public RpcGetGridOfDay process(final CmdParams cmdParams, String ipAdress, byte[] ip) {
            super.process(cmdParams, ipAdress, ip);
            //Определим услугу
            final QService service = QServiceTree.getInstance().getById(cmdParams.serviceId);
            final QSchedule sch1 = service.getCalendar() == null
                    ? QCalendarList.getInstance().getById(1).getSpecSchedule(new Date(cmdParams.date))
                    : (service.getCalendar().getSpecSchedule(new Date(cmdParams.date)));
            final QSchedule sch = (sch1 == null ? service.getSchedule() : sch1);

            final RpcGetGridOfDay.GridDayAndParams advCusts = new RpcGetGridOfDay.GridDayAndParams();
            advCusts.setAdvanceLimit(service.getAdvanceLimit());
            if (sch == null) {
                return new RpcGetGridOfDay(advCusts);
            }

            final GregorianCalendar gc = new GregorianCalendar();
            gc.setTime(new Date(cmdParams.date));
            gc.set(GregorianCalendar.HOUR_OF_DAY, 0);
            gc.set(GregorianCalendar.MINUTE, 0);
            final Date startDay = gc.getTime();
            gc.set(GregorianCalendar.HOUR_OF_DAY, 23);
            gc.set(GregorianCalendar.MINUTE, 59);
            final Date endDay = gc.getTime();

            // Определим по календарю рабочий ли день.
            // Календаря может быть два, общий с id=1 и персонально настроенный
            // Если день определяется как выходной(присутствует в БД в таблице выходных дней), то переходим к следующему дню
            if (!QCalendarList.getInstance().getById(1).checkFreeDay(startDay)
                    && !(service.getCalendar() != null
                    && service.getCalendar().checkFreeDay(startDay))) {

                // Определим время начала и нонца работы на этот день
                final QSchedule.Interval interval = sch.getWorkInterval(gc.getTime());

                // Если работаем в этот день то определим часы на которые еще можно записаться
                if (!(interval.start == null || interval.finish == null)) {
                    // Сдвинем на интервал края дня т.к. это так же сдвинуто на пунктe регистрации
                    // Такой сдвиг в трех местах. Тут при формировании свободных времен, при определении раскладки панелек на простыню выбора,
                    // при проверки доступности когда всю неделю отрисовываем
                    gc.setTime(interval.start);
                    gc.add(GregorianCalendar.MINUTE, service.getAdvanceTimePeriod());
                    Date start = gc.getTime();
                    gc.setTime(interval.finish);
                    gc.add(GregorianCalendar.MINUTE, -service.getAdvanceTimePeriod());
                    Date end = gc.getTime();

                    QLog.l().logger().trace("Загрузим уже занятых позиций ранее записанными кастомерами от " + Uses.FORMAT_FOR_REP.format(startDay) + " до " + Uses.FORMAT_FOR_REP.format(endDay) + " " + ipAdress);
                    // Загрузим уже занятых позиций ранее записанными кастомерами
                    final List<QAdvanceCustomer> advCustomers = Spring.getInstance().getHt().find("FROM QAdvanceCustomer a WHERE advance_time >'" + Uses.FORMAT_FOR_REP.format(startDay) + "' and advance_time <= '" + Uses.FORMAT_FOR_REP.format(endDay) + "' and service_id = " + service.getId());

                    // бежим по часам внутри дня
                    while (start.before(end) || start.equals(end)) {
                        // Проверка на перерыв. В перерывах нет возможности записываться, по этому это время не поедет в пункт регистрации
                        gc.setTime(start);
                        gc.add(GregorianCalendar.MINUTE, service.getAdvanceTimePeriod() - 3);
                        if (!sch.inBreak(start, gc.getTime())) { // время не попало в перерыв
                            int cnt = 0;

                            gc.setTime(start);
                            gc.set(GregorianCalendar.SECOND, 0);
                            gc.set(GregorianCalendar.MILLISECOND, 0);
                            RpcGetGridOfDay.AdvTime atime = new RpcGetGridOfDay.AdvTime(gc.getTime()); //оно уже есть, добавим записанных и дополним свободными местами

                            // пробигаем по кастомерам записанным
                            for (QAdvanceCustomer advCustomer : advCustomers) {
                                gc.setTime(start);
                                final int s = gc.get(GregorianCalendar.HOUR_OF_DAY);
                                final int s_m = gc.get(GregorianCalendar.MINUTE);
                                gc.setTime(advCustomer.getAdvanceTime());
                                final int e = gc.get(GregorianCalendar.HOUR_OF_DAY);
                                final int e_m = gc.get(GregorianCalendar.MINUTE);
                                // Если совпал день и час и минуты, то увеличим счетчик записавшихся на этот час и минуты
                                // тут учитываем что реально время предварительного может быть не по сетке, к примеру загрузили извне.
                                if (s * 60 + s_m <= e * 60 + e_m
                                        && s * 60 + s_m + service.getAdvanceTimePeriod() > e * 60 + e_m) {
                                    cnt++;
                                    atime.addACustomer(advCustomer);
                                    // Защита от того чтобы один и тодже клиент не записался предварительно в одну услугу на одну дату.
                                    // данный предв.кастомер не должен быть таким же как и авторизовавшийся на этот час
                                    if (cmdParams.customerId != null && cmdParams.customerId != -1
                                            && advCustomer.getAuthorizationCustomer() != null
                                            && advCustomer.getAuthorizationCustomer().getId() != null
                                            && advCustomer.getAuthorizationCustomer().getId().equals(cmdParams.customerId)) {
                                        cnt = 1999999999;
                                        break;
                                    }
                                }
                            }
                            // если еще количество записавшихся не привысило ограничение по услуге, то добавил этот час как доступный для записи
                            for (int i = cnt; i < service.getAdvanceLimit(); i++) {
                                atime.addACustomer(new QAdvanceCustomer(0L));
                            }
                            advCusts.addTime(atime);
                        } // не в перерыве и по этому пробивали сколько там уже стояло и не первалило ли через настройку ограничения

                        // перейдем на следующий период
                        gc.setTime(start);
                        gc.add(GregorianCalendar.MINUTE, service.getAdvanceTimePeriod());
                        start = gc.getTime();
                    }

                }

            }
            return new RpcGetGridOfDay(advCusts);
        }
    };
    /**
     * Получение таблици записанных ранее клиентов на неделю.
     */
    final Task getGridOfWeek = new Task(Uses.TASK_GET_GRID_OF_WEEK) {

        @Override
        public RpcGetGridOfWeek process(final CmdParams cmdParams, String ipAdress, byte[] ip) {
            super.process(cmdParams, ipAdress, ip);
            //Определим услугу
            final QService service = QServiceTree.getInstance().getById(cmdParams.serviceId);
            QSchedule sch = service.getSchedule();
            if (sch == null) {
                return new RpcGetGridOfWeek(new RpcGetGridOfWeek.GridAndParams("Требуемая услуга не имеет расписания."));
            }

            final Date startWeek = new Date(cmdParams.date);
            final GregorianCalendar gc = new GregorianCalendar();
            gc.setTime(startWeek);
            gc.set(GregorianCalendar.DAY_OF_YEAR, gc.get(GregorianCalendar.DAY_OF_YEAR) + 7);
            final Date endWeek = gc.getTime();

            QLog.l().logger().trace("Загрузим уже занятых позиций ранее записанными кастомерами от " + Uses.FORMAT_FOR_REP.format(startWeek) + " до " + Uses.FORMAT_FOR_REP.format(endWeek) + " " + ipAdress);
            // Загрузим уже занятых позиций ранее записанными кастомерами
            final List<QAdvanceCustomer> advCustomers = Spring.getInstance().getHt().find("FROM QAdvanceCustomer a WHERE advance_time >'" + Uses.FORMAT_FOR_REP.format(startWeek) + "' and advance_time <= '" + Uses.FORMAT_FOR_REP.format(endWeek) + "' and service_id = " + service.getId());

            final GridAndParams advCusts = new GridAndParams();
            advCusts.setStartTime(ServerProps.getInstance().getProps().getStartTime());
            advCusts.setFinishTime(ServerProps.getInstance().getProps().getFinishTime());
            advCusts.setAdvanceLimit(service.getAdvanceLimit());
            advCusts.setAdvanceTimePeriod(service.getAdvanceTimePeriod());
            advCusts.setAdvanceLimitPeriod(service.getAdvanceLimitPeriod() == null ? 0 : service.getAdvanceLimitPeriod());
            // сформируем список доступных времен
            Date day = startWeek;
            while (day.before(endWeek)) {
                final GregorianCalendar gc_day = new GregorianCalendar();
                gc_day.setTime(day);
                // Определим по календарю рабочий ли день.
                // Календаря может быть два, общий с id=1 и персонально настроенный
                // Если день определяется как выходной(присутствует в БД в таблице выходных дней), то переходим к следующему дню
                if (!QCalendarList.getInstance().getById(1).checkFreeDay(day)
                        && !(service.getCalendar() != null
                        && service.getCalendar().checkFreeDay(day))) {
                    // Определим время начала и нонца работы на этот день/ расписания могут быть перекрыты в календаре
                    final QSchedule sch1 = service.getCalendar() == null
                            ? QCalendarList.getInstance().getById(1).getSpecSchedule(gc_day.getTime())
                            : (service.getCalendar().getSpecSchedule(gc_day.getTime()));
                    sch = (sch1 == null ? service.getSchedule() : sch1);
                    final QSchedule.Interval interval = sch.getWorkInterval(gc_day.getTime());

                    // Если работаем в этот день то определим часы на которые еще можно записаться
                    if (!(interval.start == null || interval.finish == null)) {
                        // Сдвинем на час края дня т.к. это так же сдвинуто на пунктe регистрации
                        // Такой сдвиг в трех местах. Тут при формировании свободных времен, при определении раскладки панелек на простыню выбора,
                        // при проверки доступности когда всю неделю отрисовываем
                        gc.setTime(interval.start);
                        gc.add(GregorianCalendar.MINUTE, service.getAdvanceTimePeriod());
                        Date start = gc.getTime();
                        gc.setTime(interval.finish);
                        gc.add(GregorianCalendar.MINUTE, -service.getAdvanceTimePeriod());
                        final Date end = gc.getTime();

                        // бежим по часам внутри дня
                        while (start.before(end) || start.equals(end)) {

                            // Проверка на перерыв. В перерывах нет возможности записываться, по этому это время не поедет в пункт регистрации
                            gc.setTime(start);
                            gc.add(GregorianCalendar.MINUTE, service.getAdvanceTimePeriod() - 3);
                            if (!sch.inBreak(start, gc.getTime())) { // время не попало в перерыв

                                int cnt = 0;
                                // пробигаем по кастомерам записанным
                                for (QAdvanceCustomer advCustomer : advCustomers) {
                                    gc.setTime(start);
                                    final int s = gc.get(GregorianCalendar.HOUR_OF_DAY);
                                    final int s_m = gc.get(GregorianCalendar.MINUTE);
                                    gc.setTime(advCustomer.getAdvanceTime());
                                    final int e = gc.get(GregorianCalendar.HOUR_OF_DAY);
                                    final int e_m = gc.get(GregorianCalendar.MINUTE);
                                    // Если совпал день и час и минуты, то увеличим счетчик записавшихся на этот час и минуты
                                    // тут учитываем что реально время предварительного может быть не по сетке, к примеру загрузили извне.
                                    if ((gc.get(GregorianCalendar.DAY_OF_YEAR) == gc_day.get(GregorianCalendar.DAY_OF_YEAR))
                                            && (s * 60 + s_m <= e * 60 + e_m
                                            && s * 60 + s_m + service.getAdvanceTimePeriod() > e * 60 + e_m)) {
                                        cnt++;
                                        // Защита от того чтобы один и тодже клиент не записался предварительно в одну услугу на одну дату.
                                        // данный предв.кастомер не должен быть таким же как и авторизовавшийся на этот час
                                        if (cmdParams.customerId != -1
                                                && advCustomer.getAuthorizationCustomer() != null
                                                && advCustomer.getAuthorizationCustomer().getId() != null
                                                && advCustomer.getAuthorizationCustomer().getId().equals(cmdParams.customerId)) {
                                            cnt = 1999999999;
                                            break;
                                        }
                                    }
                                }
                                // если еще количество записавшихся не привысило ограничение по услуге, то добавил этот час как доступный для записи
                                if (cnt < service.getAdvanceLimit()) {
                                    gc.setTime(day);
                                    final GregorianCalendar gc2 = new GregorianCalendar();
                                    gc2.setTime(start);
                                    gc.set(GregorianCalendar.HOUR_OF_DAY, gc2.get(GregorianCalendar.HOUR_OF_DAY));
                                    gc.set(GregorianCalendar.MINUTE, gc2.get(GregorianCalendar.MINUTE));
                                    gc.set(GregorianCalendar.SECOND, 0);
                                    gc.set(GregorianCalendar.MILLISECOND, 0);
                                    advCusts.addTime(gc.getTime());
                                }
                            } // не в перерыве и по этому пробивали сколько там уже стояло и не первалило ли через настройку ограничения

                            // перейдем на следующий час
                            gc.setTime(start);
                            gc.add(GregorianCalendar.MINUTE, service.getAdvanceTimePeriod());
                            start = gc.getTime();
                        }

                    }
                } // проверка на нерабочий день календаря
                // переход на следующий день
                gc_day.add(GregorianCalendar.DAY_OF_YEAR, 1);
                day = gc_day.getTime();
            }
            return new RpcGetGridOfWeek(advCusts);
        }
    };

    /**
     * Записать кастомера предварительно в услугу.
     */
    final Task standAdvanceInService = new Task(Uses.TASK_ADVANCE_STAND_IN) {

        @Override
        synchronized public RpcGetAdvanceCustomer process(CmdParams cmdParams, String ipAdress, byte[] ip) {
            super.process(cmdParams, ipAdress, ip);

            final QService service = QServiceTree.getInstance().getById(cmdParams.serviceId);
            QLog.l().logger().trace("Предварительно записываем c ID=" + cmdParams.customerId + " к услуге \"" + service.getName() + "\"(" + service.getPrefix() + "/" + cmdParams.serviceId + ")" + " ко времени " + new Date(cmdParams.date) + " " + ipAdress);
            // Создадим вновь испеченного кастомера
            final QAdvanceCustomer customer = new QAdvanceCustomer(cmdParams.textData);

            // Определим ID авторизованного пользователя, если небыло авторизации, то оно = -1
            final Long authCustonerID = cmdParams.customerId;
            // выкачаем из базы зарегинова
            QAuthorizationCustomer acust = new QAuthorizationCustomer();
            if (cmdParams.customerId != -1) {
                Spring.getInstance().getHt().load(acust, authCustonerID);
                if (acust.getId() == null || acust.getName() == null) {
                    throw new ServerException("Авторизация не успешна." + " " + ipAdress);
                }
            } else {
                acust = null;
            }
            customer.setAuthorizationCustomer(acust);
            // Определим дату и время для кастомера
            final GregorianCalendar gc = new GregorianCalendar();
            gc.setTime(new Date(cmdParams.date));
            gc.set(GregorianCalendar.SECOND, 0);
            gc.set(GregorianCalendar.MILLISECOND, 0);
            final Date startTime = gc.getTime();
            //хорошо бы отсекать повторную запись к этому же специалиста на этот же день
            customer.setAdvanceTime(startTime);
            customer.setService(service);
            customer.setComments(cmdParams.comments);
            // время постановки проставляется автоматом при создании кастомера.
            // Приоритет "как все"
            customer.setPriority(2);

            //сохраним нового предварительного пользователя
            QLog.l().logger().debug("Старт сохранения предварительной записи в СУБД." + " " + ipAdress);
            Spring.getInstance().getTt().execute(new TransactionCallbackWithoutResult() {

                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    try {
                        Spring.getInstance().getHt().saveOrUpdate(customer);
                        QLog.l().logger().debug("Сохранили." + " " + ipAdress);
                    } catch (Exception ex) {
                        QLog.l().logger().error("Ошибка при сохранении \n" + ex.toString() + "\n" + Arrays.toString(ex.getStackTrace()) + " " + ipAdress);
                        status.setRollbackOnly();
                    }
                }
            });
            return new RpcGetAdvanceCustomer(customer);
        }
    };
    /**
     * Поставить кастомера в очередь предварительно записанного. Проверить бронь, поставить или отказать.
     */
    final Task standAdvanceCheckAndStand = new Task(Uses.TASK_ADVANCE_CHECK_AND_STAND) {

        @Override
        public RpcStandInService process(CmdParams cmdParams, String ipAdress, byte[] ip) {
            super.process(cmdParams, ipAdress, ip);

            // Вытащим из базы предварительного кастомера
            final QAdvanceCustomer advCust = Spring.getInstance().getHt().get(QAdvanceCustomer.class, cmdParams.customerId);
            if (advCust == null || advCust.getId() == null || advCust.getAdvanceTime() == null) {
                QLog.l().logger().debug("не найден клиент по его ID=" + cmdParams.customerId + " " + ipAdress);
                // Шлем отказ
                return new RpcStandInService(null, "Не верный номер предварительной записи.");
            }
            final GregorianCalendar gc = new GregorianCalendar();
            gc.setTime(advCust.getAdvanceTime());
            gc.set(GregorianCalendar.HOUR_OF_DAY, gc.get(GregorianCalendar.HOUR_OF_DAY) - 1);
            final GregorianCalendar gc1 = new GregorianCalendar();
            gc1.setTime(advCust.getAdvanceTime());
            gc1.set(GregorianCalendar.MINUTE, gc1.get(GregorianCalendar.MINUTE) + 20);
            if (new Date().before(gc1.getTime()) && new Date().after(gc.getTime())) {
                // Ставим кастомера
                //трем запись в таблице предварительных записей

                Spring.getInstance().getTt().execute(new TransactionCallbackWithoutResult() {

                    @Override
                    protected void doInTransactionWithoutResult(TransactionStatus status) {
                        try {
                            Spring.getInstance().getHt().delete(advCust);
                            QLog.l().logger().debug("Удалили предварителньную запись о кастомере." + " " + ipAdress);
                        } catch (Exception ex) {
                            status.setRollbackOnly();
                            throw new ServerException("Ошибка при удалении \n" + ex.toString() + "\n" + Arrays.toString(ex.getStackTrace()) + " " + ipAdress);
                        }
                    }
                });
                // создаем кастомера вызвав задание по созданию кастомера
                // загрузим задание
                final CmdParams params = new CmdParams();
                params.serviceId = advCust.getService().getId();
                params.parolcheg = "";
                params.priority = advCust.getPriority();
                params.textData = advCust.getInputData();
                final RpcStandInService txtCustomer = addCustomerTask.process(params, ipAdress, ip);
                txtCustomer.getResult().setInput_data(advCust.getInputData());
                return txtCustomer;
            } else {
                String answer = Locales.locMes("advclient_out_date");
                QLog.l().logger().trace(answer);
                // Шлем отказ
                return new RpcStandInService(null, answer);
            }
        }
    };
    /**
     * Удалить предварительно записанного кастомера
     */
    final Task removeAdvanceCustomer = new Task(Uses.TASK_REMOVE_ADVANCE_CUSTOMER) {

        @Override
        public JsonRPC20OK process(CmdParams cmdParams, String ipAdress, byte[] ip) {
            super.process(cmdParams, ipAdress, ip);

            // Вытащим из базы предварительного кастомера
            final QAdvanceCustomer advCust = Spring.getInstance().getHt().get(QAdvanceCustomer.class, cmdParams.customerId);
            if (advCust == null || advCust.getId() == null || advCust.getAdvanceTime() == null) {
                QLog.l().logger().debug("не найден клиент по его ID=" + cmdParams.customerId + " " + ipAdress);
                // Шлем отказ
                return new JsonRPC20OK(ADVANCED_NOT_FOUND);
            }
            //трем запись в таблице предварительных записей
            Spring.getInstance().getTt().execute(new TransactionCallbackWithoutResult() {

                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    try {
                        Spring.getInstance().getHt().delete(advCust);
                        QLog.l().logger().debug("Удалили предварителньную запись о кастомере." + " " + ipAdress);
                    } catch (Exception ex) {
                        status.setRollbackOnly();
                        throw new ServerException("Ошибка при удалении \n" + ex.toString() + "\n" + Arrays.toString(ex.getStackTrace()) + " " + ipAdress);
                    }
                }
            });
            return new JsonRPC20OK();
        }
    };
    /**
     * Получение списка отзывов.
     */
    final Task getResponseList = new Task(Uses.TASK_GET_RESPONSE_LIST) {

        @Override
        public RpcGetRespTree process(CmdParams cmdParams, String ipAdress, byte[] ip) {
            super.process(cmdParams, ipAdress, ip);
            return new RpcGetRespTree(QResponseTree.getInstance().getRoot());
        }
    };
    /**
     * Регистрация отзыва.
     */
    final Task setResponseAnswer = new Task(Uses.TASK_SET_RESPONSE_ANSWER) {

        @Override
        public AJsonRPC20 process(CmdParams cmdParams, String ipAdress, byte[] ip) {
            super.process(cmdParams, ipAdress, ip);
            final JsonRPC20OK rpc = new JsonRPC20OK();
            final QRespEvent event = new QRespEvent();
            event.setDate(new Date());
            event.setRespID(cmdParams.responseId);
            event.setServiceID(cmdParams.serviceId);
            event.setUserID(cmdParams.userId);
            event.setClientID(cmdParams.customerId);
            event.setClientData(cmdParams.textData);
            event.setComment(cmdParams.comments == null ? "" : cmdParams.comments);
            if (cmdParams.userId != null && cmdParams.customerId == null) {
                if (QUserList.getInstance().hasById(cmdParams.userId)) {
                    final QUser user = QUserList.getInstance().getById(cmdParams.userId);
                    if (user.getShadow() != null) {
                        event.setClientID(user.getShadow().getIdOldCustomer());
                        event.setServiceID(user.getShadow().getIdOldService());
                    }
                    // а теперь костыль.
                    // т.к. отзыв может прийти во время работы с кастомером, а если он только пришел, то его нет в бвзе, а у отзыва может быть
                    // сылка на ID кастомера, следовательно отстрел по констрейнту.
                    // по этому этот отзыв в рюгзак кастомеру, содержимое рюгзака сохраняем при смене состояния кастомера.
                    if (user.getCustomer() != null
                            && (user.getCustomer().getState() == CustomerState.STATE_WAIT
                            || user.getCustomer().getState() == CustomerState.STATE_INVITED
                            || user.getCustomer().getState() == CustomerState.STATE_WORK)) {
                        user.getCustomer().addNewRespEvent(event);
                        return rpc;
                    }
                } else {
                    QLog.l().logger().error("It is a bull shit! No user by id=\"" + cmdParams.userId + "\"" + " " + ipAdress);
                }
            }

            final JsonRPC20Error rpcErr = new JsonRPC20Error(0, null);
            Spring.getInstance().getTt().execute(new TransactionCallbackWithoutResult() {

                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    try {
                        Spring.getInstance().getHt().saveOrUpdate(event);
                        QLog.l().logger().debug("Сохранили отзыв в базе." + " " + ipAdress);
                    } catch (Exception ex) {
                        rpcErr.setError(new JsonRPC20Error.ErrorRPC(JsonRPC20Error.ErrorMsg.RESPONCE_NOT_SAVE, ex));
                        QLog.l().logger().error("Ошибка при сохранении \n" + ex.toString() + "\n" + Arrays.toString(ex.getStackTrace()) + " " + ipAdress);
                        status.setRollbackOnly();
                    }
                }
            });
            return rpcErr.getError().getCode() == 0 || rpcErr.getError().getData() == null ? rpc : rpcErr;
        }
    };
    /**
     * Получение информационного дерева.
     */
    final Task getInfoTree = new Task(Uses.TASK_GET_INFO_TREE) {

        @Override
        public RpcGetInfoTree process(CmdParams cmdParams, String ipAdress, byte[] ip) {
            super.process(cmdParams, ipAdress, ip);
            return new RpcGetInfoTree(QInfoTree.getInstance().getRoot());
        }
    };
    /**
     * Идентифицировать кастомера по его ID.
     */
    final Task getClientAuthorization = new Task(Uses.TASK_GET_CLIENT_AUTHORIZATION) {

        @Override
        public RpcGetAuthorizCustomer process(CmdParams cmdParams, String ipAdress, byte[] ip) {
            super.process(cmdParams, ipAdress, ip);
            // Вытащим из базы предварительного кастомера
            if (cmdParams.clientAuthId == null || cmdParams.clientAuthId.isEmpty()) {
                return new RpcGetAuthorizCustomer(null);
            }
            final List<QAuthorizationCustomer> authCusts = Spring.getInstance().getHt().findByCriteria(DetachedCriteria.forClass(QAuthorizationCustomer.class).add(Restrictions.eq("authId", cmdParams.clientAuthId).ignoreCase()));
            final QAuthorizationCustomer authCust;
            if (authCusts.isEmpty() || authCusts.get(0) == null || authCusts.get(0).getId() == null || authCusts.get(0).getName() == null) {
                QLog.l().logger().trace("Не найден клиент по его ID = '" + cmdParams.clientAuthId + "'" + " " + ipAdress);
                authCust = null;
            } else {
                authCust = authCusts.get(0);
            }
            return new RpcGetAuthorizCustomer(authCust);
        }
    };
    /**
     * Получение списка результатов по окончанию работы пользователя с клиентом.
     */
    final Task getResultsList = new Task(Uses.TASK_GET_RESULTS_LIST) {

        @Override
        public RpcGetResultsList process(CmdParams cmdParams, String ipAdress, byte[] ip) {
            super.process(cmdParams, ipAdress, ip);
            return new RpcGetResultsList(QResultList.getInstance().getItems());
        }
    };
    /**
     * Изменение приоритета кастомеру
     */
    final Task setCustomerPriority = new Task(Uses.TASK_SET_CUSTOMER_PRIORITY) {

        @Override
        public RpcGetSrt process(final CmdParams cmdParams, String ipAdress, byte[] ip) {
            super.process(cmdParams, ipAdress, ip);
            // Этому что-ли повышать приоритет
            final String num = cmdParams.clientAuthId.replaceAll("[^\\p{L}+\\d]", "");
            String s = "";
            for (QService service : QServiceTree.getInstance().getNodes()) {
                if (service.changeCustomerPriorityByNumber(num, cmdParams.priority)) {
                    s = "Клиенту с номером \"" + num + "\" в услуге \"" + service.getName() + "\" изменен приоритет.";
                    break;
                }
            }
            return new RpcGetSrt("".equals(s) ? String.format(Locales.locMes("client_not_found_by_num"), num) : s);
        }
    };
    /**
     * Проверить номер кастомера
     */
    final Task checkCustomerNumber = new Task(Uses.TASK_CHECK_CUSTOMER_NUMBER) {

        @Override
        public RpcGetTicketHistory process(final CmdParams cmdParams, String ipAdress, byte[] ip) {
            super.process(cmdParams, ipAdress, ip);
            final String num = cmdParams.clientAuthId.trim().replaceAll("[^\\p{L}+\\d]", "");
            String s = "";
            for (QService service : QServiceTree.getInstance().getNodes()) {
                for (QCustomer customer : service.getClients()) {
                    if (num.equalsIgnoreCase(customer.getPrefix() + (customer.getNumber() < 1 ? "" : customer.getNumber()))) {
                        s = String.format(Locales.locMes("client_with_number_to_service"), num, service.getName());
                        break;
                    }
                }
            }
            if ("".equals(s)) {
                for (QCustomer customer : QPostponedList.getInstance().getPostponedCustomers()) {
                    if (num.equalsIgnoreCase(customer.getPrefix() + (customer.getNumber() < 1 ? "" : customer.getNumber()))) {
                        s = String.format(Locales.locMes("client_with_number_postponed"), num);
                        break;
                    }
                }
            }

            if ("".equals(s)) {
                for (QUser user : QUserList.getInstance().getItems()) {
                    if (user.getCustomer() != null && num.equalsIgnoreCase(user.getCustomer().getFullNumber())) {
                        s = String.format(Locales.locMes("client_with_number_in_work"), num, user.getName());
                        break;
                    }
                }
            }

            if ("".equals(s) && KILLED_CUSTOMERS.get(num) != null) {
                s = String.format(Locales.locMes("client_with_number_removed"), num, Locales.getInstance().format_for_label.format(KILLED_CUSTOMERS.get(num)));
            }

            final String n = num.replaceAll("\\D+", "");
            final String p = num.replaceAll(n, "");
            final List<QCustomer> custs = Spring.getInstance().getHt().find("FROM QCustomer a WHERE service_prefix ='" + p + "' and number = " + (n.isEmpty() ? "0" : n));
            final LinkedList lc = new LinkedList();
            custs.forEach((cust) -> {
                lc.add(Uses.FORMAT_DD_MM_YYYY_TIME.format(cust.getStandTime()) + "&nbsp;&nbsp;&nbsp;&nbsp;" + (cust.getService().getName().length() > 96 ? cust.getService().getName().substring(0, 95) + "..." : cust.getService().getName()) + "&nbsp;&nbsp;&nbsp;&nbsp;" + cust.getUser().getName() + "&nbsp;&nbsp;&nbsp;&nbsp;" + CustomerState.values()[cust.getStateIn()]);
            });
            return new RpcGetTicketHistory(new RpcGetTicketHistory.TicketHistory("".equals(s) ? String.format(Locales.locMes("client_not_found_by_num_at_all"), num) : s, lc));
        }
    };
    /**
     * Рестарт сервера из админки
     */
    final Task restartServer = new Task(Uses.TASK_RESTART) {

        @Override
        public AJsonRPC20 process(final CmdParams cmdParams, String ipAdress, byte[] ip) {
            super.process(cmdParams, ipAdress, ip);
            QServer.savePool();
            ServerEvents.getInstance().restartEvent();
            QPostponedList.getInstance().clear();
            QServer.loadPool();
            MainBoard.getInstance().refresh();
            return new JsonRPC20OK();
        }
    };
    /**
     * Рестарт главного табло из админки
     */
    final Task restarMainTablo = new Task(Uses.TASK_RESTART_MAIN_TABLO) {

        @Override
        public AJsonRPC20 process(final CmdParams cmdParams, String ipAdress, byte[] ip) {
            super.process(cmdParams, ipAdress, ip);
            MainBoard.getInstance().refresh();
            return new JsonRPC20OK();
        }
    };
    /**
     * Изменить бегущий текст на табло
     */
    final Task refreshRunningText = new Task(Uses.TASK_CHANGE_RUNNING_TEXT_ON_BOARD) {

        @Override
        public AJsonRPC20 process(final CmdParams cmdParams, String ipAdress, byte[] ip) {
            super.process(cmdParams, ipAdress, ip);
            if (MainBoard.getInstance() instanceof QIndicatorBoardMonitor) {
                final QIndicatorBoardMonitor mon = (QIndicatorBoardMonitor) MainBoard.getInstance();
                if (Uses.TAG_BOARD_TOP.equals(cmdParams.infoItemName)) {
                    mon.indicatorBoard.getTopRunningLabel().stop();
                    mon.indicatorBoard.getTopRunningLabel().setText("");
                    mon.indicatorBoard.getTopRunningLabel().setShowTime(false);
                    mon.indicatorBoard.getTopRunningLabel().setRunningText(cmdParams.textData);
                    mon.indicatorBoard.getTopRunningLabel().start();
                }
                if (Uses.TAG_BOARD_LEFT.equals(cmdParams.infoItemName)) {
                    mon.indicatorBoard.getLeftRunningLabel().stop();
                    mon.indicatorBoard.getLeftRunningLabel().setText("");
                    mon.indicatorBoard.getLeftRunningLabel().setShowTime(false);
                    mon.indicatorBoard.getLeftRunningLabel().setRunningText(cmdParams.textData);
                    mon.indicatorBoard.getLeftRunningLabel().start();
                }
                if (Uses.TAG_BOARD_RIGHT.equals(cmdParams.infoItemName)) {
                    mon.indicatorBoard.getRightRunningLabel().stop();
                    mon.indicatorBoard.getRightRunningLabel().setText("");
                    mon.indicatorBoard.getRightRunningLabel().setShowTime(false);
                    mon.indicatorBoard.getRightRunningLabel().setRunningText(cmdParams.textData);
                    mon.indicatorBoard.getRightRunningLabel().start();
                }
                if (Uses.TAG_BOARD_BOTTOM.equals(cmdParams.infoItemName)) {
                    mon.indicatorBoard.getBottomRunningLabel().stop();
                    mon.indicatorBoard.getBottomRunningLabel().setText("");
                    mon.indicatorBoard.getBottomRunningLabel().setShowTime(false);
                    mon.indicatorBoard.getBottomRunningLabel().setRunningText(cmdParams.textData);
                    mon.indicatorBoard.getBottomRunningLabel().start();
                }
            }
            return new JsonRPC20OK();
        }
    };
    /**
     * Запрос на изменение приоритетов оказываемых услуг от юзеров
     */
    final Task changeFlexPriority = new Task(Uses.TASK_CHANGE_FLEX_PRIORITY) {

        @Override
        public AJsonRPC20 process(final CmdParams cmdParams, String ipAdress, byte[] ip) {
            super.process(cmdParams, ipAdress, ip);
            final QUser user = QUserList.getInstance().getById(cmdParams.userId);
            for (String str : cmdParams.textData.split("&")) {
                final String[] ss = str.split("=");
                if (!"".equals(ss[0]) && !"".equals(ss[1])) {
                    user.getPlanService(Long.parseLong(ss[0])).setCoefficient(Integer.parseInt(ss[1]));
                }
            }
            return new JsonRPC20OK();
        }
    };
    /**
     * Получение нормативов.
     */
    final Task getStandards = new Task(Uses.TASK_GET_STANDARDS) {

        @Override
        public RpcGetStandards process(CmdParams cmdParams, String ipAdress, byte[] ip) {
            super.process(cmdParams, ipAdress, ip);
            return new RpcGetStandards(ServerProps.getInstance().getStandards());
        }
    };
    /**
     * Поставить паузу у пользователя.
     */
    final Task setPause = new Task(Uses.TASK_SET_BUSSY) {

        @Override
        public RpcGetBool process(CmdParams cmdParams, String ipAdress, byte[] ip) {
            super.process(cmdParams, ipAdress, ip);
            QUserList.getInstance().getById(cmdParams.userId).setPause(cmdParams.requestBack);
            return new RpcGetBool(QUserList.getInstance().getById(cmdParams.userId).isPause());
        }
    };
    /**
     * Получить параметры из ДБ из сервера
     */
    final Task getProperties = new Task(Uses.TASK_GET_PROPERTIES) {

        @Override
        public RpcGetProperties process(CmdParams cmdParams, String ipAdress, byte[] ip) {
            super.process(cmdParams, ipAdress, ip);
            return new RpcGetProperties(ServerProps.getInstance().getDBproperties(true));
        }
    };

    /**
     * Проинитить параметры из ДБ в сервере
     */
    final Task initProperties = new Task(Uses.TASK_INIT_PROPERTIES) {

        @Override
        public RpcGetProperties process(CmdParams cmdParams, String ipAdress, byte[] ip) {
            super.process(cmdParams, ipAdress, ip);
            cmdParams.properties.forEach(prop -> {
                if (ServerProps.getInstance().getSection(prop.getSection()) == null
                        || ServerProps.getInstance().getSection(prop.getSection()).getProperty(name) == null) {
                    ServerProps.getInstance().saveOrUpdateProperty(prop);
                }
            });
            return new RpcGetProperties(ServerProps.getInstance().getDBproperties());
        }
    };

    /**
     * Сохранить параметры в ДБ сервера
     */
    final Task saveProperties = new Task(Uses.TASK_SAVE_PROPERTIES) {

        @Override
        public RpcGetProperties process(CmdParams cmdParams, String ipAdress, byte[] ip) {
            super.process(cmdParams, ipAdress, ip);
            cmdParams.properties.forEach(prop -> {
                ServerProps.getInstance().saveOrUpdateProperty(prop);
            });
            return new RpcGetProperties(ServerProps.getInstance().getDBproperties());
        }
    };

    /**
     * Обновить параметры рулона с билетами.
     */
    final Task reinitRoll = new Task(Uses.TASK_REINIT_ROLL) {

        @Override
        public AJsonRPC20 process(final CmdParams cmdParams, String ipAdress, byte[] ip) {
            super.process(cmdParams, ipAdress, ip);
            final QService service = QServiceTree.getInstance().getById(cmdParams.serviceId);
            if (service == null) {
                return new JsonRPC20Error();
            }
            final QProperty prop = ServerProps.getInstance().getProperty(service.getSectionName(), Uses.KEY_ROLL, cmdParams.first.toString(), cmdParams.last.toString());
            prop.setValue(cmdParams.first);
            prop.setComment(cmdParams.last.toString());
            ServerProps.getInstance().saveOrUpdateProperty(prop);
            service.reinitNextNumber(cmdParams.current);
            return new JsonRPC20OK();
        }
    };

//****************************************************************************
//********************* КОНЕЦ добавления в мап обработчиков заданий  *********
//****************************************************************************
//**********************************************************************************************
//**********************************   ОБРАБОТКА ЗАДАНИЙ  **************************************
//**********************************************************************************************    

    /**
     * Выполнение всех заданий, пришедших на обработку
     *
     * @param rpc      объект задания
     * @param ipAdress адрес того кто прислал задание
     * @param ip       адрес того кто прислал задание
     * @return объект результата выполнения задания
     */
    public Object doTask(JsonRPC20 rpc, String ipAdress, byte[] ip) {
        final long start = System.currentTimeMillis();
        if (!QConfig.cfg().isDebug()) {
            System.out.println("Task processing: '" + rpc.getMethod() + " " + ipAdress);
        }
        QLog.l().logger().info("Task processing: '" + rpc.getMethod() + "'" + " " + ipAdress);
        if (tasks.get(rpc.getMethod()) == null) {
            throw new ServerException("В задании не верно указано название действия: '" + rpc.getMethod() + "'" + " " + ipAdress);
        }

        final Object result;
        // Вызов обработчика задания не синхронизирован
        // Синхронизация переехала внутрь самих обработчиков с помощью блокировок
        // Это сделано потому что появилось много заданий, которые не надо синхронизировать.
        // А то что необходимо синхронизировать, то синхронизится в самих обработчиках.
        result = tasks.get(rpc.getMethod()).process(rpc.getParams(), ipAdress, ip);

        QLog.l().logger().info("Task was finished. Time: " + ((double) (System.currentTimeMillis() - start)) / 1000 + " sec." + " " + ipAdress);
        return result;
    }
}

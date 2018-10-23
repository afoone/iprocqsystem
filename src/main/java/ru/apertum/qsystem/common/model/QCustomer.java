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
package ru.apertum.qsystem.common.model;

import java.io.Serializable;
import java.util.LinkedList;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import ru.apertum.qsystem.server.model.QService;
import ru.apertum.qsystem.server.model.QUser;
import ru.apertum.qsystem.server.model.results.QResult;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.Date;
import java.util.ServiceLoader;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import ru.apertum.qsystem.common.QLog;
import ru.apertum.qsystem.common.CustomerState;
import ru.apertum.qsystem.common.QConfig;
import ru.apertum.qsystem.common.exceptions.ServerException;
import ru.apertum.qsystem.extra.IChangeCustomerStateEvent;
import ru.apertum.qsystem.server.Spring;
import ru.apertum.qsystem.server.model.IidGetter;
import ru.apertum.qsystem.server.model.response.QRespEvent;

/**
 * Implementation of the client The simplest "waiting list". Used to organize a simple queue.
 * If a DBMS is used, then saving occurs when changing the state.
 * IMPORTANT! Always change the status of the custom when it changes, especially when it is deleted.
 * @author Evgeniy Egorov
 * @author Alfonso Tienda afoone@hotmail.com
 *
 */
@Entity
@Table(name = "clients")
public final class QCustomer implements Comparable<QCustomer>, Serializable, IidGetter {

    public QCustomer() {
        id = new Date().getTime();
    }

    /**
     * создаем клиента имея только его номер в очереди. Префикс не определен, т.к. еще не знаем об услуге куда его поставить. Присвоем кастомену услугу -
     * присвоются и ее атрибуты.
     *
     * @param number номер клиента в очереди
     */
    public QCustomer(int number) {
        this.number = number;
        id = new Date().getTime();
        setStandTime(new Date()); // действия по инициализации при постановке
        // все остальные всойства кастомера об услуге куда попал проставятся в самой услуге при помещении кастомера в нее
        QLog.l().logger().debug("Created a customer with number " + number);
    }

    @Expose
    @SerializedName("id")
    private Long id = new Date().getTime();

    @Id
    @Column(name = "id")
    @Override
    //@GeneratedValue(strategy = GenerationType.AUTO) простаяляем уникальный номер времени создания.
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * ATTRIBUTES OF THE "OBJECTOR" personal number, it is on it that the system keeps records and management of the queues number - an integer Customer number. In general, he
     * Issued in order. But if the number is something entered by the user, then this number is -1.
     */
    @Expose
    @SerializedName("number")
    private Integer number;

    public void setNumber(Integer number) {
        this.number = number;
    }

    /**
     * Номер клиента. Вообще, он выдается по порядку. Но если номером является нечто введенное пользователем, то этот номер равен -1.
     *
     * @return Номер попорядку, либо -1 в случае номера как введенного пользователем текста.
     */
    @Column(name = "number")
    public int getNumber() {
        return number;
    }

    @Expose
    @SerializedName("stateIn")
    private Integer stateIn;

    @Column(name = "state_in")
    public Integer getStateIn() {
        return stateIn;
    }

    public void setStateIn(Integer stateIn) {
        this.stateIn = stateIn;
    }

    /**
     * АТРИБУТЫ "ОЧЕРЕДНИКА" состояние кастомера, именно по нему система знает что сейчас происходит с кастомером Это состояние менять только если кастомер уже
     * готов к этому и все другие параметры у него заполнены. Если данные пишутся в БД, то только по состоянию завершенности обработки над ним. Так что если
     * какая-то итерация закончена и про кастомера должно занестись в БД, то как и надо выставлять что кастомер ЗАКОНЧИЛ обрабатываться, а уж потом менять ,
     * если надо, его атрибуты и менять состояние, например на РЕДИРЕКТЕННОГО.
     * <p>
     * состояние клиента
     *
     * @see ru.apertum.qsystem.common.Uses
     */
    @Expose
    @SerializedName("state")
    private CustomerState state;

    public void setState(CustomerState state) {
        setState(state, -1L);
    }

    /**
     * Especially for redirect and return after redirect
     *
     * @param state
     * @param newServiceId - при редиректе и возврате после редиректа тут будет ID той услуги куда редиректим или возвращвем, причем услуга у кастомера все еще
     *                     прежняя, т.е. так в которой завершили с ним работать
     */
    public void setState(CustomerState state, Long newServiceId) {
        if (this.state == state) {
            return;
        }
        this.state = state;
        stateIn = state.ordinal();

        // it will be possible to follow the shadow of the customer with the user and his changes
        if (getUser() != null) {
            getUser().getShadow().setCustomerState(state);
        }

        switch (state) {
            case STATE_DEAD:
                QLog.l().logger().debug("Status: Customer number \"" + getPrefix() + getNumber() + "\" goes home on a non-appearance");
                if (getUser().hasService(getService())) {
                    getUser().getPlanService(getService()).inkKilled();
                } else {
                    QLog.l().logger().warn("Service \"" + getService() + "\" doesn't found at the user \"" + getUser() + "\"");
                }
                // хер с ним, сохраним чтоб потом почекать неподошедших. сохраним кастомера в базе
                // только финиш_тайм надо проставить, хер сним, и старт_тайм тоже, ядренбатон
                setStartTime(new Date());
                setFinishTime(new Date());
                saveToSelfDB();
                break;
            case STATE_WAIT:
                QLog.l().logger().debug("Status: The customer comes and waits with the number\"" + getPrefix() + getNumber() + "\"");
                setStandTime(new Date());
                break;
            case STATE_WAIT_AFTER_POSTPONED:
                QLog.l().logger().debug("Status: The customer was returned from the pending after the expiration of time and waits with the number \"" + getPrefix() + getNumber() + "\"");
                setStandTime(new Date());
                break;
            case STATE_WAIT_COMPLEX_SERVICE:
                QLog.l().logger().debug("Status: The customer was once again queued up. the service is bundled and waiting with the number \"" + getPrefix() + getNumber() + "\"");
                setStandTime(new Date());
                break;
            case STATE_INVITED:
                QLog.l().logger().debug("Status: Invited to the customer with the number \"" + getPrefix() + getNumber() + "\"");
                // ставим время вызова
                setCallTime(new Date());
                break;
            case STATE_INVITED_SECONDARY:
                QLog.l().logger().debug("Status: Invited again in the customer processing chain with a number \"" + getPrefix() + getNumber() + "\"");
                // ставим время вызова
                setCallTime(new Date());
                break;
            case STATE_REDIRECT:
                QLog.l().logger().debug("Status: Customer redirected with a number \"" + getPrefix() + getNumber() + "\"");
                if (getUser().hasService(getService())) {
                    getUser().getPlanService(getService()).inkWorked(System.currentTimeMillis() - getStartTime().getTime());
                } else {
                    QLog.l().logger().warn("Service \"" + getService() + "\" doesn't found at the user \"" + getUser() + "\"");
                }
                setFinishTime(new Date());
                // save the customer in the database
                saveToSelfDB();
                setStandTime(new Date());
                break;
            case STATE_WORK:
                QLog.l().logger().debug("Began to work with the customer number \"" + getPrefix() + getNumber() + "\"");
                setStartTime(new Date());
                if (getUser().hasService(getService())) {
                    getUser().getPlanService(getService()).upWait(getStartTime().getTime() - getStandTime().getTime());
                } else {
                    QLog.l().logger().warn("Service \"" + getService() + "\" doesn't found at the user \"" + getUser() + "\"");
                }
                break;
            case STATE_WORK_SECONDARY:
                QLog.l().logger().debug("Status: Next on the chain began to work with the customer number \"" + getPrefix() + getNumber() + "\"");
                setStartTime(new Date());
                if (getUser().hasService(getService())) {
                    getUser().getPlanService(getService()).upWait(getStartTime().getTime() - getStandTime().getTime());
                } else {
                    QLog.l().logger().warn("Service \"" + getService() + "\" doesn't found at the user \"" + getUser() + "\"");
                }
                break;
            case STATE_BACK:
                QLog.l().logger().debug("Status: Customer number \"" + getPrefix() + getNumber() + "\" return to the previous service");
                setStandTime(new Date());
                break;
            case STATE_FINISH:
                QLog.l().logger().debug("Status: With customer number \"" + getPrefix() + getNumber() + "\" finished work");
                setFinishTime(new Date());
                if (getUser().hasService(getService())) {
                    getUser().getPlanService(getService()).inkWorked(getFinishTime().getTime() - getStartTime().getTime());
                } else {
                    QLog.l().logger().warn("Service \"" + getService() + "\" doesn't found at the user \"" + getUser() + "\"");
                }
                // сохраним кастомера в базе
                saveToSelfDB();
                break;
            case STATE_POSTPONED:
                QLog.l().logger().debug("Customer number \"" + getPrefix() + getNumber() + "\" goes to wait on pending");
                setFinishTime(new Date());
                if (getUser().hasService(getService())) {
                    getUser().getPlanService(getService()).inkWorked(getFinishTime().getTime() - getStartTime().getTime());
                } else {
                    QLog.l().logger().warn("Service \"" + getService() + "\" doesn't found at the user \"" + getUser() + "\"");
                }
                // save the customer in the database
                saveToSelfDB();
                setStandTime(new Date());
                break;
        }

        // plugin extensibility support
        for (final IChangeCustomerStateEvent event : ServiceLoader.load(IChangeCustomerStateEvent.class)) {
            QLog.l().logger().info("Call SPI extensions. Description: " + event.getDescription());
            try {
                event.change(this, state, newServiceId);
            } catch (Exception tr) {
                QLog.l().logger().error("Call SPI extension failed. Description: " + tr);
            }
        }
    }

    @Transient
    private final LinkedList<QRespEvent> resps = new LinkedList<>();

    public void addNewRespEvent(QRespEvent event) {
        resps.add(event);
    }

    private void saveToSelfDB() {
        // сохраним кастомера в базе
        final DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SomeTxName");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = Spring.getInstance().getTxManager().getTransaction(def);
        try {
            if (input_data == null) { // вот жеж черд дернул выставить констрейнт на то что введенные данные не нул, а они этот ввод редко нужкн
                input_data = "";
            }
            Spring.getInstance().getHt().saveOrUpdate(this);
            // костыль. Если кастомер оставил отзывы прежде чем попал в БД, т.е. во время работы еще с ним.
            if (resps.size() > 0) {
                Spring.getInstance().getHt().saveAll(resps);
                resps.clear();
            }
        } catch (Exception ex) {
            Spring.getInstance().getTxManager().rollback(status);
            throw new ServerException("Ошибка при сохранении \n" + ex.toString() + "\n" + Arrays.toString(ex.getStackTrace()));
        }
        Spring.getInstance().getTxManager().commit(status);
        QLog.l().logger().debug("Сохранили.");
    }

    @Transient
    public CustomerState getState() {
        return state;
    }

    /**
     * ПРИОРИТЕТ "ОЧЕРЕДНИКА"
     */
    @Expose
    @SerializedName("priority")
    private Integer priority;

    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Transient
    public IPriority getPriority() {
        return new Priority(priority);
    }

    /**
     * Сравнение очередников для выбора первого. Участвует приоритет очередника. сравним по приоритету, потом по времени
     *
     * @param customer
     * @return используется отношение "обслужится позднее"(сравнение дает ответ на вопрос "я обслужусь позднее чем тот в параметре?") 1 - "обслужится позднее"
     * чем кастомер в параметре, -1 - "обслужится раньше" чем кастомер в параметре, 0 - одновременно -1 - быстрее обслужится чем кастомер из параметров, т.к.
     * встал раньше 1 - обслужится после чем кастомер из параметров, т.к. встал позднее
     */
    @Override
    public int compareTo(QCustomer customer) {
        int resultCmp = -1 * getPriority().compareTo(customer.getPriority()); // (-1) - т.к.  больший приоритет быстрее обслужится

        if (resultCmp == 0) {
            if (this.getStandTime().before(customer.getStandTime())) {
                resultCmp = -1;
            } else if (this.getStandTime().after(customer.getStandTime())) {
                resultCmp = 1;
            }
        }
        if (resultCmp == 0) {
            QLog.l().logger().warn("Клиенты не могут быть равны.");
            resultCmp = -1;
        }
        return resultCmp;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof QCustomer) {
            final QCustomer cust = (QCustomer) obj;
            return this.getSemiNumber().equals(cust.getSemiNumber()) && this.getId().equals(cust.getId());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return (getSemiNumber() + getId().toString()).hashCode();
    }

    /**
     * К какой услуге стоит. Нужно для статистики.
     */
    @Expose
    @SerializedName("to_service")
    private QService service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    public QService getService() {
        return service;
    }

    /**
     * Customer to put attributes of the service including name, description, prefix. And the prefix is put once and for all. When adding a customizer to the service
     * addCustomer () the same thing happens + a prefix is prefixed, if such an attribute is not added to the XML node of the customizer
     *
     * @param service не передавать тут NULL
     */
    public void setService(QService service) {
        this.service = service;
        // Префикс для кастомера проставится при его создании, один раз и на всегда.
        if (getPrefix() == null) {
            setPrefix(service.getPrefix());
        }
        QLog.l().logger().debug("Customer \"" + getFullNumber() + "\" put to service \"" + service.getName() + "\"");
    }

    /**
     * Результат работы с пользователем
     */
    private QResult result;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "result_id")
    public QResult getResult() {
        return result;
    }

    public void setResult(QResult result) {
        this.result = result;
        if (result == null) {
            QLog.l().logger().debug("It is not required to designate the result of working with a customer");
        } else {
            QLog.l().logger().debug("Marked the result of working with the customer: \"" + result.getName() + "\"");
        }
    }

    /**
     * Кто его обрабатывает. Нужно для статистики.
     */
    @Expose
    @SerializedName("from_user")
    private QUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    public QUser getUser() {
        return user;
    }

    public void setUser(QUser user) {
        this.user = user;
        QLog.l().logger().debug("Customer \"" + getFullNumber() + (user == null ? " There is no user, he did not call it\"" : " user defined: \"" + user.getName() + "\""));
    }

    /**
     * Префикс услуги, к которой стоит кастомер.
     * <p>
     * Строка префикса.
     */
    @Expose
    @SerializedName("prefix")
    private String prefix;

    @Column(name = "service_prefix")
    public String getPrefix() {
        return prefix;
    }

    /**
     * Номер талона с разделителем.
     *
     * @return Номер талона с разделителем.
     */
    @Transient()
    public String getFullNumber() {
        final String div = "".equals(getPrefix()) ? "" : QConfig.cfg().getNumDivider(getPrefix());
        return "" + getPrefix() + (getNumber() < 1 ? "" : (div + getNumber()));
    }

    /**
     * Номер талона без разделителя.
     *
     * @return Номер талона без разделителя.
     */
    @Transient()
    public String getSemiNumber() {
        return getPrefix() + (getNumber() < 1 ? "" : (getNumber()));
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix == null ? "" : prefix;
    }

    @Expose
    @SerializedName("stand_time")
    private Date standTime;

    @Column(name = "stand_time")
    @Temporal(TemporalType.TIMESTAMP)
    public Date getStandTime() {
        return standTime;
    }

    public void setStandTime(Date date) {
        this.standTime = date;
    }

    @Expose
    @SerializedName("start_time")
    private Date startTime;

    @Column(name = "start_time")
    @Temporal(TemporalType.TIMESTAMP)
    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date date) {
        this.startTime = date;
    }

    private Date callTime;

    public void setCallTime(Date date) {
        this.callTime = date;
    }

    @Transient
    public Date getCallTime() {
        return callTime;
    }

    @Expose
    @SerializedName("finish_time")
    private Date finishTime;

    @Column(name = "finish_time")
    @Temporal(TemporalType.TIMESTAMP)
    public Date getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(Date date) {
        this.finishTime = date;
    }

    @Expose
    @SerializedName("input_data")
    private String input_data = "";

    /**
     * Введенные кастомером данные на пункте регистрации.
     *
     * @return
     */
    @Column(name = "input_data")
    public String getInput_data() {
        return input_data;
    }

    public void setInput_data(String input_data) {
        this.input_data = input_data;
    }

    /**
     * Список услуг в которые необходимо вернуться после редиректа Новые услуги для возврата добвляются в начало списка. При возврате берем первую из списка и
     * удаляем ее.
     */
    private final LinkedList<QService> serviceBack = new LinkedList<>();

    /**
     * При редиректе если есть возврат. то добавим услугу для возврата
     *
     * @param service в эту услугу нужен возврат
     */
    public void addServiceForBack(QService service) {
        serviceBack.addFirst(service);
        needBack = !serviceBack.isEmpty();
    }

    /**
     * Куда вернуть если работу закончили но кастомер редиректенный
     *
     * @return вернуть в эту услугу
     */
    @Transient
    public QService getServiceForBack() {
        needBack = serviceBack.size() > 1;
        return serviceBack.pollFirst();
    }

    @Expose
    @SerializedName("need_back")
    private boolean needBack = false;

    public boolean needBack() {
        return needBack;
    }

    /**
     * Комментариии юзеров о кастомере при редиректе и отправки в отложенные
     */
    @Expose
    @SerializedName("temp_comments")
    private String tempComments = "";

    @Transient
    public String getTempComments() {
        return tempComments;
    }

    public void setTempComments(String tempComments) {
        this.tempComments = tempComments;
    }

    /**
     *
     */
    @Expose
    @SerializedName("post_status")
    private String postponedStatus = "";

    @Transient
    public String getPostponedStatus() {
        return postponedStatus;
    }

    public void setPostponedStatus(String postponedStatus) {
        this.postponedStatus = postponedStatus;
    }

    /**
     * Период отложенности в минутах. 0 - бессрочно;
     */
    @Expose
    @SerializedName("postpone_period")
    private int postponPeriod = 0;

    @Transient
    public int getPostponPeriod() {
        return postponPeriod;
    }

    /**
     * ID of the one who sees the deferred, NULL for all
     */
    @Expose
    @SerializedName("postponed_by")
    private String postponedBy = null;

    @Transient
    public String getPostponedBy() {
        return postponedBy;
    }

    public void setPostponedBy(String userName) {
        this.postponedBy = userName;
    }

    /**
     * ID of the one who sees the deferred, NULL for all
     */
    @Expose
    @SerializedName("is_mine")
    private Long isMine = null;

    @Transient
    public Long getIsMine() {
        return isMine;
    }

    public void setIsMine(Long userId) {
        this.isMine = userId;
    }

    /**
     * The number of repeated calls this client
     */
    @Expose
    @SerializedName("recall_cnt")
    private Integer recallCount = 0;

    @Transient
    public Integer getRecallCount() {
        return recallCount;
    }

    public void setRecallCount(Integer recallCount) {
        this.recallCount = recallCount;
    }

    public void upRecallCount() {
        this.recallCount++;
    }

    public void setPostponPeriod(int postponPeriod) {
        this.postponPeriod = postponPeriod;
        startPontpone = System.currentTimeMillis();
        finishPontpone = startPontpone + postponPeriod * 60 * 1000;
    }

    /**
     * When was postponed in milliseconds;
     */
    @Expose
    @SerializedName("start_postpone_period")
    private long startPontpone = 0;
    @Expose
    @SerializedName("finish_postpone_period")
    private long finishPontpone = 0;

    @Transient
    public long getStartPontpone() {
        return startPontpone;
    }

    @Transient
    public long getFinishPontpone() {
        return finishPontpone;
    }



    /**
     * Returns a string describing the customer
     *
     * @return custom string
     */
    @Override
    public String toString() {

        return getFullNumber()
                + (getInput_data() == null ? "" : (" " + getInput_data()))
                + (postponedStatus == null || postponedStatus.isEmpty() ? "" :
                (" " + postponedStatus + " ("
                        + (postponPeriod > 0 ? postponPeriod : "...") + " / "
                        + (System.currentTimeMillis() - startPontpone) / 1000 / 60 + " min.)"
                        + "por " + postponedBy
                        + (isMine != null ? " Privado" : "")));
    }



    @Transient
    @Override
    public String getName() {
        return getFullNumber() + " " + getInput_data();
    }

    @Expose
    @SerializedName("complex_id")
    public LinkedList<LinkedList<LinkedList<Long>>> complexId = new LinkedList<>();

    @Transient
    public LinkedList<LinkedList<LinkedList<Long>>> getComplexId() {
        return complexId;
    }

    public void setComplexId(LinkedList<LinkedList<LinkedList<Long>>> complexId) {
        this.complexId = complexId;
    }

    @Transient
    public Integer getWaitingMinutes() {
        return new Long((System.currentTimeMillis() - getStandTime().getTime()) / 1000 / 60 + 1).intValue();
    }

    /**
     * The locale that the customer chose when choosing a service at the kiosk
     */
    @Expose
    @SerializedName("lng")
    private String language;

    @Transient
    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}

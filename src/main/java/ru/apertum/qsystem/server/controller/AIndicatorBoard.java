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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import ru.apertum.qsystem.common.CustomerState;
import ru.apertum.qsystem.common.model.ATalkingClock;
import ru.apertum.qsystem.common.model.QCustomer;
import ru.apertum.qsystem.server.model.QUser;

/**
 * Base class for output classes. Here is implemented the engine for storing and managing strings and other info for displaying information. With direct output to
 * the board needs to call this method markShowed () to mark the records as started to hang.
 *
 * @author Evgeniy Egorov
 * @author Alfonso Tienda
 */
abstract public class AIndicatorBoard implements IIndicatorBoard {

    /**
     * The number of lines on the scoreboard. Implement a specific scoreboard.
     *
     * @return The number of lines on the scoreboard.
     */
    abstract protected Integer getLinesCount();

    /**
     * Delay update the main display in seconds.
     */
    private Integer pause = 0;

    public Integer getPause() {
        return pause;
    }

    public void setPause(Integer pause) {
        this.pause = pause;
    }
    //***********************************************************************
    //*************** Working with storing strings ******************************
    /**
     * List of displayed lines The name of the user who created this line on the display (
     * This is the identifier of the lines, because the name of the user is unique in the system) - the line
     */
    protected final LinkedHashMap<String, Record> records = new LinkedHashMap<>();

    /**
     * Adds an entry to the tail of the display list. Makes it not yet displayed. Blinking moved to the board.
     *
     * @param record
     */
    protected void addItem(Record record) {
        records.remove(record.getUserName());
        record.isShowed = false;
        //record.setState(record.getState() == Uses.STATE_INVITED ? Uses.STATE_REDIRECT : Uses.STATE_INVITED);
        records.put(record.getUserName(), record);
    }

    /**
     * Eliminamos el registro. El cliente se fué a casa. 
     *
     * @param record
     */
    protected void removeItem(Record record) {
        records.remove(record.userName);
    }


    /**
     * Devuelve los registros a mostrar
     * @return
     */
    protected LinkedList<Record> getShowRecords() {
        ArrayList<Record> arr = new ArrayList<>(records.values());
        // flip the array, since the added ones fall to the end, and display them first
        for (int i = 0; i < arr.size() / 2; i++) {
            final Record a_i = arr.get(i);
            arr.set(i, arr.get(arr.size() - 1 - i));
            arr.set(arr.size() - 1 - i, a_i);
        }

        int pos = -1; // the position of the latter is not weighty.
        for (int i = 0; i < arr.size(); i++) {
            if (!arr.get(i).isShowed()) {
                pos = i;
            }
        }
        final int startPos = (getLinesCount() - 1 > pos) ? 0 : pos - getLinesCount() + 1; // позиция первой строки на табло.
        final LinkedList<Record> res = new LinkedList<>();
        for (int j = 0; j < arr.size(); j++) {
            if (j >= startPos && j < startPos + getLinesCount()) {
                res.add(arr.get(j));
            }
        }
        return res;
    }

    /**
     * Class one line, esto es un registro de un elemento a llamar, de la cola
     */
    public class Record implements Comparable<Record> {

        final public String point;
        public String customerPrefix;
        public Integer customerNumber;

        @Override
        public String toString() {
            return customerNumber + "-" + point;
        }

        public String toJSON() {
            return "{ \"customerPrefix\": \""+customerPrefix+"\", "+
                    "\"customerNumber\": \""+customerNumber+"\", " +
                    "\"point\": \""+point+"\", " +
                    "\"userName\": \""+userName+"\", " +
                    "\"addressRS\": \""+adressRS+"\", " +
                    "\"ext_data\": \""+ext_data+"\", " +
                    "\"interval\": \""+interval+"\", " +
                    "\"isShowed\": \""+isShowed+"\", " +
                    "\"state\": \""+state+"\" " + //Última línea sin coma..
                    "}";
        }

        /**
         * The name of the user who created this line on the scoreboard. This is the row ID, because The name of the user is unique in the system.
         */
        final private String userName;

        public String getUserName() {
            return userName;
        }

        final public Integer interval;
        /**
         * With RS, this is the device address. When the monitor is the normal output number
         */
        final public Integer adressRS;
        final public String ext_data;
        /**
         * Gone on the scoreboard or not.
         */
        private boolean isShowed = false;

        /**
         * Already seemed how much you need
         *
         * @return
         */
        public boolean isShowed() {
            return isShowed;
        }

        /**
         * state values of "waiting list"
         */
        private CustomerState state = CustomerState.STATE_INVITED;

        public CustomerState getState() {
            return state;
        }

        public void setState(CustomerState state) {
            this.state = state;
        }

        /**
         * When creating a line gets to the display list with a sign that it is not weighed. The hover timer is turned on when the line hits the scoreboard.
         *
         * @param userName
         * @param point          office number where called custom.
         * @param customerPrefix
         * @param customerNumber custom number about whom record.
         * @param ext_data       third column
         * @param adressRS       client's address.
         * @param interval       mandatory time of hanging the line on the scoreboard in seconds
         */
        public Record ( String userName, String point, String customerPrefix, Integer customerNumber, 
                        String ext_data, Integer adressRS, Integer interval) {
            this.ext_data = ext_data;
            this.adressRS = adressRS;
            this.customerPrefix = customerPrefix;
            this.customerNumber = customerNumber;
            this.userName = userName;
            this.point = point;
            this.interval = interval;
            final Record re = this;
            records.put(userName, re);
            showTimer = new ATalkingClock(interval * 1000, 1) {

                @Override
                public void run() {
                    isShowed = true;
                    show(null);
                }
            };
        }

        public Record(CustomerState state, String point, String customerPrefix, Integer customerNumber, String ext_data, Integer adressRS) {
            this.ext_data = ext_data;
            this.customerPrefix = customerPrefix;
            this.customerNumber = customerNumber;
            this.point = point;
            this.state = state;
            this.interval = 0;
            this.adressRS = adressRS;
            this.userName = "noName";
            showTimer = null;
        }

        /**
         * Timer hover time on the scoreboard.
         */
        final private ATalkingClock showTimer;

        /**
         * The record hit the scoreboard.
         */
        public void startVisible() {
            if (!showTimer.isActive()) {
                showTimer.start();
            }
        }

        @Override
        public int compareTo(Record o) {
            return (o != null && adressRS.equals(o.adressRS) && customerNumber.equals(o.customerNumber) && point.equals(o.point) && state == o.state) ? 0 : -1;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Record) {
                return this.compareTo((Record) obj) == 0;
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return  (adressRS.toString() + customerNumber.toString() + point + state.name()).hashCode();
        }
    }
    //**************************************************************************
    //************************** Interaction methods *************************

    @Override
    public synchronized void inviteCustomer(QUser user, QCustomer customer) {
        Record rec = records.get(user.getName());
        if (rec == null) {
            rec = new Record(user.getName(), user.getPoint(), customer.getPrefix(), customer.getNumber(),
                    user.getPointExt().replaceAll("(#client)", customer.getFullNumber()).replaceAll("(#point)", user.getPoint()).
                            replaceAll("(#user)", user.getTabloText()).replaceAll("(#service)", customer.getService().getTabloText()).
                            replaceAll("(#inputed)", !customer.getService().getInputedAsExt() || customer.getInput_data() == null ? "" : customer.getInput_data()),
                    user.getAdressRS(), getPause());
        } else {
            // parallel call must be considered
            // i.e. immediately after the call and start working with one user, the operator can call another
            // it turns out that the call is already hanging and he needs to change the called number and his status, but the cabinet is the same.
            if (!rec.customerPrefix.equalsIgnoreCase(customer.getPrefix()) || !rec.customerNumber.equals(customer.getNumber())) {
                rec.customerPrefix = customer.getPrefix();
                rec.customerNumber = customer.getNumber();
                rec.state = customer.getState();
            }
            addItem(rec);
        }
        show(rec);
    }

    /**
     * The number of the client being called must stop blinking on the operator’s display.
     *
     * @param user the user who started working with the client.
     */
    @Override
    @SuppressWarnings("empty-statement")
    public synchronized void workCustomer(QUser user) {
        Record rec = records.get(user.getName());
        // the record may not be found after the server restart, the list of numbers on the scoreboard is not found
        if (rec == null) {
            rec = new Record(user.getName(), user.getPoint(), ((QUser) user).getCustomer().getPrefix(), ((QUser) user).getCustomer().getNumber(),
                    user.getPointExt().replaceAll("(#client)", ((QUser) user).getCustomer().getFullNumber()).replaceAll("(#point)", user.getPoint()).
                            replaceAll("(#user)", user.getTabloText()).replaceAll("(#service)", ((QUser) user).getCustomer().getService().getTabloText()).
                            replaceAll("(#inputed)", !((QUser) user).getCustomer().getService().getInputedAsExt() || ((QUser) user).getCustomer().getInput_data() == null ? "" : ((QUser) user).getCustomer().getInput_data()),
                    user.getAdressRS(), getPause());
        }
        rec.setState(CustomerState.STATE_WORK);
        show(rec);
    }

    /**
     * On the scoreboard at a specific address must clean the scoreboard
     *
     * @param user The user who deleted the client.
     */
    @Override
    public synchronized void killCustomer(QUser user) {
        final Record rec = records.get(user.getName());
        // the record may not be found after the server restart, the list of numbers on the scoreboard is not found
        if (rec != null) {
            rec.setState(CustomerState.STATE_DEAD);
            removeItem(rec);
            show(rec);
        }
    }

    /**
     * Turn off the information board.
     */
    @Override
    public synchronized void close() {
        showOnBoard(new LinkedList<>());
    }

    //**************************************************************************
    //************************** Otros métodos *********************************
    // to cut off duplication
    private Record oldRec = null;
    private LinkedList<Record> oldList = new LinkedList<>();

    private boolean compareList(LinkedList<Record> newList) {
        if (oldList.size() != newList.size()) {
            return false;
        }
        final int size = oldList.size();
        final Record[] ol = oldList.toArray(new Record[size]);
        final Record[] nl = newList.toArray(new Record[size]);
        for (int i = 0; i < size; i++) {
            if (ol[i].compareTo(nl[i]) != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Here is the whole illumination
     *
     * @param record
     */
    protected void show(Record record) {

        LinkedList<Record> newList = getShowRecords();

        //System.out.println("--swow " + record + " records for show: " +newList);
        if (!compareList(newList)) {
            oldList = new LinkedList<>();
            newList.stream().forEach((rec) -> {
                oldList.add(new Record(rec.state, rec.point, rec.customerPrefix, rec.customerNumber, rec.ext_data, rec.adressRS));
            });
            //System.out.println("go to showOnBoard " + newList);
            showOnBoard(newList);
        }
        if (record != null && record.compareTo(oldRec) != 0) {
            oldRec = new Record(record.state, record.point, record.customerPrefix, record.customerNumber, record.ext_data, record.adressRS);
            showToUser(record);
        }
    }

    /**
     * With the direct output on the scoreboard, you need to call this method to mark the records as started to hang.
     *
     * @param list список выводимых звписей.
     */
    protected void markShowed(Collection<Record> list) {
        // Записи попадают на табло
        if (list != null) {
            list.stream().filter((rec) -> (!rec.isShowed())).forEach((rec) -> {
                rec.startVisible();
            });
        }
    }

    /**
     * Высветить записи на общем табло.
     *
     * @param records Высвечиваемые записи.
     */
    abstract protected void showOnBoard(LinkedList<Record> records);

    /**
     * Высветить запись на табло оператора.
     *
     * @param record Высвечиваемая запись.
     */
    abstract protected void showToUser(Record record);
}

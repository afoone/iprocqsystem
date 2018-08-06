/*
 * Copyright (C) 2016 Evgeniy Egorov
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
package ru.apertum.qsystem.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import ru.apertum.qsystem.common.NetCommander;
import static ru.apertum.qsystem.common.QLog.logProp;
import ru.apertum.qsystem.common.model.INetProperty;
import ru.apertum.qsystem.server.ServerProps;
import ru.apertum.qsystem.server.model.QProperty;

/**
 * Это класс предоставления параметров, которые хранятся в БД и могут использоваться сервером и админкой напрямую. Это класс запросит по сети параметры.
 * Используйте метод load() для получения после инициализации параметров по сети. Именно этот класс надо использовать для доступа к настройкам в компанентах
 * системы.
 *
 * @author Evgeniy Egorov
 */
public class QProperties {

    public static final String SECTION_SERVER = "SERVER";
    public static final String SECTION_WELCOME = "WELCOME";
    public static final String SECTION_CLIENT = "OPERATOR";
    public static final String SECTION_REPORTS = "REPORTS";
    public static final String SECTION_UPDATER = "UPDATER";

    private LinkedHashMap<String, ServerProps.Section> properties = new LinkedHashMap<>();

    private QProperties() {
    }

    public static QProperties get() {
        return QPropertiesHolder.INSTANCE;
    }

    private static class QPropertiesHolder {

        private static final QProperties INSTANCE = new QProperties();
    }

    /**
     * Загрузить параметры если еще не загружены
     *
     * @param netProperty параметры для загрузки
     */
    public void load(INetProperty netProperty) {
        load(netProperty, false);
    }

    /**
     * Загрузить параметры если еще не загружены
     *
     * @param netProperty opciones para descargar
     * @param force если true, то загрузить принудительно даже если уже были загружены
     */
    public void load(INetProperty netProperty, boolean force) {
        if (force || properties == null || properties.isEmpty()) {
            properties = NetCommander.getProperties(netProperty);
            this.netProperty = netProperty;
        }
    }

    /**
     * Настройки по которым происходит загрузка. Инициализируются при первой загрузке.
     */
    private INetProperty netProperty = null;

    /**
     * Загрузить параметры если еще не загружены
     *
     */
    public void reload() {
        if (netProperty != null) {
            properties = NetCommander.getProperties(netProperty);
        }
    }

    /**
     * Может быть NULL, если нет такой сейции
     *
     * @param section имя секции или NULL.
     * @return список параметров.
     */
    public LinkedHashMap<String, QProperty> getSectionProps(String section) {
        return properties.get(section).getProperties();
    }

    /**
     * Получить существующую секцию
     *
     * @param section имя существующей секции
     * @return существующая секция с именем или NULL
     */
    public ServerProps.Section getSection(String section) {
        return properties.get(section);
    }

    /**
     * Получить все параметры списком.
     *
     * @return список всех параметров из БД
     */
    public ArrayList<QProperty> getAllProperties() {
        final ArrayList<QProperty> col = new ArrayList<>();
        properties.values().forEach(sec -> col.addAll(sec.getProperties().values()));
        return col;
    }

    /**
     * Получить все настройки, что лежат в БД
     *
     * @return список секций с параметрами.
     */
    public Collection<ServerProps.Section> getSections() {
        return properties.values();
    }

    /**
     * Получает параметр по имени из секции.
     *
     * @param section название секции
     * @param key название параметра
     * @return запрашиваемый параметер. Может быть NULL если не нашелся параметер.
     */
    public QProperty getProperty(String section, String key) {
        triceParam(section + "->" + key);
        if (key == null) {
            throw new IllegalArgumentException("Key must be not NULL");
        }
        final ServerProps.Section secmap = getSection(section);
        if (secmap == null) {
            return null;
        }
        if (!secmap.getProperties().containsKey(key)) {
            return null;
        }
        return secmap.getProperties().get(key);
    }

    /**
     * Получает параметр по имени из секции.
     *
     * @param section название секции
     * @param key название параметра
     * @param defValue значение по умолчанию.
     * @return запрашиваемый параметер. Или значение по умолчанию.
     */
    public String getProperty(String section, String key, String defValue) {
        triceParam(section + "->" + key + "[=" + defValue + "]");
        if (key == null) {
            return defValue;
        }
        final ServerProps.Section secmap = getSection(section);
        if (secmap == null) {
            return defValue;
        }
        if (!secmap.getProperties().containsKey(key)) {
            return defValue;
        }
        return secmap.getProperties().get(key).getValue();
    }

    /**
     * Проинитить параметры если еще не созданы на сервере.
     *
     * @param netProperty параметры для загрузки
     * @param propList эти параметры проинитить
     */
    public void init(INetProperty netProperty, List<QProperty> propList) {
        properties = NetCommander.initProperties(netProperty, propList);
    }

    /**
     * Сохранить параметры на сервере.
     *
     * @param netProperty параметры для загрузки
     * @param propList эти параметры сохранить
     */
    public void save(INetProperty netProperty, List<QProperty> propList) {
        properties = NetCommander.saveProperties(netProperty, propList);
    }

    private final HashMap<String, Long> trace = new HashMap<>();

    private void triceParam(String str) {
        Long tm = trace.get(str);
        if (tm == null || 1000 * 60 * 60 < System.currentTimeMillis() - tm) {
            trace.put(str, System.currentTimeMillis());
            logProp().info(str);
        }
    }
}

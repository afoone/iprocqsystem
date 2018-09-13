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
package ru.apertum.qsystem.reports.formirovators;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.HttpRequest;
import ru.apertum.qsystem.common.Uses;
import ru.apertum.qsystem.common.QLog;
import ru.apertum.qsystem.common.exceptions.ReportException;
import ru.apertum.qsystem.reports.common.Response;

/**
 *
 * @author Igor Savin
 */
public class DistributionWaitDayUsers extends AFormirovator {

    /**
     * Метод формирования параметров для отчета. В отчет нужно передать некие параметры. Они упаковываются в Мар. Если параметры не нужны, то сформировать
     * пустой Мар.
     *
     * @param driverClassName
     * @param url
     * @param username
     * @param password
     * @param request
     * @return
     */
    @Override
    public Map getParameters(String driverClassName, String url, String username, String password, HttpRequest request) {
        return paramMap;
    }
    /**
     * Для параметров
     */
    final private HashMap<String, Object> paramMap = new HashMap<>();

    /**
     * Метод получения коннекта к базе если отчет строится через коннект. Если отчет строится не через коннект, а формироватором, то выдать null.
     *
     * @param driverClassName
     * @param url
     * @param username
     * @param password
     * @param request
     * @return коннект соединения к базе или null.
     */
    @Override
    public Connection getConnection(String driverClassName, String url, String username, String password, HttpRequest request) {
        final Connection connection;
        try {
            Class.forName(driverClassName);
            connection = DriverManager.getConnection(url, username, password);
        } catch (SQLException | ClassNotFoundException ex) {
            throw new ReportException(StatisticServices.class.getName() + " " + ex);
        }
        return connection;
    }


    @Override
    public Response getDialog(String driverClassName, String url, String username, String password, HttpRequest request, String errorMessage) {
        Response result = getDialog("/ru/apertum/qsystem/reports/web/get_date_distribution_users.html", request, errorMessage);
        final StringBuilder users_select = new StringBuilder();
        try (final Connection conn = getConnection(driverClassName, url, username, password, request);
                final Statement stmt = conn.createStatement();
                final ResultSet rs = stmt.executeQuery("SELECT id, name FROM users ORDER BY name")) {
            while (rs.next()) {
                users_select.append("<option value=").append(rs.getLong(1)).append(">").append(rs.getString(2)).append("\n");
            }
        } catch (SQLException ex) {
            users_select.setLength(0);
            throw new ReportException("Ошибка выполнения запроса для диалога ввода пользователя. " + ex);
        }
        try {
            result.setData(new String(result.getData(), "UTF-8").replaceFirst("#DATA_FOR_TITLE#", "Распределение среднего времени ожидания внутри дня для пользователя:").replaceFirst("#DATA_FOR_USERS#", users_select.toString()).getBytes("UTF-8"));
        } catch (UnsupportedEncodingException ex) {
        }
        users_select.setLength(0);
        return result;
    }

    @Override
    public String validate(String driverClassName, String url, String username, String password, HttpRequest request, HashMap<String, String> params) {
        // проверка на корректность введенных параметров
        QLog.l().logger().trace("Accepted parameters \"" + params.toString() + "\".");
        if (params.size() == 3) {
            // date/user_id/user
            Date date;
            String sdate;
            long user_id;
            String user;
            try {
                date = Uses.FORMAT_DD_MM_YYYY.parse(params.get("date"));
                sdate = (new java.text.SimpleDateFormat("yyyy-MM-dd")).format(date);
                user_id = Long.parseLong(params.get("user_id"));
                user = params.get("user");
            } catch (NumberFormatException | ParseException ex) {
                return "<br>Ошибка ввода параметров! Не все параметры введены корректно (дд.мм.гггг).";
            }
            paramMap.put("sdate", sdate);
            paramMap.put("date", date);
            paramMap.put("user_id", user_id);
            paramMap.put("user", user);
        } else {
            return "<br>Ошибка ввода параметров!";
        }
        return null;
    }
}

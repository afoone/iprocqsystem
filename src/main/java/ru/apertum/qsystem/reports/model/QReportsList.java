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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.LinkedList;
import javax.swing.ComboBoxModel;

import org.apache.http.Header;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import ru.apertum.qsystem.common.QLog;
import ru.apertum.qsystem.common.Uses;
import ru.apertum.qsystem.common.exceptions.ReportException;
import ru.apertum.qsystem.reports.common.RepResBundle;
import ru.apertum.qsystem.reports.common.Response;
import ru.apertum.qsystem.reports.generators.RepCurrentUsers;
import ru.apertum.qsystem.reports.generators.ReportCurrentServices;
import ru.apertum.qsystem.reports.generators.ReportsList;
import ru.apertum.qsystem.reports.net.NetUtil;
import ru.apertum.qsystem.server.Spring;
import ru.apertum.qsystem.server.model.ATListModel;
import ru.apertum.qsystem.server.model.QUser;
import ru.apertum.qsystem.server.model.QUserList;

/**
 * @author Evgeniy Egorov
 * @author afoone@hotmail.com Alfonso Tienda Braulio
 */
public class QReportsList extends ATListModel<QReport> implements ComboBoxModel {

    private QReportsList() {
        super();
    }

    public static QReportsList getInstance() {
        return QResultListHolder.INSTANCE;
    }

    private static class QResultListHolder {

        private static final QReportsList INSTANCE = new QReportsList();
    }

    @Override
    protected LinkedList<QReport> load() {
        final LinkedList<QReport> reports = new LinkedList<>(Spring.getInstance().getHt().loadAll(QReport.class));
        QLog.l().logRep().debug("Uploaded from database" + reports.size() + " reports.");

        passMap = new HashMap<>();
        htmlRepList = "";
        htmlUsersList = "";
        reports.stream().map(report -> {
            addGenerator(report);
            return report;
        }).forEach(report -> {
            htmlRepList = htmlRepList.concat(
                    "<tr>\n"
                            + "<td style=\"text-align: left; padding-left: 60px;\">\n"
                            + "<a href=\"" + report.getHref() + ".html\" target=\"_blank\">"
                            + (RepResBundle.getInstance().present(report.getHref()) ? RepResBundle.getInstance().getStringSafe(report.getHref()) : report.getName())
                            + "</a>\n"
                        //    + "<a href=\"" + report.getHref() + ".rtf\" target=\"_blank\">[RTF]</a>\n"
                        //    + "<a href=\"" + report.getHref() + ".pdf\" target=\"_blank\">[PDF]</a>\n"
                            + "<a href=\"" + report.getHref() + ".xlsx\" target=\"_blank\">[XLSX]</a>\n"
                        //    + "<a href=\"" + report.getHref() + ".csv\" target=\"_blank\">[CSV]</a>\n"
                            + "</td>\n"
                            + "</tr>\n");
            report.setName(RepResBundle.getInstance().present(report.getHref()) ? RepResBundle.getInstance().getStringSafe(report.getHref()) : report.getName());
        });
        /*
         * This is not a report. This is a report list generator that checks the password and the user and generates
         * coocies for the browser, so that the browser would put cookies into the query and thereby the servic "recognize the user".
         * Only the preparation () method is needed here, because no generation is present.
         */
        addGenerator(new ReportsList("reportList", ""));
        /*
         * Report on the current state of the service
         */
        addGenerator(new ReportCurrentServices(Uses.REPORT_CURRENT_SERVICES.toLowerCase(), "/ru/apertum/qsystem/reports/templates/currentStateServices.jasper"));
        /*
         * Report on the current state in the context of users
         */
        addGenerator(new RepCurrentUsers(Uses.REPORT_CURRENT_USERS.toLowerCase(), "/ru/apertum/qsystem/reports/templates/currentStateUsers.jasper"));

        String sel = " selected";
        for (QUser user : QUserList.getInstance().getItems()) {
            // list of users allowed before the reports
            if (user.getReportAccess()) {
                htmlUsersList = htmlUsersList.concat("<option" + sel + ">").concat(user.getName()).concat("</option>\n");
                sel = "";
                if (user.getReportAccess()) {
                    passMap.put(user.getName(), user.getPassword());
                }
            }
        }

        return reports;
    }

    private QReport selected;

    @Override
    public void setSelectedItem(Object anItem) {
        selected = (QReport) anItem;
    }

    @Override
    public Object getSelectedItem() {
        return selected;
    }

    // задания, доступны по их ссылкам
    private static final HashMap<String, IGenerator> GENERATORS = new HashMap<>();

    private static void addGenerator(IGenerator generator) {
        GENERATORS.put(generator.getHref().toLowerCase(), generator);
    }

    private String htmlRepList;

    public String getHtmlRepList() {
        return htmlRepList;
    }

    private String htmlUsersList;

    public String getHtmlUsersList() {
        return htmlUsersList;
    }

    /**
     * User password list name - password
     */
    private HashMap<String, String> passMap;

    public boolean isTrueUser(String userName, String pwd) {
        return pwd.equals(passMap.get(userName));
    }

    /**
     * Generate a report by its name.
     *
     * @param request the request that came from the client
     * @return Report in the form of an array of bytes.
     */
    public synchronized Response generate(HttpRequest request) {
        final long start = System.currentTimeMillis();
        String url = NetUtil.getUrl(request);
        QLog.l().logRep().debug("url = "+url);
        final String nameReport = url.lastIndexOf('.') == -1 ? url.substring(1) : url.substring(1, url.lastIndexOf('.'));
        QLog.l().logRep().debug("Trying to get report "+ nameReport);

        final IGenerator generator = GENERATORS.get(nameReport.toLowerCase());

        QLog.l().logRep().debug("report generator "+ generator);
        // if there is no such report
        if (generator == null) {
            return null;
        }
        // This means that there is such a report and it can be generated
        // but if the report is requested, the password and the user in the cookies must arrive
        // to determine access to reports.
        // Cookie: username =% D0% 90% D0% B4% D0% BC% D0% B8% D0% BD% D0% B8% D1% 81% D1% 82% D1% 80% D0% B0% D1% 82 % D0% BE% D1% 80; password =
        // Check if the access is correct, and if everything is fine, we will generate the report.
        // Otherwise, we'll issue a deny access page
        // But there is a nuance, the formation of the list of reports is also a formulator, and access to it is not by cookies,
        // and on the entered password and the user. This should be checked if the parameters of the password and the user arrived,
        // entered by the user, then ignore the check of cookies. Those. if the genener reportList, then do not check the cookies
        if (!"/reportList.html".equals(url)) {

            final HashMap<String, String> cookie = new HashMap<>();
            for (Header header : request.getHeaders("Cookie")) {
                cookie.putAll(NetUtil.getCookie(header.getValue(), "; "));
            }
            if (cookie.isEmpty()) {
                // if there are no cookies
                QLog.l().logRep().debug("no cookies");
                return getLoginPage();
            }
            final String pass = cookie.get("parol");
            final String usr = cookie.get("username");
            if (pass == null || usr == null ) {
                // cookies not found
                QLog.l().logRep().debug("no cookies. found pass "+pass+" usr: "+usr);

         //       return getLoginPage();
            }
            if (!isTrueUser(usr, pass)) {
                // is not a valid user
                QLog.l().logRep().debug("not valid user "+usr+ "with passwd "+pass);
           //     return getLoginPage();
            }
        }
        System.out.println("Report build: '" + nameReport + "'\n");
        QLog.l().logRep().info("Report generation: '" + nameReport + "'");
        /*
         * Here is the report generation.
         */
        final Response result = generator.process(request);

        QLog.l().logRep().info("Generation of report list is complete. Spent time: " + ((double) (System.currentTimeMillis() - start)) / 1000 + " сек.");
        return result;
    }

    public synchronized byte[] generate(QUser user, String uri, HashMap<String, String> params) {
        final HttpEntityEnclosingRequest r = new BasicHttpEntityEnclosingRequest("POST", uri);
        r.addHeader("Cookie", "username=" + user.getName() + "; password=" + user.getPassword());
        final StringBuilder sb = new StringBuilder();
        params.keySet().stream().forEach(st -> sb.append("&").append(st).append("=").append(params.get(st)));
        final InputStream is = new ByteArrayInputStream(sb.substring(1).getBytes());
        final BasicHttpEntity b = new BasicHttpEntity();
        b.setContent(is);
        r.setEntity(b);
        sb.setLength(0);
        return generate(r).getData();
    }

    /**
     * Download the password and user login page
     *
     * @return страница в виде массива байт.
     */
    private Response getLoginPage() {
        byte[] result = null;
        // Выдаем ресурс  "/ru/apertum/qsystem/reports/web/"
        final InputStream inStream = getClass().getResourceAsStream("/ru/apertum/qsystem/reports/web/login.html");
        if (inStream != null) {
            try {
                result = Uses.readInputStream(inStream);
            } catch (IOException ex) {
                throw new ReportException("Error reading logging resource. " + ex);
            }
        } else {
            final String s = "<html><head><meta http-equiv = \"Content-Type\" content = \"text/html; charset=windows-1251\" ></head><p align=center>The resource for the input was not found.</p></html>";
            return new Response(s.getBytes());
        }
        Response res = null;
        try {
            res = new Response(RepResBundle.getInstance().prepareString(new String(result, "UTF-8")).
                    replaceFirst(Uses.ANCHOR_USERS_FOR_REPORT, getHtmlUsersList()).
                    replaceFirst(Uses.ANCHOR_PROJECT_NAME_FOR_REPORT, Uses.getLocaleMessage("project.name")).
                    getBytes("UTF-8")); //"Cp1251"
        } catch (UnsupportedEncodingException ex) {
        }
        return res;
    }
}

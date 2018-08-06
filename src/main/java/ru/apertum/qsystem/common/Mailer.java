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
package ru.apertum.qsystem.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;

import ru.apertum.qsystem.common.exceptions.ServerException;

/**
 * @author Evgeniy Egorov
 */
public class Mailer {

    private static final String ENCODING = "UTF-8";

    public static void main(String[] args) throws MessagingException, UnsupportedEncodingException {
        sendReporterMail(null, null, null, new File("asd.pdf"));
    }

    public static void sendReporterMailAtFon(String subject, String content, String addrs_to, final String attachment) {
        final Thread t = new Thread(() -> {
            final File attach = new File(attachment);
            try {
                sendReporterMail(subject, content, addrs_to, attach.exists() ? attach : null);
            } catch (MessagingException | UnsupportedEncodingException ex) {
                throw new ServerException("Рассылка не произошла.", ex);
            }
        });
        t.start();
    }

    public static void sendReporterMail(String subject, String content, String addrs_to, File attachment) throws MessagingException, UnsupportedEncodingException {
        Properties props = fetchConfig();

        final Authenticator auth = new MyAuthenticator(props.getProperty("mail.smtp.user"), props.getProperty("mail.password"));
        final Session session = Session.getDefaultInstance(props, auth);

        final MimeMessage msg = new MimeMessage(session);
        String to = addrs_to == null ? props.getProperty("mail.smtp.to") : addrs_to;
        to = to.replaceAll("  ", " ").replaceAll(" ;", ";").replaceAll(" ,", ",").replaceAll(", ", ",").replaceAll("; ", ",").replaceAll(";", ",").replaceAll(" ", ",").replaceAll(",,", ",");
        final String[] ss = to.split(",");
        final ArrayList<InternetAddress> adresses = new ArrayList<>();
        for (String str : ss) {
            if (!"".equals(str.trim())) {
                adresses.add(new InternetAddress(str.trim()));
            }
        }

        msg.setRecipients(Message.RecipientType.TO, adresses.toArray(new InternetAddress[0]));
        msg.setHeader("Content-Type", "text/html;charset=\"UTF-8\"");
        msg.setSubject(subject == null ? props.getProperty("mail.subject") : subject, ENCODING);

        final BodyPart messageBodyPart = new MimeBodyPart();

        File f = new File(props.getProperty("mail.content"));
        if (f.exists()) {
            final StringBuilder sb = new StringBuilder();
            try (final Scanner s = new Scanner(f)) {
                while (s.hasNext()) {
                    sb.append(s.next());
                }
            } catch (FileNotFoundException ex) {
                throw new ServerException(ex);
            }
            messageBodyPart.setContent(content == null ? sb.toString() : content, "text/html; charset=\"UTF-8\"");
            sb.setLength(0);
        } else {
            messageBodyPart.setContent(content == null ? props.getProperty("mail.content") : content, "text/plain; charset=\"UTF-8\"");
        }

        final Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);

        if (attachment != null) {
            final MimeBodyPart attachmentBodyPart = new MimeBodyPart();
            final DataSource source = new FileDataSource(attachment);
            attachmentBodyPart.setDataHandler(new DataHandler(source));
            attachmentBodyPart.setFileName(MimeUtility.encodeText(source.getName()));
            multipart.addBodyPart(attachmentBodyPart);
        }

        msg.setContent(multipart);

        Transport.send(msg);
    }

    /**
     * Open a specific text file containing mail server parameters, and populate a corresponding Properties object.
     *
     * @return props
     */
    public static Properties fetchConfig() {
        if (fMailServerConfig == null) {
            synchronized (MyAuthenticator.class) {
                if (fMailServerConfig != null) {
                    return fMailServerConfig;
                }
                fMailServerConfig = new Properties();
            }
        } else {
            return fMailServerConfig;
        }
        try (InputStream input = new FileInputStream("config/reporter.properties");
             final InputStreamReader inR = new InputStreamReader(input, ENCODING)) {
            //If possible, one should try to avoid hard-coding a path in this
            //manner; in a web application, one should place such a file in
            //WEB-INF, and access it using ServletContext.getResourceAsStream.
            //Another alternative is Class.getResourceAsStream.
            //This file contains the javax.mail config properties mentioned above.
            fMailServerConfig.load(inR);
        } catch (IOException ex) {
            throw new ServerException("Cannot open and load mail server properties file.", ex);
        }
        return fMailServerConfig;
    }

    private static volatile Properties fMailServerConfig;
}

class MyAuthenticator extends Authenticator {

    private final String user_;
    private final String password_;

    MyAuthenticator(String user, String password) {
        this.user_ = user;
        this.password_ = password;
    }

    @Override
    public PasswordAuthentication getPasswordAuthentication() {
        String user = user_;
        String password = password_;
        return new PasswordAuthentication(user, password);
    }
}

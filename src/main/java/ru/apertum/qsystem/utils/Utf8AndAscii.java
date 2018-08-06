/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.apertum.qsystem.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.FileSystemException;
import java.util.Properties;

/**
 * @author Evgeniy Egorov
 */
public class Utf8AndAscii {

    public static void main(String[] args) throws IOException {

        String folderIn = null;
        String regex1 = null;
        int convert = 0;
        for (int i = 0; i < args.length; i++) {
            final String arg = args[i];
            if ("-toascii".equalsIgnoreCase(arg)) {
                System.out.println("To ascii");
                convert = 1;
            }
            if ("-toutf8".equalsIgnoreCase(arg)) {
                System.out.println("To utf8");
                convert = 2;
            }

            if ("-folder".equalsIgnoreCase(arg)) {
                if (i + 1 < args.length) {
                    folderIn = args[i + 1];
                }
            }

            if ("-regex".equalsIgnoreCase(arg)) {
                if (i + 1 < args.length) {
                    regex1 = args[i + 1];
                }
            }
        }

        if (regex1 == null) {
            regex1 = ".*";
        }
        //folderIn = "D:\\ro";
        //convert = 2;
        //final String regex = "AFBo.*(_ro_RO).*";
        final String regex = regex1;

        if (folderIn == null || !new File(folderIn).isDirectory()) {
            System.out.println();
            System.out.println(" Help about Utf8AndAscii:");
            System.out.println(" Util for converting text files ASCII, using Unicode escapes (\"\\uxxxx\" notation), to UTF-8 and vice versa. ");
            System.out.println(" java -cp dist/QSystem.jar ru.apertum.qsystem.utils.Utf8AndAscii [-toascii|-toutf8] -folder folder/with/files [-regex regex/choose/files] ");
            System.out.println();
            System.out.println(" ASCII to UTF-8");
            System.out.println(" java -cp dist/QSystem.jar ru.apertum.qsystem.utils.Utf8AndAscii -toascii -folder folder/with/files -regex regex/choose/files ");
            System.out.println();
            System.out.println(" UTF-8 to ASCII");
            System.out.println(" java -cp dist/QSystem.jar ru.apertum.qsystem.utils.Utf8AndAscii -toutf8 -folder folder/with/files -regex regex/choose/files ");
            System.out.println();
            System.out.println(" select files");
            System.out.println(" java -cp dist/QSystem.jar ru.apertum.qsystem.utils.Utf8AndAscii folder/with/files regex/choose/files ");
            System.out.println();
            return;
        }
        System.out.println("Total files: " + process(convert, new File(folderIn), regex));

    }

    private static int process(int convert, File fold, String regex) throws IOException {
        if (convert == 2) {
            int cnt = 0;
            final File[] files = fold.listFiles((File pathname) -> pathname.getName().matches(regex));
            if (files == null) {
                throw new FileSystemException(fold.getName(), fold.getPath(), "Reading folder was crashed.");
            }
            for (File file : files) {

                if (file.isDirectory()) {
                    System.out.println("Directory \"" + file.getAbsolutePath() + "\"");
                    cnt += process(convert, file, regex);
                    cnt--;
                    continue;
                }
                System.out.println("---> " + file.getAbsolutePath());
                if (convert != 0) {
                    Properties prop = new Properties();
                    try (FileInputStream fis = new FileInputStream(file)) {
                        prop.load(fis);
                    }

                    Properties prop2 = new Properties();

                    prop.keySet().stream().forEach((o) -> {
                        //System.out.println(o + "=" + prop.getProperty((String) o));
                        prop2.setProperty((String) o, prop.getProperty((String) o));
                    });
                    try (OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(file.getAbsolutePath()), "UTF-8")) {
                        prop2.store(osw, "Utf8AndAncii");
                    }
                }
            }
            return files.length + cnt;

        } else {
            final File[] files = fold.listFiles((File pathname) -> pathname.getName().matches(regex));
            if (files == null) {
                throw new FileSystemException(fold.getName(), fold.getPath(), "Reading folder was crashed.");
            }
            int cnt = 0;
            for (File file : files) {

                if (file.isDirectory()) {
                    System.out.println("Directory \"" + file.getAbsolutePath() + "\"");
                    cnt += process(convert, file, regex);
                    cnt--;
                    continue;
                }
                System.out.println("---> " + file.getAbsolutePath());
                if (convert != 0) {
                    Properties prop = new Properties();
                    try (FileInputStream fis = new FileInputStream(file)) {
                        final InputStreamReader reader = new InputStreamReader(fis, "UTF-8");
                        try {
                            prop.load(reader);
                        } finally {
                            reader.close();
                        }
                    }

                    Properties prop2 = new Properties();

                    prop.keySet().stream().forEach((o) -> {
                        //System.out.println(o + "=" + prop.getProperty((String) o));
                        prop2.setProperty((String) o, prop.getProperty((String) o));
                    });
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        prop2.store(fos, "Utf8AndAncii");
                    }

                }
            }
            return files.length + cnt;
        }
    }

}

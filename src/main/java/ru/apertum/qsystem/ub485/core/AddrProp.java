/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.apertum.qsystem.ub485.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

import ru.apertum.qsystem.common.QLog;
import ru.apertum.qsystem.common.Uses;
import ru.apertum.qsystem.common.exceptions.ServerException;

/**
 * @author Evgeniy Egorov
 */
public class AddrProp {

    private final HashMap<Long, ButtonDevice> addrs = new HashMap<>();

    public HashMap<Long, ButtonDevice> getAddrs() {
        return addrs;
    }

    private static final File ADDR_FILE = new File("config/qub.adr");

    private AddrProp() {
        if (!ADDR_FILE.exists()) {
            throw new ServerException(ADDR_FILE.getAbsolutePath() + " not exists.");
        }
        try (FileInputStream fis = new FileInputStream(ADDR_FILE); Scanner s = new Scanner(fis)) {
            while (s.hasNextLine()) {
                final String line = s.nextLine().trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    final String[] ss = line.split("=");
                    if (ss.length != 2) {
                        QLog.l().logger().error(ADDR_FILE.getAbsolutePath() + " contaned an error, line: \"" + line + "\"");
                        continue;
                    }
                    final String[] ssl = ss[1].split(" ");
                    if (!(Uses.isInt(ss[0]) && Uses.isInt(ssl[0]) && (ssl.length == 2 ? Uses.isInt(ssl[1]) : true))) {
                        QLog.l().logger().error(ADDR_FILE.getAbsolutePath() + " contaned an error: \"" + line + "\" value \"" + Arrays.toString(ssl) + "\" is bad.");
                        continue;
                    }
                    addrs.put(Long.valueOf(ss[0]), new ButtonDevice(Long.valueOf(ss[0]), Byte.parseByte(ssl[0]), ssl.length == 1 ? null : Long.parseLong(ssl[1])));
                    QLog.l().logger().trace(ADDR_FILE.getAbsolutePath() + " Read line: \"" + line + "\"");
                }
            }
        } catch (IOException ex) {
            System.err.println(ex);
            throw new RuntimeException(ex);
        }
    }

    public static AddrProp getInstance() {
        return AddrPropHolder.INSTANCE;
    }

    private static class AddrPropHolder {

        private static final AddrProp INSTANCE = new AddrProp();
    }

    public ButtonDevice getAddr(Long userId) {
        return addrs.get(userId);
    }

    public ButtonDevice getAddrByRSAddr(byte rsAddr) {
        for (ButtonDevice adr : AddrProp.getInstance().getAddrs().values().toArray(new ButtonDevice[0])) {
            if (adr.addres == rsAddr) {
                return adr;
            }
        }
        return null;
    }

    public static void main(String[] ss) {
        System.out.println("addrs:");
        getInstance().addrs.keySet().stream().forEach(aLong ->
                System.out.println(aLong + "=" + getInstance().getAddr(aLong).addres
                        + " " + getInstance().getAddr(aLong).redirectServiceId));
    }
}

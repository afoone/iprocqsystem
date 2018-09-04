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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URL;
import javax.sound.sampled.*;
import javax.swing.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.apertum.qsystem.client.QProperties;

import static ru.apertum.qsystem.common.QLog.log;

import ru.apertum.qsystem.common.model.QCustomer;
import ru.apertum.qsystem.server.ServerProps;
import ru.apertum.qsystem.server.model.QService;

/**
 * La clase de reproducción de recursos de sonido y archivos. Crea un hilo separado para cada pérdida, pero está sincronizado. Por lo tanto, todos los recursos se pierden
 * uno tras otro y esto no ralentizará el flujo principal. La reproducción de un montón de pequeños archivos tiene errores, superposición, etc. en otros.
 * @author Evgeniy Egorov
 * @author afoone@hotmail.com
 */
public class SoundPlayer implements Runnable {

    // ruta a los archivos de audio
    public static final String SAMPLES_PACKAGE = "/ru/apertum/qsystem/server/sound/";

    public SoundPlayer(LinkedList<String> resourceList) {
        this.resourceList = resourceList;
    }

    /**
     * Тут храним имя ресурса для загрузки
     */
    private final LinkedList<String> resourceList;

    /**
     * reproducir recurso de sonido
     *
     * @param resourceName nombre del recurso reproducible
     */
    public static void play(String resourceName) {
        final LinkedList<String> resourceList = new LinkedList<>();
        resourceList.add(resourceName);
        play(resourceList);
    }

    /**
     * Проиграть набор звуковых ресурсов
     *
     * @param resourceList список имен проигрываемых ресурсов
     */
    public static void play(LinkedList<String> resourceList) {
        // и запускаем новый вычислительный поток (см. ф-ю run())
        final Thread playThread = new Thread(new SoundPlayer(resourceList));
        //playThread.setDaemon(true);
        playThread.setPriority(Thread.NORM_PRIORITY);
        playThread.start();
    }

    public static void printAudioFormatInfo(AudioFormat audioformat) {
        System.out.println("*****************************************");
        System.out.println("Format: " + audioformat.toString());
        System.out.println("Encoding: " + audioformat.getEncoding());
        System.out.println("SampleRate:" + audioformat.getSampleRate());
        System.out.println("SampleSizeInBits: " + audioformat.getSampleSizeInBits());
        System.out.println("Channels: " + audioformat.getChannels());
        System.out.println("FrameSize: " + audioformat.getFrameSize());
        System.out.println("FrameRate: " + audioformat.getFrameRate());
        System.out.println("BigEndian: " + audioformat.isBigEndian());
        System.out.println("*****************************************\n");
    }

    /**
     * Asks the user to select a file to play.
     *
     * @return
     */
    public File getFileToPlay() {
        File file = null;
        JFrame frame = new JFrame();
        JFileChooser chooser = new JFileChooser(".");
        int returnvalue = chooser.showDialog(frame, "Select File to Play");
        if (returnvalue == JFileChooser.APPROVE_OPTION) {
            file = chooser.getSelectedFile();
        }
        return file;
    }

    @Override
    public void run() {
        doSounds(this, resourceList);
    }

    /**
     * Листенер, срабатываюшщий при начале проигрывания семплов
     */
    private static ActionListener startListener = null;

    public static ActionListener getStartListener() {
        return startListener;
    }

    public static void setStartListener(ActionListener startListener) {
        SoundPlayer.startListener = startListener;
    }

    /**
     * Событие завершения проигрывания семплов
     */
    private static ActionListener finishListener = null;

    public static ActionListener getFinishListener() {
        return finishListener;
    }

    public static void setFinishListener(ActionListener finishListener) {
        SoundPlayer.finishListener = finishListener;
    }

    synchronized private static void doSounds(Object o, LinkedList<String> resourceList) {
        if (startListener != null) {
            startListener.actionPerformed(new ActionEvent(o, 1, "start do sounds"));
        }
        resourceList.stream().forEach((res) -> {
            doSound(o, res);
        });
        if (finishListener != null) {
            finishListener.actionPerformed(new ActionEvent(o, 1, "finish do sounds"));
        }
    }

    synchronized private static void doSound(Object o, String resourceName) {
        log().debug("Try to play sound \"" + resourceName + "\"");
        AudioInputStream ais = null;
        try {
            final URL url = Object.class.getResource(resourceName);
            ais = url == null ? AudioSystem.getAudioInputStream(new File(resourceName)) : AudioSystem.getAudioInputStream(url);
            //get the AudioFormat for the AudioInputStream 
            AudioFormat audioformat = ais.getFormat();
            //printAudioFormatInfo(audioformat);
            //ULAW & ALAW format to PCM format conversion 
            if ((audioformat.getEncoding() == AudioFormat.Encoding.ULAW)
                    || (audioformat.getEncoding() == AudioFormat.Encoding.ALAW)) {
                AudioFormat newformat = new AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED,
                        audioformat.getSampleRate(),
                        audioformat.getSampleSizeInBits() * 2,
                        audioformat.getChannels(),
                        audioformat.getFrameSize() * 2,
                        audioformat.getFrameRate(),
                        true);
                ais = AudioSystem.getAudioInputStream(newformat, ais);
                audioformat = newformat;
                //printAudioFormatInfo(audioformat);
            }
            //checking for a supported output line 
            DataLine.Info datalineinfo = new DataLine.Info(SourceDataLine.class, audioformat);
            if (!AudioSystem.isLineSupported(datalineinfo)) {
                System.out.println("Line matching " + datalineinfo + " is not supported.");
            } else {
                byte[] sounddata;
                try (SourceDataLine sourcedataline = (SourceDataLine) AudioSystem.getLine(datalineinfo)) {
                    sourcedataline.open(audioformat);
                    sourcedataline.start();
                    int framesizeinbytes = audioformat.getFrameSize();
                    int bufferlengthinframes = sourcedataline.getBufferSize() / 8;
                    int bufferlengthinbytes = bufferlengthinframes * framesizeinbytes;
                    sounddata = new byte[bufferlengthinbytes];
                    int numberofbytesread;
                    while ((numberofbytesread = ais.read(sounddata)) != -1) {
                        sourcedataline.write(sounddata, 0, numberofbytesread);
                    }
                    int frPos = -1;
                    while (frPos != sourcedataline.getFramePosition()) {
                        frPos = sourcedataline.getFramePosition();
                        Thread.sleep(50);
                    }
                }
            }

            //printAudioFormatInfo(audioformat);
        } catch (InterruptedException ex) {
            log().error("InterruptedException: " + ex);
        } catch (LineUnavailableException lue) {
            log().error("LineUnavailableException: " + lue.toString());
        } catch (UnsupportedAudioFileException uafe) {
            log().error("UnsupportedAudioFileException: " + uafe.toString());
        } catch (IOException ioe) {
            log().error("IOException: " + ioe.toString());
        } finally {
            try {
                if (ais != null) {
                    ais.close();
                }
            } catch (IOException ex) {
                log().error("IOException при освобождении входного потока медиаресурса: " + ex);
            }
        }
    }

    /**
     * Разбить фразу на звуки и сформировать набор файлов для воспроизведения. Упрощенный вариант с поиском существующих семплов.
     *
     * @param path   путь, где лежать звуковые ресурсы, это могут быть файлы на диске или ресурсы в jar
     * @param phrase фраза для разбора
     * @return список файлов для воспроизведения фразы
     */
    public static LinkedList<String> toSoundSimple2(String path, String phrase) {
        final LinkedList<String> res = new LinkedList<>();

        // Разделим на буквы и цыфры
        Matcher m = Pattern.compile("\\d").matcher(phrase);

        // Добавим лидирующие буквы если они есть во фразе и в ресурсах
        if (m.find()) {
            for (int i = 0; i < m.start(); i++) {
                String elem = phrase.substring(i, i + 1);
                final String fileName = reRus(path, elem.toLowerCase());
                if (fileName == null) {
                    continue;
                }
                final String resource = path + fileName.toLowerCase() + ".wav";
                // сэмплы букав должны быть в ресурсах
                final InputStream stream = resource.getClass().getResourceAsStream(resource);
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e) {
                    }
                    res.add(resource);
                }
            }
            phrase = phrase.substring(m.start());
        }

        m = Pattern.compile("\\D").matcher(phrase);
        final LinkedList<String> last = new LinkedList<>();

        // Добавим лидирующие буквы если они есть во фразе и в ресурсах
        if (m.find()) {
            final int b = m.start();
            for (int i = b; i < phrase.length(); i++) {
                String elem = phrase.substring(i, i + 1);
                final String fileName = reRus(path, elem.toLowerCase());
                if (fileName == null) {
                    continue;
                }
                final String resource = path + fileName.toLowerCase() + ".wav";
                // сэмплы букав должны быть в ресурсах
                final InputStream stream = resource.getClass().getResourceAsStream(resource);
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e) {
                    }
                    last.add(resource);
                }
            }
            phrase = phrase.substring(0, b);
        }

        // ну теперь расщепим цифры, найдеи под них ресурсы и сложим в список для воспроизведения
        String lastAdded = "";
        for (int i = 0; i < phrase.length(); i++) {

            final String elem = phrase.substring(i).toLowerCase();
            //System.out.println("-" + elem + "_" + isNum(elem));
            String file = path + elem + ".wav";
            final InputStream streamUp = file.getClass().getResourceAsStream(file);
            if (streamUp != null) {
                try {
                    streamUp.close();
                } catch (IOException e) {
                }
                // тут иногда перед произнесением очередных цыфр надо добавить типа препозицию, типа "тридцать и пять"
                if (lastAdded.length() == 2 && elem.length() == 1) {
                    final String fileAnd = path + "and.wav";
                    final InputStream stream = fileAnd.getClass().getResourceAsStream(fileAnd);
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException e) {
                        }
                        res.add(fileAnd);
                    }


                }
                //System.out.println(elem + " + " + file);
                res.add(file);
                break;
            } else {
                String elemZer = (elem.substring(0, 1) + "00000000000000000000000000").substring(0, elem.length());
                String elemEnd = elem.length() > 1 ? elem.substring(1) : "";
                boolean needAdd = true;
                // а вот если есть записи с интонациеей продолжения специально для больших порядков, типа 100_ ...
                if (elemEnd.matches("[0-9]*[1-9]+[0-9]*")) {
                    file = path + elemZer + "_.wav";
                    final InputStream stream = file.getClass().getResourceAsStream(file);
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException e) {
                        }
                        lastAdded = elemZer;
                        res.add(file);
                        needAdd = false;
                    }


                }
                if (needAdd) {
                    file = path + elemZer + ".wav";
                    final InputStream stream = file.getClass().getResourceAsStream(file);
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException e) {
                        }
                        lastAdded = elemZer;
                        res.add(file);
                    }

                }
            }

        }
        res.addAll(last);
        return res;
    }

    public static void main(String[] args) {
        Uses.loadPlugins("./plugins/");
        String num;
        num = "A123";
        String point = "117";

        SoundPlayer.inviteClient(new QService(), new QCustomer(), num, point, true);

    }

    // private static boolean isRus(String elem) {
    //     return "йцукенгшщзхъфывапролджэячсмитьбю".indexOf(elem.toLowerCase()) != -1;
    // }
    private static volatile HashMap<String, String> latters = null;
    private static String preffix = "";

    private static String reRus(String path, String elem) {
        if (latters == null) {
            synchronized (SoundPlayer.class) {
                if (latters == null) {
                    latters = new HashMap<>();
                    try {
                        final InputStream ris = elem.getClass().getResourceAsStream(path + "latters.properties");
                        if (ris == null) {
                            return null;
                        }
                        try (BufferedReader br = new BufferedReader(new InputStreamReader(ris, Charset.forName("utf8")))) {
                            String line;
                            boolean f = true;
                            while ((line = br.readLine()) != null) {
                                line = (f ? line.substring(1) : line);
                                f = false;
                                //System.out.println(line);
                                String[] ss = line.split("=");
                                if (ss[0].startsWith("pref")) {
                                    preffix = ss[1];
                                } else {
                                    latters.put(ss[0], ss[1]);
                                }

                            }
                        }
                    } catch (IOException ex) {
                        log().error("Не найден зкуковой ресурс или что-то в этом роде. " + ex);
                        return null;
                    }
                } else {
                    log().trace("Thread racing is present.");
                }

            }
        }

        //final String is__ru = "й ц у к е н г ш щ з х ъ ф ы в а п р о л д ж э я ч с м и т ь б ю ё ";
        //final String not_ru = "iic u k e n g shghz x zzf yyv a p r o l d jzeeiachs m i t ccb iuio";
        //int pos = is__ru.indexOf(elem.toLowerCase());
        final String ns = latters.get(elem) == null ? elem : preffix + latters.get(elem); //not_ru.substring(pos, pos + 2).trim().toLowerCase();
        return ns;
    }

    private static boolean isNum(char elem) {
        return '1' == elem || '2' == elem || '3' == elem || '4' == elem || '5' == elem || '6' == elem || '7' == elem || '8' == elem || '9' == elem || '0' == elem;
    }

    private static boolean isZero(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (!('0' == str.charAt(i) || '_' == str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Проговорить вызов клиента голосом
     *
     * @param service
     * @param customer
     * @param clientNumber номер вызываемого клиента
     * @param pointNumber  номер кабинета, куда вызвали
     * @param isFirst
     */
    public static void inviteClient(QService service, QCustomer customer, String clientNumber, String pointNumber, boolean isFirst) {
        // Для начала найдем шаблон
        QService tempServ = service;
        while ((tempServ.getSoundTemplate() == null || tempServ.getSoundTemplate().startsWith("0")) && tempServ.getParent() != null) {
            tempServ = tempServ.getParent();
        }
        if (tempServ.getSoundTemplate() == null || tempServ.getSoundTemplate().startsWith("0") || tempServ.getSoundTemplate().split("#").length == 0) {
            return;
        }

        final String[] parts = tempServ.getSoundTemplate().split("#");

        int gong = 1;
        if (parts[0].length() > 1) {
            switch (parts[0].substring(1, 2)) {
                case "1":
                    gong = 1;
                    break;
                case "2":
                    gong = 2;
                    break;
                case "3":
                    gong = 3;
                    break;
                default:
                    gong = 1;
            }
        }
        boolean client = false;
        if (parts[0].length() > 2) {
            client = "1".equals(parts[0].substring(2, 3));
        }
        boolean cl_num = false;
        if (parts[0].length() > 3) {
            cl_num = "1".equals(parts[0].substring(3, 4));
        }
        int go_to = 5;
        if (parts[0].length() > 4) {
            switch (parts[0].substring(4, 5)) {
                case "1":
                    go_to = 1;
                    break;
                case "2":
                    go_to = 2;
                    break;
                case "3":
                    go_to = 3;
                    break;
                case "4":
                    go_to = 4;
                    break;
                case "5":
                    go_to = 5;
                    break;
                default:
                    go_to = 5;
            }
        }
        boolean go_num = false;
        if (parts[0].length() > 5) {
            go_num = parts[0].endsWith("1");
        }

        final LinkedList<String> res = new LinkedList<>();
        // путь к звуковым файлам
        String path = SAMPLES_PACKAGE;

        // У услуги есть признак плагина специального для нее
        if (parts.length > 1 && !parts[1].isEmpty() && QPlugins.get().getPluginByName(QPlugins.Type.QSOUND, parts[1]) != null) {
            final QPlugins.QPlugin plugin = QPlugins.get().getPluginByName(QPlugins.Type.QSOUND, parts[1]);
            path = plugin.getPkg() + (plugin.getPkg().endsWith("/") ? "" : "/");
        } else {
            // нет признака или плагина. Ищем по признаку использования плагинов персонально по языку, выбранному пользователем.
            if (parts.length > 2 && "1".equals(parts[2]) && QPlugins.get().getPluginByLng(QPlugins.Type.QSOUND, customer.getLanguage()) != null) {
                final QPlugins.QPlugin plugin = QPlugins.get().getPluginByLng(QPlugins.Type.QSOUND, customer.getLanguage());
                path = plugin.getPkg() + (plugin.getPkg().endsWith("/") ? "" : "/");
            }
        }

        if ((isFirst && gong == 3) || gong == 2) {
            final String dingFilePath = ServerProps.getInstance().getProperty(QProperties.SECTION_SERVER, "ding.wav", "her");
            res.add(Files.exists(Paths.get(dingFilePath)) ? dingFilePath : (path + "ding.wav"));
        }

        if (!(isFirst && gong == 3)) {
            if (client) {
                res.add(path + "client.wav");
            }
            if (cl_num) {
                res.addAll(toSoundSimple2(path, clientNumber));
            }
            switch (go_to) {
                case 1:
                    res.add(path + "tocabinet.wav");
                    break;
                case 2:
                    res.add(path + "towindow.wav");
                    break;
                case 3:
                    res.add(path + "tostoika.wav");
                    break;
                case 4:
                    res.add(path + "totable.wav");
                    break;
                case 5:
                    // Это мы пропускаем произношение пункта приема.
                    break;
                default:
                    throw new IllegalArgumentException("Bad number = " + go_to);

            }
            if (go_num) {
                res.addAll(toSoundSimple2(path, pointNumber));
            }
        }
        SoundPlayer.play(res);
    }
}

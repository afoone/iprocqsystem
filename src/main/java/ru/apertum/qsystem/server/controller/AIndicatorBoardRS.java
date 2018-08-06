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

import static ru.apertum.qsystem.common.QLog.log;
import ru.apertum.qsystem.common.exceptions.ServerException;
import ru.evgenic.rxtx.serialPort.ISerialPort;

/**
 * Абстрактный класс для обеспечения взаимодействия гирлянды табло с СОМ-портом
 *
 * @author Evgeniy Egorov
 */
public abstract class AIndicatorBoardRS extends AIndicatorBoard {

    /**
     * COM порт через который будем работать с герляндой.
     */
    private ISerialPort serialPort;

    public ISerialPort getSerialPort() {
        return serialPort;
    }

    /**
     * Так через Spring мы установим объект работы с COM портом.
     *
     * @param serialPort этот рапаметр определяется в Spring
     */
    public void setComPort(ISerialPort serialPort) {
        this.serialPort = serialPort;
        this.serialPort.setLoggerListener((String message, boolean isError) -> {
            if (isError) {
                log().error(message);
            } else {
                log().debug(message);
            }
        });
        this.serialPort.setExceptionListener((String message) -> {
            throw new ServerException(message);
        });
        log().trace("Определили СОМ-порт \"" + serialPort.getName() + "\"");
    }

}

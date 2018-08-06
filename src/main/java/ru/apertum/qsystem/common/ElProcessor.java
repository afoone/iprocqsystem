/*
 * Copyright (C) 2017 Apertum Project LLC
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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;

/**
 * Синглтон предобразования строк с включениями EL ${...}. Потокобезопасный должен быть. Используется библиотека commons-jexl3.
 *
 * @author Evgeniy Egorov
 */
public class ElProcessor {

    public static void main(String[] strs) {
        Map<String, Object> ctx = new HashMap<>();
        ctx.put("foo", 101505);

        System.out.println(ElProcessor.get().process("asdasd ${foo}asdasd ${foo}asdasd ${foo}asd", ctx));
        System.out.println("************************************************");
        System.out.println(ElProcessor.get().process("${foo}asdasd ${foo}asdasd ${foo}asd", ctx));
        System.out.println("************************************************");
        System.out.println(ElProcessor.get().process("asdasd ${foo}asdasd ${foo}asdasd ${foo}", ctx));
        System.out.println("************************************************");
        System.out.println(ElProcessor.get().process("${foo}", ctx));
        System.out.println("************************************************");
        System.out.println(ElProcessor.get().process("asdasd", ctx));
        System.out.println("************************************************");
    }

    private ElProcessor() {
    }

    public static ElProcessor get() {
        return ElProcessorHolder.INSTANCE;
    }

    private static class ElProcessorHolder {

        private static final ElProcessor INSTANCE = new ElProcessor();
    }

    public static final String ONLY_LETTERS_REGEX = "[\\W&&[^а-яА-Я]]+?";

    private final Pattern p = Pattern.compile("\\$\\{.+?\\}");

    /**
     * Вычисляет EL выражения, найденные в строке, возвращяет результат.
     *
     * @param txt     строка с включенными в нее EL выражениями. Может быть NULL, в этом случае вернется строка "null".
     * @param context Контекст вычислетия, т.е. списов POJO классов, доступных по неким строковым именам - ключам этого ассоциированного массива. Вычисления
     *                кэшируются для определенного контекста. Если содерживое контекста изменилось, т.е. содержащиеся там POJO классы, то пересоздать сам класс контекста,
     *                положить в него данные и передать новый класс для вычислений. Иначе из кэша будут использованы вычисленные ранее значения.
     * @return Результирующая строка.
     */
    public String process(String txt, Map<String, Object> context) {
        if (txt == null) {
            return "null";
        }

        final StringBuilder sb = new StringBuilder();
        txt = txt.replace("\r", "").replace("\n", "");
        final Matcher m = p.matcher(txt);
        int f = 0;
        // кэшируем вычесленные значения для этого контекста. Для одного потока могли много раз вычислять для разных контекстов.
        final HashMap<String, String> cache = getCache(context);
        while (m.find()) {
            String exp = txt.substring(m.start() + 2, m.end() - 1);
            sb.append(txt.substring(f, m.start()));
            f = m.end();
            // Внимание. У нас есть синтаксис для значений переменных аналогичный EL, т.е. ${xxx}. Учесть и удалить при переходе на EL во всех местах.
            if (exp.matches("\\w*|\\d*") && !context.containsKey(exp)) {
                sb.append("${").append(exp).append("}");
            } else {
                final String eval = cache.get(exp) == null ? eval(exp, context) : cache.get(exp);
                cache.put(exp, eval);
                sb.append(eval);
            }
        }
        sb.append(txt.substring(f));
        final String res = sb.toString();
        sb.setLength(0);
        return res;
    }

    /**
     * Вычисление EL выражения.
     *
     * @param jexlExp Правильное EL выражение без лишних строк и символов.
     * @param context Контекст вычислетия, т.е. списов POJO классов, доступных по неким строковым именам - ключам этого ассоциированного массива.
     * @return Объект-результат вычислений.
     */
    public Object evaluate(String jexlExp, Map<String, Object> context) {
        if (jexlExp == null) {
            return null;
        }

        // Create a context and add data
        final JexlContext jexlContext = makeContext(context);

        // Create or retrieve an engine
        final JexlEngine jexl = jexlEngine.get();
        // Create an expression
        final JexlExpression e = jexl.createExpression(jexlExp.replace("\r", "").replace("\n", ""));
        // Now evaluate the expression, getting the result
        return e.evaluate(jexlContext);
    }

    /**
     * Создадим контекст и положим туда данные.
     *
     * @param context Данные
     * @return Контекст с данными
     */
    private JexlContext makeContext(Map<String, Object> context) {
        final JexlContext jexlContext = new MapContext();
        context.entrySet().forEach(ctx -> jexlContext.set(ctx.getKey(), ctx.getValue()));
        return jexlContext;
    }

    /**
     * Разнесем по потокам. Потоки долгоживущие для клиентской сессии. Умер поток - удалится и JexlEngine. Если потоки короткоживущие будут вызывать
     * преобразователь, то переделать на pool.
     */
    private final ThreadLocal<JexlEngine> jexlEngine = new ThreadLocal<JexlEngine>() {
        @Override
        protected JexlEngine initialValue() {
            return new JexlBuilder().create();
        }
    };

    /**
     * Кэши вычислений внутри одного потока для конкрекных контекстов. Накопленный кэш почистится после удаления привязанного к нему потоку.
     */
    private final ThreadLocal<HashMap<Map<String, Object>, HashMap<String, String>>> cacheCtx = new ThreadLocal<HashMap<Map<String, Object>, HashMap<String, String>>>() {
        @Override
        protected HashMap<Map<String, Object>, HashMap<String, String>> initialValue() {
            return new HashMap<>();
        }
    };

    /**
     * Кэшируем вычесленные значения для этого контекста. Для одного потока могли много раз вычислять для разных контекстов.
     *
     * @param context Данные для которых будет кэш.
     * @return Сам кэш в виже мапы.
     */
    private HashMap<String, String> getCache(Map<String, Object> context) {
        if (cacheCtx.get().get(context) == null) {
            cacheCtx.get().put(context, new HashMap<>());
        }
        return cacheCtx.get().get(context);
    }

    /**
     * Вычисление EL в строку.
     *
     * @param jexlExp выражение EL
     * @param context Контекст вычислетия, т.е. списов POJO классов, доступных по неким строковым именам - ключам этого ассоциированного массива.
     * @return преобразованная строка.
     */
    private String eval(String jexlExp, Map<String, Object> context) {
        final Object o = evaluate(jexlExp, context);
        return o == null ? "null" : o.toString(); // тут оперируем только строковыми предствавлениями.
    }

}

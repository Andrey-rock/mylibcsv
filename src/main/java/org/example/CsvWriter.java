package org.example;

import java.lang.reflect.Field;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Реализация интерфейса Writable для сохранения объектов Person и Student в CSV формате.
 * Поддерживает различные разделители для разных типов объектов:
 * - Для Person используется запятая как разделитель столбцов
 * - Для Student используется точка с запятой как разделитель столбцов, а оценки разделяются запятыми
 *
 * <p>Пример использования:
 * <pre>
 * {@code
 * Writable writer = new CsvWriter();
 * writer.writeToFile(personsList, "persons.csv");
 * writer.writeToFile(studentsList, "students.csv");
 * }
 * </pre>
 *
 * @author Andrei Bronskii
 * @email andrei.bronskijj@mail.ru
 * @version 1.0
 * @since 2025
 */
public class CsvWriter implements Writable {

    /**
     * Сохраняет список объектов в CSV файл с использованием рефлексии.
     * Автоматически определяет поля объектов и создает соответствующий CSV формат.
     * Null элементы в списке игнорируются.
     *
     * @param data список объектов для сохранения
     * @param fileName имя файла для сохранения данных
     * @throws IllegalArgumentException если произошла ошибка при работе с рефлексией
     *
     * @apiNote Метод автоматически создает заголовки CSV файла на основе имен полей класса
     *          и экранирует специальные символы. Для List полей используется запятая как разделитель.
     *          В случае ошибок записи выводит сообщение в стандартный поток ошибок.
     */
    @Override
    public void writeToFile(List<?> data, String fileName) {
        if (data == null || data.isEmpty()) {
            System.out.println("No data to write");
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            // Фильтруем null элементы
            List<?> filteredData = data.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (filteredData.isEmpty()) {
                System.out.println("No non-null data to write");
                return;
            }

            // Определяем тип объектов по первому ненулевому элементу
            Object firstElement = filteredData.get(0);
            Class<?> clazz = firstElement.getClass();

            // Получаем все поля класса (включая приватные)
            Field[] fields = clazz.getDeclaredFields();

            // Записываем заголовок
            writeHeader(writer, fields);

            // Записываем данные
            writeData(writer, filteredData, fields);

            System.out.println("Data successfully written to " + fileName);

        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    /**
     * Записывает заголовок CSV файла на основе имен полей класса.
     * Использует точку с запятой как разделитель столбцов.
     *
     * @param writer BufferedWriter для записи данных
     * @param fields массив полей класса
     * @throws IOException если произошла ошибка ввода-вывода
     */
    private void writeHeader(BufferedWriter writer, Field[] fields) throws IOException {
        String header = java.util.Arrays.stream(fields)
                .map(field -> {
                    // Преобразуем camelCase в более читаемый формат (опционально)
                    String fieldName = field.getName();
                    return escapeCsvField(formatFieldName(fieldName));
                })
                .collect(Collectors.joining(";"));

        writer.write(header);
        writer.newLine();
    }

    /**
     * Записывает данные объектов в CSV формате с использованием рефлексии.
     *
     * @param writer BufferedWriter для записи данных
     * @param data список объектов для сохранения
     * @param fields массив полей класса
     * @throws IOException если произошла ошибка ввода-вывода
     */
    private void writeData(BufferedWriter writer, List<?> data, Field[] fields)
            throws IOException {

        for (Object obj : data) {
            if (obj == null) continue;

            String line = java.util.Arrays.stream(fields)
                    .map(field -> {
                        try {
                            // Делаем поле доступным (для приватных полей)
                            field.setAccessible(true);
                            Object value = field.get(obj);
                            return convertFieldToString(value);
                        } catch (IllegalAccessException e) {
                            System.err.println("Error accessing field " + field.getName() + ": " + e.getMessage());
                            return "";
                        }
                    })
                    .map(this::escapeCsvField)
                    .collect(Collectors.joining(";"));

            writer.write(line);
            writer.newLine();
        }
    }

    /**
     * Конвертирует значение поля в строковое представление для CSV.
     * Особые случаи обработки:
     * - Enum: сохраняется как имя константы
     * - List: элементы объединяются через запятую
     * - null: возвращается пустая строка
     *
     * @param value значение поля
     * @return строковое представление значения
     */
    private String convertFieldToString(Object value) {
        if (value == null) {
            return "";
        }

        // Обработка Enum
        if (value instanceof Enum) {
            return ((Enum<?>) value).name();
        }

        // Обработка List (особенно для поля score в Student)
        if (value instanceof List<?> list) {
            return list.stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .collect(Collectors.joining(","));
        }

        // Стандартное преобразование для других типов
        return value.toString();
    }

    /**
     * Форматирует имя поля для лучшей читаемости в заголовке CSV.
     * Преобразует camelCase в "Camel Case".
     *
     * @param fieldName исходное имя поля
     * @return отформатированное имя поля
     */
    private String formatFieldName(String fieldName) {
        // Простая реализация - можно улучшить при необходимости
        return fieldName.substring(0, 1).toUpperCase() +
                fieldName.substring(1).replaceAll("([A-Z])", " $1");
    }

    /**
     * Экранирует поле CSV согласно стандарту RFC 4180.
     * Если поле содержит разделитель, кавычки или символы переноса строки,
     * оно заключается в двойные кавычки, а существующие кавычки удваиваются.
     *
     * @param field поле для экранирования
     * @return экранированное поле, готовое для записи в CSV
     */
    private String escapeCsvField(String field) {
        if (field == null) {
            return "";
        }
        // Если поле содержит разделитель, кавычки или переносы строк, заключаем в кавычки
        if (field.contains(";") || field.contains("\"") || field.contains("\n") || field.contains("\r")) {
            // Экранируем кавычки путем их удвоения
            field = field.replace("\"", "\"\"");
            return "\"" + field + "\"";
        }
        return field;
    }
}
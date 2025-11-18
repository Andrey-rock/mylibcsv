package org.example;

import org.example.model.Person;
import org.example.model.Student;

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
     * Сохраняет список объектов в CSV файл.
     * Автоматически определяет тип объектов (Person или Student) и применяет соответствующий формат.
     * Null элементы в списке игнорируются.
     *
     * @param data список объектов для сохранения (Person или Student)
     * @param fileName имя файла для сохранения данных
     * @throws IllegalArgumentException если передан неподдерживаемый тип данных
     *
     * @apiNote Метод автоматически создает заголовки CSV файла и экранирует специальные символы.
     *          В случае ошибок записи выводит сообщение в стандартный поток ошибок.
     */
    @Override
    @SuppressWarnings("unchecked")
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

            // Определяем тип объектов по первому ненулевому элементу и записываем заголовок
            Object firstElement = filteredData.get(0);
            if (firstElement instanceof Person) {
                writePersonHeader(writer);
                writePersonData(writer, (List<Person>) filteredData);
            } else if (firstElement instanceof Student) {
                writeStudentHeader(writer);
                writeStudentData(writer, (List<Student>) filteredData);
            } else {
                throw new IllegalArgumentException("Unsupported data type: " + firstElement.getClass().getSimpleName());
            }

            System.out.println("Data successfully written to " + fileName);

        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    /**
     * Записывает заголовок CSV файла для объектов Person.
     * Использует запятую как разделитель столбцов.
     *
     * @param writer BufferedWriter для записи данных
     * @throws IOException если произошла ошибка ввода-вывода
     */
    private void writePersonHeader(BufferedWriter writer) throws IOException {
        writer.write("FirstName,LastName,DayOfBirth,MonthOfBirth,YearOfBirth");
        writer.newLine();
    }

    /**
     * Записывает данные объектов Person в CSV формате.
     * Каждая строка представляет одного человека с разделителем-запятой.
     *
     * @param writer BufferedWriter для записи данных
     * @param persons список объектов Person для сохранения
     * @throws IOException если произошла ошибка ввода-вывода
     */
    private void writePersonData(BufferedWriter writer, List<Person> persons) throws IOException {
        for (Person person : persons) {
            String line = String.format("%s,%s,%d,%s,%d",
                    escapeCsvField(person.getFirstName(), ","),
                    escapeCsvField(person.getLastName(), ","),
                    person.getDayOfBirth(),
                    person.getMonthOfBirth() != null ? person.getMonthOfBirth().name() : "",
                    person.getYearOfBirth());
            writer.write(line);
            writer.newLine();
        }
    }

    /**
     * Записывает заголовок CSV файла для объектов Student.
     * Использует точку с запятой как разделитель столбцов.
     *
     * @param writer BufferedWriter для записи данных
     * @throws IOException если произошла ошибка ввода-вывода
     */
    private void writeStudentHeader(BufferedWriter writer) throws IOException {
        writer.write("Name;Scores");
        writer.newLine();
    }

    /**
     * Записывает данные объектов Student в CSV формате.
     * Использует точку с запятой как разделитель столбцов и запятую для разделения оценок.
     *
     * @param writer BufferedWriter для записи данных
     * @param students список объектов Student для сохранения
     * @throws IOException если произошла ошибка ввода-вывода
     */
    private void writeStudentData(BufferedWriter writer, List<Student> students) throws IOException {
        for (Student student : students) {
            // Объединяем оценки в одну строку через запятую
            String scores = "";
            if (student.getScore() != null) {
                scores = student.getScore().stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining(","));
            }

            String line = String.format("%s;%s",
                    escapeCsvField(student.getName(), ";"),
                    escapeCsvField(scores, ";"));
            writer.write(line);
            writer.newLine();
        }
    }

    /**
     * Экранирует поле CSV согласно стандарту RFC 4180.
     * Если поле содержит разделитель, кавычки или символы переноса строки,
     * оно заключается в двойные кавычки, а существующие кавычки удваиваются.
     *
     * @param field поле для экранирования
     * @param delimiter разделитель, используемый в CSV файле
     * @return экранированное поле, готовое для записи в CSV
     */
    private String escapeCsvField(String field, String delimiter) {
        if (field == null) {
            return "";
        }
        // Если поле содержит разделитель, кавычки или переносы строк, заключаем в кавычки
        if (field.contains(delimiter) || field.contains("\"") || field.contains("\n") || field.contains("\r")) {
            // Экранируем кавычки путем их удвоения
            field = field.replace("\"", "\"\"");
            return "\"" + field + "\"";
        }
        return field;
    }
}
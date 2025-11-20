import org.example.CsvWriter;
import org.example.model.Months;
import org.example.model.Person;
import org.example.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Юнит-тесты для класса CsvWriter.
 *
 * @author Andrei Bronskii
 * @version 1.0
 * @email andrei.bronskijj@mail.ru
 * @since 2025
 */
class CsvWriterTest {

    private CsvWriter csvWriter;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        csvWriter = new CsvWriter();
    }

    @Test
    @DisplayName("Должен корректно сохранять список Person в CSV файл")
    void shouldWritePersonListToFile() throws IOException {
        // Given
        List<Person> persons = Arrays.asList(
                Person.builder()
                        .firstName("John")
                        .lastName("Doe")
                        .dayOfBirth(15)
                        .monthOfBirth(Months.JANUARY)
                        .yearOfBirth(1990)
                        .build(),
                Person.builder()
                        .firstName("Jane")
                        .lastName("Smith")
                        .dayOfBirth(20)
                        .monthOfBirth(Months.MARCH)
                        .yearOfBirth(1985)
                        .build()
        );

        Path testFile = tempDir.resolve("persons.csv");

        // When
        csvWriter.writeToFile(persons, testFile.toString());

        // Then
        assertTrue(Files.exists(testFile));

        List<String> lines = Files.readAllLines(testFile);
        assertEquals(3, lines.size()); // Заголовок + 2 строки данных

        // Проверяем заголовок
        assertEquals("First Name;Last Name;Day Of Birth;Month Of Birth;Year Of Birth", lines.get(0));

        // Проверяем данные
        assertEquals("John;Doe;15;JANUARY;1990", lines.get(1));
        assertEquals("Jane;Smith;20;MARCH;1985", lines.get(2));
    }

    @Test
    @DisplayName("Должен корректно сохранять список Student в CSV файл")
    void shouldWriteStudentListToFile() throws IOException {
        // Given
        List<Student> students = Arrays.asList(
                Student.builder()
                        .name("Alice Johnson")
                        .score(Arrays.asList("A", "B", "A+"))
                        .build(),
                Student.builder()
                        .name("Bob Brown")
                        .score(Arrays.asList("B", "C", "A"))
                        .build()
        );

        Path testFile = tempDir.resolve("students.csv");

        // When
        csvWriter.writeToFile(students, testFile.toString());

        // Then
        assertTrue(Files.exists(testFile));

        List<String> lines = Files.readAllLines(testFile);
        assertEquals(3, lines.size()); // Заголовок + 2 строки данных

        // Проверяем заголовок
        assertEquals("Name;Score", lines.get(0));

        // Проверяем данные
        assertEquals("Alice Johnson;A,B,A+", lines.get(1));
        assertEquals("Bob Brown;B,C,A", lines.get(2));
    }

    @Test
    @DisplayName("Должен экранировать специальные символы в полях Person")
    void shouldEscapeSpecialCharactersInPerson() throws IOException {
        // Given
        List<Person> persons = Collections.singletonList(
                Person.builder()
                        .firstName("John, \"The Boss\"")
                        .lastName("Doe")
                        .dayOfBirth(15)
                        .monthOfBirth(Months.JANUARY)
                        .yearOfBirth(1990)
                        .build()
        );

        Path testFile = tempDir.resolve("persons_special.csv");

        // When
        csvWriter.writeToFile(persons, testFile.toString());

        // Then
        List<String> lines = Files.readAllLines(testFile);
        assertEquals("\"John, \"\"The Boss\"\"\";Doe;15;JANUARY;1990", lines.get(1));
    }

    @Test
    @DisplayName("Должен экранировать специальные символы в полях Student")
    void shouldEscapeSpecialCharactersInStudent() throws IOException {
        // Given
        List<Student> students = Collections.singletonList(
                Student.builder()
                        .name("Alice; Johnson")
                        .score(Arrays.asList("3.5", "4.8", "2.9"))
                        .build()
        );

        Path testFile = tempDir.resolve("students_special.csv");

        // When
        csvWriter.writeToFile(students, testFile.toString());

        // Then
        List<String> lines = Files.readAllLines(testFile);
        assertEquals("\"Alice; Johnson\";3.5,4.8,2.9", lines.get(1));
    }

    @Test
    @DisplayName("Должен корректно обрабатывать пустой список Person")
    void shouldHandleEmptyPersonList() {
        // Given
        List<Person> emptyList = List.of();
        Path testFile = tempDir.resolve("empty_persons.csv");

        // When
        csvWriter.writeToFile(emptyList, testFile.toString());

        // Then
        // Проверяем что метод не выбрасывает исключение и файл не создается
        assertDoesNotThrow(() -> csvWriter.writeToFile(
                emptyList, testFile.toString()));
    }

    @Test
    @DisplayName("Должен корректно обрабатывать неверный путь к файлу")
    void shouldHandleInvalidFilePath() {
        // Given
        List<Person> persons = Collections.singletonList(
                Person.builder()
                        .firstName("John")
                        .lastName("Doe")
                        .dayOfBirth(15)
                        .monthOfBirth(Months.JANUARY)
                        .yearOfBirth(1990)
                        .build()
        );

        // Неверный путь (несуществующая директория)
        String invalidPath = "/invalid/path/that/does/not/exist/file.csv";

        // When & Then
        assertDoesNotThrow(() -> csvWriter.writeToFile(persons, invalidPath));
    }

    @Test
    @DisplayName("Должен корректно обрабатывать null имя файла")
    void shouldHandleNullFileName() {
        // Given
        List<Person> persons = Collections.singletonList(
                Person.builder()
                        .firstName("John")
                        .lastName("Doe")
                        .dayOfBirth(15)
                        .monthOfBirth(Months.JANUARY)
                        .yearOfBirth(1990)
                        .build()
        );

        // When & Then
        assertDoesNotThrow(() -> csvWriter.writeToFile(persons, null));
    }

    @Test
    @DisplayName("Должен корректно обрабатывать пустое имя файла")
    void shouldHandleEmptyFileName() {
        // Given
        List<Person> persons = Collections.singletonList(
                Person.builder()
                        .firstName("John")
                        .lastName("Doe")
                        .dayOfBirth(15)
                        .monthOfBirth(Months.JANUARY)
                        .yearOfBirth(1990)
                        .build()
        );

        // When & Then
        assertDoesNotThrow(() -> csvWriter.writeToFile(persons, ""));
    }

    @Test
    @DisplayName("Должен корректно обрабатывать специальные символы в имени файла")
    void shouldHandleSpecialCharactersInFileName() {
        // Given
        List<Person> persons = Collections.singletonList(
                Person.builder()
                        .firstName("John")
                        .lastName("Doe")
                        .dayOfBirth(15)
                        .monthOfBirth(Months.JANUARY)
                        .yearOfBirth(1990)
                        .build()
        );

        // Имена файлов со специальными символами
        String[] specialFileNames = {
                "file with spaces.csv",
                "file-with-dashes.csv",
                "file_with_underscores.csv",
                "file(with)parentheses.csv",
                "file with,comma.csv"
        };

        // When & Then
        for (String fileName : specialFileNames) {
            Path testFile = tempDir.resolve(fileName);
            assertDoesNotThrow(() -> csvWriter.writeToFile(persons, testFile.toString()));

            // Проверяем что файл был создан
            assertTrue(Files.exists(testFile), "File should be created: " + fileName);
        }
    }

    @Test
    @DisplayName("Должен корректно обрабатывать очень длинные имена файлов")
    void shouldHandleLongFileName() {
        // Given
        List<Person> persons = Collections.singletonList(
                Person.builder()
                        .firstName("John")
                        .lastName("Doe")
                        .dayOfBirth(15)
                        .monthOfBirth(Months.JANUARY)
                        .yearOfBirth(1990)
                        .build()
        );

        // Создаем очень длинное имя файла
        String longFileName = "a".repeat(250) + ".csv";
        Path testFile = tempDir.resolve(longFileName);

        // When & Then
        assertDoesNotThrow(() -> csvWriter.writeToFile(persons, testFile.toString()));

        // Проверяем что файл был создан
        assertTrue(Files.exists(testFile));
    }

    @Test
    @DisplayName("Должен корректно обрабатывать смешанный список объектов (должен использовать тип первого элемента)")
    void shouldHandleMixedListUsingFirstElementType() throws IOException {
        // Given
        // Создаем список, где первый элемент - Person, остальные могут быть разными
        List<Object> mixedList = Arrays.asList(
                Person.builder()
                        .firstName("John")
                        .lastName("Doe")
                        .dayOfBirth(15)
                        .monthOfBirth(Months.JANUARY)
                        .yearOfBirth(1990)
                        .build(),
                "This should not cause exception", // Строка - неподдерживаемый тип
                12345 // Число - неподдерживаемый тип
        );

        Path testFile = tempDir.resolve("mixed_list.csv");

        // When & Then
        // Метод должен использовать тип первого элемента (Person) и проигнорировать остальные
        assertDoesNotThrow(() -> csvWriter.writeToFile(mixedList, testFile.toString()));

        // Проверяем что файл был создан и содержит данные первого элемента
        List<String> lines = Files.readAllLines(testFile);
        assertTrue(lines.size() >= 2); // Заголовок + хотя бы одна строка данных
        assertEquals("First Name;Last Name;Day Of Birth;Month Of Birth;Year Of Birth", lines.get(0));
    }

    @Test
    @DisplayName("Должен корректно обрабатывать список с null элементами")
    void shouldHandleListWithNullElements() throws IOException {
        // Given
        List<Person> personsWithNulls = Arrays.asList(
                Person.builder()
                        .firstName("John")
                        .lastName("Doe")
                        .dayOfBirth(15)
                        .monthOfBirth(Months.JANUARY)
                        .yearOfBirth(1990)
                        .build(),
                null, // null элемент
                Person.builder()
                        .firstName("Jane")
                        .lastName("Smith")
                        .dayOfBirth(20)
                        .monthOfBirth(Months.MARCH)
                        .yearOfBirth(1985)
                        .build()
        );

        Path testFile = tempDir.resolve("list_with_nulls.csv");

        // When & Then
        assertDoesNotThrow(() -> csvWriter.writeToFile(personsWithNulls, testFile.toString()));

        // Проверяем что файл был создан и содержит данные ненулевых элементов
        List<String> lines = Files.readAllLines(testFile);
        assertEquals(3, lines.size()); // Заголовок + 2 строки данных (null игнорируется)
    }

    @Test
    @DisplayName("Должен корректно обрабатывать очень большие списки данных")
    void shouldHandleLargeDataList() throws IOException {
        // Given
        int largeSize = 1000;
        Person[] largeArray = new Person[largeSize];
        for (int i = 0; i < largeSize; i++) {
            largeArray[i] = Person.builder()
                    .firstName("FirstName" + i)
                    .lastName("LastName" + i)
                    .dayOfBirth(i % 28 + 1)
                    .monthOfBirth(Months.values()[i % 12])
                    .yearOfBirth(1980 + i % 40)
                    .build();
        }

        List<Person> largeList = Arrays.asList(largeArray);
        Path testFile = tempDir.resolve("large_data.csv");

        // When & Then
        assertDoesNotThrow(() -> csvWriter.writeToFile(largeList, testFile.toString()));

        // Проверяем что файл был создан
        assertTrue(Files.exists(testFile));
        assertTrue(Files.size(testFile) > 0);
    }

    @Test
    @DisplayName("Должен корректно обрабатывать символы Unicode в данных")
    void shouldHandleUnicodeCharacters() throws IOException {
        // Given
        List<Person> persons = Arrays.asList(
                Person.builder()
                        .firstName("Jöhn") // символ с умлаутом
                        .lastName("Dœ") // лигатура
                        .dayOfBirth(15)
                        .monthOfBirth(Months.JANUARY)
                        .yearOfBirth(1990)
                        .build(),
                Person.builder()
                        .firstName("Мария") // кириллица
                        .lastName("Иванова")
                        .dayOfBirth(20)
                        .monthOfBirth(Months.MARCH)
                        .yearOfBirth(1985)
                        .build(),
                Person.builder()
                        .firstName("太郎") // японские иероглифы
                        .lastName("田中")
                        .dayOfBirth(10)
                        .monthOfBirth(Months.APRIL)
                        .yearOfBirth(1995)
                        .build()
        );

        Path testFile = tempDir.resolve("unicode_data.csv");

        // When
        assertDoesNotThrow(() -> csvWriter.writeToFile(persons, testFile.toString()));

        // Then
        assertTrue(Files.exists(testFile));
        List<String> lines = Files.readAllLines(testFile);
        assertEquals(4, lines.size()); // Заголовок + 3 строки данных
    }

    @Test
    @DisplayName("Должен корректно обрабатывать экстремальные значения дат")
    void shouldHandleExtremeDateValues() throws IOException {
        // Given
        List<Person> persons = Arrays.asList(
                Person.builder()
                        .firstName("Min")
                        .lastName("Date")
                        .dayOfBirth(1)
                        .monthOfBirth(Months.JANUARY)
                        .yearOfBirth(1900)
                        .build(),
                Person.builder()
                        .firstName("Max")
                        .lastName("Date")
                        .dayOfBirth(31)
                        .monthOfBirth(Months.DECEMBER)
                        .yearOfBirth(2100)
                        .build()
        );

        Path testFile = tempDir.resolve("extreme_dates.csv");

        // When
        assertDoesNotThrow(() -> csvWriter.writeToFile(persons, testFile.toString()));

        // Then
        assertTrue(Files.exists(testFile));
        List<String> lines = Files.readAllLines(testFile);
        assertEquals(3, lines.size()); // Заголовок + 2 строки данных
    }
}

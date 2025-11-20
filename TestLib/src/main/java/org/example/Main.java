package org.example;

import org.example.model.Months;
import org.example.model.Person;
import org.example.model.Student;

import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");
        Writable writer = new CsvWriter();

        // Создаем список Person
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

        // Сохраняем Person в CSV
        writer.writeToFile(persons, "persons.csv");

        // Создаем список Student
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

        // Сохраняем Student в CSV
        writer.writeToFile(students, "students.csv");
    }
}
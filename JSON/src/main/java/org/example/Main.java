package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        try {
            String json = Files.readString(Paths.get("person.json"));
            Person person = objectMapper.readValue(json, Person.class);
            System.out.println(person);

            person.setChildren(List.of("Olga", "Petr"));

            Car car = new Car();
            car.setLicensePlate("A111BB777");
            person.setCar(car);

            String newJson = objectMapper.writeValueAsString(person);

            FileWriter fileWriter = new FileWriter("personModified.json");
            fileWriter.write(newJson);
            fileWriter.close();

            System.out.println(person);

        } catch (IOException e) {
            e.getMessage();
        }
    }
}
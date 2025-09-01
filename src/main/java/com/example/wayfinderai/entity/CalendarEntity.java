package com.example.wayfinderai.entity;


import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class CalendarEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 200)
    private String date;

    @Column(length = 200)
    private String month;

    public CalendarEntity() {

    }

    public CalendarEntity(String date, String month) {
        this.date = date;
        this.month = month;
    }

}

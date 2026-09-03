package org.example;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Setter
@Getter
@Entity
@EqualsAndHashCode
@NoArgsConstructor
@ToString
@Table(name = "PurchaseList")
public class PurchaseList {
    @EmbeddedId
    private PurchaseListKey id;
    @Column(name = "student_name", insertable = false, updatable = false)
    private String studentName;
    @Column(name = "course_name", insertable = false, updatable = false)
    private String courseName;
    private int price;
    @Column(name = "subscription_date")
    private Date subscriptionDate;
}

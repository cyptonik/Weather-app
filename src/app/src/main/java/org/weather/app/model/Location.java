package org.weather.app.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "Locations")
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private BigDecimal latitude;
    private BigDecimal longitude;
}

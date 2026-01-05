package com.schoolplanner.timetable.domain;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeachingUnit {
    private Subject subject; // Matemātika
    private int grade; // 7. klasei
    private RoomType roomType; // šim priekšmetam obligāti vajag sporta zāli, ja tas ir sports, piemēram
}

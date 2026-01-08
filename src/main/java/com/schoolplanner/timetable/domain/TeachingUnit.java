package com.schoolplanner.timetable.domain;
import lombok.*;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeachingUnit {
    private Long id;
    private Subject subject; // Matemātika
    private int grade; // 7. klasei
    private RoomType roomType; // šim priekšmetam obligāti vajag sporta zāli, ja tas ir sports, piemēram
}

package org.springframework.samples.petclinic.achievements;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.springframework.samples.petclinic.model.BaseEntity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "achievement_types")
public class AchievementType extends BaseEntity {

    @Column(unique = true)
    @Size(min = 3, max = 50)
    private String name;

}

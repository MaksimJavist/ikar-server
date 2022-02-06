package com.ikar.ikarserver.backend.domain.entity;

import com.ikar.ikarserver.backend.util.Marker;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import java.util.UUID;

import static com.ikar.ikarserver.backend.util.Messages.EMPTY_USER_FIRSTNAME_ERROR;
import static com.ikar.ikarserver.backend.util.Messages.EMPTY_USER_PASSWORD_ERROR;
import static com.ikar.ikarserver.backend.util.Messages.EMPTY_USER_SECONDNAME_ERROR;
import static com.ikar.ikarserver.backend.util.Messages.EMPTY_USER_USERNAME_ERROR;

@Getter
@Setter
@Entity
@Table(name = "app_user")
public class AppUser {

    @Id
    @NotBlank
    @GeneratedValue
    @Column(name = "uuid")
    @NotNull(groups = Marker.Update.class)
    @Null(groups = Marker.Create.class)
    private UUID uuid;

    @Column(name = "username")
    @NotBlank(message = EMPTY_USER_USERNAME_ERROR)
    private String username;

    @Column(name = "password")
    @NotBlank(message = EMPTY_USER_PASSWORD_ERROR)
    private String password;

    @Column(name = "first_name")
    @NotBlank(message = EMPTY_USER_FIRSTNAME_ERROR)
    private String firstName;

    @Column(name = "second_name")
    @NotBlank(message = EMPTY_USER_SECONDNAME_ERROR)
    private String secondName;

    @Column(name = "middle_name")
    private String middleName;

}

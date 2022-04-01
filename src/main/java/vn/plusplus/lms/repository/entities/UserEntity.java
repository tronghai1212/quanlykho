package vn.plusplus.lms.repository.entities;

import lombok.Getter;
import lombok.Setter;
import vn.plusplus.lms.repository.entities.enumerates.AccountType;
import vn.plusplus.lms.repository.entities.enumerates.Status;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "user")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "avatar")
    private String avatar;

    @Column(name = "phone")
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "address")
    private String address;

    @Column(name = "user_name")
    private String userName;


    @Column(name = "password")
    private String password;

    @Column(name = "achievement")
    private Integer achievement;

    @Column(name = "star")
    private Integer star;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "mentor_ids_favourite")
    private String mentorIdsFavourite;

    @Column(name = "created_time")
    private Timestamp createdTime;

    @Column(name = "updated_time")
    private Timestamp updatedTime;

    @Column(name = "type")
    private String type;

    @Column(name = "account_type")
    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    @Column(name = "code_active")
    private String codeActive;

    @Column(name = "commission_percentage")
    private Integer commissionPercentage = 0;

    @Column(name = "wallet")
    private Long wallet =0L;

}

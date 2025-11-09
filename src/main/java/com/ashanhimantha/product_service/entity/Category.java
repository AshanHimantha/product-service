package com.ashanhimantha.product_service.entity;



import com.ashanhimantha.product_service.entity.enums.Status;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.SQLDelete;

@Entity
@Table(name = "categories")
@Data
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @Column(length = 500)
    private String imageUrl; // URL to category image (S3, CDN, etc.)

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_type_id")
    private CategoryType categoryType; // The sizing/measurement template for this category

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;
}
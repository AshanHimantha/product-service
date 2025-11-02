package com.ashanhimantha.product_service.entity;



import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Table(name = "categories")
@Data
@SQLDelete(sql = "UPDATE categories SET active = false WHERE id = ?")
@Where(clause = "active = true")
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

    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean active = true;
}
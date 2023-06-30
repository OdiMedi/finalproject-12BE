package com.example.finalproject12be.domain.store.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.example.finalproject12be.domain.bookmark.entity.Bookmark;
import com.example.finalproject12be.domain.comment.entity.Comment;
import com.example.finalproject12be.domain.store.dto.StoreRequest;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Second {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "SECOND_ID")
	private Long id;

	@Column
	private String gu;

	@Column
	private String name;

	@Column
	private String callNumber;

	@Column
	private String businessHours;

	@Column
	private Double longitude;

	@Column
	private Double latitude;

	@Column
	private String language;

	@Column
	private Integer nightPharmacy;

}

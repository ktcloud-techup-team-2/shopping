package com.kt.domain.board;

import java.util.List;

import com.kt.common.jpa.BaseSoftDeleteEntity;
import com.kt.domain.pet.PetType;
import com.kt.domain.user.User;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Board extends BaseSoftDeleteEntity {

	@Column(nullable = false)
	private String title;

	@Column(columnDefinition = "TEXT", nullable = false)
	private String content;

	@Enumerated(EnumType.STRING)
	private BoardCategory boardCategory;

	@Enumerated(EnumType.STRING)
	private PetType petType;

	@ElementCollection
	@CollectionTable(name = "board_images", joinColumns = @JoinColumn(name = "board_id"))
	@Column(name = "image_url")
	private List<String> imageUrls;

	private int viewCount;
	private int likeCount;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	public static Board write(User user, String title, String content, BoardCategory boardCategory, PetType petType, List<String> imageUrls) {
		Board board = new Board();
		board.user = user;
		board.title = title;
		board.content = content;
		board.boardCategory = boardCategory;
		board.petType = petType;
		board.imageUrls = imageUrls;
		return board;
	}

	public void update(String title, String content, BoardCategory category, PetType petType, List<String> imageUrls) {
		this.title = title;
		this.content = content;
		this.boardCategory = category;
		this.petType = petType;
		this.imageUrls = imageUrls;
	}

	public void increaseViewCount() {
		this.viewCount++;
	}

	public void delete(Long deleterId) {
		this.markDeleted(deleterId);
	}
}

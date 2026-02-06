package com.momentry.BE.domain.album.entity;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum MemberAlbumPermission {
	MANAGER(300),   // 사진관리 + 카테고리관리 + 멤버관리
	EDITOR(200),    // 사진관리 + 카테고리관리
	VIEWER(100);    // 기본 기능(사진 조회/다운로드, 좋아요)만

	private final int level;

	public boolean isAtLeast(MemberAlbumPermission required) {
		return this.level >= required.level;
	}

	public boolean canManageMembers() {
		return isAtLeast(MANAGER);
	}

	public boolean canEditAlbum() {
		return isAtLeast(EDITOR);
	}

	public boolean canUseDefaultFeatures() {
		return isAtLeast(VIEWER);
	}
}

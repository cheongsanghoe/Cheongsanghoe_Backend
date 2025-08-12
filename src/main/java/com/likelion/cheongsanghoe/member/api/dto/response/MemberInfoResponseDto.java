package com.likelion.cheongsanghoe.member.api.dto.response;

import com.likelion.cheongsanghoe.member.domain.Member;
import com.likelion.cheongsanghoe.member.domain.MemberStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MemberInfoResponseDto {

    private Long memberId;
    private Long userId;
    private String email;
    private String nickname;
    private String phoneNumber;
    private String address;
    private String detailAddress;
    private String zipCode;
    private String bio;
    private String profileImageUrl;
    private String role;
    private String roleDescription;
    private MemberStatus status;
    private String statusDescription;
    private Integer reportCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static MemberInfoResponseDto of(Member member) {
        return MemberInfoResponseDto.builder()
                .memberId(member.getId())
                .userId(member.getUser().getId())
                .email(member.getUser().getEmail())
                .nickname(member.getNickname())
                .phoneNumber(member.getPhoneNumber())
                .address(member.getAddress())
                .detailAddress(member.getDetailAddress())
                .zipCode(member.getZipCode())
                .bio(member.getBio())
                .profileImageUrl(member.getProfileImageUrl())
                .role(member.getUser().getRole().name())
                .roleDescription(member.getUser().getRole().getDescription())
                .status(member.getStatus())
                .statusDescription(member.getStatus().getDescription())
                .reportCount(member.getReportCount())
                .createdAt(member.getCreatedAt())
                .updatedAt(member.getUpdatedAt())
                .build();
    }

    // 간단한 정보만 포함하는 정적 팩토리 메서드 (검색 결과용)
    public static MemberInfoResponseDto ofSimple(Member member) {
        return MemberInfoResponseDto.builder()
                .memberId(member.getId())
                .nickname(member.getNickname())
                .profileImageUrl(member.getProfileImageUrl())
                .address(member.getAddress())
                .role(member.getUser().getRole().name())
                .roleDescription(member.getUser().getRole().getDescription())
                .status(member.getStatus())
                .createdAt(member.getCreatedAt())
                .build();
    }
}
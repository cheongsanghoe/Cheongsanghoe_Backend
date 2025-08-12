package com.likelion.cheongsanghoe.member.domain;

import com.likelion.cheongsanghoe.auth.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(length = 11)
    private String phoneNumber;

    @Column(length = 100)
    private String address;

    @Column(length = 100)
    private String detailAddress;

    @Column(length = 10)
    private String zipCode;

    @Column(length = 500)
    private String bio; // 자기소개

    @Column(length = 255)
    private String profileImageUrl;

    // === 포트폴리오 연관 필드들 추가 ===
    @Column(length = 100)
    private String jobCategory; // 직업군 (개발자, 디자이너, 마케터 등)

    @Column(length = 100)
    private String preferredLocation; // 희망 근무지역

    @Column(length = 500)
    private String skills; // 보유 스킬 (JSON 또는 콤마 구분)

    @Column
    private Integer experienceYears; // 경력 년수

    @Column(length = 255)
    private String education; // 학력

    @Column
    private Integer salaryExpectation; // 희망 연봉 (만원 단위)

    @Column(nullable = false)
    private Integer portfolioCount = 0; // 포트폴리오 개수 (캐싱용)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus status;

    @Column(nullable = false)
    private Integer reportCount = 0; // 신고 횟수

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Member(User user, String nickname, String phoneNumber, String address,
                  String detailAddress, String zipCode, String bio, String profileImageUrl,
                  String jobCategory, String preferredLocation,
                  String skills, Integer experienceYears, String education,
                  Integer salaryExpectation) {
        this.user = user;
        this.nickname = nickname;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.detailAddress = detailAddress;
        this.zipCode = zipCode;
        this.bio = bio;
        this.profileImageUrl = profileImageUrl;
        this.jobCategory = jobCategory;
        this.preferredLocation = preferredLocation;
        this.skills = skills;
        this.experienceYears = experienceYears;
        this.education = education;
        this.salaryExpectation = salaryExpectation;
        this.status = MemberStatus.ACTIVE;
    }

    // 비즈니스 메서드
    public void updateProfile(String nickname, String phoneNumber, String address, String detailAddress,
                              String zipCode, String bio){
        this.nickname = nickname;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.detailAddress = detailAddress;
        this.zipCode = zipCode;
        this.bio = bio;
        this.jobCategory = jobCategory;
        this.preferredLocation = preferredLocation;
        this.skills = skills;
        this.experienceYears = experienceYears;
        this.education = education;
        this.salaryExpectation = salaryExpectation;
    }

    public void updateProfileImage(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public void increasePortfolioCount() {
        this.portfolioCount++;
    }

    public void decreasePortfolioCount() {
        if (this.portfolioCount > 0) {
            this.portfolioCount--;
        }
    }

    public void increaseReportCount() {
        this.reportCount++;
        if (this.reportCount >= 10) { // 신고 10회 이상시 정지-불필요하다가 느끼시거나 신고 횟수를 증가 시켜야 한다고 생각하시면 말씀해주세요.
            this.status = MemberStatus.SUSPENDED;
        }
    }

    public void suspend() {
        this.status = MemberStatus.SUSPENDED;
    }

    public void activate() {
        this.status = MemberStatus.ACTIVE;
    }

    public void withdraw() {
        this.status = MemberStatus.WITHDRAWN;
    }

    public boolean isActive() {
        return this.status == MemberStatus.ACTIVE;
    }

    public boolean isMerchant() {
        return this.user.getRole().name().equals("MERCHANT");
    }

    public boolean isYouth() {
        return this.user.getRole().name().equals("YOUTH");
    }
}
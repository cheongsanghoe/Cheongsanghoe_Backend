package com.likelion.cheongsanghoe.member.domain.repository;

import com.likelion.cheongsanghoe.auth.domain.User;
import com.likelion.cheongsanghoe.member.domain.Member;
import com.likelion.cheongsanghoe.member.domain.MemberStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    // User 객체로 Member 조회 (OneToOne 관계 활용)
    Optional<Member> findByUser(User user);

    // User ID로 Member 조회 (기존 호환성 유지)
    @Query("SELECT m FROM Member m WHERE m.user.id = :userId")
    Optional<Member> findByUserId(@Param("userId") Long userId);

    // User Email로 Member 조회
    @Query("SELECT m FROM Member m WHERE m.user.email = :email")
    Optional<Member> findByUserEmail(@Param("email") String email);

    // 닉네임으로 조회
    Optional<Member> findByNickname(String nickname);

    // 닉네임 중복 체크
    boolean existsByNickname(String nickname);

    // 전화번호로 조회
    Optional<Member> findByPhoneNumber(String phoneNumber);

    // 전화번호 중복 체크
    boolean existsByPhoneNumber(String phoneNumber);

    // 상태별 회원 조회
    List<Member> findByStatus(MemberStatus status);
    Page<Member> findByStatus(MemberStatus status, Pageable pageable);

    // 역할별 회원 조회
    @Query("SELECT m FROM Member m WHERE m.user.role = :role")
    List<Member> findByUserRole(@Param("role") com.likelion.cheongsanghoe.auth.domain.Role role);

    @Query("SELECT m FROM Member m WHERE m.user.role = :role")
    Page<Member> findByUserRole(@Param("role") com.likelion.cheongsanghoe.auth.domain.Role role, Pageable pageable);

    // 활성화된 회원 조회
    List<Member> findByStatusOrderByCreatedAtDesc(MemberStatus status);

    // 닉네임 검색 (활성화된 회원만)
    @Query("SELECT m FROM Member m WHERE m.status = 'ACTIVE' AND m.nickname LIKE %:keyword%")
    Page<Member> searchByNickname(@Param("keyword") String keyword, Pageable pageable);

    // 지역별 회원 조회
    @Query("SELECT m FROM Member m WHERE m.status = 'ACTIVE' AND m.address LIKE %:address%")
    Page<Member> findByAddressContaining(@Param("address") String address, Pageable pageable);

    // 복합 검색 (닉네임 + 지역)
    @Query("SELECT m FROM Member m WHERE m.status = 'ACTIVE' " +
            "AND (:nickname IS NULL OR m.nickname LIKE %:nickname%) " +
            "AND (:address IS NULL OR m.address LIKE %:address%)")
    Page<Member> searchByNicknameAndAddress(@Param("nickname") String nickname,
                                            @Param("address") String address,
                                            Pageable pageable);

    // 신고 횟수가 특정 수 이상인 회원 조회
    @Query("SELECT m FROM Member m WHERE m.reportCount >= :count")
    List<Member> findByReportCountGreaterThanEqual(@Param("count") Integer count);

    // 회원 통계
    @Query("SELECT COUNT(m) FROM Member m WHERE m.status = :status")
    Long countByStatus(@Param("status") MemberStatus status);

    @Query("SELECT COUNT(m) FROM Member m WHERE m.user.role = :role AND m.status = 'ACTIVE'")
    Long countActiveByRole(@Param("role") com.likelion.cheongsanghoe.auth.domain.Role role);
}
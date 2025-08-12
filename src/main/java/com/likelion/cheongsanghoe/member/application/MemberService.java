package com.likelion.cheongsanghoe.member.application;

import com.likelion.cheongsanghoe.auth.domain.User;
import com.likelion.cheongsanghoe.auth.domain.repository.UserRepository;
import com.likelion.cheongsanghoe.exception.MemberNotFoundException;
import com.likelion.cheongsanghoe.exception.DuplicateNicknameException;
import com.likelion.cheongsanghoe.exception.DuplicatePhoneNumberException;
import com.likelion.cheongsanghoe.exception.MemberAlreadyExistException;
import com.likelion.cheongsanghoe.member.api.dto.request.MemberUpdateRequestDto;
import com.likelion.cheongsanghoe.member.api.dto.response.MemberInfoResponseDto;
import com.likelion.cheongsanghoe.member.domain.Member;
import com.likelion.cheongsanghoe.member.domain.MemberStatus;
import com.likelion.cheongsanghoe.member.domain.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final UserRepository userRepository;

    /**
     * 회원 프로필 생성 (소셜 로그인 후 추가 정보 입력)
     */
    @Transactional
    public MemberInfoResponseDto createMemberProfile(Long userId, MemberUpdateRequestDto requestDto) {
        log.info("Creating member profile for userId: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 이미 프로필이 존재하는지 확인
        if (memberRepository.findByUserId(userId).isPresent()) {
            throw new MemberAlreadyExistException("회원이 이미 존재합니다. userId: "+userId);
        }

        // 닉네임 중복 체크
        if (memberRepository.existsByNickname(requestDto.getNickname())) {
            throw new DuplicateNicknameException(requestDto.getNickname());
        }

        // 전화번호 중복 체크
        if (requestDto.getPhoneNumber() != null &&
                memberRepository.existsByPhoneNumber(requestDto.getPhoneNumber())) {
            throw new DuplicatePhoneNumberException(requestDto.getPhoneNumber());
        }

        Member member = Member.builder()
                .user(user)
                .nickname(requestDto.getNickname())
                .phoneNumber(requestDto.getPhoneNumber())
                .address(requestDto.getAddress())
                .detailAddress(requestDto.getDetailAddress())
                .zipCode(requestDto.getZipCode())
                .bio(requestDto.getBio())
                .build();

        Member savedMember = memberRepository.save(member);
        log.info("Member profile created successfully. MemberId: {}", savedMember.getId());

        return MemberInfoResponseDto.of(savedMember);
    }

    /**
     * 회원 정보 조회 (본인)
     */
    public MemberInfoResponseDto getMemberInfo(Long userId) {
        log.info("Getting member info for userId: {}", userId);

        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new MemberNotFoundException("회원 정보를 찾을 수 없습니다."));

        return MemberInfoResponseDto.of(member);
    }

    /**
     * 회원 정보 조회 (ID로)
     */
    public MemberInfoResponseDto getMemberById(Long memberId) {
        log.info("Getting member info by memberId: {}", memberId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));

        if (!member.isActive()) {
            throw new RuntimeException("비활성화된 회원입니다.");
        }

        return MemberInfoResponseDto.of(member);
    }

    /**
     * 회원 정보 수정
     */
    @Transactional
    public MemberInfoResponseDto updateMember(Long userId, MemberUpdateRequestDto requestDto) {
        log.info("Updating member info for userId: {}", userId);

        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new MemberNotFoundException("회원 정보를 찾을 수 없습니다."));

        // 닉네임 변경시 중복 체크
        if (!member.getNickname().equals(requestDto.getNickname()) &&
                memberRepository.existsByNickname(requestDto.getNickname())) {
            throw new DuplicateNicknameException(requestDto.getNickname());
        }

        // 전화번호 변경시 중복 체크
        if (requestDto.getPhoneNumber() != null &&
                !requestDto.getPhoneNumber().equals(member.getPhoneNumber()) &&
                memberRepository.existsByPhoneNumber(requestDto.getPhoneNumber())) {
            throw new DuplicatePhoneNumberException(requestDto.getPhoneNumber());
        }

        member.updateProfile(
                requestDto.getNickname(),
                requestDto.getPhoneNumber(),
                requestDto.getAddress(),
                requestDto.getDetailAddress(),
                requestDto.getZipCode(),
                requestDto.getBio()
        );

        log.info("Member info updated successfully. MemberId: {}", member.getId());
        return MemberInfoResponseDto.of(member);
    }

    /**
     * 프로필 이미지 업데이트
     */
    @Transactional
    public MemberInfoResponseDto updateProfileImage(Long userId, String profileImageUrl) {
        log.info("Updating profile image for userId: {}", userId);

        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new MemberNotFoundException("회원 정보를 찾을 수 없습니다."));

        member.updateProfileImage(profileImageUrl);
        return MemberInfoResponseDto.of(member);
    }

    /**
     * 회원 탈퇴
     */
    @Transactional
    public void withdrawMember(Long userId) {
        log.info("Withdrawing member for userId: {}", userId);

        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new MemberNotFoundException("회원 정보를 찾을 수 없습니다."));

        member.withdraw();
        log.info("Member withdrawn successfully. MemberId: {}", member.getId());
    }

    /**
     * 닉네임 중복 체크
     */
    public boolean isNicknameAvailable(String nickname) {
        return !memberRepository.existsByNickname(nickname);
    }

    /**
     * 전화번호 중복 체크
     */
    public boolean isPhoneNumberAvailable(String phoneNumber) {
        return !memberRepository.existsByPhoneNumber(phoneNumber);
    }

    /**
     * 회원 검색 (닉네임)
     */
    public Page<MemberInfoResponseDto> searchMembersByNickname(String keyword, Pageable pageable) {
        log.info("Searching members by nickname: {}", keyword);

        Page<Member> members = memberRepository.searchByNickname(keyword, pageable);
        return members.map(MemberInfoResponseDto::of);
    }

    /**
     * 지역별 회원 조회
     */
    public Page<MemberInfoResponseDto> getMembersByAddress(String address, Pageable pageable) {
        log.info("Getting members by address: {}", address);

        Page<Member> members = memberRepository.findByAddressContaining(address, pageable);
        return members.map(MemberInfoResponseDto::of);
    }

    /**
     * 복합 검색 (닉네임 + 지역)
     */
    public Page<MemberInfoResponseDto> searchMembers(String nickname, String address, Pageable pageable) {
        log.info("Searching members by nickname: {} and address: {}", nickname, address);

        Page<Member> members = memberRepository.searchByNicknameAndAddress(nickname, address, pageable);
        return members.map(MemberInfoResponseDto::of);
    }

    /**
     * 역할별 회원 조회
     */
    public Page<MemberInfoResponseDto> getMembersByRole(com.likelion.cheongsanghoe.auth.domain.Role role, Pageable pageable) {
        log.info("Getting members by role: {}", role);

        Page<Member> members = memberRepository.findByUserRole(role, pageable);
        return members.map(MemberInfoResponseDto::of);
    }

    /**
     * 회원 신고 처리
     */
    @Transactional
    public void reportMember(Long memberId) {
        log.info("Reporting member: {}", memberId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));

        member.increaseReportCount();
        log.info("Member reported. MemberId: {}, ReportCount: {}", memberId, member.getReportCount());
    }

    /**
     * 회원 정지
     */
    @Transactional
    public void suspendMember(Long memberId) {
        log.info("Suspending member: {}", memberId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));

        member.suspend();
    }

    /**
     * 회원 활성화
     */
    @Transactional
    public void activateMember(Long memberId) {
        log.info("Activating member: {}", memberId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));

        member.activate();
    }

    /**
     * 회원 통계
     */
    public Long getActiveMembers() {
        return memberRepository.countByStatus(MemberStatus.ACTIVE);
    }

    public Long getMerchantCount() {
        return memberRepository.countActiveByRole(com.likelion.cheongsanghoe.auth.domain.Role.MERCHANT);
    }

    public Long getYoungCount() {
        return memberRepository.countActiveByRole(com.likelion.cheongsanghoe.auth.domain.Role.YOUTH);
    }
}
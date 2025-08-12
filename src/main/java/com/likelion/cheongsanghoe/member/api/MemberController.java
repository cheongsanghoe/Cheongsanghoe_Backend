package com.likelion.cheongsanghoe.member.api;

import com.likelion.cheongsanghoe.member.api.dto.request.MemberUpdateRequestDto;
import com.likelion.cheongsanghoe.member.api.dto.response.MemberInfoResponseDto;
import com.likelion.cheongsanghoe.member.application.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    // private final FileService fileService; // 파일 업로드 서비스 (나중에 구현해야 하는 부분임)

    //회원 프로필 생성 (소셜 로그인 후 추가 정보 입력)

    @PostMapping("/profile")
    public ResponseEntity<MemberInfoResponseDto> createProfile(
            @Valid @RequestBody MemberUpdateRequestDto requestDto,
            Authentication authentication) {

        log.info("Creating profile for user: {}", authentication.getName());

        Long userId = Long.valueOf(authentication.getName()); // JWT에서 사용자 ID 추출하는거
        MemberInfoResponseDto response = memberService.createMemberProfile(userId, requestDto);

        return ResponseEntity.ok(response);
    }

    //내 정보 조회

    @GetMapping("/me")
    public ResponseEntity<MemberInfoResponseDto> getMyInfo(Authentication authentication) {
        log.info("Getting my info for user: {}", authentication.getName());

        Long userId = Long.valueOf(authentication.getName());
        MemberInfoResponseDto response = memberService.getMemberInfo(userId);

        return ResponseEntity.ok(response);
    }

    //특정 회원 정보 조회

    @GetMapping("/{memberId}")
    public ResponseEntity<MemberInfoResponseDto> getMemberInfo(@PathVariable Long memberId) {
        log.info("Getting member info for memberId: {}", memberId);

        MemberInfoResponseDto response = memberService.getMemberById(memberId);
        return ResponseEntity.ok(response);
    }

    //회원 정보 수정

    @PutMapping("/me")
    public ResponseEntity<MemberInfoResponseDto> updateMyInfo(
            @Valid @RequestBody MemberUpdateRequestDto requestDto,
            Authentication authentication) {

        log.info("Updating my info for user: {}", authentication.getName());

        Long userId = Long.valueOf(authentication.getName());
        MemberInfoResponseDto response = memberService.updateMember(userId, requestDto);

        return ResponseEntity.ok(response);
    }

    //프로필 이미지 업로드

    @PostMapping("/me/profile-image")
    public ResponseEntity<MemberInfoResponseDto> uploadProfileImage(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        log.info("Uploading profile image for user: {}", authentication.getName());

        try {
            Long userId = Long.valueOf(authentication.getName());

            String imageUrl = "/images/profile/default.jpg"; // 걍 임시로 넣은거

            MemberInfoResponseDto response = memberService.updateProfileImage(userId, imageUrl);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error uploading profile image", e);
            return ResponseEntity.badRequest().build();
        }
    }

    // 회원 탈퇴

    @DeleteMapping("/me")
    public ResponseEntity<Map<String, String>> withdrawMember(Authentication authentication) {
        log.info("Withdrawing member for user: {}", authentication.getName());

        Long userId = Long.valueOf(authentication.getName());
        memberService.withdrawMember(userId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "회원 탈퇴가 완료되었습니다.");

        return ResponseEntity.ok(response);
    }

    //닉네임 중복 체크

    @GetMapping("/check/nickname")
    public ResponseEntity<Map<String, Object>> checkNickname(@RequestParam String nickname) {
        log.info("Checking nickname availability: {}", nickname);

        boolean isAvailable = memberService.isNicknameAvailable(nickname);

        Map<String, Object> response = new HashMap<>();
        response.put("available", isAvailable);
        response.put("message", isAvailable ? "사용 가능한 닉네임입니다." : "이미 사용중인 닉네임입니다.");

        return ResponseEntity.ok(response);
    }

    //전화번호 중복 체크

    @GetMapping("/check/phone")
    public ResponseEntity<Map<String, Object>> checkPhoneNumber(@RequestParam String phoneNumber) {
        log.info("Checking phone number availability: {}", phoneNumber);

        boolean isAvailable = memberService.isPhoneNumberAvailable(phoneNumber);

        Map<String, Object> response = new HashMap<>();
        response.put("available", isAvailable);
        response.put("message", isAvailable ? "사용 가능한 전화번호입니다." : "이미 등록된 전화번호입니다.");

        return ResponseEntity.ok(response);
    }

    //회원 검색 (닉네임)

    @GetMapping("/search")
    public ResponseEntity<Page<MemberInfoResponseDto>> searchMembers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {

        log.info("Searching members with keyword: {}", keyword);

        Sort sortObj = Sort.by(Sort.Direction.fromString(direction), sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);

        Page<MemberInfoResponseDto> response = memberService.searchMembersByNickname(keyword, pageable);
        return ResponseEntity.ok(response);
    }

    //검색 (닉네임 + 지역)

    @GetMapping("/search/advanced")
    public ResponseEntity<Page<MemberInfoResponseDto>> advancedSearch(
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false) String address,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {

        log.info("Advanced search with nickname: {}, address: {}", nickname, address);

        Sort sortObj = Sort.by(Sort.Direction.fromString(direction), sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);

        Page<MemberInfoResponseDto> response = memberService.searchMembers(nickname, address, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * 지역별 회원 조회
     * GET /api/members/by-address?address=지역명
     */
    @GetMapping("/by-address")
    public ResponseEntity<Page<MemberInfoResponseDto>> getMembersByAddress(
            @RequestParam String address,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {

        log.info("Getting members by address: {}", address);

        Sort sortObj = Sort.by(Sort.Direction.fromString(direction), sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);

        Page<MemberInfoResponseDto> response = memberService.getMembersByAddress(address, pageable);
        return ResponseEntity.ok(response);
    }


    //역할별 회원 조회 (상인/청년)

    @GetMapping("/by-role")
    public ResponseEntity<Page<MemberInfoResponseDto>> getMembersByRole(
            @RequestParam String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {

        log.info("Getting members by role: {}", role);

        try {
            com.likelion.cheongsanghoe.auth.domain.Role roleEnum =
                    com.likelion.cheongsanghoe.auth.domain.Role.valueOf(role.toUpperCase());

            Sort sortObj = Sort.by(Sort.Direction.fromString(direction), sort);
            Pageable pageable = PageRequest.of(page, size, sortObj);

            Page<MemberInfoResponseDto> response = memberService.getMembersByRole(roleEnum, pageable);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Invalid role parameter: {}", role);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 회원 신고
     * POST /api/members/{memberId}/report
     */
    @PostMapping("/{memberId}/report")
    public ResponseEntity<Map<String, String>> reportMember(@PathVariable Long memberId) {
        log.info("Reporting member: {}", memberId);

        memberService.reportMember(memberId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "신고가 접수되었습니다.");

        return ResponseEntity.ok(response);
    }

    //회원 통계 조회 (관리자용)

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getMemberStatistics() {
        log.info("Getting member statistics");

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalActiveMembers", memberService.getActiveMembers());
        statistics.put("ownerCount", memberService.getMerchantCount());
        statistics.put("youngCount", memberService.getYoungCount());

        return ResponseEntity.ok(statistics);
    }

     //회원 정지 (관리자용)

    @PostMapping("/{memberId}/suspend")
    public ResponseEntity<Map<String, String>> suspendMember(@PathVariable Long memberId) {
        log.info("Suspending member: {}", memberId);

        memberService.suspendMember(memberId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "회원이 정지되었습니다.");

        return ResponseEntity.ok(response);
    }

    //회원 활성화 (관리자용)

    @PostMapping("/{memberId}/activate")
    public ResponseEntity<Map<String, String>> activateMember(@PathVariable Long memberId) {
        log.info("Activating member: {}", memberId);

        memberService.activateMember(memberId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "회원이 활성화되었습니다.");

        return ResponseEntity.ok(response);
    }
}

package com.likelion.cheongsanghoe.member.api.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MemberSearchRequestDto {

    private String keyword; // 닉네임 검색
    private String address; // 지역 검색
    private String role;
    private String status; // ACTIVE, SUSPENDED, WITHDRAWN->그 활성된 계정같은거 의미하는거

    public MemberSearchRequestDto(String keyword, String address, String role, String status) {
        this.keyword = keyword;
        this.address = address;
        this.role = role;
        this.status = status;
    }
}
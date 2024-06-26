package com.m3pro.groundflip.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.m3pro.groundflip.domain.dto.community.ListResponseCommunity;
import com.m3pro.groundflip.domain.dto.community.ResponseGetGroup;
import com.m3pro.groundflip.domain.entity.Community;
import com.m3pro.groundflip.service.CommunityService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class CommunityController {
	private final CommunityService communityService;

	@GetMapping("/api/groups")//그룹 검색
	public List<ListResponseCommunity> getCommunitys(@RequestParam String searchKeyword) {
		return communityService.findCommunityByName(searchKeyword);
	}

	@GetMapping("/api/groups/{groupId}")//그룹 정보 api인데 아직 미완성
	public ResponseGetGroup findGroupById(@PathVariable Long groupId) {
		return communityService.findCommunityById(groupId);
	}

}

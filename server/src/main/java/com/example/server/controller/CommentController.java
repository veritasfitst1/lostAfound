package com.example.server.controller;

import com.example.server.dto.CommentCreateRequest;
import com.example.server.dto.CommentVO;
import com.example.server.dto.CommonResponse;
import com.example.server.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/items/{itemId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping
    public CommonResponse<List<CommentVO>> list(@PathVariable Long itemId) {
        return CommonResponse.ok(commentService.listByItemId(itemId));
    }

    @PostMapping
    public CommonResponse<CommentVO> create(@RequestAttribute Long userId, @PathVariable Long itemId, @Valid @RequestBody CommentCreateRequest req) {
        return CommonResponse.ok(commentService.create(userId, itemId, req.getContent()));
    }
}

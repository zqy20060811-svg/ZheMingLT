package com.zheminglt.impl;

import com.zheminglt.constant.BusinessConstant;
import com.zheminglt.constant.ErrorCodeConstant;
import com.zheminglt.constant.MessageConstant;
import com.zheminglt.dto.CommentDTO;
import com.zheminglt.mapper.CommentMapper;
import com.zheminglt.model.Comment;
import com.zheminglt.service.CommentService;
import com.zheminglt.vo.CommentVO;
import com.zheminglt.vo.ResponseVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Override
    public ResponseVO<CommentVO> createComment(Long userId, CommentDTO commentDTO) {
        Comment comment = new Comment();
        BeanUtils.copyProperties(commentDTO, comment);
        Comment savedComment = commentMapper.save(comment);
        CommentVO commentVO = new CommentVO();
        BeanUtils.copyProperties(savedComment, commentVO);
        return ResponseVO.success(commentVO);
    }

    @Override
    public ResponseVO<Page<CommentVO>> getPostComments(Long postId, int page, int size) {
        Page<Comment> comments = commentMapper.findByPostId(postId, PageRequest.of(page - 1, size));
        Page<CommentVO> commentVOs = comments.map(comment -> {
            CommentVO commentVO = new CommentVO();
            BeanUtils.copyProperties(comment, commentVO);
            return commentVO;
        });
        return ResponseVO.success(commentVOs);
    }

    @Override
    public ResponseVO<CommentVO> getCommentById(Long id) {
        Comment comment = commentMapper.findById(id).orElse(null);
        if (comment == null) {
            return ResponseVO.error(ErrorCodeConstant.CODE_NOT_FOUND, MessageConstant.COMMENT_NOT_FOUND);
        }
        CommentVO commentVO = new CommentVO();
        BeanUtils.copyProperties(comment, commentVO);
        return ResponseVO.success(commentVO);
    }

    @Override
    public ResponseVO<CommentVO> updateComment(Long id, Long userId, String content) {
        Comment existingComment = commentMapper.findById(id).orElse(null);
        if (existingComment == null) {
            return ResponseVO.error(ErrorCodeConstant.CODE_NOT_FOUND, MessageConstant.COMMENT_NOT_FOUND);
        }
        Comment savedComment = commentMapper.save(existingComment);
        CommentVO commentVO = new CommentVO();
        BeanUtils.copyProperties(savedComment, commentVO);
        return ResponseVO.success(commentVO);
    }

    @Override
    public ResponseVO<Void> deleteComment(Long id, Long userId) {
        Comment comment = commentMapper.findById(id).orElse(null);
        if (comment == null) {
            return ResponseVO.error(ErrorCodeConstant.CODE_NOT_FOUND, MessageConstant.COMMENT_NOT_FOUND);
        }
        commentMapper.delete(comment);
        return ResponseVO.success(null);
    }
}

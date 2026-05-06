package com.zheminglt.impl;

import com.zheminglt.constant.BusinessConstant;
import com.zheminglt.constant.ErrorCodeConstant;
import com.zheminglt.constant.MessageConstant;
import com.zheminglt.dto.CommentDTO;
import com.zheminglt.mapper.CommentMapper;
import com.zheminglt.mapper.PostMapper;
import com.zheminglt.mapper.UserMapper;
import com.zheminglt.model.Comment;
import com.zheminglt.model.Post;
import com.zheminglt.model.User;
import com.zheminglt.service.CommentService;
import com.zheminglt.service.NotificationService;
import com.zheminglt.vo.CommentVO;
import com.zheminglt.vo.ResponseVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private NotificationService notificationService;

    @Override
    @Transactional
    public ResponseVO<CommentVO> createComment(Long userId, CommentDTO commentDTO) {
        Comment comment = new Comment();
        BeanUtils.copyProperties(commentDTO, comment);

        // 设置用户
        User user = userMapper.findById(userId).orElse(null);
        if (user == null) {
            return ResponseVO.error(ErrorCodeConstant.CODE_NOT_FOUND, MessageConstant.USER_NOT_FOUND);
        }
        comment.setUser(user);

        // 设置帖子
        Post post = postMapper.findById(commentDTO.getPostId()).orElse(null);
        if (post == null) {
            return ResponseVO.error(ErrorCodeConstant.CODE_NOT_FOUND, MessageConstant.POST_NOT_FOUND);
        }
        comment.setPost(post);
        
        // 设置默认状态
        comment.setStatus(1);

        Comment savedComment = commentMapper.save(comment);

        // 更新帖子评论数
        post.setCommentCount(post.getCommentCount() + 1);
        postMapper.save(post);

        // 发送通知给帖子作者
        if (post.getUser() != null && !post.getUser().getId().equals(userId)) {
            notificationService.createCommentNotification(
                    post.getId(),
                    savedComment.getId(),
                    userId,
                    post.getUser().getId()
            );
        }

        CommentVO commentVO = convertToVO(savedComment);
        return ResponseVO.success(commentVO);
    }

    @Override
    public ResponseVO<Page<CommentVO>> getPostComments(Long postId, int page, int size) {
        Page<Comment> comments = commentMapper.findByPostId(postId, PageRequest.of(page - 1, size));
        Page<CommentVO> commentVOs = comments.map(this::convertToVO);
        return ResponseVO.success(commentVOs);
    }

    @Override
    public ResponseVO<CommentVO> getCommentById(Long id) {
        Comment comment = commentMapper.findById(id).orElse(null);
        if (comment == null) {
            return ResponseVO.error(ErrorCodeConstant.CODE_NOT_FOUND, MessageConstant.COMMENT_NOT_FOUND);
        }
        CommentVO commentVO = convertToVO(comment);
        return ResponseVO.success(commentVO);
    }

    @Override
    public ResponseVO<CommentVO> updateComment(Long id, Long userId, String content) {
        Comment existingComment = commentMapper.findById(id).orElse(null);
        if (existingComment == null) {
            return ResponseVO.error(ErrorCodeConstant.CODE_NOT_FOUND, MessageConstant.COMMENT_NOT_FOUND);
        }

        // 检查是否是评论作者
        if (existingComment.getUser() == null || !existingComment.getUser().getId().equals(userId)) {
            return ResponseVO.error(ErrorCodeConstant.CODE_FORBIDDEN, MessageConstant.FORBIDDEN);
        }

        existingComment.setContent(content);
        Comment savedComment = commentMapper.save(existingComment);
        CommentVO commentVO = convertToVO(savedComment);
        return ResponseVO.success(commentVO);
    }

    @Override
    @Transactional
    public ResponseVO<Void> deleteComment(Long id, Long userId) {
        Comment comment = commentMapper.findById(id).orElse(null);
        if (comment == null) {
            return ResponseVO.error(ErrorCodeConstant.CODE_NOT_FOUND, MessageConstant.COMMENT_NOT_FOUND);
        }

        // 检查是否是评论作者或管理员
        User user = userMapper.findById(userId).orElse(null);
        if (user == null) {
            return ResponseVO.error(ErrorCodeConstant.CODE_NOT_FOUND, MessageConstant.USER_NOT_FOUND);
        }

        boolean isAuthor = comment.getUser() != null && comment.getUser().getId().equals(userId);
        boolean isAdmin = user.getRole() != null && user.getRole() == BusinessConstant.ROLE_ADMIN;

        if (!isAuthor && !isAdmin) {
            return ResponseVO.error(ErrorCodeConstant.CODE_FORBIDDEN, MessageConstant.FORBIDDEN);
        }

        // 删除评论
        Long postId = comment.getPost() != null ? comment.getPost().getId() : null;
        commentMapper.delete(comment);

        // 更新帖子评论数
        if (postId != null) {
            Post post = postMapper.findById(postId).orElse(null);
            if (post != null && post.getCommentCount() > 0) {
                post.setCommentCount(post.getCommentCount() - 1);
                postMapper.save(post);
            }
        }

        return ResponseVO.success(null);
    }

    // 辅助方法：将Comment转换为CommentVO
    private CommentVO convertToVO(Comment comment) {
        CommentVO vo = new CommentVO();
        vo.setId(comment.getId());
        vo.setContent(comment.getContent());
        vo.setStatus(comment.getStatus());
        vo.setCreatedAt(comment.getCreatedAt());

        // 设置postId
        if (comment.getPost() != null) {
            vo.setPostId(comment.getPost().getId());
        }

        // 设置parentId
        if (comment.getParent() != null) {
            vo.setParentId(comment.getParent().getId());
        }

        // 设置作者信息
        if (comment.getUser() != null) {
            vo.setAuthorId(comment.getUser().getId());
            vo.setAuthorName(comment.getUser().getUsername());
            vo.setAuthorAvatar(comment.getUser().getAvatar());
        }

        return vo;
    }
}

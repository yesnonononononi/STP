package com.summit.stp.post.application.service.impl;

import com.summit.stp.common.application.api.result.Result;
import com.summit.stp.common.application.domain.exception.ParameterException;
import com.summit.stp.common.application.domain.model.UserSession;
import com.summit.stp.common.application.service.TextSafe.TextSafeServiceProvider;
import com.summit.stp.common.auth.UserHolder;
import com.summit.stp.post.api.dto.request.ImageInfo;
import com.summit.stp.post.application.command.CreatePostCommand;
import com.summit.stp.post.application.service.PostCacheProvider;
import com.summit.stp.post.application.service.PostMessageSender;
import com.summit.stp.post.application.service.PostQueryService;
import com.summit.stp.post.domain.model.Post;
import com.summit.stp.post.domain.model.PostStatus;
import com.summit.stp.post.domain.model.PostType;
import com.summit.stp.post.domain.repository.PostCollectRepository;
import com.summit.stp.post.domain.repository.PostImageRepository;
import com.summit.stp.post.domain.repository.PostLikeRepository;
import com.summit.stp.post.domain.repository.PostRepository;
import com.summit.stp.post.infrastructure.persistence.mapper.PostsMapper;
import com.summit.stp.tag.application.service.TagCacheProvider;
import com.summit.stp.tag.domain.model.Tag;
import com.summit.stp.tag.domain.repository.PostTagRelRepository;
import com.summit.stp.tag.domain.repository.TagRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostAppServiceImplTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostImageRepository postImageRepository;

    @Mock
    private TextSafeServiceProvider textSafeServiceProvider;

    @Mock
    private PostQueryService postQueryService;

    @Mock
    private PostCacheProvider postCacheProvider;

    @Mock
    private PostMessageSender postMessageSender;

    @Mock
    private PostLikeRepository postLikeRepository;

    @Mock
    private PostCollectRepository postCollectRepository;

    @Mock
    private PostsMapper postsMapper;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private PostTagRelRepository postTagRelRepository;

    @Mock
    private TagCacheProvider tagCacheProvider;

    @Mock
    private TransactionTemplate transactionTemplate;

    @InjectMocks
    private PostAppServiceImpl postAppService;

    private static final Long TEST_USER_ID = 10001L;

    private static final Long GENERATED_POST_ID = 88888L;

    @BeforeEach
    void setUp() {
        UserSession session = UserSession.builder()
                .id(TEST_USER_ID)
                .username("testUser")
                .build();
        UserHolder.setUser(session);
    }

    @AfterEach
    void tearDown() {
        UserHolder.clear();
    }

    @Test
    @DisplayName("发布图文帖子 - 成功流程测试")
    void testCreatePost_Success() {
        CreatePostCommand command = buildMockCommand("测试标题", "这是一篇CI自动化测试帖子内容", PostType.IMAGE.getCode());

        when(textSafeServiceProvider.xssFilter(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(tagRepository.findByIds(anyList()))
                .thenReturn(List.of(Tag.builder().id(1L).tagName("Java").build()));
        // 让 TransactionTemplate 直接执行回调，避免依赖真实事务管理器
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(null);
        });
        when(postRepository.savePost(any(Post.class))).thenReturn(GENERATED_POST_ID);

        Result<Void> result = postAppService.createPost(command);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(1, result.getCode());

        verify(postRepository).savePost(any(Post.class));
        verify(postImageRepository).batchSave(anyList());
        verify(postTagRelRepository).batchSave(anyLong(), anyList());
        verify(postMessageSender, times(1)).sendEsPostUpdateEvent(any());
    }

    @Test
    @DisplayName("发布帖子 - 标题超长校验失败")
    void testCreatePost_TitleExceedsLimit() {
        String longTitle = "A".repeat(101);
        CreatePostCommand command = buildMockCommand(longTitle, "测试内容", PostType.TEXT.getCode());

        assertThrows(ParameterException.class, () -> postAppService.createPost(command));
        verify(postRepository, never()).savePost(any(Post.class));
    }

    private CreatePostCommand buildMockCommand(String title, String content, Integer type) {
        return CreatePostCommand.builder()
                .title(title)
                .content(content)
                .type(type)
                .status(PostStatus.NORMAL.getCode())
                .tagIds(List.of(1L))
                .mediaUrls(List.of(new ImageInfo("https://example.com/mock.jpg", 100, 100)))
                .build();
    }
}

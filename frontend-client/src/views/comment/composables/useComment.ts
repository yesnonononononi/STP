import {reactive, ref, type Ref, watch} from 'vue'
import {CommentAPI} from '@/services/comment'
import type {CommentVO} from '@/views/comment/types/comment'
import {init} from '@/views/comment/types/comment'
import {XssUtils} from '@/utils/xss'
import {CommonAPI} from '@/services/common/api'
import {log} from '@/utils/log'
import {useAuthStore} from '@/views/auth/store'

export enum CommentType {
  IMAGE = 1,
  VIDEO = 2,
  TEXT = 3,
  AUDIO = 4,
}

export function useComment(postIdRef: Ref<string | number | undefined>) {
  const comments = ref<CommentVO[]>([])

  const getImageDimensions = (file: File): Promise<{ width: number; height: number }> => {
    return new Promise((resolve) => {
      if (!file.type.startsWith('image/')) {
        resolve({ width: 0, height: 0 })
        return
      }
      const img = new Image()
      const objectUrl = URL.createObjectURL(file)
      img.src = objectUrl
      img.onload = () => {
        resolve({ width: img.naturalWidth, height: img.naturalHeight })
        URL.revokeObjectURL(objectUrl)
      }
      img.onerror = () => {
        resolve({ width: 0, height: 0 })
        URL.revokeObjectURL(objectUrl)
      }
    })
  }
  const loading = ref(false)
  const replyInput = ref<string>('')
  const nextCursor = ref<string | null>(null)
  const state = reactive({
    expended: false,
    hasMore: true,
  })

  async function like(comment: CommentVO) {
    const authStore = useAuthStore()
    if (!authStore.token) {
      log.warning('请先登录后操作')
      authStore.showLoginDialog()
      return
    }
    const isOk = (await CommentAPI.like(comment.id)).data
    comment.item.likeCount = (Number(comment.item.likeCount) || 0) + (isOk ? 1 : -1)
    comment.item.isLike = isOk.valueOf()
  }

  async function top(comment: CommentVO) {
    const authStore = useAuthStore()
    if (!authStore.token) {
      log.warning('请先登录后操作')
      authStore.showLoginDialog()
      return
    }
    const postId = postIdRef.value
    if (!postId) return
    if (comment.rootId || comment.parentId) {
      return
    }
    const res = await CommentAPI.top(comment.id, postId.toString())
    if (res.code !== 1) return
    comments.value = []
    nextCursor.value = null
    state.hasMore = true
    await loadData()
  }

  async function loadReply(comment?: CommentVO) {
    const postId = comment?.postId || postIdRef.value
    const rootId = comment?.rootId || comment?.id
    if (comment && comment.FunctionField) {
      if (comment.FunctionField.loading || !comment.FunctionField.hasMore) return
      comment.FunctionField.loading = true
    }
    try {
      const currentReplies = comment?.FunctionField?.replys || []
      const cursor = currentReplies[currentReplies.length - 1]?.id
      const res = (
        await CommentAPI.getReplies({
          cursor: cursor,
          postId: postId?.toString() || '',
          rootId: rootId?.toString() || '',
          limit: 10,
        })
      ).data
      if (res) {
        if (res.list && res.list.length > 0) {
          const newReplies = [
            ...new Map([...currentReplies, ...res.list].map((v) => [v.id, v])).values(),
          ]
          newReplies.sort((a, b) => a.id.localeCompare(b.id))
          processReplies(newReplies, newReplies)
          if (comment && comment.FunctionField) comment.FunctionField.replys = newReplies
        }
        if (comment && comment.FunctionField) {
          comment.FunctionField.hasMore = res.hasMore
        }
      }
    } catch (err) {
      console.error('加载回复失败:', err)
    } finally {
      if (comment && comment.FunctionField) comment.FunctionField.loading = false
    }
  }

  async function loadData(isReply: boolean = false, comment?: CommentVO) {
    if (isReply) {
      await loadReply(comment)
      return
    }
    if (loading.value || !state.hasMore || !postIdRef.value) return

    loading.value = true
    try {
      let idCursor, hsCursor
      if (nextCursor.value) {
        const cursor = nextCursor.value.split('_')
        hsCursor = cursor[0]
        idCursor = cursor[1]
      }
      const res = (
        await CommentAPI.getByPostId({
          postId: postIdRef.value.toString(),
          idCursor: idCursor || '',
          hsCursor: hsCursor || '',
          limit: 10,
        })
      ).data

      if (res) {
        if (res.list && res.list.length > 0) {
          processComments(res.list, true)
        }
        nextCursor.value = res.cursor
        state.hasMore = res.hasMore
      }
    } catch (err) {
      console.error('加载评论失败:', err)
    } finally {
      loading.value = false
    }
  }

  function reply(comment: CommentVO, content: string) {
    let rootId = comment.rootId || comment.id
    if (!rootId) return
    const parentId = comment.id
    const postId = comment.postId || 0
    CommentAPI.post({
      postId: postId.toString(),
      rootId: rootId.toString(),
      parentId: parentId.toString(),
      type: 3, // TEXT
      content: content,
      extra: undefined,
    }).then((res) => {
      if (res.code === 1 && res.data) {
        const newReply = res.data as CommentVO
        init(newReply)

        const rootComment = comments.value.find((c) => c.id.toString() === rootId.toString())
        if (rootComment) {
          rootComment.item.replyCount = (Number(rootComment.item.replyCount) || 0) + 1
          if (rootComment.FunctionField) {
            rootComment.FunctionField.expandMore = true

            // 如果是在别人的二级回复下再回复，设置被回复者的昵称进行展示
            if (parentId !== rootId) {
              const parentComment = rootComment.FunctionField.replys.find((c) => c.id === parentId)
              if (parentComment) {
                newReply.replyToUser = parentComment.publisher?.nick
              } else if (comment.id === parentId) {
                newReply.replyToUser = comment.publisher?.nick
              }
            }

            rootComment.FunctionField.replys.push(newReply)
            // 按 ID 排序实现二级评论发布时间升序排列
            rootComment.FunctionField.replys.sort((a, b) => a.id.localeCompare(b.id))
            rootComment.FunctionField.showCount = rootComment.FunctionField.replys.length
          }
        }
      }
    })
  }

  /**
   * 附件处理与上传函数
   *
   * 【上传策略说明】
   * 当前采用的是“提交时上传（Submit-Time Upload）”策略。
   * 用户在选择图片/视频/音频媒体文件时，前端并不会立刻向服务器发起上传请求（仅在浏览器本地生成 Blob URL 用于预览）。
   * 只有当用户点击“发表评论”按钮触发最终提交时，本函数才会被调用。
   * 它将遍历文件列表，异步依次将未上传的文件通过 CommonAPI.upload 上传至服务器，
   * 并在全部上传成功后装配成符合后端接收规范的 extra（媒体配置）和最终的评论类型返回。
   *
   * @param files 待上传的多媒体文件列表
   * @returns 包含 type (最终评论类型) 和 extra (JSON 扩展实体) 的对象
   */
  async function uploadAndAssembleExtra(files?: any[]): Promise<{ type: CommentType; extra: any }> {
    let extra: any = {}
    let type = CommentType.TEXT // default to TEXT

    if (!files || files.length === 0) {
      return { type, extra }
    }

    const firstFile = files[0]
    const fileType = firstFile.raw?.type || ''
    const fileName = firstFile.name || ''

    const isVideo = fileType.startsWith('video/') || /\.(mp4|webm|ogg|mov)$/i.test(fileName)
    const isAudio = fileType.startsWith('audio/') || /\.(mp3|wav|ogg|aac|m4a)$/i.test(fileName)

    if (isVideo) {
      type = CommentType.VIDEO
      let videoUrl = firstFile.url
      if (!videoUrl || videoUrl.startsWith('blob:') || videoUrl.startsWith('data:')) {
        const res = await CommonAPI.upload(firstFile.raw, 'comment-media')
        if (res.code === 1 && res.data) {
          videoUrl = res.data.url
        } else {
          throw new Error(res.errMsg || '视频上传失败')
        }
      }
      extra = {
        mediaType: 3, // MediaType.VIDEO
        mediaUrl: videoUrl,
      }
    } else if (isAudio) {
      type = CommentType.AUDIO
      let audioUrl = firstFile.url
      if (!audioUrl || audioUrl.startsWith('blob:') || audioUrl.startsWith('data:')) {
        const res = await CommonAPI.upload(firstFile.raw, 'comment-media')
        if (res.code === 1 && res.data) {
          audioUrl = res.data.url
        } else {
          throw new Error(res.errMsg || '音频上传失败')
        }
      }
      extra = {
        mediaType: 2, // MediaType.AUDIO
        mediaUrl: audioUrl,
        audioMoment: {
          audioUrl: audioUrl,
          audioName: firstFile.name || 'audio',
          duration: firstFile.duration || 0,
          typeCode: 4,
        },
      }
    } else {
      type = CommentType.IMAGE
      const uploadPromises = files.map(async (file, index) => {
        if (file.url && !file.url.startsWith('blob:') && !file.url.startsWith('data:')) {
          return {
            imageUrl: file.url,
            imageName: file.name || '',
            width: file.width || 0,
            height: file.height || 0,
            typeCode: 1,
            sortOrder: index,
            size: file.size || 0,
          }
        }

        let dims = { width: 0, height: 0 }
        if (file.raw && file.raw.type.startsWith('image/')) {
          dims = await getImageDimensions(file.raw)
        }

        const res = await CommonAPI.upload(file.raw, 'comment-media')
        if (res.code === 1 && res.data) {
          return {
            imageUrl: res.data.url,
            imageName: res.data.fileName || file.name || '',
            width: dims.width,
            height: dims.height,
            typeCode: 1,
            sortOrder: index,
            size: file.raw.size || file.size || 0,
          }
        } else {
          throw new Error(res.errMsg || `${file.name} 上传失败`)
        }
      })

      const imageMoments = await Promise.all(uploadPromises)
      extra = {
        mediaType: 1, // MediaType.IMAGE
        imageMoments: imageMoments,
      }
    }

    return { type, extra }
  }

  async function publish(
    e: any,
    parentId?: number | string,
    rootId?: number | string,
    files?: any[],
  ) {
    const authStore = useAuthStore()
    if (!authStore.token) {
      log.warning('请先登录后操作')
      authStore.showLoginDialog()
      return
    }
    if ((!replyInput.value && files?.length == 0) || !postIdRef.value) return
    if (e && typeof e === 'object') {
      if (e.shiftKey) return
      if (typeof e.preventDefault === 'function') {
        e.preventDefault()
      }
    }
    const content = XssUtils.filter(replyInput.value)

    let uploadResult
    try {
      uploadResult = await uploadAndAssembleExtra(files)
    } catch (err: any) {
      log.error(err.message || '文件上传失败')
      console.error(err)
      throw err
    }

    return CommentAPI.post({
      postId: postIdRef.value.toString(),
      rootId: rootId?.toString(),
      parentId: parentId?.toString(),
      type: uploadResult.type,
      content: content,
      extra: uploadResult.extra,
    }).then((res) => {
      if (res.code === 1 && res.data) {
        replyInput.value = ''
        const newComment = res.data as CommentVO
        init(newComment)
        comments.value.unshift(newComment) // 丝滑插入一级评论列表最前
        return res
      } else {
        log.error(res.errMsg || '发表评论失败')
        throw new Error(res.errMsg || '发表评论失败')
      }
    })
  }

  function processReplies(replies: CommentVO[], allComments: CommentVO[]) {
    replies.forEach((reply) => {
      if (reply.parentId && reply.rootId && reply.parentId !== reply.rootId) {
        const parentComment = allComments.find((c) => c.id === reply.parentId)
        if (parentComment) {
          reply.replyToUser = parentComment.publisher?.nick
        }
      }
    })
  }

  function processComments(commentList: CommentVO[], isAppend: boolean = false) {
    const oneComment = commentList.filter((comment) => !comment.rootId && !comment.parentId)
    oneComment.forEach((comment) => {
      init(comment)
      const replys = commentList.filter((item) => item.rootId === comment.id)
      replys.sort((a, b) => a.id.localeCompare(b.id))
      processReplies(replys, commentList)
      comment.FunctionField!.replys = replys
    })
    if (isAppend) {
      comments.value = [...comments.value, ...oneComment]
    } else {
      comments.value = oneComment
    }
  }

  function attemptAcquireRepliedComment(parentId: string, rootId: string): string {
    const rootList = comments.value.filter((comment) => comment.id === rootId)
    if (!rootList || rootList.length == 0) {
      return '未知用户'
    }
    const resList = rootList[0]?.FunctionField?.replys.filter((comment) => comment.id === parentId)
    if (resList && resList.length == 1) {
      return resList[0]!.publisher.nick
    }
    return '未知用户'
  }

  const expendMore = (comment: CommentVO) => {
    return (
      !comment.FunctionField?.expandMore && comment.item.replyCount && comment.item.replyCount > 0
    )
  }

  // 监听 postId 改变时重置并重新加载
  watch(
    postIdRef,
    (newId) => {
      comments.value = []
      nextCursor.value = null
      state.hasMore = true
      if (newId) {
        loadData()
      }
    },
    { immediate: true },
  )

  return {
    comments,
    loading,
    replyInput,
    state,
    like,
    top,
    loadReply,
    loadData,
    reply,
    publish,
    expendMore,
    attemptAcquireRepliedComment,
    checkCommentType,
  }
}

/**
 * 评论类型展示与资源校验助手函数
 *
 * 根据传入的目标类型，不仅校验评论的 type 字段，
 * 还会确保 extra 扩展参数中所关联的多媒体资源（如图片列表、音视频 URL）真实有效，
 * 以确保页面 vif 渲染条件的高可读性与无错性。
 *
 * @param comment 评论展示VO实体
 * @param type 需要匹配的评论类型
 * @returns boolean 是否匹配且多媒体资源有效
 */
export function checkCommentType(comment: CommentVO, type: CommentType): boolean {
  if (comment.type !== type) {
    return false
  }
  if (type === CommentType.IMAGE) {
    return !!(comment.extra?.imageMoments && comment.extra.imageMoments.length > 0)
  }
  if (type === CommentType.VIDEO || type === CommentType.AUDIO) {
    return !!comment.extra?.mediaUrl
  }
  return true
}

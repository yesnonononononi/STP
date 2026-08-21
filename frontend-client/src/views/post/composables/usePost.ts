import {nextTick, onMounted, onUnmounted, ref, watch} from 'vue'
import {onBeforeRouteLeave, useRoute} from 'vue-router'
import router from '@/router'
import {type CreatePostRequest, PostAPI, PostStatus, PostType, TagAPI, type TagVO,} from '@/services/post'
import {CommonAPI} from '@/services/common/api'
import {log} from '@/utils/log'
import {useUserInfoStore} from '@/stores/userInfo'
import {getCursorXY} from '../utils/cursor'

/**
 * 动态发布与编辑页面业务逻辑 Composable
 *
 * 整合了动态表单数据、媒体上传校验、话题联想交互、
 * 页面生命周期挂载、空白点击防丢拦截与草稿自动保存等完整发帖业务流。
 */
export function usePost() {
  const submitting = ref(false)
  /** 当前用户正在录入的话题临时搜索关键字 */
  const userAddTagContent = ref('')
  const route = useRoute()
  /** 编辑状态下的帖子 ID */
  const postId = route.query.postId as string
  /** 文件上传或校验错误提示信息 */
  const uploadError = ref('')
  /** 媒体文件放大预览 Dialog 显隐状态 */
  const dialogVisible = ref(false)
  /** 预览图片的 Blob 或远程网络 URL 链接 */
  const previewImageUrl = ref('')
  /** 预览视频的 Blob 或远程网络 URL 链接 */
  const previewVideoUrl = ref('')
  /** 帖子是否已成功提交发布状态标识 */
  const isSubmitSuccess = ref(false)
  const user = useUserInfoStore().user
  /** 备用新标签名称 */
  const newTag = ref<string>()

  /** 局部的“请选择话题”面板可见性 */
  const visibleLocalSelector = ref(false)
  /** 输入 "#" 号后触发的光标定位悬浮话题面板可见性 */
  const visibleCaretSelector = ref(false)

  /** 帖子创建/编辑请求表单 */
  const form = ref<CreatePostRequest>({
    title: '',
    type: PostType.TEXT,
    content: '',
    mediaUrls: [],
    status: PostStatus.NORMAL,
    isTop: 0,
    visibleScope: 1,
  })
  /** 用户在文本域中输入的原始内容 */
  const userPostContent = ref('')
  /** 当前帖子已绑定的标签列表 */
  const selectedTags = ref<TagVO[]>([])
  /** 供选择的预置话题建议列表 */
  const tagList = ref<TagVO[]>()
  /** 上传至前端 of 媒体文件列表对象，支持回显与本地 Blob blob: 预览 */
  const fileList = ref<any[]>([])
  /** 内部状态：用户是否正处于输入 "#" 录入话题的过程 */
  const addTaging = ref(false)
  /** 当前最新的联想话题建议缓存列表 */
  const currentSuggests = ref<any[]>([])

  /** 初始化加载数据，若 query 携带 postId 则为编辑模式，回显帖子标题、正文、媒体和标签 */
  onMounted(async () => {
    if (postId) {
      const res = await PostAPI.getById(postId)
      const postData = res.data

      // 复制基本属性，避免类型覆盖冲突
      form.value.title = postData.title
      form.value.type = postData.type
      form.value.content = postData.content || ''
      form.value.status = (postData.status as PostStatus) || PostStatus.NORMAL
      form.value.isTop = postData.isTop !== undefined ? postData.isTop : 0
      form.value.visibleScope = postData.visibleScope !== undefined ? postData.visibleScope : 1

      // 提取并剥离 content 中的标签 a，保持文本输入框展示纯文本
      let cleanContent = postData.content || ''
      cleanContent = cleanContent.replace(/<a[^>]*>#(.*?)#<\/a>/g, '').trim()
      userPostContent.value = cleanContent

      // 转换并回显媒体文件列表
      if (
        postData.type === PostType.IMAGE &&
        postData.mediaUrls &&
        Array.isArray(postData.mediaUrls)
      ) {
        form.value.mediaUrls = postData.mediaUrls.map((item: any) => ({
          url: item.imageUrl || item.url,
          width: item.width,
          height: item.height,
        }))
        fileList.value = postData.mediaUrls.map((item: any, index: number) => {
          return {
            name: `file_${index}`,
            url: item.imageUrl || item.url,
            type: 'image/jpeg',
            uid: Date.now() + index,
            width: item.width,
            height: item.height,
          }
        })
      } else if (
        postData.type === PostType.VIDEO &&
        postData.extraMediaUrl &&
        fileList.value.length == 0
      ) {
        fileList.value.push({
          name: ' video.mp4',
          url: postData.extraMediaUrl,
          type: 'video/mp4',
          uid: Date.now(),
        })
        form.value.mediaUrls = [
          {
            url: postData.extraMediaUrl,
            width: 0,
            height: 0,
          },
        ]
      } else {
        form.value.mediaUrls = []
        fileList.value = []
      }

      if (postData.tags) {
        selectedTags.value = postData.tags
      }
    }
    loadTags()
    const queryTag = route.query.tag as string
    if (queryTag) {
      try {
        const res = await TagAPI.getSearchSuggest(queryTag, 1)
        const suggest = res.data?.suggestList?.[0]
        if (suggest && suggest.keyword === queryTag) {
          selectedTags.value.push({
            id: suggest.id,
            tagName: suggest.keyword
          } as any)
        } else {
          selectedTags.value.push({
            id: Date.now(),
            tagName: queryTag
          } as any)
        }
      } catch (err) {
        console.error('Failed to pre-fill tag from query:', err)
      }
    }
  })

  /** 文本域对应 DOM 节点引用 */
  const textareaRef = ref<any>(null)
  /** 光标绝对定位文档坐标 */
  const popupPos = ref({ x: 0, y: 0 })

  // 监听输入正文，抓取 "#" 触发词以显示光标话题联想面板，同时将 "#" 字符移除
  watch(
    () => userPostContent.value,
    (newVal, oldVal) => {
      if (!newVal) {
        visibleCaretSelector.value = false
        addTaging.value = false
        return
      }

      const textarea = textareaRef.value?.$el?.querySelector('textarea') as HTMLTextAreaElement
      const selectionStart = textarea ? textarea.selectionStart : 0

      // 判断是插入字符操作，且定位到刚刚插入的字符
      const isInsert = oldVal ? newVal.length > oldVal.length : true
      const insertedChar = selectionStart > 0 ? newVal.charAt(selectionStart - 1) : newVal.charAt(newVal.length - 1)

      if ((insertedChar === '#' || insertedChar === '＃') && isInsert) {
        addTaging.value = true

        // 精准计算要删除的 '#' 号的位置
        const targetCursorPos = selectionStart > 0 ? selectionStart - 1 : newVal.length - 1
        // 瞬间抹去被输入的那一个 '#' 号，不污染正文其它文字
        const cleanVal = selectionStart > 0
          ? newVal.slice(0, targetCursorPos) + newVal.slice(selectionStart)
          : newVal.slice(0, -1)

        userPostContent.value = cleanVal

        nextTick(() => {
          const textarea = textareaRef.value?.$el?.querySelector('textarea') as HTMLTextAreaElement
          if (!textarea) return

          // 保持光标位置不动，避免其因赋值跳至文本末端
          textarea.setSelectionRange(targetCursorPos, targetCursorPos)

          // 依据光标处坐标定位话题面板
          const pos = getCursorXY(textarea, targetCursorPos)
          popupPos.value = pos
          visibleCaretSelector.value = true
        })
      }
    },
    { deep: true },
  )

  /**
   * 用户选择或自定义创建话题标签时的回调
   *
   * 若选择的是新话题（未注册），则会首先自动调用接口创建并反查其主键，
   * 之后将话题添加进已选列表。
   *
   * @param tag 选择的话题数据
   */
  async function handleAddTag(tag: import('@/services/post/types').TopicTag) {
    let finalId = tag.id
    if (tag.id === '' || tag.extra === -1) {
      try {
        await TagAPI.create({ tagName: tag.keyword })
        const res = await TagAPI.getSearchSuggest(tag.keyword, 1)
        const list = res.data?.suggestList || []
        const createdTag = list.find((t) => t.keyword === tag.keyword)
        if (createdTag) {
          finalId = createdTag.id
        } else {
          finalId = Date.now().toString()
        }
      } catch (err) {
        console.error('自动创建话题出错:', err)
        finalId = Date.now().toString()
      }
    }

    const isExist = selectedTags.value.some((t) => t.id === finalId)
    if (!isExist) {
      selectedTags.value.push({
        id: finalId,
        tagName: tag.keyword,
      } as any)
    }

    visibleLocalSelector.value = false
    visibleCaretSelector.value = false

    // 状态机重置
    addTaging.value = false

    nextTick(() => {
      const textarea = textareaRef.value?.$el?.querySelector('textarea') as HTMLTextAreaElement
      if (textarea) {
        textarea.focus()
      }
    })
  }

  /**
   * 点击局部的“请选择话题”时的控制逻辑
   */
  function handleSelectTopicClick() {
    visibleLocalSelector.value = !visibleLocalSelector.value
  }

  /**
   * 键盘事件的占位回调
   */
  const handleTextareaKeydown = (e: KeyboardEvent) => {
    // 话题录入的焦点已转移至弹窗内的 input，故在此不需要特殊处理
  }

  /**
   * 判断上传文件是否为视频格式
   *
   * @param file 待校验的原始文件或 UploadFile 封装对象
   * @returns 是否是视频
   */
  function isVideo(file: any): boolean {
    const rawFile = file.raw || file
    if (rawFile.type) {
      return rawFile.type.startsWith('video/')
    }
    const name = rawFile.name || ''
    return /\.(mp4|webm|ogg|mov)$/i.test(name)
  }

  /**
   * 触发大图/视频多媒体的预览弹框
   *
   * @param file 点击预览的文件对象
   */
  function handlePictureCardPreview(file: any) {
    previewImageUrl.value = ''
    previewVideoUrl.value = ''
    const url = file.url || (file.raw ? URL.createObjectURL(file.raw) : '')
    if (isVideo(file)) {
      previewVideoUrl.value = url
    } else {
      previewImageUrl.value = url
    }
    dialogVisible.value = true
  }

  /**
   * 限制和处理上传文件的变更校验
   *
   * 包括禁止多种格式混传、限制单视频、以及视频 50MB / 图片 10MB 的大小阈值校验。
   *
   * @param file 新增的文件
   * @param files 当前列表所有文件
   */
  function handleChange(file: any, files: any[]) {
    const video = isVideo(file)
    if (fileList.value.length === 0) {
      form.value.type = video ? PostType.VIDEO : PostType.IMAGE
    } else {
      if (form.value.type === PostType.VIDEO) {
        if (video) {
          log.warning('请勿上传多个视频文件')
        } else {
          log.warning('多种类型文件不能同时上传')
        }
        URL.revokeObjectURL(file.url)
        fileList.value = files.filter((f) => f.uid !== file.uid)
        return
      }
      if (form.value.type === PostType.IMAGE && video) {
        log.warning('多种类型文件不能同时上传')
        URL.revokeObjectURL(file.url)
        fileList.value = files.filter((f) => f.uid !== file.uid)
        return
      }
    }

    // 校验：文件大小
    const sizeMB = file.size / 1024 / 1024
    if (video) {
      if (sizeMB > 50) {
        log.error(`视频文件 [${file.name}] 不能超过 50MB`)
        URL.revokeObjectURL(file.url)
        fileList.value = files.filter((f) => f.uid !== file.uid)
        return
      }
    } else {
      if (sizeMB > 10) {
        log.error(`图片文件 [${file.name}] 不能超过 10MB`)
        URL.revokeObjectURL(file.url)
        fileList.value = files.filter((f) => f.uid !== file.uid)
        return
      }
    }

    if (file.status === 'ready') {
      file.url = URL.createObjectURL(file.raw)
    }
    fileList.value = files
  }

  /**
   * 移除已选择的媒体文件，销毁对应的本地 Blob 临时内存链接以规避内存泄漏
   *
   * @param file 被移除的媒体文件
   */
  function handleRemove(file: any) {
    if (file.url && file.url.startsWith('blob:')) {
      URL.revokeObjectURL(file.url)
    }
    fileList.value = fileList.value.filter((item) => item.uid !== file.uid)
    if (fileList.value.length === 0) {
      form.value.type = PostType.TEXT
    }
  }

  /**
   * 异步获取待上传图片的原始分辨率长宽尺寸，便于自适应布局
   *
   * @param file 图片文件
   * @returns 包含长宽的分辨率信息 Promise
   */
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

  /**
   * 核心动态发布提交方法
   *
   * 承担前置完整性校验、图片分片/大文件切片上传、组装媒体 JSON 并调用发帖 API 的全流程。
   * 支持通过 status 参数保存为草稿（DRAFT）。
   *
   * @param status 帖子发布状态 (默认 NORMAL-发布，DRAFT-草稿)
   */
  async function submit(status: PostStatus = PostStatus.NORMAL) {
    if (submitting.value) return
    if (!beforeCreateCheck()) return
    submitting.value = true
    form.value!.status = status
    form.value.tagIds = selectedTags.value
      .map((t) => String(t.id))
      .filter((id) => id !== undefined)


    
    try {
      if (fileList.value.length > 0) {
        uploadError.value = ''
        const uploadPromises = fileList.value.map(async (file) => {
          if (file.url && !file.url.startsWith('blob:')) {
            return {
              url: file.url,
              width: file.width || 0,
              height: file.height || 0,
            }
          }

          let dims = { width: 0, height: 0 }
          if (file.raw && file.raw.type.startsWith('image/')) {
            dims = await getImageDimensions(file.raw)
          }

          let res
          //如果是视频且大小大于5mb
          if (file.raw.type.startsWith('video/') && file.size > 5 * 1024 * 1024) {
            res = await CommonAPI.uploadLargeFile(file.raw, 'post-media')
          } else {
            res = await CommonAPI.upload(file.raw, 'post-media')
          }
          if (res.code === 1 && res.data) {
            return {
              url: res.data.url,
              width: dims.width,
              height: dims.height,
            }
          } else {
            throw new Error(res.errMsg || `${file.name} 上传失败`)
          }
        })

        const imageInfos = await Promise.all(uploadPromises)
        form.value.mediaUrls = imageInfos
      } else {
        form.value.type = PostType.TEXT
        form.value.mediaUrls = []
      }

      await PostAPI.create(form.value!)
      if (status === PostStatus.NORMAL) {
        log.success('发布成功')
        isSubmitSuccess.value = true
        router.push('/')
      } else {
        log.success('已自动保存至草稿')
      }
    } catch (err: any) {
      log.error(err.message || '发布失败')
      console.error(err)
    } finally {
      submitting.value = false
    }
  }

  /**
   * 点击标签时的翻转选择控制
   */
  function toggleTag(tag: TagVO) {
    const isExist = selectedTags.value.some((t) => t.id === tag.id)
    if (!isExist) {
      selectTag(tag)
    } else {
      removeTag(tag)
    }
  }

  /**
   * 添加选中的话题标签进入已选列表
   */
  function selectTag(tag: TagVO) {
    if (!tag || !tag.id) return
    if (selectedTags.value.some((t) => t.id === tag.id)) return
    selectedTags.value.push(tag)
  }

  /**
   * 分页拉取热门话题标签
   */
  async function loadTags() {
    tagList.value = (await TagAPI.getPage(1, 5)).data.records
  }

  /**
   * 移除已绑定的话题标签
   *
   * @param tag 被移除的话题标签VO
   */
  function removeTag(tag: TagVO) {
    if (!tag || tag.id === undefined || tag.id === null) return
    selectedTags.value = selectedTags.value.filter((t) => t.id !== tag.id)
  }

  /**
   * 创建帖子的前置完整性校验与多媒体大小硬校验
   *
   * @returns 校验结果是否通过
   */
  const beforeCreateCheck = () => {
    let finalContent = userPostContent.value || ''
    if (selectedTags.value.length > 0) {
      const tagsHtml = selectedTags.value
        .map((t) => `<a href="">#${t.tagName}#</a>`)
        .join(' ')
      finalContent = finalContent.trim() + '\n' + tagsHtml
    }
    form.value.content = finalContent
    if (!form.value) {
      log.error('未输入任何内容')
      return false
    } else if (!form.value.title) {
      log.error('请输入标题')
      return false
    }

    if (fileList.value.length > 10) {
      log.error('最多只能上传 10 个媒体文件')
      return false
    }

    for (const file of fileList.value) {
      const rawFile = file.raw
      if (!rawFile) continue
      const isVideoFile = isVideo(file)
      const sizeMB = rawFile.size / 1024 / 1024
      if (isVideoFile) {
        if (sizeMB > 50) {
          log.error(`视频文件 [${rawFile.name}] 不能超过 50MB`)
          return false
        }
      } else {
        if (sizeMB > 10) {
          log.error(`图片文件 [${rawFile.name}] 不能超过 10MB`)
          return false
        }
      }
    }
    return true
  }

  /** 浏览器窗口关闭或刷新时的防丢失提示监听 */
  const handleBeforeUnload = (e: BeforeUnloadEvent) => {
    if (isSubmitSuccess.value) return
    const hasContent =
      (userPostContent.value && userPostContent.value.trim().length > 0) ||
      (form.value.title && form.value.title.trim().length > 0) ||
      (fileList.value && fileList.value.length > 0)
    if (hasContent) {
      e.preventDefault()
      e.returnValue = ''
    }
  }

  /** 全局点击空白处关闭话题弹窗的监听 */
  const handleDocumentClick = (e: MouseEvent) => {
    const target = e.target as HTMLElement
    if (visibleCaretSelector.value || visibleLocalSelector.value) {
      const inTextarea = textareaRef.value?.$el?.contains(target)
      const inSelector = target.closest('.absolute') || target.closest('.el-select-dropdown')
      if (!inTextarea && !inSelector) {
        visibleCaretSelector.value = false
        visibleLocalSelector.value = false
        addTaging.value = false
      }
    }
  }

  onMounted(() => {
    window.addEventListener('beforeunload', handleBeforeUnload)
    document.addEventListener('click', handleDocumentClick)
  })

  onUnmounted(() => {
    window.removeEventListener('beforeunload', handleBeforeUnload)
    document.removeEventListener('click', handleDocumentClick)
  })

  /** 路由切换离开时的防丢提醒与存草稿行为守卫 */
  onBeforeRouteLeave(async (to, from, next) => {
    if (isSubmitSuccess.value) {
      next()
      return
    }
    const hasContent =
      (userPostContent.value && userPostContent.value.trim().length > 0) ||
      (form.value.title && form.value.title.trim().length > 0) ||
      (fileList.value && fileList.value.length > 0)
    if (hasContent) {
      if (confirm('有未发布的内容，将会保存至草稿')) {
        try {
          await submit(PostStatus.DRAFT)
        } catch (err) {
          console.error('保存草稿失败:', err)
        }
        next()
      } else {
        next()
      }
    } else {
      next()
    }
  })

  return {
    form,
    userPostContent,
    selectedTags,
    tagList,
    fileList,
    uploadError,
    dialogVisible,
    previewImageUrl,
    previewVideoUrl,
    visibleLocalSelector,
    visibleCaretSelector,
    newTag,
    popupPos,
    textareaRef,
    handleAddTag,
    handleSelectTopicClick,
    handleTextareaKeydown,
    isVideo,
    handlePictureCardPreview,
    handleChange,
    handleRemove,
    submitting,
    submit,
    toggleTag,
    removeTag,
  }
}

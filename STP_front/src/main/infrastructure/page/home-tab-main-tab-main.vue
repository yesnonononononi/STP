<template>
  <div class="container-comments w-full h-full flex flex-col">
    <div v-for="item in topicList" :key="item.id" class="p-8 pb-2 border-b border-b-gray-300">
      <div class="publisher flex items-center">
        <div class="avatar relative">
          <img
            class="w-12 h-12 rounded-full peer bg-gray-400"
            :src="item.publisher.avatar"
            alt=""
          />
          <div
            class="publisher-introduce opacity-0 pointer-events-none peer-hover:pointer-events-auto peer-hover:opacity-100 transition-opacity duration-400 delay-500 absolute top-14 w-96 h-48 bg-white z-10 border"
          >
            <div class="flex flex-col items-center">
              <div class="introduce p-4 pb-2 w-72 flex">
                <div class="flex items-center gap-6">
                  <div class="avatar">
                    <img
                      class="w-16 h-16 rounded-full bg-gray-200"
                      :src="item.publisher.avatar"
                      alt=""
                    />
                  </div>
                  <div class="detail">
                    <div class="grid grid-cols-3 gap-4">
                      <div class="fans flex flex-col p-2 items-center">
                        <span class="text-xs mb-2">粉丝</span>
                        <div class="text-md">{{ item.publisher.fans || 0 }}</div>
                      </div>
                      <div class="topic">
                        <div class="topic flex flex-col p-2 items-center">
                          <span class="text-xs mb-2">帖子</span>
                          <div class="text-md">{{ item.publisher.topic || 0 }}</div>
                        </div>
                      </div>
                      <div class="liked">
                        <div class="liked flex flex-col p-2 items-center">
                          <span class="text-xs mb-2">获赞</span>
                          <div class="text-md">{{ item.publisher.liked || 0 }}</div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              <div class="mid h-6 w-72 pl-5 flex items-center">
                <span class="truncate max-w-24 block">{{ item.publisher.nick }}</span>
                <div class="tag">
                  <el-tag type="success">Lv: {{ item.publisher.level || 0 }}</el-tag>
                </div>
              </div>

              <div class="habbit w-72 h-4 pl-5"></div>
              <div class="operation w-72 pl-5 h-12">
                <div class="flex items-center justify-between gap-2">
                  <el-button type="info" class="text-gray-500">关注</el-button>
                  <el-button type="success" class="text-gray-500">私信</el-button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="nick h-12">
          <span class="text-md p-2 truncate max-w-24 block">{{ item.publisher.nick }}</span>
          <div class="h-[50%] mx-2">
            <span class="text-xs mr-2 text-gray-400">{{ item.publishTime }}</span>
            <span class="text-xs text-gray-500">{{ item.publisher.ip }}</span>
          </div>
        </div>
      </div>
      <div class="content p-4 pl-0">
        <p class="line-clamp-3 text-gray-700">{{ item.content.content }}</p>
      </div>
      <div class="items w-72 h-4 grid grid-cols-4 gap-1">
        <div class="like flex items-center gap-1 hover:text-blue-300 cursor-pointer">
          <el-icon>
            <Star />
          </el-icon>
          <span class="text-md text-gray-500">{{ item.likeCount || 0 }}</span>
        </div>
        <div class="comment flex items-center gap-1 hover:text-blue-300 cursor-pointer">
          <el-icon>
            <ChatDotRound />
          </el-icon>
          <span class="text-md text-gray-500">{{ item.replyCount || 0 }}</span>
        </div>
        <div class="collect flex items-center gap-1 hover:text-blue-300 cursor-pointer">
          <el-icon>
            <Star />
          </el-icon>
          <span class="text-md text-gray-500">{{ item.collectedCount || 0 }}</span>
        </div>
        <div class="shared flex items-center gap-1 hover:text-blue-300 cursor-pointer">
          <el-icon>
            <Share />
          </el-icon>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { onMounted } from 'vue'
import type { topicItem } from '../../api/types/comment'
import { Star, ChatDotRound, Share } from '@element-plus/icons-vue'
import { Topic } from '@/main/api/hooks/topic'
import type { Symbols } from '@/main/api/types/statics'
const topic = new Topic()
let curTab: Symbols = { id: '', name: '' }
let topicList: topicItem[] = [
  {
    id: '1',
    publisher: {
      id: '1',
      avatar: '',
      nick: '张三sfafdsdfsdfsdfsdf',
      level: 1,
      ip: '湖南',
    },
    content: {
      type: 'text',
      title: '这是标题',
      content: '这是评论内容',
      img: '',
      video: '',
      audio: '',
    },
    publishTime: '5-16 09:05:05',
    likeCount: '12',
    replyCount: '1',
    isLiked: true,
    collectedCount: '',
    isCollected: true,
  },
]
onMounted(() => {
  topicList = topic.queryTopicList(curTab.id).data
})
</script>

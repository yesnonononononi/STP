<template>
  <Teleport to="body">
    <div v-if="visible" class="fixed inset-0  w-screen h-screen bg-gray-400/50 z-40">
      <div class="postInfo w-2/5 m-auto h-full ">
        <postInfo v-if="curPost" @close="curPost = null; visible = false" :visible="visible" :post="curPost" />
      </div>
    </div>
  </Teleport>
  <div class="main relative flex min-h-screen flex-col items-center w-full">
    <div class="mid flex items-center justify-around gap-4 w-full max-w-300 px-4 h-24 m-4">
      <div
        class="flex-1 bg-linear-to-br from-white to-amber-50/10 p-4 cursor-pointer group rounded-xl border border-gray-100 hover:border-amber-200 hover:shadow-[0_8px_30px_rgb(251,191,36,0.1)] hover:-translate-y-0.5 hover:scale-[1.01] transition-all duration-300"
        @click="router.push({ name: 'coupon' })">
        <div class="flex items-center gap-3">
          <div
            class="p-2 bg-amber-50 rounded-xl group-hover:scale-110 group-hover:bg-amber-100 transition-all duration-300 shrink-0 flex items-center justify-center">
            <svg t="1781768147155" class="icon w-8 h-8" viewBox="0 0 1024 1024" version="1.1"
              xmlns="http://www.w3.org/2000/svg" p-id="5006">
              <path
                d="M836.152889 224.009481a75.851852 75.851852 0 0 1 75.851852 75.851852v116.129186a96.009481 96.009481 0 0 0 0 192v116.167111a75.851852 75.851852 0 0 1-75.851852 75.851851H187.847111a75.851852 75.851852 0 0 1-75.851852-75.851851v-116.167111a96.009481 96.009481 0 0 0 0-191.981038v-116.148148a75.851852 75.851852 0 0 1 75.851852-75.851852h648.305778z m-383.469037 138.733038a24.007111 24.007111 0 0 0-33.943704 33.943703l51.313778 51.313778h-46.061037a24.007111 24.007111 0 1 0 0 47.995259h64v32.009482h-64a24.007111 24.007111 0 1 0 0 47.995259h64v80.004741a24.007111 24.007111 0 1 0 48.014222 0l-0.018963-80.023704 64.018963 0.018963a24.007111 24.007111 0 1 0 0-47.995259h-64.018963v-32.009482h64.018963a24.007111 24.007111 0 1 0 0-47.995259h-46.08l51.332741-51.313778 1.744592-1.953185a24.007111 24.007111 0 0 0-35.688296-31.990518l-56.566518 56.566518-1.744593 1.953185-0.986074 1.365334a24.139852 24.139852 0 0 0-2.768593-3.318519z"
                fill="#f59e0b" p-id="5007"></path>
            </svg>
          </div>
          <div class="flex flex-col gap-0.5 justify-start">
            <span
              class="text-gray-800 font-semibold group-hover:text-amber-500 transition-colors duration-200 text-sm">优惠券</span>
            <span class="text-gray-400 text-xs">更多优惠福利</span>
          </div>
          <div
            class="ml-auto opacity-0 group-hover:opacity-100 group-hover:translate-x-1 transition-all duration-300 text-amber-500 shrink-0">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2.5"
              stroke="currentColor" class="w-4 h-4">
              <path stroke-linecap="round" stroke-linejoin="round" d="M8.25 4.5l7.5 7.5-7.5 7.5" />
            </svg>
          </div>
        </div>
      </div>
      <div class="flex-1 bg-white"></div>
      <div class="flex-1 bg-white"></div>
      <div class="flex-1 bg-white"></div>
    </div>
    <div class="flex items-start w-full max-w-300 px-4 gap-4 relative">
      <div class="left w-[75%] min-h-125 rounded-lg">
        <div
          class="tab flex items-center p-4 h-12 border-b-gray-200 border-b justify-between sticky top-16  bg-white z-9">
          <div class="grid grid-cols-6 p-2 w-[70%] sticky top-0 font-semibold ">
            <span class="flex-1 " v-for="item in items" :key="item.id"
              :class="curTab == item.id ? 'bg-clip-text text-transparent bg-linear-to-r transition-all duration-200 from-blue-200 to-blue-400' : ''">
              <span class="cursor-pointer" @click="handleTabClick(item.id)">
                {{ item.name }}
              </span>
            </span>
          </div>
          <div class="flex items-center gap-4 text-xs font-semibold text-gray-400 select-none mr-2">
            <span class="cursor-pointer px-2.5 py-1 rounded-md transition-all duration-200"
              :class="orderType === 'new' ? 'text-blue-500 bg-blue-50/80 scale-105 shadow-xs' : 'hover:text-gray-600 hover:bg-gray-50'"
              @click="toggleOrder('new')">
              最新
            </span>
            <span class="cursor-pointer px-2.5 py-1 rounded-md transition-all duration-200"
              :class="orderType === 'hot' ? 'text-amber-500 bg-amber-50/80 scale-105 shadow-xs' : 'hover:text-gray-600 hover:bg-gray-50'"
              @click="toggleOrder('hot')">
              最热
            </span>
          </div>
        </div>
        <div class="container-comments  bg-white  w-full h-full min-h-200  flex flex-col">
          <PostItem v-for="item in topicList" :key="item.id" :post="item" @like="like" @collect="collect"
            @comment-click="curPost = $event; visible = true;" />
          <div v-if="topicList.length == 0" class="flex items-center min-h-200 justify-center inset-0">
            <div class="flex items-center flex-col gap-2">
              <svg t="1786090250255" class="icon size-36" viewBox="0 0 2138 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="5654" ><path d="M1891.9574 976.454818v-58.514146a47.542744 47.542744 0 0 1 95.085488 0v58.514146a47.542744 47.542744 0 0 1-95.085488 0zM1467.729839 1023.997562H433.979919a53.637968 53.637968 0 0 1-53.637967-53.637968V689.97931a53.637968 53.637968 0 0 1 53.637967-53.637968h63.01486a38.0025 38.0025 0 0 1 8.506494-0.807008c63.390325 0.304761 68.571265-34.974393 70.704593-82.590279l1.618892 0.243809v-104.060095-2.121138c-0.295009-61.876272-35.379116-66.991383-82.59028-69.105207H255.99939a53.637968 53.637968 0 0 1-53.637967-53.637967V268.189838a53.637968 53.637968 0 0 1 53.637967-53.637968h294.860098c20.348294-0.731427 26.914069-12.921874 26.91407-33.70171 0-0.416913 0.017067-0.811884 0.053638-1.182473v-12.885303c-0.694855-20.304409-11.990524-27.79422-32.69478-27.79422-0.243809 0-0.468113 0-0.689979-0.017066H53.637968A53.637968 53.637968 0 0 1 0 85.33313V53.637968A53.637968 53.637968 0 0 1 53.637968 0h1784.681465a53.637968 53.637968 0 0 1 53.637967 53.637968v31.695162a53.637968 53.637968 0 0 1-53.637967 53.637968H1554.311271c-19.321859 0.54857-25.165959 10.266795-25.629196 29.29852V182.36909h0.031695c-0.607084 23.16185 10.193652 32.136457 32.136457 32.136456 0.409599 0 0.794817 0.017067 1.16053 0.0512H2084.566465a53.637968 53.637968 0 0 1 53.637968 53.637968v56.076057a53.637968 53.637968 0 0 1-53.637968 53.637967H1606.498575a37.653853 37.653853 0 0 1-8.706417 0.853332c-52.738313-0.243809-65.187197 24.137085-69.110083 59.747819v349.841453a32.280304 32.280304 0 0 1 1.6579 11.388316c-0.302323 62.936841 34.479461 68.495685 81.576035 70.660708H1801.748091a53.637968 53.637968 0 0 1 53.637968 53.637967v46.323699a53.637968 53.637968 0 0 1-53.637968 53.637968z" fill="#EFEFEF" p-id="5655"></path><path d="M754.625251 455.922724h546.05889L1445.787034 733.864919H609.522358z" fill="#FFFFFF" p-id="5656"></path><path d="M755.807724 524.189228h119.466382l-9.752357 21.942805h-119.466382z" fill="#DDDDDD" p-id="5657"></path><path d="M736.303009 572.951017h168.228171l-9.752358 21.942805h-168.228171z" fill="#DDDDDD" p-id="5658"></path><path d="M1136.149676 663.160326h236.494675l14.628536 24.380894h-236.494675z" fill="#DDDDDD" p-id="5659"></path><path d="M1194.663822 619.274716h163.351992l14.628537 24.380894h-163.351992z" fill="#DDDDDD" p-id="5660"></path><path d="M797.255245 275.504106h436.418008v112.152114H797.255245z" fill="#EFEFEF" p-id="5661"></path><path d="M1220.553894 176.290933h-358.130957a65.153064 65.153064 0 0 0-65.096988 65.165254v213.254807a65.111616 65.111616 0 0 0 65.096988 65.06773h183.319945s75.824581 61.727548 86.118195 69.768368 14.443242 6.197623 14.306708 0-0.070705-69.768367-0.070704-69.768368h74.456813a65.096988 65.096988 0 0 0 65.133559-65.06773V241.443997a65.135997 65.135997 0 0 0-65.133559-65.153064z m-283.698525 209.297787a34.884184 34.884184 0 1 1 34.884184-34.884184 34.864679 34.864679 0 0 1-34.884184 34.884184z m104.650113 0a34.884184 34.884184 0 1 1 34.884184-34.884184 34.864679 34.864679 0 0 1-34.881746 34.884184z m104.650113 0a34.884184 34.884184 0 1 1 34.884183-34.884184 34.864679 34.864679 0 0 1-34.881745 34.884184z" fill="#DDDDDD" p-id="5662"></path><path d="M776.616818 692.566122l15.847581-57.278035-14.160424 0.156038-6.794955 4.486085-5.302844 15.313639 5.441815 6.017205-9.793805 6.229319-6.339032 18.27348 4.098428 6.802269z" fill="#BFBFBF" p-id="5663"></path><path d="M701.687015 692.568561h19.387687l25.317121-57.280474-16.169409 0.163352-7.692172 4.693323c-11.049421 21.313778-11.339554 21.65511-23.615335 45.319206z" fill="#BFBFBF" p-id="5664"></path><path d="M809.12874 635.288087h-12.780465l-15.064954 57.278035h15.391658z" fill="#BFBFBF" p-id="5665"></path><path d="M832.158933 692.566122l6.402423-57.278035-14.140919 0.156038-6.070843 4.486085-2.774545 15.313639 6.434118 6.017205-8.777122 6.229319-3.315802 18.27348 5.222388 6.802269z" fill="#BFBFBF" p-id="5666"></path><path d="M855.203754 635.288087h-12.782903l-5.607605 57.278035h15.403849z" fill="#BFBFBF" p-id="5667"></path><path d="M763.043974 635.288087h-12.782903l-24.522304 57.280474h15.403849z" fill="#BFBFBF" p-id="5668"></path></svg>
              <span class="text-xl   text-gray-500">暂无帖子发布,尝试去发布一篇帖子吧</span>
            </div>
           </div>
        </div>



      </div>
      <div class="right  w-[25%] ">
        <home-feed></home-feed>
      </div>
    </div>
  </div>



</template>

<script lang="ts" setup>
import {onMounted, onUnmounted, ref, watch} from 'vue'
import {useRoute} from 'vue-router'
import homeFeed from './rankboard-home.vue'
import {PostAPI, type PostVO} from '@/services/post'
import postInfo from '@/views/post/pages/postInfo.vue'
import PostItem from '@/views/post/pages/postItem.vue'
import {scrollerFromBottom} from '@/utils/scollerbar'
import {usePostInteractions} from '@/views/post/composables/usePostInteractions'
import router from '@/router'
import type {Symbols} from '../types/statics'
import {useAuthStore} from '@/views/auth/store'
import {log} from '@/utils/log'

const curTab = ref('1')
const orderType = ref('new')

function handleTabClick(tabId: string) {
  if (tabId === '2') {
    const authStore = useAuthStore()
    if (!authStore.token) {
      log.warning('请先登录后查看关注内容')
      authStore.showLoginDialog()
      return
    }
  }
  if (curTab.value === tabId) {
    if (route.query.keyword) {
      router.push({ name: 'homeMain', query: { ...route.query, keyword: undefined } })
    }
  } else {
    curTab.value = tabId
  }
}

function toggleOrder(type: string) {
  if (orderType.value === type) {
    if (route.query.keyword) {
      router.push({ name: 'homeMain', query: { ...route.query, keyword: undefined } })
    }
    return
  }
  orderType.value = type
  if (route.query.keyword) {
    router.push({ name: 'homeMain', query: { ...route.query, keyword: undefined } })
  } else {
    loadData()
  }
}


const cursor = ref<string | number | null>(null);
const loading = ref(false);
const noMore = ref(false);



const items: Symbols[] = [
  {
    name: '推荐',
    id: '1',
  },
  {
    name: '关注',
    id: '2',
  },

]
const curPost = ref<PostVO | null>(null)
const visible = ref(false)
const topicList = ref<PostVO[]>([])

const {
  like,
  collect
} = usePostInteractions(topicList)

const route = useRoute()

let remove: () => void;
onMounted(async () => {
  remove = scrollerFromBottom(async () => {
    await loadData(true)
  }, window) || (() => { })
  await loadData();
})

onUnmounted(() => {
  if (remove) remove()
})

async function loadData(append = false) {
  if (loading.value || (append && noMore.value)) return;
  loading.value = true;
  try {
    const keyword = route.query.keyword as string;
    if (keyword && keyword.trim()) {
      if (append) {
        noMore.value = true;
        return;
      }
      const res = await PostAPI.search(keyword.trim());
      if (res && res.code === 1 && res.data) {
        topicList.value = res.data;
        noMore.value = true;
        cursor.value = null;
      }
    } else {
      const currentCursor = append ? cursor.value : null;
      const res = await PostAPI.getPage({
        cursor: currentCursor,
        self: false,
        orderType: orderType.value
      });
      if (res.code === 1 && res.data) {
        if (append) {
          topicList.value = [...topicList.value, ...res.data];
        } else {
          topicList.value = res.data;
          noMore.value = false;
        }
        if (res.data.length === 0) {
          noMore.value = true;
        } else {
          cursor.value = res.data[res.data.length - 1]?.id || null;
        }
      }
    }
  } catch (e) {
    console.error('加载动态列表异常: ', e);
  } finally {
    loading.value = false;
  }
}

watch(() => curTab.value, () => {
  if (route.query.keyword) {
    router.push({ name: 'homeMain', query: { ...route.query, keyword: undefined } })
  } else {
    loadData();
  }
})

watch(() => route.query.keyword, () => {
  cursor.value = null;
  noMore.value = false;
  loadData();
})
</script>

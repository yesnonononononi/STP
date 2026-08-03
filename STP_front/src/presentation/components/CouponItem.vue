<template>
  <div class="relative flex flex-col items-center bg-white shadow-[0_4px_20px_rgba(0,0,0,0.03)] rounded-xl w-full border border-gray-100/60 overflow-hidden hover:shadow-[0_6px_24px_rgba(0,0,0,0.06)] transition-all duration-200">
    <!-- 优惠券打孔半圆凹槽与垂直虚线分栏 -->
    <div class="absolute -top-2 left-[106px] -translate-x-1/2 w-4 h-4 bg-gray-50 border border-gray-100 rounded-full z-10"></div>
    <div class="absolute -bottom-2 left-[106px] -translate-x-1/2 w-4 h-4 bg-gray-50 border border-gray-100 rounded-full z-10"></div>
    <div class="absolute top-3 bottom-3 left-[106px] border-r border-dashed border-gray-200/80 z-5"></div>

    <div class="flex items-center justify-between w-full p-2.5 gap-2 relative z-1">
      <div class="flex items-center gap-4 flex-1 min-w-0">
        <!-- 优惠金额/折扣展示 -->
        <div class="flex flex-col items-center justify-center shrink-0 w-24 pr-4 py-1.5 bg-gradient-to-r from-red-50/20 via-transparent to-transparent rounded-l-lg">
          <span class="text-red-500 font-bold flex items-baseline gap-0.5 drop-shadow-2xs">
            <template v-if="item.amount">
              <span class="text-sm">¥</span>
              <span class="text-3xl font-black tracking-tight">{{ item.amount }}</span>
            </template>
            <template v-else-if="item.discount">
              <span class="text-2xl font-black tracking-tight">{{ Number(item.discount) * 10 }}</span>
              <span class="text-xs font-bold">折</span>
            </template>
            <template v-else>
              <span class="text-xl font-bold">免费</span>
            </template>
          </span>
          <span class="text-[10px] text-red-400 mt-1 font-medium bg-red-50/40 px-1 py-0.2 rounded border border-red-100/10">
            {{ item.scopeType === 1 || item.scopeType === 0 ? '无门槛' : '限制使用' }}
          </span>
        </div>

        <!-- 优惠券名称及有效期 -->
        <div class="flex flex-col gap-1 justify-start flex-1 min-w-0">
          <span v-if="!props.me && item.name" class="text-[10px] text-blue-600 font-bold tracking-wide bg-blue-50/70 border border-blue-100/40 px-1.5 py-0.5 rounded w-max truncate max-w-full mb-1">
            {{ item.name }}
          </span>
          <div class="text-sm font-semibold text-gray-800 flex gap-1.5 items-center">
            <span class="truncate">{{ item.couponName || item.name }}</span>

            <!-- 通用气泡提示 -->
            <Tooltip :content="item.description" placement="bottom" theme="glass">
              <svg t="1781775325917"
                class="icon size-4.5 cursor-pointer text-gray-400 hover:text-blue-500 transition-colors duration-200"
                viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg">
                <path
                  d="M511.333 127.333c51.868 0 102.15 10.144 149.451 30.15 45.719 19.337 86.792 47.034 122.078 82.321 35.287 35.286 62.983 76.359 82.321 122.078 20.006 47.3 30.15 97.583 30.15 149.451s-10.144 102.15-30.15 149.451c-19.337 45.719-47.034 86.792-82.321 122.078-35.286 35.287-76.359 62.983-122.078 82.321-47.3 20.006-97.583 30.15-149.451 30.15s-102.15-10.144-149.451-30.15c-45.719-19.337-86.792-47.034-122.078-82.321-35.287-35.286-62.983-76.359-82.321-122.078-20.006-47.3-30.15-97.583-30.15-149.451s10.144-102.15 30.15-149.451c19.337-45.719 47.034-86.792 82.321-122.078 35.286-35.287 76.359-62.983 122.078-82.321 47.301-20.006 97.583-30.15 149.451-30.15m0-64c-247.424 0-448 200.576-448 448s200.576 448 448 448 448-200.576 448-448-200.576-448-448-448z"
                  fill="currentColor"></path>
                <path d="M543.334 576h-64.001l-31.246-320.047h128.025z" fill="currentColor"></path>
                <path d="M512.099 702.965m-40 0a40 40 0 1 0 80 0 40 40 0 1 0-80 0Z" fill="currentColor"></path>
              </svg>
            </Tooltip>
          </div>
          <span class="text-gray-400 text-xs truncate">
            {{ validityText }}
          </span>
        </div>
      </div>

      <!-- 右侧交互区域 -->
      <div class="w-20 flex flex-col items-center gap-1.5 justify-center shrink-0" v-if="item.available">
        <!-- 1. 通用领取模式 -->
        <template v-if="props.item.type === 1">
          <button @click="$emit('receive', item.id)"
            class="rounded-full w-full py-1 text-center  text-white hover:shadow-[0px_2px_6px_rgba(59,130,246,0.5)] transition-all duration-200 cursor-pointer text-xs font-medium"
            :class="item.available ? 'bg-linear-to-br from-blue-100 to-blue-500 hover:from-blue-200 hover:to-blue-600' : 'bg-gray-300'">
            {{ '领取' }}
          </button>
        </template>

        <!-- 2. 秒杀倒计时模式 -->
        <template v-else-if="props.item.type === 2">
          <button v-if="state.countdownSeconds > 0" disabled
            class="rounded-full w-full py-1 text-center bg-gray-200 text-gray-500 border border-gray-300 text-xs font-semibold select-none cursor-not-allowed">
            {{ state.countdownText }}
          </button>
          <button v-else @click="$emit('receive', item.id)"
            class="rounded-full w-full py-1 text-center text-white hover:shadow-[0px_2px_6px_rgba(239,68,68,0.5)] transition-all duration-200 cursor-pointer text-xs font-medium"
            :class="item.available ? 'bg-linear-to-br from-red-100 to-red-500 hover:from-red-200 hover:to-red-600' : 'bg-gray-300'">

            {{ '领取' }}
          </button>
        </template>

        <!-- 3. 我的已持有模式 -->
        <template v-if="props.me">
          <button v-if="state.isExpired || props.item.status === CouponStatus.EXPIRED" disabled
            class="rounded-full w-full py-1 text-center bg-gray-200 text-gray-400 border border-gray-300 text-xs font-medium cursor-not-allowed select-none">
            已过期
          </button>
          <button v-else-if="props.item.status === CouponStatus.USED" disabled
            class="rounded-full w-full py-1 text-center bg-gray-200 text-gray-400 border border-gray-300 text-xs font-medium cursor-not-allowed select-none">
            已使用
          </button>
          <button v-else @click="$emit('use', item)"
            class="rounded-full w-full py-1 text-center bg-linear-to-br from-emerald-100 to-emerald-500 hover:from-emerald-200 hover:to-emerald-600 text-white hover:shadow-[0px_2px_6px_rgba(16,185,129,0.5)] transition-all duration-200 cursor-pointer text-xs font-medium">
            去使用
          </button>
        </template>

        <!-- 下拉详情触发箭头 -->
        <span class="cursor-pointer text-gray-400 hover:text-gray-600 transition-colors duration-200"
          @click="state.showMore = !state.showMore">
          <svg t="1781791086826" class="icon size-5 transition-transform duration-300"
            :class="state.showMore ? 'rotate-180' : ''" viewBox="0 0 1024 1024" version="1.1"
            xmlns="http://www.w3.org/2000/svg">
            <path d="M185.884 327.55 146.3 367.133 512.021 732.779 877.7 367.133 838.117 327.55 511.997 653.676Z">
            </path>
          </svg>
        </span>
      </div>
      <svg t="1782094615884" class="icon size-12" viewBox="0 0 1024 1024" version="1.1"
        v-if="item && item.isAvailable === false" xmlns="http://www.w3.org/2000/svg" p-id="6198">
        <path
          d="M569.916378 188.032655l21.999141 2.319909 14.719425 17.119332 7.999688-20.079216 20.159212-7.999688-17.119331-14.719425-2.079919-22.079137-18.719269 11.039569-21.279168-5.679778 5.679778 21.519159z m261.669779 238.870669v26.798953l19.359244-11.919534 20.639193 8.559665-3.359868-26.318971 12.7995-21.51916-21.439163-4.479825-12.719503-21.99914-9.919612 23.999062-20.719191 7.999688zM725.430304 273.629311l19.839225 12.55951 7.039725 24.959025 15.999375-17.839303 22.239131 0.799968-10.159603-23.519081 6.719737-24.559041-21.99914 3.279872-18.079294-15.999375-3.439866 25.599z m-279.989063-88.636537v22.479122l23.27909-9.519629 24.719035 7.999688-3.919847-22.239131 15.439397-17.759307-25.35901-4.799812-15.199406-18.559275-11.999531 19.679231-24.799031 6.319753z m-18.319285 650.934573l-21.99914-2.239913-14.719425-17.039334-7.999688 20.159212-20.159212 7.999688 17.039334 14.719425 2.239912 21.99914 18.559275-11.039568 21.51916 5.599781-5.599781-21.51916z m-129.594937-72.557166l-19.839225-12.479513-6.959729-24.959025-15.999375 17.9193-22.239131-0.639975 10.159603 23.519082-6.799734 24.55904 21.999141-3.359868 18.079293 15.999375 3.439866-25.599zM191.611156 623.055662v-26.798953L172.411906 608.016249l-20.799188-7.999687 3.199875 26.558962L142.173087 648.014687l21.439163 4.479825 12.639506 21.75915 9.999609-23.999063 20.719191-7.999687z"
          fill="#3CB1EC" p-id="6199"></path>
        <path
          d="M873.824507 149.794149a511.980001 511.980001 0 1 0 0 724.371704 511.980001 511.980001 0 0 0 0-724.371704z m-700.372642 700.372641a478.221319 478.221319 0 1 1 676.293583 0 478.701301 478.701301 0 0 1-676.293583 0z"
          fill="#3CB1EC" p-id="6200"></path>
        <path
          d="M807.587094 202.912074a427.903285 427.903285 0 1 0 0 605.096363 428.463263 428.463263 0 0 0 0-605.256357zM216.250193 794.408968a408.944026 408.944026 0 1 1 368.945589 111.995626l9.759618-21.119175 22.879107-7.999688-18.319285-15.999375-1.439944-23.439084-21.199171 11.599546-23.999063-6.159759 5.199797 22.959103-13.199485 19.599235 24.479044 2.639897 15.439397 18.15929a408.704035 408.704035 0 0 1-368.6256-111.995625z"
          fill="#3CB1EC" p-id="6201"></path>
        <path
          d="M688.471747 338.026796a255.99 255.99 0 0 1 71.997188 154.553963l16.959337-1.679935a273.829304 273.829304 0 0 0-76.797-165.11355 263.029725 263.029725 0 0 0-337.986797-32.718722l9.599625 14.319441a246.310379 246.310379 0 0 1 316.227647 30.638803z m-353.826178 347.986407a255.99 255.99 0 0 1-71.997188-154.553963l-16.959338 1.679934a273.9093 273.9093 0 0 0 76.717004 165.113551 263.029725 263.029725 0 0 0 337.986797 32.718721l-9.599625-14.31944a246.150385 246.150385 0 0 1-316.14765-30.638803z"
          fill="#3CB1EC" p-id="6202"></path>
        <path
          d="M388.323472 400.824343l-12.719503 26.398969-7.999688 17.039334a148.23421 148.23421 0 0 0-5.1198 14.639428l-20.159212-9.599625 2.639897-5.439787a56.557791 56.557791 0 0 1 4.799812-7.999688l-90.636459-43.438303-27.038944 56.557791a10.959572 10.959572 0 0 0 4.159837 15.039412 372.545447 372.545447 0 0 0 57.757744 30.47881q33.598688 15.119409 40.9584 13.839459c5.039803-1.359947 10.879575-7.439709 17.359322-18.319284a9.519628 9.519628 0 0 1 2.5599-3.519863 57.837741 57.837741 0 0 0 15.3594 19.679231c-13.599469 13.839459-23.999063 20.639194-30.7188 20.559197s-21.599156-5.279794-45.598219-15.999375q-19.439241-9.359634-42.23835-21.199172-24.479044-12.639506-34.958634-18.559275c-9.279638-5.759775-10.879575-13.759463-4.639819-23.999062l36.318581-75.917035a45.358228 45.358228 0 0 0 3.279872-8.95965 46.158197 46.158197 0 0 0 2.879888-7.999687c13.999453 6.639741 20.4792 11.039569 19.43924 13.119487a21.919144 21.919144 0 0 1-2.959884 4.239835 22.319128 22.319128 0 0 0-5.279794 7.039725l91.436428 43.678293 21.119175-44.158275-85.99664-41.118393a315.987657 315.987657 0 0 0-30.238819-12.55951l-2.319909-1.039959 7.999687-17.039335a323.187375 323.187375 0 0 0 30.238819 15.999375l71.997187 34.398657 17.039335 7.999687c7.519706 2.959884 13.439475 5.1198 17.839303 6.639741a55.997813 55.997813 0 0 0-7.999688 13.199484c-3.759853 6.959728-6.559744 12.239522-8.559665 16.319363zM538.957588 468.101715L564.476591 480.021249a172.713253 172.713253 0 0 0 22.479122 10.719581 46.238194 46.238194 0 0 0 11.199562 2.5599l-7.999687 17.039335a186.232725 186.232725 0 0 0-30.958791-16.719347l-27.118941-12.959494a78.796922 78.796922 0 0 0-5.999765 10.479591c-5.199797 9.519628-9.199641 16.479356-11.919535 20.959181l37.998516 18.079294c22.479122 11.359556 35.438616 16.63935 39.038475 15.999375l-9.039647 18.639272a61.837584 61.837584 0 0 0-10.47959-5.919769c-3.359869-2.239913-12.319519-6.799734-26.718957-13.759462l-31.038787-14.799422q-7.439709 53.677903 43.118315 91.036444a41.758369 41.758369 0 0 0-19.839225 7.999687 7.999688 7.999688 0 0 0-3.359868 3.119878q-44.79825-49.91805-33.358697-101.596031-37.678528 41.038397-109.595719 36.158587a45.038241 45.038241 0 0 0-8.479669-23.999062q68.317331 18.079294 105.195891-25.279013l-33.358697-15.999375q-29.758838-13.199484-38.638491-16.559353l8.479669-17.759306c0.799969 2.239913 8.95965 7.119722 24.479044 14.479434a77.516972 77.516972 0 0 0 11.999531 4.799813l37.198547 17.759306q3.039881-4.239834 7.999688-13.199484c3.199875-6.719738 6.479747-12.7995 9.679621-18.239288l-42.638334-20.319206-23.999063 18.879262a72.477169 72.477169 0 0 0-15.999375-15.119409 113.995547 113.995547 0 0 0 56.717785-49.038085 161.673685 161.673685 0 0 1 20.319206 17.359322 7.279716 7.279716 0 0 1-4.239834 2.719894 15.999375 15.999375 0 0 0-7.199719 3.199875 61.117613 61.117613 0 0 0-13.999453 11.439553l37.198547 17.759307c0.719972-1.599938 1.679934-3.599859 2.959884-6.239757a229.351041 229.351041 0 0 0 14.719425-34.79864A141.434475 141.434475 0 0 1 560.956728 432.023124c0 0.559978-1.119956 1.039959-2.639897 1.599938a27.678919 27.678919 0 0 0-6.719737 8.879653 586.697082 586.697082 0 0 0-12.639506 25.599zM673.432335 662.654115l-19.19925 2.319909a151.994063 151.994063 0 0 0-7.999688-27.518925 142.954416 142.954416 0 0 1-61.197609 14.559432c-0.559978-1.599938-1.119956-3.119878-1.599938-4.639819a54.237881 54.237881 0 0 0-7.839693-15.3594 106.795828 106.795828 0 0 0 65.917425-10.399594 342.146635 342.146635 0 0 0-15.999375-36.158587l16.479356-4.559822c0.799969 2.319909 2.239913 6.479747 4.479825 12.559509 1.839928 4.079841 4.719816 10.559588 8.799656 19.439241a187.272685 187.272685 0 0 0 22.959103-23.999063c9.679622 11.599547 13.999453 17.759306 13.039491 18.559275a5.439788 5.439788 0 0 1-3.439866 1.199953 12.959494 12.959494 0 0 0-5.35979 3.199875 48.798094 48.798094 0 0 0-5.679778 3.919847 103.995938 103.995938 0 0 1-15.199407 11.759541c4.879809 15.039413 8.799656 26.798953 11.839538 35.118628z m-11.039569-119.995313l10.319597 15.999375a415.183782 415.183782 0 0 1-54.557869 22.559119 40.558416 40.558416 0 0 0-8.719659-15.999375 242.870513 242.870513 0 0 0 52.957931-22.959103z m9.199641-19.759228l34.878637 16.63935 15.999375 7.999688c6.479747 2.399906 11.359556 4.079841 14.719425 5.039803l-7.999687 15.999375c-7.999688-4.479825-17.599313-9.679622-28.718878-15.999375l-34.878638-16.719347a177.593063 177.593063 0 0 0-27.118941-11.039569l7.999688-15.999375c10.719581 6.559744 18.959259 11.199563 25.119019 14.07945z m89.276512 52.157963l37.198547 17.759306a172.633257 172.633257 0 0 0 20.879185 7.999688l-7.999688 15.999375c-7.519706-4.239834-13.519472-7.999688-17.839303-10.479591l-3.119878-1.439944q-28.718878 51.997969-60.15765 71.997188a97.836178 97.836178 0 0 0 30.798797 48.958087 35.198625 35.198625 0 0 0-23.279091 6.959729 184.472794 184.472794 0 0 1-23.999063-48.718097 243.030507 243.030507 0 0 1-61.277606 8.719659c0-1.999922-0.559978-2.799891-0.799969-2.239913a38.318503 38.318503 0 0 0-9.19964-20.559196 111.995625 111.995625 0 0 0 69.037303-3.199875q-2.479903-14.559431 5.1198-58.477716l17.359322 3.519862a205.911957 205.911957 0 0 0-7.999688 47.998125 184.632788 184.632788 0 0 0 47.998125-62.637553l-25.519003-12.239522c-12.399516 8.639663-20.079216 14.159447-23.119097 16.559353A74.2371 74.2371 0 0 0 711.990829 586.737081a214.551619 214.551619 0 0 0 64.637475-55.997813c12.239522 10.319597 18.079294 15.999375 17.599312 16.959338s-1.119956 1.039959-2.639897 1.599937a17.039334 17.039334 0 0 0-6.479747 3.599859 22.959103 22.959103 0 0 1-4.559821 3.519863c-9.359634 9.279638-16.079372 15.279403-19.599235 18.639272z m-45.198234 28.878872l-19.039256 4.07984a212.391703 212.391703 0 0 0-15.439397-45.438225l17.679309-2.5599c6.639741 15.999375 12.719503 30.958791 16.879341 43.918285z m-4.079841-73.277138l-19.679231-4.639819a128.634975 128.634975 0 0 0 2.5599-27.358931l18.719269 5.359791c-1.039959 12.559509-1.599938 21.519159-1.599938 26.638959z"
          fill="#3CB1EC" p-id="6203"></path>
      </svg>
    </div>

    <!-- 详情展开容器（不占空间） -->
    <div class="grid transition-[grid-template-rows] duration-300 ease-in-out w-full border-t border-gray-50/50"
      :class="state.showMore ? 'grid-rows-[1fr]' : 'grid-rows-[0fr]'">
      <div class="overflow-hidden">
        <div class="flex flex-col text-xs text-gray-500 gap-1.5 w-full p-3 bg-gray-50/70">
          <div class="flex gap-2 items-center justify-start">
            <span class="font-medium text-gray-600 shrink-0">使用范围:</span>
            <span>
              {{
                item.scopeType === 1 || item.scopeType === 0 ? '全场通用' :
                  (item.scopeType === 2 ? `仅限特定类别使用(${item.scopeDescription || '未知'})` :
                    (item.scopeType === 3 ? `仅限特定套餐使用(${item.scopeDescription || '未知'})` : ''))
              }}
            </span>
          </div>
          <div class="flex gap-2 items-start justify-start">
            <span class="font-medium text-gray-600 shrink-0">计算规则:</span>
            <span class="leading-relaxed">
              {{ item.description }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import Tooltip from '@/presentation/components/Tooltip.vue';
import { TimeUtils } from '@/utils/time';
import { computed, ref, onMounted, onUnmounted, reactive } from 'vue';
import { CouponStatus } from '@/services/coupon/type';
import type { CouponActivityVO, CouponVO } from '@/services/coupon/coupon';
const state = reactive({
  isExpired: computed(() => {
    return props.item.status === CouponStatus.EXPIRED;
  }),
  isUsed: computed(() => {
    return props.item.status === CouponStatus.USED;
  }),
  isAvaliable: computed(() => {
    return props.item.status === CouponStatus.UNUSED;
  }),
  relatedOrderId: computed(() => {
    return props.item.relatedOrderId;
  }),
  showMore: ref(false),
  countdownSeconds: ref(0),
  countdownText: ref('00:00'),

});
interface Props {
  item: any;
  me: boolean;
  relatedOrderId?: string
}

const props = defineProps<Props>();
defineEmits<{
  (e: 'receive', id: string | number): void;
  (e: 'use', item: any): void;
}>();


let timer: ReturnType<typeof setInterval> | null = null;

// 格式化有效期文本
const validityText = computed(() => {
  if (state.isUsed) {
    return state.relatedOrderId ? `订单ID: ${state.relatedOrderId}` : '已使用';
  }
  const item = props.item;
  if (props.me) {
    return item.endTime ? `${TimeUtils.timestampToDate(item.endTime)} 后过期` : '';
  }
  if (item.timeType === 1) {
    return `${TimeUtils.timestampToDate(item.startTime || item.activityStartTime)} - ${TimeUtils.timestampToDate(item.endTime || item.activityEndTime)}`;
  } else if (item.timeType === 2) {
    return `${item.validDays || 0}天内有效`;
  } else if (item.timeType === 3) {
    return `${item.validHours || 0}小时内有效`;
  }
  return '';
});


// 计算倒数格式
function formatCountdown(seconds: number): string {
  if (seconds <= 0) return '00:00';
  const h = Math.floor(seconds / 3600);
  const m = Math.floor((seconds % 3600) / 60);
  const s = seconds % 60;
  const pad = (n: number) => String(n).padStart(2, '0');

  if (h > 0) {
    return `${pad(h)}:${pad(m)}:${pad(s)}`;
  }
  return `${pad(m)}:${pad(s)}`;
}

// 更新倒数
function updateCountdown() {
  if (props.item.type !== 2) return;
  const diff = TimeUtils.calculateTimeByNow(props.item.activityStartTime);
  state.countdownSeconds = Math.max(0, diff);
  state.countdownText = formatCountdown(state.countdownSeconds);

  if (state.countdownSeconds <= 0 && timer) {
    clearInterval(timer);
    timer = null;
  }
}

onMounted(() => {
  if (props.item.type === 2) {
    updateCountdown();
    if (state.countdownSeconds > 0) {
      timer = setInterval(updateCountdown, 1000);
    }
  }

});

onUnmounted(() => {
  if (timer) {
    clearInterval(timer);
  }
});
</script>

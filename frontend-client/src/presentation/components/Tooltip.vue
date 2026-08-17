<template>
  <div class="relative inline-flex items-center group z-100 select-none">
    <!-- 触发器插槽 -->
    <div class="inline-flex items-center cursor-pointer">
      <slot></slot>
    </div>

    <!-- 提示框内容 -->
    <div
      class="absolute z-50 pointer-events-none opacity-0 group-hover:opacity-100 group-hover:pointer-events-auto transition-all duration-300 transform scale-95 group-hover:scale-100"
      :class="[
        positionClasses[placement],
        width
      ]">
      <div class="px-3 py-2 text-xs rounded-lg shadow-xl backdrop-blur-md border relative" :class="themeClasses[theme]">
        <slot name="content">{{ content }}</slot>

        <!-- 指示箭头 -->
        <div class="absolute w-2 h-2 rotate-45 border" :class="[
          arrowClasses[placement],
          arrowThemeClasses[theme]
        ]"></div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">

interface Props {
  content?: string;
  placement?: 'top' | 'bottom' | 'left' | 'right';
  theme?: 'dark' | 'light' | 'glass';
  width?: string;
}

const props = withDefaults(defineProps<Props>(), {
  content: '',
  placement: 'top',
  theme: 'glass',
  width: 'max-w-xs w-max'
});

// 位置映射类
const positionClasses = {
  top: 'bottom-full left-1/2 -translate-x-1/2 mb-2 pb-1.5 origin-bottom',
  bottom: 'top-full left-1/2 -translate-x-1/2 mt-2 pt-1.5 origin-top',
  left: 'right-full top-1/2 -translate-y-1/2 mr-2 pr-1.5 origin-right',
  right: 'left-full top-1/2 -translate-y-1/2 ml-2 pl-1.5 origin-left'
};

// 主题映射类
const themeClasses = {
  dark: 'bg-slate-900/95 text-slate-100 border-slate-800',
  light: 'bg-white/95 text-slate-800 border-slate-200',
  glass: 'bg-white/75 border-white/40 text-slate-700 backdrop-blur-md'
};

// 箭头定位映射类
const arrowClasses = {
  top: 'bottom-0 left-1/2 -translate-x-1/2 translate-y-1/2 border-r border-b border-t-0 border-l-0',
  bottom: 'top-0 left-1/2 -translate-x-1/2 -translate-y-1/2 border-t border-l border-r-0 border-b-0',
  left: 'right-0 top-1/2 -translate-y-1/2 translate-x-1/2 border-t border-r border-b-0 border-l-0',
  right: 'left-0 top-1/2 -translate-y-1/2 -translate-x-1/2 border-l border-b border-t-0 border-r-0'
};

// 箭头主题映射类
const arrowThemeClasses = {
  dark: 'bg-slate-900 border-slate-800',
  light: 'bg-white border-slate-200',
  glass: 'bg-white/75 border-white/40'
};
</script>

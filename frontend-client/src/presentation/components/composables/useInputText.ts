import {ref, type Ref} from 'vue'

/**
 * 封装输入框组件提交的中继逻辑
 * @param model 绑定编辑器内容的 Ref 状态
 * @param publishCallback 发布评论的上层回调函数
 */
export function useInputText(model: Ref<string>, publishCallback: (content: any) => void) {
  const inputRef = ref<any>()

  /**
   * 调用发布接口发布评论
   * @param e 可选事件对象
   */
  async function publish(e?: any) {
    publishCallback(e)
  }

  return {
    inputRef,
    publish
  }
}

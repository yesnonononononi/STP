// 节流函数
function throttle<T extends (...args: any[]) => any>(
  fn: T,
  delay = 150,
): (...args: Parameters<T>) => void {
  let lastTime = 0
  let timer: any = null
  return function (this: any, ...args: Parameters<T>) {
    const now = Date.now()
    if (now - lastTime >= delay) {
      if (timer) {
        clearTimeout(timer)
        timer = null
      }
      lastTime = now
      fn.apply(this, args)
    } else {
      if (timer) clearTimeout(timer)
      timer = setTimeout(() => {
        lastTime = Date.now()
        fn.apply(this, args)
      }, delay)
    }
  }
}

//滚动条快触底后触发
export function scrollerFromBottom(
  this: any,
  operation: () => void,
  container: HTMLElement | Window | null = window,
) {
  const target = container || window
  const throttledExecutor = throttle(() => {
    executor(target, operation)
  }, 200)

  target.addEventListener('scroll', throttledExecutor)
  return () => {
    target.removeEventListener('scroll', throttledExecutor)
  }
}

function executor(this: any, container: HTMLElement | Window, operation: () => void) {
  //滚动条总高度
  const pageHeight =
    container instanceof Window ? document.documentElement.scrollHeight : container.scrollHeight

  //滚动条距离顶部的高度
  const curHeightFromTop = container instanceof Window ? container.scrollY : container.scrollTop

  //视口高度
  const viewHeight = container instanceof Window ? window.innerHeight : container.clientHeight

  //滚动条距离底部的高度
  const heightFromBottom = pageHeight - (curHeightFromTop + viewHeight)

  if (heightFromBottom < viewHeight * 0.3) {
    operation.call(this)
  }
}

//滚动条快触顶后触发
export function scrollerFromTop(
  this: any,
  operation: () => void,
  container: HTMLElement | Window | null = window,
) {
  const target = container || window
  const throttledExecutor = throttle(() => {
    executorTop(target, operation)
  }, 200)

  target.addEventListener('scroll', throttledExecutor)
  return () => {
    target.removeEventListener('scroll', throttledExecutor)
  }
}

function executorTop(this: any, container: HTMLElement | Window, operation: () => void) {
  //滚动条距离顶部的高度
  const curHeightFromTop = container instanceof Window ? container.scrollY : container.scrollTop
  //视口高度
  const viewHeight = container instanceof Window ? window.innerHeight : container.clientHeight

  // 滚动条距离顶部的高度小于视口高度的 20% 时触发
  if (curHeightFromTop < viewHeight * 0.2) {
    operation.call(this)
  }
}


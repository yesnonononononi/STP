/**
 * 获取输入框或文本域中光标相对于文档 body 的绝对像素坐标
 * @param element HTMLInputElement | HTMLTextAreaElement
 * @param selectionIndex 光标索引位置
 */
export function getCursorXY(element: HTMLTextAreaElement | HTMLInputElement, selectionIndex: number) {
  const div = document.createElement('div')
  const copyStyle = window.getComputedStyle(element)
  
  const stylesToCopy = [
    'direction', 'boxSizing', 'width', 'height', 'overflowX', 'overflowY',
    'borderTopWidth', 'borderRightWidth', 'borderBottomWidth', 'borderLeftWidth', 'borderStyle',
    'paddingTop', 'paddingRight', 'paddingBottom', 'paddingLeft',
    'fontStyle', 'fontVariant', 'fontWeight', 'fontStretch', 'fontSize', 'fontSizeAdjust', 'lineHeight', 'fontFamily',
    'textAlign', 'textTransform', 'textIndent', 'textDecoration', 'letterSpacing', 'wordSpacing', 'tabSize'
  ]
  
  stylesToCopy.forEach(style => {
    div.style[style as any] = (copyStyle[style as any] || '') as string
  })
  
  div.style.position = 'absolute'
  div.style.visibility = 'hidden'
  div.style.whiteSpace = 'pre-wrap'
  div.style.wordBreak = 'break-word'
  div.style.width = element.clientWidth + 'px'
  
  document.body.appendChild(div)
  
  div.textContent = element.value.substring(0, selectionIndex)
  
  const span = document.createElement('span')
  span.textContent = element.value.substring(selectionIndex) || '.'
  div.appendChild(span)
  
  const rect = span.getBoundingClientRect()
  const divRect = div.getBoundingClientRect()
  const elementRect = element.getBoundingClientRect()
  
  const relativeLeft = rect.left - divRect.left
  const relativeTop = rect.top - divRect.top
  
  document.body.removeChild(div)
  
  const x = elementRect.left + relativeLeft - element.scrollLeft + window.scrollX
  const y = elementRect.top + relativeTop + 24 - element.scrollTop + window.scrollY
  
  return { x, y }
}

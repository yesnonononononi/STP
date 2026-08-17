import router from '@/router'

export function newPageWithId(id: number | undefined, pageName: string) {
  const route = router.resolve({ name: pageName, params: { id: id } })
  window.open(route.href, '_blank')
}

export function formatNum(num: number): string {
  if (num === null || num === undefined || isNaN(num)) {
    return '0'
  }
  if (num < 1000) return num.toString()
  // 亿级
  const yi = parseFloat((num / 100000000).toFixed(1))
  if (yi >= 1) {
    return `${yi}亿`
  }

  // 万级 (w)
  const wan = parseFloat((num / 10000).toFixed(1))
  if (wan >= 1) {
    return `${wan}w`
  }

  // 千级 (k)
  const qian = parseFloat((num / 1000).toFixed(1))
  if (qian >= 1) {
    return `${qian}k`
  }

  return num.toString()
}

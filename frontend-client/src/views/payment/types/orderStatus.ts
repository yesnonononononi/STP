export enum OrderStatus {
  PENDING = 0,
  PAID = 1,
  COMPLETED = 2,
  CANCELLED = 3,
}

export namespace OrderStatus {
  export interface Meta {
    code: OrderStatus
    description: string
  }

  const list: Meta[] = [
    { code: OrderStatus.PENDING, description: '待支付' },
    { code: OrderStatus.PAID, description: '已支付' },
    { code: OrderStatus.COMPLETED, description: '已完成' },
    { code: OrderStatus.CANCELLED, description: '已取消' },
  ]

  export function getList(): Meta[] {
    return list
  }

  export function getDescription(code: OrderStatus | number | string): string {
    const found = list.find((item) => item.code === fromCode(code))
    return found ? found.description : '未知状态'
  }

  export function fromCode(code: number | string | undefined | null): OrderStatus | undefined {
    if (code === undefined || code === null) return undefined
    if (typeof code === 'string') {
      const upper = code.toUpperCase()
      if (upper === 'PENDING') return OrderStatus.PENDING
      if (upper === 'PAID') return OrderStatus.PAID
      if (upper === 'COMPLETED') return OrderStatus.COMPLETED
      if (upper === 'CANCELLED') return OrderStatus.CANCELLED
    }
    const num = typeof code === 'number' ? code : parseInt(String(code), 10)
    if (isNaN(num)) return undefined
    const found = list.find((item) => item.code === num)
    return found ? found.code : undefined
  }
}

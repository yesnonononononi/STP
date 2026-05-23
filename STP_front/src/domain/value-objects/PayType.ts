export enum PayType {
  WX_PAY = 1,
  ALI_PAY = 2,
}

export namespace PayType {
  export interface Meta {
    code: PayType
    description: string
  }

  const list: Meta[] = [
    { code: PayType.WX_PAY, description: '微信支付' },
    { code: PayType.ALI_PAY, description: '支付宝' },
  ]

  export function getList(): Meta[] {
    return list
  }

  export function getDescription(code: PayType | number | string): string {
    const found = list.find((item) => item.code === fromCode(code))
    return found ? found.description : '未知支付'
  }

  export function fromCode(code: number | string): PayType | undefined {
    const num = typeof code === 'string' ? parseInt(code, 10) : code
    const found = list.find((item) => item.code === num)
    return found ? found.code : undefined
  }

  /**
   * 根据类型名称小写匹配对应的 PayType 枚举值 (code)
   */
  export function fromType(type: string): PayType | undefined {
    if (!type) return undefined
    const normalizedType = type.toLowerCase().replace(/_/g, '')
    for (const item of list) {
      const keyName = PayType[item.code]
      const normalizedKey = keyName.toLowerCase().replace(/_/g, '')
      if (normalizedKey === normalizedType) {
        return item.code
      }
    }
    return undefined
  }
}

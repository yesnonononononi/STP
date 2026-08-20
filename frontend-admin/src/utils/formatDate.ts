import dayjs from 'dayjs'

export const formatDate = (val?: string | number | Date | null): string => {
  if (!val) return '-'
  const d = dayjs(val)
  return d.isValid() ? d.format('YYYY-MM-DD HH:mm:ss') : String(val)
}

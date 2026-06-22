/**
 * 前端 XSS 安全过滤工具类
 */
export class XssUtils {
  /**
   * 过滤文本内容中的任意 HTML 标签、脚本等元素，仅保留纯文本字符，防范前端 XSS 注入风险
   * @param text 原始输入文本
   */
  static filter(text: string): string {
    if (!text) return '';
    try {
      const parser = new DOMParser();
      const doc = parser.parseFromString(text, 'text/html');
      return doc.body.textContent || '';
    } catch (e) {
      // 降级使用正则过滤所有的 HTML 标签
      return text.replace(/<[^>]*>/g, '');
    }
  }
}

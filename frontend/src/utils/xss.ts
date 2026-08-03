/**
 * 前端 XSS 安全过滤工具类
 */
export class XssUtils {
  /**
   * 过滤文本内容中的危险 HTML 标签和属性，保留安全的 a、img 等富文本元素，防范前端 XSS 注入风险
   * @param text 原始输入文本
   */
  static filter(text: string): string {
    if (!text) return '';
    try {
      const parser = new DOMParser();
      const doc = parser.parseFromString(text, 'text/html');
      
      const allowedTags = ['img', 'a', '#text'];
      const allowedAttrs: Record<string, string[]> = {
        img: ['src', 'alt', 'title', 'class', 'style', 'width', 'height'],
        a: ['href', 'target', 'class', 'style']
      };

      const sanitize = (node: Node) => {
        const childNodes = Array.from(node.childNodes);
        for (const child of childNodes) {
          const nodeName = child.nodeName.toLowerCase();
          
          if (!allowedTags.includes(nodeName)) {
            // 如果是危险标签直接移除
            if (['script', 'style', 'iframe', 'object', 'embed', 'form'].includes(nodeName)) {
              child.remove();
            } else {
              // 否则如果是普通包装标签，把它的子节点提上来然后把标签本身去掉
              while (child.firstChild) {
                child.parentNode?.insertBefore(child.firstChild, child);
              }
              child.remove();
            }
          } else if (child instanceof Element) {
            // 已经是白名单标签（img / a），净化属性
            const attrs = Array.from(child.attributes);
            const tag = child.tagName.toLowerCase();
            
            for (const attr of attrs) {
              const name = attr.name.toLowerCase();
              if (!allowedAttrs[tag]?.includes(name)) {
                child.removeAttribute(attr.name);
              } else if (name === 'href' || name === 'src') {
                const val = attr.value.trim().toLowerCase();
                if (val.startsWith('javascript:')) {
                  child.removeAttribute(attr.name);
                }
              }
            }
            sanitize(child);
          }
        }
      };

      sanitize(doc.body);
      return doc.body.innerHTML;
    } catch (e) {
      // 降级使用正则过滤
      return text.replace(/<(script|iframe|style|object|embed)[^>]*>[\s\S]*?<\/\1>/gi, '');
    }
  }
}

const PLACEHOLDER = 'data:image/svg+xml,' + encodeURIComponent(
  '<svg xmlns="http://www.w3.org/2000/svg" width="400" height="300"><rect fill="#e2e8f0" width="400" height="300"/><text x="50%" y="50%" fill="#94a3b8" font-size="18" text-anchor="middle" dy=".3em">暂无图片</text></svg>'
)

/** 统一处理上传路径与外链 */
export function resolveImageUrl(url) {
  if (!url) return PLACEHOLDER
  if (url.startsWith('http') || url.startsWith('data:')) return url
  if (url.startsWith('/')) return url
  return '/' + url
}

export { PLACEHOLDER }

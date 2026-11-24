(() => {
  const escapeHtml = (value) => value
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#039;');

  const renderInline = (value) => {
    let html = escapeHtml(value);
    const links = [];

    html = html.replace(/\[([^\]]+)\]\((https?:\/\/[^\s)<>"]+)\)/gi, (_, label, url) => {
      const token = '@@MDLINK' + links.length + '@@';
      links.push('<a href="' + url + '" target="_blank" rel="noopener noreferrer">' + label + '</a>');
      return token;
    });

    html = html.replace(/https?:\/\/[^\s<>"]+/gi, (url) =>
      '<a href="' + url + '" target="_blank" rel="noopener noreferrer">' + url + '</a>'
    );
    html = html.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>');
    html = html.replace(/__([^_]+?)__/g, '<strong>$1</strong>');
    html = html.replace(/\*([^*]+?)\*/g, '<em>$1</em>');
    html = html.replace(/_([^_]+?)_/g, '<em>$1</em>');

    links.forEach((link, index) => {
      html = html.replace('@@MDLINK' + index + '@@', link);
    });
    return html;
  };

  const renderMarkdown = (value) => {
    if (!value.trim()) return '';

    return value.split('\n').map((line) => {
      const trimmed = line.trim();
      if (!trimmed) return '<br>';
      if (/^[-*]\s+/.test(trimmed)) {
        return '<div class="markdown-list-item">* ' + renderInline(trimmed.replace(/^[-*]\s+/, '')) + '</div>';
      }
      return '<div>' + renderInline(line) + '</div>';
    }).join('');
  };

  document.querySelectorAll('.markdown-source').forEach((source) => {
    const preview = source.nextElementSibling;
    if (!preview || !preview.classList.contains('markdown-preview')) return;
    preview.innerHTML = renderMarkdown(source.value);
  });
})();

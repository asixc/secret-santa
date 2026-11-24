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
    const nextElement = source.nextElementSibling;
    const preview = nextElement && nextElement.classList.contains('markdown-preview')
      ? nextElement
      : null;
    const targetPreview = source.dataset.previewTarget
      ? document.getElementById(source.dataset.previewTarget)
      : null;
    const emptyState = source.dataset.emptyTarget
      ? document.querySelector('[data-preference-empty="' + source.dataset.emptyTarget + '"]')
      : null;
    const updatePreview = () => {
      const rendered = renderMarkdown(source.value);
      if (preview) preview.innerHTML = rendered;
      if (targetPreview) targetPreview.innerHTML = rendered;
      if (emptyState) emptyState.hidden = Boolean(source.value.trim());
    };
    source.addEventListener('input', updatePreview);
    updatePreview();
  });

  document.querySelectorAll('[data-preference-modal]').forEach((modal) => {
    const source = document.getElementById(modal.dataset.preferenceSource);
    const openButtons = document.querySelectorAll('[data-preference-open="' + modal.id + '"]');
    const fields = modal.querySelectorAll('input:not([type="hidden"]), textarea');
    let originalValues = new Map();
    let lastTrigger = null;
    if (!source || !openButtons.length) return;

    const closeModal = (restore) => {
      if (restore) {
        fields.forEach((field) => {
          field.value = originalValues.get(field) || '';
        });
        source.dispatchEvent(new Event('input'));
      }
      modal.hidden = true;
      document.body.classList.remove('preference-modal-open');
      if (lastTrigger) lastTrigger.focus();
    };

    openButtons.forEach((openButton) => openButton.addEventListener('click', () => {
      lastTrigger = openButton;
      originalValues = new Map([...fields].map((field) => [field, field.value]));
      modal.hidden = false;
      document.body.classList.add('preference-modal-open');
      fields[0].focus();
    }));

    modal.querySelectorAll('[data-preference-cancel], [data-preference-close]')
      .forEach((button) => button.addEventListener('click', () => closeModal(true)));
    modal.querySelector('[data-preference-apply]')
      .addEventListener('click', () => closeModal(false));
    modal.addEventListener('click', (event) => {
      if (event.target === modal) closeModal(true);
    });
    document.addEventListener('keydown', (event) => {
      if (event.key === 'Escape' && !modal.hidden) closeModal(true);
    });
  });
})();

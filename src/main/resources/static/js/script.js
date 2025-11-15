// script.js — single reel, auto-load from server data
(() => {
  const namesPool = [];
  let loaded = false;
  let assignedFromServer = null;
  const itemsPerReel = 60;

  const spinBtn = document.getElementById('spinBtn');
  const resultEl = document.getElementById('result');
  const resultNameEl = document.getElementById('resultName');
  const confettiCanvas = document.getElementById('confettiCanvas');
  const reelEl = document.getElementById('reel');
  const reelOverlay = document.querySelector('.reel-overlay');

  if (!spinBtn || !resultEl || !resultNameEl || !confettiCanvas || !reelEl) {
    console.error('Faltan elementos del DOM. Revisa index.html');
    return;
  }

  const strip = document.createElement('div');
  strip.className = 'strip';
  for (let i = 0; i < itemsPerReel; i++) {
    const it = document.createElement('div');
    it.className = 'item';
    it.textContent = '—';
    strip.appendChild(it);
  }
  strip.style.transform = 'translateY(-40px)';
  reelEl.appendChild(strip);

  // Audio context para música
  let audio = null;

  // Cargar datos del servidor (ya vienen en la página)
  if (window.SECRET_SANTA_DATA) {
    applyServerData(window.SECRET_SANTA_DATA);
  } else {
    console.error('No hay datos disponibles');
    spinBtn.disabled = true;
  }

  spinBtn.addEventListener('click', async () => {
    if (!loaded) return alert('Cargando... espera unos instantes');
    const finalName = assignedFromServer || mockAssign('', namesPool);
    if (!finalName) return alert('No hay nombre disponible');
    spinBtn.disabled = true;
    
    // Iniciar música
    playMusic();
    
    // Mostrar advertencia con cuenta atrás
    await showCountdown();
    
    // Quitar el overlay blur cuando empieza a girar
    if (reelOverlay) {
      reelOverlay.style.opacity = '0';
      reelOverlay.style.transition = 'opacity 0.3s ease';
      setTimeout(() => reelOverlay.style.display = 'none', 300);
    }
    
    await spinAnimation(finalName);
    resultNameEl.textContent = finalName;
    resultEl.classList.remove('hidden');
    fireConfetti();
    
    // Fade out música después de 10 segundos (5 countdown + 5 animación)
    setTimeout(() => stopMusic(), 10000);
  });

  function applyServerData(data) {
    namesPool.length = 0;
    namesPool.push(...(data.names || []));
    assignedFromServer = data.assigned || null;
    loaded = true;
    spinBtn.disabled = false;

    const items = Array.from(strip.children);
    const repeats = Math.ceil(items.length / Math.max(1, namesPool.length));
    const source = shuffle(Array(repeats).fill(namesPool).flat());
    items.forEach((it, i) => { it.textContent = source[i % source.length] || '—'; });
    strip.style.transition = 'none';
    strip.style.transform = 'translateY(-40px)';
  }

  function mockAssign(player, pool) {
    const candidates = pool.filter(n => n !== player);
    if (!candidates.length) return null;
    return candidates[Math.floor(Math.random() * candidates.length)];
  }

  function shuffle(a) {
    for (let i = a.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [a[i], a[j]] = [a[j], a[i]];
    }
    return a;
  }

  function playMusic() {
    try {
      audio = new Audio('/audio/bg_song.m4a');
      audio.currentTime = 6; // Empezar desde el segundo 6
      audio.volume = 0.4; // Volumen al 40%
      audio.play().catch(e => console.log('Error al reproducir audio:', e));
    } catch (e) {
      console.log('Audio no disponible:', e);
    }
  }

  function stopMusic() {
    if (audio) {
      const fadeOut = setInterval(() => {
        if (audio.volume > 0.05) {
          audio.volume = Math.max(0, audio.volume - 0.05);
        } else {
          audio.pause();
          audio = null;
          clearInterval(fadeOut);
        }
      }, 100);
    }
  }

  function showCountdown() {
    return new Promise(resolve => {
      const countdownEl = document.createElement('div');
      countdownEl.className = 'countdown-overlay';
      countdownEl.innerHTML = `
        <div class="countdown-card">
          <p class="countdown-warning">🤫 ¡Asegúrate que nadie esté viendo tu pantalla!</p>
          <p class="countdown-secret">Top Secret</p>
          <p class="countdown-number">5</p>
          <p class="countdown-sound">🔊 Activa el sonido 🔉</p>
        </div>
      `;
      document.body.appendChild(countdownEl);

      const numberEl = countdownEl.querySelector('.countdown-number');
      let count = 5;

      const interval = setInterval(() => {
        count--;
        if (count > 0) {
          numberEl.textContent = count;
          numberEl.style.animation = 'none';
          setTimeout(() => numberEl.style.animation = 'countPulse 0.8s ease-out', 10);
        } else {
          clearInterval(interval);
          countdownEl.style.opacity = '0';
          setTimeout(() => {
            countdownEl.remove();
            resolve();
          }, 300);
        }
      }, 1000);
    });
  }

  function spinAnimation(finalName) {
    return new Promise(resolve => {
      const stripEl = strip;
      const items = Array.from(stripEl.children);
      if (!items.length) return resolve();

      const finalIndex = Math.floor(items.length / 2);
      const repeats = Math.ceil(items.length / Math.max(1, namesPool.length));
      const source = shuffle(Array(repeats).fill(namesPool).flat());
      items.forEach((it, idx) => it.textContent = source[idx % source.length] || '—');
      items[finalIndex].textContent = finalName;

      const itemHeight = 120;
      const finalTranslate = -itemHeight * finalIndex;
      const duration = 5000; // 5 segundos de animación

      stripEl.style.transition = 'none';
      stripEl.style.transform = 'translateY(-40px)';

      requestAnimationFrame(() => {
        requestAnimationFrame(() => {
          // Una sola transición suave de principio a fin
          stripEl.style.transition = `transform ${duration}ms cubic-bezier(0.25, 0.46, 0.45, 0.94)`;
          stripEl.style.transform = `translateY(${finalTranslate}px)`;
          setTimeout(() => resolve(), duration + 50);
        });
      });
    });
  }

  let confettiCtx = null, confettiPieces = [], confettiAnimId = null;
  function setupConfetti() {
    confettiCanvas.width = window.innerWidth;
    confettiCanvas.height = window.innerHeight;
    confettiCtx = confettiCanvas.getContext('2d');
  }
  function fireConfetti() {
    setupConfetti();
    if (!confettiCtx) return;
    confettiPieces = [];
    const count = 120;
    for (let i = 0; i < count; i++) confettiPieces.push({
      x: Math.random()*confettiCanvas.width,
      y: Math.random()*-confettiCanvas.height,
      r: (Math.random()*6)+4,
      color: `hsl(${Math.floor(Math.random()*360)},80%,60%)`,
      tilt: Math.random()*10-10,
      tiltInc: Math.random()*0.07+0.05,
      v: Math.random()*3+2
    });
    renderConfetti();
    setTimeout(() => stopConfetti(), 5000);
  }
  function renderConfetti() {
    if (!confettiCtx) return;
    confettiCtx.clearRect(0, 0, confettiCanvas.width, confettiCanvas.height);
    confettiPieces.forEach(p => {
      confettiCtx.beginPath();
      confettiCtx.fillStyle = p.color;
      confettiCtx.ellipse(p.x, p.y, p.r, p.r*0.6, p.tilt, 0, Math.PI*2);
      confettiCtx.fill();
      p.y += p.v;
      p.x += Math.sin(p.tilt)*2;
      p.tilt += p.tiltInc;
      if (p.y > confettiCanvas.height + 20) p.y = -10;
    });
    confettiAnimId = requestAnimationFrame(renderConfetti);
  }
  function stopConfetti() { if (confettiAnimId) cancelAnimationFrame(confettiAnimId); }
  function clearConfetti() { stopConfetti(); if (confettiCtx) confettiCtx.clearRect(0,0,confettiCanvas.width,confettiCanvas.height); }

  window.addEventListener('resize', setupConfetti);
  setupConfetti();
})();

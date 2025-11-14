@import java.util.List
@param boolean hasToken
@param List<String> names = null
@param String assignedName = null

<!doctype html>
<html lang="es">
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width,initial-scale=1" />
  <title>Amigo Invisible - Secret Santa</title>
  <link rel="stylesheet" href="/css/styles.css">
</head>
<body>
  <main class="container">
    @if(hasToken)
      <div class="tree-container">
        <div class="tree">
          <div class="tree-top">
            <div class="star">⭐</div>
          </div>
          <div class="tree-section tree-section-1">
            <span class="light"></span>
            <span class="light"></span>
            <span class="light"></span>
          </div>
          <div class="tree-section tree-section-2">
            <span class="light"></span>
            <span class="light"></span>
            <span class="light"></span>
            <span class="light"></span>
          </div>
          <div class="tree-section tree-section-3">
            <span class="light"></span>
            <span class="light"></span>
            <span class="light"></span>
            <span class="light"></span>
            <span class="light"></span>
          </div>
          <div class="tree-trunk"></div>
        </div>
      </div>
      <h1>¡Bienvenidx!</h1>
      <h2>¡Descubre quién te ha tocado!</h2>

      <div class="controls">
        <button id="spinBtn">¡Girar!</button>
      </div>

      <section class="slot-machine">
        <div class="reel" id="reel"></div>
        <div class="reel-overlay"></div>
      </section>

      <div id="result" class="result hidden">
        <div class="result-card">
          <h2>¡Te ha tocado!</h2>
          <p id="resultName" class="name"></p>
        </div>
      </div>

      <canvas id="confettiCanvas"></canvas>
    @else
      <div class="tree-container">
        <div class="tree">
          <div class="tree-top">
            <div class="star">⭐</div>
          </div>
          <div class="tree-section tree-section-1">
            <span class="light"></span>
            <span class="light"></span>
            <span class="light"></span>
          </div>
          <div class="tree-section tree-section-2">
            <span class="light"></span>
            <span class="light"></span>
            <span class="light"></span>
            <span class="light"></span>
          </div>
          <div class="tree-section tree-section-3">
            <span class="light"></span>
            <span class="light"></span>
            <span class="light"></span>
            <span class="light"></span>
            <span class="light"></span>
          </div>
          <div class="tree-trunk"></div>
        </div>
      </div>
      <div class="welcome-message">
        <h1>🎅 ¡Hola! Bienvenidx a Secret Santa</h1>
        <p>Para mostrar la información de tu amigo invisible, accede desde el email que has recibido.</p>
        <p>Si no lo tienes, ponte en contacto con el administrador del sorteo.</p>
        <p class="christmas-wish">¡Feliz Navidad! 🎄✨</p>
      </div>
    @endif

    <footer class="note">
      With ❤️ by Miriam & Jotxee
    </footer>
  </main>

  @if(hasToken && names != null && assignedName != null)
  <script>
    // Datos del servidor
    window.SECRET_SANTA_DATA = {
      names: [
        @for(var i = 0; i < names.size(); i++)
          "${names.get(i)}"$unsafe{i < names.size() - 1 ? "," : ""}
        @endfor
      ],
      assigned: "${assignedName}"
    };
  </script>
  @endif
  <script src="/js/script.js"></script>
</body>
</html>

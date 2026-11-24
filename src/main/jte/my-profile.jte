@import java.util.List
@import java.time.format.DateTimeFormatter
@import dev.jotxee.secretsanta.entity.Usuario
@import dev.jotxee.secretsanta.entity.PerfilSorteo
@import dev.jotxee.secretsanta.entity.Sorteo
@param Usuario usuario
@param List<PerfilSorteo> perfiles
@param List<Sorteo> sorteos
@param Usuario viendoUsuario
@param List<PerfilSorteo> viendoPerfiles = null
@param String success = null
@param String error = null

<!doctype html>
<html lang="es">
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width,initial-scale=1" />
  <title>Mi Perfil - Amigo Invisible</title>
  <link rel="icon" type="image/svg+xml" href="/favicon.svg" />
  <link rel="stylesheet" href="/css/styles.css" />
  <style>
    body {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
      min-height: 100vh;
      margin: 0;
      padding: 20px;
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
      overflow-y: auto !important;
      overflow-x: hidden !important;
      display: block !important;
    }

    .profile-container {
      max-width: 1600px;
      margin: 0 auto;
      padding: 0 20px;
    }

    .profile-header {
      background: linear-gradient(135deg, #c31432 0%, #240b36 100%);
      color: white;
      padding: 30px;
      border-radius: 12px;
      text-align: center;
      margin-bottom: 30px;
      box-shadow: 0 4px 20px rgba(0,0,0,0.2);
    }

    .profile-header h1 {
      margin: 0 0 10px 0;
      font-size: 32px;
    }

    .profile-header .email {
      opacity: 0.9;
      font-size: 14px;
    }

    .profile-header .logout-btn {
      display: inline-block;
      padding: 10px 25px;
      background: rgba(120, 0, 24, 0.55);
      border: 1px solid rgba(255,255,255,0.45);
      color: white;
      text-decoration: none;
      border-radius: 25px;
      font-size: 14px;
      transition: background 0.3s, transform 0.2s;
    }

    .profile-header .logout-btn:hover {
      background: rgba(120, 0, 24, 0.8);
      transform: translateY(-2px);
    }

    .profile-actions {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 20px;
      margin-top: 24px;
    }

    .alert {
      padding: 15px 20px;
      border-radius: 8px;
      margin-bottom: 20px;
      animation: slideIn 0.3s ease-out;
    }

    .alert-success {
      background: #d4edda;
      color: #155724;
      border-left: 4px solid #28a745;
    }

    .alert-error {
      background: #f8d7da;
      color: #721c24;
      border-left: 4px solid #dc3545;
    }

    @keyframes slideIn {
      from {
        opacity: 0;
        transform: translateY(-10px);
      }
      to {
        opacity: 1;
        transform: translateY(0);
      }
    }

    .section {
      background: white;
      padding: 25px;
      border-radius: 12px;
      box-shadow: 0 2px 10px rgba(0,0,0,0.1);
      margin-bottom: 25px;
    }

    .section h2 {
      margin: 0 0 20px 0;
      color: #c31432;
      font-size: 24px;
      border-bottom: 2px solid #f0f0f0;
      padding-bottom: 10px;
    }

    .draws-section {
      background: transparent;
      box-shadow: none;
      padding: 0;
    }

    .draws-section > h2 {
      background: white;
      padding: 25px;
      border-radius: 12px;
      box-shadow: 0 2px 10px rgba(0,0,0,0.1);
    }

    .form-group {
      margin-bottom: 20px;
    }

    .form-group label {
      display: block;
      margin-bottom: 8px;
      color: #333;
      font-weight: 600;
    }

    .form-group input,
    .form-group textarea {
      width: 100%;
      padding: 12px;
      border: 2px solid #e0e0e0;
      border-radius: 8px;
      font-size: 14px;
      transition: border-color 0.3s;
      box-sizing: border-box;
    }

    .form-group input:focus,
    .form-group textarea:focus {
      outline: none;
      border-color: #c31432;
    }

    .form-group textarea {
      resize: vertical;
      min-height: 100px;
      font-family: inherit;
    }

    .form-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 15px;
    }

    .btn-primary {
      background: linear-gradient(135deg, #c31432 0%, #240b36 100%);
      color: white;
      padding: 12px 30px;
      border: none;
      border-radius: 25px;
      font-size: 16px;
      font-weight: 600;
      cursor: pointer;
      transition: transform 0.2s, box-shadow 0.2s;
      box-shadow: 0 4px 15px rgba(195, 20, 50, 0.3);
    }

    .btn-primary:hover {
      transform: translateY(-2px);
      box-shadow: 0 6px 20px rgba(195, 20, 50, 0.4);
    }

    .viewing-badge {
      display: inline-block;
      background: #ffc107;
      color: #333;
      padding: 5px 15px;
      border-radius: 20px;
      font-size: 12px;
      font-weight: 600;
      margin-left: 10px;
      vertical-align: middle;
    }

    .back-to-profile-btn {
      display: inline-block;
      padding: 10px 25px;
      background: rgba(255,255,255,0.9);
      color: #c31432;
      text-decoration: none;
      border-radius: 25px;
      font-size: 14px;
      font-weight: 600;
      transition: all 0.3s;
    }

    .back-to-profile-btn:hover {
      background: white;
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0,0,0,0.2);
    }

    .profile-draw-context {
      margin: -8px 0 15px;
      color: #666;
      font-size: 13px;
      font-weight: 600;
    }

    @media (max-width: 600px) {
      body {
        padding: 10px;
      }

      .profile-container {
        max-width: 100%;
        padding: 0 4px;
      }

      .section {
        padding: 16px;
      }

      .draws-section {
        padding: 0;
      }

      .draws-section > h2 {
        padding: 16px;
      }

      .profile-actions {
        flex-direction: column;
        gap: 12px;
      }

      .profile-actions a {
        width: 100%;
        box-sizing: border-box;
        text-align: center;
      }

    }

    .sorteo-card {
      background: #f8f9fa;
      padding: 20px;
      border-radius: 8px;
      margin-bottom: 32px;
      border-left: 4px solid #c31432;
    }

    .sorteo-card h3 {
      margin: 0 0 10px 0;
      color: #c31432;
    }

    .sorteo-card .fecha {
      color: #666;
      font-size: 14px;
      margin-bottom: 15px;
    }

    .draw-profile-card {
      background: #f8f9fa;
      padding: 20px;
      border-radius: 8px;
      margin-bottom: 20px;
      border-left: 4px solid #c31432;
    }

    .draw-profile-card h3 {
      margin: 0 0 15px;
      color: #c31432;
    }

    .draw-profile-editor {
      background: white;
      padding: 20px;
      border-radius: 8px;
      margin: 20px 0;
      border: 1px solid #ececec;
    }

    .draw-profile-editor h4 {
      margin: 0 0 15px;
      color: #c31432;
    }

    .participantes-list {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
      gap: 15px;
      margin-top: 15px;
    }

    .participante-card {
      background: white;
      padding: 15px;
      border-radius: 8px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
      transition: transform 0.2s, box-shadow 0.2s;
    }

    .participante-card:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0,0,0,0.15);
    }

    .participante-card h4 {
      margin: 0 0 8px 0;
      color: #333;
      font-size: 16px;
    }

    .participante-card .email {
      color: #666;
      font-size: 13px;
      margin-bottom: 10px;
    }

    .ver-perfil-btn {
      display: inline-block;
      padding: 8px 16px;
      background: linear-gradient(135deg, #c31432 0%, #240b36 100%);
      color: white;
      text-decoration: none;
      border-radius: 20px;
      font-size: 13px;
      transition: transform 0.2s;
    }

    .ver-perfil-btn:hover {
      transform: scale(1.05);
    }

    .markdown-preview a {
      color: #a71d38;
      font-weight: 600;
      overflow-wrap: anywhere;
    }

    .markdown-preview {
      margin-top: 10px;
      padding: 12px 14px;
      border-left: 3px solid #d8dbea;
      background: #fafbfe;
      color: #444;
      font-size: 14px;
      line-height: 1.5;
      overflow-wrap: anywhere;
    }

    .markdown-preview:empty {
      display: none;
    }

    .markdown-help {
      display: block;
      margin-top: 6px;
      color: #777;
      font-size: 12px;
    }

    .markdown-source-readonly {
      display: none;
    }

    .markdown-list-item {
      padding-left: 12px;
    }

    .preference-card {
      margin-top: 8px;
      padding: 16px 18px;
      background: #fafbfe;
      border: 1px solid #ececf2;
      border-radius: 10px;
    }

    .preference-card-heading {
      display: flex;
      align-items: flex-start;
      justify-content: space-between;
      gap: 16px;
    }

    .preference-card-heading label {
      display: block;
      margin: 0;
      color: #333;
      font-weight: 600;
    }

    .preference-edit-button {
      flex: 0 0 auto;
      padding: 8px 14px;
      border: 1px solid #c31432;
      border-radius: 20px;
      background: white;
      color: #a71d38;
      cursor: pointer;
      font-size: 13px;
      font-weight: 600;
    }

    .preference-edit-button:hover {
      background: #fff4f6;
    }

    .preference-display {
      min-height: 20px;
    }

    .profile-field-summary {
      width: 100%;
      padding: 14px;
      border: 1px solid #e0e0e0;
      border-radius: 8px;
      background: white;
      color: #333;
      cursor: pointer;
      font: inherit;
      text-align: left;
    }

    .profile-field-summary:hover {
      border-color: #c31432;
      box-shadow: 0 2px 8px rgba(195, 20, 50, 0.1);
    }

    .profile-field-summary-label {
      display: block;
      margin-bottom: 6px;
      color: #555;
      font-size: 14px;
      font-weight: 600;
    }

    .profile-field-summary-value {
      display: block;
      color: #333;
      font-size: 16px;
    }

    .preference-empty {
      margin: 12px 0 0;
      color: #888;
      font-size: 14px;
      font-style: italic;
    }

    .preference-modal {
      position: fixed;
      z-index: 1000;
      inset: 0;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 20px;
      background: rgba(36, 11, 54, 0.58);
    }

    .preference-modal[hidden] {
      display: none;
    }

    .preference-modal-panel {
      width: min(760px, 100%);
      max-height: min(720px, calc(100vh - 40px));
      overflow-y: auto;
      padding: 24px;
      border-radius: 14px;
      background: white;
      box-shadow: 0 20px 60px rgba(36, 11, 54, 0.32);
    }

    .preference-modal-heading {
      display: flex;
      align-items: flex-start;
      justify-content: space-between;
      gap: 16px;
      margin-bottom: 18px;
    }

    .preference-modal-heading h3 {
      margin: 0;
      color: #c31432;
      font-size: 21px;
    }

    .preference-modal-heading p {
      margin: 5px 0 0;
      color: #777;
      font-size: 13px;
    }

    .preference-modal-close {
      width: 34px;
      height: 34px;
      border: 0;
      border-radius: 50%;
      background: #f5e9ed;
      color: #a71d38;
      cursor: pointer;
      font-size: 20px;
      line-height: 1;
    }

    .preference-modal textarea {
      width: 100%;
      box-sizing: border-box;
      min-height: 190px;
      resize: vertical;
    }

    .preference-modal-actions {
      display: flex;
      justify-content: flex-end;
      gap: 10px;
      margin-top: 20px;
    }

    .preference-cancel-button,
    .preference-apply-button {
      padding: 10px 18px;
      border-radius: 22px;
      cursor: pointer;
      font-size: 14px;
      font-weight: 600;
    }

    .preference-cancel-button {
      border: 1px solid #ddd;
      background: white;
      color: #666;
    }

    .preference-apply-button {
      border: 0;
      background: linear-gradient(135deg, #c31432 0%, #240b36 100%);
      color: white;
    }

    body.preference-modal-open {
      overflow: hidden;
    }

    @media (max-width: 600px) {
      .preference-card-heading {
        align-items: stretch;
        flex-direction: column;
      }

      .preference-edit-button {
        width: 100%;
      }

      .preference-modal {
        align-items: flex-end;
        padding: 0;
      }

      .preference-modal-panel {
        max-height: calc(100vh - 18px);
        border-radius: 14px 14px 0 0;
        padding: 20px;
      }

      .preference-modal-actions {
        flex-direction: column-reverse;
      }

      .preference-cancel-button,
      .preference-apply-button {
        width: 100%;
      }
    }

  </style>
</head>
<body>
  <div class="profile-container">

    <!-- Header del perfil -->
    <div class="profile-header">
      <h1>👤
        @if(viendoUsuario.getId().equals(usuario.getId()))
          Mi Perfil
        @else
          Perfil de ${viendoUsuario.getNombre()}
          <span class="viewing-badge">📖 Modo Lectura</span>
        @endif
      </h1>
      @if(viendoUsuario.getId().equals(usuario.getId()))
        <div class="email">${usuario.getEmail()}</div>
      @endif
      <div class="profile-actions">
        @if(viendoUsuario.getId().equals(usuario.getId()))
          <a href="/logout" class="logout-btn">🚪 Cerrar Sesión</a>
        @else
          <a href="/my-profile" class="back-to-profile-btn">↩️ Volver a mi perfil</a>
        @endif
      </div>
    </div>

    <!-- Mensajes -->
    @if(success != null)
      <div class="alert alert-success">
        ✅ ${success}
      </div>
    @endif

    @if(error != null)
      <div class="alert alert-error">
        ❌ ${error}
      </div>
    @endif

    @if(!viendoUsuario.getId().equals(usuario.getId()))
      <div class="section">
        <h2>📏 Tallas y preferencias por sorteo</h2>

        !{var perfilesAMostrar = viendoPerfiles != null ? viendoPerfiles : perfiles;}
        @if(perfilesAMostrar.isEmpty())
          <p style="text-align: center; padding: 20px; color: #999;">
            Este usuario no tiene información de tallas todavía.
          </p>
        @else
          @for(var perfil : perfilesAMostrar)
            <div class="draw-profile-card">
              <h3>🎄 ${perfil.getSorteo().getNombre()}</h3>
              <div class="profile-draw-context">Datos específicos de este sorteo</div>

              !{var vCamisa = perfil.getTallaCamisa() != null ? perfil.getTallaCamisa() : "No especificada";}
              !{var vPantalon = perfil.getTallaPantalon() != null ? perfil.getTallaPantalon() : "No especificada";}
              !{var vZapato = perfil.getTallaZapato() != null ? perfil.getTallaZapato() : "No especificada";}
              !{var vChaqueta = perfil.getTallaChaqueta() != null ? perfil.getTallaChaqueta() : "No especificada";}
              !{var vPreferencias = perfil.getPreferencias() != null ? perfil.getPreferencias() : "No hay preferencias especificadas";}

              <div class="form-grid">
                <div class="form-group">
                  <label>👕 Camisa</label>
                  <input type="text" readonly value="${vCamisa}">
                </div>
                <div class="form-group">
                  <label>👖 Pantalón</label>
                  <input type="text" readonly value="${vPantalon}">
                </div>
                <div class="form-group">
                  <label>👟 Zapato</label>
                  <input type="text" readonly value="${vZapato}">
                </div>
                <div class="form-group">
                  <label>🧥 Chaqueta</label>
                  <input type="text" readonly value="${vChaqueta}">
                </div>
              </div>

              <div class="form-group">
                <label>💭 Preferencias y descripción</label>
                <textarea readonly class="markdown-source markdown-source-readonly">${vPreferencias}</textarea>
                <div class="markdown-preview"></div>
              </div>

            </div>
          @endfor
        @endif
      </div>
    @else
      <div class="section draws-section">
        <h2>🎁 Mis Sorteos</h2>

        @if(sorteos.isEmpty())
          <p style="text-align: center; padding: 40px; color: #999;">📭 No estás participando en ningún sorteo todavía.</p>
        @else
          !{var formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");}
          @for(var sorteo : sorteos)
            !{var perfilDelSorteo = perfiles.stream().filter(perfil -> perfil.getSorteo().getId().equals(sorteo.getId())).findFirst().orElse(null);}
            <div class="sorteo-card">
              <h3>🎄 ${sorteo.getNombre()}</h3>
              <div class="fecha">📅 Creado: ${sorteo.getFechaCreacion().format(formatter)}</div>

              @if(perfilDelSorteo != null)
                <div class="draw-profile-editor">
                  <h4>📏 Mis tallas y preferencias</h4>
                  <div class="profile-draw-context">Solo se aplican a este sorteo</div>

                  !{var valorCamisa = perfilDelSorteo.getTallaCamisa() != null ? perfilDelSorteo.getTallaCamisa() : "";}
                  !{var valorPantalon = perfilDelSorteo.getTallaPantalon() != null ? perfilDelSorteo.getTallaPantalon() : "";}
                  !{var valorZapato = perfilDelSorteo.getTallaZapato() != null ? perfilDelSorteo.getTallaZapato() : "";}
                  !{var valorChaqueta = perfilDelSorteo.getTallaChaqueta() != null ? perfilDelSorteo.getTallaChaqueta() : "";}
                  !{var valorPreferencias = perfilDelSorteo.getPreferencias() != null ? perfilDelSorteo.getPreferencias() : "";}
                  !{var mostrarCamisa = valorCamisa.isEmpty() ? "No especificada" : valorCamisa;}
                  !{var mostrarPantalon = valorPantalon.isEmpty() ? "No especificada" : valorPantalon;}
                  !{var mostrarZapato = valorZapato.isEmpty() ? "No especificada" : valorZapato;}
                  !{var mostrarChaqueta = valorChaqueta.isEmpty() ? "No especificada" : valorChaqueta;}

                  <form action="/my-profile/actualizar-tallas" method="post">
                    <input type="hidden" name="perfilId" value="${perfilDelSorteo.getId()}">
                    <div class="form-grid">
                      <button type="button" class="profile-field-summary" data-preference-open="preferencias_modal_${perfilDelSorteo.getId()}">
                        <span class="profile-field-summary-label">👕 Talla de Camisa</span>
                        <span class="profile-field-summary-value">${mostrarCamisa}</span>
                      </button>
                      <button type="button" class="profile-field-summary" data-preference-open="preferencias_modal_${perfilDelSorteo.getId()}">
                        <span class="profile-field-summary-label">👖 Talla de Pantalón</span>
                        <span class="profile-field-summary-value">${mostrarPantalon}</span>
                      </button>
                      <button type="button" class="profile-field-summary" data-preference-open="preferencias_modal_${perfilDelSorteo.getId()}">
                        <span class="profile-field-summary-label">👟 Talla de Zapato</span>
                        <span class="profile-field-summary-value">${mostrarZapato}</span>
                      </button>
                      <button type="button" class="profile-field-summary" data-preference-open="preferencias_modal_${perfilDelSorteo.getId()}">
                        <span class="profile-field-summary-label">🧥 Talla de Chaqueta</span>
                        <span class="profile-field-summary-value">${mostrarChaqueta}</span>
                      </button>
                    </div>

                    <div class="form-group">
                      <div class="preference-card">
                        <div class="preference-card-heading">
                          <div>
                            <label>💭 Preferencias y gustos</label>
                          </div>
                          <button type="button" class="preference-edit-button" data-preference-open="preferencias_modal_${perfilDelSorteo.getId()}" aria-controls="preferencias_modal_${perfilDelSorteo.getId()}">✎ Editar</button>
                        </div>
                        <div id="preferencias_preview_${perfilDelSorteo.getId()}" class="markdown-preview preference-display" aria-live="polite"></div>
                        <p class="preference-empty" data-preference-empty="preferencias_preview_${perfilDelSorteo.getId()}">Todavía no has añadido preferencias.</p>
                      </div>
                    </div>

                    <div id="preferencias_modal_${perfilDelSorteo.getId()}" class="preference-modal" data-preference-modal data-preference-source="preferencias_${perfilDelSorteo.getId()}" hidden>
                      <div class="preference-modal-panel" role="dialog" aria-modal="true" aria-labelledby="preferencias_modal_title_${perfilDelSorteo.getId()}">
                        <div class="preference-modal-heading">
                          <div>
                            <h3 id="preferencias_modal_title_${perfilDelSorteo.getId()}">Editar tallas y preferencias</h3>
                            <p>Actualiza los datos y pulsa Guardar para aplicar los cambios.</p>
                          </div>
                          <button type="button" class="preference-modal-close" data-preference-close aria-label="Cerrar">&times;</button>
                        </div>
                        <div class="form-grid">
                          <div class="form-group">
                            <label for="tallaCamisa_${perfilDelSorteo.getId()}">👕 Talla de Camisa</label>
                            <input type="text" id="tallaCamisa_${perfilDelSorteo.getId()}" name="tallaCamisa" value="${valorCamisa}" placeholder="Ej: S, M, L, XL">
                          </div>
                          <div class="form-group">
                            <label for="tallaPantalon_${perfilDelSorteo.getId()}">👖 Talla de Pantalón</label>
                            <input type="text" id="tallaPantalon_${perfilDelSorteo.getId()}" name="tallaPantalon" value="${valorPantalon}" placeholder="Ej: 42, 44">
                          </div>
                          <div class="form-group">
                            <label for="tallaZapato_${perfilDelSorteo.getId()}">👟 Talla de Zapato</label>
                            <input type="text" id="tallaZapato_${perfilDelSorteo.getId()}" name="tallaZapato" value="${valorZapato}" placeholder="Ej: 42, 38">
                          </div>
                          <div class="form-group">
                            <label for="tallaChaqueta_${perfilDelSorteo.getId()}">🧥 Talla de Chaqueta</label>
                            <input type="text" id="tallaChaqueta_${perfilDelSorteo.getId()}" name="tallaChaqueta" value="${valorChaqueta}" placeholder="Ej: S, M, L">
                          </div>
                        </div>
                        <div class="form-group">
                          <label for="preferencias_${perfilDelSorteo.getId()}">💭 Preferencias y gustos</label>
                          <textarea id="preferencias_${perfilDelSorteo.getId()}" name="preferencias" class="markdown-source" placeholder="Cuéntanos tus gustos, hobbies y colores favoritos. Puedes usar Markdown y pegar enlaces directamente." data-preview-target="preferencias_preview_${perfilDelSorteo.getId()}" data-empty-target="preferencias_preview_${perfilDelSorteo.getId()}">${valorPreferencias}</textarea>
                        </div>
                        <small class="markdown-help">Ejemplos: <strong>**negrita**</strong>, <em>*cursiva*</em>, <code>- Lista de deseos</code>, enlace con texto <code>[Zapatillas adidas](https://www.adidas.es/producto)</code> o URL directa <code>https://www.adidas.es/producto</code>.</small>
                        <div class="preference-modal-actions">
                          <button type="button" class="preference-cancel-button" data-preference-cancel>Cancelar</button>
                          <button type="submit" class="preference-apply-button" data-preference-apply>Guardar</button>
                        </div>
                      </div>
                    </div>

                  </form>
                </div>
              @endif

              <h4 style="margin-top: 20px; margin-bottom: 10px; color: #666;">👥 Participantes del sorteo:</h4>
              <div class="participantes-list">
                @for(var p : sorteo.getPerfiles())
                  <div class="participante-card">
                    <h4>
                      @if(p.getUsuario().getId().equals(usuario.getId()))
                        ${p.getUsuario().getNombre()} <span style="color: #c31432;">(Tú)</span>
                      @else
                        ${p.getUsuario().getNombre()}
                      @endif
                    </h4>
                    <div class="email">${p.getUsuario().getEmail()}</div>
                    @if(!p.getUsuario().getId().equals(usuario.getId()))
                      <a href="/my-profile/usuario/${p.getUsuario().getId()}" class="ver-perfil-btn">
                        👁️ Ver perfil
                      </a>
                    @endif
                  </div>
                @endfor
              </div>
            </div>
          @endfor
        @endif
      </div>
    @endif
  </div>
  <script src="/js/markdown.js?v=3"></script>
</body>
</html>

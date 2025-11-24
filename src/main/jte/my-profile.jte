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
      margin-top: 15px;
      padding: 10px 25px;
      background: rgba(255,255,255,0.2);
      color: white;
      text-decoration: none;
      border-radius: 25px;
      font-size: 14px;
      transition: background 0.3s;
    }

    .profile-header .logout-btn:hover {
      background: rgba(255,255,255,0.3);
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
      margin-top: 15px;
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

    .sorteo-card {
      background: #f8f9fa;
      padding: 20px;
      border-radius: 8px;
      margin-bottom: 20px;
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
      <a href="/logout" class="logout-btn">🚪 Cerrar Sesión</a>
      
      @if(!viendoUsuario.getId().equals(usuario.getId()))
        <a href="/my-profile" class="back-to-profile-btn">↩️ Volver a mi perfil</a>
      @endif
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

    <!-- Información de tallas -->
    <div class="section">
      <h2>📏 Información de Tallas</h2>

      @if(!viendoUsuario.getId().equals(usuario.getId()))
        !{var perfilesAMostrar = viendoPerfiles != null ? viendoPerfiles : perfiles;}
        
        @if(perfilesAMostrar.isEmpty())
          <p style="text-align: center; padding: 20px; color: #999;">
            Este usuario no tiene información de tallas todavía.
          </p>
        @else
          @for(var perfil : perfilesAMostrar)
            <div style="background: #f8f9fa; padding: 20px; border-radius: 8px; margin-bottom: 20px;">
              <h3 style="margin: 0 0 15px 0; color: #c31432;">
                  🎄 ${perfil.getSorteo().getNombre()}@if(sorteos.size() > 1) - ${perfil.getSorteo().getNombreInterno()}@endif
              </h3>
              
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
                <label>💭 Preferencias</label>
                <textarea readonly>${vPreferencias}</textarea>
              </div>
            </div>
          @endfor
        @endif
      @else
        @if(perfiles.isEmpty())
          <p style="text-align: center; padding: 20px; color: #999;">
            📭 No estás participando en ningún sorteo todavía.
          </p>
        @else
          @for(var perfil : perfiles)
            <div style="background: #f8f9fa; padding: 20px; border-radius: 8px; margin-bottom: 20px;">
              <h3 style="margin: 0 0 15px 0; color: #c31432;">
                🎄 ${perfil.getSorteo().getNombre()}@if(perfiles.size() > 1) - ${perfil.getSorteo().getNombreInterno()}@endif
              </h3>

              !{var valorCamisa = perfil.getTallaCamisa() != null ? perfil.getTallaCamisa() : "";}
              !{var valorPantalon = perfil.getTallaPantalon() != null ? perfil.getTallaPantalon() : "";}
              !{var valorZapato = perfil.getTallaZapato() != null ? perfil.getTallaZapato() : "";}
              !{var valorChaqueta = perfil.getTallaChaqueta() != null ? perfil.getTallaChaqueta() : "";}
              !{var valorPreferencias = perfil.getPreferencias() != null ? perfil.getPreferencias() : "";}

              <form action="/my-profile/actualizar-tallas" method="post">
                <input type="hidden" name="perfilId" value="${perfil.getId()}">
                
                <div class="form-grid">
                  <div class="form-group">
                    <label for="tallaCamisa_${perfil.getId()}">👕 Talla de Camisa</label>
                    <input type="text" id="tallaCamisa_${perfil.getId()}" name="tallaCamisa" value="${valorCamisa}" placeholder="Ej: S, M, L, XL">
                  </div>

                  <div class="form-group">
                    <label for="tallaPantalon_${perfil.getId()}">👖 Talla de Pantalón</label>
                    <input type="text" id="tallaPantalon_${perfil.getId()}" name="tallaPantalon" value="${valorPantalon}" placeholder="Ej: 42, 44">
                  </div>

                  <div class="form-group">
                    <label for="tallaZapato_${perfil.getId()}">👟 Talla de Zapato</label>
                    <input type="text" id="tallaZapato_${perfil.getId()}" name="tallaZapato" value="${valorZapato}" placeholder="Ej: 42, 38">
                  </div>

                  <div class="form-group">
                    <label for="tallaChaqueta_${perfil.getId()}">🧥 Talla de Chaqueta</label>
                    <input type="text" id="tallaChaqueta_${perfil.getId()}" name="tallaChaqueta" value="${valorChaqueta}" placeholder="Ej: S, M, L">
                  </div>
                </div>

                <div class="form-group">
                  <label for="preferencias_${perfil.getId()}">💭 Preferencias y gustos</label>
                  <textarea id="preferencias_${perfil.getId()}" name="preferencias" placeholder="Cuéntanos tus gustos, hobbies, colores favoritos, etc.">${valorPreferencias}</textarea>
                </div>

                <button type="submit" class="btn-primary">💾 Guardar Cambios</button>
              </form>
            </div>
          @endfor
        @endif
      @endif
    </div>

    <!-- Mis sorteos -->
    @if(viendoUsuario.getId().equals(usuario.getId()))
      <div class="section">
        <h2>🎁 Mis Sorteos</h2>

        @if(sorteos.isEmpty())
          <p style="text-align: center; padding: 40px; color: #999;">📭 No estás participando en ningún sorteo todavía.</p>
        @else
          !{var formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");}
          @for(var sorteo : sorteos)
            <div class="sorteo-card">
              <h3>🎄 ${sorteo.getNombre()}</h3>
              <div class="fecha">📅 Creado: ${sorteo.getFechaCreacion().format(formatter)}</div>
              
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
</body>
</html>


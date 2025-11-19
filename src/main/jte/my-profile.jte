@import java.util.List
@import java.time.format.DateTimeFormatter
@import dev.jotxee.secretsanta.entity.Participante
@import dev.jotxee.secretsanta.entity.Sorteo
@param Participante participante
@param List<Sorteo> sorteos
@param Participante viendoParticipante
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
    .profile-container {
      max-width: 900px;
      margin: 20px auto;
      padding: 20px;
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
    }

    .readonly-info {
      background: #e3f2fd;
      padding: 15px;
      border-radius: 8px;
      border-left: 4px solid #2196f3;
      margin-bottom: 20px;
    }
  </style>
</head>
<body style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); min-height: 100vh;">
  <div class="profile-container">

    <!-- Header del perfil -->
    <div class="profile-header">
      <h1>👤
        @if(viendoParticipante.getId().equals(participante.getId()))
          Mi Perfil
        @else
          Perfil de ${viendoParticipante.getNombre()}
          <span class="viewing-badge">📖 Modo Lectura</span>
        @endif
      </h1>
      <div class="email">${participante.getEmail()}</div>
      <a href="/logout" class="logout-btn">🚪 Cerrar Sesión</a>
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

      @if(!viendoParticipante.getId().equals(participante.getId()))
        !{var vCamisa = viendoParticipante.getTallaCamisa() != null ? viendoParticipante.getTallaCamisa() : "No especificada";}
        !{var vPantalon = viendoParticipante.getTallaPantalon() != null ? viendoParticipante.getTallaPantalon() : "No especificada";}
        !{var vZapato = viendoParticipante.getTallaZapato() != null ? viendoParticipante.getTallaZapato() : "No especificada";}
        !{var vChaqueta = viendoParticipante.getTallaChaqueta() != null ? viendoParticipante.getTallaChaqueta() : "No especificada";}
        !{var vPreferencias = viendoParticipante.getPreferencias() != null ? viendoParticipante.getPreferencias() : "No hay preferencias especificadas";}

        <div class="readonly-info">
          ℹ️ Estás viendo el perfil de <strong>${viendoParticipante.getNombre()}</strong>.
          Esta información es de solo lectura.
        </div>

        <div class="form-group">
          <label>👕 Talla de Camisa</label>
          <input type="text" readonly value="${vCamisa}">
        </div>

        <div class="form-group">
          <label>👖 Talla de Pantalón</label>
          <input type="text" readonly value="${vPantalon}">
        </div>

        <div class="form-group">
          <label>👟 Talla de Zapato</label>
          <input type="text" readonly value="${vZapato}">
        </div>

        <div class="form-group">
          <label>🧥 Talla de Chaqueta</label>
          <input type="text" readonly value="${vChaqueta}">
        </div>

        <div class="form-group">
          <label>💭 Preferencias</label>
          <textarea readonly>${vPreferencias}</textarea>
        </div>
      @else
        !{var valorCamisa = participante.getTallaCamisa() != null ? participante.getTallaCamisa() : "";}
        !{var valorPantalon = participante.getTallaPantalon() != null ? participante.getTallaPantalon() : "";}
        !{var valorZapato = participante.getTallaZapato() != null ? participante.getTallaZapato() : "";}
        !{var valorChaqueta = participante.getTallaChaqueta() != null ? participante.getTallaChaqueta() : "";}
        !{var valorPreferencias = participante.getPreferencias() != null ? participante.getPreferencias() : "";}

        <form action="/my-profile/actualizar-tallas" method="post">
          <div class="form-group">
            <label for="tallaCamisa">👕 Talla de Camisa</label>
            <input type="text" id="tallaCamisa" name="tallaCamisa" value="${valorCamisa}" placeholder="Ej: M, L, XL">
          </div>

          <div class="form-group">
            <label for="tallaPantalon">👖 Talla de Pantalón</label>
            <input type="text" id="tallaPantalon" name="tallaPantalon" value="${valorPantalon}" placeholder="Ej: 42, 44">
          </div>

          <div class="form-group">
            <label for="tallaZapato">👟 Talla de Zapato</label>
            <input type="text" id="tallaZapato" name="tallaZapato" value="${valorZapato}" placeholder="Ej: 42, 38">
          </div>

          <div class="form-group">
            <label for="tallaChaqueta">🧥 Talla de Chaqueta</label>
            <input type="text" id="tallaChaqueta" name="tallaChaqueta" value="${valorChaqueta}" placeholder="Ej: M, L">
          </div>

          <div class="form-group">
            <label for="preferencias">💭 Preferencias y gustos</label>
            <textarea id="preferencias" name="preferencias" placeholder="Cuéntanos tus gustos, hobbies, colores favoritos, etc.">${valorPreferencias}</textarea>
          </div>

          <button type="submit" class="btn-primary">💾 Guardar Cambios</button>
        </form>
      @endif
    </div>

    <!-- Mis sorteos -->
    <div class="section">
      <h2>🎁 Mis Sorteos</h2>

      @if(sorteos.isEmpty())
        <p style="text-align: center; padding: 40px; color: #999;">📭 No estás participando en ningún sorteo todavía.</p>
      @else
        !{var formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");}
        @for(var sorteo : sorteos)
          <div style="background: #f8f9fa; padding: 15px; border-radius: 8px; margin-bottom: 10px;">
            <h3 style="margin: 0; color: #c31432;">🎄 ${sorteo.getNombre()}</h3>
            <p style="margin: 5px 0 0 0; color: #666; font-size: 14px;">Creado: ${sorteo.getFechaCreacion().format(formatter)}</p>
          </div>
        @endfor
      @endif
    </div>

  </div>
</body>
</html>


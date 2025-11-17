@import java.util.List
@import java.time.format.DateTimeFormatter
@import dev.jotxee.secretsanta.entity.Sorteo
@param List<Sorteo> sorteos
@param String success = null
@param String error = null

<!doctype html>
<html lang="es">
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width,initial-scale=1" />
  <title>Crear Sorteo - Secret Santa</title>
  <link rel="stylesheet" href="/css/admin.css">
</head>
<body>
  <div class="admin-container">
    <header class="admin-header">
      <h1>🎄 Panel de Administración - Secret Santa</h1>
      <a href="/" class="btn-back">← Volver al inicio</a>
    </header>

    <main class="admin-content">
      <!-- Mensajes de éxito/error -->
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

      <!-- Sección: Crear Nuevo Sorteo -->
      <section class="create-section">
        <h2>✨ Crear Nuevo Sorteo</h2>
        <form action="/create" method="post" class="create-form">
          <div class="form-group">
            <label for="sorteoNombre">Nombre del Sorteo (visible para participantes)</label>
            <input type="text" id="sorteoNombre" name="nombre" required 
                   placeholder="Ej: Amigo Invisible Familia 2025">
            <small>Este nombre aparecerá en los emails y en la página de reveal</small>
          </div>

          <div class="form-group">
            <label for="sorteoNombreInterno">Nombre Interno (para administración)</label>
            <input type="text" id="sorteoNombreInterno" name="nombreInterno" 
                   placeholder="Ej: Amigo invisible navidad 2025 familia Sánchez">
            <small>Nombre histórico para referencias año tras año. Opcional.</small>
          </div>

          <div class="form-group">
            <label>Importe del Regalo</label>
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px;">
              <div>
                <input type="number" name="importeMinimo" step="0.01" min="0" 
                       placeholder="Mínimo (€)" style="width: 100%;">
              </div>
              <div>
                <input type="number" name="importeMaximo" step="0.01" min="0" 
                       placeholder="Máximo (€)" style="width: 100%;">
              </div>
            </div>
            <small>Define la franja de precio del regalo. Ambos campos son opcionales.</small>
          </div>

          <div class="participants-section">
            <h3>Participantes</h3>
            <div id="participantsList">
              <div class="participant-row">
                <input type="text" name="participantes[0].nombre" required placeholder="Nombre del participante">
                <input type="email" name="participantes[0].email" required placeholder="email@ejemplo.com">
                <div class="gender-select">
                  <label><input type="radio" name="participantes[0].genero" value="hombre"> 👨 Hombre</label>
                  <label><input type="radio" name="participantes[0].genero" value="mujer"> 👩 Mujer</label>
                </div>
                <button type="button" class="btn-remove" onclick="removeParticipant(this)">🗑️</button>
              </div>
              <div class="participant-row">
                <input type="text" name="participantes[1].nombre" required placeholder="Nombre del participante">
                <input type="email" name="participantes[1].email" required placeholder="email@ejemplo.com">
                <div class="gender-select">
                  <label><input type="radio" name="participantes[1].genero" value="hombre"> 👨 Hombre</label>
                  <label><input type="radio" name="participantes[1].genero" value="mujer"> 👩 Mujer</label>
                </div>
                <button type="button" class="btn-remove" onclick="removeParticipant(this)">🗑️</button>
              </div>
              <div class="participant-row">
                <input type="text" name="participantes[2].nombre" required placeholder="Nombre del participante">
                <input type="email" name="participantes[2].email" required placeholder="email@ejemplo.com">
                <div class="gender-select">
                  <label><input type="radio" name="participantes[2].genero" value="hombre"> 👨 Hombre</label>
                  <label><input type="radio" name="participantes[2].genero" value="mujer"> 👩 Mujer</label>
                </div>
                <button type="button" class="btn-remove" onclick="removeParticipant(this)">🗑️</button>
              </div>
            </div>
            <button type="button" class="btn-add" onclick="addParticipant()">+ Añadir Participante</button>
          </div>

          <button type="submit" class="btn-submit">🎁 Crear Sorteo y Enviar Emails</button>
        </form>
      </section>

      <!-- Sección: Sorteos Activos -->
      <section class="sorteos-section">
        <h2>📋 Sorteos Activos</h2>
        
        @if(sorteos.isEmpty())
          <p class="empty-message">No hay sorteos activos. ¡Crea uno arriba!</p>
        @else
          @for(var sorteo : sorteos)
            !{var formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");}
            <div class="sorteo-card">
              <div class="sorteo-header">
                <div>
                  <h3>${sorteo.getNombre()}</h3>
                  @if(sorteo.getNombreInterno() != null && !sorteo.getNombreInterno().isEmpty())
                    <span class="sorteo-internal-name">📁 ${sorteo.getNombreInterno()}</span>
                  @endif
                  @if(sorteo.getImporteMinimo() != null || sorteo.getImporteMaximo() != null)
                    <span class="sorteo-price">
                      💰 
                      @if(sorteo.getImporteMinimo() != null && sorteo.getImporteMaximo() != null)
                        ${sorteo.getImporteMinimo()}€ - ${sorteo.getImporteMaximo()}€
                      @elseif(sorteo.getImporteMinimo() != null)
                        Desde ${sorteo.getImporteMinimo()}€
                      @else
                        Hasta ${sorteo.getImporteMaximo()}€
                      @endif
                    </span>
                  @endif
                  <span class="sorteo-date">Creado: ${sorteo.getFechaCreacion().format(formatter)}</span>
                </div>
                <button class="btn-delete-sorteo" onclick="deleteSorteo(${sorteo.getId()})">
                  🗑️ Eliminar
                </button>
              </div>
              
              <div class="participants-list">
                <h4>Participantes (${sorteo.getParticipantes().size()})</h4>
                @for(var participante : sorteo.getParticipantes())
                  <div class="participant-item">
                    <div class="participant-info">
                      <span class="participant-name">👤 ${participante.getNombre()}</span>
                      <span class="participant-email">📧 ${participante.getEmail()}</span>
                      @if(participante.getAsignadoA() != null && !participante.getAsignadoA().isEmpty())
                        <span class="participant-assigned">🎁 → ${participante.getAsignadoAEncrypted()}</span>
                      @else
                        <span class="participant-assigned">🎁 → Sin asignar</span>
                      @endif
                    </div>
                    <div class="participant-actions">
                      <button class="btn-edit" onclick="editEmail(${participante.getId()}, '${participante.getEmail()}')">
                        ✏️ Editar Email
                      </button>
                      <button class="btn-resend" onclick="resendEmail(${participante.getId()})">
                        📨 Reenviar
                      </button>
                    </div>
                  </div>
                @endfor
              </div>
            </div>
          @endfor
        @endif
      </section>
    </main>
  </div>

  <script>
    let participantIndex = 3;

    function addParticipant() {
      const list = document.getElementById('participantsList');
      const row = document.createElement('div');
      row.className = 'participant-row';
      row.innerHTML = '<input type="text" name="participantes[' + participantIndex + '].nombre" required placeholder="Nombre del participante">' +
        '<input type="email" name="participantes[' + participantIndex + '].email" required placeholder="email@ejemplo.com">' +
        '<div class="gender-select">' +
        '<label><input type="radio" name="participantes[' + participantIndex + '].genero" value="hombre"> 👨 Hombre</label>' +
        '<label><input type="radio" name="participantes[' + participantIndex + '].genero" value="mujer"> 👩 Mujer</label>' +
        '</div>' +
        '<button type="button" class="btn-remove" onclick="removeParticipant(this)">🗑️</button>';
      list.appendChild(row);
      participantIndex++;
    }

    function removeParticipant(button) {
      const list = document.getElementById('participantsList');
      if (list.children.length > 3) {
        button.parentElement.remove();
      } else {
        alert('Debe haber al menos 3 participantes');
      }
    }

    function editEmail(participanteId, currentEmail) {
      const newEmail = prompt('Nuevo email:', currentEmail);
      if (newEmail && newEmail !== currentEmail) {
        fetch('/api/participante/' + participanteId + '/email', {
          method: 'PUT',
          headers: {'Content-Type': 'application/json'},
          body: JSON.stringify({email: newEmail})
        })
        .then(response => response.ok ? location.reload() : alert('Error al actualizar'))
        .catch(() => alert('Error de conexión'));
      }
    }

    function resendEmail(participanteId) {
      if (confirm('¿Reenviar email a este participante?')) {
        fetch('/api/participante/' + participanteId + '/resend', {method: 'POST'})
        .then(response => response.ok ? alert('Email enviado!') : alert('Error al enviar'))
        .catch(() => alert('Error de conexión'));
      }
    }

    function deleteSorteo(sorteoId) {
      if (confirm('¿Estás seguro de eliminar este sorteo?\n\nEsta acción eliminará el sorteo y todos sus participantes.')) {
        // Crear un formulario oculto para hacer DELETE via POST
        const form = document.createElement('form');
        form.method = 'POST';
        form.action = '/sorteo/' + sorteoId;
        
        // Añadir campo hidden para simular DELETE (Spring soporta _method)
        const methodInput = document.createElement('input');
        methodInput.type = 'hidden';
        methodInput.name = '_method';
        methodInput.value = 'DELETE';
        form.appendChild(methodInput);
        
        document.body.appendChild(form);
        form.submit();
      }
    }
  </script>
</body>
</html>

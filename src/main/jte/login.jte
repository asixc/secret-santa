@param String error = null

<!doctype html>
<html lang="es">
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width,initial-scale=1" />
  <title>Login Administración - Secret Santa</title>
  <link rel="stylesheet" href="/css/admin.css">
  <style>
    .login-container { max-width: 400px; margin: 60px auto; background: #fff; border-radius: 8px; box-shadow: 0 2px 8px #0002; padding: 2em; }
    .login-title { text-align: center; margin-bottom: 1em; }
    .login-form label { display: block; margin-bottom: 0.5em; }
    .login-form input { width: 100%; padding: 0.5em; margin-bottom: 1em; border-radius: 4px; border: 1px solid #ccc; }
    .login-form button { width: 100%; padding: 0.7em; background: #2d7a2d; color: #fff; border: none; border-radius: 4px; font-size: 1.1em; }
    .login-error { color: #b00; text-align: center; margin-bottom: 1em; }
  </style>
</head>
<body>
  <div class="login-container">
    <h2 class="login-title">🔒 Acceso Administración</h2>
    @if(error != null)
      <div class="login-error">Usuario o contraseña incorrectos</div>
    @endif
    <form class="login-form" method="post" action="/login">
      <label for="username">Usuario</label>
      <input type="text" id="username" name="username" required autofocus />
      <label for="password">Contraseña</label>
      <input type="password" id="password" name="password" required />
      <button type="submit">Entrar</button>
    </form>
  </div>
</body>
</html>

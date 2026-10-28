# Denuncia Service

API REST del examen de Taller de Desarrollo de Aplicaciones Móviles. Paquete `com.lara.denunciaservice`; Java 17, Spring Boot 3.5.16, Maven y PostgreSQL.

## Capas

- `entity/Denuncia`: tabla `denuncias`, identificador autoincremental y campos del enunciado.
- `repository/DenunciaRepository`: acceso con JPA y consulta con `LIMIT` y `OFFSET` exactos.
- `service/DenunciaService`: reglas de registro, validación de consultas y atención.
- `controller/DenunciaController`: cinco operaciones HTTP.
- `dto/CrearDenunciaRequest`: valida lo enviado; no admite que el cliente controle el estado ni el identificador.
- `exception/`: respuestas 400 y 404 con errores legibles.

El flujo es `Android → Controller → Service → Repository → PostgreSQL`.

## Contrato

| Método | Ruta | Resultado |
|---|---|---|
| POST | `/denuncias/` | 201, objeto creado, estado 1 |
| GET | `/denuncias/?offset=0&limit=5` | 200, arreglo de objetos ordenado por id ascendente |
| GET | `/denuncias/{id}` | 200, objeto; 404 si no existe |
| GET | `/denuncias/ciudadano/{dni}` | 200, arreglo con **todas** sus denuncias |
| PUT | `/denuncias/{id}` | Sin cuerpo; 200, objeto actualizado con estado 0 |

Se aceptan rutas con o sin barra final. `offset` indica registros omitidos: 0, 5, 10; también admite otros desplazamientos como 2. `limit` admite 1 a 100. DNI conserva sus ceros iniciales porque es texto de ocho dígitos. Repetir PUT conserva estado 0. Una consulta sin coincidencias devuelve `[]`.

Ejemplo de cuerpo POST (datos ficticios):

```json
{
  "dni": "01234567",
  "fecha": "2026-10-28T09:30:00",
  "tipo": "ROBO",
  "direccion": "Av. Prueba 100",
  "descripcion": "Reporte ficticio usado para la demostración"
}
```

La fecha se registra como fecha y hora del hecho, sin zona horaria. El tipo admite texto de hasta 30 caracteres y se normaliza a mayúsculas; no se limita a tres ejemplos.

## Ejecutar con Docker

1. Copiar `.env.example` a `.env` y reemplazar la contraseña por una propia.
2. Con Docker Desktop funcionando, ejecutar en esta carpeta:

```powershell
docker compose up --build -d
```

API: `http://localhost:18080/denuncias/`. PostgreSQL local: puerto **55432**, para evitar interferir con otras instalaciones. Los datos viven en el volumen `denuncias_data`. Para detener sin borrar datos: `docker compose stop`.

## Ejecutar API con Maven y PostgreSQL local

Con Java 17 o superior compatible y Maven, levantar solo la base:

```powershell
docker compose up -d postgres
$env:DB_URL='jdbc:postgresql://localhost:55432/denuncias'
$env:DB_USERNAME='denuncias'
$env:PORT='18080'
# Configurar DB_PASSWORD en el proceso con la misma clave privada de .env.
mvn spring-boot:run
```

Spring Boot no lee `.env` automáticamente al ejecutar Maven: debes configurar las tres variables en ese proceso. El archivo `.env` sí lo lee Docker Compose.

## Pruebas

```powershell
mvn test
```

Las pruebas con MockMvc usan H2 en memoria **solo en el perfil `test`**, verifican las cinco rutas, estados, persistencia, offset exacto, DNI y errores. La aplicación normal y la entrega usan PostgreSQL real; aprobar H2 no demuestra por sí solo una conexión a PostgreSQL ni un despliegue en Render.

## Render

Crear PostgreSQL y Web Service Docker en la misma región. El `Dockerfile` compila y ejecuta las pruebas antes de empaquetar la API.

Configurar en el Web Service:

- `DB_URL`: `jdbc:postgresql://HOST_INTERNO:5432/NOMBRE_BASE?sslmode=require`
- `DB_USERNAME`: usuario de Render.
- `DB_PASSWORD`: contraseña privada de Render.

El puerto usa `PORT` de Render; el puerto interno predeterminado es 8080 y Compose lo publica como 18080 en esta PC. No pegar una URL `postgresql://usuario:clave@host/base` directamente en `DB_URL`: Java necesita el prefijo `jdbc:postgresql://` y aquí usuario/clave van en variables separadas. No subir `.env` ni credenciales.

`spring.jpa.hibernate.ddl-auto=update` crea/actualiza la tabla al iniciar. PostgreSQL expresa `datetime` como `timestamp`; JPA mapea `LocalDateTime` a ese tipo. El estado tiene valor predeterminado 1 también en el esquema.

## Evidencia de entrega pendiente

Después de publicar, probar con Postman las cinco operaciones contra la **URL HTTPS pública**, comprobar que la app consume esa misma URL y completar los enlaces reales de ambos repositorios y el servicio. El código preparado no sustituye esas verificaciones.

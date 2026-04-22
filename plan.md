# Documento de Arquitectura y Especificación - Leafy

**Plataforma:** Android Nativo (Kotlin, Jetpack Compose, Material Design 3)
**Arquitectura:** Offline-First, Spec-Driven Development (SDD), Test-Driven Development (TDD), Clean Architecture + mvvm
**Almacenamiento Local:** Room (SQLite), DataStore (Preferences)
**Trabajos en Segundo Plano:** WorkManager
**Estrategia de APIs:** BYOK (Bring Your Own Key) para botánica + OpenWeatherMap (Free Tier) para clima.

## Funciones Core de la Aplicación (Features)

1. **Onboarding y BYOK:** Configuración inicial de API Keys (Pl@ntNet, Trefle.io y OpenWeatherMap). Localización dinámica en tiempo real.
2. **Escáner de Identificación:** Captura de fotos para identificar nuevas especies usando la API de Pl@ntNet.
3. **Mi Jardín (Offline-First):** Base de datos relacional local (Room) con esquema v6 para datos técnicos botánicos.
4. **Monitoreo de Salud y Evolución:** Cámara dedicada desde el detalle de la planta para capturar fotos de seguimiento sin re-identificación de especie.
5. **Timeline Fotográfico Dinámico:** Galería cronológica con selector de fotos pasadas para comparar cualquier momento del historial con la foto actual mediante un slider.
6. **Ficha Técnica Botánica Completa:** Datos avanzados de Trefle.io (Familia, Género, pH, Temperatura, Hábito de Crecimiento, Toxicidad).
7. **Alertas Climáticas Inteligentes (WorkManager):** Tarea en segundo plano que evalúa el clima local para posponer riegos.
8. **Soberanía de Datos:** Exportación/Importación del huerto completo en formato `.json` (Pendiente).

---

## Estado de Avance (Actualizado: 22 de Abril, 2026)

### Fase 1: Completada (Fundamentos)
- [x] **Onboarding BYOK:** Validación y guardado de API Keys.
- [x] **Arquitectura Base:** Clean Architecture + MVVM + Hilt + Room + DataStore.
- [x] **Escáner (Identificación):** Integración de CameraX con Pl@ntNet.
- [x] **Mi Jardín:** Lista reactiva con estados de carga y lógica de riego.

### Fase 2: Completada (Alertas y Localización)
- [x] **WeatherWorker:** Proceso en segundo plano con WorkManager y notificaciones de lluvia.
- [x] **Localización Dinámica:** Implementada lógica de cambio de idioma (EN/ES) en tiempo real sin reinicio de app.
- [x] **Configuración de Ubicación:** Control de privacidad (Manual/GPS).

### Fase 3: Completada (Migración y Datos Técnicos)
- [x] **Migración a Trefle.io:** Eliminación total de Perenual por limitaciones de plan gratuito.
- [x] **Enriquecimiento Botánico:** Implementada ficha técnica con Familia, Género, Autor, pH, Temperatura, Hábito y Toxicidad.
- [x] **Lógica de Búsqueda Robusta:** Limpieza de nombres científicos y Fallback de búsqueda por Género en Trefle.

### Fase 4: Completada (Timeline y Monitoreo)
- [x] **Sistema de Monitoreo:** Cámara independiente en Detalles para guardar fotos directamente en la línea de tiempo.
- [x] **Galería de Comparación:** Slider funcional que permite navegar por fotos pasadas y compararlas con la más reciente.
- [x] **UI Fullscreen:** Eliminación de headers intrusivos en Cámara y Detalles para una experiencia inmersiva.

### Fase 5: Próximos Pasos
- [ ] **Exportación/Importación:** Funcionalidad de backup en JSON.
- [ ] **Mejorar alertas:** Permitir programar poda y fertilización de forma manual.
- [ ] **Pulir Visuales:** Animaciones de transición entre pantallas.

### 🛠️ Mejoras Técnicas Recientes
- **Base de Datos:** Versión 6 con soporte para campos técnicos y metadatos de fotos.
- **Resiliencia API:** Manejo de datos tri-estado (Yes/No/NA) para evitar información falsa en toxicidad y comestibilidad.
- **UI UX:** Centrado de elementos de confirmación y protección de barras de sistema (status bar).

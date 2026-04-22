# Diseño Técnico - Leafy 🌿

Este documento complementa el `plan.md` con las definiciones técnicas necesarias para iniciar el desarrollo.

## 🛠 Stack Tecnológico Seleccionado

| Categoría | Herramienta | Razón |
| :--- | :--- | :--- |
| **DI** | **Hilt** | Estándar de Android, integración nativa con ViewModel y WorkManager. |
| **Red** | **Retrofit + OkHttp** | Ideal para SDD (Spec-Driven Development) y manejo robusto de JSON. |
| **JSON** | **KotlinX Serialization** | Más rápido y seguro que Gson/Moshi para Kotlin. |
| **Imágenes** | **Coil** | Basado en Coroutines, ligero y diseñado para Jetpack Compose. |
| **Navegación** | **Compose Navigation** | Uso de rutas Type-Safe (Kotlin DSL) para evitar errores de strings. |
| **Reactividad** | **Kotlin Flows** | Integración nativa con Room y DataStore. |

---

## 📂 Estructura de Proyecto (Clean Architecture)

El proyecto seguirá una estructura de paquetes basada en capas:

```text
com.actiometa.leafy/
├── core/
│   ├── di/                 # Módulos de Hilt
│   ├── theme/              # Material 3 Design System
│   └── util/               # Extensiones y helpers
├── data/
│   ├── local/
│   │   ├── dao/            # Room DAOs
│   │   ├── entities/       # Room Entities
│   │   └── LeafyDatabase.kt
│   ├── remote/
│   │   ├── api/            # Interfaces de Retrofit
│   │   └── dto/            # Data Transfer Objects
│   ├── repository/         # Implementaciones de Repositorios
│   └── datastore/          # SettingsRepositoryImpl
├── domain/
│   ├── model/              # Modelos de dominio puros
│   ├── repository/         # Interfaces de Repositorios
│   └── usecase/            # Lógica de negocio (TDD Focused)
└── ui/
    ├── navigation/         # NavHost y Rutas
    ├── components/         # UI atomizada reutilizable
    ├── features/           # Carpetas por funcionalidad
    │   ├── garden/
    │   ├── scanner/
    │   └── onboarding/
    └── MainActivity.kt
```

---

## 🎨 Guía de Estilo Visual (Material 3)

*   **Paleta de Colores:** Dominante Verde Esmeralda (`#2E7D32`) para la naturaleza, con acentos en Terracota (`#A1887F`) para representar la tierra/macetas.
*   **Tipografía:** `Roboto` para legibilidad y `Montserrat` (o similar) para encabezados botánicos.
*   **Componentes Clave:** 
    *   `Card` con elevación mínima para las plantas del jardín.
    *   `FloatingActionButton` para el escáner rápido.
    *   `NavigationBar` inferior para navegación principal.

---

## 🛡️ Manejo de Errores y Estados de UI

Se utilizará una clase sellada (`Sealed Class`) para representar el estado de la UI en los ViewModels:

```kotlin
sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
```

---

## 🚀 Próximos Pasos (Fase 0)

1.  Configurar `build.gradle.kts` con las dependencias mencionadas.
2.  Implementar `SettingsRepository` con DataStore para el flujo de Onboarding.
3.  Crear la estructura de carpetas base.
4.  Definir los contratos de la API en la capa `data/remote`.

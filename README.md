# HydroSwell - Plataforma de Comercio Electrónico E-Commerce

¡Bienvenido al repositorio oficial de **HydroSwell**! Este proyecto consiste en una plataforma web Full-Stack moderna y escalable, diseñada específicamente para la distribución, catálogo y gestión automatizada de suplementos deportivos y nutrición. 

La aplicación cuenta con una arquitectura de **Monorepo** que integra un frontend altamente interactivo para los clientes y un panel de administración centralizado, respaldado por un backend robusto y seguro.

---

## Arquitectura del Proyecto

El repositorio está organizado de forma limpia dividiendo las responsabilidades del sistema:

* **`hydroswell-web/` (Frontend):** Desarrollado con **Next.js**, enfocado en ofrecer una experiencia de usuario rápida, una estética minimalista y un catálogo optimizado para dispositivos móviles.
* **`demo hydroswell/demo/` (Backend):** Desarrollado con **Java y Spring Boot**, encargado de la lógica de negocio, seguridad, persistencia de datos y conectividad externa.

---

## Tecnologías Utilizadas

### Frontend
* **Next.js** (React Framework)
* **TypeScript** (Tipado estático y robustez)
* **Tailwind CSS** (Diseño moderno, fluido y *fitness aesthetic*)
* Componentes optimizados para carrito de compras (`CartDrawer`).

### Backend
* **Java** & **Spring Boot**
* **Spring Security & JWT** (Autenticación y protección de rutas de administración)
* **Spring Data JPA** (Persistencia y mapeo relacional)
* **PostgreSQL / MySQL** (Base de datos relacional para control estricto de Stock y Pedidos)
* **Maven** (Gestor de dependencias)

### Integraciones y Automatizaciones (Próximamente / En Desarrollo)
* **Mercado Pago API:** Pasarela de pagos integrada en el checkout para transacciones reales.
* **n8n Webhooks:** Conectores automatizados integrados en el backend para alertas instantáneas a WhatsApp/Telegram, sincronización con Google Sheets y flujos de marketing automatizados.
* **Java Mail Sender:** Sistema de notificaciones por correo electrónico (SMTP de Gmail) para confirmación de compras.

---

##  Características Principales

* **Catálogo de Productos Dinámico:** Filtros por categoría y visualización limpia de stock en tiempo real.
* **Panel de Administración Protegido:** Módulo CRUD completo (Crear, Leer, Actualizar, Borrar) para productos y categorías al que solo acceden usuarios administradores autorizados.
* **Gestión de Stock Inteligente:** El sistema valida la disponibilidad física de los productos antes de procesar cualquier orden de compra para evitar ventas cruzadas erróneas.
* **Ecosistema Conectado:** Arquitectura preparada con controladores específicos (`WebhookController`) listos para disparar eventos automáticos en n8n ante cada compra.

---

## Configuración Local

Si querés replicar el entorno de desarrollo en tu máquina:

### Requisitos previos
* Java 17 o superior instalado.
* Node.js (versión LTS recomendada).
* Una instancia de base de datos relacional corriendo.

### Pasos
1. **Clonar el repositorio:**
   ```bash
   git clone [https://github.com/jeregaletto/tiendaHydroSwell.git](https://github.com/jeregaletto/tiendaHydroSwell.git)
   cd tiendaHydroSwell
   
2. Levantar el Backend:

Entrar a la carpeta del backend.

Configurar las credenciales de la base de datos en src/main/resources/application.properties.

Ejecutar el proyecto con tu IDE de preferencia o mediante Maven:

Bash
./mvnw spring-boot:run

3. Levantar el Frontend:

Abrir una nueva terminal en la carpeta hydroswell-web.

Instalar dependencias y levantar el servidor de desarrollo:

Bash
npm install
npm run dev

Desarrollador
Jeremías Galetto - Full-Stack Developer & Software Engineering Student

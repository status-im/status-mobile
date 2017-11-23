(ns status-im.translations.es-ar)

(def translations
  {
   ;common
   :members-title                         "Miembros"
   :not-implemented                       "!no implementado"
   :chat-name                             "Nombre del chat"
   :notifications-title                   "Notificaciones y sonidos"
   :offline                               "Fuera de línea"

   ;drawer
   :switch-users                          "Cambiar de usuario"

   ;chat
   :is-typing                             "está escribiendo"
   :and-you                               "y tú"
   :search-chat                           "Buscar chat"
   :members                               {:one   "1 miembro"
                                           :other "{{count}} miembros"
                                           :zero  "no hay miembros"}
   :members-active                        {:one   "1 miembro, 1 activo"
                                           :other "{{count}} miembros, {{count}} activo(s)"
                                           :zero  "no hay miembros"}
   :active-online                         "En línea"
   :active-unknown                        "Desconocido"
   :available                             "Disponible"
   :no-messages                           "No hay mensajes"
   :suggestions-requests                  "Solicitudes"
   :suggestions-commands                  "Comandos"

   ;sync
   :sync-in-progress                      "Sincronizando..."
   :sync-synced                           "Sincronizado"

   ;messages
   :status-sending                        "Enviando"
   :status-pending                        "Pendiente"
   :status-sent                           "Enviado"
   :status-seen-by-everyone               "Visto por todos"
   :status-seen                           "Visto"
   :status-delivered                      "Enviado"
   :status-failed                         "Fallido"

   ;datetime
   :datetime-second                       {:one   "segundo"
                                           :other "segundos"}
   :datetime-minute                       {:one   "minuto"
                                           :other "minutos"}
   :datetime-hour                         {:one   "hora"
                                           :other "horas"}
   :datetime-day                          {:one   "día"
                                           :other "días"}
   :datetime-ago                          "atrás"
   :datetime-yesterday                    "ayer"
   :datetime-today                        "hoy"

   ;profile
   :profile                               "Perfil"
   :message                               "Mensaje"
   :not-specified                         "No especificado"
   :public-key                            "Clave pública"
   :phone-number                          "Número telefónico"
   :add-to-contacts                       "Agregar a contactos"

   ;;make_photo
   :image-source-title                    "Imagen de perfil"
   :image-source-make-photo               "Capturar"
   :image-source-gallery                  "Seleccionar de la galería"

   ;;sharing
   :sharing-copy-to-clipboard             "Copiar"
   :sharing-share                         "Compartir..."
   :sharing-cancel                        "Cancelar"

   ;sign-up
   :contacts-syncronized                  "Tus contactos se han sincronizado"
   :confirmation-code                     (str "¡Gracias! Te hemos enviado un código de confirmación por mensaje de texto. "
                                               "Ingresa este código para confirmar tu número telefónico")
   :incorrect-code                        (str "Lo siento, el código no era correcto; ingrésalo de nuevo")
   :phew-here-is-your-passphrase          "*Wow* eso estuvo difícil, aquí tienes tu contraseña, *¡anótala y mantenla segura!* La necesitarás para recuperar tu cuenta."
   :here-is-your-passphrase               "Aquí tienes tu frase de contraseña, *¡Anótala y mantenga segura!* La necesitarás para recuperar tu cuenta."
   :phone-number-required                 "Pulsa aquí para ingresar tu número telefónico y yo encontraré a tus amigos"
   :intro-status                          "Chatea conmigo para establecer tu cuenta y cambiar tu configuración!"
   :intro-message1                        "Bienvenido(a) a Status\n¡Pulsa en este mensaje para establecer tu contraseña y comenzar!"
   :account-generation-message            "¡Dame un segundo, necesito realizar un súper cálculo para generar tu cuenta!"

   ;chats
   :chats                                 "Chats"
   :new-group-chat                        "Nuevo chat de grupo"

   ;discover
   :discover                              "Descubrimiento"
   :none                                  "Ninguno"
   :search-tags                           "Ingresa aquí tus etiquetas de búsqueda"
   :popular-tags                          "Etiquetas populares"
   :recent                                "Reciente"
   :no-statuses-discovered                "No se encontró ningún estatus"

   ;settings
   :settings                              "Configuración"

   ;contacts
   :contacts                              "Contactos"
   :new-contact                           "Nuevo contacto"
   :contacts-group-new-chat               "Iniciar nuevo chat"
   :no-contacts                           "No hay contactos todavía"
   :show-qr                               "Mostrar QR"

   ;group-settings
   :remove                                "Eliminar"
   :save                                  "Guardar"
   :clear-history                         "Borrar historial"
   :chat-settings                         "Configuración del chat"
   :edit                                  "Editar"
   :add-members                           "Agregar miembros"

   ;commands
   :chat-send-eth                         "{{amount}} ETH"

   ;new-group

   ;participants

   ;protocol
   :received-invitation                   "recibió invitación a chat"
   :removed-from-chat                     "te retiró del grupo de chat"
   :left                                  "restantes"
   :invited                               "invitado"
   :removed                               "retirado"
   :You                                   "Tú"

   ;new-contact
   :add-new-contact                       "Agregar nuevo contacto"
   :scan-qr                               "Escanear QR"
   :name                                  "Nombre"
   :address-explication                   "Tal vez, aquí debería haber alguna indicación explicando qué es una dirección y dónde buscarla"
   :contact-already-added                 "El contacto ya ha sido agregado"
   :can-not-add-yourself                  "No puedes agregarte a ti mismo(a)"
   :unknown-address                       "Dirección desconocida"


   ;login
   :connect                               "Conectar"
   :address                               "Dirección"
   :password                              "Contraseña"
   :wrong-password                        "Contraseña incorrecta"

   ;recover
   :passphrase                            "Frase de contraseña"
   :recover                               "Recuperar"

   ;accounts
   :recover-access                        "Recuperar acceso"

   ;wallet-qr-code
   :done                                  "Completado"
   :main-wallet                           "Cartera principal"

   ;validation
   :invalid-phone                         "Número telefónico inválido"
   :amount                                "Monto"
   ;transactions
   :status                                "Estatus"
   :recipient                             "Destinatario"

   ;:webview
   :web-view-error                        "oops, error"

   :confirm                               "Confirmar"
   :phone-national                        "Nacional"
   :public-group-topic                    "Tema"
   :debug-enabled                         "¡Se inició el servidor de depuración! Ahora puede agregar su DApp ejecutando *status-dev-cli scan* desde su computadora"
   :new-public-group-chat                 "Unirse al chat público"
   :datetime-ago-format                   "{{number}} {{time-intervals}} {{ago}}"
   :twelve-words-in-correct-order         "12 palabras en el orden correcto"
   :remove-from-contacts                  "Eliminar de los contactos"
   :delete-chat                           "Eliminar chat"
   :edit-chats                            "Editar chats"
   :sign-in                               "Iniciar sesión"
   :create-new-account                    "Crear cuenta nueva"
   :sign-in-to-status                     "Iniciar sesión en Status"
   :got-it                                "Entendido"
   :move-to-internal-failure-message      "Necesitamos mover algunos archivos importantes del almacenamiento externo al interno. Para esto, necesitamos su permiso. No usaremos almacenamiento externo en futuras versiones."
   :edit-group                            "Editar grupo"
   :delete-group                          "Eliminar grupo"
   :browsing-title                        "Explorar"
   :reorder-groups                        "Reorganizar grupos"
   :browsing-cancel                       "Cancelar"
   :faucet-success                        "Se recibió la solicitud de Faucet"
   :choose-from-contacts                  "Elegir de los contactos"
   :new-group                             "Nuevo grupo"
   :phone-e164                            "Internacional 1"
   :remove-from-group                     "Eliminar del grupo"
   :search-contacts                       "Buscar contactos"
   :transaction                           "Transacción"
   :public-group-status                   "Público"
   :leave-chat                            "Abandonar chat"
   :start-conversation                    "Iniciar conversación"
   :topic-format                          "Formato incorrecto [a-z0-9\\-]+"
   :enter-valid-public-key                "Ingrese una clave pública válida o escanee un código QR"
   :faucet-error                          "Error de solicitud de Faucet"
   :phone-significant                     "Significativo"
   :search-for                            "Buscar..."
   :phone-international                   "Internacional 2"
   :enter-address                         "Ingresar dirección"
   :send-transaction                      "Enviar transacción"
   :delete-contact                        "Eliminar contacto"
   :mute-notifications                    "Silenciar notificaciones"


   :contact-s                             {:one   "contacto"
                                           :other "contactos"}
   :next                                  "Siguiente"
   :from                                  "De"
   :search-chats                          "Buscar chats"
   :in-contacts                           "En contactos"

   :type-a-message                        "Escribir un mensaje..."
   :type-a-command                        "Comience a escribir un comando..."
   :shake-your-phone                      "¿Encontró un error o tiene una sugerencia? ¡~Sacuda~ su teléfono!"
   :status-prompt                         "Cree un estado para que la gente vea lo que ofrece. También puede usar #hashtags."
   :add-a-status                          "Agregar un estado..."
   :error                                 "Error"
   :edit-contacts                         "Editar contactos"
   :more                                  "más"
   :cancel                                "Cancelar"
   :no-statuses-found                     "No se encontraron estados"
   :browsing-open-in-web-browser          "Abrir en navegador web"
   :delete-group-prompt                   "Esto no afectará a los contactos"
   :edit-profile                          "Editar perfil"


   :empty-topic                           "Tema vacío"
   :to                                    "Para"
   :data                                  "Datos"})

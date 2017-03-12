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
   :invite-friends                        "Invitar amigos"
   :faq                                   "Preguntas frecuentes"
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
   :datetime-multiple                     "s"
   :datetime-ago                          "atrás"
   :datetime-yesterday                    "ayer"
   :datetime-today                        "hoy"

   ;profile
   :profile                               "Perfil"
   :report-user                           "REPORTAR USUARIO"
   :message                               "Mensaje"
   :username                              "Nombre de usuario"
   :not-specified                         "No especificado"
   :public-key                            "Clave pública"
   :phone-number                          "Número telefónico"
   :email                                 "Correo electrónico"
   :profile-no-status                     "Sin estatus"
   :add-to-contacts                       "Agregar a contactos"
   :error-incorrect-name                  "Elecciona otro nombre"
   :error-incorrect-email                 "Correo electrónico incorrecto"

   ;;make_photo
   :image-source-title                    "Imagen de perfil"
   :image-source-make-photo               "Capturar"
   :image-source-gallery                  "Seleccionar de la galería"
   :image-source-cancel                   "Cancelar"

   ;;sharing
   :sharing-copy-to-clipboard             "Copiar"
   :sharing-share                         "Compartir..."
   :sharing-cancel                        "Cancelar"

   ;sign-up
   :contacts-syncronized                  "Tus contactos se han sincronizado"
   :confirmation-code                     (str "¡Gracias! Te hemos enviado un código de confirmación por mensaje de texto. "
                                               "Ingresa este código para confirmar tu número telefónico")
   :incorrect-code                        (str "Lo siento, el código no era correcto; ingrésalo de nuevo")
   :generate-passphrase                   (str "Voy a generar una frase de contraseña para que puedas restablecer tu "
                                               "acceso o iniciar sesión desde otro dispositivo")
   :phew-here-is-your-passphrase          "*Wow* eso estuvo difícil, aquí tienes tu contraseña, *¡anótala y mantenla segura!* La necesitarás para recuperar tu cuenta."
   :here-is-your-passphrase               "Aquí tienes tu frase de contraseña, *¡Anótala y mantenga segura!* La necesitarás para recuperar tu cuenta."
   :written-down                          "Asegúrate de haberla anotado en un lugar seguro"
   :phone-number-required                 "Pulsa aquí para ingresar tu número telefónico y yo encontraré a tus amigos"
   :intro-status                          "Chatea conmigo para establecer tu cuenta y cambiar tu configuración!"
   :intro-message1                        "Bienvenido(a) a Status\n¡Pulsa en este mensaje para establecer tu contraseña y comenzar!"
   :account-generation-message            "¡Dame un segundo, necesito realizar un súper cálculo para generar tu cuenta!"

   ;chats
   :chats                                 "Chats"
   :new-chat                              "Nuevo chat"
   :new-group-chat                        "Nuevo chat de grupo"

   ;discover
   :discover                             "Descubrimiento"
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
   :show-all                              "MOSTRAR TODOS"
   :contacts-group-dapps                  "ÐApps"
   :contacts-group-people                 "Gente"
   :contacts-group-new-chat               "Iniciar nuevo chat"
   :no-contacts                           "No hay contactos todavía"
   :show-qr                               "Mostrar QR"

   ;group-settings
   :remove                                "Eliminar"
   :save                                  "Guardar"
   :change-color                          "Cambiar color"
   :clear-history                         "Borrar historial"
   :delete-and-leave                      "Suprimir y dejar"
   :chat-settings                         "Configuración del chat"
   :edit                                  "Editar"
   :add-members                           "Agregar miembros"
   :blue                                  "Azul"
   :purple                                "Púrpura"
   :green                                 "Verde"
   :red                                   "Rojo"

   ;commands
   :money-command-description             "Enviar dinero"
   :location-command-description          "Enviar ubicación"
   :phone-command-description             "Enviar número telefónico"
   :phone-request-text                    "Solicitud de número telefónico"
   :confirmation-code-command-description "Enviar código de confirmación"
   :confirmation-code-request-text        "Solicitud de código de confirmación"
   :send-command-description              "Enviar ubicación"
   :request-command-description           "Enviar solicitud"
   :keypair-password-command-description  ""
   :help-command-description              "Ayuda"
   :request                               "Solicitud"
   :chat-send-eth                         "{{amount}} ETH"
   :chat-send-eth-to                      "{{amount}} ETH a {{chat-name}}"
   :chat-send-eth-from                    "{{amount}} ETH de {{chat-name}}"

   ;new-group
   :group-chat-name                       "Nombre del chat"
   :empty-group-chat-name                 "Ingresa un nombre"
   :illegal-group-chat-name               "Selecciona otro nombre"

   ;participants
   :add-participants                      "Agregar participantes"
   :remove-participants                   "Retirar participantes"

   ;protocol
   :received-invitation                   "recibió invitación a chat"
   :removed-from-chat                     "te retiró del grupo de chat"
   :left                                  "restantes"
   :invited                               "invitado"
   :removed                               "retirado"
   :You                                   "Tú"

   ;new-contact
   :add-new-contact                       "Agregar nuevo contacto"
   :import-qr                             "Importar"
   :scan-qr                               "Escanear QR"
   :name                                  "Nombre"
   :whisper-identity                      "Identidad de Whisper"
   :address-explication                   "Tal vez, aquí debería haber alguna indicación explicando qué es una dirección y dónde buscarla"
   :enter-valid-address                   "Ingresa una dirección válida o escanea un código QR"
   :contact-already-added                 "El contacto ya ha sido agregado"
   :can-not-add-yourself                  "No puedes agregarte a ti mismo(a)"
   :unknown-address                       "Dirección desconocida"


   ;login
   :connect                               "Conectar"
   :address                               "Dirección"
   :password                              "Contraseña"
   :login                                 "Inicio de sesión"
   :wrong-password                        "Contraseña incorrecta"

   ;recover
   :recover-from-passphrase               "Recuperar con frase de contraseña"
   :recover-explain                       "Ingresa la frase de contraseña para tu contraseña para recuperar el acceso"
   :passphrase                            "Frase de contraseña"
   :recover                               "Recuperar"
   :enter-valid-passphrase                "Ingresa una frase de contraseña"
   :enter-valid-password                  "Ingresa una contraseña"

   ;accounts
   :recover-access                        "Recuperar acceso"
   :add-account                           "Agregar cuenta"

   ;wallet-qr-code
   :done                                  "Completado"
   :main-wallet                           "Cartera principal"

   ;validation
   :invalid-phone                         "Número telefónico inválido"
   :amount                                "Monto"
   :not-enough-eth                        (str "No hay suficiente ETH en tu saldo "
                                               "({{balance}} ETH)")
   ;transactions
   :confirm-transactions                  {:one   "Confirmar transacción"
                                           :other "Confirmar {{count}} transacciones"
                                           :zero  "No hay transacciones"}
   :status                                "Estatus"
   :pending-confirmation                  "Esperando confirmación"
   :recipient                             "Destinatario"
   :one-more-item                         "Un artículo más"
   :fee                                   "Tarifa"
   :value                                 "Valor"

   ;:webview
   :web-view-error                        "oops, error"})

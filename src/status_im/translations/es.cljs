(ns status-im.translations.es)

(def translations
  {
   ;common
   :members-title                         "Miembros"
   :not-implemented                       "¡No se ha implementado!"
   :chat-name                             "Nombre del chat"
   :notifications-title                   "Notificaciones y sonidos"
   :offline                               "Desconectado"

   ;drawer
   :invite-friends                        "Invitar amigos"
   :faq                                   "Preguntas más frecuentes"
   :switch-users                          "Cambiar usuarios"

   ;chat
   :is-typing                             "está escribiendo"
   :and-you                               "y tú"
   :search-chat                           "Buscar chat"
   :members                               {:one   "1 miembro"
                                           :other "{{count}} miembros"
                                           :zero  "sin miembros"}
   :members-active                        {:one   "1 miembro, 1 activo"
                                           :other "{{count}} miembros, {{count}} activos"
                                           :zero  " sin miembros"}
   :active-online                         "En línea"
   :active-unknown                        "Desconocido"
   :available                             "Disponible"
   :no-messages                           "No hay mensajes"
   :suggestions-requests                  "Peticiones"
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
   :status-delivered                      "Entregado"
   :status-failed                         "Fallido"

   ;datetime
   :datetime-ago-format                   "{{ago}} {{number}} {{time-intervals}}"
   :datetime-second                       {:one   "segundo"
                                           :other "segundos"}
   :datetime-minute                       {:one   "minuto"
                                           :other "minutos"}
   :datetime-hour                         {:one   "hora"
                                           :other "horas"}
   :datetime-day                          {:one   "día"
                                           :other "días"}
   :datetime-multiple                     "s"
   :datetime-ago                          "hace"
   :datetime-yesterday                    "ayer"
   :datetime-today                        "hoy"

   ;profile
   :profile                               "Perfil"
   :report-user                           "DENUNCIAR AL USUARIO"
   :message                               "Mensaje"
   :username                              "Nombre de usuario"
   :not-specified                         "No especificado"
   :public-key                            "Clave pública"
   :phone-number                          "Número de teléfono"
   :email                                 "Correo electrónico"
   :profile-no-status                     "Sin estado"
   :add-to-contacts                       "Añadir a contactos"
   :error-incorrect-name                  "Por favor, selecciona otro nombre"
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
   :contacts-syncronized                  "Se han sincronizado tus contactos"
   :confirmation-code                     (str "¡Gracias! Te hemos enviado un mensaje de texto con un código"
                                               "de confirmación. Por favor, introducelo para confirmar tu número de teléfono")
   :incorrect-code                        (str "Lo sentimos, el código era incorrecto. Por favor escríbelo de nuevo.")
   :generate-passphrase                   (str "Generaré una frase de respaldo para que puedas restaurar tu contraseña"
                                               "o iniciar sesión desde otro dispositivo")
   :phew-here-is-your-passphrase          "*Uf* ha sido difícil, aquí está tu contraseña, * escríbela y ¡guárdala en un sitio seguro! * La necesitarás para recuperar tu cuenta."
   :here-is-your-passphrase               "Aquí está tu frase de respaldo, *escríbela y ¡guárdala en un sitio seguro! *La necesitarás para recuperar tu cuenta."
   :written-down                          "Asegúrate de haberla escrito de manera segura"
   :phone-number-required                 "Toca aquí para escribir tu número de teléfono y encontraré a tus amigos"
   :intro-status                          "¡Habla conmigo para configurar tu cuenta y cambiar tu configuración!"
   :intro-message1                        "Bienvenido a Status\n¡Toca este mensaje para configurar tu contraseña y empezar!"
   :account-generation-message            "Dame un segundo, ¡tengo que hacer unas cálculos para generar tu cuenta!"

   ;chats
   :chats                                 "Chats"
   :new-chat                              "Chat nuevo"
   :new-group-chat                        "Grupo de chat nuevo"

   ;discover
   :discover                              "Descubrir"
   :none                                  "Ninguno"
   :search-tags                           "Escribe tus etiquetas de búsqueda aquí"
   :popular-tags                          "Etiquetas populares"
   :recent                                "Reciente"
   :no-statuses-discovered                "No se han encontrado Status"

   ;settings
   :settings                              "Ajustes"

   ;contacts
   :contacts                              "Contactos"
   :new-contact                           "Un contacto nuevo"
   :show-all                              "MOSTRAR TODOS"
   :contacts-group-dapps                  "ÐApps"
   :contacts-group-people                 "Gente"
   :contacts-group-new-chat               "Empezar un chat nuevo"
   :no-contacts                           "No hay contactos todavía"
   :show-qr                               "Mostrar QR"

   ;group-settings
   :remove                                "Eliminar"
   :save                                  "Guardar"
   :change-color                          "Cambiar el color"
   :clear-history                         "Borrar el historial"
   :delete-and-leave                      "Borrar y salir"
   :chat-settings                         "Ajustes del chat"
   :edit                                  "Editar"
   :add-members                           "Añadir miembros"
   :blue                                  "Azul"
   :purple                                "Púrpura"
   :green                                 "Verde"
   :red                                   "Rojo"

   ;commands
   :money-command-description             "Enviar dinero"
   :location-command-description          "Enviar ubicación"
   :phone-command-description             "Enviar número de teléfono"
   :phone-request-text                    "Solicitud de número de teléfono"
   :confirmation-code-command-description "Enviar código de confirmación"
   :confirmation-code-request-text        "Solicitud de código de confirmación"
   :send-command-description              "Enviar comando"
   :request-command-description           "Enviar solicitud"
   :keypair-password-command-description  ""
   :help-command-description              "Ayuda"
   :request                               "Solicitar"
   :chat-send-eth                         "{{amount}} ETH"
   :chat-send-eth-to                      "{{amount}} ETH a {{chat-name}}"
   :chat-send-eth-from                    "{{amount}} ETH de{{chat-name}}"

   ;new-group
   :group-chat-name                       "Nombre del chat"
   :empty-group-chat-name                 "Por favor, escribe un nombre"
   :illegal-group-chat-name               "Por favor, selecciona otro nombre"

   ;participants
   :add-participants                      "Agregar participantes"
   :remove-participants                   "Eliminar participantes"

   ;protocol
   :received-invitation                   "invitación de chat recibida"
   :removed-from-chat                     "te ha eliminado del grupo de chat"
   :left                                  "salió"
   :invited                               "invitado"
   :removed                               "eliminado"
   :You                                   "Tú"

   ;new-contact
   :add-new-contact                       "Añadir un nuevo contacto"
   :import-qr                             "Importar"
   :scan-qr                               "Escanear QR"
   :name                                  "Nombre"
   :whisper-identity                      "Identidad de susurro"
   :address-explication                   "Tal vez aquí debería haber algún texto explicando qué es una dirección y dónde buscarla"
   :enter-valid-address                   "Por favor, escribe una dirección válida o escanea un código QR "
   :contact-already-added                 "Ya se ha añadido el contacto"
   :can-not-add-yourself                  "No puedes agregarte"
   :unknown-address                       "Dirección desconocida"


   ;login
   :connect                               "Conectar"
   :address                               "Dirección"
   :password                              "Contraseña"
   :login                                 "Iniciar sesión"
   :wrong-password                        "Contraseña incorrecta"

   ;recover
   :recover-from-passphrase               "Recuperar con tu frase de respaldo"
   :recover-explain                       "Por favor, escribe la frase de respaldo a modo de contraseña para poder entrar"
   :passphrase                            "Frase de respaldo"
   :recover                               "Recuperar"
   :enter-valid-passphrase                "Por favor, escribe la frase de respaldo"
   :enter-valid-password                  "Por favor, escribe la contraseña"

   ;accounts
   :recover-access                        "Recuperar el acceso"
   :add-account                           "Añadir cuenta"

   ;wallet-qr-code
   :done                                  "Hecho"
   :main-wallet                           "Cartera principal"

   ;validation
   :invalid-phone                         "El número de teléfono no es válido"
   :amount                                "Cantidad"
   :not-enough-eth                        (str "No hay suficiente ETH en conjunto "
                                               "({{balance}} ETH)")
   ;transactions
   :confirm-transactions                  {:one   "Confirmar transacción"
                                           :other "Confirmar {{count}} transacciones"
                                           :zero  "No hay transacciones"}
   :status                                "Estado"
   :pending-confirmation                  "Confirmación pendiente"
   :recipient                             "Beneficiario"
   :one-more-item                         "Un elemento más"
   :fee                                   "Tasa"
   :value                                 "Valor"

   ;:webview
   :web-view-error                        "oops, error"

   :confirm                               "Confirmar"
   :phone-national                        "Nacional"
   :transactions-confirmed                {:one   "Transacción confirmada"
                                           :other "{{count}} transacciones confirmadas"
                                           :zero  "No hay transacciones confirmadas"}
   :public-group-topic                    "Tema"
   :debug-enabled                         "¡El servidor de depuración se ha puesto en marcha! Ahora puede añadir su DApp ejecutando *status-dev-cli scan* desde su ordenador"
   :new-public-group-chat                 "Unirse al chat público"
   :share-qr                              "Compartir QR"
   :feedback                              "¿Tiene comentarios?\n¡Agite su teléfono!"
   :twelve-words-in-correct-order         "12 palabras en el orden correcto"
   :remove-from-contacts                  "Eliminar de los contactos"
   :delete-chat                           "Eliminar chat"
   :edit-chats                            "Editar chats"
   :sign-in                               "Registrarse"
   :create-new-account                    "Crear nueva cuenta"
   :sign-in-to-status                     "Registrarse en Estado"
   :got-it                                "Entendido"
   :move-to-internal-failure-message      "Necesitamos mover algunos archivos importantes del almacenamiento externo al interno. Para ello, necesitamos su permiso. No usaremos el almacenamiento externo en futuras versiones."
   :edit-group                            "Editar grupo"
   :delete-group                          "Eliminar grupo"
   :browsing-title                        "Examinar"
   :reorder-groups                        "Reordenar grupos"
   :browsing-cancel                       "Cancelar"
   :faucet-success                        "La solicitud de clave se ha recibido"
   :choose-from-contacts                  "Elegir entre contactos"
   :new-group                             "Nuevo grupo"
   :phone-e164                            "Internacional 1"
   :remove-from-group                     "Eliminar del grupo"
   :search-contacts                       "Buscar contactos"
   :transaction                           "Transacción"
   :public-group-status                   "Público"
   :leave-chat                            "Salir del chat"
   :start-conversation                    "Iniciar conversación"
   :topic-format                          "Formato incorrecto [a-z0-9\\-]+"
   :enter-valid-public-key                "Introduzca una clave pública válida o escanee un código QR"
   :faucet-error                          "Error de solicitud de clave"
   :phone-significant                     "Significativo"
   :search-for                            "Buscar..."
   :phone-international                   "Internacional 2"
   :enter-address                         "Introducir dirección"
   :send-transaction                      "Enviar transacción"
   :delete-contact                        "Borrar contacto"
   :mute-notifications                    "Silenciar notificaciones"


   :contact-s                             {:one   "contacto"
                                           :other "contactos"}
   :group-name                            "Nombre del grupo"
   :next                                  "Siguiente"
   :from                                  "De"
   :search-chats                          "Buscar chats"
   :in-contacts                           "En los contactos"

   :type-a-message                        "Escribir un mensaje..."
   :type-a-command                        "Comenzar escribiendo un comando..."
   :shake-your-phone                      "¿Ha encontrar un error o tiene una sugerencia? ¡Solo tiene que ~agitar~ su teléfono!"
   :status-prompt                         "Cree un estado para ayudar a la gente a conocer las cosas que está ofreciendo. También puede usar #hashtags."
   :add-a-status                          "Añadir un estado..."
   :error                                 "Error"
   :edit-contacts                         "Editar contactos"
   :more                                  "más"
   :cancel                                "Cancelar"
   :no-statuses-found                     "No se han encontrado estados"
   :swow-qr                               "Mostrar QR"
   :browsing-open-in-web-browser          "Abrir en el navegador web"
   :delete-group-prompt                   "Esto no afectará a los contactos"
   :edit-profile                          "Editar perfil"


   :enter-password-transactions           {:one   "Confirmar transacción introduciendo su contraseña"
                                           :other "Confirmar transacciones introduciendo su contraseña"}
   :unsigned-transactions                 "Transacciones no firmadas"
   :empty-topic                           "Tema vacío"
   :to                                    "A"
   :group-members                         "Miembros del grupo"
   :estimated-fee                         "Cuota est."
   :data                                  "Datos"})

(ns status-im.translations.es-mx)

(def translations
  {
   ;common
   :members-title                         "Miembros"
   :not-implemented                       "!No implementado!"
   :chat-name                             "Nombre del chat"
   :notifications-title                   "Notificaciones y sonidos"
   :offline                               "Desconectado"
   :search-for                            "Buscar..."
   :cancel                                "Cancelar"
   :next                                  "Siguiente"
   :type-a-message                        "Escribe un mensaje..."
   :type-a-command                        "Escribe un comando..."
   :error                                 "Error"

   :camera-access-error                   "Para permitir el acceso a la cámara, porfavor, ve a configuración de sistema y asegúrate que la opción 'Status > Cámara' está seleccionada."
   :photos-access-error                   "Para permitir el acceso a fotos, porfavor, ve a configuración de sistema y asegúrate que la opción 'Status > Fotos' está seleccionada."

   ;drawer
   :invite-friends                        "Invitar amigos"
   :faq                                   "Preguntas frecuentes"
   :switch-users                          "Cambiar usuarios"
   :feedback                              "Tienes comentarios?\n¡Agita tu teléfono!"
   :view-all                              "Ver todo"
   :current-network                       "Red actual"

   ;chat
   :is-typing                             "está escribiendo"
   :and-you                               "y tú"
   :search-chat                           "Buscar chat"
   :members                               {:one   "1 miembro"
                                           :other "{{count}} miembros"
                                           :zero  "no hay miembros"}
   :members-active                        {:one   "1 miembro"
                                           :other "{{count}} miembros"
                                           :zero  "no hay miembros"}
   :public-group-status                   "Público"
   :active-online                         "En línea"
   :active-unknown                        "Desconocido"
   :available                             "Disponible"
   :no-messages                           "No hay mensajes"
   :suggestions-requests                  "Peticiones"
   :suggestions-commands                  "Comandos"
   :faucet-success                        "Se recibió la solicitud de Faucet"
   :faucet-error                          "Error de solicitud de Faucet"

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
   :datetime-ago-format                   "{{number}} {{time-intervals}} {{ago}}"
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
   :edit-profile                          "Editar perfil"
   :report-user                           "REPORTAR USUARIO"
   :message                               "Mensaje"
   :username                              "Nombre de usuario"
   :not-specified                         "No especificado"
   :public-key                            "Llave pública"
   :phone-number                          "Número de teléfono"
   :email                                 "Correo electrónico"
   :update-status                         "Actualizar tu estado..."
   :add-a-status                          "Agregar un estado..."
   :status-prompt                         "Crea un estado para informar a los demás lo que ofreces. También puedes usar #hashtags."
   :add-to-contacts                       "Agregar a contactos"
   :in-contacts                           "En contactos"
   :remove-from-contacts                  "Eliminar de contactos"
   :start-conversation                    "Empezar conversación"
   :send-transaction                      "Enviar transacción"
   :share-qr                              "Compartir QR"
   :error-incorrect-name                  "Por favor, selecciona otro nombre"
   :error-incorrect-email                 "Correo electrónico incorrecto"

   ;;make_photo
   :image-source-title                    "Imagen de perfil"
   :image-source-make-photo               "Tomar foto"
   :image-source-gallery                  "Seleccionar de la galería"
   :image-source-cancel                   "Cancelar"

   ;;sharing
   :sharing-copy-to-clipboard             "Copiar"
   :sharing-share                         "Compartir..."
   :sharing-cancel                        "Cancelar"

   :browsing-title                        "Navegar"
   :browsing-browse                       "@navegar"
   :browsing-open-in-web-browser          "Abrir en navegador"
   :browsing-cancel                       "Cancelar"

   ;sign-up
   :contacts-syncronized                  "Tus contactos se han sincronizado"
   :confirmation-code                     (str "¡Gracias! Te hemos enviado un mensaje de texto con un código de confirmación "
                                               ". Por favor, usa el código para confirmar tu número de teléfono")
   :incorrect-code                        (str "Lo sentimos, el código es incorrecto, escríbelo de nuevo")
   :generate-passphrase                   (str "Te generaré una frase de contraseña para poder restablecer tu "
                                               "acceso o inicia sesión desde otro dispositivo")
   :phew-here-is-your-passphrase          "*Uff* eso fue difícil, esta es tu frase de contraseña, *¡escríbela y guárdala en un lugar seguro!* La necesitarás para recuperar tu cuenta."
   :here-is-your-passphrase               "Esta es tu frase de contraseña, *¡escríbela y guárdala en un lugar seguro!* La necesitarás para recuperar tu cuenta."
   :written-down                          "Cerciórate de haberla escrito de manera segura"
   :phone-number-required                 "Toca aquí para ingresar tu número de teléfono y encontraré a tus amigos"
   :shake-your-phone                      "¿Encontraste un bug o tienes una sugerencia? ¡~Agíta tu teléfono~!"
   :intro-status                          "¡Chatea conmigo para establecer tu cuenta y modificar tu configuración!"
   :intro-message1                        "¡Bienvenid@ a Status\nToca este mensaje para configurar tu contraseña y empezar!"
   :account-generation-message            "Dame un segundo, ¡haré algunos cálculos para generar tu cuenta!"
   :move-to-internal-failure-message      "Necesitamos mover algunos datos importantes de almacenamiento externo a interno. Necesitamos tu permiso para hacerlo. No usaremos almacenamiento externo en futuras versiones."
   :debug-enabled                         "¡El servidor de depuración ha sido lanzado! Ahora puedes ejecutar *status-dev-cli scan* para encontrar el servidor desde tu computadora en la misma red."

   ;phone types
   :phone-e164                            "Internacional 1"
   :phone-international                   "Internacional 2"
   :phone-national                        "Nacional"
   :phone-significant                     "Importante"

   ;chats
   :chats                                 "Chats"
   :new-chat                              "Nuevo chat"
   :delete-chat                           "Eliminar chat"
   :new-group-chat                        "Nuevo grupo de chat"
   :new-public-group-chat                 "Unirte a chat público"
   :edit-chats                            "Editar chats"
   :search-chats                          "Buscar chats"
   :empty-topic                           "Tema vacío"
   :topic-format                          "Formato incorrecto [a-z0-9\\-]+"
   :public-group-topic                    "Tema"

   ;discover
   :discover                              "Descubrir"
   :none                                  "Ninguno"
   :search-tags                           "Escribe tus etiquetas de búsqueda aquí"
   :popular-tags                          "Etiquetas populares"
   :recent                                "Recientes"
   :no-statuses-discovered                "No se descubrieron estados"
   :no-statuses-found                     "No se encontraron estados"

   ;settings
   :settings                              "Ajustes"

   ;contacts
   :contacts                              "Nuevo contacto"
   :delete-contact                        "Borrar contacto"
   :delete-contact-confirmation           "Este contacto será eliminado de tus contactos"
   :remove-from-group                     "Eliminar del grupo"
   :edit-contacts                         "Editar contactos"
   :search-contacts                       "Buscar contactos"
   :show-all                              "MOSTAR TODOS"
   :contacts-group-dapps                  "ÐApps"
   :contacts-group-people                 "Personas"
   :contacts-group-new-chat               "Iniciar chat nuevo"
   :choose-from-contacts                  "Seleccionar de contactos"
   :no-contacts                           "Aún no hay contactos"
   :show-qr                               "Mostrar QR"
   :enter-address                         "Ingresar dirección"
   :more                                  "más"

   ;group-settings
   :remove                                "Remover"
   :save                                  "Guardar"
   :delete                                "Eliminar"
   :change-color                          "Cambiar color"
   :clear-history                         "Borrar historial"
   :mute-notifications                    "Silenciar notificaciones"
   :leave-chat                            "Abandonar chat"
   :delete-and-leave                      "Eliminar y salir"
   :chat-settings                         "Ajustes de chat"
   :edit                                  "Editar"
   :add-members                           "Agregar miembros"
   :blue                                  "Azul"
   :purple                                "Morado"
   :green                                 "Verde"
   :red                                   "Rojo"

   ;commands
   :money-command-description             "Enviar dinero"
   :location-command-description          "Enviar ubicación"
   :phone-command-description             "Enviar número de teléfono"
   :phone-request-text                    "Solicitar número de teléfono"
   :confirmation-code-command-description "Enviar código de confirmación"
   :confirmation-code-request-text        "Solicitar código de confirmación"
   :send-command-description              "Enviar ubicación"
   :request-command-description           "Enviar solicitud"
   :keypair-password-command-description  ""
   :help-command-description              "Ayuda"
   :request                               "Solicitar"
   :chat-send-eth                         "{{amount}} ETH"
   :chat-send-eth-to                      "{{amount}} ETH a {{chat-name}}"
   :chat-send-eth-from                    "{{amount}} ETH de {{chat-name}}"

   ;new-group
   :group-chat-name                       "Nombre del chat"
   :empty-group-chat-name                 "Por favor ingresa un nombre"
   :illegal-group-chat-name               "Por favor selecciona otro nombre"
   :new-group                             "Nuevo grupo"
   :reorder-groups                        "Reordenar grupos"
   :group-name                            "Nombre del grupo"
   :edit-group                            "Editar grupo"
   :delete-group                          "Eliminar grupo"
   :delete-group-confirmation             "Este grupo será eliminado de tus grupos. Esto no afectará tus contactos"
   :delete-group-prompt                   "Esto no afectará tus contactos"
   :group-members                         "Miembros del grupo"
   :contact-s                             {:one   "contacto"
                                           :other "contactos"}
   ;participants
   :add-participants                      "Agregar participantes"
   :remove-participants                   "Eliminar participantes"

   ;protocol
   :received-invitation                   "recibió invitación de chat"
   :removed-from-chat                     "te removió del chat"
   :left                                  "salió"
   :invited                               "invitado"
   :removed                               "eliminado"
   :You                                   "Tú"

   ;new-contact
   :add-new-contact                       "Agregar nuevo contacto"
   :import-qr                             "Importar"
   :scan-qr                               "Escanear QR"
   :swow-qr                               "Mostrar QR"
   :name                                  "Nombre"
   :whisper-identity                      "Identidad whisper"
   :address-explication                   "Tal vez aquí debería haber algún texto explicando qué es una dirección y dónde encontrarla"
   :enter-valid-address                   "Por favor ingresa una dirección válida o escanea un código QR"
   :enter-valid-public-key                "Por favor ingresa una llave pública o escanea un código QR"
   :contact-already-added                 "El contacto ya ha sido agregado"
   :can-not-add-yourself                  "No puedes agregarte a ti mismo"
   :unknown-address                       "Dirección desconocida"


   ;login
   :connect                               "Conectar"
   :address                               "Dirección"
   :password                              "Contraseña"
   :login                                 "Iniciar sesión"
   :sign-in-to-status                     "Regístrate a Status"
   :sign-in                               "Regístrate"
   :wrong-password                        "Contraseña incorrecta"

   ;recover
   :recover-from-passphrase               "Recuperar con frase de contraseña"
   :recover-explain                       "Por favor ingresa la frase de contraseña para recuperar acceso"
   :passphrase                            "Frase de contraseña"
   :recover                               "Recuperar"
   :enter-valid-passphrase                "Por favor ingresa la frase de contraseña"
   :enter-valid-password                  "Por favor ingresa una contraseña"
   :twelve-words-in-correct-order         "12 palabras en orden correcto"

   ;accounts
   :recover-access                        "Recuperar acceso"
   :add-account                           "Agregar cuenta"
   :create-new-account                    "Crear nueva cuenta"

   ;wallet-qr-code
   :done                                  "Listo"
   :main-wallet                           "Wallet principal"

   ;validation
   :invalid-phone                         "Número de teléfono incorrecto"
   :amount                                "Cantidad"
   :not-enough-eth                        (str "No hay suficiente ETH en tu balance"
                                               "({{balance}} ETH)")

   ;transactions
   :confirm                               "Confirmar"
   :confirm-transactions                  {:one   "Confirmar transacción"
                                           :other "Confirmar {{count}} transacciones"
                                           :zero  "Sin transacciones"}
   :transactions-confirmed                {:one   "Transacción confirmada"
                                           :other "{{count}} transacciones confirmadas"
                                           :zero  "Sin transacciones confirmadas"}
   :transaction                           "Transacción"
   :unsigned-transactions                 "Transacción firmada"
   :no-unsigned-transactions              "No hay transacciones sin firmar"
   :enter-password-transactions           {:one   "Confirma la transacción ingresando tu contraseña"
                                           :other "Confirma las transacciones ingresando tu contraseña"}
   :status                                "Estado"
   :pending-confirmation                  "Confirmación pendiente"
   :recipient                             "Receptor"
   :one-more-item                         "Un artículo más"
   :fee                                   "Comisión"
   :estimated-fee                         "Comisión estimada"
   :value                                 "Valor"
   :to                                    "Para"
   :from                                  "De"
   :data                                  "Datos"
   :got-it                                "Entendido"
   :contract-creation                     "Creación de contrato"

   ;:webview
   :web-view-error                        "oops, error"})

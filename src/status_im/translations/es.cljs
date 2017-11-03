(ns status-im.translations.en)

(def translations
  {
   ;;common
   :members-title                         "Miembros"
   :not-implemented                       "¡No se ha implementado!"
   :chat-name                             "Nombre del chat"
   :notifications-title                   "Notificaciones y sonidos"
   :offline                               "Desconectado"
   :search-for                            "Buscar..."
   :cancel                                "Cancelar"
   :next                                  "Siguiente"
   :open                                  "Abierto"
   :description                           "Descripción"
   :url                                   "URL"
   :type-a-message                        "Escribe un mensaje..."
   :type-a-command                        "Empieza escribiendo un comando..."
   :error                                 "Error"
   :unknown-status-go-error               "Error desconocido status-go"
   :node-unavailable                      "Nodo Ethereum no detectado"
   :yes                                   "Sí"
   :no                                    "No"

   :camera-access-error                   "Para conceder el permiso requerido de la cámara, por favor dirígete a los ajustes del sistema y asegúrate de que Status > Camera está seleccionado."
   :photos-access-error                   "Para conceder el permiso requerido de fotos, por favor dirígete a los ajustes del sistema y asegúrate de que Status > Photos está seleccionado."

   ;;drawer
   :switch-users                          "Cambiar de usuarios"
   :current-network                       "Red actual"

   ;;chat
   :is-typing                             "está escribiendo"
   :and-you                               "y tú"
   :search-chat                           "Buscar chat"
   :members                               {:one   "1 miembro"
                                           :other "{{count}} miembros"
                                           :zero  "sin miembros"}
   :members-active                        {:one   "1 miembro"
                                           :other "{{count}} miembros"
                                           :zero  "sin miembros"}
   :public-group-status                   "Público"
   :active-online                         "En línea"
   :active-unknown                        "Desconocido"
   :available                             "Disponible"
   :no-messages                           "Sin mensajes"
   :suggestions-requests                  "Peticiones"
   :suggestions-commands                  "Comandos"
   :faucet-success                        "La petición del faucet ha sido recibida"
   :faucet-error                          "Error solicitando el faucet"

   ;;sync
   :sync-in-progress                      "Sincronizando..."
   :sync-synced                           "Sincronizado"

   ;;messages
   :status-pending                        "Pendiente"
   :status-sent                           "Enviado"
   :status-seen-by-everyone               "Visto por todos"
   :status-seen                           "Visto"
   :status-delivered                      "Entregado"
   :status-failed                         "Fallído"

   ;;datetime
   :datetime-ago-format                   "{{number}} {{time-intervals}} {{ago}}"
   :datetime-second                       {:one   "segundo"
                                           :other "segundos"}
   :datetime-minute                       {:one   "minuto"
                                           :other "minutos"}
   :datetime-hour                         {:one   "hora"
                                           :other "horas"}
   :datetime-day                          {:one   "día"
                                           :other "días"}
   :datetime-ago                          "hace"
   :datetime-yesterday                    "ayer"
   :datetime-today                        "hoy"

   ;;profile
   :profile                               "Perfil"
   :edit-profile                          "Editar perfil"
   :message                               "Mensaje"
   :not-specified                         "No especificado"
   :public-key                            "Clave pública"
   :phone-number                          "Número de teléfono"
   :update-status                         "Actualiza tu estado..."
   :add-a-status                          "Añade un estado..."
   :status-prompt                         "Establece tu estado. Usando #hastags permitirás que los demás te encuentren y podrás hablar sobre lo que tengas en mente"
   :add-to-contacts                       "Añadir a contactos"
   :in-contacts                           "En contactos"
   :remove-from-contacts                  "Eliminar de contactos"
   :start-conversation                    "Empezar conversación"
   :send-transaction                      "Enviar transacción"
   :testnet-text                          "Estás en la {{testnet}} Testnet. No envíes ETH o SNT real a tu dirección"
   :mainnet-text                          "Estás en la Mainnet. Se enviará ETH real"

   ;;make_photo
   :image-source-title                    "Imagen de perfil"
   :image-source-make-photo               "Captura"
   :image-source-gallery                  "Seleccionar de la galería"

   ;;sharing
   :sharing-copy-to-clipboard             "Copiar al portapapeles"
   :sharing-share                         "Compartir..."
   :sharing-cancel                        "Cancelar"

   :browsing-title                        "Navegar"
   :browsing-browse                       "@navega"
   :browsing-open-in-web-browser          "Abrir en el navegador web"
   :browsing-cancel                       "Cancelar"

   ;;sign-up
   :contacts-syncronized                  "Tus contactos se han sincronizado"
   :confirmation-code                     (str "¡Gracias! Has enviado un mensaje de texto con una confirmación "
                                               "código. Por favor danos ese código para confirmar tu número de teléfono")
   :incorrect-code                        (str "Lo siento el código es incorrecto, por favor inténtalo de nuevo")
   :phew-here-is-your-passphrase          "*Uf*, ha sido difícil. Aquí esta tu frase de respaldo, *¡anótala y mantenla a salvo!* La vas a necesitar para recuperar tu cuenta."
   :here-is-your-passphrase               "Aquí está tu frase de respaldo, *escríbela y ¡guárdala en un sitio seguro! *La necesitarás para recuperar tu cuenta."
   :here-is-your-signing-phrase           "Aquí está tu frase para firmar. La vas a usar para firmar tus transacciones. *¡Anótala y mantenla a salvo!*"
   :phone-number-required                 "Toca aquí para validar tu número de teléfono y encontraré a tus amigos."
   :shake-your-phone                      "¿Has encontrado un error o tienes alguna sugerencia? ¡Simplemente ~agita~ tu teléfono!"
   :intro-status                          "Habla conmigo para configurar tu cuenta y cambiar tus ajustes."
   :intro-message1                        "¡Bienvenido a Status!\nToca este mensaje para establecer tu clave y empezar."
   :account-generation-message            "Dame un segundo, ¡tengo que hacer unas cálculos para generar tu cuenta!"
   :move-to-internal-failure-message      "Tenemos que mover algunos archivos importantes del almacenamiento externo al interno. Para ello necesitamos tu permiso. No usaremos el almacenamiento externo en versiones futuras."
   :debug-enabled                         "¡El servidor de depuración ha sido activado! Ahora puedes ejecutar *status-dev-cli scan* para encontrar el servidor en tu ordenador desde la misma red."

   ;;phone types
   :phone-e164                            "Internacional 1"
   :phone-international                   "Internacional 2"
   :phone-national                        "Nacional"
   :phone-significant                     "Significante"

   ;;chats
   :chats                                 "Chats"
   :delete-chat                           "Eliminar chat"
   :new-group-chat                        "Nuevo chat grupal"
   :new-public-group-chat                 "Unise a chat público"
   :edit-chats                            "Editar chats"
   :search-chats                          "Buscar chats"
   :empty-topic                           "Limpiar tema"
   :topic-format                          "Formato incorrecto [a-z0-9\\-]+"
   :public-group-topic                    "Tema"

   ;;discover
   :discover                              "Descubrir"
   :none                                  "Nada"
   :search-tags                           "Escribe tus etiquetas de búsqueda aquí"
   :popular-tags                          "#hashtags populares"
   :recent                                "Estados recientes"
   :no-statuses-found                     "No se han encontrado estados"
   :chat                                  "Chat"
   :all                                   "Todo"
   :public-chats                          "Chats públicos"
   :soon                                  "Pronto"
   :public-chat-user-count                "{{count}} personas"
   :dapps                                 "ÐApps"
   :dapp-profile                          "Perfil ÐApp"
   :no-statuses-discovered                "No se han descubierto estados"
   :no-statuses-discovered-body           "Cuando alguien publique\nun estado lo verás aquí."
   :no-hashtags-discovered-title          "No se han descubierto #hashtags"
   :no-hashtags-discovered-body           "Cuando un #hashtag se vuelva\npopular lo verás aquí."

   ;;settings
   :settings                              "Ajustes"

   ;;contacts
   :contacts                              "Contactos"
   :new-contact                           "Nuevo contacto"
   :delete-contact                        "Eliminar contacto"
   :delete-contact-confirmation           "Este contacto será eliminado de tus contactos"
   :remove-from-group                     "Eliminar del grupo"
   :edit-contacts                         "Editar contactos"
   :search-contacts                       "Buscar contactos"
   :contacts-group-new-chat               "Empezar un nuevo chat"
   :choose-from-contacts                  "Elegir de los contactos"
   :no-contacts                           "No hay contactos todavía"
   :show-qr                               "Mostrar código QR"
   :enter-address                         "Introducir dirección"
   :more                                  "más"

   ;;group-settings
   :remove                                "Eliminar"
   :save                                  "Guardar"
   :delete                                "Eliminar"
   :clear-history                         "Limpiar historial"
   :mute-notifications                    "Silenciar notificaciones"
   :leave-chat                            "Abandonar chat"
   :chat-settings                         "Ajustes de chat"
   :edit                                  "Editar"
   :add-members                           "Añadir miembros"

   ;;commands
   :chat-send-eth                         "{{amount}} ETH"

   ;;location command
   :your-current-location                 "Tu posición actual"
   :places-nearby                         "Lugares cerca"
   :search-results                        "Buscar resultados"
   :dropped-pin                           "Pin caído"
   :location                              "Localización"
   :open-map                              "Abrir Mapa"
   :sharing-copy-to-clipboard-address     "Copiar la dirección"
   :sharing-copy-to-clipboard-coordinates "Copiar coordenadas"

   ;;new-group
   :new-group                             "Nuevo grupo"
   :reorder-groups                        "Reordenar grupos"
   :edit-group                            "Editar grupo"
   :delete-group                          "Eliminar grupo"
   :delete-group-confirmation             "Este grupo será eliminado de tus grupos. Esto no afectará a tus contactos"
   :delete-group-prompt                   "Esto no afectará a tus contactos"
   :contact-s                             {:one   "contacto"
                                           :other "contactos"}

   ;;protocol
   :received-invitation                   "recibida invitación de chat"
   :removed-from-chat                     "eliminado del chat grupal"
   :left                                  "abandonado"
   :invited                               "invitado"
   :removed                               "eliminado"
   :You                                   "Tú"

   ;;new-contact
   :add-new-contact                       "Añadir nuevo contacto"
   :scan-qr                               "Escanear código QR"
   :name                                  "Nombre"
   :address-explication                   "Tu clave pública se usa para generar tu dirección en Ethereum y es una serie de números y letras. La puedes encontrar fácilmente en tu perfil"
   :enter-valid-public-key                "Por favor introduce una clave pública válida o escanea un código QR"
   :contact-already-added                 "El contacto se ha añadido"
   :can-not-add-yourself                  "No te puedes añadir a tí mismo"
   :unknown-address                       "Dirección desconocida"

   ;;login
   :connect                               "Conectar"
   :address                               "Dirección"
   :password                              "Contraseña"
   :sign-in-to-status                     "Iniciar sesión en Status"
   :sign-in                               "Iniciar sesión"
   :wrong-password                        "Contraseña incorrecta"
   :enter-password                        "Introduce contraseña"

   ;;recover
   :passphrase                            "Frase clave"
   :recover                               "Recuperar"
   :twelve-words-in-correct-order         "12 palabras en el orden correcto"

   ;;accounts
   :recover-access                        "Recuperar acceso"
   :create-new-account                    "Crear una nueva cuenta"

   ;;wallet-qr-code
   :done                                  "Hecho"

   ;;validation
   :invalid-phone                         "Número de teléfono inválido"
   :amount                                "Cantidad"

   ;;transactions
   :confirm                               "Confirmar"
   :transaction                           "Transacción"
   :unsigned-transaction-expired          "Transación sin firmar expirada"
   :status                                "Estado"
   :recipient                             "Destinatario"
   :to                                    "Para"
   :from                                  "De"
   :data                                  "Datos"
   :got-it                                "Entendido"
   :block                                 "Bloquear"
   :hash                                  "Hash"
   :gas-limit                             "Límite de gas"
   :gas-price                             "Precio del gas"
   :gas-used                              "Gas usado"
   :cost-fee                              "Coste/Tarifa"
   :nonce                                 "Nonce"
   :confirmations                         "Confirmaciones"
   :confirmations-helper-text             "Por favor espera al menos a que haya 12 confirmaciones para asegurarte de que tu transacción es procesada con seguridad"
   :copy-transaction-hash                 "Copiar hash de transacción"
   :open-on-etherscan                     "Abrir en Etherscan.io"

   ;;webview
   :web-view-error                        "oops, error"

   ;;testfairy warning
   :testfairy-title                       "¡Advertencia!"
   :testfairy-message                     "Estás usando una aplicación instalada desde una versión nightly. Por motivos de testeo esta versión incluye grabación de la sesión si se usa conexión wifi, así que todas las interacciones con esta aplicación se guardan (así como vídeo y registros) y pueden ser usadas por nuestro equipo de desarrollo para investigar posibles problemas. Los video/registros no incluyen tus contraseñas. La grabación se realiza solo si la app está instalada desde una versión nightly. Nada se graba si la app está instalada desde la PlayStore o desde TestFlight."

   ;; wallet
   :wallet                                "Monedero"
   :wallets                               "Monederos"
   :your-wallets                          "Tus monederos"
   :main-wallet                           "Monedero principal"
   :wallet-error                          "Error cargando datos"
   :wallet-send                           "Enviar"
   :wallet-request                        "Solicitar"
   :wallet-exchange                       "Intercambio"
   :wallet-assets                         "Activos"
   :wallet-add-asset                      "Añadir activo"
   :wallet-total-value                    "Valor total"
   :wallet-settings                       "Ajustes de monedero"
   :signing-phrase-description            "Firma la transación poniendo tu contraseña. Asegúrate de que las palabras de encima coinciden con tu frase de firmas secreta"
   :wallet-insufficient-funds             "Fondos insuficientes"
   :request-transaction                   "Solicitar transacción"
   :send-request                          "Enviar solicitud"
   :share                                 "Compartir"
   :eth                                   "ETH"
   :currency                              "Divisa"
   :usd-currency                          "USD"
   :transactions                          "Transacciones"
   :transaction-details                   "Detalles de la transación"
   :transaction-failed                    "Transacción fallida"
   :transactions-sign                     "Firmar"
   :transactions-sign-all                 "Firmar todo"
   :transactions-sign-transaction         "Firmar transacción"
   :transactions-sign-later               "Firmar luego"
   :transactions-delete                   "Eliminar transación"
   :transactions-delete-content           "La transacción será eliminada de la lista 'Sin firmar'"
   :transactions-history                  "Historial"
   :transactions-unsigned                 "Sin firmar"
   :transactions-history-empty            "No hay transacciones en tu historial todavía"
   :transactions-unsigned-empty           "No tienes ninguna transacción sin firmar"
   :transactions-filter-title             "Filtrar historial"
   :transactions-filter-tokens            "Tokens"
   :transactions-filter-type              "Tipo"
   :transactions-filter-select-all        "Seleccionar todo"
   :view-transaction-details              "Ver detalles de la transacción"
   :transaction-description               "Espera hasta al menos 12 confirmaciones para asegurarte de que la transacción es procesada con seguridad"
   :transaction-sent                      "Transacción enviada"
   :transaction-moved-text                "La transacción permanecerá en la lista 'Sin firmar' durante los siguientes 5 minutos"
   :transaction-moved-title               "Transacción movida"
   :sign-later-title                      "¿Firmar la transacción después?"
   :sign-later-text                       "Comprueba el historial de transacciones para firmar esta transacción"
   :not-applicable                        "No aplicable para transacciones sin firmar"

   ;; Wallet Send
   :wallet-choose-recipient               "Eligir destinatario"
   :wallet-choose-from-contacts           "Elegir de los Contactos"
   :wallet-address-from-clipboard         "Usar Dirección del Portapapeles"
   :wallet-invalid-address                "Dirección inválida: \n {{data}}"
   :wallet-browse-photos                  "Navegar Imágenes"
   :validation-amount-invalid-number      "Cantidad no es un número válido"
   :validation-amount-is-too-precise      "Cantidad es demasiado precisa. La mínima unidad que puedes enviar es 1 Wei (1x10^-18 ETH)"



   ;; network settings
   :new-network                           "Nueva red"
   :add-network                           "Añadir red"
   :add-new-network                       "Añadir nueva red"
   :existing-networks                     "Redes existentes"
   :add-json-file                         "Añadir un archivo JSON"
   :paste-json-as-text                    "Pegar JSON como texto"
   :paste-json                            "Pegar JSON"
   :specify-rpc-url                       "Especificar una URL RPC"
   :edit-network-config                   "Editar configuración de red"
   :connected                             "Conectado"
   :process-json                          "Procesar JSON"
   :error-processing-json                 "Error procesando JSON"
   :rpc-url                               "RPC URL"
   :remove-network                        "Eliminar red"
   :network-settings                      "Ajustes de red"
   :edit-network-warning                  "Cuidado, editar los datos de red puede deshabilitar esta red para tí"
   :connecting-requires-login             "Conectarse a otra red requiere inicio de sesión"
   :close-app-title                       "¡Advertencia!"
   :close-app-content                     "La app se parará y cerrará. Cuando la vuelvas a abrir, la red seleccionada será usada"
   :close-app-button                      "Confirmar"})

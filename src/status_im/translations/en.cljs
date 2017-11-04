(ns status-im.translations.en)

(def translations
  {
   ;;common
   :members-title                         "Miembros"
   :not-implemented                       "¡No se ha implementado!"
   :chat-name                             "Nombre del Chat"
   :notifications-title                   "Notificaciones y sonidos"
   :offline                               "No Conectado"
   :search-for                            "Buscar..."
   :cancel                                "Cancelar"
   :next                                  "Siguiente"
   :open                                  "Abierto"
   :description                           "Descripción"
   :url                                   "URL"
   :type-a-message                        "Escribe un mensaje..."
   :type-a-command                        "Escribir un comando..."
   :error                                 "Error"
   :unknown-status-go-error               "status-go Error desconocido"
   :node-unavailable                      "No hay nodo de ethereum activo"
   :yes                                   "Sí"
   :no                                    "No"

   :camera-access-error                   "Para garantizar los permisos a la cámara, ve a configuración del sistema y asegúrate que la opción 'Cámara' esta seleccionada."
   :photos-access-error                   "Para garantizar los permisos a la cámara, ve a configuración del sistema y asegúrate que la opción 'fotos' esta seleccionada."

   ;;drawer
   :switch-users                          "Cambiar de usuarios"
   :current-network                       "Red actual"

   ;;chat
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
   :suggestions-requests                  "Solicitudes"
   :suggestions-commands                  "Comandos"
   :faucet-success                        "La Solicitud para el grifo ha sido recibida"
   :faucet-error                          "Error en la solicitud del grifo"

   ;;sync
   :sync-in-progress                      "Sincronizando..."
   :sync-synced                           "Sincronizado"

   ;;messages
   :status-pending                        "Pendiente"
   :status-sent                           "Enviado"
   :status-seen-by-everyone               "Visto por todos"
   :status-seen                           "Visto"
   :status-delivered                      "Entregado"
   :status-failed                         "Fallo"

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
   :add-a-status                          "Agregar estado..."
   :status-prompt                         "Pon tu estado. Usando #hastags ayudaras a otros a encontrarte y opinar sobre que estás pensando"
   :add-to-contacts                       "Agregar a contactos"
   :in-contacts                           "En contactos"
   :remove-from-contacts                  "Borrar de contactos"
   :start-conversation                    "Iniciar conversación"
   :send-transaction                      "Enviar transacción"
   :testnet-text                          "Estas en {{testnet}} la red de prueba. No en envíes ETH o SNT reales a tu dirección"
   :mainnet-text                          "Estas en la red principal. ETH real será enviado"

   ;;make_photo
   :image-source-title                    "Imagen de perfil"
   :image-source-make-photo               "Capturar"
   :image-source-gallery                  "Seleccionar de la galería"

   ;;sharing
   :sharing-copy-to-clipboard             "Copiar al portapapeles"
   :sharing-share                         "Compartir..."
   :sharing-cancel                        "Cancelar"

   :browsing-title                        "Navegar"
   :browsing-browse                       "@navega"
   :browsing-open-in-web-browser          "Abrir en navegador"
   :browsing-cancel                       "Cancelar"

   ;;sign-up
   :contacts-syncronized                  "Tus contactos han sido sincronizados"
   :confirmation-code                     (str "Gracias! te enviamos un mensaje de texto con el código de confirmación "
                                               "Escribe dicho código para confirmar tu número telefónico")
   :incorrect-code                        (str "Lo sentimos, el código es incorrecto, intenta nuevamente")
   :phew-here-is-your-passphrase          "Uff, fue difícil. Aquí está tu frase de seguridad, *Escríbela en un lugar seguro y respáldala por tu seguridad!* la necesitaras para recuperar tu cuenta."
   :here-is-your-passphrase               "Aquí está tu frase de seguridad, *Escríbela en un lugar seguro y respáldala por tu seguridad!* la necesitaras para recuperar tu cuenta."
   :here-is-your-signing-phrase           "Aquí está tu frase para iniciar sesión. La usaras para verificar transacciones. *Escríbela en un lugar seguro y respáldala por tu seguridad!*"
   :phone-number-required                 "Haz click aquí para validar tu número telefónico y buscar a tus amigos."
   :shake-your-phone                      "Encontraste un error o tienes algún comentario? Solamente ~sacude~ tu teléfono!"
   :intro-status                          "Platica conmigo para preparar tu cuenta y cambiar las opciones."
   :intro-message1                        "Bienvenido a Status!\nclick en este mensaje para crear tu contraseña e iniciar."
   :account-generation-message            "Espera un momentito, Hare unos cálculos muy extraños para generar tu cuenta!"
   :move-to-internal-failure-message      "Necesitamos mover archivos importantes de la memoria externa a la memoria interna. Para hacerlo, necesitamos que estés de acuerdo. No utilizaremos memoria externa en versiones futuras."
   :debug-enabled                         "El servidor de depuración ha sido activado! puedes correr *status-dev-cli scan* para encontrar el servidor desde una computadora en la misma red."

   ;;phone types
   :phone-e164                            "Internacional 1"
   :phone-international                   "Internacional 2"
   :phone-national                        "Nacional"
   :phone-significant                     "Significado"

   ;;chats
   :chats                                 "Mensajes"
   :delete-chat                           "Borrar mensajes"
   :new-group-chat                        "Nuevo grupo"
   :new-public-group-chat                 "Unirse a un grupo público"
   :edit-chats                            "Editar mensajes"
   :search-chats                          "Buscar mensajes"
   :empty-topic                           "Limpiar tema"
   :topic-format                          "Formato incorrecto [a-z0-9\\-]+"
   :public-group-topic                    "Tema"

   ;;discover
   :discover                              "Descubrir"
   :none                                  "Nada"
   :search-tags                           "Escribe tus etiquetas a buscar aquí"
   :popular-tags                          "#hashtags Populares"
   :recent                                "Estados Recientes"
   :no-statuses-found                     "Estado no encontrado"
   :chat                                  "mensaje"
   :all                                   "Todo"
   :public-chats                          "Mensajes públicos"
   :soon                                  "Rápido"
   :public-chat-user-count                "{{count}} gente"
   :dapps                                 "ÐApps"
   :dapp-profile                          "perfil de ÐApp"
   :no-statuses-discovered                "Estado no descubierto"
   :no-statuses-discovered-body           "Cuando alguien ponga\n un estado lo veras aquí."
   :no-hashtags-discovered-title          "#hashtags no descubiertos"
   :no-hashtags-discovered-body           "Si un #hashtag es \nlo veras aquí."

   ;;settings
   :settings                              "Configuración"

   ;;contacts
   :contacts                              "Contactos"
   :new-contact                           "Nuevo contacto"
   :delete-contact                        "Borrar contacto"
   :delete-contact-confirmation           "Este contacto será borrado de tu lista de contactos"
   :remove-from-group                     "Borrar de grupo"
   :edit-contacts                         "Editar contactos"
   :search-contacts                       "Buscar contactos"
   :contacts-group-new-chat               "Iniciar platica nueva"
   :choose-from-contacts                  "Seleccionar de contactos"
   :no-contacts                           "Aún no hay contactos"
   :show-qr                               "Mostrar código QR"
   :enter-address                         "Introducir dirección"
   :more                                  "más"

   ;;group-settings
   :remove                                "Borrar"
   :save                                  "Grabar"
   :delete                                "Borrar"
   :clear-history                         "Borrar historial"
   :mute-notifications                    "Silenciar notificaciones"
   :leave-chat                            "Abandonar platica"
   :chat-settings                         "Configuraciones de la plática"
   :edit                                  "Editar"
   :add-members                           "Agregar miembros"

   ;;commands
   :chat-send-eth                         "{{amount}} ETH"

   ;;location command
   :your-current-location                 "Ubicación actual"
   :places-nearby                         "Lugares cercanos"
   :search-results                        "Buscar resultados"
   :dropped-pin                           "Marcador puesto"
   :location                              "Ubicación"
   :open-map                              "Abrir Mapa"
   :sharing-copy-to-clipboard-address     "Copiar la dirección"
   :sharing-copy-to-clipboard-coordinates "Copiar las coordenadas"

   ;;new-group
   :new-group                             "Grupo nuevo"
   :reorder-groups                        "Reordenar grupos"
   :edit-group                            "Modificar grupo"
   :delete-group                          "Borrar grupo"
   :delete-group-confirmation             "Este grupo será eliminado de tus grupos. No serán afectados tus contactos"
   :delete-group-prompt                   "No serán afectados tus contactos"
   :contact-s                             {:one   "contacto"
                                           :other "contactos"}

   ;;protocol
   :received-invitation                   "invitación de platica recibida"
   :removed-from-chat                     "Has sido borrado del grupo de platica"
   :left                                  "abandonado"
   :invited                               "invitado"
   :removed                               "eliminado"
     :You                                   "Tú"

   ;;new-contact
   :add-new-contact                       "Agregar contacto nuevo"
   :scan-qr                               "Escanear código QR"
   :name                                  "Nombre"
   :address-explication                   "Tu llave publica es usada para generar una dirección de Ethereum, es una serie de números y letras. Puedes encontrarla fácilmente en tu perfil"
   :enter-valid-public-key                "Por favor, ingresa una llave publica valida o escanea un código QR"
   :contact-already-added                 "El contacto ha sido agregado"
   :can-not-add-yourself                  "No te puedes agregar a ti mismo"
     :unknown-address                       "Dirección desconocida"

   ;;login
   :connect                               "Conectar"
   :address                               "Dirección"
   :password                              "Contraseña"
   :sign-in-to-status                     "Ingresar a Status"
   :sign-in                               "Ingresar"
   :wrong-password                        "Contraseña incorrecta"
   :enter-password                        "Poner contraseña"

   ;;recover
   :passphrase                            "Frase de seguridad"
   :recover                               "Recuperar"
   :twelve-words-in-correct-order         "12 palabras en el orden correcto"

   ;;accounts
   :recover-access                        "Recuperar acceso"
   :create-new-account                    "Crear cuenta nueva"

   ;;wallet-qr-code
     :done                                  "Hecho"

   ;;validation
   :invalid-phone                         "Número de teléfono inválido"
   :amount                                "Cantidad"

   ;;transactions
   :confirm                               "Confirmar"
     :transaction                           "Transacción"
   :unsigned-transaction-expired          "Transacción no firmada caducada"
   :status                                "Estado"
   :recipient                             "Destinatario"
   :to                                    "De"
   :from                                  "Para"
   :data                                  "Dato"
   :got-it                                "Muy bien"
   :block                                 "Block"
   :hash                                  "Hash"
   :gas-limit                             "limite de Gas"
   :gas-price                             "Precio del Gas"
   :gas-used                              "Gas utilizado"
   :cost-fee                              "Costo/Comisión"
   :nonce                                 "mientras tanto"
   :confirmations                         "Confirmaciones"
   :confirmations-helper-text             "Por favor espera mínimo 12 confirmaciones para asegurar que tu transacción ha sido procesada de manera segura"
   :copy-transaction-hash                 "Copiar el hash de la transacción"
   :open-on-etherscan                     "Abrir en Etherscan.io"

   ;;webview
   :web-view-error                        "Ups, un error"

   ;;testfairy warning
   :testfairy-title                       "Precaución!"
   :testfairy-message                     "Estas utilizando una aplicación instalad de una compilación nocturna. Para propósitos de prueba esta compilación incluye sesión de grabación si hay conexión wifi, por lo tanto, todas tus operaciones dentro de la aplicación son grabados (como video) y pueden ser utilizados por nuestro equipo de desarrollo para investigar posibles malfuncionamientos. video/logs guardados no incluyen tu contraseña. Las grabaciones son echas solo si la aplicación fue instalada de una compilación nocturna. Nada es grabado si la aplicación fue instalada de PlayStore o TestFlight."

   ;; wallet
   :wallet                                "Cartera"
   :wallets                               "Carteras"
   :your-wallets                          "Tus carteras"
   :main-wallet                           "Cartera principal"
   :wallet-error                          "Error cargando datos"
   :wallet-send                           "Enviar"
   :wallet-request                        "Solicitud"
   :wallet-exchange                       "Intercambiar"
   :wallet-assets                         "Bienes"
   :wallet-add-asset                      "Agregar un bien"
   :wallet-total-value                    "Valor total"
   :wallet-settings                       "Configuración de tu cartera"
   :signing-phrase-description            "Firma la transacción poniendo tu contraseña. Asegúrate que las palabras arriba son iguales a tu frase de seguridad"
   :wallet-insufficient-funds             "Fondos Insuficientes"
   :request-transaction                   "Solicitar transacción"
   :send-request                          "Enviar solicitud"
   :share                                 "Compartir"
   :eth                                   "ETH"
   :currency                              "Moneda"
   :usd-currency                          "USD"
   :transactions                          "Transacciones"
   :transaction-details                   "Detalles de la Transacción"
   :transaction-failed                    "Transacción fallida"
   :transactions-sign                     "Firmar"
   :transactions-sign-all                 "Firmar todo"
   :transactions-sign-transaction         "Firmar transacción"
   :transactions-sign-later               "Firmar después"
   :transactions-delete                   "Borrar transacción"
   :transactions-delete-content           "La Transacción será borrada de la lista 'no firmada'"
   :transactions-history                  "Historial"
   :transactions-unsigned                 "No firmada"
   :transactions-history-empty            "Aún no hay transacciones en tu historial"
   :transactions-unsigned-empty           "No tienes transacciones no firmadas"
   :transactions-filter-title             "Filtrar historial"
   :transactions-filter-tokens            "Tokens"
   :transactions-filter-type              "Tipo"
   :transactions-filter-select-all        "Seleccionar todo"
   :view-transaction-details              "Ver detalles de la transacción"
   :transaction-description               "Por favor espera al menos por 12 confirmaciones para asegurar que tu transacción fue procesada de manera segura"
   :transaction-sent                      "Transacción enviada"
   :transaction-moved-text                "La transacción permanecerá en la lista 'no firmada' por los siguientes 5 mins"
   :transaction-moved-title               "Transacción movida"
   :sign-later-title                      "¿Firmar la transacción después?"
   :sign-later-text                       "Ver el historial de la transacción para firmar esta transacción"
   :not-applicable                        "No es posible para transacciones no firmadas"

   ;; Wallet Send
   :wallet-choose-recipient               "Elegir destinatario"
   :wallet-choose-from-contacts           "Seleccionar de Contactos"
   :wallet-address-from-clipboard         "Usar Dirección del Portapapeles"
   :wallet-invalid-address                "Dirección invalida: \n {{data}}"
   :wallet-browse-photos                  "Buscar fotos"
   :validation-amount-invalid-number      "Cantidad no es un número válido"
   :validation-amount-is-too-precise      "La cantidad es muy precisa. la cantidad mínima que puedes enviar es 1 Wei(1x10^-18 ETH)"



   ;; network settings
   :new-network                           "Red nueva"
   :add-network                           "Agregar red"
   :add-new-network                       "Agregar red nueva"
   :existing-networks                     "Redes existentes"
   :add-json-file                         "Agregar archivo JSON"
   :paste-json-as-text                    "Pegar JSON como texto"
   :paste-json                            "Pegar JSON"
   :specify-rpc-url                       "Especificar un RPC URL"
   :edit-network-config                   "Editar configuración de red"
   :connected                             "Conectado"
   :process-json                          "Procesar JSON"
   :error-processing-json                 "Error procesando JSON"
   :rpc-url                               "RPC URL"
   :remove-network                        "Eliminar red"
   :network-settings                      "Configuración de redes"
   :edit-network-warning                  "Precaución, si editas los datos de la red podrías quedar fuera de la red"
   :connecting-requires-login             "Conectarse a otra red te pedirá registrarte"
   :close-app-title                       "Precaución!"
   :close-app-content                     "La aplicación se detendrá y cerrará. Al reiniciarse, la red seleccionada será utilizada"
   :close-app-button                      "Confirmar"})


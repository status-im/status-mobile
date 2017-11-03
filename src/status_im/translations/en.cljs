(ns status-im.translations.en)

(def translations
  {
   ;;common
   :members-title                         "Miembros"
   :not-implemented                       "!no implementado"
   :chat-name                             "Nombre del Chat"
   :notifications-title                   "Notificaciones y sonidos"
   :offline                               "No Conectado"
   :search-for                            "Buscar..."
   :cancel                                "Cancelar"
   :next                                  "Siguienta"
   :open                                  "Abierto"
   :description                           "Descripcion"
   :url                                   "URL"
   :type-a-message                        "Escribe un mensaje..."
   :type-a-command                        "Escribir un comando..."
   :error                                 "Error"
   :unknown-status-go-error               "status-go Error desconocido"
   :node-unavailable                      "No hay nodo de ethereum activo"
   :yes                                   "Si"
   :no                                    "No"

   :camera-access-error                   "Para garantizar los permisos a la camara, ve a configuracion del sistema y asegurate que la opcion 'Camara' esta seleccionada."
   :photos-access-error                   "Para garantizar los permisos a la camara, ve a configuracion del sistema y asegurate que la opcion 'fotos' esta seleccionada."

   ;;drawer
   :switch-users                          "Cambiar de usuario"
   :current-network                       "Red actual"

   ;;chat
   :is-typing                             "esta escribiendo"
   :and-you                               "y tu"
   :search-chat                           "Buscar chat"
   :members                               {:one   "1 miembro"
                                           :other "{{count}} miembros"
                                           :zero  "no hay miembros"}
   :members-active                        {:one   "1 miembro"
                                           :other "{{count}} miembros"
                                           :zero  "no hay miembros"}
   :public-group-status                   "Publico"
   :active-online                         "En linea"
   :active-unknown                        "Desconocido"
   :available                             "Disponible"
   :no-messages                           "No hay mensajes"
   :suggestions-requests                  "Solicitudes"
   :suggestions-commands                  "Comandos"
   :faucet-success                        "La Solicitud para el grifo ha sido recivida"
   :faucet-error                          "Error en la solicitud del grifo"

   ;;sync
   :sync-in-progress                      "Sincronizando..."
   :sync-synced                           "En syncronizacion"

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
                                           :other "segundoss"}
   :datetime-minute                       {:one   "minuto"
                                           :other "minutos"}
   :datetime-hour                         {:one   "hora"
                                           :other "horas"}
   :datetime-day                          {:one   "dia"
                                           :other "dias"}
   :datetime-ago                          "hace"
   :datetime-yesterday                    "ayer"
   :datetime-today                        "hoy"

   ;;profile
   :profile                               "Perfil"
   :edit-profile                          "Editar perfil"
   :message                               "Mensaje"
   :not-specified                         "No especificado"
   :public-key                            "Llave publica"
   :phone-number                          "Numero de telefono"
   :update-status                         "Actualiza tu estado..."
   :add-a-status                          "Agregar estado..."
   :status-prompt                         "Pon tu estado. Usando #hastags ayudaras a otros a encontrarte y opinar sobre que estas pensando"
   :add-to-contacts                       "Agregar a contactos"
   :in-contacts                           "En contactos"
   :remove-from-contacts                  "Borrar de contactos"
   :start-conversation                    "Iniciar conversacion"
   :send-transaction                      "Enviar transaccion"
   :testnet-text                          "Estas en {{testnet}} la red de prueba. No en envies ETH o SNT reales a tu direccion"
   :mainnet-text                          "Estas en la red principal. ETH real sera enviado"

   ;;make_photo
   :image-source-title                    "Imagen de perfil"
   :image-source-make-photo               "Capturar"
   :image-source-gallery                  "Seleccionar de la galeria"

   ;;sharing
   :sharing-copy-to-clipboard             "Copair al portapapeles"
   :sharing-share                         "Compartir..."
   :sharing-cancel                        "Cancelar"

   :browsing-title                        "Navegar"
   :browsing-browse                       "@navegar"
   :browsing-open-in-web-browser          "Abrir en navegador"
   :browsing-cancel                       "Cancelar"

   ;;sign-up
   :contacts-syncronized                  "Tus contactos han sido sincronizados"
   :confirmation-code                     (str "Gracias! te enviamos un mensaje de texto con el codigo de confirmacion "
                                               "Escribe dicho codigo para confirmar tu numero telefonico")
   :incorrect-code                        (str "Lo sentimos, el codigo es incorrecto, intenta nuevamente")
   :phew-here-is-your-passphrase          "Uff, fue dificil. Aqui esta tu frase de seguridad, *Escribela en un lugar seguro y respaldala por tu seguridad!* la necesitaras para recuperar tu cuenta."
   :here-is-your-passphrase               "Aqui esta tu frase de seguridad, *Escribela en un lugar seguro y respaldala por tu seguridad!* la necesitaras para recuperar tu cuenta."
   :here-is-your-signing-phrase           "Aqui esta tu frase para iniciar sesion. La usaras para verificar transacciones. *Escribela en un lugar seguro y respaldala por tu seguridad!*"
   :phone-number-required                 "Haz click aqui para validar tu numero telefonico y buscar a tus amigos."
   :shake-your-phone                      "Encontraste un error o tienes algun comentario? Solamente ~sacude~ tu telefono!"
   :intro-status                          "Platica conmigo para preparar tu cuenta y cambiar las opciones."
   :intro-message1                        "Bienvendio a Status!\nclick en este mensaje para crear tu contraseña e iniciar."
   :account-generation-message            "Espera un momentito, Hare unos calculos muy extraños para generar tu cuenta!"
   :move-to-internal-failure-message      "Necesitamos mover archivos importantes de la memoria externa a la memoria interna. Para hacerlo, necesitamos que estes de acuerdo. No utulizaremos memoria externa en versiones futuras."
   :debug-enabled                         "El servidor de depuracion ha sido activado! puedes correr *status-dev-cli scan* para encontrar el servidor desde una computadora en la misma red."

   ;;phone types
   :phone-e164                            "Internacional 1"
   :phone-international                   "Internacional 2"
   :phone-national                        "Nacional"
   :phone-significant                     "Significado"

   ;;chats
   :chats                                 "Mensajes"
   :delete-chat                           "Borrar mensajes"
   :new-group-chat                        "Nuevo grupo"
   :new-public-group-chat                 "Unirse a un grupo publico"
   :edit-chats                            "Editar mensajes"
   :search-chats                          "Buscar mensajes"
   :empty-topic                           "Topico vacio"
   :topic-format                          "Formato incorrecto [a-z0-9\\-]+"
   :public-group-topic                    "Topico"

   ;;discover
   :discover                              "Descubrur"
   :none                                  "Nada"
   :search-tags                           "Escribe tus tags a buscar aqui"
   :popular-tags                          "#hashtags Populares"
   :recent                                "Estados Recientes"
   :no-statuses-found                     "Estado no encontrado"
   :chat                                  "mensaje"
   :all                                   "Todo"
   :public-chats                          "Mensajes publicos"
   :soon                                  "Rapido"
   :public-chat-user-count                "{{count}} gente"
   :dapps                                 "ÐApps"
   :dapp-profile                          "perfil de ÐApp"
   :no-statuses-discovered                "Estado no descubierto"
   :no-statuses-discovered-body           "Cuando alguien ponga\n un estadoa lo veras aqui."
   :no-hashtags-discovered-title          "#hashtags no descubiertos"
   :no-hashtags-discovered-body           "Si un #hashtag es \nlo veras aqui."

   ;;settings
   :settings                              "Configuracion"

   ;;contacts
   :contacts                              "Contactos"
   :new-contact                           "Nuevo contacto"
   :delete-contact                        "Borrar contacto"
   :delete-contact-confirmation           "Este contacto sera borrado de tu lista de contactos"
   :remove-from-group                     "Borrar de grupo"
   :edit-contacts                         "Editar contactos"
   :search-contacts                       "Buscar contactos"
   :contacts-group-new-chat               "Iniciar platica nueva"
   :choose-from-contacts                  "Seleccionar de contactos"
   :no-contacts                           "No hay contactos aun"
   :show-qr                               "Mostrar codigo QR"
   :enter-address                         "Escribir direccion"
   :more                                  "mas"

   ;;group-settings
   :remove                                "Borrar"
   :save                                  "Grabar"
   :delete                                "Borrar"
   :clear-history                         "Borrar historial"
   :mute-notifications                    "Silenciar notificaciones"
   :leave-chat                            "Abandonar platica"
   :chat-settings                         "Configuraciones de la platica"
   :edit                                  "Editar"
   :add-members                           "Agregar miembros"

   ;;commands
   :chat-send-eth                         "{{amount}} ETH"

   ;;location command
   :your-current-location                 "Ubicacion actual"
   :places-nearby                         "Lugares cerca"
   :search-results                        "Buscar resultados"
   :dropped-pin                           "Poner marcador"
   :location                              "Ubicacion"
   :open-map                              "Abrir Mapa"
   :sharing-copy-to-clipboard-address     "Copiar la direccion"
   :sharing-copy-to-clipboard-coordinates "Copiar las coordenadas"

   ;;new-group
   :new-group                             "Grupo nuevo"
   :reorder-groups                        "Grabar grupos"
   :edit-group                            "Modificar grupo"
   :delete-group                          "Borrar grupo"
   :delete-group-confirmation             "Este grupo sera eliminado de tus grupos. No seran afectados tus contactos"
   :delete-group-prompt                   "No seran afectados tus contactos"
   :contact-s                             {:one   "contacto"
                                           :other "contactps"}

   ;;protocol
   :received-invitation                   "invitacion de platica recivida"
   :removed-from-chat                     "Has sido borrado del grupo de platica"
   :left                                  "abandonado"
   :invited                               "invitado"
   :removed                               "eliminado"
   :You                                   "Tu"

   ;;new-contact
   :add-new-contact                       "Agregar contacto nuevo"
   :scan-qr                               "Escanera codigo QR"
   :name                                  "Nombre"
   :address-explication                   "Tu llave publica es usada para generar una direccion de Ethereum, es una serie de numeros y letras. Puedes encontrarla facilmente en tu perfil"
   :enter-valid-public-key                "Porfavor, ingresa una llave publica valida o escanea un codigo QR"
   :contact-already-added                 "El contacto ha sido agregado"
   :can-not-add-yourself                  "No te puedes agregar a ti mismo"
   :unknown-address                       "Direccion desconocida"

   ;;login
   :connect                               "Conectado"
   :address                               "Direccion"
   :password                              "Contraseñq"
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
   :done                                  "Echo"

   ;;validation
   :invalid-phone                         "Numero de telefono no valido"
   :amount                                "Cantidad"

   ;;transactions
   :confirm                               "Confirmar"
   :transaction                           "Transaccion"
   :unsigned-transaction-expired          "Transaccion no firmada caducada"
   :status                                "Estado"
   :recipient                             "Receptor"
   :to                                    "De"
   :from                                  "Para"
   :data                                  "Dato"
   :got-it                                "Muy bien"
   :block                                 "Block"
   :hash                                  "Hash"
   :gas-limit                             "limite de Gas"
   :gas-price                             "Precio del Gas"
   :gas-used                              "Gas utilizado"
   :cost-fee                              "Costo/Comision"
   :nonce                                 "mientras tanto"
   :confirmations                         "Confirmaciones"
   :confirmations-helper-text             "Porfavor espera minimo 12 confirmaciones para asegurar que tu trasaccion a sido procesada de manera segura"
   :copy-transaction-hash                 "Copiar el hash de la transaccion"
   :open-on-etherscan                     "Abrir en Etherscan.io"

   ;;webview
   :web-view-error                        "Ups, un error"

   ;;testfairy warning
   :testfairy-title                       "Precaucion!"
   :testfairy-message                     "Estas utilazndo un aplicacion instalad de una compilacion nocturna. Para propositos de prueba esta compilacion incluye sesion de grabacion si hay coneccion wifi, por lo tanto todos tus operaciones dentro de la aplicacion son gravados (como video) y pueden ser utilizados por nuestro equipo de desarrollo para investigar posibles malfuncionamientos. video/logs guardados no incluyen tu contraseña. Las grabaciones son echas solo si la aplicacion fue instalada de una compilacion nocturna. Nada es grabado si la aplicacion fue instalada de PlayStore o TestFlight."

   ;; wallet
   :wallet                                "Carrtera"
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
   :wallet-settings                       "Configuracion de tu cartera"
   :signing-phrase-description            "Firma la transaccion poniendo tu contraseña. Asegurate que las palabras arriba son iguales a tu frase de seguridad"
   :wallet-insufficient-funds             "Fondos Insuficientes"
   :request-transaction                   "Solicitar transaccion"
   :send-request                          "Enviar solicitud"
   :share                                 "Compartir"
   :eth                                   "ETH"
   :currency                              "Moneda"
   :usd-currency                          "USD"
   :transactions                          "Transacciones"
   :transaction-details                   "Detalles de la Transaccion"
   :transaction-failed                    "Transaccion fallida"
   :transactions-sign                     "Firmar"
   :transactions-sign-all                 "Firmar todo"
   :transactions-sign-transaction         "Firmar transaccion"
   :transactions-sign-later               "Firmar despues"
   :transactions-delete                   "Borrar transaccion"
   :transactions-delete-content           "La Transaccion sera borrada de la lista 'no fimrada'"
   :transactions-history                  "Historial"
   :transactions-unsigned                 "No firmada"
   :transactions-history-empty            "Aun no hay transacciones en tu historial"
   :transactions-unsigned-empty           "No tienes transaccinoes no firmadas"
   :transactions-filter-title             "Filtrar historial"
   :transactions-filter-tokens            "Tokens"
   :transactions-filter-type              "Tipo"
   :transactions-filter-select-all        "Seleccionar todo"
   :view-transaction-details              "Ver detalles de la transaccion"
   :transaction-description               "Porfavor espera al menos por 12 confirmaciones para asegurar que tu transaccion fue procesada de manera segura"
   :transaction-sent                      "Transaccion enviada"
   :transaction-moved-text                "La transaccion permanecera en la lista 'no firmada' por los siguientes 5 mins"
   :transaction-moved-title               "Transaccion movida"
   :sign-later-title                      "Firmar transaccion despues?"
   :sign-later-text                       "Ver el historial de la transaccion para firmar esta transaccion"
   :not-applicable                        "No es posible para transacciones no firmadas"

   ;; Wallet Send
   :wallet-choose-recipient               "Seleccionad receptor"
   :wallet-choose-from-contacts           "Seleccionar de Contactos"
   :wallet-address-from-clipboard         "Usar direccion del portapapeles"
   :wallet-invalid-address                "Direccion invalida: \n {{data}}"
   :wallet-browse-photos                  "Busacar fotos"
   :validation-amount-invalid-number      "La Cantidad no es un numero valido"
   :validation-amount-is-too-precise      "La cantidad es muy precisa. la cantidad minima que puedes enviar es 1 Wei(1x10^-18 ETH)"



   ;; network settings
   :new-network                           "Red nueva"
   :add-network                           "Agregar red"
   :add-new-network                       "Agregar red nueva"
   :existing-networks                     "Redes existentes"
   :add-json-file                         "Agregar archivo JSON"
   :paste-json-as-text                    "Pegar JSON como texto"
   :paste-json                            "Pegar JSON"
   :specify-rpc-url                       "Especificar un RPC URL"
   :edit-network-config                   "Editar la configiuracion de red"
   :connected                             "Conectado"
   :process-json                          "Procesar JSON"
   :error-processing-json                 "Error procesando JSON"
   :rpc-url                               "RPC URL"
   :remove-network                        "Elimiear red"
   :network-settings                      "Configuraion de redes"
   :edit-network-warning                  "Precaucion, si editas los datos de la red podrias quedar fuera de la red"
   :connecting-requires-login             "Connectarse a otra red te pedira registrarte"
   :close-app-title                       "Precaucion!"
   :close-app-content                     "La applicacion se detendra y cerrara. Al reiniciarse, la red seleccionada sera utilizada"
   :close-app-button                      "Confirmar"})

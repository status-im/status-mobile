(ns status-im.translations.pt-pt)

(def translations
  {
   ;common
   :members-title                         "Membros"
   :not-implemented                       "!não implementado"
   :chat-name                             "Nome no chat"
   :notifications-title                   "Notificações e sons"
   :offline                               "Offline"

   ;drawer
   :invite-friends                        "Convidar amigos"
   :faq                                   "FAQ"
   :switch-users                          "Mudar de utilizadores"

   ;chat
   :is-typing                             "está a digitar"
   :and-you                               "e você"
   :search-chat                           "Pesquisar chat"
   :members                               {:one   "1 membro"
                                           :other "{{count}} membros"
                                           :zero  "sem membros"}
   :members-active                        {:one   "1 membro, 1 ativo"
                                           :other "{{count}} membros, {{count}} ativos"
                                           :zero  "sem membros"}
   :active-online                         "Online"
   :active-unknown                        "Desconhecido"
   :available                             "Disponível"
   :no-messages                           "Sem mensagens"
   :suggestions-requests                  "Pedidos"
   :suggestions-commands                  "Comandos"

   ;sync
   :sync-in-progress                      "A sincronizar..."
   :sync-synced                           "Em sincronização"

   ;messages
   :status-sending                        "A enviar"
   :status-pending                        "Pendente"
   :status-sent                           "Enviado"
   :status-seen-by-everyone               "Visto por todos"
   :status-seen                           "Visto"
   :status-delivered                      "Entregue"
   :status-failed                         "Falhou"

   ;datetime
   :datetime-second                       {:one   "segundo"
                                           :other "segundos"}
   :datetime-minute                       {:one   "minuto"
                                           :other "minutos"}
   :datetime-hour                         {:one   "hora"
                                           :other "horas"}
   :datetime-day                          {:one   "dia"
                                           :other "dias"}
   :datetime-multiple                     "s"
   :datetime-ago                          "atrás"
   :datetime-yesterday                    "ontem"
   :datetime-today                        "hoje"

   ;profile
   :profile                               "Perfil"
   :report-user                           "DENUNCIAR O UTILIZADOR"
   :message                               "Mensagem"
   :username                              "Nome de utilizador"
   :not-specified                         "Não especificado"
   :public-key                            "Chave Pública"
   :phone-number                          "Número de telefone"
   :email                                 "E-mail"
   :profile-no-status                     "Sem estado"
   :add-to-contacts                       "Adicionar aos contactos"
   :error-incorrect-name                  "Por favor, selecione outro nome"
   :error-incorrect-email                 "E-mail incorreto"

   ;;make_photo
   :image-source-title                    "Imagem de perfil"
   :image-source-make-photo               "Capturar"
   :image-source-gallery                  "Selecionar a partir da galeria"
   :image-source-cancel                   "Cancelar"

   ;sign-up
   :contacts-syncronized                  "Os seus contactos foram sincronizados"
   :confirmation-code                     (str "Obrigado! Enviámos-lhe uma mensagem de texto com um código de "
                                               "confirmação. Por favor, forneça esse código para confirmar o seu número de telefone")
   :incorrect-code                        (str "Desculpe, o código estava incorreto. Por favor, volte a digitar")
   :generate-passphrase                   (str "Vou gerar uma frase-chave para si para poder recuperar o seu "
                                               "acesso ou log in a partir de outro dispositivo")
   :phew-here-is-your-passphrase          "*Ufa* foi complicado, aqui está a sua frase-chave, *anote-a e mantenha-a em segurança!* Vai precisar dela para recuperar a sua conta."
   :here-is-your-passphrase               "Aqui está a sua frase-chave, *anote-a e mantenha-a em segurança!* Vai precisar dela para recuperar a sua conta."
   :written-down                          "Certifique-se de que a anotou de forma segura"
   :phone-number-required                 "Toque aqui para digitar o seu número de telefone e vou encontrar os seus amigos"
   :intro-status                          "Fale comigo para configurar a sua conta e alterar as suas definições!"
   :intro-message1                        "Bem-vindo ao Status\nToque nesta mensagem para definir a sua palavra-passe e começar!"
   :account-generation-message            "Dê-me um segundo, tenho de fazer alguns cálculos doidos para gerar a sua conta!"

   ;chats
   :chats                                 "Chats"
   :new-chat                              "Novo chat"
   :new-group-chat                        "Novo chat em grupo"

   ;discover
   :discover                             "Descoberta"
   :none                                  "Nenhum"
   :search-tags                           "Digite aqui os seus tags de pesquisa"
   :popular-tags                          "Tags populares"
   :recent                                "Recente"
   :no-statuses-discovered                "Não foi descoberto nenhum estado"

   ;settings
   :settings                              "Definições"

   ;contacts
   :contacts                              "Contactos"
   :new-contact                           "Novo Contacto"
   :show-all                              "MOSTRAR TUDO"
   :contacts-group-dapps                  "ÐApps"
   :contacts-group-people                 "Pessoas"
   :contacts-group-new-chat               "Iniciar um novo chat"
   :no-contacts                           "Ainda sem contactos"
   :show-qr                               "Mostrar QR"

   ;group-settings
   :remove                                "Remover"
   :save                                  "Guardar"
   :change-color                          "Mudar de cor"
   :clear-history                         "Limpar o histórico"
   :delete-and-leave                      "Eliminar e sair"
   :chat-settings                         "Definições de chat"
   :edit                                  "Editar"
   :add-members                           "Adicionar Membros"
   :blue                                  "Azul"
   :purple                                "Roxo"
   :green                                 "Verde"
   :red                                   "Vermelho"

   ;commands
   :money-command-description             "Enviar dinheiro"
   :location-command-description          "Enviar a localização"
   :phone-command-description             "Enviar o número de telefone"
   :phone-request-text                    "Pedido de número de telefone"
   :confirmation-code-command-description "Enviar o código de confirmação"
   :confirmation-code-request-text        "Pedido de código de confirmação"
   :send-command-description              "Enviar a localização"
   :request-command-description           "Enviar o pedido"
   :keypair-password-command-description  ""
   :help-command-description              "Ajuda"
   :request                               "Pedido"
   :chat-send-eth                         "{{amount}} ETH"
   :chat-send-eth-to                      "{{amount}} ETH para {{chat-name}}"
   :chat-send-eth-from                    "{{amount}} ETH de {{chat-name}}"
   :command-text-location                 "Localização: {{address}}"
   :command-text-browse                   "Página Web de Navegação: {{webpage}}"
   :command-text-send                     "Transação: {{amount}} ETH"
   :command-text-help                     "Ajuda"

   ;new-group
   :group-chat-name                       "Nome no chat"
   :empty-group-chat-name                 "Por favor, digite um nome"
   :illegal-group-chat-name               "Por favor, selecione outro nome"

   ;participants
   :add-participants                      "Adicionar Participantes"
   :remove-participants                   "Remover os Participantes"

   ;protocol
   :received-invitation                   "recebeu um convite de chat"
   :removed-from-chat                     "removeu-o do chat em grupo"
   :left                                  "saiu"
   :invited                               "convidado"
   :removed                               "removido"
   :You                                   "Você"

   ;new-contact
   :add-new-contact                       "Adicionar novo contacto"
   :import-qr                             "Importar"
   :scan-qr                               "Digitalizar QR"
   :name                                  "Nome"
   :whisper-identity                      "Sussurar a Identidade"
   :address-explication                   "Talvez aqui devesse aparecer algum texto a explicar o que é um endereço e onde procurá-lo"
   :enter-valid-address                   "Por favor, digite um endereço válido ou digitalize um código QR"
   :contact-already-added                 "O contacto já foi adicionado"
   :can-not-add-yourself                  "Não pode adicionar-se a si mesmo"
   :unknown-address                       "Endereço desconhecido"


   ;login
   :connect                               "Ligar"
   :address                               "Endereço"
   :password                              "Palavra-passe"
   :login                                 "Login"
   :wrong-password                        "Palavra-passe errada"

   ;recover
   :recover-from-passphrase               "Recuperar a partir da frase-chave"
   :recover-explain                       "Por favor, digite a frase-chave para a sua palavra-passe recuperar o acesso"
   :passphrase                            "Frase-chave"
   :recover                               "Recuperar"
   :enter-valid-passphrase                "Por favor, digite uma frase-chave"
   :enter-valid-password                  "Por favor, digite uma palavra-passe"

   ;accounts
   :recover-access                        "Recuperar o acesso"
   :add-account                           "Adicionar conta"

   ;wallet-qr-code
   :done                                  "Concluído"
   :main-wallet                           "Carteira Principal"

   ;validation
   :invalid-phone                         "Número de telefone inválido"
   :amount                                "Montante"
   :not-enough-eth                        (str "Não há ETH suficiente no saldo "
                                               "({{balance}} ETH)")
   ;transactions
   :confirm-transactions                  {:one   "Confirmar a transação"
                                           :other "Confirmar as {{count}} transações"
                                           :zero  "Sem transações"}
   :status                                "Estado"
   :pending-confirmation                  "Confirmação pendente"
   :recipient                             "Destinatário"
   :one-more-item                         "Mais um item"
   :fee                                   "Taxa"
   :value                                 "Valor"

   ;:webview
   :web-view-error                        "ups, erro"})

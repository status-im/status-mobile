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
   :datetime-ago                          "atrás"
   :datetime-yesterday                    "ontem"
   :datetime-today                        "hoje"

   ;profile
   :profile                               "Perfil"
   :message                               "Mensagem"
   :not-specified                         "Não especificado"
   :public-key                            "Chave Pública"
   :phone-number                          "Número de telefone"
   :add-to-contacts                       "Adicionar aos contactos"

   ;;make_photo
   :image-source-title                    "Imagem de perfil"
   :image-source-make-photo               "Capturar"
   :image-source-gallery                  "Selecionar a partir da galeria"

   ;sign-up
   :contacts-syncronized                  "Os seus contactos foram sincronizados"
   :confirmation-code                     (str "Obrigado! Enviámos-lhe uma mensagem de texto com um código de "
                                               "confirmação. Por favor, forneça esse código para confirmar o seu número de telefone")
   :incorrect-code                        (str "Desculpe, o código estava incorreto. Por favor, volte a digitar")
   :phew-here-is-your-passphrase          "*Ufa* foi complicado, aqui está a sua frase-chave, *anote-a e mantenha-a em segurança!* Vai precisar dela para recuperar a sua conta."
   :here-is-your-passphrase               "Aqui está a sua frase-chave, *anote-a e mantenha-a em segurança!* Vai precisar dela para recuperar a sua conta."
   :phone-number-required                 "Toque aqui para digitar o seu número de telefone e vou encontrar os seus amigos"
   :intro-status                          "Fale comigo para configurar a sua conta e alterar as suas definições!"
   :intro-message1                        "Bem-vindo ao Status\nToque nesta mensagem para definir a sua palavra-passe e começar!"
   :account-generation-message            "Dê-me um segundo, tenho de fazer alguns cálculos doidos para gerar a sua conta!"

   ;chats
   :chats                                 "Chats"
   :new-group-chat                        "Novo chat em grupo"

   ;discover
   :discover                              "Descoberta"
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
   :contacts-group-new-chat               "Iniciar um novo chat"
   :no-contacts                           "Ainda sem contactos"
   :show-qr                               "Mostrar QR"

   ;group-settings
   :remove                                "Remover"
   :save                                  "Guardar"
   :clear-history                         "Limpar o histórico"
   :chat-settings                         "Definições de chat"
   :edit                                  "Editar"
   :add-members                           "Adicionar Membros"

   ;commands
   :chat-send-eth                         "{{amount}} ETH"

   ;new-group

   ;participants

   ;protocol
   :received-invitation                   "recebeu um convite de chat"
   :removed-from-chat                     "removeu-o do chat em grupo"
   :left                                  "saiu"
   :invited                               "convidado"
   :removed                               "removido"
   :You                                   "Você"

   ;new-contact
   :add-new-contact                       "Adicionar novo contacto"
   :scan-qr                               "Digitalizar QR"
   :name                                  "Nome"
   :address-explication                   "Talvez aqui devesse aparecer algum texto a explicar o que é um endereço e onde procurá-lo"
   :contact-already-added                 "O contacto já foi adicionado"
   :can-not-add-yourself                  "Não pode adicionar-se a si mesmo"
   :unknown-address                       "Endereço desconhecido"


   ;login
   :connect                               "Ligar"
   :address                               "Endereço"
   :password                              "Palavra-passe"
   :wrong-password                        "Palavra-passe errada"

   ;recover
   :passphrase                            "Frase-chave"
   :recover                               "Recuperar"

   ;accounts
   :recover-access                        "Recuperar o acesso"

   ;wallet-qr-code
   :done                                  "Concluído"
   :main-wallet                           "Carteira Principal"

   ;validation
   :invalid-phone                         "Número de telefone inválido"
   :amount                                "Montante"
   ;transactions
   :status                                "Estado"
   :recipient                             "Destinatário"

   ;:webview
   :web-view-error                        "ups, erro"

   :confirm                               "Confirmar"
   :phone-national                        "Nacional"
   :public-group-topic                    "Tema"
   :debug-enabled                         "Foi lançado o servidor de depuração! Agora, pode adicionar a sua DApp executando *status-dev-cli scan* do seu computador"
   :new-public-group-chat                 "Juntar-se ao chat público"
   :datetime-ago-format                   "{{number}} {{time-intervals}} {{ago}}"
   :sharing-cancel                        "Cancelar"
   :twelve-words-in-correct-order         "12 palavras na ordem correta"
   :remove-from-contacts                  "Remover dos contactos"
   :delete-chat                           "Eliminar o chat"
   :edit-chats                            "Editar chats"
   :sign-in                               "Entrar"
   :create-new-account                    "Criar nova conta"
   :sign-in-to-status                     "Entrar com o Estado"
   :got-it                                "Entendido"
   :move-to-internal-failure-message      "Precisamos de mover alguns ficheiros importantes do armazenamento externo para o interno. Para fazê-lo, precisamos da sua permissão. Não utilizaremos o armazenamento externo em versões futuras."
   :edit-group                            "Editar grupo"
   :delete-group                          "Eliminar grupo"
   :browsing-title                        "Procurar"
   :reorder-groups                        "Reordenar grupos"
   :browsing-cancel                       "Cancelar"
   :faucet-success                        "O pedido de torneira foi recebido"
   :choose-from-contacts                  "Escolher entre os contactos"
   :new-group                             "Novo grupo"
   :phone-e164                            "Internacional 1"
   :remove-from-group                     "Remover do grupo"
   :search-contacts                       "Pesquisar contactos"
   :transaction                           "Transação"
   :public-group-status                   "Público"
   :leave-chat                            "Sair do chat"
   :start-conversation                    "Iniciar a conversa"
   :topic-format                          "Formato errado [a-z0-9\\-]+"
   :enter-valid-public-key                "Por favor, introduza uma chave pública válida ou analise um código QR"
   :faucet-error                          "Erro de pedido de torneira"
   :phone-significant                     "Significativo"
   :search-for                            "Procurar por..."
   :sharing-copy-to-clipboard             "Copiar para a área de transferência"
   :phone-international                   "Internacional 2"
   :enter-address                         "Digitar o endereço"
   :send-transaction                      "Enviar a transação"
   :delete-contact                        "Apagar contacto"
   :mute-notifications                    "Silenciar as notificações"


   :contact-s                             {:one   "contacto"
                                           :other "Contactos"}
   :next                                  "Próximo"
   :from                                  "De"
   :search-chats                          "Pesquisar chats"
   :in-contacts                           "Nos contactos"

   :sharing-share                         "Partilhar..."
   :type-a-message                        "Digitar uma mensagem..."
   :type-a-command                        "Começar a digitar um comando..."
   :shake-your-phone                      "Encontrar um bug ou ter uma sugestão? Basta ~ abanar ~ o seu telefone!"
   :status-prompt                         "Crie um estado para ajudar as pessoas a saberem sobre as coisas que está a oferecer. Também pode utilizar #hashtags."
   :add-a-status                          "Adicionar um estado..."
   :error                                 "Erro"
   :edit-contacts                         "Editar contactos"
   :more                                  "mais"
   :cancel                                "Cancelar"
   :no-statuses-found                     "Nenhum estado encontrado"
   :browsing-open-in-web-browser          "Abrir no navegador Web"
   :delete-group-prompt                   "Isto não afetará os contactos"
   :edit-profile                          "Editar perfil"


   :empty-topic                           "Tema em branco"
   :to                                    "Para"
   :data                                  "Dados"})

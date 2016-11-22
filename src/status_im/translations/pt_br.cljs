(ns status-im.translations.pt-br)

(def translations
  {
   ;common
   :members-title                         "Membros"
   :not-implemented                       "!não implementado"
   :chat-name                             "Nome do bate-papo"
   :notifications-title                   "Notificações e sons"
   :offline                               "Offline"

   ;drawer
   :invite-friends                        "Convidar amigos"
   :faq                                   "Dúvidas frequentes"
   :switch-users                          "Trocar usuário"

   ;chat
   :is-typing                             "está digitando"
   :and-you                               "e você"
   :search-chat                           "Pesquisar bate-papo"
   :members                               {:one   "1 membro"
                                           :other "{{count}} membros"
                                           :zero  "nenhum membro"}
   :members-active                        {:one   "1 membro, 1 ativo"
                                           :other "{{count}} membros, {{count}} ativo"
                                           :zero  "nenhum membro"}
   :active-online                         "Online"
   :active-unknown                        "Desconhecido"
   :available                             "Disponível"
   :no-messages                           "Nenhuma mensagem"
   :suggestions-requests                  "Solicitações"
   :suggestions-commands                  "Comandos"

   ;sync
   :sync-in-progress                      "Sincronizando..."
   :sync-synced                           "Sincronizado"

   ;messages
   :status-sending                        "Enviando"
   :status-pending                        "Pendente"
   :status-sent                           "Enviado"
   :status-seen-by-everyone               "Visto por todos"
   :status-seen                           "Visto"
   :status-delivered                      "Entregue"
   :status-failed                         "Malsucedido"

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
   :report-user                           "DENUNCIAR USUÁRIO"
   :message                               "Mensagem"
   :username                              "Nome de usuário"
   :not-specified                         "Não especificao"
   :public-key                            "Chave pública"
   :phone-number                          "Número de telefone"
   :email                                 "E-mail"
   :profile-no-status                     "Nenhum status"
   :add-to-contacts                       "Adicionar aos contatos"
   :error-incorrect-name                  "Por favor, selecione outro nome"
   :error-incorrect-email                 "E-mail incorreto"

   ;;make_photo
   :image-source-title                    "Imagem do perfil"
   :image-source-make-photo               "Tirar foto"
   :image-source-gallery                  "Escolher na galeria"
   :image-source-cancel                   "Cancelar"

   ;sign-up
   :contacts-syncronized                  "Seus contatos foram sincronizados"
   :confirmation-code                     (str "Obrigado! Nós lhe enviamos uma mensagem de texto com um código de "
                                               "confirmação. Por favor, informe esse código para confirmar seu número de telefone")
   :incorrect-code                        (str "Desculpe, o código estava incorreto. Por favor, digite novamente")
   :generate-passphrase                   (str "Vou gerar uma frase secreta para você poder restaurar o seu "
                                               "acesso ou entrar a partir de outro dispositivo")
   :phew-here-is-your-passphrase          "*Ufa* isso foi difícil. Aqui está a sua frase secreta. *Anote-a e guarde-a em segurança!* Você precisará dela para recuperar a sua conta."
   :here-is-your-passphrase               "Aqui está a sua frase secreta. *Anote-a e guarde-a em segurança!* Você precisará dela para recuperar a sua conta."
   :written-down                          "Certifique-se de tê-la anotado em segurança"
   :phone-number-required                 "Toque aqui para inserir seu número de telefone e eu vou encontrar seus amigos"
   :intro-status                          "Converse comigo para configurar a sua conta e alterar suas definições!"
   :intro-message1                        "Bem-vindo ao Status\nToque nesta mensagem para definir sua senha e começar!"
   :account-generation-message            "Mê dê um segundinho. Tenho de fazer uns cálculos malucos para gerar a sua conta!"

   ;chats
   :chats                                 "Bate-papos"
   :new-chat                              "Novo bate-papo"
   :new-group-chat                        "Novo bate-papo em grupo"

   ;discover
   :discover                             "Descoberta"
   :none                                  "Nenhum(a)"
   :search-tags                           "Digite suas tags de pesquisa aqui"
   :popular-tags                          "Tags populares"
   :recent                                "Recentes"
   :no-statuses-discovered                "Nenhum status descoberto"

   ;settings
   :settings                              "Configurações"

   ;contacts
   :contacts                              "Contatos"
   :new-contact                           "Novo contato"
   :show-all                              "MOSTRAR TODOS"
   :contacts-group-dapps                  "ÐApps"
   :contacts-group-people                 "Pessoas"
   :contacts-group-new-chat               "Iniciar novo bate-papo"
   :no-contacts                           "Você ainda não tem contatos"
   :show-qr                               "Mostrar QR"

   ;group-settings
   :remove                                "Remover"
   :save                                  "Salvar"
   :change-color                          "Alterar cor"
   :clear-history                         "Apagar histórico"
   :delete-and-leave                      "Excluir e sair"
   :chat-settings                         "Configurações de bate-papo"
   :edit                                  "Editar"
   :add-members                           "Adicionar membros"
   :blue                                  "Azul"
   :purple                                "Roxo"
   :green                                 "Verde"
   :red                                   "Vermelho"

   ;commands
   :money-command-description             "Enviar dinheiro"
   :location-command-description          "Enviar localização"
   :phone-command-description             "Enviar número de telefone"
   :phone-request-text                    "Solicitação de número de telefone"
   :confirmation-code-command-description "Enviar código de confirmação"
   :confirmation-code-request-text        "Solicitação de código de confirmação"
   :send-command-description              "Enviar localização"
   :request-command-description           "Enviar solicitação"
   :keypair-password-command-description  ""
   :help-command-description              "Ajuda"
   :request                               "Solicitar"
   :chat-send-eth                         "{{amount}} ETH"
   :chat-send-eth-to                      "{{amount}} ETH para {{chat-name}}"
   :chat-send-eth-from                    "{{amount}} ETH de {{chat-name}}"
   :command-text-location                 "Localização: {{address}}"
   :command-text-browse                   "Página de navegação: {{webpage}}"
   :command-text-send                     "Transação: {{amount}} ETH"
   :command-text-help                     "Ajuda"

   ;new-group
   :group-chat-name                       "Nome do bate-papo"
   :empty-group-chat-name                 "Por favor, informe um nome"
   :illegal-group-chat-name               "Por favor, selecione outro nome"

   ;participants
   :add-participants                      "Adicionar participantes"
   :remove-participants                   "Remover participantes"

   ;protocol
   :received-invitation                   "recebeu o convite para o bate-papo"
   :removed-from-chat                     "removeu você do bate-papo em grupo"
   :left                                  "saiu"
   :invited                               "convidou"
   :removed                               "removeu"
   :You                                   "Você"

   ;new-contact
   :add-new-contact                       "Adicionar novo contato"
   :import-qr                             "Importar"
   :scan-qr                               "Escanear QR"
   :name                                  "Nome"
   :whisper-identity                      "Identidade Whisper"
   :address-explication                   "Talvez aqui deveria haver algum texto explicando o que é um endereço e onde procurá-lo"
   :enter-valid-address                   "Por favor, digite um endereço válido ou escaneie um código QR"
   :contact-already-added                 "O contato já foi adicionado"
   :can-not-add-yourself                  "Não é possível adicionar a si mesmo"
   :unknown-address                       "E-mail desconhecido"


   ;login
   :connect                               "Conectar"
   :address                               "Endereço"
   :password                              "Senha"
   :login                                 "Entrar"
   :wrong-password                        "Senha incorreta"

   ;recover
   :recover-from-passphrase               "Recuperar a partir da frase secreta"
   :recover-explain                       "Por favor, digite a frase secreta da sua senha para recuperar o acesso"
   :passphrase                            "Frase secreta"
   :recover                               "Recuperar"
   :enter-valid-passphrase                "Por favor, digite uma frase secreta"
   :enter-valid-password                  "Por favor, digite uma senha"

   ;accounts
   :recover-access                        "Recuperar o acesso"
   :add-account                           "Adicionar conta"

   ;wallet-qr-code
   :done                                  "Concluído"
   :main-wallet                           "Carteira principal"

   ;validation
   :invalid-phone                         "Número de telefone inválido"
   :amount                                "Quantia"
   :not-enough-eth                        (str "ETH insuficiente no saldo "
                                               "({{balance}} ETH)")
   ;transactions
   :confirm-transactions                  {:one   "Confirmar a transação"
                                           :other "Confirmar {{count}} transações"
                                           :zero  "Nenhuma transação"}
   :status                                "Status"
   :pending-confirmation                  "Confirmação pendente"
   :recipient                             "Destinatário"
   :one-more-item                         "Mais um item"
   :fee                                   "Tarifa"
   :value                                 "Valor"

   ;:webview
   :web-view-error                        "Opa, erro"})

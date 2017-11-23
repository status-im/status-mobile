(ns status-im.translations.pt-br)

(def translations
  {
   ;common
   :members-title                         "Membros"
   :not-implemented                       "não implementado"
   :chat-name                             "Nome do chat"
   :notifications-title                   "Notificações e sons"
   :offline                               "Offline"

   ;drawer
   :switch-users                          "Trocar usuário"

   ;chat
   :is-typing                             "está digitando"
   :and-you                               "e você"
   :search-chat                           "Pesquisar chat"
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
   :status-sending                        "Enviando..."
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
   :datetime-ago                          "atrás"
   :datetime-yesterday                    "ontem"
   :datetime-today                        "hoje"

   ;profile
   :profile                               "Perfil"
   :message                               "Mensagem"
   :not-specified                         "Não especificado"
   :public-key                            "Chave pública"
   :phone-number                          "Número de telefone"
   :add-to-contacts                       "Adicionar aos contatos"

   ;;make_photo
   :image-source-title                    "Imagem do perfil"
   :image-source-make-photo               "Tirar foto"
   :image-source-gallery                  "Escolher na galeria"

   ;sign-up
   :contacts-syncronized                  "Seus contatos foram sincronizados"
   :confirmation-code                     (str "Obrigado! Nós lhe enviamos uma mensagem de texto com um código de "
                                               "confirmação. Por favor, informe esse código para confirmar seu número de telefone")
   :incorrect-code                        (str "Desculpe, o código estava incorreto. Por favor, digite novamente")
   :phew-here-is-your-passphrase          "*Ufa* isso foi difícil. Aqui está a sua frase secreta. *Anote-a e guarde-a em segurança!* Você precisará dela para recuperar a sua conta."
   :here-is-your-passphrase               "Aqui está a sua frase secreta. *Anote-a e guarde-a em segurança!* Você precisará dela para recuperar a sua conta."
   :phone-number-required                 "Toque aqui para inserir seu número de telefone e eu vou encontrar seus amigos"
   :intro-status                          "Converse comigo para configurar a sua conta e alterar suas definições!"
   :intro-message1                        "Bem-vindo ao Status\nToque nesta mensagem para definir sua senha e começar!"
   :account-generation-message            "Mê dê um segundinho. Tenho de fazer uns cálculos malucos para gerar a sua conta!"

   ;chats
   :chats                                 "Chats"
   :new-group-chat                        "Novo grupo de chat"

   ;discover
   :discover                              "Descobrir"
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
   :contacts-group-new-chat               "Iniciar novo chat"
   :no-contacts                           "Você ainda não tem contatos"
   :show-qr                               "Mostrar QR"

   ;group-settings
   :remove                                "Remover"
   :save                                  "Salvar"
   :clear-history                         "Apagar histórico"
   :chat-settings                         "Configurações do chat"
   :edit                                  "Editar"
   :add-members                           "Adicionar membros"

   ;commands
   :chat-send-eth                         "{{amount}} ETH"

   ;new-group

   ;participants

   ;protocol
   :received-invitation                   "recebeu o convite para o chat"
   :removed-from-chat                     "removeu você do grupo"
   :left                                  "saiu"
   :invited                               "convidou"
   :removed                               "removeu"
   :You                                   "Você"

   ;new-contact
   :add-new-contact                       "Adicionar novo contato"
   :scan-qr                               "Escanear QR"
   :name                                  "Nome"
   :address-explication                   "Talvez aqui deveria haver algum texto explicando o que é um endereço e onde procurá-lo"
   :contact-already-added                 "O contato já foi adicionado"
   :can-not-add-yourself                  "Não é possível adicionar a si mesmo"
   :unknown-address                       "E-mail desconhecido"


   ;login
   :connect                               "Conectar"
   :address                               "Endereço"
   :password                              "Senha"
   :wrong-password                        "Senha incorreta"

   ;recover
   :passphrase                            "Frase secreta"
   :recover                               "Recuperar"

   ;accounts
   :recover-access                        "Recuperar o acesso"

   ;wallet-qr-code
   :done                                  "Concluído"
   :main-wallet                           "Carteira principal"

   ;validation
   :invalid-phone                         "Número de telefone inválido"
   :amount                                "Quantia"
   ;transactions
   :status                                "Status"
   :recipient                             "Destinatário"

   ;:webview
   :web-view-error                        "Ops, erro"

   :confirm                               "Confirmar"
   :phone-national                        "Nacional"
   :public-group-topic                    "Assunto"
   :debug-enabled                         "O servidor de debug foi inicializado! Você agora pode adicionar seu DApp ao executar *status-dev-cli scan* a partir de seu computador."
   :new-public-group-chat                 "Juntar-se a bate-papo público."
   :datetime-ago-format                   "{{number}} {{time-intervals}} {{ago}}"
   :sharing-cancel                        "Cancelar"
   :twelve-words-in-correct-order         "12 palavras na ordem correta"
   :remove-from-contacts                  "Remover dos contatos"
   :delete-chat                           "Excluir bate-papo"
   :edit-chats                            "Editar bate-papos"
   :sign-in                               "Entrar"
   :create-new-account                    "Criar nova conta"
   :sign-in-to-status                     "Entrar para Status"
   :got-it                                "Entendido"
   :move-to-internal-failure-message      "Precisamos mover alguns arquivos importantes do armazenamento externo para o interno. Para fazer isso, precisamos de sua permissão. Não utilizaremos armazenamento externo em versões futuras."
   :edit-group                            "Editar grupo"
   :delete-group                          "Excluir grupo"
   :browsing-title                        "Buscar"
   :reorder-groups                        "Reorganizar grupos"
   :browsing-cancel                       "Cancelar"
   :faucet-success                        "Requisição faucet foi recebida"
   :choose-from-contacts                  "Selecionar a partir dos contatos"
   :new-group                             "Novo grupo"
   :phone-e164                            "Internacional 1"
   :remove-from-group                     "Remover do grupo"
   :search-contacts                       "Buscar contatos"
   :transaction                           "Transação"
   :public-group-status                   "Público"
   :leave-chat                            "Sair do bate-papo"
   :start-conversation                    "Iniciar conversa"
   :topic-format                          "Formato errado [a-z0-9\\-]+"
   :enter-valid-public-key                "Por favor, insira uma chave pública válida ou capture um código QR"
   :faucet-error                          "Erro na requisição faucet"
   :phone-significant                     "Significante"
   :search-for                            "Buscar por..."
   :sharing-copy-to-clipboard             "Copiar para área de transferência"
   :phone-international                   "Internacional 2"
   :enter-address                         "Inserir endereço"
   :send-transaction                      "Enviar transação"
   :delete-contact                        "Excluir contato"
   :mute-notifications                    "Silenciar notificações"


   :contact-s                             {:one   "contato"
                                           :other "contatos"}
   :next                                  "Próximo"
   :from                                  "De"
   :search-chats                          "Buscar bate-papos"
   :in-contacts                           "Em contatos"

   :sharing-share                         "Compartilhar..."
   :type-a-message                        "Digite uma mensagem..."
   :type-a-command                        "Comece a digitar um comando..."
   :shake-your-phone                      "Encontrou um erro ou tem uma sugestão? Basta ~agitar~ seu telefone!"
   :status-prompt                         "Crie um status para ajudar as pessoas a saberem sobre as coisas que você está oferecendo. Você também pode utilizar #hashtags."
   :add-a-status                          "Adicionar um status..."
   :error                                 "Erro"
   :edit-contacts                         "Editar contatos"
   :more                                  "mais"
   :cancel                                "Cancelar"
   :no-statuses-found                     "Nenhum status encontrado"
   :browsing-open-in-web-browser          "Abrir no navegador de internet"
   :delete-group-prompt                   "Isso não afetará os contatos"
   :edit-profile                          "Editar perfil"


   :empty-topic                           "Assunto vazio"
   :to                                    "Para"
   :data                                  "Dados"})

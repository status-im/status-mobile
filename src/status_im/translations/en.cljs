(ns status-im.translations.en)

(def translations
  {
   ;;common
   :members-title                         "Membres"
   :not-implemented                       "!Non disponible"
   :chat-name                             "Pseudo"
   :notifications-title                   "Notifications et sons"
   :offline                               "Horsligne"
   :search-for                            "Recherche..."
   :cancel                                "Annuler"
   :next                                  "Suivant"
   :open                                  "Ouvert"
   :description                           "Description"
   :url                                   "URL"
   :type-a-message                        "Ecriver un message"
   :type-a-command                        "Commencez à écrire un message"
   :error                                 "Érreure"
   :unknown-status-go-error               "Érreure inconnu de Statu-go"
   :node-unavailable                      "Aucun noeud ethereum en cours d'exécution"
   :yes                                   "Oui "
   :no                                    "Non"

   :camera-access-error                   "Merci d'aller dans vos paramètres système et de vous assurer que Status > Caméra est sélectionné afin d'accorder la permission d'utiliser votre caméra."
   :photos-access-error                   "Merci d'aller dans vos paramètres système et de vous assurer que Status > Photos est sélectionné afin d'accorder la permission d'utiliser vos photos."

   ;;drawer
   :switch-users                          "Change d'utilisateurs"
   :current-network                       "Réseau actuel"

   ;;chat
   :is-typing                             "Écrit..."
   :and-you                               "Et vous"
   :search-chat                           "Recherche conversation"
   :members                               {:on  "1 membre"
                                           :other "{{comte}} membres"
                                           :zero  "aucun membres"}
   :members-active                        {:one   "1 membre"
                                           :other "{{compte}} membres"
                                           :zero  "aucun membres"}
   :public-group-status                   "Publique"
   :active-online                         "En ligne"
   :active-unknown                        "Inconnu"
   :available                             "Disponible"
   :no-messages                           "Aucun messages"
   :suggestions-requests                  "Demande"
   :suggestions-commands                  "Commandes"
   :faucet-success                        "La demande faucet a été reçue"
   :faucet-error                          "Érreure avec la demande faucet"

   ;;sync
   :sync-in-progress                      "Synchronisation..."
   :sync-synced                           "Synchronisé"

   ;;messages
   :status-pending                        "Envoi en cours..."
   :status-sent                           "Envoyé"
   :status-seen-by-everyone               "Vu par tous"
   :status-seen                           "Vu"
   :status-delivered                      "Délivré"
   :status-failed                         "Échec"

   ;;datetime
   :datetime-ago-format                   "{{nombre}} {{time-intervals}} {{ago}}"
   :datetime-second                       {:one   "seconde"
                                           :other "secondes"}
   :datetime-minute                       {:one   "minute"
                                           :other "minutes"}
   :datetime-hour                         {:one   "heure"
                                           :other "heures"}
   :datetime-day                          {:one   "jour"
                                           :other "jours"}
   :datetime-ago                          "Il y a"
   :datetime-yesterday                    "hier"
   :datetime-today                        "Aujourd'hui"

   ;;profile
   :profile                               "Profil"
   :edit-profile                          "Modifier profil"
   :message                               "Message"
   :not-specified                         "Non spécifique"
   :public-key                            "Clé publique"
   :phone-number                          "Numéro de téléphone"
   :update-status                         "Actualiser votre Status"
   :add-a-status                          "Ajouter un Status"
   :status-prompt                         "Créez un nouveau statut pour partager vos intérêts avec tout le monde. Vous pouvez aussi utiliser les #hashtags."
   :add-to-contacts                       "Ajouter aux contactes"
   :in-contacts                           "Dans les contactes"
   :remove-from-contacts                  "Retirer des contactes"
   :start-conversation                    "Débuter une conversation"
   :send-transaction                      "Envoyer la transaction"
   :testnet-text                          "Vous êtes sur le {{testnet}} Testnet. N'envoyez pas de véritable ETH ou SNT à votre adresse"
   :mainnet-text                          "Vous êtes sur Mainet. De véritable ETH vont être envoyé"

   ;;make_photo
   :image-source-title                    "Photo de profile"
   :image-source-make-photo               "Capturé"
   :image-source-gallery                  "Sélectionner de la gallerie"

   ;;sharing
   :sharing-copy-to-clipboard             "Copier vers le presse-papiers"
   :sharing-share                         "Partager..."
   :sharing-cancel                        "Annuler"

   :browsing-title                        "Naviguer"
   :browsing-browse                       "@Naviguer"
   :browsing-open-in-web-browser          "Ouvrir dans le navigateur web"
   :browsing-cancel                       "Annuler"

   ;;sign-up
   :contacts-syncronized                  "Vos contactes on été synchronisés"
   :confirmation-code                     (str "Merci! Nous vous avons envoyé un message textes avec une comfirmation "
                                               "Code. Veuillez fournir ce code pour comfirmer votre numéro de téléphone")
   :incorrect-code                        (str "Désolé ce code est incorrect, veuillez saisir à nouveau")
   :phew-here-is-your-passphrase          "Ouff, c'était difficile. Voiçi votre phrase secrète *Écrivez-la quelque part et garder-la en lieu sûr!* Vous en aurez besoin pour récupérer votre compte."
   :here-is-your-passphrase               "Voici votre phrase secrète, *écrivez-la et gardez-la en lieu sûr !* Vous en aurez besoin pour restaurer votre compte."
   :here-is-your-signing-phrase           "Voici votre phrase de signature. Vous en aurez besoin pour vérifer votre transaction *Écrivez-la quelque part et garder-la en lieu sûr!*"
   :phone-number-required                 "Appuyez ici pour entrer votre numéro de téléphone et je vais trouver vos amis"
   :shake-your-phone                      "Vous avez trouvé un bug ou alors avez une suggestion? ~Secouez~ simplement votre téléphone!"
   :intro-status                          "Chattez avec moi pour configurer votre compte et modifier vos paramètres !"
   :intro-message1                        "Bienvenue dans Status\nTapez sur ce message pour définir votre mot de passe et commencer !."
   :account-generation-message            "Donnez-moi une seconde, je dois faire quelques calculs de ouf pour générer votre compte !"
   :move-to-internal-failure-message      "Nous devons déplacer des fichiers importants depuis un stockage extérieur vers un stockage local. Afin de faire cela, nous avons besoin de votre permission. Nous n'utiliserons plus de stockage extérieur dans nos futures versions."
   :debug-enabled                         "Le serveur Debug a été lancé! Vous pouvez désormais utiliser *status-dev-cli scan* pour trouver le serveur depuis votre ordinateur sur le même réseau."

   ;;phone types
   :phone-e164                            "International 1"
   :phone-international                   "International 2"
   :phone-national                        "National"
   :phone-significant                     "Significatif"

   ;;chats
   :chats                                 "Chats"
   :delete-chat                           "Suprimer le chat"
   :new-group-chat                        "Nouveau chat de groupe"
   :new-public-group-chat                 "Rejoindre un chat prublique"
   :edit-chats                            "Modifier le chat"
   :search-chats                          "Rechercher un chat"
   :empty-topic                           "Sujet vide"
   :topic-format                          "Mauvais format [a-z0-9\\-]+"
   :public-group-topic                    "Sujet"

   ;;discover
   :discover                              "Découvrir"
   :none                                  "Aucun"
   :search-tags                           "Entrez vos mots clés ici"
   :popular-tags                          "#hashtags populaires"
   :recent                                "Statut récent"
   :no-statuses-found                     "Aucun statuts trouvés"
   :chat                                  "Chat"
   :all                                   "Tous"
   :public-chats                          "Chats publiques"
   :soon                                  "Bientôt"
   :public-chat-user-count                "{{count}} Gens"
   :dapps                                 "ÐApps"
   :dapp-profile                          "ÐApp profile"
   :no-statuses-discovered                "Aucun Status découvert"
   :no-statuses-discovered-body           "Quand quelqun poste\na status, vous le verré içi."
   :no-hashtags-discovered-title          "Aucun #hashtags découvert"
   :no-hashtags-discovered-body           "Quand un #hashtag devient \npopular vous le verré içi"

   ;;settings
   :settings                              "Paramètres"

   ;;contacts
   :contacts                              "Contacts"
   :new-contact                           "Nouveau contact"
   :delete-contact                        "Suprimer le contact"
   :delete-contact-confirmation           "Ce contact sera supprimé de vos contacts"
   :remove-from-group                     "Supprimé du contact"
   :edit-contacts                         "Modifier le contact"
   :search-contacts                       "Rechercher contacts"
   :contacts-group-new-chat               "Débuter un nouveau chat"
   :choose-from-contacts                  "Choisir depuis les contacts"
   :no-contacts                           "Aucun contact pour l'instant"
   :show-qr                               "Afficher le code QR"
   :enter-address                         "Entre l'adresse"
   :more                                  "plus"

   ;;group-settings
   :remove                                "Suprimer"
   :save                                  "Sauvregarder"
   :delete                                "Suprimer"
   :clear-history                         "Éffacer l'historique"
   :mute-notifications                    "Couper le son des modifications"
   :leave-chat                            "Quitter le chat"
   :chat-settings                         "Paramètre du chat"
   :edit                                  "Modifer"
   :add-members                           "Ajouter membres"

   ;;commands
   :chat-send-eth                         "{{amount}} ETH"

   ;;location command
   :your-current-location                 "Votre location actuelle"
   :places-nearby                         "Endroits dans les alentour"
   :search-results                        "Résultat de la recherche"
   :dropped-pin                           "Épinglé"
   :location                              "Location"
   :open-map                              "Ovrir la carte"
   :sharing-copy-to-clipboard-address     "Copier l'adresse"
   :sharing-copy-to-clipboard-coordinates "Copier les coordonnés"

   ;;new-group
   :new-group                             "Nouveau groupe"
   :reorder-groups                        "Réorganiser les groupes"
   :edit-group                            "modifier le groupe"
   :delete-group                          "Supprimez"
   :delete-group-confirmation             "Ce groupe sera enlevé de vos groupes. Cela n'affectera pas vos contacts"
   :delete-group-prompt                   "Cela n'affectera pas vos contacts"
   :contact-s                             {:one   "contact"
                                           :other "contacts"}

   ;;protocol
   :received-invitation                   "Invitation de chat reçue"
   :removed-from-chat                     "vous avez été retiré du chat de groupe"
   :left                                  "est parti(e)"
   :invited                               "invité"
   :removed                               "Supprimé"
   :You                                   "Vous"

   ;;new-contact
   :add-new-contact                       "Ajouter un nouveau contact"
   :scan-qr                               "Scanner le code QR"
   :name                                  "Nom"
   :address-explication                   "Votre clé publique est utilisé pour générer votre adresse sur Ethereum et est un série de chiffres et de lettres. Vous pouvez le trouver facilement dans votre profile."
   :enter-valid-public-key                "Veuillez écrire une clé publique valide ou scanner votre code QR."
   :contact-already-added                 "Le contact a déjà été ajouté"
   :can-not-add-yourself                  "Vous ne pouvez pas vous ajouter vous même"
   :unknown-address                       "Adresse inconnue"

   ;;login
   :connect                               "Ce connecter"
   :address                               "Addresse"
   :password                              "Mot de passe"
   :sign-in-to-status                     "S'inscrire à Status"
   :sign-in                               "Inscription"
   :wrong-password                        "Mot de passe incorrect"
   :enter-password                        "Enter password"

   ;;recover
   :passphrase                            "Phrase secrète"
   :recover                               "Restaurer"
   :twelve-words-in-correct-order         "12 mots dans l'ordre approprié"

   ;;accounts
   :recover-access                        "Restaurer l'accès"
   :create-new-account                    "Crée un nouveau compte"

   ;;wallet-qr-code
   :done                                  "Terminer"

   ;;validation
   :invalid-phone                         "Numéro de téléphone invalide"
   :amount                                "Quantité"

   ;;transactions
   :confirm                               "Confirmer"
   :transaction                           "Transaction"
   :unsigned-transaction-expired          "Transaction expirée non signé"
   :status                                "Statuts"
   :recipient                             "bénéficiaire"
   :to                                    "À"
   :from                                  "De"
   :data                                  "Donné"
   :got-it                                "Je l'ai"
   :block                                 "Bloc"
   :hash                                  "Hash"
   :gas-limit                             "Limite de gaz"
   :gas-price                             "Prix du gaz"
   :gas-used                              "Gaz utilisé"
   :cost-fee                              "Coût/frais"
   :nonce                                 "Nonce"
   :confirmations                         "Confirmations"
   :confirmations-helper-text             "Veuillez attendre au moins 12 confirmations pour vous assurer que votre transaction est traitée en toute sécurité"
   :copy-transaction-hash                 "Copier le hash de transaction"
   :open-on-etherscan                     "Ouvrir sur Etherscan.io"

   ;;webview
   :web-view-error                        "oops, erreure"

   ;;testfairy warning
   :testfairy-title                       "Attention!"
   :testfairy-message                     "Vous utilisez une application installée à partir d'une version nocturne. À des fins de test, cette version inclut l'enregistrement de session si la connexion Wi-Fi est utilisée, ainsi toutes vos interactions avec cette application sont sauvegardées (vidéo et journaux) et pourraient être utilisées par notre équipe de développement pour enquêter sur d'éventuels problèmes. Les vidéos / journaux enregistrés n'incluent pas vos mots de passe. L'enregistrement est effectué uniquement si l'application est installée à partir d'une version nocturne. Rien n'est enregistré si l'application est installée depuis PlayStore ou TestFlight. "

   ;; wallet
   :wallet                                "Portefeuille"
   :wallets                               "Portefeuilles"
   :your-wallets                          "Votre portefeuille"
   :main-wallet                           "Portefeuille principal"
   :wallet-error                          "Erreur lors du chargement des données"
   :wallet-send                           "Envoyé"
   :wallet-request                        "Demande"
   :wallet-exchange                       "Échange"
   :wallet-assets                         "Atouts"
   :wallet-add-asset                      "Ajouter des atouts"
   :wallet-total-value                    "Valeur total"
   :wallet-settings                       "Paramètre du portefeuille"
   :signing-phrase-description            "Signez la transaction en entrant votre mot de passe. Assurez-vous que les mots ci-dessus correspondent à votre phrase de signature secrète"
   :wallet-insufficient-funds             "Fond insuffisants"
   :request-transaction                   "Demande de transaction"
   :send-request                          "Envoyer la transaction"
   :share                                 "Partager"
   :eth                                   "ETH"
   :currency                              "Devise"
   :usd-currency                          "USD"
   :transactions                          "Transactions"
   :transaction-details                   "Détails de Transaction "
   :transaction-failed                    "La transaction a échoué"
   :transactions-sign                     "Signer"
   :transactions-sign-all                 "Signer tous"
   :transactions-sign-transaction         "Sign la transaction"
   :transactions-sign-later               "Sign plus tard"
   :transactions-delete                   "Supprimer la transaction"
   :transactions-delete-content           "La transaction sera retirée de la liste 'Non signée'"
   :transactions-history                  "Historique"
   :transactions-unsigned                 "Non signé"
   :transactions-history-empty            "Aucune transaction dans votre historique pour l'instant"
   :transactions-unsigned-empty           "Vous avez aucune transactions non signée"
   :transactions-filter-title             "Filtrer l'historique"
   :transactions-filter-tokens            "Jetons"
   :transactions-filter-type              "Type"
   :transactions-filter-select-all        "sélection tout"
   :view-transaction-details              "Voir les détails de transaction"
   :transaction-description               "Veuillez attendre au moins 12 confirmations pour vous assurer que votre transaction est traitée en toute sécurité"
   :transaction-sent                      "Transaction envoyé"
   :transaction-moved-text                "La transaction restera dans la liste 'Non signée' pour les 5 prochaines minutes"
   :transaction-moved-title               "Transaction déplacée"
   :sign-later-title                      "Signé la transaction plus tard?"
   :sign-later-text                       "Vérifiez l'historique des transactions pour signer celle-çi"
   :not-applicable                        "Non applicable pour les transactions non signées"

   ;; Wallet Send
   :wallet-choose-recipient               "Selectionner le bénéficiaire"
   :wallet-choose-from-contacts           "Choisissez parmi les contacts"
   :wallet-address-from-clipboard         "Utiliser l'adresse du presse-papiers"
   :wallet-invalid-address                "Adresse invalide: \n {{data}}"
   :wallet-browse-photos                  "Parcourir les photos"
   :validation-amount-invalid-number      "Le montant n'est pas un nombre valide"
   :validation-amount-is-too-precise      "Le montant est trop précis. La plus petite unité que vous pouvez envoyer est 1 Wei (1x10^-18 ETH)"



   ;; network settings
   :new-network                           "Nouveau réseau"
   :add-network                           "Ajouter un réseau"
   :add-new-network                       "Ajouter un nouveau réseau"
   :existing-networks                     "Réseau existant"
   :add-json-file                         "Ajouter un fichier JSON"
   :paste-json-as-text                    "Copier JSON comme texte"
   :paste-json                            "Copier JSON"
   :specify-rpc-url                       "Spécifier un RPC URL"
   :edit-network-config                   "Modifier les configurations du réseau"
   :connected                             "Connecté"
   :process-json                          "Traité le JSON"
   :error-processing-json                 "Érreur au cours du traitement du JSON"
   :rpc-url                               "RPC URL"
   :remove-network                        "Supprimer le reseau"
   :network-settings                      "Paramètre du réseau"
   :edit-network-warning                  "Attention, l'édition des données réseau peut désactiver ce réseau pour vous"
   :connecting-requires-login             "La connexion à un autre réseau nécessite une connexion"
   :close-app-title                       "Attention!"
   :close-app-content                     "L'application va s'arrêter et se fermer. Lorsque vous le réouvrez, le réseau sélectionné sera utilisé"
   :close-app-button                      "Confirmation"})

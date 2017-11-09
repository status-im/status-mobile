(ns status-im.translations.fr)

(def translations
  {
   ;;common
   :members-title                         "Membres"
   :not-implemented                       "! Pas disponible"
   :chat-name                             "Pseudo"
   :notifications-title                   "Notifications et sons"
   :offline                               "Hors ligne"
   :search-for                            "Rechercher..."
   :cancel                                "Annuler"
   :next                                  "Suivant"
   :open                                  "Ouvrir"
   :description                           "Description"
   :url                                   "Lien"
   :type-a-message                        "Saisissez un message..."
   :type-a-command                        "Saisissez une commande..."
   :error                                 "Erreur"
   :unknown-status-go-error               "Erreur status-go inconnue"
   :node-unavailable                      "Pas de nœud ethereum disponible"
   :yes                                   "Oui"
   :no                                    "Non"

   :camera-access-error                   "Merci d'aller dans vos paramètres système et de vous assurer que Status > Caméra est sélectionné afin d'accorder la permission d'utiliser votre caméra."
   :photos-access-error                   "Merci d'aller dans vos paramètres système et de vous assurer que Status > Photos est sélectionné afin d'accorder la permission d'utiliser vos photos."

   ;;drawer
   :switch-users                          "Changer d'utilisateur"
   :current-network                       "Réseau actuel"

   ;;chat
   :is-typing                             "écrit..."
   :and-you                               "et vous"
   :search-chat                           "Rechercher un chat"
   :members                               {:one   "1 membre"
                                           :other "{{count}} membres"
                                           :zero  "Aucun membre"}
   :members-active                        {:one   "1 membre, 1 actif"
                                           :other "{{count}} membres, {{count}} actifs"
                                           :zero  "Aucun membre"}
   :public-group-status                   "Public"
   :active-online                         "En ligne"
   :active-unknown                        "Inconnu"
   :available                             "Disponible"
   :no-messages                           "Pas de messages"
   :suggestions-requests                  "Demandes"
   :suggestions-commands                  "Commandes"
   :faucet-success                        "La demande de Faucet a été reçue"
   :faucet-error                          "Erreur avec la demande de Faucet"

   ;;sync
   :sync-in-progress                      "Synchronisation en cours..."
   :sync-synced                           "Synchronisé"

   ;messages
   :status-sending                        "Envoi en cours..."
   :status-pending                        "En attente..."
   :status-sent                           "Envoyé"
   :status-seen-by-everyone               "Vu par tout le monde"
   :status-seen                           "Vu"
   :status-delivered                      "Délivré"
   :status-failed                         "Échec"

   ;datetime
   :datetime-ago-format                   "{{number}} {{time-intervals}} {{ago}}"
   :datetime-second                       {:one   "seconde"
                                           :other "secondes"}
   :datetime-minute                       {:one   "minute"
                                           :other "minutes"}
   :datetime-hour                         {:one   "heure"
                                           :other "heures"}
   :datetime-day                          {:one   "jour"
                                           :other "jours"}
   :datetime-ago                          "il y a"
   :datetime-yesterday                    "hier"
   :datetime-today                        "aujourd'hui"

   ;;profile
   :profile                               "Profil"
   :edit-profile                          "Modifier profil"
   :message                               "Message"
   :not-specified                         "Non spécifié"
   :public-key                            "Clé publique"
   :phone-number                          "Numéro de téléphone"
   :update-status                         "Actualisez votre statut..."
   :add-a-status                          "Ajoutez un statut..."
   :status-prompt                         "Créez un nouveau statut pour partager vos intérêts avec tout le monde. Vous pouvez aussi utiliser les #hashtags."
   :add-to-contacts                       "Ajouter aux contacts"
   :in-contacts                           "Dans les contacts"
   :remove-from-contacts                  "Supprimer des contacts"
   :start-conversation                    "Débuter une conversation"
   :send-transaction                      "Envoyer une transaction"
   :testnet-text                          "Vous êtes connecté au {{testnet}} Testnet. N'envoyez pas de vrais ETH ou de SNT à votre adresse"
   :mainnet-text                          "Vous êtes connecté au Mainnet. De vrais ETH seront envoyés"

   ;;make_photo
   :image-source-title                    "Photo de profil"
   :image-source-make-photo               "Capturer"
   :image-source-gallery                  "Sélectionner dans la galerie"

   ;;sharing
   :sharing-copy-to-clipboard             "Copier vers le presse-papiers"
   :sharing-share                         "Partager..."
   :sharing-cancel                        "Annuler"

   :browsing-title                        "Naviguer"
   :browsing-browse                       "@naviguer"
   :browsing-open-in-web-browser          "Ouvrir dans le navigateur"
   :browsing-cancel                       "Annuler"

   ;;sign-up
   :contacts-syncronized                  "Vos contacts ont été synchronisés"
   :confirmation-code                     (str "Merci ! Nous vous avons envoyé un message texte contenant le code "
                                               "de confirmation. Veuillez fournir ce code pour confirmer votre numéro de téléphone")
   :incorrect-code                        (str "Désolé, le code est incorrect. Veuillez le saisir à nouveau.")
   :generate-passphrase                   (str "Je vais créer une phrase secrète pour vous permettre de restaurer votre "
                                               "accès ou de vous connecter depuis un autre périphérique")
   :phew-here-is-your-passphrase          "*Ouf* c'était dur, voici votre phrase secrète, *écrivez-la et gardez-la en lieu sûr !* Vous en aurez besoin pour restaurer votre compte."
   :here-is-your-passphrase               "Voici votre phrase secrète, *écrivez-la et gardez-la en lieu sûr !* Vous en aurez besoin pour restaurer votre compte."
   :here-is-your-signing-phrase           "Ceci est votre phrase de signature. Vous l'utiliserez pour verifier vos transactions. *Ecrivez-la et gardez-la en sécurité!*"
   :phone-number-required                 "Appuyez ici pour entrer votre numéro de téléphone et je vais trouver vos amis"
   :shake-your-phone                      "Vous avez trouvé un bug ou alors avez une suggestion? ~Secouez~ simplement votre téléphone!"
   :intro-status                          "Chattez avec moi pour configurer votre compte et modifier vos paramètres !"
   :intro-message1                        "Bienvenue dans Status\nTapez sur ce message pour définir votre mot de passe et commencer !"
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
   :delete-chat                           "Supprimer chat"
   :new-group-chat                        "Nouveau chat de groupe"
   :new-public-group-chat                 "Rejoindre chat public"
   :edit-chats                            "Modifier chats"
   :search-chats                          "Rechercher chats"
   :empty-topic                           "Sujet indéfini"
   :topic-format                          "Mauvais format [a-z0-9\\-]+"
   :public-group-topic                    "Sujet"

   ;;discover
   :discover                              "Découvrir"
   :none                                  "Aucun"
   :search-tags                           "Entrez vos mots clés ici"
   :popular-tags                          "Mots-clés populaires"
   :recent                                "Récent"
   :no-statuses-found                     "Aucun statut trouvé"
   :chat                                  "Chat"
   :all                                   "Tous"
   :public-chats                          "Chats publiques"
   :soon                                  "Bientôt"
   :public-chat-user-count                "{{count}} personnes"
   :dapps                                 "ÐApps"
   :dapp-profile                          "Profil de la ÐApp"
   :no-statuses-discovered                "Aucun statut trouvé"
   :no-statuses-discovered-body           "Quand quelqu'un met\nun statut, vous le verrez ici."
   :no-hashtags-discovered-title          "Aucun #hashtag découvert"
   :no-hashtags-discovered-body           "Quand un #hashtag devient\npopulaire, vous le verrez ici."

   ;;settings
   :settings                              "Paramètres"

   ;;contacts
   :contacts                              "Contacts"
   :new-contact                           "Nouveau contact"
   :delete-contact                        "Supprimer contact"
   :delete-contact-confirmation           "Ce contact sera supprimé de vos contacts"
   :remove-from-group                     "Supprimer ce groupe"
   :edit-contacts                         "Modifier contacts"
   :search-contacts                       "Rechercher contacts"
   :contacts-group-new-chat               "Démarrer un nouveau chat"
   :choose-from-contacts                  "Ajouter depuis mes contacts"
   :no-contacts                           "Pas encore de contacts"
   :show-qr                               "Afficher le QR"
   :enter-address                         "Entrer Adresse"
   :more                                  "plus"

   ;;group-settings
   :remove                                "Retirer"
   :save                                  "Sauvegarder"
   :delete                                "Supprimer"
   :clear-history                         "Effacer l'historique"
   :mute-notifications                    "Couper le son des notifications"
   :leave-chat                            "Quitter le chat"
   :chat-settings                         "Paramètres de chat"
   :edit                                  "Modifier"
   :add-members                           "Ajouter des membres"

   ;;commands
   :chat-send-eth                         "{{amount}} ETH"

   ;;location command
   :your-current-location                 "Votre emplacement"
   :places-nearby                         "Places nearby"
   :search-results                        "Resultat de recherche"
   :dropped-pin                           "Repère placé"
   :location                              "Location"
   :open-map                              "Ouvrir la carte"
   :sharing-copy-to-clipboard-address     "Copier l'adresse"
   :sharing-copy-to-clipboard-coordinates "Copier les coordonnées"

   ;;new-group
   :new-group                             "Nouveau groupe"
   :reorder-groups                        "Réarranger les groupes"
   :edit-group                            "Modifier le groupe"
   :delete-group                          "Supprimer le groupe"
   :delete-group-confirmation             "Ce groupe sera retiré de vos groupes. Cela n'affectera pas vos contacts"
   :delete-group-prompt                   "Cela n'affectera pas vos contacts"
   :contact-s                             {:one   "contact"
                                           :other "contacts"}
   ;;protocol
   :received-invitation                   "invitation de chat reçue"
   :removed-from-chat                     "vous avez été retiré du chat de groupe"
   :left                                  "est parti(e)"
   :invited                               "invité"
   :removed                               "supprimé"
   :You                                   "Vous"

   ;;new-contact
   :add-new-contact                       "Ajouter un nouveau contact"
   :scan-qr                               "Scanner le QR"
   :name                                  "Nom"
   :address-explication                   "Peut-être qu'ici devrait se trouver un texte expliquant ce qu'est une adresse et où la chercher"
   :enter-valid-public-key                "Veuillez saisir une clé publique valide ou scanner un code QR"
   :contact-already-added                 "Le contact a déjà été ajouté"
   :can-not-add-yourself                  "Vous ne pouvez pas vous ajouter vous-même"
   :unknown-address                       "Adresse inconnue"

   ;;login
   :connect                               "Connecter"
   :address                               "Adresse"
   :password                              "Mot de passe"
   :sign-in-to-status                     "S'inscrire à Status"
   :sign-in                               "Inscription"
   :wrong-password                        "Mot de passe incorrect"
   :enter-password                        "Saisir son mot de passe"

   ;;recover
   :passphrase                            "Phrase secrète"
   :recover                               "Restaurer"
   :twelve-words-in-correct-order         "12 mots dans l'ordre"

   ;;accounts
   :recover-access                        "Restaurer l'accès"
   :create-new-account                    "Créer un nouveau compte"

   ;;wallet-qr-code
   :done                                  "Terminé"

   ;;validation
   :invalid-phone                         "Numéro de téléphone invalide"
   :amount                                "Montant"

   ;;transactions
   :confirm                               "Confirmer"
   :transaction                           "Transaction"
   :unsigned-transaction-expired          "Transaction non-signée expirée"
   :status                                "Statut"
   :recipient                             "Destinataire"
   :to                                    "à"
   :from                                  "de"
   :data                                  "Données"
   :got-it                                "Compris"
   :block                                 "Block"
   :hash                                  "Hash"
   :gas-limit                             "Limite de gaz"
   :gas-price                             "Prix du gaz"
   :gas-used                              "Gaz utilisé"
   :cost-fee                              "Coût"
   :nonce                                 "Nonce"
   :confirmations                         "Confirmations"
   :confirmations-helper-text             "Merci d'attendre au moins 12 confirmations afin de s'assurer que votre transactions soit traitée de manière securisée"
   :copy-transaction-hash                 "Copier le hash de la transaction"
   :open-on-etherscan                     "Ouvrir sur Etherscan.io"

   ;;webview
   :web-view-error                        "Oops, erreur"

   ;;testfairy warning
   :testfairy-title                       "Attention!"
   :testfairy-message                     "Vous utilisez une application installée à partir d'une version de developpement. À des fins de test, si vous êtes connecté en Wi-Fi votre session sera enregistrée, ainsi toutes vos interactions avec l'application (vidéo et logs). Ces enregistrements peuvent être utilisés par notre équipe de développement pour résoudre d'éventuels problèmes. Les vidéos et logs enregistrés n'incluent pas vos mots de passe. L'enregistrement est effectué uniquement si l'application a été installée à partir d'une version de developpement. Rien n'est enregistré si l'application a été installée depuis le PlayStore ou TestFlight."

   ;; wallet
   :wallet                                "Portefeuille"
   :wallets                               "Portefeuilles"
   :your-wallets                          "Vos portefeuilles"
   :main-wallet                           "Portefeuille principal"
   :wallet-error                          "Erreur lors du chargement des données"
   :wallet-send                           "Envoyer"
   :wallet-request                        "Demander"
   :wallet-exchange                       "Echanger"
   :wallet-assets                         "Actifs"
   :wallet-add-asset                      "Ajouter un actif"
   :wallet-total-value                    "Valeur totale"
   :wallet-settings                       "Paramètres du portefeuille"
   :signing-phrase-description            "Signer la transaction en saisissant votre mot de passe. Assurez-vous que les mots ci-dessus correspondent à votre phrase de signature"
   :wallet-insufficient-funds             "Fonds insuffisants"
   :request-transaction                   "Demander une transaction"
   :send-request                          "Envoyer une requête"
   :share                                 "Partager"
   :eth                                   "ETH"
   :currency                              "Devise"
   :usd-currency                          "USD"
   :transactions                          "Transactions"
   :transaction-details                   "Details de la transaction"
   :transaction-failed                    "Transaction échouée"
   :transactions-sign                     "Signer"
   :transactions-sign-all                 "Tout signer"
   :transactions-sign-transaction         "Signer la transaction"
   :transactions-sign-later               "Signer plus tard"
   :transactions-delete                   "Supprimer la transaction"
   :transactions-delete-content           "La transaction sera retirée de la liste des transactions 'non-signées'"
   :transactions-history                  "Historique"
   :transactions-unsigned                 "Non-signée"
   :transactions-history-empty            "Aucune transaction dans votre historique"
   :transactions-unsigned-empty           "Vous n'avez pas de transactions non-signées"
   :transactions-filter-title             "Filtre sur historique"
   :transactions-filter-tokens            "Tokens"
   :transactions-filter-type              "Type"
   :transactions-filter-select-all        "Tout selectionner"
   :view-transaction-details              "Voir les details de la transaction"
   :transaction-description               "Merci d'attendre au moins 12 confirmations afin de s'assurer que votre transaction soit traitée de manière securisée"
   :transaction-sent                      "Transaction envoyée"
   :transaction-moved-text                "La transaction restera dnas la listes des transactions 'non-signées' pour les prochaines 5 minutes"
   :transaction-moved-title               "Transaction déplacée"
   :sign-later-title                      "Signer la transaction plus tard?"
   :sign-later-text                       "Voir l'historique de la transaction pour la signer"
   :not-applicable                        "Non disponible pour les transactions non-signées"

   ;; Wallet Send
   :wallet-choose-recipient               "Choisir le destinataire"
   :wallet-choose-from-contacts           "Choisir à partir des contacts"
   :wallet-address-from-clipboard         "Utiliser l'adresse du presse-papiers"
   :wallet-invalid-address                "Adresse invalide: \n {{data}}"
   :wallet-browse-photos                  "Parcourir les photos"
   :validation-amount-invalid-number      "Le montant n'est pas un nombre valide"
   :validation-amount-is-too-precise      "Le montant est trop précis. La plus petite unité que vous pouvez envoyer est 1 Wei (1x10^-18 ETH)"



   ;; network settings
   :new-network                           "Nouveau réseau"
   :add-network                           "Ajouter un réseau"
   :add-new-network                       "Ajouter un nouveau réseau"
   :existing-networks                     "Réseaux existants"
   :add-json-file                         "Ajouter un fichier JSON"
   :paste-json-as-text                    "Coller JSON comme texte"
   :paste-json                            "Coller JSON"
   :specify-rpc-url                       "Spécifier un RPC URL"
   :edit-network-config                   "Modifier les paramètres du réseau"
   :connected                             "Connecté"
   :process-json                          "Traiter le JSON"
   :error-processing-json                 "Érreur au cours du traitement du JSON"
   :rpc-url                               "RPC URL"
   :remove-network                        "Supprimer le reseau"
   :network-settings                      "Paramètres du réseau"
   :edit-network-warning                  "Attention, changer les paramètres du réseau peut vous le désactiver"
   :connecting-requires-login             "Pour se connecter à un autre réseau, l'authentification est nécessaire"
   :close-app-title                       "Attention!"
   :close-app-content                     "L'application va se fermer. Lorsque vous la réouvrerez, le réseau sélectionné sera utilisé"
   :close-app-button                      "Confirmer"})

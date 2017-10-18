(ns status-im.translations.fr)

(def translations
  {
   ;common
   :members-title                         "Membres"
   :not-implemented                       "! Pas disponible"
   :chat-name                             "Pseudo"
   :notifications-title                   "Notifications et sons"
   :offline                               "Hors ligne"
   :search-for                            "Rechercher..."
   :cancel                                "Annuler"
   :next                                  "Suivant"
   :type-a-message                        "Saisissez un message..."
   :type-a-command                        "Saisissez une commande..."
   :error                                 "Erreur"

   :camera-access-error                   "Merci d'aller dans vos paramètres système et de vous assurer que Status > Caméra est sélectionné afin d'accorder la permission d'utiliser votre caméra."
   :photos-access-error                   "Merci d'aller dans vos paramètres système et de vous assurer que Status > Photos est sélectionné afin d'accorder la permission d'utiliser vos photos."

   ;drawer
   :invite-friends                        "Inviter des amis"
   :faq                                   "FAQ"
   :switch-users                          "Changer d'utilisateur"
   :feedback                              "Des remarques?\nSecouez votre téléphone!"
   :view-all                              "Voir tout"
   :current-network                       "Le réseau actuel"

   ;chat
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

   ;sync
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
   :datetime-multiple                     "s"
   :datetime-ago                          "il y a"
   :datetime-yesterday                    "hier"
   :datetime-today                        "aujourd'hui"

   ;profile
   :profile                               "Profil"
   :edit-profile                          "Modifier profil"
   :report-user                           "DÉNONCER UN UTILISATEUR"
   :message                               "Message"
   :username                              "Nom d'utilisateur"
   :not-specified                         "Non spécifié"
   :public-key                            "Clé publique"
   :phone-number                          "Numéro de téléphone"
   :email                                 "Adresse e-mail"
   :update-status                         "Actualisez votre statut..."
   :add-a-status                          "Ajoutez un statut..."
   :status-prompt                         "Créez un nouveau statut pour partager vos intérêts avec tout le monde. Vous pouvez aussi utiliser les #hashtags."
   :add-to-contacts                       "Ajouter aux contacts"
   :in-contacts                           "Dans les contacts"
   :remove-from-contacts                  "Supprimer des contacts"
   :start-conversation                    "Débuter une conversation"
   :send-transaction                      "Envoyer une transaction"
   :share-qr                              "Partager QR"
   :error-incorrect-name                  "Veuillez choisir un autre nom"
   :error-incorrect-email                 "Adresse e-mail incorrecte"

   ;;make_photo
   :image-source-title                    "Photo de profil"
   :image-source-make-photo               "Capturer"
   :image-source-gallery                  "Sélectionner dans la galerie"
   :image-source-cancel                   "Annuler"

   ;;sharing
   :sharing-copy-to-clipboard             "Copier vers le presse-papiers"
   :sharing-share                         "Partagez..."
   :sharing-cancel                        "Annuler"

   :browsing-title                        "Naviguer"
   :browsing-browse                       "@naviguer"
   :browsing-open-in-web-browser          "Ouvrir dans le navigateur"
   :browsing-cancel                       "Annuler"

   ;sign-up
   :contacts-syncronized                  "Vos contacts ont été synchronisés"
   :confirmation-code                     (str "Merci ! Nous vous avons envoyé un message texte contenant le code "
                                               "de confirmation. Veuillez fournir ce code pour confirmer votre numéro de téléphone")
   :incorrect-code                        (str "Désolé, le code est incorrect. Veuillez le saisir à nouveau.")
   :generate-passphrase                   (str "Je vais créer une phrase secrète pour vous permettre de restaurer votre "
                                               "accès ou de vous connecter depuis un autre périphérique")
   :phew-here-is-your-passphrase          "*Ouf* c'était dur, voici votre phrase secrète, *écrivez-la et gardez-la en lieu sûr !* Vous en aurez besoin pour restaurer votre compte."
   :here-is-your-passphrase               "Voici votre phrase secrète, *écrivez-la et gardez-la en lieu sûr !* Vous en aurez besoin pour restaurer votre compte."
   :written-down                          "Assurez-vous de l'avoir bien écrite"
   :phone-number-required                 "Appuyez ici pour entrer votre numéro de téléphone et je vais trouver vos amis"
   :shake-your-phone                      "Vous avez trouvé un bug ou alors avez une suggestion? ~Secouez~ simplement votre téléphone!"
   :intro-status                          "Chattez avec moi pour configurer votre compte et modifier vos paramètres !"
   :intro-message1                        "Bienvenue dans Status\nTapez sur ce message pour définir votre mot de passe et commencer !"
   :account-generation-message            "Donnez-moi une seconde, je dois faire quelques calculs de ouf pour générer votre compte !"
   :move-to-internal-failure-message      "Nous devons déplacer des fichiers importants depuis un stockage extérieur vers un stockage local. Afin de faire cela, nous avons besoin de votre permission. Nous n'utiliserons plus de stockage extérieur dans nos futures versions."
   :debug-enabled                         "Le serveur Debug a été lancé! Vous pouvez désormais utiliser *status-dev-cli scan* pour trouver le serveur depuis votre ordinateur sur le même réseau."

   ;phone types
   :phone-e164                            "International 1"
   :phone-international                   "International 2"
   :phone-national                        "National"
   :phone-significant                     "Significatif"

   ;chats
   :chats                                 "Chats"
   :new-chat                              "Nouveau chat"
   :delete-chat                           "Supprimer chat"
   :new-group-chat                        "Nouveau chat de groupe"
   :new-public-group-chat                 "Rejoindre chat public"
   :edit-chats                            "Modifier chats"
   :search-chats                          "Rechercher chats"
   :empty-topic                           "Sujet indéfini"
   :topic-format                          "Mauvais format [a-z0-9\\-]+"
   :public-group-topic                    "Sujet"

   ;discover
   :discover                              "Découvrir"
   :none                                  "Aucun"
   :search-tags                           "Entrez vos mots clés ici"
   :popular-tags                          "Mots-clés populaires"
   :recent                                "Récent"
   :no-statuses-discovered                "Aucun statut découvert"
   :no-statuses-found                     "Aucun statut trouvé"

   ;settings
   :settings                              "Paramètres"

   ;contacts
   :contacts                              "Contacts"
   :new-contact                           "Nouveau contact"
   :delete-contact                        "Supprimer contact"
   :delete-contact-confirmation           "Ce contact sera supprimé de vos contacts"
   :remove-from-group                     "Supprimer ce groupe"
   :edit-contacts                         "Modifier contacts"
   :search-contacts                       "Rechercher contacts"
   :show-all                              "TOUT AFFICHER"
   :contacts-group-dapps                  "ÐApps"
   :contacts-group-people                 "Personnes"
   :contacts-group-new-chat               "Démarrer un nouveau chat"
   :choose-from-contacts                  "Ajouter depuis mes contacts"
   :no-contacts                           "Pas encore de contacts"
   :show-qr                               "Afficher le QR"
   :enter-address                         "Entrer Adresse"
   :more                                  "plus"

   ;group-settings
   :remove                                "Retirer"
   :save                                  "Sauvegarder"
   :delete                                "Supprimer"
   :change-color                          "Changer la couleur"
   :clear-history                         "Effacer l'historique"
   :mute-notifications                    "Couper le son des notifications"
   :leave-chat                            "Quitter le chat"
   :delete-and-leave                      "Supprimer et quitter"
   :chat-settings                         "Paramètres de chat"
   :edit                                  "Modifier"
   :add-members                           "Ajouter des membres"
   :blue                                  "Bleu"
   :purple                                "Violet"
   :green                                 "Vert"
   :red                                   "Rouge"

   ;commands
   :money-command-description             "Envoyer de l'argent"
   :location-command-description          "Envoyer l'emplacement"
   :phone-command-description             "Envoyer le numéro de téléphone"
   :phone-request-text                    "Demande de numéro de téléphone"
   :confirmation-code-command-description "Envoyer le code de confirmation"
   :confirmation-code-request-text        "Demande de code de confirmation"
   :send-command-description              "Envoyer l'emplacement"
   :request-command-description           "Envoyer une demande"
   :keypair-password-command-description  ""
   :help-command-description              "Aide"
   :request                               "Demande"
   :chat-send-eth                         "{{amount}} ETH"
   :chat-send-eth-to                      "{{amount}} ETH à {{chat-name}}"
   :chat-send-eth-from                    "{{amount}} ETH de {{chat-name}}"

   ;new-group
   :group-chat-name                       "Pseudo"
   :empty-group-chat-name                 "Veuillez saisir un nom"
   :illegal-group-chat-name               "Veuillez choisir un autre nom"
   :new-group                             "Nouveau groupe"
   :reorder-groups                        "Réarranger les groupes"
   :group-name                            "Nom du groupe"
   :edit-group                            "Modifier groupe"
   :delete-group                          "Supprimer group"
   :delete-group-confirmation             "Ce groupe sera enlevé de vos groupes. Cela n'affectera pas vos contacts"
   :delete-group-prompt                   "Cela n'affectera pas vos contacts"
   :group-members                         "Membres du groupe"
   :contact-s                             {:one   "contact"
                                           :other "contacts"}
   ;participants
   :add-participants                      "Ajouter des participants"
   :remove-participants                   "Supprimer des participants"

   ;protocol
   :received-invitation                   "invitation de chat reçue"
   :removed-from-chat                     "vous avez été retiré du chat de groupe"
   :left                                  "est parti(e)"
   :invited                               "invité"
   :removed                               "supprimé"
   :You                                   "Vous"

   ;new-contact
   :add-new-contact                       "Ajouter un nouveau contact"
   :import-qr                             "Importer"
   :scan-qr                               "Scanner le QR"
   :swow-qr                               "Montrer le QR"
   :name                                  "Nom"
   :whisper-identity                      "Murmurer l'identité"
   :address-explication                   "Peut-être qu'ici devrait se trouver un texte expliquant ce qu'est une adresse et où la chercher"
   :enter-valid-address                   "Veuillez saisir une adresse valide ou scanner un code QR"
   :enter-valid-public-key                "Veuillez saisir une clé publique valide ou scanner un code QR"
   :contact-already-added                 "Le contact a déjà été ajouté"
   :can-not-add-yourself                  "Vous ne pouvez pas vous ajouter vous-même"
   :unknown-address                       "Adresse inconnue"


   ;login
   :connect                               "Connecter"
   :address                               "Adresse"
   :password                              "Mot de passe"
   :login                                 "Connexion"
   :sign-in-to-status                     "S'inscrire à Status"
   :sign-in                               "Inscription"
   :wrong-password                        "Mot de passe incorrect"

   ;recover
   :recover-from-passphrase               "Restaurer avec la phrase secrète"
   :recover-explain                       "Veuillez saisir la phrase secrète correspondant à votre mot de passe pour restaurer votre accès"
   :passphrase                            "Phrase secrète"
   :recover                               "Restaurer"
   :enter-valid-passphrase                "Veuillez saisir une phrase secrète"
   :enter-valid-password                  "Veuillez saisir un mot de passe"
   :twelve-words-in-correct-order         "12 mots dans l'ordre"

   ;accounts
   :recover-access                        "Restaurer l'accès"
   :add-account                           "Ajouter un compte"
   :create-new-account                    "Créer un nouveau compte"

   ;wallet-qr-code
   :done                                  "Terminé"
   :main-wallet                           "Portefeuille principal"

   ;validation
   :invalid-phone                         "Numéro de téléphone invalide"
   :amount                                "Montant"
   :not-enough-eth                        (str "Pas assez d'ETH sur le solde "
                                               "({{balance}} ETH)")
   ;transactions
   :confirm                               "Confirmer"
   :confirm-transactions                  {:one   "Confirmer la transaction"
                                           :other "Confirmer {{count}} transactions"
                                           :zero  "Aucune transaction"}
   :transactions-confirmed                {:one   "Transaction confirmée"
                                           :other "{{count}} transactions confirmées"
                                           :zero  "Aucune transaction confirmée"}
   :transaction                           "Transaction"
   :unsigned-transactions                 "Transactions non signées"
   :no-unsigned-transactions              "Aucune transaction non signée"
   :enter-password-transactions           {:one   "Confirmez la transaction en saisissant votre mot de passe"
                                           :other "Confirmez les transactions en saisissant votre mot de passe"}
   :status                                "Statut"
   :pending-confirmation                  "Confirmation en attente"
   :recipient                             "Destinataire"
   :one-more-item                         "Encore un élément"
   :fee                                   "Frais"
   :estimated-fee                         "Frais Estimés"
   :value                                 "Valeur"
   :to                                    "à"
   :from                                  "de"
   :data                                  "Données"
   :got-it                                "Compris"
   :contract-creation                     "Création du contrat"

   ;:webview
   :web-view-error                        "Oops, erreur"})

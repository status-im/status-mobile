(ns status-im.translations.fr-ch)

(def translations
  {
   ;common
   :members-title                         "Membres"
   :not-implemented                       "!pas mis en place"
   :chat-name                             "Nom de chat"
   :notifications-title                   "Notifications et sons"
   :offline                               "Hors ligne"

   ;drawer
   :invite-friends                        "Inviter des amis"
   :faq                                   "FAQ"
   :switch-users                          "Changer d'utilisateur"

   ;chat
   :is-typing                             "est en train de taper"
   :and-you                               "et vous"
   :search-chat                           "Chercher dans une conversation"
   :members                               {:one   "1 membre"
                                           :other "{{count}} membres"
                                           :zero  "pas de membres"}
   :members-active                        {:one   "1 membre, 1 actif"
                                           :other "{{count}} membres, {{count}} actif(s)"
                                           :zero  "pas de membres"}
   :active-online                         "En ligne"
   :active-unknown                        "Inconnu"
   :available                             "Disponible"
   :no-messages                           "Pas de messages"
   :suggestions-requests                  "Demandes"
   :suggestions-commands                  "Commandes"

   ;sync
   :sync-in-progress                      "En cours de synchronisation..."
   :sync-synced                           "Synchronisé"

   ;messages
   :status-sending                        "Envoi en cours"
   :status-pending                        "En attendant"
   :status-sent                           "Envoyé"
   :status-seen-by-everyone               "Vu par tous"
   :status-seen                           "Vu"
   :status-delivered                      "Livré"
   :status-failed                         "Echoué"

   ;datetime
   :datetime-second                       {:one   "seconde"
                                           :other "secondes"}
   :datetime-minute                       {:one   "minute"
                                           :other "minutes"}
   :datetime-hour                         {:one   "heure"
                                           :other "heures"}
   :datetime-day                          {:one   "jour"
                                           :other "jours"}
   :datetime-multiple                     "s"
   :datetime-ago                          "avant"
   :datetime-yesterday                    "hier"
   :datetime-today                        "aujourd'hui"

   ;profile
   :profile                               "Profil"
   :report-user                           "SIGNALER UTILISATEUR"
   :message                               "Message"
   :username                              "Nom d'utilisateur"
   :not-specified                         "Non spécifié"
   :public-key                            "Clé publique"
   :phone-number                          "Numéro de téléphone"
   :email                                 "E-mail"
   :profile-no-status                     "Pas de statut"
   :add-to-contacts                       "Ajouter aux contacts"
   :error-incorrect-name                  "Veuillez sélectionner un autre nom"
   :error-incorrect-email                 "E-mail incorrect"

   ;;make_photo
   :image-source-title                    "Image du profil"
   :image-source-make-photo               "Capture"
   :image-source-gallery                  "Sélectionner dans la galerie"
   :image-source-cancel                   "Annuler"

   ;sign-up
   :contacts-syncronized                  "Vos contacts ont été synchronisés"
   :confirmation-code                     (str "Merci! Nous vous avons envoyé un message de texte avec un code "
                                               "de confirmation. Veuillez fournir ce code pour confirmer votre numéro de téléphone")
   :incorrect-code                        (str "Désolé, le code est incorrect, veuillez réessayer")
   :generate-passphrase                   (str "Je vais générer une phrase de passe pour vous afin que vous puissiez "
                                               "rétablir l'accès ou vous connecter depuis un autre appareil")
   :phew-here-is-your-passphrase          "*Pffff* c'était dur, voici votre phrase de passe, *notez-la et gardez-la en sécurité!* Vous en aurez besoin pour récupérer votre compte."
   :here-is-your-passphrase               "Voici votre phrase de passe, *notez-la et gardez-la en sécurité!* Vous en aurez besoin pour récupérer votre compte."
   :written-down                          "Assurez-vous de l'avoir notée de manière sûre"
   :phone-number-required                 "Tapez ici pour saisir votre numéro de téléphone et je trouverai vos amis"
   :intro-status                          "Chattez avec moi pour configurer votre compte et changer vos paramètres !"
   :intro-message1                        "Bienvenue dans le statut\nTapez ce message pour établir votre mot de passe et vous lancer !"
   :account-generation-message            "Une seconde svp, je dois lancer quelques formules magiques pour générer votre compte !"

   ;chats
   :chats                                 "Chats"
   :new-chat                              "Nouveau chat"
   :new-group-chat                        "Nouveau chat de groupe"

   ;discover
   :discover                             "Découverte"
   :none                                  "Aucun"
   :search-tags                           "Tapez vos clés de recherche ici"
   :popular-tags                          "Clés populaires"
   :recent                                "Récent"
   :no-statuses-discovered                "Aucun statut trouvé"

   ;settings
   :settings                              "Paramètres"

   ;contacts
   :contacts                              "Contacts"
   :new-contact                           "Nouveau Contact"
   :show-all                              "MONTRER TOUS"
   :contacts-group-dapps                  "ÐApps"
   :contacts-group-people                 "Personnes"
   :contacts-group-new-chat               "Lancer un nouveau chat"
   :no-contacts                           "Pas encore de contacts"
   :show-qr                               "Montrer QR"

   ;group-settings
   :remove                                "Supprimer"
   :save                                  "Sauvegarder"
   :change-color                          "Changer la couleur"
   :clear-history                         "Effacer l'historique"
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
   :location-command-description          "Envoyer un emplacement"
   :phone-command-description             "Envoyer un numéro de téléphone"
   :phone-request-text                    "Demande de numéro de téléphone"
   :confirmation-code-command-description "Envoyer un code de confirmation"
   :confirmation-code-request-text        "Demande de code de confirmation"
   :send-command-description              "Envoyer un emplacement"
   :request-command-description           "Envoyer une demande"
   :keypair-password-command-description  ""
   :help-command-description              "Aide"
   :request                               "Demande"
   :chat-send-eth                         "{{amount}} ETH"
   :chat-send-eth-to                      "{{amount}} ETH pour {{chat-name}}"
   :chat-send-eth-from                    "{{amount}} ETH de {{chat-name}}"
   :command-text-location                 "Emplacement: {{address}}"
   :command-text-browse                   "Site web de navigation: {{webpage}}"
   :command-text-send                     "Transaction: {{amount}} ETH"
   :command-text-help                     "Aide"

   ;new-group
   :group-chat-name                       "Nom du chat"
   :empty-group-chat-name                 "Veuillez entrer un nom"
   :illegal-group-chat-name               "Veuillez sélectionner un autre nom"

   ;participants
   :add-participants                      "Ajouter des participants"
   :remove-participants                   "Supprimer des participants"

   ;protocol
   :received-invitation                   "reçu une invitation à un chat"
   :removed-from-chat                     "vous a supprimé du chat de groupe"
   :left                                  "restant"
   :invited                               "invité"
   :removed                               "supprimé"
   :You                                   "Vous"

   ;new-contact
   :add-new-contact                       "Ajouter un nouveau contact"
   :import-qr                             "Importer"
   :scan-qr                               "Scannner QR"
   :name                                  "Nom"
   :whisper-identity                      "Murmurer l'identité"
   :address-explication                   "On pourrait mettre ici un texte qui explique ce qu'est une adresse et comment la rechercher"
   :enter-valid-address                   "Veuillez saisir une adresse valable ou scanner un code QR"
   :contact-already-added                 "Le contact a déjà été ajouté"
   :can-not-add-yourself                  "Vous ne pouvez pas vous ajouter vous-même"
   :unknown-address                       "Adresse inconnue"


   ;login
   :connect                               "Se connecter"
   :address                               "Adresse"
   :password                              "Mot de passe"
   :login                                 "Nom d'utilisateur"
   :wrong-password                        "Mauvais mot de passe"

   ;recover
   :recover-from-passphrase               "Restaurer à partir d'une phrase de passe"
   :recover-explain                       "Veuillez saisir la phrase de passe pour votre mot de passe afin de récupérer l'accès"
   :passphrase                            "Phrase de passe"
   :recover                               "Restaurer"
   :enter-valid-passphrase                "Veuillez taper une phrase de passe"
   :enter-valid-password                  "Veuillez taper un mot de passe"

   ;accounts
   :recover-access                        "Récupérer l'accès"
   :add-account                           "Ajouter un compte"

   ;wallet-qr-code
   :done                                  "Terminé"
   :main-wallet                           "Portefeuille principal"

   ;validation
   :invalid-phone                         "Numéro de téléphone non valable"
   :amount                                "Montant"
   :not-enough-eth                        (str "Pas assez d'ETH sur le solde "
                                               "({{balance}} ETH)")
   ;transactions
   :confirm-transactions                  {:one   "Confirmer la transaction"
                                           :other "Confirmer {{count}} transactions"
                                           :zero  "Pas de transactions"}
   :status                                "Statut"
   :pending-confirmation                  "Confirmation en attente"
   :recipient                             "Destinataire"
   :one-more-item                         "Encore un objet"
   :fee                                   "Tarif"
   :value                                 "Valeur"

   ;:webview
   :web-view-error                        "oups, erreur"})

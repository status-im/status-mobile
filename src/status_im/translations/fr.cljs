(ns status-im.translations.fr)

(def translations
  {
   ;common
   :members-title                         "Membres"
   :not-implemented                       "! non mis en œuvre"
   :chat-name                             "Pseudo"
   :notifications-title                   "Notifications et sons"
   :offline                               "Hors ligne"

   ;drawer
   :invite-friends                        "Inviter des amis"
   :faq                                   "FAQ"
   :switch-users                          "Changer d'utilisateur"

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
   :active-online                         "En ligne"
   :active-unknown                        "Inconnu"
   :available                             "Disponible"
   :no-messages                           "Pas de messages"
   :suggestions-requests                  "Demandes"
   :suggestions-commands                  "Commandes"

   ;sync
   :sync-in-progress                      "Synchronisation en cours..."
   :sync-synced                           "En synchronisation"

   ;messages
   :status-sending                        "Envoi en cours..."
   :status-pending                        "En attendant..."
   :status-sent                           "Envoyé"
   :status-seen-by-everyone               "Vu par tout le monde"
   :status-seen                           "Vu"
   :status-delivered                      "Livré"
   :status-failed                         "Échec"

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
   :datetime-ago                          "il y a"
   :datetime-yesterday                    "hier"
   :datetime-today                        "aujourd'hui"

   ;profile
   :profile                               "Profil"
   :report-user                           "DÉNONCER UN UTILISATEUR"
   :message                               "Message"
   :username                              "Nom d'utilisateur"
   :not-specified                         "Non spécifié"
   :public-key                            "Clé publique"
   :phone-number                          "Numéro de téléphone"
   :email                                 "Adresse e-mail"
   :profile-no-status                     "Pas de statut"
   :add-to-contacts                       "Ajouter aux contacts"
   :error-incorrect-name                  "Veuillez choisir un autre nom"
   :error-incorrect-email                 "Adresse e-mail incorrecte"

   ;;make_photo
   :image-source-title                    "Photo de profil"
   :image-source-make-photo               "Capturer"
   :image-source-gallery                  "Sélectionner dans la galerie"
   :image-source-cancel                   "Annuler"

   ;sign-up
   :contacts-syncronized                  "Vos contacts ont été synchronisés"
   :confirmation-code                     (str "Merci ! Nous vous avons envoyé un message texte contenant le code "
                                               "de confirmation. Veuillez fournir ce code pour confirmer votre numéro de téléphone")
   :incorrect-code                        (str "Désolé, le code est incorrect. Veuillez le saisir à nouveau.")
   :generate-passphrase                   (str "Je vais créer une phrase de passe pour vous permettre de restaurer votre "
                                               "accès ou de vous connecter depuis un autre périphérique")
   :phew-here-is-your-passphrase          "*Ouf* c'était dur, voici votre phrase secrète, *écrivez-la et gardez-la en lieu sûr !* Vous en aurez besoin pour récupérer votre compte."
   :here-is-your-passphrase               "Voici votre phrase secrète, *écrivez-la et gardez-la en lieu sûr !* Vous en aurez besoin pour récupérer votre compte."
   :written-down                          "Assurez-vous de l'avoir bien écrite"
   :phone-number-required                 "Appuyez ici pour entrer votre numéro de téléphone et je vais trouver vos amis"
   :intro-status                          "Chattez avec moi pour configurer votre compte et modifier vos paramètres !"
   :intro-message1                        "Bienvenue dans Status\nAppuyez sur ce message pour définir votre mot de passe et commencer !"
   :account-generation-message            "Donnez-moi une seconde, je dois faire quelques calculs de ouf pour générer votre compte !"

   ;chats
   :chats                                 "Chats"
   :new-chat                              "Nouveau chat"
   :new-group-chat                        "Nouveau chat de groupe"

   ;discover
   :discover                             "Découverte"
   :none                                  "Aucun"
   :search-tags                           "Entrez vos mots clés de"
   :popular-tags                          "Mots-clés populaires"
   :recent                                "Récent"
   :no-statuses-discovered                "Aucun statut découvert"

   ;settings
   :settings                              "Paramètres"

   ;contacts
   :contacts                              "Contacts"
   :new-contact                           "Nouveau contact"
   :show-all                              "TOUT AFFICHER"
   :contacts-group-dapps                  "ÐApps"
   :contacts-group-people                 "Personnes"
   :contacts-group-new-chat               "Démarrer un nouveau chat"
   :no-contacts                           "Pas de contacts encore"
   :show-qr                               "Afficher le QR"

   ;group-settings
   :remove                                "Retirer"
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
   :command-text-location                 "Emplacement : {{address}}"
   :command-text-browse                   "Page de navigation : {{webpage}}"
   :command-text-send                     "Transaction : {{amount}} ETH"
   :command-text-help                     "Aide"

   ;new-group
   :group-chat-name                       "Pseudo"
   :empty-group-chat-name                 "Veuillez saisir un nom"
   :illegal-group-chat-name               "Veuillez choisir un autre nom"

   ;participants
   :add-participants                      "Ajouter des participants"
   :remove-participants                   "Supprimer des participants"

   ;protocol
   :received-invitation                   "invitation de chat reçue"
   :removed-from-chat                     "vous a retiré du chat de groupe"
   :left                                  "est parti(e)"
   :invited                               "invité"
   :removed                               "supprimé"
   :You                                   "Vous"

   ;new-contact
   :add-new-contact                       "Ajouter un nouveau contact"
   :import-qr                             "Importer"
   :scan-qr                               "Scanner le QR"
   :name                                  "Nom"
   :whisper-identity                      "Murmurer l'identité"
   :address-explication                   "Peut-être qu'ici devrait se trouver un texte expliquant ce qu'est une adresse et où la chercher"
   :enter-valid-address                   "Veuillez saisir une adresse valide ou scanner un code QR"
   :contact-already-added                 "Le contact a déjà été ajouté"
   :can-not-add-yourself                  "Vous ne pouvez pas vous ajouter vous-même"
   :unknown-address                       "Adresse inconnue"


   ;login
   :connect                               "Connecter"
   :address                               "Adresse"
   :password                              "Mot de passe"
   :login                                 "Connexion"
   :wrong-password                        "Mot de passe incorrect"

   ;recover
   :recover-from-passphrase               "Récupérer avec la phrase secrète"
   :recover-explain                       "Veuillez saisir la phrase secrète de votre mot de passe pour restaurer votre accès"
   :passphrase                            "Phrase secrète"
   :recover                               "Récupérer"
   :enter-valid-passphrase                "Veuillez saisir une phrase secrète"
   :enter-valid-password                  "Veuillez saisir un mot de passe"

   ;accounts
   :recover-access                        "Récupérer l'accès"
   :add-account                           "Ajouter un compte"

   ;wallet-qr-code
   :done                                  "Terminé"
   :main-wallet                           "Portefeuille principal"

   ;validation
   :invalid-phone                         "Numéro de téléphone invalide"
   :amount                                "Montant"
   :not-enough-eth                        (str "Pas assez d'ETH sur le solde "
                                               "({{balance}} ETH)")
   ;transactions
   :confirm-transactions                  {:one   "Confirmer la transaction"
                                           :other "Confirmer {{count}} transactions"
                                           :zero  "Aucune transaction"}
   :status                                "Statut"
   :pending-confirmation                  "Confirmation en attente"
   :recipient                             "Destinataire"
   :one-more-item                         "Encore un élément"
   :fee                                   "Frais"
   :value                                 "Valeur"

   ;:webview
   :web-view-error                        "Oops, erreur"})

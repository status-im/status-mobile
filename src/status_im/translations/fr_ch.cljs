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
   :datetime-ago                          "avant"
   :datetime-yesterday                    "hier"
   :datetime-today                        "aujourd'hui"

   ;profile
   :profile                               "Profil"
   :message                               "Message"
   :not-specified                         "Non spécifié"
   :public-key                            "Clé publique"
   :phone-number                          "Numéro de téléphone"
   :add-to-contacts                       "Ajouter aux contacts"

   ;;make_photo
   :image-source-title                    "Image du profil"
   :image-source-make-photo               "Capture"
   :image-source-gallery                  "Sélectionner dans la galerie"

   ;sign-up
   :contacts-syncronized                  "Vos contacts ont été synchronisés"
   :confirmation-code                     (str "Merci! Nous vous avons envoyé un message de texte avec un code "
                                               "de confirmation. Veuillez fournir ce code pour confirmer votre numéro de téléphone")
   :incorrect-code                        (str "Désolé, le code est incorrect, veuillez réessayer")
   :phew-here-is-your-passphrase          "*Pffff* c'était dur, voici votre phrase de passe, *notez-la et gardez-la en sécurité!* Vous en aurez besoin pour récupérer votre compte."
   :here-is-your-passphrase               "Voici votre phrase de passe, *notez-la et gardez-la en sécurité!* Vous en aurez besoin pour récupérer votre compte."
   :phone-number-required                 "Tapez ici pour saisir votre numéro de téléphone et je trouverai vos amis"
   :intro-status                          "Chattez avec moi pour configurer votre compte et changer vos paramètres !"
   :intro-message1                        "Bienvenue dans le statut\nTapez ce message pour établir votre mot de passe et vous lancer !"
   :account-generation-message            "Une seconde svp, je dois lancer quelques formules magiques pour générer votre compte !"

   ;chats
   :chats                                 "Chats"
   :new-group-chat                        "Nouveau chat de groupe"

   ;discover
   :discover                              "Découverte"
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
   :contacts-group-new-chat               "Lancer un nouveau chat"
   :no-contacts                           "Pas encore de contacts"
   :show-qr                               "Montrer QR"

   ;group-settings
   :remove                                "Supprimer"
   :save                                  "Sauvegarder"
   :clear-history                         "Effacer l'historique"
   :chat-settings                         "Paramètres de chat"
   :edit                                  "Modifier"
   :add-members                           "Ajouter des membres"

   ;commands
   :chat-send-eth                         "{{amount}} ETH"

   ;new-group

   ;participants

   ;protocol
   :received-invitation                   "reçu une invitation à un chat"
   :removed-from-chat                     "vous a supprimé du chat de groupe"
   :left                                  "restant"
   :invited                               "invité"
   :removed                               "supprimé"
   :You                                   "Vous"

   ;new-contact
   :add-new-contact                       "Ajouter un nouveau contact"
   :scan-qr                               "Scannner QR"
   :name                                  "Nom"
   :address-explication                   "On pourrait mettre ici un texte qui explique ce qu'est une adresse et comment la rechercher"
   :contact-already-added                 "Le contact a déjà été ajouté"
   :can-not-add-yourself                  "Vous ne pouvez pas vous ajouter vous-même"
   :unknown-address                       "Adresse inconnue"


   ;login
   :connect                               "Se connecter"
   :address                               "Adresse"
   :password                              "Mot de passe"
   :wrong-password                        "Mauvais mot de passe"

   ;recover
   :passphrase                            "Phrase de passe"
   :recover                               "Restaurer"

   ;accounts
   :recover-access                        "Récupérer l'accès"

   ;wallet-qr-code
   :done                                  "Terminé"
   :main-wallet                           "Portefeuille principal"

   ;validation
   :invalid-phone                         "Numéro de téléphone non valable"
   :amount                                "Montant"
   ;transactions
   :status                                "Statut"
   :recipient                             "Destinataire"

   ;:webview
   :web-view-error                        "oups, erreur"

   :confirm                               "Confirmer"
   :phone-national                        "National"
   :public-group-topic                    "Sujet"
   :debug-enabled                         "Le serveur de débogage a été lancé ! Vous pouvez maintenant ajouter votre Dapp en exécutant *status-dev-cli scan* depuis votre ordinateur"
   :new-public-group-chat                 "Rejoindre le chat public"
   :datetime-ago-format                   "{{number}} {{time-intervals}} {{ago}}"
   :sharing-cancel                        "Annuler"
   :twelve-words-in-correct-order         "12 mots dans le bon ordre"
   :remove-from-contacts                  "Retirer des contacts"
   :delete-chat                           "Supprimer la discussion"
   :edit-chats                            "Modifier les discussions"
   :sign-in                               "S'identifier"
   :create-new-account                    "Créer un nouveau compte"
   :sign-in-to-status                     "S'identifier vers le statut"
   :got-it                                "J'ai compris"
   :move-to-internal-failure-message      "Nous devons déplacer des fichiers importants depuis le stockage externe vers le stockage interne. Pour cela, nous avons besoin de votre permission. Nous n'utiliserons pas le stockage externe dans les versions futures."
   :edit-group                            "Modifier le groupe"
   :delete-group                          "Supprimer le groupe"
   :browsing-title                        "Parcourir"
   :reorder-groups                        "Réorganiser les groupes"
   :browsing-cancel                       "Annuler"
   :faucet-success                        "La requête du site faucet a été reçue"
   :choose-from-contacts                  "Choisir parmi les contacts"
   :new-group                             "Nouveau groupe"
   :phone-e164                            "International 1"
   :remove-from-group                     "Retirer du groupe"
   :search-contacts                       "Rechercher des contacts"
   :transaction                           "Transaction"
   :public-group-status                   "Public"
   :leave-chat                            "Quitter le chat"
   :start-conversation                    "Débuter une conversation"
   :topic-format                          "Mauvais format [a-z0-9\\-]+"
   :enter-valid-public-key                "Merci de renseigner une clé publique valide ou de scanner un code QR"
   :faucet-error                          "Erreur de la requête du site faucet"
   :phone-significant                     "Important"
   :search-for                            "Rechercher…"
   :sharing-copy-to-clipboard             "Copier vers le presse-papiers"
   :phone-international                   "International 2"
   :enter-address                         "Renseigner l'adresse"
   :send-transaction                      "Envoyer la transaction"
   :delete-contact                        "Supprimer le contact"
   :mute-notifications                    "Désactiver le son des notifications"


   :contact-s                             {:one   "contact"
                                           :other "contacts"}
   :next                                  "Suivant"
   :from                                  "De"
   :search-chats                          "Rechercher dans les discussions"
   :in-contacts                           "Dans les contacts"

   :sharing-share                         "Partagez…"
   :type-a-message                        "Tapez un message…"
   :type-a-command                        "Commencez à taper une commande…"
   :shake-your-phone                      "Vous trouvez un bug ou vous avez une suggestion ? ~Secouez~ simplement votre natel !"
   :status-prompt                         "Créez un statut afin d'aider les gens à savoir ce que vous offrez. Vous pouvez également utiliser les #hashtags."
   :add-a-status                          "Ajoutez un statut…"
   :error                                 "Erreur"
   :edit-contacts                         "Modifier les contacts"
   :more                                  "plus"
   :cancel                                "Annuler"
   :no-statuses-found                     "Aucun statut trouvé"
   :browsing-open-in-web-browser          "Ouvrir dans le navigateur"
   :delete-group-prompt                   "Cela n'affectera pas les contacts"
   :edit-profile                          "Modifier le profil"


   :empty-topic                           "Sujet vide"
   :to                                    "À"
   :data                                  "Données"})

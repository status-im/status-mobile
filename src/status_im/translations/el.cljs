(ns status-im.translations.el)

(def translations
  {
   ;;common
   :members-title                    "Μέλη"
   :not-implemented                  "!μη υλοποιημένο"
   :chat-name                        "Όνομα συνομιλίας"
   :notifications-title              "Ειδοποιήσεις και ήχοι"
   :offline                          "Εκτός σύνδεσης"
   :search-for                       "Αναζήτηση για..."
   :cancel                           "Ακύρωση"
   :next                             "Επόμενο"
   :open                             "Άνοιγμα"
   :description                      "Περιγραφή"
   :url                              "URL"
   :type-a-message                   "Πληκτρολογήστε ένα μήνυμα..."
   :type-a-command                   "Αρχίστε να πληκτρολογείτε μια εντολή..."
   :error                            "Σφάλμα"
   :unknown-status-go-error          "Άγνωστο status-go σφάλμα"
   :node-unavailable                 "Κανένας κόμβος ethereum σε λειτουργία"
   :yes                              "Ναι"
   :no                               "Όχι"

   :camera-access-error              "Για να χορηγήσετε την απαιτούμενη άδεια κάμερας, παρακαλώ μεταβείτε στις ρυθμίσεις του συστήματός σας και σιγουρευτείτε ότι το Status > Κάμερα είναι επιλεγμένο."
   :photos-access-error              "Για να χορηγήσετε την απαιτούμενη άδεια φωτογραφιών, παρακαλώ μεταβείτε στις ρυθμίσεις του συστήματός σας και σιγουρευτείτε ότι το Status > Φωτογραφίες είναι επιλεγμένο."

   ;;drawer
   :switch-users                     "Αλλαγή χρηστών"
   :logout                           "Αποσύνδεση"
   :current-network                  "Τρέχον δίκτυο"

   ;;home
   :home                             "Αρχική"

   ;;chat
   :is-typing                        "πληκτρολογεί"
   :and-you                          "και εσείς"
   :search-chat                      "Αναζήτηση συνομιλίας"
   :members                          {:one   "1 μέλος"
                                      :other "{{count}} μέλη"
                                      :zero  "κανένα μέλος"}
   :members-active                   {:one   "1 μέλος"
                                      :other "{{count}} μέλη"
                                      :zero  "κανένα μέλος"}
   :public-group-status              "Δημόσιο"
   :active-online                    "Σε σύνδεση"
   :active-unknown                   "Άγνωστος"
   :available                        "Διαθέσιμος"
   :no-messages                      "Κανένα μήνυμα"
   :suggestions-requests             "Αιτήσεις"
   :suggestions-commands             "Εντολές"
   :faucet-success                   "Το αίτημα faucet έχει παραληφθεί"
   :faucet-error                     "Το αίτημα faucet έχει σφάλμα"

   ;;sync
   :sync-in-progress                 "Συγχρονισμός..."
   :sync-synced                      "Σε συγχρονισμό"

   ;;messages
   :status-sending                   "Αποστολή..."
   :status-pending                   "Εκκρεμεί"
   :status-sent                      "Απεσταλμένα"
   :status-seen-by-everyone          "Το έχουν δει όλοι"
   :status-seen                      "Το είδε"
   :status-delivered                 "Παραδόθηκε"
   :status-failed                    "Απέτυχε"

   ;;datetime
   :datetime-ago-format              "{{number}} {{time-intervals}} {{ago}}"
   :datetime-second                  {:one   "δευτερόλεπτο"
                                      :other "δευτερόλεπτα"}
   :datetime-minute                  {:one   "λεπτό"
                                      :other "λεπτά"}
   :datetime-hour                    {:one   "ώρα"
                                      :other "ώρες"}
   :datetime-day                     {:one   "μέρα"
                                      :other " μέρες"}
   :datetime-ago                     "πριν"
   :datetime-yesterday               "χτες"
   :datetime-today                   "σήμερα"

   ;;profile
   :profile                          "Προφίλ"
   :edit-profile                     "Επεξεργασία προφίλ"
   :message                          "Μήνυμα"
   :not-specified                    "Δεν διευκρινίζεται"
   :public-key                       "Δημόσιο κλειδί"
   :phone-number                     "Αριθμός τηλεφώνου"
   :update-status                    "Ενημέρωση της κατάστασής σας..."
   :add-a-status                     "Προσθέστε μια κατάσταση..."
   :status-prompt                    "Ορίστε την κατάστασή σας. Χρησιμοποιώντας #hastags θα βοηθήσετε τους άλλους να σας ανακαλύψουν και να μιλήσετε σχετικά με το τι έχετε στο μυαλό σας"
   :add-to-contacts                  "Προσθήκη στις επαφές"
   :in-contacts                      "Στις επαφές"
   :remove-from-contacts             "Κατάργηση από τις επαφές"
   :start-conversation               "Έναρξη συνομιλίας"
   :testnet-text                     "Βρίσκεστε στο {{testnet}} Testnet. Μην στέλνετε πραγματικό ETH ή SNT στη διεύθυνσή σας"
   :mainnet-text                     "Βρίσκεστε στο Mainnet. Θα στείλετε πραγματικό ETH"

   ;;make_photo
   :image-source-title               "Εικόνα προφίλ"
   :image-source-make-photo          "Λήψη"
   :image-source-gallery             "Επιλέξτε από τη συλλογή"

   ;;sharing
   :sharing-copy-to-clipboard        "Αντιγραφή στο πρόχειρο"
   :sharing-share                    "Κοινοποίηση..."
   :sharing-cancel                   "Ακύρωση"

   :browsing-title                   "Περιηγηθείτε"
   :browsing-open-in-web-browser     "Ανοίξτε στο πρόγραμμα περιήγησης ιστού"
   :browsing-cancel                  "Ακύρωση"

   ;;sign-up
   :contacts-syncronized             "Οι επαφές σας έχουν συγχρονιστεί"
   :confirmation-code                (str "Ευχαριστούμε! Σας έχουμε στείλει ένα μήνυμα κειμένου με έναν κωδικό επιβεβαίωσης. "
                                          "Παρακαλούμε καταχωρήστε αυτόν τον κωδικό για να επιβεβαιώσετε τον αριθμό τηλεφώνου σας")
   :incorrect-code                   (str "Δυστυχώς ο κωδικός ήταν λανθασμένος, παρακαλούμε εισάγετέ τον ξανά")
   :phew-here-is-your-passphrase     "Πωπω, αυτό ήταν δύσκολο. Αυτό είναι το συνθηματικό σας, *γράψτε το και κρατήστε το ασφαλές!* Θα χρειαστεί για ανάκτηση του λογαριασμού σας."
   :here-is-your-passphrase          "Αυτό είναι το συνθηματικό σας, *γράψτε το και κρατήστε το ασφαλές!* Θα χρειαστεί για ανάκτηση του λογαριασμού σας."
   :here-is-your-signing-phrase      "Εδώ είναι η φράση επικύρωσής σας. Θα τη χρησιμοποιήσετε για να επαληθεύσετε τις συναλλαγές σας. *Γράψτε τη και κρατήστε την ασφαλές!*"
   :phone-number-required            "Αγγίξτε εδώ για να επικυρώσετε τον αριθμό τηλεφώνου σας & θα βρω τους φίλους σας."
   :shake-your-phone                 "Βρήκατε ένα σφάλμα ή έχετε μια πρόταση; Απλά ~κουνήστε~ το τηλέφωνό σας!"
   :intro-status                     "Συζητήστε μαζί μου για να ρυθμίσετε το λογαριασμό σας και να αλλάξετε τις ρυθμίσεις σας."
   :intro-message1                   "Καλωσήρθες στο Status!\nΑγγίξτε αυτό το μήνυμα για να ορίσετε τον κωδικό πρόσβασής σας και να ξεκινήσετε."
   :account-generation-message       "Δώστε μου ένα δευτερόλεπτο, πρέπει να κάνω κάποιους τρελούς μαθηματικούς υπολογισμούς για να δημιουργήσω το λογαριασμό σας!"
   :move-to-internal-failure-message "Πρέπει να μεταφέρουμε κάποια σημαντικά αρχεία από την εξωτερική μνήμη στην εσωτερική μνήμη. Για να γίνει αυτό, χρειαζόμαστε την άδειά σας. Δεν θα χρησιμοποιούμε την εξωτερική μνήμη σε μελλοντικές εκδόσεις."
   :debug-enabled                    "Ο debug server έχει ξεκινήσει! Τώρα μπορείτε να εκτελέσετε *status-dev-cli scan* για να βρείτε τον server από τον υπολογιστή σας στο ίδιο δίκτυο."

   ;;phone types
   :phone-e164                       "Διεθνές 1"
   :phone-international              "Διεθνές 2"
   :phone-national                   "Εθνικό"
   :phone-significant                "Σημαντικό"

   ;;chats
   :chats                            "Συνομιλίες"
   :delete-chat                      "Διαγραφή συνομιλίας"
   :new-group-chat                   "Νέα ομαδική συνομιλία"
   :new-public-group-chat            "Συμμετοχή σε δημόσια συνομιλία"
   :edit-chats                       "Επεξεργασία συνομιλίας"
   :search-chats                     "Αναζήτηση συνομιλιών"
   :empty-topic                      "Κενό θέμα"
   :topic-format                     "Λάθος μορφή [a-z0-9\\-]+"
   :public-group-topic               "Θέμα"

   ;;discover
   :discover                         "Ανακαλύψτε"
   :none                             "Κανένα"
   :search-tags                      "Πληκτρολογήστε εδώ τις ετικέτες αναζήτησης"
   :popular-tags                     "Δημοφιλή #hashtags"
   :recent                           "Πρόσφατες καταστάσεις"
   :no-statuses-found                "Δεν βρέθηκαν καταστάσεις"
   :chat                             "Συνομιλία"
   :all                              "Όλα"
   :public-chats                     "Δημόσιες συνομιλίες"
   :soon                             "Σύντομα"
   :public-chat-user-count           "{{count}} άνθρωποι"
   :dapps                            "ÐApps"
   :dapp-profile                     "ÐApp προφίλ"
   :no-statuses-discovered           "Δεν βρέθηκαν καταστάσεις"
   :no-statuses-discovered-body      "Όταν κάποιος δημοσιεύει\nμια κατάσταση θα την δείτε εδώ."
   :no-hashtags-discovered-title     "Δε βρέθηκαν #hashtags"
   :no-hashtags-discovered-body      "Όταν κάποιο #hashtag γίνεται\nδημοφιλές θα το δείτε εδώ."

   ;;settings
   :settings                         "Ρυθμίσεις"

   ;;contacts
   :contacts                         "Επαφές"
   :new-contact                      "Νέα επαφή"
   :delete-contact                   "Διαγραφή επαφής"
   :delete-contact-confirmation      "Αυτή η επαφή θα καταργηθεί από τις επαφές σας"
   :remove-from-group                "Κατάργηση από την ομάδα"
   :edit-contacts                    "Επεξεργασία επαφών"
   :search-contacts                  "Αναζήτηση επαφών"
   :contacts-group-new-chat          "Ξεκινήστε νέα συνομιλία"
   :choose-from-contacts             "Επιλέξτε από τις επαφές"
   :no-contacts                      "Δεν υπάρχουν επαφές"
   :show-qr                          "Εμφάνιση κωδικού QR"
   :enter-address                    "Εισάγετε τη διεύθυνση"
   :more                             "περισσότερα"

   ;;group-settings
   :remove                           "Αφαιρέστε"
   :save                             "Αποθηκεύστε"
   :delete                           "Διαγράψτε"
   :clear-history                    "Καθαρισμός ιστορικού"
   :mute-notifications               "Σίγαση ειδοποιήσεων"
   :leave-chat                       "Φύγετε από τη συνομιλία"
   :chat-settings                    "Ρυθμίσεις συνομιλίας"
   :edit                             "Επεξεργασία"
   :add-members                      "Προσθήκη μελών"

   ;;commands
   :chat-send-eth                    "{{amount}} ETH"

   ;;new-group
   :new-group                        "Νέα ομάδα"
   :reorder-groups                   "Επαναδιαρθρώστε τις ομάδες"
   :edit-group                       "Επεξεργασία ομάδας"
   :delete-group                     "Διαγραφή ομάδας"
   :delete-group-confirmation        "Αυτή η ομάδα θα καταργηθεί από τις ομάδες σας. Αυτό δε θα επηρεάσει τις επαφές σας"
   :delete-group-prompt              "Αυτό δε θα επηρεάσει τις επαφές σας"
   :contact-s                        {:one   "επαφή"
                                      :other "επαφές"}

   ;;protocol
   :received-invitation              "έλαβες πρόσκληση συνομιλίας"
   :removed-from-chat                "αφαιρέθηκες από την ομαδική συζήτηση"
   :left                             "έφυγες"
   :invited                          "προσκλήθηκες"
   :removed                          "αφαιρέθηκες"
   :You                              "Εσύ"

   ;;new-contact
   :add-new-contact                  "Προσθήκη νέας επαφής"
   :scan-qr                          "Σάρωση κωδικού QR"
   :name                             "Όνομα"
   :address-explication              "Το δημόσιο κλειδί σας χρησιμοποιείται για τη δημιουργία της διεύθυνσής σας στο Ethereum και είναι μια σειρά αριθμών και γραμμάτων. Μπορείτε να το βρείτε εύκολα στο προφίλ σας"
   :enter-valid-public-key           "Παρακαλώ εισάγετε ένα έγκυρο δημόσιο κλειδί ή σαρώσετε έναν κωδικό QR"
   :contact-already-added            "Η επαφή έχει ήδη προστεθεί"
   :can-not-add-yourself             "Δεν μπορείτε να προσθέσετε τον εαυτό σας"
   :unknown-address                  "Άγνωστη διεύθυνση"

   ;;login
   :connect                          "Σύνδεση"
   :address                          "Διεύθυνση"
   :password                         "Κωδικός πρόσβασης"
   :sign-in-to-status                "Συνδεθείτε στο Status"
   :sign-in                          "Συνδεθείτε"
   :wrong-password                   "Λάθος κωδικός πρόσβασης"
   :enter-password                   "Εισάγετε κωδικός πρόσβασης"

   ;;recover
   :passphrase                       "Συνθηματικό"
   :recover                          "Ανάκτηση"
   :twelve-words-in-correct-order    "12 λέξεις σε σωστή σειρά"

   ;;accounts
   :recover-access                   "Ανάκτηση πρόσβασης"
   :create-new-account               "Δημιουργία νέου λογαριασμού"

   ;;wallet-qr-code
   :done                             "Ολοκληρώθηκε"

   ;;validation
   :invalid-phone                    "Μη έγκυρος αριθμός τηλεφώνου"
   :amount                           "Ποσό"

   ;;transactions
   :confirm                          "Επιβεβαιώνω"
   :transaction                      "Συναλλαγή"
   :unsigned-transaction-expired     "Η μη επικυρωμένη συναλλαγή έληξε"
   :status                           "Κατάσταση"
   :recipient                        "Παραλήπτης"
   :to                               "Προς"
   :from                             "Από"
   :data                             "Δεδομένα"
   :got-it                           "Το κατάλαβα"
   :block                            "Block"
   :hash                             "Hash"
   :gas-limit                        "όριο Gas"
   :gas-price                        "τιμή Gas"
   :gas-used                         "χρησιμοποιημένο Gas"
   :cost-fee                         "Κόστος/Χρέωση"
   :nonce                            "Nonce"
   :confirmations                    "Επιβεβαιώσεις"
   :confirmations-helper-text        "Παρακαλώ περιμένετε τουλάχιστον 12 επιβεβαιώσεις για να βεβαιωθείτε ότι η συναλλαγή σας γίνεται με ασφάλεια"
   :copy-transaction-hash            "Αντιγραφή συναλλαγής hash"
   :open-on-etherscan                "Άνοιγμα στο Etherscan.io"
   :incoming                         "Εισερχόμενη"
   :outgoing                         "Εξερχόμενη"
   :pending                          "Εκκρεμής"
   :postponed                        "Αναβλήθηκε"

   ;;webview
   :web-view-error                   "ωπ, σφάλμα"

   ;;testfairy warning
   :testfairy-title                  "Προειδοποίηση!"
   :testfairy-message                "Χρησιμοποιείτε μια εφαρμογή εγκατεστημένη από ένα νυχτερινό build. Για πειραματικούς σκοπούς αυτό το build περιλαμβάνει καταγραφή της συνεδρίας σας, εφόσον χρησιμοποιείτε wifi. Έτσι αποθηκεύονται όλες οι αλληλεπιδράσεις σας με αυτήν την εφαρμογή (όπως βίντεο και logs) και θα μπορούσαν να χρησιμοποιηθούν από την ομάδα ανάπτυξής μας για τη διερεύνηση πιθανών προβλημάτων. Τα αποθηκευμένα βίντεο/logs δεν συμπεριλαμβάνουν τους κωδικούς πρόσβασής σας. Η καταγραφή γίνεται μόνο αν η εφαρμογή είναι εγκατεστημένη από ένα νυχτερινό build. Δεν καταγράφεται τίποτα εφόσον η εφαρμογή είναι εγκατεστημένη μέσω του PlayStore ή του TestFlight."

   ;; wallet
   :wallet                           "Πορτοφόλι"
   :wallets                          "Πορτοφόλια"
   :your-wallets                     "Τα πορτοφόλια σας"
   :main-wallet                      "Κύριο Πορτοφόλι"
   :wallet-error                     "Σφάλμα κατά τη φόρτωση δεδομένων"
   :wallet-send                      "Στείλτε"
   :wallet-send-token                "Στείλτε {{symbol}}"
   :wallet-request                   "Αίτημα"
   :wallet-exchange                  "Αντάλλαγμα"
   :wallet-asset                     "Ενεργητικό"
   :wallet-assets                    "Ενεργητικό"
   :wallet-add-asset                 "Προσθήκη ενεργητικού"
   :wallet-total-value               "Συνολική αξία"
   :wallet-settings                  "Ρυθμίσεις πορτοφολιού"
   :wallet-manage-assets             "Διαχείριση ενεργητικού"
   :signing-phrase-description       "Επικυρώστε τη συναλλαγή εισάγοντας το συνθηματικό σας. Βεβαιωθείτε ότι οι παραπάνω λέξεις ταιριάζουν με τη μυστική φράση επικύρωσης που έχετε"
   :wallet-insufficient-funds        "Ανεπαρκείς πόροι"
   :request-transaction              "Αίτηση συναλλαγής"
   :send-request                     "Στείλε αίτημα"
   :share                            "Κοινοποίηση"
   :eth                              "ETH"
   :currency                         "Νόμισμα"
   :usd-currency                     "USD"
   :amount-placeholder               "Καθορίστε το ποσό"
   :transactions                     "Συναλλαγές"
   :transaction-details              "Λεπτομέρειες συναλλαγής"
   :transaction-failed               "Αποτυχία συναλλαγής"
   :transactions-sign                "Επικύρωση"
   :transactions-sign-all            "Επικύρωση όλων"
   :transactions-sign-transaction    "Επικύρωση συναλλαγής"
   :transactions-sign-later          "Επικύρωση αργότερα"
   :transactions-delete              "Διαγραφή συναλλαγής"
   :transactions-delete-content      "Η συναλλαγή θα αφαιρεθεί από 'Ανυπόγραφη' λίστα"
   :transactions-history             "Ιστορικό"
   :transactions-unsigned            "Μη επικυρωμένες"
   :transactions-history-empty       "Δεν υπάρχουν ακόμη συναλλαγές στο ιστορικό σας"
   :transactions-unsigned-empty      "Δεν έχετε καμία μη επικυρωμένη συναλλαγή"
   :transactions-filter-title        "Φίλτρο ιστορικού"
   :transactions-filter-tokens       "Tokens"
   :transactions-filter-type         "Τύπος"
   :transactions-filter-select-all   "Επιλογή όλων"
   :view-transaction-details         "Προβολή λεπτομερειών συναλλαγής"
   :transaction-description          "Περιμένετε τουλάχιστον 12 επιβεβαιώσεις για να βεβαιωθείτε ότι η συναλλαγή σας γίνεται με ασφάλεια"
   :transaction-sent                 "Η συναλλαγή έχει σταλεί"
   :transaction-moved-text           "Η συναλλαγή θα παραμείνει στη λίστα των 'Μη επικυρωμένων' για τα επόμενα 5 λεπτά"
   :transaction-moved-title          "Η συναλλαγή μετακινήθηκε"
   :sign-later-title                 "Επικυρώστε τη συναλλαγή αργότερα;"
   :sign-later-text                  "Ελέγξτε το ιστορικό συναλλαγών για να επικυρώσετε αυτήν τη συναλλαγή"
   :not-applicable                   "Δεν ισχύει για συναλλαγές που δεν έχουν υπογραφεί"
   :send-transaction                 "Αποστολή συναλλαγής"
   :receive-transaction              "Λήψη συναλλαγής"
   :transaction-history              "Ιστορικό συναλλαγών"

   ;; Wallet Send
   :wallet-choose-recipient          "Επιλογή παραλήπτη"
   :wallet-choose-from-contacts      "Επιλογή από Επαφές"
   :wallet-address-from-clipboard    "Χρησιμοποιήστε τη διεύθυνση από το πρόχειρο"
   :wallet-invalid-address           "Μη έγκυρη διεύθυνση: \n {{data}}"
   :wallet-invalid-chain-id          "Το δίκτυο δεν ταιριάζει: \n {{data}} αλλά η τρέχουσα αλυσίδα είναι {{chain}}"
   :wallet-browse-photos             "Περιηγηθείτε στις φωτογραφίες"
   :wallet-advanced                  "Προχωρημένη"
   :wallet-transaction-fee           "Χρέωση Συναλλαγής"
   :wallet-transaction-fee-details   "Το όριο gas είναι το ποσό του gas που θα αποσταλεί με τη συναλλαγή σας. Η αύξηση αυτού του αριθμού δε θα οδηγήσει σε ταχύτερη επεξεργασία της συναλλαγής σας"
   :wallet-transaction-total-fee     "Σύνολική Χρέωση"
   :validation-amount-invalid-number "Το ποσό δεν είναι έγκυρος αριθμός"
   :validation-amount-is-too-precise "Το ποσό είναι πολύ ακριβές. Η μικρότερη μονάδα που μπορείτε να στείλετε είναι 1 Wei (1x10^-18 ETH)"



   ;; network settings
   :new-network                      "Νέο δίκτυο"
   :add-network                      "Προσθήκη δικτύου"
   :add-new-network                  "Προσθήκη νέου δικτύου"
   :add-wnode                        "Προσθήκη mailserver"
   :existing-networks                "Υπάρχοντα δίκτυα"
   ;; TODO(dmitryn): come up with better description/naming. Suggested namings: Mailbox and Master Node
   :existing-wnodes                  "Υπάρχοντες mailservers"
   :add-json-file                    "Προσθήκη ενός JSON αρχείου"
   :paste-json-as-text               "Επικόλληση JSON ως κείμενο"
   :paste-json                       "Επικόλληση JSON"
   :specify-rpc-url                  "Καθορίστε ένα RPC URL"
   :edit-network-config              "Επεξεργασία ρυθμίσεων δικτύου"
   :connected                        "Συνδεδεμένος"
   :process-json                     "Επεξεργασία JSON"
   :error-processing-json            "Σφάλμα επεξεργασίας JSON"
   :rpc-url                          "RPC URL"
   :remove-network                   "Αφαίρεση δικτύου"
   :network-settings                 "Ρυθμίσεις δικτύου"
   :offline-messaging-settings       "Ρυθμίσεις μηνυμάτων εκτός σύνδεσης"
   :edit-network-warning             "Προσοχή, η επεξεργασία των δεδομένων δικτύου ενδέχεται να απενεργοποιήσει αυτό το δίκτυο για εσάς"
   :connecting-requires-login        "Η σύνδεση σε άλλο δίκτυο απαιτεί είσοδο"
   :close-app-title                  "Προειδοποίηση!"
   :close-app-content                "Η εφαρμογή θα κλείσει. Όταν την ξανανοίξετε, το επιλεγμένο δίκτυο θα χρησιμοποιηθεί"
   :close-app-button                 "Επιβεβαιώστε"})

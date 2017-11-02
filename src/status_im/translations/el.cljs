(ns status-im.translations.el)

(def translations
  {
   ;;common
   :members-title                         "Μέλη"
   :not-implemented                       "!μη υλοποιήμενο"
   :chat-name                             "Όνομα συνομιλίας"
   :notifications-title                   "Ειδοποιήσεις και ήχοι"
   :offline                               "Εκτός σύνδεσης"
   :search-for                            "Αναζήτηση για..."
   :cancel                                "Ακύρωση"
   :next                                  "Επόμενο"
   :open                                  "Άνοιξε"
   :description                           "Περιγραφή"
   :url                                   "URL"
   :type-a-message                        "Πληκτρολογήστε ένα μήνυμα..."
   :type-a-command                        "Αρχίστε να πληκτρολογείτε μια εντολή..."
   :error                                 "Σφάλμα"
   :unknown-status-go-error               "Άγνωστο status-go σφάλμα"
   :node-unavailable                      "Κανένας κόμβος ethereum σε λειτουργία"
   :yes                                   "Ναι"
   :no                                    "Όχι"

   :camera-access-error                   "Για να χορηγήσετε την απαιτούμενη άδεια κάμερας, παρακαλώ μεταβείτε στις ρυθμίσεις του συστήματός σας και σιγουρευτείτε ότι Status > Κάμερα είναι επιλεγμένο."
   :photos-access-error                   "Για να χορηγήσετε την απαιτούμενη άδεια φωτογραφιών, παρακαλώ μεταβείτε στις ρυθμίσεις του συστήματός σας και σιγουρευτείτε ότι Status > Φωτογραφίες είναι επιλεγμένο."

   ;;drawer
   :switch-users                          "Αλλαγή χρηστών"
   :current-network                       "Τρέχον δίκτυο"

   ;;chat
   :is-typing                             "πληκτρολογεί"
   :and-you                               "και εσύ"
   :search-chat                           "Αναζήτηση συνομιλίας"
   :members                               {:one   "1 μέλος"
                                           :other "{{count}} μέλη"
                                           :zero  "κανένα μέλος"}
   :members-active                        {:one   "1 μέλος"
                                           :other "{{count}} μέλη"
                                           :zero  "κανένα μέλος"}
   :public-group-status                   "Δημόσιο"
   :active-online                         "Σε σύνδεση"
   :active-unknown                        "Άγνωστος"
   :available                             "Διαθέσιμος"
   :no-messages                           "Κανένα μήνυμα"
   :suggestions-requests                  "Αιτήσεις"
   :suggestions-commands                  "Εντολές"
   :faucet-success                        "Faucet αίτημα έχει παραληφθεί"
   :faucet-error                          "Faucet αίτημα έχει σφάλμα"

   ;;sync
   :sync-in-progress                      "Συγχρονισμός..."
   :sync-synced                           "Σε συγχρονισμό"

   ;;messages
   :status-pending                        "Εκκρεμής"
   :status-sent                           "Απεσταλμένα"
   :status-seen-by-everyone               "Το έχουν δει όλοι"
   :status-seen                           "Είδα"
   :status-delivered                      "Παραδόθηκε"
   :status-failed                         "Απέτυχε"

   ;;datetime
   :datetime-ago-format                   "{{number}} {{time-intervals}} {{ago}}"
   :datetime-second                       {:one   "δευτερόλεπτο"
                                           :other "δευτερόλεπτα"}
   :datetime-minute                       {:one   "λεπτό"
                                           :other "λεπτά"}
   :datetime-hour                         {:one   "ώρα"
                                           :other "ώρες"}
   :datetime-day                          {:one   "μέρα"
                                           :other " μέρες"}
   :datetime-ago                          "πριν"
   :datetime-yesterday                    "χτες"
   :datetime-today                        "σήμερα"

   ;;profile
   :profile                               "Προφίλ"
   :edit-profile                          "Επεξεργασία προφίλ"
   :message                               "Μήνυμα"
   :not-specified                         "Δεν διευκρινίζεται"
   :public-key                            "Δημόσιο κλειδί"
   :phone-number                          "Αριθμός τηλεφώνου"
   :update-status                         "Ενημέρωση της κατάστασής σας..."
   :add-a-status                          "Προσθέστε μια κατάσταση..."
   :status-prompt                         "Ορίστε την κατάστασή σας. Χρησιμοποιώντας #hastags θα βοηθήσετε τους άλλους να σας ανακαλύψουν και να μιλήσετε σχετικά με το τι είναι στο μυαλό σας"
   :add-to-contacts                       "Προσθήκη στις επαφές"
   :in-contacts                           "Στις επαφές"
   :remove-from-contacts                  "Κατάργηση από τις επαφές"
   :start-conversation                    "Έναρξη συνομιλίας"
   :send-transaction                      "Αποστολή συναλλαγής"
   :testnet-text                          "Βρίσκεστε στο {{testnet}} Testnet. Μην στέλνετε πραγματικό ETH ή SNT στη διεύθυνσή σας"
   :mainnet-text                          "Βρίσκεστε στο Mainnet. Πραγματικό ETH θα σταλεί"

   ;;make_photo
   :image-source-title                    "Εικόνα προφίλ"
   :image-source-make-photo               "Λήψη"
   :image-source-gallery                  "Επιλέξτε από τη συλλογή"

   ;;sharing
   :sharing-copy-to-clipboard             "Αντιγραφή στο πρόχειρο"
   :sharing-share                         "Κοινοποίηση..."
   :sharing-cancel                        "Ακύρωση"

   :browsing-title                        "Περιηγηθείτε"
   :browsing-browse                       "@browse"
   :browsing-open-in-web-browser          "Ανοίξτε στο πρόγραμμα περιήγησης ιστού"
   :browsing-cancel                       "Ακύρωση"

   ;;sign-up
   :contacts-syncronized                  "Οι επαφές σας έχουν συγχρονιστεί"
   :confirmation-code                     (str "Ευχαριστούμε! Σας έχουμε στείλει ένα μήνυμα κειμένου με έναν κωδικό επιβεβαίωσης. "
                                               "Παρακαλούμε καταχωρίστε αυτόν τον κωδικό για να επιβεβαιώσετε τον αριθμό τηλεφώνου σας")
   :incorrect-code                        (str "Δυστυχώς ο κωδικός ήταν λανθασμένος, παρακαλούμε εισάγετε τον ξανά")
   :phew-here-is-your-passphrase          "Φτου, αυτό ήταν δύσκολο. Αυτή είναι η φράση πρόσβασης σας, *γράψτε τη και κρατήστε τη ασφαλές!* Θα χρειαστεί για ανακτήση του λογαριασμού σας."
   :here-is-your-passphrase               "Αυτή είναι η φράση πρόσβασης σας, *γράψτε τη και κρατήστε τη ασφαλές!* Θα χρειαστεί για ανακτήση του λογαριασμού σας."
   :here-is-your-signing-phrase           "Εδώ είναι η φράση υπογραφής σας. Θα τη χρησιμοποιήσετε για να επαληθεύσετε τις συναλλαγές σας. *Γράψτε τη και κρατήστε τη ασφαλές!*"
   :phone-number-required                 "Αγγίξτε εδώ για να επικυρώσετε τον αριθμό τηλεφώνου σας & θα βρω τους φίλους σας."
   :shake-your-phone                      "Βρήκατε ένα σφάλμα ή έχετε μια πρόταση; Απλά ~κουνήστε~ το τηλέφωνό σας!"
   :intro-status                          "Συζητήστε μαζί μου για να ρυθμίσετε το λογαριασμό σας και να αλλάξετε τις ρυθμίσεις σας."
   :intro-message1                        "Καλωσήρθες στο Status!\nΑγγίξτε αυτό το μήνυμα για να ορίσετε τον κωδικό πρόσβασής σας και να ξεκινήσετε."
   :account-generation-message            "Δώστε μου ένα δευτερόλεπτο, πρέπει να κάνω κάποιους τρελούς μαθηματικούς υπολογισμούς για να δημιουργήσω το λογαριασμό σου!"
   :move-to-internal-failure-message      "Πρέπει να μεταφέρουμε κάποια σημαντικά αρχεία από την εξωτερική στην εσωτερική αποθήκευση. Για να γίνει αυτό, χρειαζόμαστε την άδειά σας. Δεν θα χρησιμοποιούμε εξωτερικό αποθηκευτικό χώρο σε μελλοντικές εκδόσεις."
   :debug-enabled                         "Ο debug server έχει ξεκινήσει! Τώρα μπορείτε να εκτελέσετε *status-dev-cli scan* για να βρείτε τον server από τον υπολογιστή σας στο ίδιο δίκτυο."

   ;;phone types
   :phone-e164                            "Διεθνές 1"
   :phone-international                   "Διεθνές 2"
   :phone-national                        "Εθνικό"
   :phone-significant                     "Σημαντικό"

   ;;chats
   :chats                                 "Συνομιλίες"
   :delete-chat                           "Διαγραφή συνομιλίας"
   :new-group-chat                        "Νέα ομαδική συνομιλία"
   :new-public-group-chat                 "Συμμετοχή σε δημόσια συνομιλία"
   :edit-chats                            "Επεξεργασία συνομιλία"
   :search-chats                          "Αναζήτηση συνομιλιών"
   :empty-topic                           "Κενό θέμα"
   :topic-format                          "Λάθος μορφή [a-z0-9\\-]+"
   :public-group-topic                    "Θέμα"

   ;;discover
   :discover                              "Ανακαλύψτε"
   :none                                  "Κανένα"
   :search-tags                           "Πληκτρολογήστε εδώ τις ετικέτες αναζήτησης"
   :popular-tags                          "Δημοφιλής #hashtags"
   :recent                                "Πρόσφατες καταστάσεις"
   :no-statuses-found                     "Δεν βρέθηκαν καταστάσεις"
   :chat                                  "Συνομιλία"
   :all                                   "Όλα"
   :public-chats                          "Δημόσιες συνομιλίες"
   :soon                                  "Σύντομα"
   :public-chat-user-count                "{{count}} άνθρωποι"
   :dapps                                 "ÐApps"
   :dapp-profile                          "ÐApp προφίλ"
   :no-statuses-discovered                "Δεν βρέθηκαν καταστάσεις"
   :no-statuses-discovered-body           "Όταν κάποιος δημοσιεύει\nμια κατάσταση θα την δείτε εδώ."
   :no-hashtags-discovered-title          "Κανέμα #hashtags δε βρέθηκε"
   :no-hashtags-discovered-body           "Όταν #hashtag γίνεται\nδημοφιλές θα το δείτε εδώ."

   ;;settings
   :settings                              "Ρυθμίσεις"

   ;;contacts
   :contacts                              "Επαφές"
   :new-contact                           "Νέα επαφή"
   :delete-contact                        "Διαγραφή επαφής"
   :delete-contact-confirmation           "Αυτή η επαφή θα καταργηθεί από τις επαφές σας"
   :remove-from-group                     "Κατάργηση από την ομάδα"
   :edit-contacts                         "Επεξεργασία επαφών"
   :search-contacts                       "Αναζήτηση επαφών"
   :contacts-group-new-chat               "Ξεκινήστε νέα συνομιλία"
   :choose-from-contacts                  "Επιλέξτε από τις επαφές"
   :no-contacts                           "Δεν υπάρχουν επαφές"
   :show-qr                               "Εμφάνιση QR κωδικού"
   :enter-address                         "Εισαγάγετε τη διεύθυνση"
   :more                                  "περισσότερα"

   ;;group-settings
   :remove                                "Αφαιρέστε"
   :save                                  "Αποθηκεύστε"
   :delete                                "Διαγράψτε"
   :clear-history                         "Καθαρισμός ιστορικού"
   :mute-notifications                    "Σίγαση ειδοποιήσεων"
   :leave-chat                            "Φύγετε από τη συνομιλία"
   :chat-settings                         "Ρυθμίσεις συνομιλίας"
   :edit                                  "Επεξεργασία"
   :add-members                           "Προσθήκη μελών"

   ;;commands
   :chat-send-eth                         "{{amount}} ETH"

   ;;location command
   :your-current-location                 "Η τρέχουσα τοποθεσία σας"
   :places-nearby                         "Τοποθεσίες κοντά"
   :search-results                        "Αποτελέσματα αναζήτησης"
   :dropped-pin                           "Πτώση καρφίτσας"
   :location                              "Τοποθεσία"
   :open-map                              "Άνοιγμα χάρτη"
   :sharing-copy-to-clipboard-address     "Αντιγραφή διεύθυνσης"
   :sharing-copy-to-clipboard-coordinates "Αντιγραφή συντεταγμένων"

   ;;new-group
   :new-group                             "Νέα ομάδα"
   :reorder-groups                        "Επαναδιαρθρώστε τις ομάδες"
   :edit-group                            "Επεξεργασία ομάδας"
   :delete-group                          "Διαγραφή ομάδας"
   :delete-group-confirmation             "Αυτή η ομάδα θα καταργηθεί από τις ομάδες σας. Αυτό δεν θα επηρεάσει τις επαφές σας"
   :delete-group-prompt                   "Αυτό δεν θα επηρεάσει τις επαφές σας"
   :contact-s                             {:one   "επαφή"
                                           :other "επαφές"}

   ;;protocol
   :received-invitation                   "έλαβες πρόσκληση συνομιλίας"
   :removed-from-chat                     "αφαιρέθηκες από την ομαδική συζήτηση"
   :left                                  "έφυγες"
   :invited                               "προσκλήθηκες"
   :removed                               "αφαίρεσες"
   :You                                   "Εσύ"

   ;;new-contact
   :add-new-contact                       "Προσθήκη νέας επαφής"
   :scan-qr                               "Σάρωση QR κωδικού"
   :name                                  "Όνομα"
   :address-explication                   "Το δημόσιο σας κλειδί χρησιμοποιείται για τη δημιουργία της διεύθυνσής σας στο Ethereum και είναι μια σειρά αριθμών και γραμμάτων. Μπορείτε να το βρείτε εύκολα στο προφίλ σας"
   :enter-valid-public-key                "Παρακαλώ εισάγετε ένα έγκυρο δημόσιο κλειδί ή να σαρώσετε έναν QR κωδικό"
   :contact-already-added                 "Η επαφή έχει ήδη προστεθεί"
   :can-not-add-yourself                  "Δεν μπορείτε να προσθέσετε τον εαυτό σας"
   :unknown-address                       "Άγνωστη διεύθυνση"

   ;;login
   :connect                               "Σύνδεση"
   :address                               "Διεύθυνση"
   :password                              "Κωδικός πρόσβασης"
   :sign-in-to-status                     "Συνδεθείτε στο Status"
   :sign-in                               "Συνδεθείτε"
   :wrong-password                        "Λάθος κωδικός πρόσβασης"
   :enter-password                        "Εισάγετε κωδικός πρόσβασης"

   ;;recover
   :passphrase                            "Φράση πρόσβασης"
   :recover                               "Ανακτώ"
   :twelve-words-in-correct-order         "12 λέξεις σε σωστή σειρά"

   ;;accounts
   :recover-access                        "Ανάκτηση πρόσβασης"
   :create-new-account                    "Δημιουργία νέου λογαριασμού"

   ;;wallet-qr-code
   :done                                  "Ολοκληρώθηκε"

   ;;validation
   :invalid-phone                         "Μη έγκυρος αριθμός τηλεφώνου"
   :amount                                "Ποσό"

   ;;transactions
   :confirm                               "Επιβεβαιώνω"
   :transaction                           "Συναλλαγή"
   :unsigned-transaction-expired          "Η μη υπογεγραμμένη συναλλαγή έληξε"
   :status                                "Κατάσταση"
   :recipient                             "Παραλήπτης"
   :to                                    "Προς"
   :from                                  "Από"
   :data                                  "Δεδομένα"
   :got-it                                "Το κατάλαβα"
   :block                                 "Block"
   :hash                                  "Hash"
   :gas-limit                             "όριο Gas"
   :gas-price                             "τιμή Gas"
   :gas-used                              "χρησιμοποιημένο Gas"
   :cost-fee                              "Κόστος/Χρέωση"
   :nonce                                 "Nonce"
   :confirmations                         "Επιβεβαιώσεις"
   :confirmations-helper-text             "Παρακαλώ περιμένετε τουλάχιστον 12 επιβεβαιώσεις για να βεβαιωθείτε ότι η συναλλαγή σας γίνεται με ασφάλεια"
   :copy-transaction-hash                 "Αντιγραφή συναλλαγής hash"
   :open-on-etherscan                     "Άνοιγμα στο Etherscan.io"

   ;;webview
   :web-view-error                        "ουπς, σθάλμα"

   ;;testfairy warning
   :testfairy-title                       "Προειδοποίηση!"
   :testfairy-message                     "Χρησιμοποιείτε μια εφαρμογή εγκατεστημένη από ένα νυχτερινό build. Για σκοπούς testing αυτό το build περιλαμβάνει εγγραφή συνεδρίας εάν wifi σύνδεση χρησιμοποιείται, έτσι αποθηκεύονται όλες οι αλληλεπιδράσεις σας με αυτήν την εφαρμογή (όπως βίντεο και logs) και θα μπορούσε να χρησιμοποιηθεί από την ομάδα ανάπτυξης μας για να διερευνήσει πιθανά θέματα. Αποθηκευμένα βίντεο/logs δεν συμπεριλαμβάνουν τους κωδικούς πρόσβασής σας. Η εγγραφή γίνεται μόνο αν η εφαρμογή είναι εγκατεστημένη από ένα νυχτερινό build. Nothing is recorded if the app is installed from PlayStore or TestFlight."

   ;; wallet
   :wallet                                "Πορτοφόλι"
   :wallets                               "Πορτοφόλια"
   :your-wallets                          "Τα πορτοφόλια σας"
   :main-wallet                           "Κύριο Πορτοφόλι"
   :wallet-error                          "Σφάλμα κατά τη φόρτωση δεδομένων"
   :wallet-send                           "Στείλτε"
   :wallet-request                        "Αίτημα"
   :wallet-exchange                       "Αντάλλαγμα"
   :wallet-assets                         "Ενεργητικό"
   :wallet-add-asset                      "Προσθήκη ενεργητικού"
   :wallet-total-value                    "Συνολική αξία"
   :wallet-settings                       "Ρυθμίσεις πορτοφολιού"
   :signing-phrase-description            "Υπογράψτε τη συναλλαγή εισάγοντας τον κωδικό πρόσβασής σας. Βεβαιωθείτε ότι οι παραπάνω λέξεις ταιριάζουν με τη μυστική φράση υπογραφής σας"
   :wallet-insufficient-funds             "Ανεπαρκείς πόροι"
   :request-transaction                   "Αίτηση συναλλαγής"
   :send-request                          "Στείλε αίτημα"
   :share                                 "Κοινοποίηση"
   :eth                                   "ETH"
   :currency                              "Νόμισμα"
   :usd-currency                          "USD"
   :transactions                          "Συναλλαγές"
   :transaction-details                   "Λεπτομέρειες συναλλαγής"
   :transaction-failed                    "Αποτυχία συναλλαγής"
   :transactions-sign                     "Υπογραφή"
   :transactions-sign-all                 "Υπογραφή όλων"
   :transactions-sign-transaction         "Υπογραφή συναλλαγής"
   :transactions-sign-later               "Υπογραφή αργότερα"
   :transactions-delete                   "Διαγραφή συναλλαγής"
   :transactions-delete-content           "Η συναλλαγή θα αφαιρεθεί από 'Ανυπόγραφη' λίστα"
   :transactions-history                  "Ιστορικό"
   :transactions-unsigned                 "Ανυπόγραφη"
   :transactions-history-empty            "Δεν υπάρχουν ακόμη συναλλαγές στο ιστορικό σας"
   :transactions-unsigned-empty           "Δεν έχετε καμία ανυπόγραφη συναλλαγή"
   :transactions-filter-title             "Φίλτρο ιστορικού"
   :transactions-filter-tokens            "Tokens"
   :transactions-filter-type              "Τύπος"
   :transactions-filter-select-all        "Επιλογή όλων"
   :view-transaction-details              "Προβολή λεπτομερειών συναλλαγής"
   :transaction-description               "Περιμένετε τουλάχιστον 12 επιβεβαιώσεις για να βεβαιωθείτε ότι η συναλλαγή σας γίνεται με ασφάλεια"
   :transaction-sent                      "Η συναλλαγή έχει σταλεί"
   :transaction-moved-text                "Η συναλλαγή θα παραμείνει στην 'Ανυπόγραφη' λίστα για τα επόμενα 5 λεπτά"
   :transaction-moved-title               "Η συναλλαγή μετακινήθηκε"
   :sign-later-title                      "Υπογράψτε τη συναλλαγή αργότερα?"
   :sign-later-text                       "Ελέγξτε το ιστορικό συναλλαγών για να υπογράψετε αυτήν τη συναλλαγή"
   :not-applicable                        "Δεν ισχύει για συναλλαγές που δεν έχουν υπογραφεί"

   ;; Wallet Send
   :wallet-choose-recipient               "Επιλογή παραλήπτη"
   :wallet-choose-from-contacts           "Επιλογή από Επαφές"
   :wallet-address-from-clipboard         "Χρησιμοποιήστε τη διεύθυνση από το πρόχειρο"
   :wallet-invalid-address                "Μη έγκυρη διεύθυνση: \n {{data}}"
   :wallet-browse-photos                  "Αναζητήστε φωτογραφίες"
   :validation-amount-invalid-number      "Το ποσό δεν είναι έγκυρος αριθμός"
   :validation-amount-is-too-precise      "Το ποσό είναι πολύ ακριβές. Η μικρότερη μονάδα που μπορείτε να στείλετε είναι 1 Wei (1x10^-18 ETH)"



   ;; network settings
   :new-network                           "Νέο δίκτυο"
   :add-network                           "Προσθήκη δίκτυο"
   :add-new-network                       "Προσθήκη νέου δικτύου"
   :existing-networks                     "Υπάρχον δίκτυα"
   :add-json-file                         "Προσθήκη ενός JSON αρχείου"
   :paste-json-as-text                    "Επικόλληση JSON ως κείμενο"
   :paste-json                            "Επικόλληση JSON"
   :specify-rpc-url                       "Καθορίστε ένα RPC URL"
   :edit-network-config                   "Επεξεργασία δίκτυο config"
   :connected                             "Συνδεδεμένος"
   :process-json                          "Επεξεργασία JSON"
   :error-processing-json                 "Σφάλμα επεξεργασίας JSON"
   :rpc-url                               "RPC URL"
   :remove-network                        "Αφαίρεση δίκτυου"
   :network-settings                      "Ρυθμίσεις δικτύου"
   :edit-network-warning                  "Προσοχή, η επεξεργασία των δεδομένων δικτύου ενδέχεται να απενεργοποιήσει αυτό το δίκτυο για εσάς"
   :connecting-requires-login             "Η σύνδεση σε άλλο δίκτυο απαιτεί σύνδεση"
   :close-app-title                       "Προειδοποίηση!"
   :close-app-content                     "Η εφαρμογή θα σταματήσει και θα κλείσει. Όταν το ξανανοίξετε, το επιλεγμένο δίκτυο θα χρησιμοποιηθεί"
   :close-app-button                      "Επιβεβαιώστε"})

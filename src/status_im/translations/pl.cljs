(ns status-im.translations.pl)

(def translations
  {
   ;common
   :members-title                         "Użytkownicy"
   :not-implemented                       "nie wprowadzono"
   :chat-name                             "Nazwa czatu"
   :notifications-title                   "Powiadomienia i dźwięki"
   :offline                               "Offline"

   ;drawer
   :switch-users                          "Przełącz użytkowników"

   ;chat
   :is-typing                             "wpisuje tekst"
   :and-you                               "i ty"
   :search-chat                           "Przeszukaj czat"
   :members                               {:one   "1 użytkownik"
                                           :other "Użytkownicy: {{count}}"
                                           :zero  "brak użytkowników "}
   :members-active                        {:one   "1 użytkownik, 1 aktywny"
                                           :other "Użytkownicy:{{count}}, aktywni:{{count}}"
                                           :zero  "brak użytkowników"}
   :active-online                         "Online"
   :active-unknown                        "Nieznany"
   :available                             "Dostępny"
   :no-messages                           "Brak wiadomości"
   :suggestions-requests                  "Żądania"
   :suggestions-commands                  "Polecenia"

   ;sync
   :sync-in-progress                      "Synchronizacja..."
   :sync-synced                           "W synchronizacji"

   ;messages
   :status-sending                        "Wysyłanie"
   :status-pending                        "W oczekiwaniu"
   :status-sent                           "Wysłano"
   :status-seen-by-everyone               "Przeczytane przez wszystkich"
   :status-seen                           "Przeczytane"
   :status-delivered                      "Dostarczono"
   :status-failed                         "Niepowodzenie"

   ;datetime
   :datetime-second                       {:one   "sekunda"
                                           :other "sekund(y)"}
   :datetime-minute                       {:one   "minuta"
                                           :other "minut(y)"}
   :datetime-hour                         {:one   "godzina"
                                           :other "godziny(y)"}
   :datetime-day                          {:one   "dzień"
                                           :other "dni"}
   :datetime-ago                          "temu"
   :datetime-yesterday                    "wczoraj"
   :datetime-today                        "dzisiaj"

   ;profile
   :profile                               "Profil"
   :message                               "Wiadomość"
   :not-specified                         "Nie określono"
   :public-key                            "Klucz publiczny"
   :phone-number                          "Numer telefonu "
   :add-to-contacts                       "Dodaj do kontaktów"

   ;;make_photo
   :image-source-title                    "Zdjęcie profilowe "
   :image-source-make-photo               "Przechwyć"
   :image-source-gallery                  "Wybierz z galerii "

   ;sign-up
   :contacts-syncronized                  "Twoje kontakty zostały zsynchronizowane"
   :confirmation-code                     (str "Dziękujemy! Wysłaliśmy ci SMS-a z kodem"
                                               "potwierdzającym. Prosimy o podanie kodu w celu zweryfikowania swojego numeru telefonu")
   :incorrect-code                        (str "Przepraszamy, kod jest nieprawidłowy. Prosimy wprowadzić kod ponownie")
   :phew-here-is-your-passphrase          "*Uff*, to nie było łatwe. Oto twoje specjalne hasło, *zapisz je i	 przechowuj w bezpiecznym miejscu!* Będzie ci potrzebne podczas procedury odzyskiwania konta."
   :here-is-your-passphrase               "Oto twoje specjalne hasło, *zapisz je i przechowuj w bezpiecznym miejscu!* Będzie ci potrzebne podczas procedury odzyskiwania konta."
   :phone-number-required                 "Dotknij tutaj, aby wprowadzić swój numer telefonu, a my znajdziemy twoich znajomych "
   :intro-status                          "Porozmawiaj ze mną na czacie, aby skonfigurować swoje konto i zmienić ustawienia!"
   :intro-message1                        "Witamy w sekcji Status\nWybierz tę wiadomość, aby ustawić hasło i rozpocząć!"
   :account-generation-message            "Daj mi chwilkę, muszę wykonać szalone obliczenia, żeby utworzyć dla Ciebie konto!"

   ;chats
   :chats                                 "Czaty"
   :new-group-chat                        "Nowy czat grupowy"

   ;discover
   :discover                              "Odkryte"
   :none                                  "Brak"
   :search-tags                           "Tutaj wpisz swoje tagi wyszukiwania"
   :popular-tags                          "Popularne tagi"
   :recent                                "Najnowsze"
   :no-statuses-discovered                "Nie odkryto statusów"

   ;settings
   :settings                              "Ustawienia"

   ;contacts
   :contacts                              "Kontakty"
   :new-contact                           "Nowy kontakt"
   :contacts-group-new-chat               "Rozpocznij nowy czat"
   :no-contacts                           "W tej chwili brak kontaktów"
   :show-qr                               "Pokaż QR"

   ;group-settings
   :remove                                "Usuń"
   :save                                  "Zapisz"
   :clear-history                         "Wyczyść historię"
   :chat-settings                         "Ustawienia czatu"
   :edit                                  "Edytuj"
   :add-members                           "Dodaj użytkowników"

   ;commands
   :chat-send-eth                         "{{amount}} ETH"

   ;new-group

   ;participants

   ;protocol
   :received-invitation                   "otrzymano zaproszenie na czat"
   :removed-from-chat                     "usunięto z czatu grupowego"
   :left                                  "pozostało"
   :invited                               "zaproszeni"
   :removed                               "usunięci"
   :You                                   "Ty"

   ;new-contact
   :add-new-contact                       "Dodaj nowy kontakt"
   :scan-qr                               "Skanuj QR"
   :name                                  "Nazwa"
   :address-explication                   "Być może tutaj powinien znajdować się tekst wyjaśniający, czym jest adres i gdzie go szukać"
   :contact-already-added                 "Kontakt został już dodany"
   :can-not-add-yourself                  "Nie możesz dodać samego siebie"
   :unknown-address                       "Nieznany adres"


   ;login
   :connect                               "Połącz"
   :address                               "Adres"
   :password                              "Hasło"
   :wrong-password                        "Nieprawidłowe hasło"

   ;recover
   :passphrase                            "Hasło specjalne"
   :recover                               "Odzyskaj"

   ;accounts
   :recover-access                        "Odzyskaj dostęp"

   ;wallet-qr-code
   :done                                  "Zrobione"
   :main-wallet                           "Portfel główny"

   ;validation
   :invalid-phone                         "Nieprawidłowy numer telefonu"
   :amount                                "Kwota"
   ;transactions
   :status                                "Status"
   :recipient                             "Odbiorca"

   ;:webview
   :web-view-error                        "oj, mamy błąd"

   :confirm                               "Potwierdź"
   :phone-national                        "Krajowy"
   :public-group-topic                    "Temat"
   :debug-enabled                         "Uruchomiono serwer debugowania! Możesz teraz dodać program DApp, uruchamiając *status-dev-cli scan* na swoim komputerze"
   :new-public-group-chat                 "Dołącz do publicznego czatu"
   :datetime-ago-format                   "{{number}} {{time-intervals}} {{ago}}"
   :sharing-cancel                        "Anuluj"
   :twelve-words-in-correct-order         "12 słów w prawidłowym porządku"
   :remove-from-contacts                  "Usuń z kontaktów"
   :delete-chat                           "Usuń czat"
   :edit-chats                            "Edytuj czaty"
   :sign-in                               "Zaloguj się"
   :create-new-account                    "Utwórz nowe konto"
   :sign-in-to-status                     "Zaloguj się do Statusu"
   :got-it                                "Rozumiem"
   :move-to-internal-failure-message      "Musimy przenieść ważne pliki z zewnętrznego na wewnętrzny nośnik danych. W tym celu potrzebujemy Twojego pozwolenia. W przyszłych wersjach zewnętrzne nośniki danych nie będą stosowane."
   :edit-group                            "Edytuj grupę"
   :delete-group                          "Usuń grupę"
   :browsing-title                        "Przeglądaj"
   :reorder-groups                        "Przestaw grupy"
   :browsing-cancel                       "Anuluj"
   :faucet-success                        "Odebrano prośbę dotyczącą kranu"
   :choose-from-contacts                  "Wybierz spośród kontaktów"
   :new-group                             "Nowa grupa"
   :phone-e164                            "Międzynarodowy 1"
   :remove-from-group                     "Usuń z grupy"
   :search-contacts                       "Przeszukaj kontakty"
   :transaction                           "Transakcja"
   :public-group-status                   "Publiczny"
   :leave-chat                            "Opuść czat"
   :start-conversation                    "Rozpocznij rozmowę"
   :topic-format                          "Nieprawidłowy format [a-z0-9\\-]+"
   :enter-valid-public-key                "Wprowadź prawidłowy klucz publiczny lub zeskanuj kod QR"
   :faucet-error                          "Błąd wysyłania prośby dotyczącej kranu"
   :phone-significant                     "Ważne"
   :search-for                            "Wyszukaj..."
   :sharing-copy-to-clipboard             "Skopiuj do schowka"
   :phone-international                   "Międzynarodowy 2"
   :enter-address                         "Wprowadź adres"
   :send-transaction                      "Wyślij transakcję"
   :delete-contact                        "Usuń kontakt"
   :mute-notifications                    "Wycisz powiadomienia"


   :contact-s                             {:one   "kontakt"
                                           :other "kontakty"}
   :next                                  "Dalej"
   :from                                  "Z"
   :search-chats                          "Przeszukaj czaty"
   :in-contacts                           "W kontaktach"

   :sharing-share                         "Udostępnij..."
   :type-a-message                        "Wpisz wiadomość..."
   :type-a-command                        "Zacznij wpisywać komendę..."
   :shake-your-phone                      "Znalazłeś błąd lub masz pytanie? Wystarczy ~potrząsnąć~ telefonem!"
   :status-prompt                         "Utwórz status, aby inni mogli dowiedzieć się, co oferujesz. Możesz również wykorzystać #hashtagi."
   :add-a-status                          "Dodaj status..."
   :error                                 "Błąd"
   :edit-contacts                         "Edytuj kontakty"
   :more                                  "więcej"
   :cancel                                "Anuluj"
   :no-statuses-found                     "Nie znaleziono statusów"
   :browsing-open-in-web-browser          "Otwórz w przeglądarce"
   :delete-group-prompt                   "To nie będzie miało wpływu na kontakty"
   :edit-profile                          "Edytuj profil"


   :empty-topic                           "Pusty temat"
   :to                                    "Do"
   :data                                  "Dane"})
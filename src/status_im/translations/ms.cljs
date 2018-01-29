(ns status-im.translations.ms)

(def translations
  {
   ;common
   :members-title                         "Ahli"
   :not-implemented                       "!tidak diimplementasikan"
   :chat-name                             "Nama perbualan"
   :notifications-title                   "Pemberitahuan dan bunyi"
   :offline                               "Luar talian"
   :search-for                            "Cari..."
   :cancel                                "Batal"
   :next                                  "Seterusnya"
   :type-a-message                        "Taip mesej..."
   :type-a-command                        "Mulakan menaip arahan..."
   :error                                 "Gagal"

   :camera-access-error                   "Untuk memberi keizinan akses kamera yang diperlukan, sila pergi ke aturan sistem anda dan pastikan bahawa Status > Kamera telah dipilih."
   :photos-access-error                   "Untuk memberi keizinan akses gambar yang diperlukan, sila pergi ke aturan sistem anda dan pastikan bahawa Status > Gambar telah dipilih."

   ;drawer
   :switch-users                          "Tukar pengguna"
   :current-network                       "Rangkaian sekarang"

   ;chat
   :is-typing                             "sedang menaip"
   :and-you                               "dan anda"
   :search-chat                           "Cari perbualan"
   :members                               {:one   "1 ahli"
                                           :other "{{count}} ahli"
                                           :zero  "tiada ahli"}
   :members-active                        {:one   "1 ahli"
                                           :other "{{count}} ahli"
                                           :zero  "tiada ahli"}
   :public-group-status                   "Umum"
   :active-online                         "Dalam talian"
   :active-unknown                        "Tidak diketahui"
   :available                             "Ada"
   :no-messages                           "Tiada mesej"
   :suggestions-requests                  "Permintaan"
   :suggestions-commands                  "Arahan"
   :faucet-success                        "Permintaan Faucet telah diterima"
   :faucet-error                          "Permintaan Faucet gagal"

   ;sync
   :sync-in-progress                      "sedang disegerakan..."
   :sync-synced                           "Dalam penyegeraan"

   ;messages
   :status-sending                        "Menghantar"
   :status-pending                        "Belum selesai"
   :status-sent                           "Dihantar"
   :status-seen-by-everyone               "Dilihat oleh semua"
   :status-seen                           "Dilihat"
   :status-delivered                      "Dihantar"
   :status-failed                         "Gagal"

   ;datetime
   :datetime-ago-format                   "{{number}} {{time-intervals}} {{ago}}"
   :datetime-second                       {:one   "saat"
                                           :other "saat"}
   :datetime-minute                       {:one   "minit"
                                           :other "minit"}
   :datetime-hour                         {:one   "jam"
                                           :other "jam"}
   :datetime-day                          {:one   "hari"
                                           :other "hari"}
   :datetime-ago                          "lalu"
   :datetime-yesterday                    "semalam"
   :datetime-today                        "hari ini"

   ;profile
   :profile                               "Profail"
   :edit-profile                          "Ubah profail"
   :message                               "Mesej"
   :not-specified                         "Tidak diberikan"
   :public-key                            "Public key"
   :phone-number                          "Nombor telefon"
   :update-status                         "Kemaskini status anda..."
   :add-a-status                          "Tambah pada status..."
   :status-prompt                         "Ciptakan satu status untuk membantu pengguna lain mengetahui tentang perkara yang anda tawarkan. Anda juga boleh menggunakan #hashtag. "
   :add-to-contacts                       "Tambah pada senarai kenalan"
   :in-contacts                           "Dalam senarai kenalan"
   :remove-from-contacts                  "Buang dari senarai kenalan"
   :start-conversation                    "Mulakan perbualan"
   :send-transaction                      "Hantar transaksi"

   ;;make_photo
   :image-source-title                    "Gambar profail"
   :image-source-make-photo               "Rakam"
   :image-source-gallery                  "Pilih dari galeri"

   ;;sharing
   :sharing-copy-to-clipboard             "Salin ke clipboard"
   :sharing-share                         "Kongsi..."
   :sharing-cancel                        "Batal"

   :browsing-title                        "Pelayar"
   :browsing-open-in-web-browser          "Buka dalam pelayar web"
   :browsing-cancel                       "Batal"

   ;sign-up
   :contacts-syncronized                  "Senarai kenalan anda telah disegerakan"
   :confirmation-code                     (str "Terima kasih! Kami telah menghantar kepada anda satu mesej mengandungi kod"
                                               "pengesahan. Sila berikan kod tersebut untuk mengesahkan nombor telefon anda")
   :incorrect-code                        (str "Maaf kod salah, sila masukkan sekali lagi")
   :phew-here-is-your-passphrase          "*Wah* itu sangatlah susah, ini adalah ayat pengesahan anda, *sila tulis dan pastikan ini selamat!* Anda akan memerlukan ia untuk memulihkan akses ke akaun anda."
   :here-is-your-passphrase               "Ini adalah ayat pengesahan anda, *sila tulis dan pastikan ini selamat!* Anda akan memerlukan ia untuk memulihkan akses ke akaun anda."
   :phone-number-required                 "Sentuh disini untuk memasukkan nombor telefon anda & saya akan mecari rakan anda"
   :shake-your-phone                      "Jumpa kerentanan atau terdapat cadangan? Hanya ~goncang~ telefon anda!"
   :intro-status                          "Mulakan perbualan dengan saya untuk mencipta akaun anda dan mengubah aturan akaun anda!"
   :intro-message1                        "Selamat datang ke Status\nSila sentuh mesej ini untuk menetapkan kata lalaun & memulakan!"
   :account-generation-message            "Berikan saya sedikit masa, saya perlu menyelesaikan masalah matematik yang rumit untuk menciptakan akaun anda!"
   :move-to-internal-failure-message      "Kita perlu mengubah lokasi sedikit fail penting dari memori dalaman ke memori luaran. Untuk itu, kami perlukan kebenaran anda. Kami tidak akan menggunakan memori luaran pada versi akan datang."
   :debug-enabled                         "Debug server telah dijalankan! Sekarang anda boleh menjalankan *status-dev-cli scan* untuk mecari sambungan server dari komputer anda dalam jaringan yang sama."

   ;phone types
   :phone-e164                            "Internasional 1"
   :phone-international                   "Internasional 2"
   :phone-national                        "Nasional"
   :phone-significant                     "Penting"

   ;chats
   :chats                                 "Perbualan"
   :delete-chat                           "Padam perbualan"
   :new-group-chat                        "Perbualan kumpulan baru"
   :new-public-group-chat                 "Sertai perbualan umum"
   :edit-chats                            "Ubah perbualan"
   :search-chats                          "Cari perbualan"
   :empty-topic                           "Tiada topik"
   :topic-format                          "Format salah [a-z0-9\\-]+"
   :public-group-topic                    "Topik"

   ;discover
   :discover                              "Jelajah"
   :none                                  "Tiada"
   :search-tags                           "Taip carian anda disini"
   :popular-tags                          "Popular"
   :recent                                "Terbaru"
   :no-statuses-discovered                "Tiada status ditemui"
   :no-statuses-found                     "Tiada status ditemui"

   ;settings
   :settings                              "Aturan"

   ;contacts
   :contacts                              "Kenalan"
   :new-contact                           "Kenalan baru"
   :delete-contact                        "Padam kenalan"
   :delete-contact-confirmation           "Kenalan ini akan dipadam dari senarai kenalan anda"
   :remove-from-group                     "Buang dari kumpulan"
   :edit-contacts                         "Ubah kenalan"
   :search-contacts                       "Cari kenalan"
   :contacts-group-new-chat               "Mulakan perbualan baru"
   :choose-from-contacts                  "Pilih dari kenalan"
   :no-contacts                           "Masih tiada kenalan"
   :show-qr                               "Tunjuk kod QR"
   :enter-address                         "Masukkan address"
   :more                                  "seterusnya"

   ;group-settings
   :remove                                "Buang"
   :save                                  "Simpan"
   :delete                                "Padam"
   :clear-history                         "Padam sejarah perbualan"
   :mute-notifications                    "Pemberitahuan senyap"
   :leave-chat                            "Tinggalkan perbualan"
   :chat-settings                         "Aturan perbualan"
   :edit                                  "Ubah"
   :add-members                           "Tambah ahli"

   ;commands
   :chat-send-eth                         "{{amount}} ETH"

   ;new-group
   :new-group                             "Kumpulan baru"
   :reorder-groups                        "Susun kumpulan"
   :edit-group                            "Ubah kumpulan"
   :delete-group                          "Padam kumpulan"
   :delete-group-confirmation             "Kumpulan ini akan dipadam dari senarai kumpulan anda. Ini tidak akan menjejaskan senarai kenalan anda"
   :delete-group-prompt                   "Ini tidak akan menjejaskan senarai kenalan anda"
   :contact-s                             {:one   "kenalan"
                                           :other "kenalan"}
   ;participants

   ;protocol
   :received-invitation                   "menerima permintaan bual"
   :removed-from-chat                     "membuang anda dari perbualan kumpulan"
   :left                                  "pergi"
   :invited                               "dijemput"
   :removed                               "dibuang"
   :You                                   "Anda"

   ;new-contact
   :add-new-contact                       "Tambah kenalan baru"
   :scan-qr                               "Imbas kod QR"
   :name                                  "Nama"
   :address-explication                   "Mungkin disini sepatutnya terdapat sedikit teks menjelaskan apa itu address dan dimana untuk melihatnya"
   :enter-valid-public-key                "Sila masukkan public key yang sah atau imbas kod QR"
   :contact-already-added                 "Kenalan telah ditambah"
   :can-not-add-yourself                  "Anda tidak boleh menambah diri anda sendiri"
   :unknown-address                       "Address tidak diketahui"


   ;login
   :connect                               "Sambung"
   :address                               "Address"
   :password                              "Kata laluan"
   :sign-in-to-status                     "Daftar masuk ke Status"
   :sign-in                               "Daftar masuk"
   :wrong-password                        "Kata laluan salah"

   ;recover
   :passphrase                            "Ayat pengesahan"
   :recover                               "Pulihkan"
   :twelve-words-in-correct-order         "12 perkataan dalam susunan yang betul"

   ;accounts
   :recover-access                        "Pulihkan akses"
   :create-new-account                    "Cipta akaun baru"

   ;wallet-qr-code
   :done                                  "Selesai"
   :main-wallet                           "Dompet utama"

   ;validation
   :invalid-phone                         "Nombor telefon salah"
   :amount                                "Jumlah"
   ;transactions
   :confirm                               "Sahkan"
   :transaction                           "Transaksi"
   :status                                "Status"
   :recipient                             "Penerima"
   :to                                    "Ke"
   :from                                  "Dari"
   :data                                  "Maklumat"
   :got-it                                "Ya"

   ;:webview
   :web-view-error                        "oops, gagal"})

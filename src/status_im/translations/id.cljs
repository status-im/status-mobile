(ns status-im.translations.id)

(def translations
  {
   ;common
   :members-title                         "anggota"
   :not-implemented                       "tidak diimplementasikan !"
   :chat-name                             "nama obrolan"
   :notifications-title                   "judul notifikasi"
   :offline                               "offline"

   ;drawer
   :invite-friends                        "undang teman"
   :faq                                   "faq"
   :switch-users                          "beralih pengguna"

   ;chat
   :is-typing                             "sedang mengetik"
   :and-you                               "dan kamu"
   :search-chat                           "cari obrolan"
   :members                               {:one   "1 orang"
                                           :other "anggota {{count}}orang"
                                           :zero  "Tidak ada anggota"}
   :members-active                        {:one   "1 anggota, 1 aktivitas"
                                           :other "anggota {{count}}orang, aktivitas {{count}}orang"
                                           :zero  "Tidak ada anggota"}
   :active-online                         "online"
   :active-unknown                        "tidak diketahui"
   :available                             "tersedia"
   :no-messages                           "tidak ada pesan"
   :suggestions-requests                  "permintaan"
   :suggestions-commands                  "perintah"

   ;sync
   :sync-in-progress                      "sinkronisasi..."
   :sync-synced                           "disinkronisasi"

   ;messages
   :status-sending                        "mengirim"
   :status-pending                        "pending"
   :status-sent                           "dikirim"
   :status-seen-by-everyone               "terlihat oleh semua"
   :status-seen                           "terlihat"
   :status-delivered                      "terkirim"
   :status-failed                         "gagal"

   ;datetime
   :datetime-second                       {:one   "detik"
                                           :other "detik"}
   :datetime-minute                       {:one   "menit"
                                           :other "menit"}
   :datetime-hour                         {:one   "jam"
                                           :other "jam"}
   :datetime-day                          {:one   "hari"
                                           :other "hari"}
   :datetime-multiple                     "detik"
   :datetime-ago                          "dari"
   :datetime-yesterday                    "kemarin"
   :datetime-today                        "hari ini"

   ;profile
   :profile                               "profil"
   :report-user                           "laporkan penguna"
   :message                               "pesan"
   :username                              "nama pengguna"
   :not-specified                         "tidak ditentukan"
   :public-key                            "public key"
   :phone-number                          "nomor telepon"
   :email                                 "email"
   :profile-no-status                     "tidak ada informasi status"
   :add-to-contacts                       "tambahkan ke Kontak"
   :error-incorrect-name                  "nama tidak valid"
   :error-incorrect-email                 "email tidak valid"

   ;;make_photo
   :image-source-title                    "foto profil"
   :image-source-make-photo               "capture"
   :image-source-gallery                  "pilih dari Galeri"
   :image-source-cancel                   "batalkan"

   ;sign-up
   :contacts-syncronized                  "kontak telah disinkronkan"
   :confirmation-code                     (str "terima kasih! Kami telah mengirim pesan teks untuk mengkonfirmasi akun anda "
                                               "kode. untuk mengkonfirmasi nomor telepon Anda, masukkan kode ini")
   :incorrect-code                        (str "maaf, kode yang anda masukan salah. Silakan coba lagi")
   :generate-passphrase                   (str "generate passphrase "
                                               "agar Anda dapat memulihkan akses atau login pada perangkat lain")
   :phew-here-is-your-passphrase          "* wah * hal yang rumit, inilah passphrase anda, * simpan dan jaga baik - baik! * anda akan membutuhkannya untuk mengembalikan akun Anda."
   :here-is-your-passphrase               "berikut adalah passphrase anda. * jaga agar tetap aman dan menyimpannya! * bila anda perlu untuk memulihkan akun Anda."
   :written-down                          "pastikan Anda telah menulisnya dengan aman"
   :phone-number-required                 "ketuk di sini untuk memasukkan nomor telepon Anda, anda akan menemukan teman-teman anda"
   :intro-status                          "berbicara dengan saya untuk mengatur akun Anda dan mengubah pengaturan Anda!"
   :intro-message1                        "selamat datang di Status \n klik pesan ini untuk mengatur sandi Anda & memulai!"
   :account-generation-message            "beri kami waktu, kami harus membuat perhitungan super untuk menghasilkan akun anda sendiri"

   ;chats
   :chats                                 "obrolan"
   :new-chat                              "obrolan baru"
   :new-group-chat                        "obrolan grup baru"

   ;discover
   :discover                              "mendeteksi"
   :none                                  "apa saja"
   :search-tags                           "silakan memasukkan pencarian di tag"
   :popular-tags                          "tag populer"
   :recent                                "terbaru"
   :no-statuses-discovered                "informasi status tidak ditemukan"

   ;settings
   :settings                              "pengaturan"

   ;contacts
   :contacts                              "kontak"
   :new-contact                           "kontak baru"
   :show-all                              "tampilkan semua"
   :contacts-group-dapps                  "ÐApps"
   :contacts-group-people                 "pengguna"
   :contacts-group-new-chat               "untuk memulai obrolan baru"
   :no-contacts                           "tidak ada kontak"
   :show-qr                               "show QR kode"

   ;group-settings
   :remove                                "menghapus"
   :save                                  "menyimpan"
   :change-color                          "ubah warna"
   :clear-history                         "bersihkan riwayat"
   :delete-and-leave                      "hapus dan tinggalkan"
   :chat-settings                         "pengaturan obrolan"
   :edit                                  "edit"
   :add-members                           "tambah anggota"
   :blue                                  "biru"
   :purple                                "ungu"
   :green                                 "hijau"
   :red                                   "merah"

   ;commands
   :money-command-description             "kirim dana"
   :location-command-description          "kirim lokasi"
   :phone-command-description             "kirim nomor telepon"
   :phone-request-text                    "meminta nomor telepon"
   :confirmation-code-command-description "kirim kode verifikasi"
   :confirmation-code-request-text        "meminta kode konfirmasi"
   :send-command-description              "kirim lokasi"
   :request-command-description           "kirim permintaan"
   :keypair-password-command-description  ""
   :help-command-description              "bantuan"
   :request                               "permintaan"
   :chat-send-eth                         "{{amount}} ETH"
   :chat-send-eth-to                      "{{amount}} ETH untuk: {{chat-name}}"
   :chat-send-eth-from                    "{{amount}} ETH dari: {{chat-name}}"

   ;new-group
   :group-chat-name                       "nama obrolan"
   :empty-group-chat-name                 "silakan masukkan nama"
   :illegal-group-chat-name               "silakan pilih nama lain"

   ;participants
   :add-participants                      "tambahkan peserta"
   :remove-participants                   "hapus peserta"

   ;protocol
   :received-invitation                   "telah menerima undangan untuk obrolan"
   :removed-from-chat                     "anda dihapus dari grup obrolan"
   :left                                  "tinggalkan"
   :invited                               "undang"
   :removed                               "hapus"
   :You                                   "kamu"

   ;new-contact
   :add-new-contact                       "menambahkan kontak baru"
   :import-qr                             "import"
   :scan-qr                               "memindai kode QR ini"
   :name                                  "nama"
   :whisper-identity                      "whisper ID"
   :address-explication                   "mungkin harus ada beberapa konten untuk menjelaskan alamat ini dan di mana menemukannya"
   :enter-valid-address                   "harap masukkan alamat valid atau pindai kode QR"
   :contact-already-added                 "Kontak yang telah ditambahkan"
   :can-not-add-yourself                  "tidak dapat menambahkan anda sendiri"
   :unknown-address                       "alamat tidak diketahui"


   ;login
   :connect                               "terhubung"
   :address                               "alamat"
   :password                              "kata sandi"
   :login                                 "masuk"
   :wrong-password                        "kata sandi salah"

   ;recover
   :recover-from-passphrase               "pulihkan dari passphrase"
   :recover-explain                       "masukkan kata sandi anda untuk memulihkan akses"
   :passphrase                            "passphrase"
   :recover                               "pemulihan"
   :enter-valid-passphrase                "masukkan valid passphrase"
   :enter-valid-password                  "masukkan kata sandi yang valid"

   ;accounts
   :recover-access                        "akses Pemulihan"
   :add-account                           "tambahkan Akun"

   ;wallet-qr-code
   :done                                  "lengkap"
   :main-wallet                           "wallet utama"

   ;validation
   :invalid-phone                         "nomor telepon salah"
   :amount                                "jumlah dana"
   :not-enough-eth                        (str "balance ETH tidak cukup"
                                               "({{balance}} ETH)")
   ;transactions
   :confirm-transactions                  {:one   "konfirmasi transaksi"
                                           :other "{{count}} transaksi terkonfirmasi"
                                           :zero  "tidak ada transaksi"}
   :status                                "kondisi"
   :pending-confirmation                  "konfirmasi pending"
   :recipient                             "penerima"
   :one-more-item                         "tambahkan lagi item "
   :fee                                   "fee"
   :value                                 "nilai"

   ;:webview
   :web-view-error                        "oopss, error"})

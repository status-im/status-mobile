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
   :datetime-ago                          "dari"
   :datetime-yesterday                    "kemarin"
   :datetime-today                        "hari ini"

   ;profile
   :profile                               "profil"
   :message                               "pesan"
   :not-specified                         "tidak ditentukan"
   :public-key                            "public key"
   :phone-number                          "nomor telepon"
   :add-to-contacts                       "tambahkan ke Kontak"

   ;;make_photo
   :image-source-title                    "foto profil"
   :image-source-make-photo               "capture"
   :image-source-gallery                  "pilih dari Galeri"

   ;sign-up
   :contacts-syncronized                  "kontak telah disinkronkan"
   :confirmation-code                     (str "terima kasih! Kami telah mengirim pesan teks untuk mengkonfirmasi akun anda "
                                               "kode. untuk mengkonfirmasi nomor telepon Anda, masukkan kode ini")
   :incorrect-code                        (str "maaf, kode yang anda masukan salah. Silakan coba lagi")
   :phew-here-is-your-passphrase          "* wah * hal yang rumit, inilah passphrase anda, * simpan dan jaga baik - baik! * anda akan membutuhkannya untuk mengembalikan akun Anda."
   :here-is-your-passphrase               "berikut adalah passphrase anda. * jaga agar tetap aman dan menyimpannya! * bila anda perlu untuk memulihkan akun Anda."
   :phone-number-required                 "ketuk di sini untuk memasukkan nomor telepon Anda, anda akan menemukan teman-teman anda"
   :intro-status                          "berbicara dengan saya untuk mengatur akun Anda dan mengubah pengaturan Anda!"
   :intro-message1                        "selamat datang di Status \n klik pesan ini untuk mengatur sandi Anda & memulai!"
   :account-generation-message            "beri kami waktu, kami harus membuat perhitungan super untuk menghasilkan akun anda sendiri"

   ;chats
   :chats                                 "obrolan"
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
   :contacts-group-new-chat               "untuk memulai obrolan baru"
   :no-contacts                           "tidak ada kontak"
   :show-qr                               "show QR kode"

   ;group-settings
   :remove                                "menghapus"
   :save                                  "menyimpan"
   :clear-history                         "bersihkan riwayat"
   :chat-settings                         "pengaturan obrolan"
   :edit                                  "edit"
   :add-members                           "tambah anggota"

   ;commands
   :chat-send-eth                         "{{amount}} ETH"

   ;new-group

   ;participants

   ;protocol
   :received-invitation                   "telah menerima undangan untuk obrolan"
   :removed-from-chat                     "anda dihapus dari grup obrolan"
   :left                                  "tinggalkan"
   :invited                               "undang"
   :removed                               "hapus"
   :You                                   "kamu"

   ;new-contact
   :add-new-contact                       "menambahkan kontak baru"
   :scan-qr                               "memindai kode QR ini"
   :name                                  "nama"
   :address-explication                   "mungkin harus ada beberapa konten untuk menjelaskan alamat ini dan di mana menemukannya"
   :contact-already-added                 "Kontak yang telah ditambahkan"
   :can-not-add-yourself                  "tidak dapat menambahkan anda sendiri"
   :unknown-address                       "alamat tidak diketahui"


   ;login
   :connect                               "terhubung"
   :address                               "alamat"
   :password                              "kata sandi"
   :wrong-password                        "kata sandi salah"

   ;recover
   :passphrase                            "passphrase"
   :recover                               "pemulihan"

   ;accounts
   :recover-access                        "akses Pemulihan"

   ;wallet-qr-code
   :done                                  "lengkap"
   :main-wallet                           "wallet utama"

   ;validation
   :invalid-phone                         "nomor telepon salah"
   :amount                                "jumlah dana"
   ;transactions
   :status                                "kondisi"
   :recipient                             "penerima"

   ;:webview
   :web-view-error                        "oopss, error"})

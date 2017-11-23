(ns status-im.translations.th)

(def translations
  {
   ;common
   :members-title                         "สมาชิก"
   :not-implemented                       "!ยังไม่ได้ดำเนินการ"
   :chat-name                             "ชื่อแชท"
   :notifications-title                   "การแจ้งเตือนและเสียง"
   :offline                               "ออฟไลน์"

   ;drawer
   :switch-users                          "สลับผู้ใช้"

   ;chat
   :is-typing                             "กำลังพิมพ์"
   :and-you                               "และคุณ"
   :search-chat                           "ค้นหาแชท"
   :members                               {:one   "1 สมาชิก"
                                           :other "{{count}} สมาชิก"
                                           :zero  "ไม่มีสมาชิก"}
   :members-active                        {:one   "1 สมาชิก, 1 ใช้งานอยู่"
                                           :other "{{count}} สมาชิก, {{count}} ใช้งานอยู่"
                                           :zero  "ไม่มีสมาชิก"}
   :active-online                         "ออนไลน์"
   :active-unknown                        "ไม่ทราบ"
   :available                             "มีให้ใช้ได้"
   :no-messages                           "ไม่มีข้อความ"
   :suggestions-requests                  "คำร้องขอ"
   :suggestions-commands                  "คำสั่ง"

   ;sync
   :sync-in-progress                      "กำลังซิงค์..."
   :sync-synced                           "ระหว่างการซิงค์"

   ;messages
   :status-sending                        "กำลังส่ง"
   :status-pending                        "อยู่ระหว่างดำเนินการ"
   :status-sent                           "ส่งแล้ว"
   :status-seen-by-everyone               "อ่านแล้วโดยทุกคน"
   :status-seen                           "อ่านแล้ว"
   :status-delivered                      "ส่งแล้ว"
   :status-failed                         "ล้มเหลว"

   ;datetime
   :datetime-second                       {:one   "วินาที"
                                           :other "วินาที"}
   :datetime-minute                       {:one   "นาที"
                                           :other "นาที"}
   :datetime-hour                         {:one   "ชั่วโมง"
                                           :other "ชั่วโมง"}
   :datetime-day                          {:one   "วัน"
                                           :other "วัน"}
   :datetime-ago                          "ที่ผ่านมา"
   :datetime-yesterday                    "เมื่อวาน"
   :datetime-today                        "วันนี้"

   ;profile
   :profile                               "โปรไฟล์"
   :message                               "ข้อความ"
   :not-specified                         "ไม่ระบุ"
   :public-key                            "คีย์สาธารณะ"
   :phone-number                          "หมายเลขโทรศัพท์"
   :add-to-contacts                       "เพิ่มไปยังผู้ติดต่อ"

   ;;make_photo
   :image-source-title                    "รูปโปรไฟล์"
   :image-source-make-photo               "ถ่ายภาพ"
   :image-source-gallery                  "เลือกจากแกลเลอรี"

   ;sign-up
   :contacts-syncronized                  "ไซิงค์รายชื่อผู้ติดต่อของคุณแล้ว"
   :confirmation-code                     (str "ขอขอบคุณ! เราได้ส่งข้อความตัวอักษรพร้อมรหัสยืนยันแล้ว"
                                               "โปรดมอบรหัสนั้นเพื่อยืนยันหมายเลขโทรศัพท์ของคุณ")
   :incorrect-code                        (str "ขออภัย รหัสไม่ถูกต้อง โปรดกรอกอีกครั้ง")
   :phew-here-is-your-passphrase          "*โล่งอกไปที* มันไม่ง่ายเลย นี่คือวลีรหัสผ่านของคุณ *จดมันไว้และรักษามันให้ปลอดภัย!* คุณจะจำเป็นต้องใช้มันเพื่อกู้คืนบัญชีของคุณ"
   :here-is-your-passphrase               "นี่คือวลีรหัสผ่านของคุณ *จดมันไว้และรักษามันให้ปลอดภัย!* คุณจะจำเป็นต้องใช้มันเพื่อกู้คืนบัญชีของคุณ"
   :phone-number-required                 "แตะที่นี่เพื่อกรอกหมายเลขโทรศัพท์ของคุณ & ฉันจะค้นหาเพื่อนของคุณ"
   :intro-status                          "แชทกับฉันเพื่อตั้งค่าบัญชีของคุณและเปลี่ยนการตั้งค่าของคุณ!"
   :intro-message1                        "ยินดีต้อนรับสู่สถานะ \n แตะข้อความนี้เพื่อตั้งรหัสผ่านของคุณ & เริ่มต้น!"
   :account-generation-message            "ให้เวลาฉันหนึ่งวินาที ฉันจะต้องคำนวณอย่างหนักเพื่อสร้างบัญชีของคุณ!"

   ;chats
   :chats                                 "แชท"
   :new-group-chat                        "แชทกลุ่มใหม่"

   ;discover
   :discover                              "การค้นพบ"
   :none                                  "ไม่มี"
   :search-tags                           "พิมพ์แท็กการค้นหาของคุณที่นี่"
   :popular-tags                          "แท็กยอดนิยม"
   :recent                                "เมื่อเร็ว ๆ นี้"
   :no-statuses-discovered                "ไม่พบสถานะใด ๆ"

   ;settings
   :settings                              "การตั้งค่า"

   ;contacts
   :contacts                              "ผู้ติดต่อ"
   :new-contact                           "ผู้ติดต่อใหม่"
   :contacts-group-new-chat               "เริ่มแชทใหม่"
   :no-contacts                           "ยังไม่มีผู้ติดต่อ"
   :show-qr                               "แสดง QR"

   ;group-settings
   :remove                                "ลบ"
   :save                                  "บันทึก"
   :clear-history                         "ลบประวัติ"
   :chat-settings                         "การตั้งค่าแชท"
   :edit                                  "แก้ไข"
   :add-members                           "เพิ่มสมาชิก"

   ;commands
   :chat-send-eth                         "{{amount}} ETH"

   ;new-group

   ;participants

   ;protocol
   :received-invitation                   "คำเชิญแชทที่ได้รับ"
   :removed-from-chat                     "ลบคุณออกจากแชทกลุ่ม"
   :left                                  "ออกไปแล้ว"
   :invited                               "เชิญแล้ว"
   :removed                               "ลบแล้ว"
   :You                                   "คุณ"

   ;new-contact
   :add-new-contact                       "เพิ่มผู้ติดต่อใหม่"
   :scan-qr                               "สแกน QR"
   :name                                  "ชื่อ"
   :address-explication                   "บางที ในที่นี้คุณควรกรอกข้อความสักเล็กน้อยเพื่อแสดงที่อยู่หรือสถานที่ที่จะมองหามันได้"
   :contact-already-added                 "ได้มีการเพิ่มผู้ติดต่อนี้แล้ว"
   :can-not-add-yourself                  "คุณไม่สามารถเพิ่มตัวคุณเอง"
   :unknown-address                       "ที่อยู่ที่ไม่ทราบ"


   ;login
   :connect                               "เชื่อมต่อ"
   :address                               "ที่อยู่"
   :password                              "รหัสผ่าน"
   :wrong-password                        "รหัสผ่านไม่ถูกต้อง"

   ;recover
   :passphrase                            "วลีรหัสผ่าน"
   :recover                               "กู้คืน"

   ;accounts
   :recover-access                        "กู้คืนการเข้าถึง"

   ;wallet-qr-code
   :done                                  "เสร็จสิ้น"
   :main-wallet                           "กระเป๋าเงินหลัก"

   ;validation
   :invalid-phone                         "หมายเลขโทรศัพท์ไม่ถูกต้อง"
   :amount                                "จำนวน"
   ;transactions
   :status                                "สถานะ"
   :recipient                             "ผู้รับ"

   ;:webview
   :web-view-error                        "อุ๊ย มีข้อผิดพลาด"

   :confirm                               "ยืนยัน"
   :phone-national                        "ระดับชาติ"
   :public-group-topic                    "หัวข้อ"
   :debug-enabled                         "มีการเปิดการดำเนินงานของเซิร์ฟเวอร์ที่ได้แก้ไขข้อบกพร่องแล้ว! ตอนนี้คุณสามารถเพิ่ม DApp ของคุณโดยการเปิดดำเนินการ *status-dev-cli scan* จากคอมพิวเตอร์ของคุณ"
   :new-public-group-chat                 "เข้าร่วมแชทสาธารณะ"
   :datetime-ago-format                   "{{number}} {{time-intervals}} {{ago}}"
   :sharing-cancel                        "ยกเลิก"
   :twelve-words-in-correct-order         "12 คำตามลำดับที่ถูกต้อง"
   :remove-from-contacts                  "ลบออกจากรายชื่อติดต่อ"
   :delete-chat                           "ลบแชท"
   :edit-chats                            "แก้ไขแชท"
   :sign-in                               "ลงชื่อเข้าใช้"
   :create-new-account                    "สร้างบัญชีใหม่"
   :sign-in-to-status                     "ลงชื่อเข้าใช้ในสถานะ"
   :got-it                                "เข้าใจแล้ว"
   :move-to-internal-failure-message      "เราจำเป็นต้องย้ายไฟล์สำคัญบางไฟล์จากที่เก็บข้อมูลภายนอกมาเก็บไว้ในที่เก็บข้อมูลภายใน ในการดำเนินการนี้ เราต้องการการอนุญาตจากคุณ เราจะไม่ใช้ที่เก็บข้อมูลภายนอกสำหรับในเวอร์ชันในอนาคต"
   :edit-group                            "แก้ไขกลุ่ม"
   :delete-group                          "ลบกลุ่ม"
   :browsing-title                        "เบราว์"
   :reorder-groups                        "จัดกลุ่มใหม่"
   :browsing-cancel                       "ยกเลิก"
   :faucet-success                        "ได้รับคำขอไขก๊อกแล้ว"
   :choose-from-contacts                  "เลือกจากรายชื่อติดต่อ"
   :new-group                             "กลุ่มใหม่"
   :phone-e164                            "ระหว่างประเทศ 1"
   :remove-from-group                     "ลบออกจากกลุ่ม"
   :search-contacts                       "ค้นหารายชื่อติดต่อ"
   :transaction                           "การทำธุรกรรม"
   :public-group-status                   "สาธารณะ"
   :leave-chat                            "ออกจากแชท"
   :start-conversation                    "เริ่มการสนทนา"
   :topic-format                          "รูปแบบไม่ถูกต้อง [a-z0-9\\-]+"
   :enter-valid-public-key                "โปรดใส่คีย์สาธารณะที่ถูกต้องหรือสแกนรหัส QR"
   :faucet-error                          "คำขอไขก๊อกผิดพลาด"
   :phone-significant                     "มีความสำคัญ"
   :search-for                            "ค้นหา..."
   :sharing-copy-to-clipboard             "คัดลอกไปยังคลิปบอร์ด"
   :phone-international                   "ระหว่างประเทศ 2"
   :enter-address                         "ใส่ที่อยู่"
   :send-transaction                      "ส่งการทำธุรกรรม"
   :delete-contact                        "ลบรายชื่อติดต่อ"
   :mute-notifications                    "ปิดเสียงการแจ้งเตือน"

   :contact-s
                                          {:one   "รายชื่อติดต่อ"
                                           :other "รายชื่อติดต่อ"}
   :next                                  "ถัดไป"
   :from                                  "จาก"
   :search-chats                          "ค้นหาแชท"
   :in-contacts                           "ในรายชื่อติดต่อ"

   :sharing-share                         "แชร์..."
   :type-a-message                        "พิมพ์ข้อความ..."
   :type-a-command                        "เริ่มพิมพ์คำสั่ง..."
   :shake-your-phone                      "พบข้อบกพร่อง หรือมีข้อเสนอแนะ? เพียง ~เขย่า~ โทรศัพท์ของคุณ!"
   :status-prompt                         "สร้างสถานะเพื่อช่วยให้ผู้คนได้รู้ถึงสิ่งที่คุณกำลังเสนอ คุณสามารถใช้ #hashtags ได้ด้วย"
   :add-a-status                          "เพิ่มสถานะ..."
   :error                                 "ข้อผิดพลาด"
   :edit-contacts                         "แก้ไขรายชื่อติดต่อ"
   :more                                  "เพิ่มเติม"
   :cancel                                "ยกเลิก"
   :no-statuses-found                     "ไม่พบสถานะ"
   :browsing-open-in-web-browser          "เปิดในเว็บเบราว์เซอร์"
   :delete-group-prompt                   "การดำเนินการนี้จะไม่ส่งผลต่อรายชื่อติดต่อ"
   :edit-profile                          "แก้ไขโปรไฟล์"

   :empty-topic                           "หัวข้อว่างเปล่า"
   :to                                    "ถึง"
   :data                                  "ข้อมูล"})
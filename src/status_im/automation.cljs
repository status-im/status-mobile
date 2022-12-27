(ns status-im.automation
  (:require [re-frame.core :as rf :refer [dispatch] :rename {dispatch d}]
            [utils.security.core :as security]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; recover accounts
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; Stimulating Darkseagreen Frenchbulldog
;; chat-key
;; 0x04259170489c0317ae326447d92b48051e103960e5a8d6470840628856b799559b9f6d5f89d86d78459783d9e832c3f154d6fad8d2a1ed84636927230d9b9392d0
(d [:init-root :onboarding])
(d [:multiaccounts.recover.ui/recover-multiaccount-button-pressed])
(d [:status-im.multiaccounts.recover.core/enter-phrase-pressed])
;; add :passphrase to :intro-wizard
(d [:multiaccounts.recover/enter-phrase-input-changed
    (security/mask-data "bridge pride cake piece lucky host perfect require wild light attend click")])
;; import multiaccount into :intro-wizard
(d [:multiaccounts.recover/enter-phrase-next-pressed])
;; change :step in :intro-wizard from :recovery-success to :create-code
(d [:multiaccounts.recover/re-encrypt-pressed])
;; store multiaccount to :db
(d [:multiaccounts.recover/enter-password-next-pressed "testabc"])

;; Rural Electric Bass
;; chat-key
;; 0x041adb97f30458f5c5978f5ee82727f4d8f26f94dbfe23a43f59b938f41e3dec070e35dda4fd4315e244d45cb30f45b98ac0a9bcdd366b5102f3ecad6fe193f677
(d [:init-root :onboarding])
(d [:multiaccounts.recover.ui/recover-multiaccount-button-pressed])
(d [:status-im.multiaccounts.recover.core/enter-phrase-pressed])
(d [:multiaccounts.recover/enter-phrase-input-changed
    (security/mask-data "rose hood father immense claw school crisp goat replace gather razor slab")])
(d [:multiaccounts.recover/enter-phrase-next-pressed])
(d [:multiaccounts.recover/re-encrypt-pressed])
(d [:multiaccounts.recover/enter-password-next-pressed "testabc"])

;; Deep Heavy Marlin
;; chat-key
;; 0x04351fb713ee1fd8b15cc70918ccb852176f6f4068af9928051d2f78148cbd850fa6f72550142f2c8195082799d9ef90b226cd4183c67e0ad56c657c211cd83ed6
(d [:multiaccounts.recover.ui/recover-multiaccount-button-pressed])
(d [:status-im.multiaccounts.recover.core/enter-phrase-pressed])
(d [:multiaccounts.recover/enter-phrase-input-changed
    (security/mask-data
     "town asset resist supply unable slab eyebrow provide huge believe keen weather")])
(d [:multiaccounts.recover/enter-phrase-next-pressed])
(d [:multiaccounts.recover/re-encrypt-pressed])
(d [:multiaccounts.recover/enter-password-next-pressed "testabc"])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 1-1 chat (bass -> bulldog)
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; (def chat-id
;;   "0x04259170489c0317ae326447d92b48051e103960e5a8d6470840628856b799559b9f6d5f89d86d78459783d9e832c3f154d6fad8d2a1ed84636927230d9b9392d0")
(def chat-id
  "0x041adb97f30458f5c5978f5ee82727f4d8f26f94dbfe23a43f59b938f41e3dec070e35dda4fd4315e244d45cb30f45b98ac0a9bcdd366b5102f3ecad6fe193f677")
(def chat-id
  "0x04351fb713ee1fd8b15cc70918ccb852176f6f4068af9928051d2f78148cbd850fa6f72550142f2c8195082799d9ef90b226cd4183c67e0ad56c657c211cd83ed6")
(d [:chat.ui/start-chat chat-id])
(d [:chat.ui/navigate-to-chat chat-id])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; log into accounts
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; log into `bass` account
(d [:multiaccounts.login.ui/multiaccount-selected
    "0x71473d9e9a22275372a7931af8c7cb2f74c8df74977a4f25fdcc61e7f1455b99"])
(d [:set-in [:multiaccounts/login :password] "testabc"])
(d [:multiaccounts.login.ui/password-input-submitted])

;; log into `bulldog` account
(d [:multiaccounts.login.ui/multiaccount-selected
    "0xf994124ce1ce1444f36c17539b47fed056846b44f293ea3e20d566d11a04a2a2"])
(d [:set-in [:multiaccounts/login :password] "testabc"])
(d [:multiaccounts.login.ui/password-input-submitted])

;; log into `marlin` account
(d [:multiaccounts.login.ui/multiaccount-selected
    "0x05b0db564fab9d0d38d87dd193926128f3e69723b50474fbbd6b3c6294699ae4"])
(d [:set-in [:multiaccounts/login :password] "testabc"])
(d [:multiaccounts.login.ui/password-input-submitted])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; events
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(d [:logout])

(d [:bottom-sheet/show-sheet :add-new {}]) ;show bottom sheet
(d [:bottom-sheet/hide]) ;hide bottom sheet
(d [:open-modal :new-chat]) ;open new chat
;; (d [:set-view-id :new-chat])
;; (d [:screens/on-will-focus :new-chat])
;; (d [:status-im.add-new.core/new-chat-focus])
(d [:bottom-sheet/show-sheet])

;; just need these for development =>
;; (d [:open-modal :new-chat2])
(d [:open-modal :new-chat])
(d [:navigate-back])

(d [:open-modal :new-contact])

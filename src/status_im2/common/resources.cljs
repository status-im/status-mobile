(ns status-im2.common.resources
  (:require [quo2.foundations.colors :as colors]))

(def ui
  {:add-new-contact               (js/require "../resources/images/ui2/add-contact.png")
   :desktop-how-to-pair-sign-in   (js/require "../resources/images/ui2/desktop-how-to-pair-sign-in.png")
   :desktop-how-to-pair-logged-in (js/require
                                   "../resources/images/ui2/desktop-how-to-pair-logged-in.png")
   :mobile-how-to-pair-sign-in    (js/require "../resources/images/ui2/mobile-how-to-pair-sign-in.png")
   :mobile-how-to-pair-logged-in  (js/require "../resources/images/ui2/mobile-how-to-pair-logged-in.png")
   :lifestyle                     (js/require "../resources/images/ui2/lifestyle.png")
   :music                         (js/require "../resources/images/ui2/music.png")
   :podcasts                      (js/require "../resources/images/ui2/podcasts.png")
   :generate-keys                 (js/require "../resources/images/ui2/generate-keys.png")
   :ethereum-address              (js/require "../resources/images/ui2/ethereum-address.png")
   :use-keycard                   (js/require "../resources/images/ui2/keycard.png")
   :onboarding-illustration       (js/require "../resources/images/ui2/onboarding_illustration.png")
   :qr-code                       (js/require "../resources/images/ui2/qr-code.png")
   :keycard-logo                  (js/require "../resources/images/ui2/keycard-logo.png")
   :keycard-chip-light            (js/require "../resources/images/ui2/keycard-chip-light.png")
   :keycard-chip-dark             (js/require "../resources/images/ui2/keycard-chip-dark.png")
   :keycard-watermark             (js/require "../resources/images/ui2/keycard-watermark.png")
   :discover                      (js/require "../resources/images/ui2/discover.png")
   :invite-friends                (js/require "../resources/images/ui2/invite-friends.png")
   :no-contacts-light             (js/require "../resources/images/ui2/no-contacts-light.png")
   :no-contacts-dark              (js/require "../resources/images/ui2/no-contacts-dark.png")
   :no-messages-light             (js/require "../resources/images/ui2/no-messages-light.png")
   :no-messages-dark              (js/require "../resources/images/ui2/no-messages-dark.png")})

(def mock-images
  {:coinbase             (js/require "../resources/images/mock2/coinbase.png")
   :collectible          (js/require "../resources/images/mock2/collectible.png")
   :community-banner     (js/require "../resources/images/mock2/community-banner.png")
   :community-logo       (js/require "../resources/images/mock2/community-logo.png")
   :community-cover      (js/require "../resources/images/mock2/community-cover.png")
   :decentraland         (js/require "../resources/images/mock2/decentraland.png")
   :gif                  (js/require "../resources/images/mock2/gif.png")
   :photo1               (js/require "../resources/images/mock2/photo1.png")
   :photo2               (js/require "../resources/images/mock2/photo2.png")
   :photo3               (js/require "../resources/images/mock2/photo3.png")
   :qr-code              (js/require "../resources/images/mock2/qr-code.png")
   :rarible              (js/require "../resources/images/mock2/rarible.png")
   :small-opt-card-icon  (js/require "../resources/images/mock2/small_opt_card_icon.png")
   :small-opt-card-main  (js/require "../resources/images/mock2/small_opt_card_main.png")
   :status-logo          (js/require "../resources/images/mock2/status-logo.png")
   :sticker              (js/require "../resources/images/mock2/sticker.png")
   :ring                 (js/require "../resources/images/mock2/ring.png")
   :user-picture-female2 (js/require "../resources/images/mock2/user_picture_female2.png")
   :user-picture-male4   (js/require "../resources/images/mock2/user_picture_male4.png")
   :user-picture-male5   (js/require "../resources/images/mock2/user_picture_male5.png")})

(def tokens
  {:eth  (js/require "../resources/images/tokens/mainnet/ETH.png")
   :knc  (js/require "../resources/images/tokens/mainnet/KNC.png")
   :mana (js/require "../resources/images/tokens/mainnet/MANA.png")
   :rare (js/require "../resources/images/tokens/mainnet/RARE.png")
   :dai  (js/require "../resources/images/tokens/mainnet/DAI.png")
   :fxc  (js/require "../resources/images/tokens/mainnet/FXC.png")
   :usdt (js/require "../resources/images/tokens/mainnet/USDT.png")
   :snt  (js/require "../resources/images/tokens/mainnet/SNT.png")})
(def parallax-video
  {:biometrics  [(js/require "../resources/videos2/enable_biometrics_0.mp4")
                 (js/require "../resources/videos2/enable_biometrics_1.mp4")
                 (js/require "../resources/videos2/enable_biometrics_2.mp4")
                 (js/require "../resources/videos2/enable_biometrics_3.mp4")]
   :biometrics1 [(js/require "../resources/videos2/Biometrics_Layer_01.mp4")
                 (js/require "../resources/videos2/Biometrics_Layer_02.mp4")
                 (js/require "../resources/videos2/Biometrics_Layer_03.mp4")
                 (js/require "../resources/videos2/Biometrics_Layer_04.mp4")]
   :biometrics2 [(js/require "../resources/videos2/Biometrics_Layer_01.mp4")
                 (js/require "../resources/videos2/Biometrics_Layer_02.mp4")
                 (js/require "../resources/videos2/Biometrics_Layer_03.mp4")
                 (js/require "../resources/videos2/Biometrics_Layer_04.mp4")]
  })

(defn get-mock-image
  [k]
  (get mock-images k))

(defn get-image
  [k]
  (get ui k))

(defn get-themed-image
  [k k2]
  (get ui (if (colors/dark?) k k2)))

(defn get-token
  [k]
  (get tokens k))

(defn get-parallax-video
  [k]
  (get parallax-video k))

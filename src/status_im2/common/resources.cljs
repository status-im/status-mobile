(ns status-im2.common.resources)

(def ui
  {:add-new-contact         (js/require "../resources/images/ui2/add-contact.png")
   :lifestyle               (js/require "../resources/images/ui2/lifestyle.png")
   :music                   (js/require "../resources/images/ui2/music.png")
   :podcasts                (js/require "../resources/images/ui2/podcasts.png")
   :generate-keys           (js/require "../resources/images/ui2/generate-keys.png")
   :ethereum-address        (js/require "../resources/images/ui2/ethereum-address.png")
   :use-keycard             (js/require "../resources/images/ui2/keycard.png")
   :onboarding-illustration (js/require "../resources/images/ui2/onboarding_illustration.png")
   :qr-code                 (js/require "../resources/images/ui2/qr-code.png")})

(def mock-images
  {:01                   (js/require "../resources/images/mock2/01.png")
   :02                   (js/require "../resources/images/mock2/02.png")
   :03                   (js/require "../resources/images/mock2/03.png")
   :04                   (js/require "../resources/images/mock2/04.png")
   :coinbase             (js/require "../resources/images/mock2/coinbase.png")
   :collectible          (js/require "../resources/images/mock2/collectible.png")
   :community-banner     (js/require "../resources/images/mock2/community-banner.png")
   :community-logo       (js/require "../resources/images/mock2/community-logo.png")
   :community-cover      (js/require "../resources/images/mock2/community-cover.png")
   :gif                  (js/require "../resources/images/mock2/gif.png")
   :photo1               (js/require "../resources/images/mock2/photo1.png")
   :photo2               (js/require "../resources/images/mock2/photo2.png")
   :photo3               (js/require "../resources/images/mock2/photo3.png")
   :qr-code              (js/require "../resources/images/mock2/qr-code.png")
   :small-opt-card-icon  (js/require "../resources/images/mock2/small_opt_card_icon.png")
   :small-opt-card-main  (js/require "../resources/images/mock2/small_opt_card_main.png")
   :status-logo          (js/require "../resources/images/mock2/status-logo.png")
   :sticker              (js/require "../resources/images/mock2/sticker.png")
   :ring                 (js/require "../resources/images/mock2/ring.png")
   :user-picture-female2 (js/require "../resources/images/mock2/user_picture_female2.png")
   :user-picture-male4   (js/require "../resources/images/mock2/user_picture_male4.png")
   :user-picture-male5   (js/require "../resources/images/mock2/user_picture_male5.png")})

(def videos
  {:biometrics-01       (js/require "../resources/videos/biometrics_01.mp4")
   :biometrics-02       (js/require "../resources/videos/biometrics_02.mp4")
   :biometrics-03       (js/require "../resources/videos/biometrics_03.mp4")
   :biometrics-04       (js/require "../resources/videos/biometrics_04.mp4")
   :biometrics-01-low   (js/require "../resources/videos/biometrics_1_low.mp4")
   :biometrics-02-low   (js/require "../resources/videos/biometrics_2_low.mp4")
   :biometrics-03-low   (js/require "../resources/videos/biometrics_3_low.mp4")
   :biometrics-04-low   (js/require "../resources/videos/biometrics_4_low.mp4")
   :biometrics-01-v-low (js/require "../resources/videos/Biometrics_Layers_v1_01_2.mp4")
   :biometrics-02-v-low (js/require "../resources/videos/Biometrics_Layers_v1_02_2.mp4")
   :biometrics-03-v-low (js/require "../resources/videos/Biometrics_Layers_v1_03_2.mp4")
   :biometrics-04-v-low (js/require "../resources/videos/Biometrics_Layers_v1_04_2.mp4")})

(def tokens
  {:eth  (js/require "../resources/images/tokens/mainnet/ETH.png")
   :knc  (js/require "../resources/images/tokens/mainnet/KNC.png")
   :mana (js/require "../resources/images/tokens/mainnet/MANA.png")
   :rare (js/require "../resources/images/tokens/mainnet/RARE.png")
   :dai  (js/require "../resources/images/tokens/mainnet/DAI.png")
   :fxc  (js/require "../resources/images/tokens/mainnet/FXC.png")
   :usdt (js/require "../resources/images/tokens/mainnet/USDT.png")
   :snt  (js/require "../resources/images/tokens/mainnet/SNT.png")})

(defn get-mock-image
  [k]
  (get mock-images k))

(defn get-image
  [k]
  (get ui k))

(defn get-token
  [k]
  (get tokens k))

(defn get-video
  [k]
  (get videos k))

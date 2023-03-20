(ns status-im2.common.resources)

(def ui
  {:add-new-contact    (js/require "../resources/images/ui2/add-contact.png")
   :intro-1            (js/require "../resources/images/ui2/intro-1.png")
   :intro-2            (js/require "../resources/images/ui2/intro-2.png")
   :intro-3            (js/require "../resources/images/ui2/intro-3.png")
   :intro-4            (js/require "../resources/images/ui2/intro-4.png")
   :lifestyle          (js/require "../resources/images/ui2/lifestyle.png")
   :music              (js/require "../resources/images/ui2/music.png")
   :podcasts           (js/require "../resources/images/ui2/podcasts.png")
   :sync-device        (js/require "../resources/images/ui2/sync-new-device-cover-background.png")
   :onboarding-bg-1    (js/require "../resources/images/ui2/onboarding-bg-1.png")
   :onboarding-blur-bg (js/require "../resources/images/ui2/onboarding_blur_bg.png")
   :generate-keys      (js/require "../resources/images/ui2/generate_keys.png")
   :ethereum-address   (js/require "../resources/images/ui2/ethereum_address.png")
   :use-keycard        (js/require "../resources/images/ui2/keycard.png")})

(def mock-images
  {:coinbase             (js/require "../resources/images/mock2/coinbase.png")
   :collectible          (js/require "../resources/images/mock2/collectible.png")
   :community-banner     (js/require "../resources/images/mock2/community-banner.png")
   :community-logo       (js/require "../resources/images/mock2/community-logo.png")
   :community-cover      (js/require "../resources/images/mock2/community-cover.png")
   :gif                  (js/require "../resources/images/mock2/gif.png")
   :photo1               (js/require "../resources/images/mock2/photo1.png")
   :photo2               (js/require "../resources/images/mock2/photo2.png")
   :photo3               (js/require "../resources/images/mock2/photo3.png")
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

(defn get-mock-image
  [k]
  (get mock-images k))

(defn get-image
  [k]
  (get ui k))

(defn get-token
  [k]
  (get tokens k))

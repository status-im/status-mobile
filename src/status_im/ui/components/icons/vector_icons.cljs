(ns status-im.ui.components.icons.vector-icons
  (:require-macros [status-im.utils.slurp :refer [slurp-svg]])
  (:require [reagent.core :as reagent]
            [status-im.utils.platform :as platform]
            [status-im.ui.components.styles :as styles]
            [status-im.ui.components.react :as react]
            [status-im.react-native.js-dependencies :as rn-dependencies]))

(defn get-property [name]
  (aget rn-dependencies/svg name))

(defn adapt-class [class]
  (when class
    (reagent/adapt-react-class class)))

(defn get-class [name]
  (adapt-class (get-property name)))

(def svg (get-class "Svg"))
(def g (get-class "G"))
(def rect (get-class "Rect"))
(def path (get-class "Path"))
(def use-def (get-class "Use"))
(def defs (get-class "Defs"))

(def icons {:icons/home                (slurp-svg "./resources/icons/bottom/home_gray.svg")
            :icons/home-active         (slurp-svg "./resources/icons/bottom/home_blue.svg")
            :icons/profile             (slurp-svg "./resources/icons/bottom/profile_gray.svg")
            :icons/profile-active      (slurp-svg "./resources/icons/bottom/profile_blue.svg")
            :icons/wallet              (slurp-svg "./resources/icons/bottom/wallet_gray.svg")
            :icons/wallet-active       (slurp-svg "./resources/icons/bottom/wallet_active.svg")
            :icons/speaker             (slurp-svg "./resources/icons/speaker.svg")
            :icons/speaker-off         (slurp-svg "./resources/icons/speaker_off.svg")
            :icons/transaction-history (slurp-svg "./resources/icons/transaction_history.svg")
            :icons/add                 (slurp-svg "./resources/icons/add.svg")
            :icons/add-wallet          (slurp-svg "./resources/icons/add_wallet.svg")
            :icons/address             (slurp-svg "./resources/icons/address.svg")
            :icons/arrow-left          (slurp-svg "./resources/icons/arrow_left.svg")
            :icons/arrow-right         (slurp-svg "./resources/icons/arrow_right.svg")
            :icons/flash-active        (slurp-svg "./resources/icons/flash_active.svg")
            :icons/flash-inactive      (slurp-svg "./resources/icons/flash_inactive.svg")
            :icons/attach              (slurp-svg "./resources/icons/attach.svg")
            :icons/browse              (slurp-svg "./resources/icons/browse.svg")
            :icons/close               (slurp-svg "./resources/icons/close.svg")
            :icons/copy-from           (slurp-svg "./resources/icons/copy_from.svg")
            :icons/dots-horizontal     (slurp-svg "./resources/icons/dots_horizontal.svg")
            :icons/dots-vertical       (slurp-svg "./resources/icons/dots_vertical.svg")
            :icons/exclamation_mark    (slurp-svg "./resources/icons/exclamation_mark.svg")
            :icons/filter              (slurp-svg "./resources/icons/filter.svg")
            :icons/fullscreen          (slurp-svg "./resources/icons/fullscreen.svg")
            :icons/group-big           (slurp-svg "./resources/icons/group_big.svg")
            :icons/group-chat          (slurp-svg "./resources/icons/group_chat.svg")
            :icons/hamburger           (slurp-svg "./resources/icons/hamburger.svg")
            :icons/hidden              (slurp-svg "./resources/icons/hidden.svg")
            :icons/mic                 (slurp-svg "./resources/icons/mic.svg")
            :icons/ok                  (slurp-svg "./resources/icons/ok.svg")
            :icons/public              (slurp-svg "./resources/icons/public.svg")
            :icons/public-chat         (slurp-svg "./resources/icons/public_chat.svg")
            :icons/qr                  (slurp-svg "./resources/icons/QR.svg")
            :icons/search              (slurp-svg "./resources/icons/search.svg")
            :icons/smile               (slurp-svg "./resources/icons/smile.svg")
            :icons/commands-list       (slurp-svg "./resources/icons/commands_list.svg")
            :icons/back                (slurp-svg "./resources/icons/back.svg")
            :icons/forward             (slurp-svg "./resources/icons/forward.svg")
            :icons/dropdown-up         (slurp-svg "./resources/icons/dropdown_up.svg")
            :icons/up                  (slurp-svg "./resources/icons/up.svg")
            :icons/down                (slurp-svg "./resources/icons/down.svg")
            :icons/grab                (slurp-svg "./resources/icons/grab.svg")
            :icons/share               (slurp-svg "./resources/icons/share.svg")
            :icons/tooltip-triangle    (slurp-svg "./resources/icons/tooltip-triangle.svg")
            :icons/open                (slurp-svg "./resources/icons/open.svg")
            :icons/network             (slurp-svg "./resources/icons/network.svg")
            :icons/wnode               (slurp-svg "./resources/icons/wnode.svg")})

(defn normalize-property-name [n]
  (if (= n :icons/options)
    (if platform/ios? :icons/dots-horizontal :icons/dots-vertical)
    n))

(def default-viewbox {:width 24 :height 24 :viewBox "0 0 24 24"})

(defn icon
  ([name] (icon name nil))
  ([name {:keys [color container-style style accessibility-label]
          :or {accessibility-label :icon}}]
   ^{:key name}
   [react/view {:style               container-style
                :accessibility-label accessibility-label}
    (if-let [icon-fn (get icons (normalize-property-name name))]
      (into []
            (concat
              [svg (merge default-viewbox style)]
              (icon-fn
                (cond
                  (keyword? color)
                  (case color
                    :dark styles/icon-dark-color
                    :gray styles/icon-gray-color
                    :blue styles/color-light-blue
                    :active styles/color-blue4
                    :white styles/color-white
                    :red styles/icon-red-color
                    styles/icon-dark-color)
                  (string? color)
                  color
                  :else
                  styles/icon-dark-color))))
      (throw (js/Error. (str "Unknown icon: " name))))]))
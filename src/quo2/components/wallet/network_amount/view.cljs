(ns quo2.components.wallet.network-amount.view
  (:require
    [clojure.string :as str]
    [quo2.components.markdown.text :as text]
    [quo2.foundations.colors :as colors]
    [react-native.core :as rn]
    [status-im2.common.resources :as resources]
    [quo2.components.wallet.network-amount.style :as style]))

(defn network-amount
  [{:keys [amount token]}]
  [rn/view {:style style/container}
   [rn/image
    {:source (resources/tokens token)
     :style  {:width 12 :height 12}}]
   [text/text
    {:width :medium
     :size  :paragraph-2
     :style style/text}
    (str amount " " (str/upper-case (clj->js token)))]
   [rn/view
    {:style {:width            1
             :height           8
             :background-color (colors/theme-colors colors/neutral-40 colors/neutral-50)}}]])

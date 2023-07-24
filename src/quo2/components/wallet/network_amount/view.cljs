(ns quo2.components.wallet.network-amount.view
  (:require
    [clojure.string :as str]
    [quo2.components.markdown.text :as text]
    [quo2.theme :as quo.theme]
    [react-native.core :as rn]
    [quo2.components.wallet.network-amount.style :as style]
    [quo2.foundations.resources :as resources]))

(defn- network-amount-internal
  [{:keys [amount token theme]}]
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
    {:style (style/divider theme)}]])

(def network-amount (quo.theme/with-theme network-amount-internal))

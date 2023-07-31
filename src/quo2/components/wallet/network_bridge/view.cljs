(ns quo2.components.wallet.network-bridge.view
  (:require
    [clojure.string :as string]
    [quo2.components.icon :as icon]
    [quo2.components.markdown.text :as text]
    [quo2.foundations.colors :as colors]
    [quo2.foundations.resources :as resources]
    [quo2.theme :as quo.theme]
    [react-native.core :as rn]
    [quo2.components.wallet.network-bridge.style :as style]))

(defn network-bridge-internal
  [{:keys [theme network state amount]}]
  (let [network-text (if (= network :ethereum) "Mainnet" (string/capitalize (name network)))]
    [rn/view
     {:style               (style/container network state)
      :accessibility-label :container}
     (if (= state :loading)
       [rn/view
        {:style               (style/loading-skeleton theme)
         :accessibility-label :loading}]
       [rn/view
        {:style {:flex-direction  :row
                 :justify-content :space-between}}
        [text/text
         {:size   :paragraph-2
          :weight :medium} amount]
        (when (= state :locked)
          [icon/icon :i/locked
           {:size                12
            :color               (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)
            :accessibility-label :lock}])])
     [rn/view
      {:style {:flex-direction :row
               :align-items    :center}}
      [rn/image
       {:source (resources/networks network)
        :style  style/network-icon}]
      [text/text
       {:size   :label
        :weight :medium
        :style  {:color (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)}}
       network-text]]]))

(def network-bridge (quo.theme/with-theme network-bridge-internal))

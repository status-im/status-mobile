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


(defn network-bridge-add
  [{:keys [network state]}]
  [rn/view {:style (merge (style/container network state) (style/add-container))}
   [icon/icon :i/add-circle {:size 12 :no-color true}]])

(defn view-internal
  [{:keys [theme network status amount] :as args}]
  (let [network-text (if (= network :ethereum) "Mainnet" (string/capitalize (name network)))]
    (if (= status :add)
      [network-bridge-add args]
      [rn/view
       {:style               (style/container network status)
        :accessible          true
        :accessibility-label :container}
       (if (= status :loading)
         [rn/view
          {:style               (style/loading-skeleton theme)
           :accessible          true
           :accessibility-label :loading}]
         [rn/view
          {:style {:flex-direction  :row
                   :justify-content :space-between}}
          [text/text
           {:size   :paragraph-2
            :weight :medium} amount]
          (when (= status :locked)
            [icon/icon :i/locked
             {:size                12
              :color               (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)
              :accessible          true
              :accessibility-label :lock}])])
       [rn/view
        {:style {:flex-direction :row
                 :align-items    :center}}
        [rn/image
         {:source (resources/get-network network)
          :style  style/network-icon}]
        [text/text
         {:size   :label
          :weight :medium
          :style  {:color (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)}}
         network-text]]])))

(def view (quo.theme/with-theme view-internal))

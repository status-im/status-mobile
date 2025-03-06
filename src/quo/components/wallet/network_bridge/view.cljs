(ns quo.components.wallet.network-bridge.view
  (:require
    [clojure.string :as string]
    [quo.components.icon :as icon]
    [quo.components.markdown.text :as text]
    [quo.components.wallet.network-bridge.schema :as network-bridge-schema]
    [quo.components.wallet.network-bridge.style :as style]
    [quo.foundations.colors :as colors]
    [quo.foundations.resources :as resources]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [schema.core :as schema]))

(defn network-bridge-add
  [{:keys [network state theme container-style on-press]}]
  [rn/pressable
   {:style    [(style/container network state theme)
               (style/add-container theme)
               container-style]
    :on-press on-press}
   [icon/icon :i/edit
    {:size  12
     :color (colors/theme-colors colors/neutral-50
                                 colors/neutral-60
                                 theme)}]])

(defn- network->text
  [network]
  (cond (not network)         ""
        (= network :ethereum) "Mainnet"
        :else                 (string/capitalize (name network))))

(defn view-internal
  [{:keys [network status amount container-style on-press on-long-press] :as args}]
  (let [theme (quo.theme/use-theme)]
    (if (= status :edit)
      [network-bridge-add (assoc args :theme theme)]
      [rn/pressable
       {:style               [(style/container network status theme) container-style]
        :accessible          true
        :accessibility-label :container
        :on-press            on-press
        :on-long-press       on-long-press}
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
         (network->text network)]]])))

(def view (schema/instrument #'view-internal network-bridge-schema/?schema))

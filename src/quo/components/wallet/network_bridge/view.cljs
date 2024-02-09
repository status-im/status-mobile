(ns quo.components.wallet.network-bridge.view
  (:require
    [clojure.string :as string]
    [quo.components.icon :as icon]
    [quo.components.markdown.text :as text]
    [quo.components.wallet.network-bridge.style :as style]
    [quo.foundations.colors :as colors]
    [quo.foundations.resources :as resources]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [schema.core :as schema]))

(defn network-bridge-add
  [{:keys [network state theme container-style on-press]}]
  [rn/pressable
   {:style    (merge (style/container network state theme)
                     (style/add-container theme)
                     container-style)
    :on-press on-press}
   [icon/icon :i/add-circle {:size 12 :no-color true}]])

(defn- network->text
  [network]
  (cond (not network)         ""
        (= network :ethereum) "Mainnet"
        :else                 (string/capitalize (name network))))

(def ?schema
  [:=>
   [:catn
    [:props
     [:map {:closed true}
      [:theme [:schema.common/theme]]
      [:network [:maybe :keyword]]
      [:status [:enum :add :loading :locked]]
      [:amount [:maybe :string]]
      [:container-style [:maybe :map]]
      [:on-press [:maybe :fn]]]]
    :any]])

(defn view-internal
  [{:keys [theme network status amount container-style on-press] :as args}]
  (if (= status :add)
    [network-bridge-add args]
    [rn/pressable
     {:style               (merge (style/container network status theme) container-style)
      :accessible          true
      :accessibility-label :container
      :on-press            on-press}
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
       (network->text network)]]]))

(def view
  (quo.theme/with-theme
   (schema/instrument #'view-internal ?schema)))

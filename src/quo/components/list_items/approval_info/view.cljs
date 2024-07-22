(ns quo.components.list-items.approval-info.view
  (:require [clojure.string :as string]
            [quo.components.avatars.account-avatar.view :as account-avatar]
            [quo.components.avatars.collection-avatar.view :as collection-avatar]
            [quo.components.avatars.community-avatar.view :as community-avatar]
            [quo.components.avatars.dapp-avatar.view :as dapp-avatar]
            [quo.components.avatars.icon-avatar :as icon-avatar]
            [quo.components.avatars.token-avatar.view :as token-avatar]
            [quo.components.avatars.wallet-user-avatar.view :as wallet-user-avatar]
            [quo.components.buttons.button.view :as button]
            [quo.components.icon :as icon]
            [quo.components.list-items.approval-info.style :as style]
            [quo.components.markdown.text :as text]
            [quo.components.tags.tiny-tag.view :as tiny-tag]
            [quo.foundations.colors :as colors]
            quo.theme
            [react-native.core :as rn]
            [schema.core :as schema]))

(def ?schema
  [:=>
   [:catn
    [:props
     [:map {:closed true}
      [:type
       [:enum :spending-cap :token-contract :account :spending-contract :network :date-signed
        :collectible :address :community :collectible-contract]]
      [:avatar-props :map]
      [:label :string]
      [:description {:optional true} [:maybe :string]]
      [:button-label {:optional true} [:maybe :string]]
      [:button-icon {:optional true} [:maybe :keyword]]
      [:blur? {:optional true} [:maybe :boolean]]
      [:unlimited-icon? {:optional true} [:maybe :boolean]]
      [:option-icon {:optional true} [:maybe :keyword]]
      [:tag-label {:optional true} [:maybe :string]]
      [:on-button-press {:optional true} [:maybe fn?]]
      [:on-avatar-press {:optional true} [:maybe fn?]]
      [:on-option-press {:optional true} [:maybe fn?]]
      [:container-style {:optional true} [:maybe :any]]]]]
   :any])

(defn- avatar
  [{:keys [type avatar-props blur? theme on-press]}]
  [rn/pressable {:on-press on-press}
   (case type
     :account              [account-avatar/view (assoc avatar-props :size 32)]
     :collectible-contract [collection-avatar/view (assoc avatar-props :size :size-32)]
     :spending-contract    [dapp-avatar/view avatar-props]
     :date-signed          [icon-avatar/icon-avatar
                            (assoc avatar-props
                                   :size    :size-32
                                   :opacity 10
                                   :color   (if blur?
                                              colors/white
                                              (colors/theme-colors colors/neutral-50
                                                                   colors/neutral-40
                                                                   theme)))]
     :address              [wallet-user-avatar/wallet-user-avatar
                            (assoc avatar-props
                                   :size       :size-32
                                   :monospace? true
                                   :lowercase? true
                                   :neutral?   true)]
     :community            [community-avatar/view avatar-props]
     [token-avatar/view
      (assoc avatar-props
             :type
             (if (= type :collectible) :collectible :asset))])])

(defn- view-internal
  [{:keys [type avatar-props label description blur? unlimited-icon? container-style
           on-option-press on-avatar-press on-button-press button-label button-icon tag-label
           option-icon]}]
  (let [theme        (quo.theme/use-theme)
        description? (not (string/blank? description))]
    [rn/view
     {:style               (merge (style/container description? blur? theme) container-style)
      :accessibility-label :approval-info}
     [avatar
      {:type         type
       :blur?        blur?
       :theme        theme
       :on-press     on-avatar-press
       :avatar-props avatar-props}]
     [rn/view
      {:style style/labels}
      [rn/view
       {:style {:flex-direction :row :align-items :center}}
       [text/text
        {:size   :paragraph-1
         :weight (if (= type :address) :monospace :semi-bold)
         :style  (style/label blur? theme)}
        label]
       (when unlimited-icon?
         [icon/icon :i/alert
          {:container-style {:margin-left 4}
           :size            16
           :color           (style/icon-description-color blur? theme)}])]
      (when description?
        [text/text
         {:size   :paragraph-2
          :weight (if (contains? #{:collectible-contract :spending-contract :account :token-contract}
                                 type)
                    :monospace
                    :regular)
          :style  (style/description blur? theme)}
         description])]
     (when (= type :account) [tiny-tag/view {:label tag-label}])
     (when (and (= type :spending-cap) button-icon)
       [button/button
        {:type      :outline
         :size      24
         :blur?     blur?
         :icon-left button-icon
         :on-press  on-button-press}
        button-label])
     (when option-icon
       [rn/pressable {:on-press on-option-press}
        [icon/icon option-icon
         {:color (style/icon-description-color blur? theme)
          :size  20}]])]))

(def view (schema/instrument #'view-internal ?schema))

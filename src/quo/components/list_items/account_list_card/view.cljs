(ns quo.components.list-items.account-list-card.view
  (:require
    [quo.components.avatars.account-avatar.view :as account-avatar]
    [quo.components.icon :as icon]
    [quo.components.list-items.account-list-card.schema :as component-schema]
    [quo.components.list-items.account-list-card.style :as style]
    [quo.components.markdown.text :as text]
    [quo.components.wallet.address-text.view :as address-text]
    [quo.context :as quo.context]
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]
    [schema.core :as schema]))

(defn- internal-view
  [{:keys [action blur? account-props on-press on-options-press]}]
  (let [theme             (quo.context/use-theme)
        [state set-state] (rn/use-state :default)
        on-press-in       (rn/use-callback #(set-state :pressed))
        on-press-out      (rn/use-callback #(set-state :default))]
    [rn/pressable
     {:style               (style/container {:state state :blur? blur? :theme theme})
      :on-press-in         on-press-in
      :on-press-out        on-press-out
      :on-press            on-press
      :accessibility-label :container}
     [rn/view {:style style/left-container}
      [account-avatar/view account-props]
      [rn/view {:style {:margin-left 8}}
       [text/text
        {:weight :semi-bold
         :size   :paragraph-2}
        (:name account-props)]
       [address-text/view
        {:blur?   blur?
         :address (:address account-props)
         :format  :short}]]]
     (when (= action :icon)
       [rn/pressable {:on-press on-options-press}
        [icon/icon :i/options
         {:color               (if blur?
                                 colors/white-opa-70
                                 (colors/theme-colors colors/neutral-50 colors/neutral-40 theme))
          :accessibility-label :icon}]])]))

(def view (schema/instrument #'internal-view component-schema/?schema))

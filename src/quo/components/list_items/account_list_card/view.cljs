(ns quo.components.list-items.account-list-card.view
  (:require
    [quo.components.avatars.account-avatar.view :as account-avatar]
    [quo.components.icon :as icon]
    [quo.components.list-items.account-list-card.style :as style]
    [quo.components.markdown.text :as text]
    [quo.foundations.colors :as colors]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [quo.components.wallet.address-text.view :as address-text]))

(defn- internal-view
  []
  (let [state (reagent/atom :default)]
    (fn [{:keys [action blur? account-props networks on-press on-options-press theme]}]
      [rn/pressable
       {:style               (style/container {:state @state :blur? blur? :theme theme})
        :on-press-in         #(reset! state :pressed)
        :on-press-out        #(reset! state :default)
        :on-press            on-press
        :accessibility-label :container}
       [rn/view {:style style/left-container}
        [account-avatar/view account-props]
        [rn/view {:style {:margin-left 8}}
         [text/text
          {:weight :semi-bold
           :size   :paragraph-2}
          (:name account-props)]
         [address-text/view {:networks networks
                             :address (:address account-props)}]]]
       (when (= action :icon)
         [rn/pressable {:on-press on-options-press}
          [icon/icon :i/options
           {:color               (if blur?
                                   colors/white-opa-70
                                   (colors/theme-colors colors/neutral-50 colors/neutral-40 theme))
            :accessibility-label :icon}]])])))

(def view (quo.theme/with-theme internal-view))

(ns status-im2.contexts.contacts.drawers.nickname-drawer.view
  (:require [clojure.string :as string]
            [quo2.components.icon :as icons]
            [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [reagent.core :as reagent]
            [status-im2.contexts.contacts.drawers.nickname-drawer.style :as style]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn add-nickname-and-show-toast
  [primary-name entered-nickname public-key]
  (when-not (string/blank? entered-nickname)
    (rf/dispatch [:hide-bottom-sheet])
    (rf/dispatch [:toasts/upsert
                  {:id         :add-nickname
                   :icon       :i/correct
                   :icon-color (colors/theme-colors colors/success-60 colors/success-50)
                   :text       (i18n/label
                                :t/set-nickname-toast
                                {:primary-name primary-name
                                 :nickname     (string/trim entered-nickname)})}])
    (rf/dispatch [:contacts/update-nickname public-key (string/trim entered-nickname)])))

(defn nickname-drawer
  [{:keys [contact]}]
  (let [{:keys [primary-name nickname public-key]} contact
        entered-nickname                           (reagent/atom (or nickname ""))
        photo-path                                 (rf/sub [:chats/photo-path public-key])
        insets                                     (safe-area/get-insets)]
    (fn [{:keys [title description accessibility-label
                 close-button-text]}]
      [rn/view
       {:style               (style/nickname-container insets)
        :accessibility-label accessibility-label}
       [quo/text
        {:weight :semi-bold
         :size   :heading-1} title]
       [rn/view {:style (style/context-container)}
        [quo/user-avatar
         {:full-name         primary-name
          :profile-picture   photo-path
          :size              :xxs
          :status-indicator? false}]
        [quo/text
         {:weight :medium
          :size   :paragraph-2
          :style  {:margin-left 4}} primary-name]]
       [quo/input
        {:type              :text
         :blur?             true
         :placeholder       (i18n/label :t/type-nickname)
         :auto-focus        true
         :max-length        32
         :on-change-text    (fn [nickname]
                              (reset! entered-nickname nickname))
         :on-submit-editing #(add-nickname-and-show-toast primary-name @entered-nickname public-key)}]
       [rn/view
        {:style style/nickname-description-container}
        [icons/icon :i/info
         {:size  16
          :color (colors/theme-colors colors/black colors/white)}]
        [quo/text
         {:weight :regular
          :size   :paragraph-2
          :style  (style/nickname-description)}
         description]]
       [rn/view {:style style/buttons-container}
        [quo/button
         {:type            :grey
          :container-style {:flex 0.48}
          :on-press        #(rf/dispatch [:hide-bottom-sheet])}
         (or close-button-text (i18n/label :t/cancel))]

        [quo/button
         {:type            :primary
          :disabled?       (string/blank? @entered-nickname)
          :container-style {:flex 0.48}
          :on-press        #(add-nickname-and-show-toast primary-name @entered-nickname public-key)}
         title]]])))

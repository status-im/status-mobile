(ns status-im.contexts.chat.contacts.drawers.nickname-drawer.view
  (:require
    [clojure.string :as string]
    [quo.context]
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [status-im.constants :as constants]
    [status-im.contexts.chat.contacts.drawers.nickname-drawer.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn add-nickname-and-show-toast
  [primary-name entered-nickname public-key]
  (when-not (string/blank? entered-nickname)
    (rf/dispatch [:hide-bottom-sheet])
    (rf/dispatch [:toasts/upsert
                  {:id   :add-nickname
                   :type :positive
                   :text (i18n/label
                          :t/set-nickname-toast
                          {:primary-name primary-name
                           :nickname     (string/trim entered-nickname)})}])
    (rf/dispatch [:contacts/update-nickname public-key (string/trim entered-nickname)])))

(defn nickname-drawer
  [{:keys [contact]}]
  (let [{:keys [primary-name nickname public-key]} contact
        entered-nickname                           (reagent/atom (or nickname ""))
        photo-path                                 (rf/sub [:chats/photo-path public-key])
        theme                                      (quo.context/use-theme)
        insets                                     (safe-area/get-insets)]
    (fn [{:keys [title description accessibility-label
                 close-button-text]}]
      [rn/view
       {:style               (style/nickname-container insets)
        :accessibility-label accessibility-label}
       [quo/text
        {:weight :semi-bold
         :size   :heading-2} title]
       [rn/view {:style (style/context-container theme)}
        [quo/context-tag
         {:type            :default
          :blur?           false
          :profile-picture photo-path
          :full-name       primary-name
          :size            24}]]
       [quo/input
        {:type              :text
         :blur?             false
         :placeholder       (i18n/label :t/type-nickname)
         :auto-focus        true
         :max-length        constants/profile-name-max-length
         :label             (i18n/label :t/nickname)
         :char-limit        constants/profile-name-max-length
         :on-change-text    (fn [nickname]
                              (reset! entered-nickname nickname))
         :on-submit-editing #(add-nickname-and-show-toast primary-name @entered-nickname public-key)}]
       [rn/view
        {:style style/nickname-description-container}
        [quo/icon :i/info
         {:size  16
          :color (colors/theme-colors colors/black colors/white theme)}]
        [quo/text
         {:weight :regular
          :size   :paragraph-2
          :style  (style/nickname-description theme)}
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

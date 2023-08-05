(ns status-im2.contexts.chat.composer.edit.view
  (:require
    [react-native.reanimated :as reanimated]
    [status-im2.contexts.chat.composer.constants :as constants]
    [status-im2.contexts.chat.composer.utils :as utils]
    [utils.i18n :as i18n]
    [quo2.core :as quo]
    [quo2.foundations.colors :as colors]
    [react-native.core :as rn]
    [status-im2.contexts.chat.composer.edit.style :as style]
    [utils.re-frame :as rf]))

(defn edit-message
  [state]
  [rn/view
   {:style               style/container
    :accessibility-label :edit-message}
   [rn/view {:style style/content-container}
    [quo/icon
     :i/connector-dotted
     {:size            16
      :color           (colors/theme-colors colors/neutral-40 colors/neutral-60)
      :container-style style/icon-container}]
    [rn/view {:style style/text-container}
     [quo/text
      {:weight :medium
       :size   :paragraph-2}
      (i18n/label :t/editing-message)]]]
   [quo/button
    {:size                24
     :icon-only?          true
     :accessibility-label :edit-cancel-button
     :on-press            (fn []
                            (utils/cancel-edit-message state)
                            (rf/dispatch [:chat.ui/cancel-message-edit]))
     :type                :outline}
    :i/close]])

(defn- f-view
  [state]
  (let [edit   (rf/sub [:chats/edit-message])
        height (reanimated/use-shared-value (if edit constants/edit-container-height 0))]
    (rn/use-effect #(reanimated/animate height (if edit constants/edit-container-height 0)) [edit])
    [reanimated/view {:style (reanimated/apply-animations-to-style {:height height} {})}
     (when edit [edit-message state])]))

(defn view
  [state]
  [:f> f-view state])

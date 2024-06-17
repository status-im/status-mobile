(ns status-im.contexts.chat.messenger.composer.edit.view
  (:require
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [quo.theme]
    [react-native.core :as rn]
    [react-native.reanimated :as reanimated]
    [status-im.contexts.chat.messenger.composer.constants :as constants]
    [status-im.contexts.chat.messenger.composer.edit.style :as style]
    [status-im.contexts.chat.messenger.composer.utils :as utils]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn edit-message
  [{:keys [text-value input-ref input-height]}]
  (let [theme (quo.theme/use-theme)]
    [rn/view
     {:style               style/container
      :accessibility-label :edit-message}
     [rn/view {:style style/content-container}
      [quo/icon
       :i/connector-dotted
       {:size            16
        :color           (colors/theme-colors colors/neutral-40 colors/neutral-60 theme)
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
       :on-press            #(utils/cancel-edit-message text-value input-ref input-height)
       :type                :outline}
      :i/close]]))

(defn- f-view
  [props]
  (let [edit   (rf/sub [:chats/edit-message])
        height (reanimated/use-shared-value (if edit constants/edit-container-height 0))]
    (rn/use-effect #(reanimated/animate height (if edit constants/edit-container-height 0)) [edit])
    [reanimated/view {:style (reanimated/apply-animations-to-style {:height height} {})}
     (when edit [edit-message props])]))

(defn view
  [props]
  [:f> f-view props])

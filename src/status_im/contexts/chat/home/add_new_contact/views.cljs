(ns status-im.contexts.chat.home.add-new-contact.views
  (:require [clojure.string :as string]
            [quo.core :as quo]
            [react-native.clipboard :as clipboard]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im.contexts.chat.home.add-new-contact.style :as style]
            [utils.address :as address]
            [utils.debounce :as debounce]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn found-contact
  [public-key]
  (let [{:keys [primary-name compressed-key]} (rf/sub [:contacts/contact-by-identity public-key])
        photo-path                            (rf/sub [:chats/photo-path public-key])]
    (when primary-name
      [rn/view style/found-user
       [quo/text (style/text-description)
        (i18n/label :t/user-found)]
       [rn/view (style/found-user-container)
        [quo/user-avatar
         {:full-name         primary-name
          :profile-picture   photo-path
          :size              :small
          :status-indicator? false}]
        [rn/view style/found-user-text
         [quo/text
          {:weight :semi-bold
           :size   :paragraph-1
           :style  (style/found-user-display-name)}
          primary-name]
         [quo/text
          {:weight :regular
           :size   :paragraph-2
           :style  (style/found-user-key)}
          (address/get-shortened-compressed-key compressed-key)]]]])))

(defn- header
  []
  [:<>
   [quo/button
    {:type                :grey
     :icon-only?          true
     :accessibility-label :new-contact-close-button
     :size                32
     :on-press            #(rf/dispatch [:navigate-back])}
    :i/close]
   [quo/text (style/text-title) (i18n/label :t/add-a-contact)]
   [quo/text (style/text-subtitle) (i18n/label :t/find-your-friends)]])

(defn- search-input
  []
  (reagent/with-let [input-value    (reagent/atom nil)
                     input-ref      (atom nil)
                     clear-input    (fn []
                                      (reset! input-value nil)
                                      (rf/dispatch [:contacts/clear-new-identity]))
                     paste-on-input #(clipboard/get-string
                                      (fn [clipboard-text]
                                        (reset! input-value clipboard-text)
                                        (rf/dispatch [:contacts/set-new-identity clipboard-text nil])))]
    (let [{:keys [scanned]} (rf/sub [:contacts/new-identity])
          empty-input?      (and (string/blank? @input-value)
                                 (string/blank? scanned))]
      [rn/view {:style style/input-and-scan-container}
       [quo/input
        {:accessibility-label :enter-contact-code-input
         :ref                 #(reset! input-ref %)
         :container-style     {:flex 1}
         :auto-capitalize     :none
         :multiline?          true
         :blur-on-submit      true
         :return-key-type     :done
         :label               (i18n/label :t/ens-or-chat-key)
         :placeholder         (i18n/label :t/type-some-chat-key)
         :clearable?          (not empty-input?)
         :on-clear            clear-input
         :button              (when empty-input?
                                {:on-press paste-on-input
                                 :text     (i18n/label :t/paste)})
         ;; NOTE: `scanned` has priority over `@input-value`, we clean it when the input is updated
         ;; so that it's `nil` and `@input-value` is shown. To fastly clean it, we use
         ;; `dispatch-sync`. This call could be avoided if `::qr-scanner/scan-code` were able to
         ;; receive a callback function, not only a re-frame event as callback.
         :value               (or scanned @input-value)
         :on-change-text      (fn [new-text]
                                (reset! input-value new-text)
                                (as-> [:contacts/set-new-identity new-text nil] $
                                  (if (string/blank? scanned)
                                    (debounce/debounce-and-dispatch $ 600)
                                    (rf/dispatch-sync $))))}]
       [rn/view {:style style/scan-button-container}
        [quo/button
         {:type       :outline
          :icon-only? true
          :size       40
          :on-press   #(rf/dispatch [:open-modal :scan-profile-qr-code])}
         :i/scan]]])
    (finally
     (rf/dispatch [:contacts/clear-new-identity]))))

(defn- invalid-text
  [message]
  [rn/view style/container-invalid
   [quo/icon :i/alert style/icon-invalid]
   [quo/text style/text-invalid
    (i18n/label (or message :t/invalid-ens-or-key))]])

(defn new-contact
  []
  (let [{:keys [public-key ens state msg]} (rf/sub [:contacts/new-identity])
        customization-color                (rf/sub [:profile/customization-color])]
    [rn/keyboard-avoiding-view {:style {:flex 1}}
     [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
      [rn/view {:style (style/container-outer)}
       [rn/view {:style style/container-inner}
        [header]
        [search-input]
        (case state
          :invalid [invalid-text msg]
          :valid   [found-contact public-key]
          nil)]
       [quo/button
        {:type                :primary
         :customization-color customization-color
         :size                40
         :container-style     style/button-view-profile
         :accessibility-label :new-contact-button
         :icon-left           :i/profile
         :disabled?           (not= state :valid)
         :on-press            (fn []
                                (rf/dispatch [:navigate-back])
                                (rf/dispatch [:chat.ui/show-profile public-key ens])
                                (js/setTimeout #(rf/dispatch [:contacts/clear-new-identity])
                                               600))}
        (i18n/label :t/view-profile)]]]]))

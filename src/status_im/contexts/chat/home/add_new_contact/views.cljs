(ns status-im.contexts.chat.home.add-new-contact.views
  (:require [clojure.string :as string]
            [quo.core :as quo]
            [quo.theme]
            [react-native.clipboard :as clipboard]
            [react-native.core :as rn]
            [status-im.common.floating-button-page.view :as floating-button-page]
            [status-im.contexts.chat.home.add-new-contact.style :as style]
            [utils.address :as address]
            [utils.debounce :as debounce]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn found-contact
  [public-key]
  (let [{:keys [primary-name compressed-key]} (rf/sub [:contacts/contact-by-identity public-key])
        photo-path                            (rf/sub [:chats/photo-path public-key])
        theme                                 (quo.theme/use-theme)]
    (when primary-name
      [rn/view style/found-user
       [quo/text
        {:size   :paragraph-2
         :weight :medium
         :style  (style/text-description theme)}
        (i18n/label :t/user-found)]
       [rn/view (style/found-user-container theme)
        [quo/user-avatar
         {:full-name         primary-name
          :profile-picture   photo-path
          :size              :small
          :status-indicator? false}]
        [rn/view style/found-user-text
         [quo/text
          {:weight :semi-bold
           :size   :paragraph-1
           :style  (style/found-user-display-name theme)}
          primary-name]
         [quo/text
          {:weight :regular
           :size   :paragraph-2
           :style  (style/found-user-key theme)}
          (address/get-shortened-compressed-key compressed-key)]]]])))

(defn- search-input
  []
  (let [[input-value set-input-value] (rn/use-state nil)
        input-ref                     (rn/use-ref-atom nil)
        on-ref                        (rn/use-callback #(reset! input-ref %))
        {:keys              [scanned]
         contact-public-key :id}      (rf/sub [:contacts/new-identity])
        contact-public-key-or-scanned (or contact-public-key scanned)
        empty-input?                  (and (string/blank? input-value)
                                           (string/blank? contact-public-key-or-scanned))
        clear-input                   (rn/use-callback
                                       (fn []
                                         (set-input-value nil)
                                         (rf/dispatch [:contacts/clear-new-identity])))
        paste-on-input                (rn/use-callback
                                       (fn []
                                         (clipboard/get-string
                                          (fn [clipboard-text]
                                            (set-input-value clipboard-text)
                                            (rf/dispatch [:contacts/set-new-identity
                                                          {:input clipboard-text}])))))
        on-change-text                (rn/use-callback
                                       (fn [new-text]
                                         (set-input-value new-text)
                                         (if (string/blank? contact-public-key-or-scanned)
                                           (debounce/debounce-and-dispatch [:contacts/set-new-identity
                                                                            {:input new-text}]
                                                                           600)
                                           (rf/dispatch-sync [:contacts/set-new-identity
                                                              {:input new-text}])))
                                       [contact-public-key-or-scanned])]
    (rn/use-unmount #(rf/dispatch [:contacts/clear-new-identity]))
    [rn/view {:style style/input-and-scan-container}
     [quo/input
      {:accessibility-label :enter-contact-code-input
       :ref                 on-ref
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
       ;; NOTE: `contact-public-key-or-scanned` has priority over `input-value`,
       ;; we clean it when the input is updated so that it's `nil` and
       ;; `input-value`is shown. To fastly clean it, we use `dispatch-sync`.
       ;; This call could be avoided if `::qr-scanner/scan-code` were able to
       ;; receive a callback function, not only a re-frame event as callback.
       :value               (or contact-public-key-or-scanned input-value)
       :on-change-text      on-change-text}]
     [rn/view {:style style/scan-button-container}
      [quo/button
       {:type       :outline
        :icon-only? true
        :size       40
        :on-press   #(rf/dispatch [:open-modal :scan-profile-qr-code])}
       :i/scan]]]))

(defn- invalid-text
  [message]
  [rn/view style/container-invalid
   [quo/icon :i/alert style/icon-invalid]
   [quo/text
    {:size  :paragraph-2
     :style style/text-invalid}
    (i18n/label (or message :t/invalid-ens-or-key))]])

(defn navigate-back [] (rf/dispatch [:navigate-back]))

(defn new-contact
  []
  (let [{:keys [public-key ens state msg]} (rf/sub [:contacts/new-identity])
        customization-color                (rf/sub [:profile/customization-color])
        theme                              (quo.theme/use-theme)]
    [floating-button-page/view
     {:header-container-style {:margin-top 8}
      :header                 [quo/page-nav
                               {:type                :no-title
                                :icon-name           :i/close
                                :accessibility-label :new-contact-close-button
                                :on-press            navigate-back}]
      :footer                 [quo/button
                               {:type                :primary
                                :customization-color customization-color
                                :size                40
                                :accessibility-label :new-contact-button
                                :icon-left           :i/profile
                                :disabled?           (not= state :valid)
                                :on-press            (fn []
                                                       (rf/dispatch [:navigate-back])
                                                       (rf/dispatch [:chat.ui/show-profile public-key
                                                                     ens])
                                                       (js/setTimeout #(rf/dispatch
                                                                        [:contacts/clear-new-identity])
                                                                      600))}
                               (i18n/label :t/view-profile)]}
     [quo/page-top
      {:title            (i18n/label :t/add-a-contact)
       :description      :text
       :description-text (i18n/label :t/find-your-friends)
       :container-style  {:padding-vertical 8}}]
     [rn/view {:style (style/container-outer theme)}
      [search-input]
      (case state
        :invalid [invalid-text msg]
        :valid   [found-contact public-key]
        nil)]]))

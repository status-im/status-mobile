(ns status-im.contexts.profile.contact.contact-request.view
  (:require [clojure.string :as string]
            [quo.core :as quo]
            [re-frame.core :as re-frame]
            [react-native.core :as rn]
            [react-native.platform :as platform]
            [reagent.ratom]
            [status-im.constants :as constants]
            [status-im.contexts.profile.contact.contact-request.style :as style]
            [status-im.contexts.profile.utils :as profile.utils]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn make-stateful-callbacks
  [state-ratom]
  (let [state-ref (atom nil)]
    (js/console.log "init make-stateful-callbacks")
    (let [reaction-sub (reagent.ratom/track!
                        (fn [state-ratom]
                          (js/console.log "reagent reaction")
                          (let [state @state-ratom]
                            (js/console.log
                             (if (nil? @state-ref)
                               "state init"
                               "state update")
                             (clj->js state))
                            (reset! state-ref state)))
                        state-ratom)]
      @reaction-sub
      {:sub reaction-sub
       :factory (memoize
                 (fn [callback]
                   (js/console.log "init callback")
                   (fn [event]
                     (callback @state-ref event))))})))

(defn use-bind [state-ratom]
  (let [{:keys [factory sub]} (rn/use-memo (fn []
                                         (make-stateful-callbacks state-ratom))
                                       [state-ratom])] 
    (rn/use-unmount (fn []
                      (js/console.log "unmount sub")
                      (reagent.ratom/dispose! sub)))
    factory))

(defonce contact-request-message-store
  (reagent.ratom/atom {}))

(defn access-message-store
  ([k] (get-in @contact-request-message-store k ""))
  ([k v] (swap! contact-request-message-store assoc-in k v)))

(defn get-message-sub [public-key]
  (reagent.ratom/cursor access-message-store public-key))

(defn on-press-test
  [state event]
  (js/console.log "on-press state" (clj->js state))
  (js/console.log "on-press event" #js{:target (.-target event)})
  (access-message-store (:public-key state) "")
  (rf/dispatch [:hide-bottom-sheet]))

(defn combine-subs [message-sub profile-sub]
  {:message @message-sub
   :public-key @(reagent.ratom/cursor profile-sub [:public-key])})

(defn view
  []
  (let [profile-sub           (re-frame/subscribe [:contacts/current-contact])
        {:keys [public-key customization-color]
         :as   profile}       (deref profile-sub)
        customization-color   customization-color
        full-name             (profile.utils/displayed-name profile)
        profile-picture       (profile.utils/photo profile)
        input-ref             (rn/use-ref-atom nil)
        message-sub           (get-message-sub public-key)
        [message set-message] (rn/use-state "")
        on-message-change     (rn/use-callback #(do
                                                  (reset! message-sub %)
                                                  (set-message %)))
        callback-state-sub    (reagent.ratom/track combine-subs message-sub profile-sub)
        bind                  (use-bind callback-state-sub)
        on-message-submit     (rn/use-callback (fn []
                                                 (rf/dispatch [:hide-bottom-sheet])
                                                 (rf/dispatch [:contact.ui/send-contact-request
                                                               public-key message])
                                                 (rf/dispatch [:toasts/upsert
                                                               {:id   :send-contact-request
                                                                :type :positive
                                                                :text (i18n/label
                                                                       :t/contact-request-was-sent)}]))
                                               [public-key message])]
    (rn/use-mount
     (fn []
       (let [listener (.addListener rn/keyboard
                                    "keyboardDidHide"
                                    (fn [_event]
                                      (when (and platform/android? @input-ref)
                                        (.blur ^js @input-ref))))]
         #(.remove ^js listener))))
    [:<>
     [quo/drawer-top
      {:type                :context-tag
       :context-tag-type    :default
       :title               (i18n/label :t/send-contact-request)
       :full-name           full-name
       :profile-picture     profile-picture
       :customization-color customization-color}]
     [quo/text {:style style/message-prompt-wrapper}
      (i18n/label :t/contact-request-message-prompt)]
     [rn/view {:style style/message-input-wrapper}
      [quo/input
       {:type                  :text
        :ref                   #(reset! input-ref %)
        :multiline?            true
        :char-limit            constants/contact-request-message-max-length
        :max-length            constants/contact-request-message-max-length
        :placeholder           (i18n/label :t/type-something)
        :auto-focus            true
        :accessibility-label   :contact-request-message
        :label                 (i18n/label :t/message)
        :value                 @message-sub
        :on-change-text        on-message-change
        :container-style       {:flex-shrink 1}
        :input-container-style {:flex-shrink 1}}]]
     [quo/bottom-actions
      {:container-style  {:style {:flex 1}}
       :actions          :two-actions
       :button-one-props {:disabled?           (string/blank? message)
                          :accessibility-label :send-contact-request
                          :customization-color customization-color
                          :on-press            on-message-submit}
       :button-one-label (i18n/label :t/send-contact-request)
       :button-two-props {:accessibility-label :test-button
                          :customization-color :danger
                          :on-press            (bind on-press-test)}
       :button-two-label "test"}]]))


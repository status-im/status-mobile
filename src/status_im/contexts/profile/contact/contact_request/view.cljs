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

;; ---

(def contact-request-message-store
  (reagent.ratom/atom {}))

(re-frame/reg-sub
 :contact-request-message
 (fn []
   [contact-request-message-store])
 (fn [[store] [_ id]]
   (get store id "")))

(re-frame/reg-fx
 :store/set-contact-request-message
 (fn [[id message]]
   (swap! contact-request-message-store assoc id message)))

(re-frame/reg-event-fx
 :ui/set-contact-request-message
 (fn [_cofx [id message]]
   {:fx [[:store/set-contact-request-message [id message]]]}))

;; ---

(defn make-state-ref
  [state-ratom label]
  (let [state-ref (atom nil)]
    (js/console.log "init make-state-ref" label)
    (let [reaction-sub (reagent.ratom/track!
                        (fn [state-ratom]
                          (js/console.log "reagent reaction" label)
                          (let [state @state-ratom]
                            (js/console.log
                             (if (nil? @state-ref)
                               "state-ref init"
                               "state-ref update")
                             label
                             (clj->js state))
                            (reset! state-ref state)
                            state))
                        state-ratom)]
      @reaction-sub
      {:sub reaction-sub
       :ref state-ref})))

(defn make-state-handler-factory [state-ref]
  (rn/use-callback
   (memoize
    (fn [callback]
      (fn [event]
        (callback @state-ref event))))
   [state-ref]))

(defn make-snapshot-handler-factory [state-ref]
  (rn/use-callback
   (let [snapshot @state-ref]
     (fn [handler]
       ((memoize
         (fn [callback]
           (fn [event]
             (callback snapshot event))))
        handler)))
   [@state-ref]))

(defn make-snapshot-state-handler-factory [state-ref]
  (rn/use-callback
   (let [snapshot @state-ref]
     (fn [handler]
       ((memoize
         (fn [callback]
           (fn [event]
             (callback snapshot @state-ref event))))
        handler)))
   [@state-ref]))

(defn use-bind-sub [state-sub]
  (let [{:keys [ref sub]}
        (rn/use-memo (fn []
                       (make-state-ref state-sub "bind-sub"))
                     [state-sub])]
    (rn/use-unmount (fn []
                      (js/console.log "unmount bind-sub")
                      (reagent.ratom/dispose! sub)))
    (make-state-handler-factory ref)))

(defn use-bind-sub-snapshot [state-sub]
  (let [{:keys [ref sub]}
        (rn/use-memo (fn []
                       (make-state-ref state-sub "bind-sub-snapshot"))
                     [state-sub])]
    (rn/use-unmount (fn []
                      (js/console.log "unmount bind-sub-snapshot")
                      (reagent.ratom/dispose! sub)))
    (make-snapshot-state-handler-factory ref)))

(defn use-bind-state [state]
  (let [state-ref (rn/use-ref-atom state)]
    (reset! state-ref state)
    (make-snapshot-handler-factory state-ref)))

;; ---

(defn on-message-submit
  ([state _event]
   (js/console.log "on-press state" (clj->js state))
   (let [{:keys [public-key message]} state]
    ;;  (rf/dispatch [:hide-bottom-sheet])
    ;;  (rf/dispatch [:contact.ui/send-contact-request
    ;;                public-key message])
     (rf/dispatch [:toasts/upsert
                   {:id   :send-contact-request
                    :type :positive
                    :text message}]))))

(defn combine-subs-helper [subs-map]
  (reduce (fn [result [name sub]]
            (assoc result name @sub))
          {}
          subs-map))

(defn combine-subs [subs-map]
  (reagent.ratom/track combine-subs-helper subs-map))

(defn view
  []
  (let [profile-sub           (re-frame/subscribe [:contacts/current-contact])
        {:keys [public-key customization-color]
         :as   profile}       (deref profile-sub)
        customization-color   customization-color
        full-name             (profile.utils/displayed-name profile)
        profile-picture       (profile.utils/photo profile)
        input-ref             (rn/use-ref-atom nil)
        message-sub           (re-frame/subscribe [:contact-request-message public-key])
        [message set-message] (rn/use-state "")
        on-message-change     (rn/use-callback #(do
                                                  (rf/dispatch-sync [:ui/set-contact-request-message public-key %])
                                                  (set-message %)))
        state-sub             (combine-subs {:message message-sub
                                             :public-key (reagent.ratom/cursor
                                                          profile-sub
                                                          [:public-key])})
        bind-sub              (use-bind-sub state-sub)
        bind-sub-alt          (use-bind-sub state-sub)
        bind-state            (use-bind-state {:public-key public-key
                                               :message message})]
    (rn/use-unmount
     (fn []
       (rf/dispatch [:ui/set-contact-request-message public-key ""])))
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
                          :on-press            (bind-sub-alt on-message-submit)}
       :button-one-label "Test Button One"
       :button-two-props {:accessibility-label :test-button
                          :customization-color :danger
                          :on-press            (bind-sub on-message-submit)}
       :button-two-label "Test Button Two"}]]))


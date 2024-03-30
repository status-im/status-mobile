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

(def ^:private lookup-sentinel (js-obj))

(defn memo
  "Returns a memoized version of a referentially transparent function. The
  memoized version of the function keeps a cache of the mapping from arguments
  to results and, when calls with the same arguments are repeated often, has
  higher performance at the expense of higher memory use."
  [mem f]
  (fn [& args]
    (js/console.log "lookup")
    (let [v (get @mem args lookup-sentinel)]
      (if (identical? v lookup-sentinel)
        (let [ret (apply f args)]
          (js/console.log "swap")
          (swap! mem assoc args ret)
          ret)
        v))))

(defn make-state-ref
  [state-ratom]
  (let [state-ref (atom nil)]
    (js/console.log "init make-state-ref")
    (let [reaction-sub
          (reagent.ratom/track!
           (fn [state-ratom]
             (js/console.log "reagent reaction")
             (let [state @state-ratom]
               (js/console.log
                (if (nil? @state-ref)
                  "state-ref init"
                  "state-ref update")
                (clj->js state))
               (reset! state-ref state)
               state))
           state-ratom)]
      @reaction-sub
      {:sub reaction-sub
       :ref state-ref})))

(defn make-state-handler-factory [state-ref]
  (let [storage (rn/use-memo #(atom {}) [state-ref])
        factory (rn/use-callback
                 (memo storage
                       (fn [callback]
                         (print "make state handler")
                         (fn [event]
                           (callback @state-ref event))))
                 [state-ref storage])]
    {:factory factory
     :storage storage}))

(defn make-snapshot-handler-factory [snapshot]
  (let [storage (rn/use-memo #(atom {}) [])
        capture (rn/use-memo #(atom snapshot) [])
        factory (rn/use-memo
                 (do (swap! capture (fn [_] snapshot))
                     #(memo storage
                            (fn [callback]
                              (fn [event]
                                (callback @capture event)))))
                 [storage capture])]
    {:factory factory
     :storage storage}))

(defn make-past-present-state-handler-factory [state-ref]
  (let [storage (rn/use-memo #(atom {}) [state-ref])
        capture (rn/use-memo #(atom @state-ref) [state-ref])
        factory (rn/use-memo
                 (fn []
                   (fn [handler]
                     (reset! capture @state-ref)
                     ((memo storage
                            (fn [callback]
                              (fn [event]
                                (callback @capture @state-ref event))))
                      handler)))
                 [state-ref storage])]
    {:factory factory
     :storage storage}))

(defn use-bind-sub [state-sub]
  (let [{:keys [ref sub]}
        (rn/use-memo (fn []
                       (make-state-ref state-sub))
                     [state-sub])
        {:keys [factory storage]}
        (make-state-handler-factory ref)]
    (rn/use-unmount (fn []
                      (js/console.log "unmount bind-sub")
                      (prn "unmount storage" storage)
                      (reagent.ratom/dispose! sub)))
    factory))

(defn use-bind-past-present-sub [state-sub]
  (let [{:keys [ref sub]}
        (rn/use-memo (fn []
                       (make-state-ref state-sub))
                     [state-sub])
        {:keys [factory storage]}
        (make-past-present-state-handler-factory ref)]
    (rn/use-unmount (fn []
                      (js/console.log "unmount bind-sub-and-snapshot")
                      (prn "unmount storage" storage)
                      (reagent.ratom/dispose! sub)))
    factory))

(defn use-bind-snapshot [snapshot]
  (let [{:keys [factory storage]}
        (make-snapshot-handler-factory snapshot)]
    (rn/use-unmount (fn []
                      (js/console.log "unmount bind-snapshot")
                      (prn "unmount storage" storage)))
    factory))

;; ---

(defn on-message-submit
  ([snapshot state _event]
   (js/console.log "on-press snapshot" (clj->js snapshot))
   (js/console.log "on-press state" (clj->js state))
   (let [{:keys [public-key message]} state]
    ;;  (rf/dispatch [:hide-bottom-sheet])
    ;;  (rf/dispatch [:contact.ui/send-contact-request
    ;;                public-key message])
     (rf/dispatch [:toasts/upsert
                   {:id   :send-contact-request
                    :type :positive
                    :text message}])))
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
        bind-past-present-sub (use-bind-past-present-sub state-sub)
        bind-snapshot         (use-bind-snapshot {:public-key public-key
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
       :actions          :one-action
       :button-one-props {:disabled?           (string/blank? message)
                          :accessibility-label :send-contact-request
                          :customization-color :purple
                          :on-press            (bind-past-present-sub on-message-submit)}
       :button-one-label "Past Present Sub"}]
     [quo/bottom-actions
      {:container-style  {:style {:flex 1}}
       :actions          :two-actions
       :button-one-props {:accessibility-label :send-contact-request
                          :customization-color :blue
                          :on-press            (bind-snapshot on-message-submit)}
       :button-one-label "Snapshot"
       :button-two-props {:accessibility-label :test-button
                          :customization-color :orange
                          :on-press            (bind-sub on-message-submit)}
       :button-two-label "Sub"}]]))


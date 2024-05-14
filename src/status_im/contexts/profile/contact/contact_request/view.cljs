(ns status-im.contexts.profile.contact.contact-request.view
  (:require [clojure.string :as string]
            [quo.core :as quo]
            [react-native.core :as rn]
            [react-native.platform :as platform]
            [reagent.ratom]
            [status-im.constants :as constants]
            [status-im.contexts.profile.contact.contact-request.style :as style]
            [status-im.contexts.profile.utils :as profile.utils]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

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

(defn make-snapshot-handler-factory
  [snapshot]
  (let [storage (rn/use-memo #(atom {}) [])
        capture (rn/use-memo #(atom snapshot) [])
        _update (rn/use-layout-effect
                 (fn []
                   (swap! capture (fn [_] snapshot)))
                 snapshot)
        factory (rn/use-memo
                 (fn []
                   (fn [handler]
                     ((memo storage
                            (fn [callback]
                              (fn [event]
                                (callback @capture event))))
                      handler)))
                 [storage capture])]
    {:factory factory
     :storage storage}))

(defn use-bind-snapshot
  [snapshot]
  (let [{:keys [factory storage]}
        (make-snapshot-handler-factory snapshot)]
    (rn/use-unmount (fn []
                      (js/console.log "unmount bind-snapshot")
                      (prn "unmount storage" storage)))
    factory))

;; ---

(defn on-message-submit
  [state _event]
  (js/console.log "on-press state" (clj->js state))
  (let [{:keys [public-key message]} state]
    ;;  (rf/dispatch [:hide-bottom-sheet])
    ;;  (rf/dispatch [:contact.ui/send-contact-request
    ;;                public-key message])
    (rf/dispatch [:toasts/upsert
                  {:id   :send-contact-request
                   :type :positive
                   :text message}])))

(defn on-message-change
  [{:keys [set-message]} message-text]
  (set-message message-text))

(defn use-event
  [f scope]
  (let [bind (use-bind-snapshot scope)]
    (bind f)))

(defn use-events
  [fs scope]
  (let [bind (use-bind-snapshot scope)]
    (->> fs
         (reduce-kv
          (fn [acc label f]
            (assoc acc label (bind f)))
          {}))))

(defn view
  []
  (let [{:keys [public-key customization-color]
         :as   profile}       (rf/sub [:contacts/current-contact])
        full-name             (profile.utils/displayed-name profile)
        profile-picture       (profile.utils/photo profile)
        input-ref             (rn/use-ref-atom nil)
        [message set-message] (rn/use-state "")
        on-change             (use-event on-message-change
                                         {:set-message set-message})
        on-submit             (use-event on-message-submit
                                         {:public-key public-key
                                          :message    message})]
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
        :on-change-text        on-change
        :container-style       {:flex-shrink 1}
        :input-container-style {:flex-shrink 1}}]]
     [quo/bottom-actions
      {:container-style  {:style {:flex 1}}
       :actions          :one-action
       :button-one-props {:disabled?           (string/blank? message)
                          :accessibility-label :send-contact-request
                          :customization-color :purple
                          :on-press            on-submit}
       :button-one-label "Past Present Sub"}]
     [quo/bottom-actions
      {:container-style  {:style {:flex 1}}
       :actions          :two-actions
       :button-one-props {:accessibility-label :send-contact-request
                          :customization-color :blue
                          :on-press            on-submit}
       :button-one-label "Snapshot"
       :button-two-props {:accessibility-label :test-button
                          :customization-color :orange
                          :on-press            on-submit}
       :button-two-label "Sub"}]]))


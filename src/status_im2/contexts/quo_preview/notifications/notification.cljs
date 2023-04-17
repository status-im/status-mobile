(ns status-im2.contexts.quo-preview.notifications.notification
  (:require [quo2.components.avatars.user-avatar.view :as user-avatar]
            [quo2.components.buttons.button :as button]
            [quo2.components.code.snippet :as snippet]
            [quo2.components.info.info-message :as info-message]
            [react-native.core :as rn]
            [status-im2.common.resources :as resources]
            [status-im2.contexts.quo-preview.code.snippet :as snippet-preview]
            [utils.re-frame :as rf]))

(defn notification-button
  ([id opts] (notification-button id id opts))
  ([text id opts]
   (let [notification-opts (rf/sub [:toasts/toast id])
         dismiss!          #(rf/dispatch [:toasts/close id])
         notification!     #(rf/dispatch [:toasts/upsert (assoc opts :id id)])
         dismissed?        (not notification-opts)]
     [rn/view {:style {:margin-bottom 10}}
      [button/button
       {:size     32
        :on-press #(if dismissed? (notification!) (dismiss!))}
       (if dismissed? text (str "DISMISS " text))]])))

(defn notification-button-0
  []
  [notification-button
   "Notification: empty"
   {:duration 4000
    :type     :notification}])

(defn notification-button-1
  []
  [notification-button
   "Notification: with title(header)"
   {:avatar       [user-avatar/user-avatar
                   {:full-name           "A Y"
                    :status-indicator?   true
                    :online?             true
                    :size                :small
                    :customization-color :blue
                    :ring-background     (resources/get-mock-image :ring)}]
    :title        "Alisher Yakupov accepted your contact request"
    :duration     4000
    :title-weight :medium
    :type         :notification}])

(defn notification-button-2
  []
  [notification-button
   "with title and body"
   {:avatar   [user-avatar/user-avatar
               {:full-name           "A Y"
                :status-indicator?   true
                :online?             true
                :size                :small
                :customization-color :blue
                :ring-background     (resources/get-mock-image :ring)}]
    :title    "Default to semibold title"
    :text     "The quick brown fox jumped over the lazy dog and ate a potatoe."
    :duration 4000
    :type     :notification}])

(defn notification-button-3
  []
  [notification-button
   "with anything as header & body"
   {:avatar   [user-avatar/user-avatar
               {:full-name           "A Y"
                :status-indicator?   true
                :online?             true
                :size                :small
                :customization-color :blue
                :ring-background     (resources/get-mock-image :ring)}]
    :header   [rn/view
               [info-message/info-message
                {:type :success
                 :size :tiny
                 :icon :i/placeholder}
                "info-message as title"]]
    :body     [snippet/snippet {:language :clojure :max-lines 15 :syntax true}
               snippet-preview/clojure-example]
    :duration 3000
    :type     :notification}])

(defn preview
  []
  (fn []
    [rn/view
     [rn/view
      {:background-color "#508485"
       :flex-direction   :column
       :justify-content  :flex-start
       :height           300}]
     [into
      [rn/view
       {:flex       1
        :padding    16
        :margin-top 60}]
      [^{:key :0} [notification-button-0]
       ^{:key :1} [notification-button-1]
       ^{:key :2} [notification-button-2]
       ^{:key :3} [notification-button-3]]
     ]]))

(defn preview-notification
  []
  [rn/view {:flex 1}
   [rn/flat-list
    {:flex                         1
     :header                       [preview]
     :key-fn                       str
     :keyboard-should-persist-taps :always}]])

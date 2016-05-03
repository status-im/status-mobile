(ns syng-im.android.core
  (:require-macros
    [natal-shell.back-android :refer [add-event-listener remove-event-listener]])
  (:require [reagent.core :as r :refer [atom]]
            [cljs.core :as cljs]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.handlers]
            [syng-im.subs]
            [syng-im.components.react :refer [navigator app-registry]]
            [syng-im.components.contact-list.contact-list :refer [contact-list]]
            [syng-im.components.discovery.discovery :refer [discovery]]
            [syng-im.components.chat :refer [chat]]
            [syng-im.components.sign-up :refer [sign-up-view]]
            [syng-im.components.sign-up-confirm :refer [sign-up-confirm-view]]
            [syng-im.components.chats.chats-list :refer [chats-list]]
            [syng-im.components.chats.new-group :refer [new-group]]
            [syng-im.components.chat.new-participants :refer [new-participants]]
            [syng-im.components.chat.remove-participants :refer [remove-participants]]
            [syng-im.utils.logging :as log]
            [syng-im.navigation :as nav]
            [syng-im.utils.encryption]))

(def back-button-handler (cljs/atom {:nav     nil
                                     :handler nil}))

(defn init-back-button-handler! [nav]
  (let [handler @back-button-handler]
    (when-not (= nav (:nav handler))
      (remove-event-listener "hardwareBackPress" (:handler handler))
      (let [new-listener (fn []
                           (binding [nav/*nav-render* false]
                             (when (< 1 (.-length (.getCurrentRoutes nav)))
                               (nav/nav-pop nav)
                               true)))]
        (reset! back-button-handler {:nav     nav
                                     :handler new-listener})
        (add-event-listener "hardwareBackPress" new-listener)))))

(defn app-root []
  [navigator {:initial-route (clj->js {:view-id :discovery})
              :render-scene  (fn [route nav]
                               (log/debug "route" route)
                               (when true                   ;; nav/*nav-render*
                                 (let [{:keys [view-id]} (js->clj route :keywordize-keys true)
                                       view-id (keyword view-id)]
                                   (init-back-button-handler! nav)
                                   (case view-id
                                     :discovery (r/as-element [discovery {:navigator nav}])
                                     :add-participants (r/as-element [new-participants {:navigator nav}])
                                     :remove-participants (r/as-element [remove-participants {:navigator nav}])
                                     :chat-list (r/as-element [chats-list {:navigator nav}])
                                     :new-group (r/as-element [new-group {:navigator nav}])
                                     :contact-list (r/as-element [contact-list {:navigator nav}])
                                     :chat (r/as-element [chat {:navigator nav}])
                                     :sign-up (r/as-element [sign-up-view {:navigator nav}])
                                     :sign-up-confirm (r/as-element [sign-up-confirm-view {:navigator nav}])))))}])

(defn init []
  (dispatch-sync [:initialize-db])
  (dispatch [:initialize-crypt])
  (dispatch [:initialize-protocol])
  (dispatch [:load-user-phone-number])
  (dispatch [:load-syng-contacts])
  ;; TODO execute on first run only
  ;; (dispatch [:set-sign-up-chat])
  (.registerComponent app-registry "SyngIm" #(r/reactify-component app-root)))
